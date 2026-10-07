package com.ridewise.model;

import java.util.Objects;

/**
 * Domain entity representing a ride booking.
 */
public class Ride {
    private final String id;
    private final Rider rider;
    private Driver driver;
    private final Location startLocation;
    private final Location destinationLocation;
    private final double distance;
    private RideStatus status;
    private FareReceipt receipt;

    public Ride(String id, Rider rider, Location startLocation, Location destinationLocation, double distance) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Ride ID cannot be null or blank");
        }
        if (distance < 0) {
            throw new IllegalArgumentException("Distance cannot be negative");
        }
        this.id = id;
        this.rider = Objects.requireNonNull(rider, "Rider cannot be null");
        this.startLocation = Objects.requireNonNull(startLocation, "Start location cannot be null");
        this.destinationLocation = Objects.requireNonNull(destinationLocation, "Destination location cannot be null");
        this.distance = distance;
        this.status = RideStatus.REQUESTED;
    }

    public Ride(String id, Rider rider, Driver driver, Location startLocation, Location destinationLocation, double distance) {
        this(id, rider, startLocation, destinationLocation, distance);
        assignDriver(driver);
    }

    public String getId() {
        return id;
    }

    public Rider getRider() {
        return rider;
    }

    public Driver getDriver() {
        return driver;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public Location getDestinationLocation() {
        return destinationLocation;
    }

    public double getDistance() {
        return distance;
    }

    public RideStatus getStatus() {
        return status;
    }

    public FareReceipt getReceipt() {
        return receipt;
    }

    /**
     * Assigns a driver to the ride, updating its status to ASSIGNED.
     */
    public void assignDriver(Driver driver) {
        if (this.status != RideStatus.REQUESTED) {
            throw new IllegalStateException("Ride can only be assigned from REQUESTED state. Current status: " + this.status);
        }
        this.driver = Objects.requireNonNull(driver, "Driver cannot be null");
        this.status = RideStatus.ASSIGNED;
    }

    /**
     * Completes the ride with a calculated fare receipt.
     */
    public void complete(FareReceipt receipt) {
        if (this.status != RideStatus.ASSIGNED) {
            throw new IllegalStateException("Only ASSIGNED rides can be completed. Current status: " + this.status);
        }
        this.receipt = Objects.requireNonNull(receipt, "FareReceipt cannot be null");
        this.status = RideStatus.COMPLETED;
    }

    /**
     * Cancels the ride if it hasn't already been completed.
     */
    public void cancel() {
        if (this.status == RideStatus.COMPLETED) {
            throw new IllegalStateException("Completed ride cannot be cancelled");
        }
        this.status = RideStatus.CANCELLED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ride ride = (Ride) o;
        return Objects.equals(id, ride.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ride{" +
                "id='" + id + '\'' +
                ", rider=" + rider.getName() + " (" + rider.getId() + ")" +
                ", driver=" + (driver != null ? driver.getName() + " (" + driver.getId() + ")" : "Unassigned") +
                ", distance=" + String.format("%.2f", distance) + " km" +
                ", status=" + status +
                ", fare=" + (receipt != null ? String.format("%.2f", receipt.getAmount()) : "Pending") +
                '}';
    }
}
