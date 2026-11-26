package com.example.hotel.exception;

/** A rule of the hotel was broken (bad dates, room in maintenance, too many guests...). Maps to HTTP 400. */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) { super(message); }
}
