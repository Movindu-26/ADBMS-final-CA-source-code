package com.example.hotel.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.hotel.entity.ReservationStatus;
import com.example.hotel.entity.Room;
import com.example.hotel.entity.RoomStatus;
import com.example.hotel.entity.RoomType;

import jakarta.persistence.LockModeType;

public interface RoomRepository extends JpaRepository<Room, Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(@Param("id") Long id);

    List<Room> findByBranchCode(String branchCode);

    List<Room> findByRoomType(RoomType roomType);

    List<Room> findByBranchCodeAndRoomType(String branchCode, RoomType roomType);

    boolean existsByBranchCodeAndRoomNumber(String branchCode, String roomNumber);

    /** Temporal query: active rooms of a branch with no blocking stay that overlaps [checkIn, checkOut). */
    @Query("""
            select r from Room r
            where r.branchCode = :branch
              and r.status = :activeStatus
              and not exists (
                  select x.id from Reservation x
                  where x.room.id = r.id
                    and x.status in :blocking
                    and x.checkInDate < :checkOut
                    and x.checkOutDate > :checkIn)
            order by r.roomNumber
            """)
    List<Room> findAvailable(@Param("branch") String branch,
                             @Param("activeStatus") RoomStatus activeStatus,
                             @Param("blocking") Collection<ReservationStatus> blocking,
                             @Param("checkIn") LocalDate checkIn,
                             @Param("checkOut") LocalDate checkOut);
}
