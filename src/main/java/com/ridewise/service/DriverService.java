package com.ridewise.service;

import com.ridewise.exception.EntityNotFoundException;
import com.ridewise.model.Driver;
import com.ridewise.model.Location;
import com.ridewise.model.VehicleType;
import com.ridewise.repository.DriverRepository;
import com.ridewise.repository.InMemoryDriverRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service managing driver registration, availability, and queries.
 */
public class DriverService {

    private final DriverRepository driverRepository;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = Objects.requireNonNull(driverRepository, "DriverRepository cannot be null");
    }

    public DriverService() {
        this(new InMemoryDriverRepository());
    }

    /**
     * Registers a new driver with an auto-generated ID.
     */
    public Driver registerDriver(String name, Location location, VehicleType vehicleType) {
        String id = "D" + idCounter.getAndIncrement();
        return registerDriver(id, name, location, vehicleType);
    }

    /**
     * Registers a new driver with an explicit ID.
     */
    public Driver registerDriver(String id, String name, Location location, VehicleType vehicleType) {
        if (driverRepository.existsById(id)) {
            throw new IllegalArgumentException("Driver with ID '" + id + "' already exists");
        }
        Driver driver = new Driver(id, name, location, vehicleType);
        return driverRepository.save(driver);
    }

    /**
     * Toggles/updates the availability of a driver.
     */
    public void updateDriverAvailability(String driverId, boolean available) {
        Driver driver = getDriverById(driverId);
        driver.setAvailable(available);
        driverRepository.save(driver);
    }

    /**
     * Retrieves all currently available drivers.
     */
    public List<Driver> getAvailableDrivers() {
        return Collections.unmodifiableList(driverRepository.findAvailableDrivers());
    }

    /**
     * Retrieves a driver by their unique ID.
     */
    public Driver getDriverById(String id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Driver not found with ID: " + id));
    }

    /**
     * Retrieves all drivers regardless of availability.
     */
    public List<Driver> getAllDrivers() {
        return Collections.unmodifiableList(driverRepository.findAll());
    }
}
