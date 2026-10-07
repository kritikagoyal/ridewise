package com.ridewise.model;

import java.util.Objects;

/**
 * Domain entity representing a driver.
 */
public class Driver {
    private final String id;
    private String name;
    private Location currentLocation;
    private boolean available;
    private VehicleType vehicleType;
    private int completedRidesCount;

    public Driver(String id, String name, Location currentLocation, VehicleType vehicleType) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Driver ID cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Driver name cannot be null or blank");
        }
        this.id = id;
        this.name = name;
        this.currentLocation = Objects.requireNonNull(currentLocation, "Current location cannot be null");
        this.vehicleType = Objects.requireNonNull(vehicleType, "Vehicle type cannot be null");
        this.available = true;
        this.completedRidesCount = 0;
    }

    public Driver(String id, String name, Location currentLocation) {
        this(id, name, currentLocation, VehicleType.CAR);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Driver name cannot be null or blank");
        }
        this.name = name;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = Objects.requireNonNull(currentLocation, "Current location cannot be null");
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = Objects.requireNonNull(vehicleType, "Vehicle type cannot be null");
    }

    public int getCompletedRidesCount() {
        return completedRidesCount;
    }

    public void incrementCompletedRides() {
        this.completedRidesCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Driver driver = (Driver) o;
        return Objects.equals(id, driver.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Driver{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", currentLocation=" + currentLocation +
                ", available=" + available +
                ", vehicleType=" + vehicleType +
                ", completedRidesCount=" + completedRidesCount +
                '}';
    }
}
