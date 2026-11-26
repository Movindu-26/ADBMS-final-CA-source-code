package com.example.hotel.exception;

/** Double booking, duplicate data, related records exist... Maps to HTTP 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
