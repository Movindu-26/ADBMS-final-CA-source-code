package com.example.hotel.dto;

import jakarta.validation.constraints.*;

public record GuestRequest(
        @NotBlank(message = "fullName is required")
        @Size(max = 100, message = "fullName must be at most 100 characters")
        String fullName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        @Size(max = 100, message = "email must be at most 100 characters")
        String email,

        @NotBlank(message = "phone is required")
        @Pattern(regexp = "^[0-9+\\- ]{7,20}$", message = "phone must be 7-20 characters (digits, +, -, space)")
        String phone,

        @NotBlank(message = "idNumber is required")
        @Size(max = 30, message = "idNumber must be at most 30 characters")
        String idNumber
) {
}
