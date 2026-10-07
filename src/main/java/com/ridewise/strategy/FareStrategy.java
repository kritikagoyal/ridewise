package com.ridewise.strategy;

import com.ridewise.model.Ride;

/**
 * Strategy interface for calculating the fare of a ride.
 */
public interface FareStrategy {

    /**
     * Calculates the monetary fare for the given ride.
     *
     * @param ride the ride whose fare is to be calculated
     * @return the total fare amount
     */
    double calculateFare(Ride ride);
}
