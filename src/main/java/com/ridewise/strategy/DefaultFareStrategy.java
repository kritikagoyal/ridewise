package com.ridewise.strategy;

import com.ridewise.model.Ride;
import com.ridewise.model.VehicleType;

import java.util.Objects;

/**
 * Standard pricing strategy:
 * Fare = (baseFare + (distance * ratePerKm)) * vehicleMultiplier
 */
public class DefaultFareStrategy implements FareStrategy {

    public static final double DEFAULT_BASE_FARE = 50.0;
    public static final double DEFAULT_RATE_PER_KM = 10.0;

    private final double baseFare;
    private final double ratePerKm;

    public DefaultFareStrategy() {
        this(DEFAULT_BASE_FARE, DEFAULT_RATE_PER_KM);
    }

    public DefaultFareStrategy(double baseFare, double ratePerKm) {
        if (baseFare < 0 || ratePerKm < 0) {
            throw new IllegalArgumentException("Base fare and rate per km cannot be negative");
        }
        this.baseFare = baseFare;
        this.ratePerKm = ratePerKm;
    }

    @Override
    public double calculateFare(Ride ride) {
        Objects.requireNonNull(ride, "Ride cannot be null");

        double vehicleMultiplier = 1.0;
        if (ride.getDriver() != null && ride.getDriver().getVehicleType() != null) {
            vehicleMultiplier = ride.getDriver().getVehicleType().getFareMultiplier();
        }

        double rawFare = (baseFare + (ride.getDistance() * ratePerKm)) * vehicleMultiplier;
        return Math.round(rawFare * 100.0) / 100.0;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getRatePerKm() {
        return ratePerKm;
    }
}
