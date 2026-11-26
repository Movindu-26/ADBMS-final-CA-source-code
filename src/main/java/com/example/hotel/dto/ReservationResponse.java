package com.example.hotel.dto;

import com.example.hotel.entity.Reservation;
import com.example.hotel.entity.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationResponse(Long id, Long guestId, Long roomId, LocalDate checkInDate,
                                  LocalDate checkOutDate, Integer numGuests, BigDecimal totalAmount,
                                  ReservationStatus status, LocalDateTime createdAt,
                                  LocalDateTime cancelledAt) {

    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(r.getId(), r.getGuest().getId(), r.getRoom().getId(),
                r.getCheckInDate(), r.getCheckOutDate(), r.getNumGuests(), r.getTotalAmount(),
                r.getStatus(), r.getCreatedAt(), r.getCancelledAt());
    }
}
