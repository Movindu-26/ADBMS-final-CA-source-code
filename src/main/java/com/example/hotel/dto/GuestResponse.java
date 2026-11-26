package com.example.hotel.dto;

import com.example.hotel.entity.Guest;

import java.time.LocalDateTime;

public record GuestResponse(Long id, String fullName, String email, String phone, String idNumber,
                            Integer loyaltyPoints, LocalDateTime createdAt) {

    public static GuestResponse from(Guest g) {
        return new GuestResponse(g.getId(), g.getFullName(), g.getEmail(), g.getPhone(), g.getIdNumber(),
                g.getLoyaltyPoints(), g.getCreatedAt());
    }
}
