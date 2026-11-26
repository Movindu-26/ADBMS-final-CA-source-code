package com.example.hotel.service;

import com.example.hotel.dto.CreateReservationRequest;
import com.example.hotel.dto.ModifyReservationRequest;
import com.example.hotel.dto.ReservationResponse;
import com.example.hotel.entity.*;
import com.example.hotel.exception.BusinessRuleException;
import com.example.hotel.exception.ConflictException;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.exception.SimulatedFailureException;
import com.example.hotel.repository.GuestRepository;
import com.example.hotel.repository.ReservationRepository;
import com.example.hotel.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Transactions of the system (Section A):
 *   T1 create reservation   T2 modify reservation   T3 cancel reservation
 *
 * Every public write method is ONE database transaction (@Transactional): either all of its writes
 * are committed together, or all are rolled back (atomicity).
 *
 * IMPORTANT ordering rule: the first database access in a booking transaction must be the locking
 * read (SELECT ... FOR UPDATE). MySQL's default isolation level is REPEATABLE READ, whose snapshot is
 * created by the first NON-locking read. If we read first and lock afterwards, the overlap check could
 * use an old snapshot and miss a booking that another transaction committed while we waited.
 */
@Service
@Transactional
public class ReservationService {

    /** Stays with these statuses block the room. */
    private static final List<ReservationStatus> BLOCKING =
            List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);
    private static final int POINTS_PER_NIGHT = 10;
    private static final long NO_EXCLUDE = 0L;   // ids start at 1

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final boolean lockingEnabled;
    private final long demoDelayMs;

    public ReservationService(ReservationRepository reservationRepository,
                              RoomRepository roomRepository,
                              GuestRepository guestRepository,
                              @Value("${booking.locking.enabled:true}") boolean lockingEnabled,
                              @Value("${booking.demo.delay-ms:0}") long demoDelayMs) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.lockingEnabled = lockingEnabled;
        this.demoDelayMs = demoDelayMs;
    }

    // ------------------------------------------------------------------
    // T1: create reservation
    // ------------------------------------------------------------------
    @Transactional(rollbackFor = Exception.class)
    public ReservationResponse create(CreateReservationRequest req, boolean simulateFailure) {
        validateStay(req.checkInDate(), req.checkOutDate());

        // 1. lock the room row (serialises all bookings of this room)
        Room room = lockRoom(req.roomId());

        Guest guest = guestRepository.findById(req.guestId())
                .orElseThrow(() -> new ResourceNotFoundException("Guest " + req.guestId() + " not found"));

        // 2. business rules
        checkRoomRules(room, req.numGuests());

        // 3. overlap check (temporal predicate)
        long clashes = reservationRepository.countOverlapping(room.getId(), BLOCKING,
                req.checkInDate(), req.checkOutDate(), NO_EXCLUDE);
        if (clashes > 0) {
            throw new ConflictException("Room " + room.getBranchCode() + "-" + room.getRoomNumber()
                    + " is already booked between " + req.checkInDate() + " and " + req.checkOutDate());
        }

        pauseForDemo();   // only used to make the race visible when locking is switched off

        long nights = nights(req.checkInDate(), req.checkOutDate());

        // 4. write 1: loyalty points on the guest (atomic UPDATE inside the database).
        //    Done BEFORE the insert on purpose: it takes the exclusive lock on the guest row first, so the
        //    foreign-key check of the INSERT cannot deadlock with another booking by the same guest.
        guestRepository.addLoyaltyPoints(guest.getId(), (int) (nights * POINTS_PER_NIGHT));

        // 5. write 2: insert the reservation
        Reservation reservation = new Reservation();
        reservation.setGuest(guest);
        reservation.setRoom(room);
        reservation.setCheckInDate(req.checkInDate());
        reservation.setCheckOutDate(req.checkOutDate());
        reservation.setNumGuests(req.numGuests());
        reservation.setTotalAmount(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation = reservationRepository.saveAndFlush(reservation);

        // Demo only: fail AFTER both writes. Spring rolls back both, so nothing is saved.
        if (simulateFailure) {
            throw new SimulatedFailureException(
                    "Simulated failure after both writes - the whole transaction was rolled back");
        }
        return ReservationResponse.from(reservation);
    }

    // ------------------------------------------------------------------
    // T2: modify reservation (new dates / number of guests)
    // ------------------------------------------------------------------
    @Transactional(rollbackFor = Exception.class)
    public ReservationResponse modify(Long id, ModifyReservationRequest req) {
        validateStay(req.checkInDate(), req.checkOutDate());

        // locking reads first (no snapshot yet): the reservation row, then its room row
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation " + id + " not found"));
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Only CONFIRMED reservations can be modified");
        }
        Room room = lockRoom(reservation.getRoom().getId());
        checkRoomRules(room, req.numGuests());

        long clashes = reservationRepository.countOverlapping(room.getId(), BLOCKING,
                req.checkInDate(), req.checkOutDate(), reservation.getId());
        if (clashes > 0) {
            throw new ConflictException("Room is already booked for the new dates");
        }

        long oldNights = nights(reservation.getCheckInDate(), reservation.getCheckOutDate());
        long newNights = nights(req.checkInDate(), req.checkOutDate());

        reservation.setCheckInDate(req.checkInDate());
        reservation.setCheckOutDate(req.checkOutDate());
        reservation.setNumGuests(req.numGuests());
        reservation.setTotalAmount(room.getPricePerNight().multiply(BigDecimal.valueOf(newNights)));
        reservation = reservationRepository.saveAndFlush(reservation);

        int delta = (int) ((newNights - oldNights) * POINTS_PER_NIGHT);
        if (delta != 0) {
            guestRepository.addLoyaltyPoints(reservation.getGuest().getId(), delta);
        }
        return ReservationResponse.from(reservation);
    }

    // ------------------------------------------------------------------
    // T3: cancel reservation
    // ------------------------------------------------------------------
    @Transactional(rollbackFor = Exception.class)
    public ReservationResponse cancel(Long id) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation " + id + " not found"));
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BusinessRuleException("Reservation is already cancelled");
        }
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessRuleException("Only CONFIRMED reservations can be cancelled");
        }
        long nights = nights(reservation.getCheckInDate(), reservation.getCheckOutDate());

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(LocalDateTime.now());
        reservation = reservationRepository.saveAndFlush(reservation);

        guestRepository.addLoyaltyPoints(reservation.getGuest().getId(), (int) (-nights * POINTS_PER_NIGHT));
        return ReservationResponse.from(reservation);
    }

    // ------------------------------------------------------------------
    // reads
    // ------------------------------------------------------------------
    @Transactional(readOnly = true)
    public ReservationResponse get(Long id) {
        return reservationRepository.findById(id).map(ReservationResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> list(Long guestId) {
        List<Reservation> list = (guestId == null)
                ? reservationRepository.findAll()
                : reservationRepository.findByGuest_IdOrderByCheckInDateDesc(guestId);
        return list.stream().map(ReservationResponse::from).toList();
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    /** Pessimistic lock on the room row, or a plain read when locking is switched off (baseline demo). */
    private Room lockRoom(Long roomId) {
        return (lockingEnabled ? roomRepository.findByIdForUpdate(roomId) : roomRepository.findById(roomId))
                .orElseThrow(() -> new ResourceNotFoundException("Room " + roomId + " not found"));
    }

    private void validateStay(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BusinessRuleException("checkOutDate must be after checkInDate");
        }
    }

    private void checkRoomRules(Room room, int numGuests) {
        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new BusinessRuleException("Room is not available (status " + room.getStatus() + ")");
        }
        if (numGuests > room.getCapacity()) {
            throw new BusinessRuleException("Room capacity is " + room.getCapacity()
                    + " but " + numGuests + " guests were requested");
        }
    }

    private long nights(LocalDate checkIn, LocalDate checkOut) {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    private void pauseForDemo() {
        if (demoDelayMs > 0) {
            try {
                Thread.sleep(demoDelayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
