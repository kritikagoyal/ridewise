package com.ridewise.exception;

/**
 * Thrown when no eligible driver is available to accept a ride.
 */
public class NoDriverAvailableException extends RideSharingException {
    public NoDriverAvailableException(String message) {
        super(message);
    }
}
