package com.example.hotel.repository;

import com.example.hotel.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    boolean existsByEmail(String email);

    Optional<Guest> findByEmail(String email);

    /**
     * Atomic update done inside the database ("points = points + delta"), so two bookings by the
     * same guest can never overwrite each other's points (no lost update). Never goes below zero.
     */
    @Modifying(flushAutomatically = true)
    @Query("""
            update Guest g
            set g.loyaltyPoints = case when g.loyaltyPoints + :delta < 0 then 0
                                       else g.loyaltyPoints + :delta end
            where g.id = :id
            """)
    int addLoyaltyPoints(@Param("id") Long id, @Param("delta") int delta);
}
