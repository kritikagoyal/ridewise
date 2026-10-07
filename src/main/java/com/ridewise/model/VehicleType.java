package com.ridewise.model;

/**
 * Types of vehicles supported by the ride-sharing platform.
 */
public enum VehicleType {
    BIKE(1.0),
    AUTO(1.2),
    CAR(1.5);

    private final double fareMultiplier;

    VehicleType(double fareMultiplier) {
        this.fareMultiplier = fareMultiplier;
    }

    public double getFareMultiplier() {
        return fareMultiplier;
    }
}
