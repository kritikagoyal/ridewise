package com.ridewise.exception;

/**
 * Thrown when an entity (Rider, Driver, Ride) is not found.
 */
public class EntityNotFoundException extends RideSharingException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
