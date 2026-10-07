package com.ridewise.model;

import java.util.Objects;

/**
 * Value object representing 2D spatial coordinates for riders and drivers.
 */
public record Location(double x, double y) {

    public Location {
        // Validation if needed
    }

    /**
     * Calculates the Euclidean distance to another location.
     *
     * @param other target location
     * @return Euclidean distance
     */
    public double distanceTo(Location other) {
        Objects.requireNonNull(other, "Target location cannot be null");
        return Math.hypot(this.x - other.x, this.y - other.y);
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", x, y);
    }
}
