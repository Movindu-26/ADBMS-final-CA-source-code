package com.example.hotel.exception;

/** Thrown on purpose (demo only) after the database writes, to prove that the transaction rolls back. */
public class SimulatedFailureException extends RuntimeException {
    public SimulatedFailureException(String message) { super(message); }
}
