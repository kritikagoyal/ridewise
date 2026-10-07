package com.ridewise.exception;

/**
 * Base exception for RideWise domain errors.
 */
public class RideSharingException extends RuntimeException {
    public RideSharingException(String message) {
        super(message);
    }

    public RideSharingException(String message, Throwable cause) {
        super(message, cause);
    }
}
