package com.ridewise.model;

import java.util.Objects;

/**
 * Domain entity representing a rider/passenger.
 */
public class Rider {
    private final String id;
    private String name;
    private Location location;

    public Rider(String id, String name, Location location) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Rider ID cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Rider name cannot be null or blank");
        }
        this.id = id;
        this.name = name;
        this.location = Objects.requireNonNull(location, "Rider location cannot be null");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Rider name cannot be null or blank");
        }
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = Objects.requireNonNull(location, "Rider location cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rider rider = (Rider) o;
        return Objects.equals(id, rider.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Rider{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location=" + location +
                '}';
    }
}
