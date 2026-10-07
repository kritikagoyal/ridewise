package com.ridewise.strategy;

import com.ridewise.model.Driver;
import com.ridewise.model.Rider;

import java.util.List;

/**
 * Strategy interface for selecting a driver for a rider.
 */
public interface RideMatchingStrategy {

    /**
     * Finds and selects the best matching available driver for the given rider.
     *
     * @param rider   the rider requesting the ride
     * @param drivers list of candidate drivers
     * @return the selected driver, or null if no eligible driver is found
     */
    Driver findDriver(Rider rider, List<Driver> drivers);
}
