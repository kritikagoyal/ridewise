package com.ridewise.strategy;

import com.ridewise.model.Driver;
import com.ridewise.model.Rider;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Strategy that matches the rider with the available driver having the lowest completed ride count.
 * Ties are broken by distance to the rider.
 */
public class LeastActiveDriverStrategy implements RideMatchingStrategy {

    @Override
    public Driver findDriver(Rider rider, List<Driver> drivers) {
        Objects.requireNonNull(rider, "Rider cannot be null");
        if (drivers == null || drivers.isEmpty()) {
            return null;
        }

        return drivers.stream()
                .filter(Driver::isAvailable)
                .min(Comparator
                        .comparingInt(Driver::getCompletedRidesCount)
                        .thenComparingDouble(driver -> driver.getCurrentLocation().distanceTo(rider.getLocation())))
                .orElse(null);
    }
}
