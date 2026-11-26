package com.example.hotel.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record ModifyReservationRequest(
        @NotNull(message = "checkInDate is required (yyyy-MM-dd)")
        @FutureOrPresent(message = "checkInDate cannot be in the past")
        LocalDate checkInDate,

        @NotNull(message = "checkOutDate is required (yyyy-MM-dd)")
        LocalDate checkOutDate,

        @NotNull(message = "numGuests is required")
        @Positive(message = "numGuests must be greater than 0")
        Integer numGuests
) {
}
