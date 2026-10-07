package com.ridewise.strategy;

import com.ridewise.model.Driver;
import com.ridewise.model.Rider;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Strategy that matches the rider with the nearest available driver.
 */
public class NearestDriverStrategy implements RideMatchingStrategy {

    @Override
    public Driver findDriver(Rider rider, List<Driver> drivers) {
        Objects.requireNonNull(rider, "Rider cannot be null");
        if (drivers == null || drivers.isEmpty()) {
            return null;
        }

        return drivers.stream()
                .filter(Driver::isAvailable)
                .min(Comparator.comparingDouble(
                        driver -> driver.getCurrentLocation().distanceTo(rider.getLocation())))
                .orElse(null);
    }
}
