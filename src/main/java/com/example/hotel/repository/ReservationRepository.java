package com.example.hotel.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hotel.entity.Reservation;
import com.example.hotel.entity.ReservationStatus;

import jakarta.persistence.LockModeType;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** "SELECT ... FOR UPDATE" on one reservation row (used by modify and cancel). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

    @Query("""
            select count(r) from Reservation r
            where r.room.id = :roomId
              and r.status in :statuses
              and r.checkInDate < :checkOut
              and r.checkOutDate > :checkIn
              and r.id <> :excludeId
            """)
    long countOverlapping(@Param("roomId") Long roomId,
                          @Param("statuses") Collection<ReservationStatus> statuses,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut,
                          @Param("excludeId") Long excludeId);

    List<Reservation> findByGuest_IdOrderByCheckInDateDesc(Long guestId);

    List<Reservation> findByRoom_Id(Long roomId);

    boolean existsByRoom_Id(Long roomId);

    boolean existsByGuest_Id(Long guestId);
}
