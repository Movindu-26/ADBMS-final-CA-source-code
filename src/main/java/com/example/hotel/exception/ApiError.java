package com.example.hotel.exception;

import java.util.Map;

/** Same JSON shape for every error response. */
public record ApiError(String timestamp, int status, String error, String message, Map<String, String> fieldErrors) {
}
