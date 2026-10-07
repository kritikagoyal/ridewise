package com.ridewise.exception;

/**
 * Thrown when an invalid domain operation or illegal state transition is attempted.
 */
public class InvalidOperationException extends RideSharingException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
