package com.example.hotel.dto;

import com.example.hotel.entity.RoomStatus;
import com.example.hotel.entity.RoomType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank(message = "branchCode is required")
        @Pattern(regexp = "^[A-Za-z]{2,10}$", message = "branchCode must be 2-10 letters (e.g. COL, KDY, GAL)")
        String branchCode,

        @NotBlank(message = "roomNumber is required")
        @Size(max = 10, message = "roomNumber must be at most 10 characters")
        String roomNumber,

        @NotNull(message = "roomType is required (SINGLE, DOUBLE or SUITE)")
        RoomType roomType,

        @NotNull(message = "capacity is required")
        @Positive(message = "capacity must be greater than 0")
        Integer capacity,

        @NotNull(message = "pricePerNight is required")
        @DecimalMin(value = "0.01", message = "pricePerNight must be greater than 0")
        @Digits(integer = 8, fraction = 2, message = "pricePerNight allows up to 8 digits and 2 decimals")
        BigDecimal pricePerNight,

        RoomStatus status   // optional, defaults to ACTIVE
) {
}
