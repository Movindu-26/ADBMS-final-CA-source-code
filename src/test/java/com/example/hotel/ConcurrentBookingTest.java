package com.example.hotel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.hotel.dto.CreateReservationRequest;
import com.example.hotel.entity.Guest;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomStatus;
import com.example.hotel.entity.RoomType;
import com.example.hotel.exception.ConflictException;
import com.example.hotel.repository.GuestRepository;
import com.example.hotel.repository.ReservationRepository;
import com.example.hotel.repository.RoomRepository;
import com.example.hotel.service.ReservationService;


@SpringBootTest(properties = {
        "booking.locking.enabled=true",
        "booking.demo.delay-ms=200"
})
class ConcurrentBookingTest {

    private static final int THREADS = 10;

    @Autowired ReservationService reservationService;
    @Autowired RoomRepository roomRepository;
    @Autowired GuestRepository guestRepository;
    @Autowired ReservationRepository reservationRepository;

    private Room room;
    private final List<Guest> guests = new ArrayList<>();

    @BeforeEach
    void setUp() {
        String unique = String.valueOf(System.nanoTime() % 1_000_000);
        room = new Room();
        room.setBranchCode("TST");
        room.setRoomNumber("C" + unique);
        room.setRoomType(RoomType.DOUBLE);
        room.setCapacity(2);
        room.setPricePerNight(new BigDecimal("10000.00"));
        room.setStatus(RoomStatus.ACTIVE);
        room = roomRepository.save(room);

        for (int i = 0; i < THREADS; i++) {
            Guest g = new Guest();
            g.setFullName("Test Guest " + i);
            g.setEmail("concurrent" + unique + "_" + i + "@test.example");
            g.setPhone("+94770000000");
            g.setIdNumber("TEST" + i);
            guests.add(guestRepository.save(g));
        }
    }

    @AfterEach
    void cleanUp() {
        reservationRepository.deleteAll(reservationRepository.findByRoom_Id(room.getId()));
        guestRepository.deleteAll(guests);
        roomRepository.delete(room);
    }

    @Test
    void tenGuestsBookTheSameRoomAtOnce_onlyOneSucceeds() throws Exception {
        LocalDate in = LocalDate.now().plusDays(30);
        LocalDate out = in.plusDays(3);

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        AtomicInteger unexpected = new AtomicInteger();

        for (Guest guest : guests) {
            pool.submit((Callable<Void>) () -> {
                ready.countDown();
                start.await();                       // all threads are released at the same moment
                try {
                    reservationService.create(
                            new CreateReservationRequest(guest.getId(), room.getId(), in, out, 2), false);
                    succeeded.incrementAndGet();
                } catch (ConflictException e) {
                    rejected.incrementAndGet();      // 409 in the REST API
                } catch (Exception e) {
                    unexpected.incrementAndGet();
                    e.printStackTrace();
                }
                return null;
            });
        }
        ready.await();
        start.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(60, TimeUnit.SECONDS)).isTrue();

        int saved = reservationRepository.findByRoom_Id(room.getId()).size();
        System.out.println("=== WITH LOCKING: succeeded=" + succeeded + ", rejected(409)=" + rejected
                + ", reservations saved in DB=" + saved);

        assertThat(unexpected.get()).isZero();
        assertThat(succeeded.get()).isEqualTo(1);
        assertThat(rejected.get()).isEqualTo(THREADS - 1);
        assertThat(saved).isEqualTo(1);
    }
}
