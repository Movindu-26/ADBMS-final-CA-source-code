package com.example.hotel.service;

import com.example.hotel.dto.RoomRequest;
import com.example.hotel.dto.RoomResponse;
import com.example.hotel.entity.ReservationStatus;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomStatus;
import com.example.hotel.entity.RoomType;
import com.example.hotel.exception.BusinessRuleException;
import com.example.hotel.exception.ConflictException;
import com.example.hotel.exception.ResourceNotFoundException;
import com.example.hotel.repository.ReservationRepository;
import com.example.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    public RoomResponse create(RoomRequest req) {
        String branch = req.branchCode().trim().toUpperCase();
        if (roomRepository.existsByBranchCodeAndRoomNumber(branch, req.roomNumber().trim())) {
            throw new ConflictException("Room " + req.roomNumber() + " already exists in branch " + branch);
        }
        Room room = new Room();
        apply(room, req);
        return RoomResponse.from(roomRepository.save(room));
    }

    @Transactional(readOnly = true)
    public RoomResponse get(Long id) {
        return RoomResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> list(String branch, RoomType type) {
        String b = (branch == null || branch.isBlank()) ? null : branch.trim().toUpperCase();
        List<Room> rooms;
        if (b != null && type != null) rooms = roomRepository.findByBranchCodeAndRoomType(b, type);
        else if (b != null) rooms = roomRepository.findByBranchCode(b);
        else if (type != null) rooms = roomRepository.findByRoomType(type);
        else rooms = roomRepository.findAll();
        return rooms.stream().map(RoomResponse::from).toList();
    }

    public RoomResponse update(Long id, RoomRequest req) {
        Room room = find(id);
        String branch = req.branchCode().trim().toUpperCase();
        String number = req.roomNumber().trim();
        boolean identityChanged = !room.getBranchCode().equals(branch) || !room.getRoomNumber().equals(number);
        if (identityChanged && roomRepository.existsByBranchCodeAndRoomNumber(branch, number)) {
            throw new ConflictException("Room " + number + " already exists in branch " + branch);
        }
        apply(room, req);
        return RoomResponse.from(roomRepository.saveAndFlush(room));
    }

    public void delete(Long id) {
        Room room = find(id);
        if (reservationRepository.existsByRoom_Id(id)) {
            throw new ConflictException(
                    "Room has reservations and cannot be deleted. Set its status to MAINTENANCE instead.");
        }
        roomRepository.delete(room);
    }

    /** Temporal query: which active rooms of a branch are free for [checkIn, checkOut)? */
    @Transactional(readOnly = true)
    public List<RoomResponse> findAvailable(String branch, LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BusinessRuleException("checkOut must be after checkIn");
        }
        return roomRepository.findAvailable(branch.trim().toUpperCase(), RoomStatus.ACTIVE,
                        List.of(ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN), checkIn, checkOut)
                .stream().map(RoomResponse::from).toList();
    }

    private Room find(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room " + id + " not found"));
    }

    private void apply(Room room, RoomRequest req) {
        room.setBranchCode(req.branchCode().trim().toUpperCase());
        room.setRoomNumber(req.roomNumber().trim());
        room.setRoomType(req.roomType());
        room.setCapacity(req.capacity());
        room.setPricePerNight(req.pricePerNight());
        room.setStatus(req.status() != null ? req.status() : RoomStatus.ACTIVE);
    }
}
