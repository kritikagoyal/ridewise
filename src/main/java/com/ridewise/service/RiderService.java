package com.ridewise.service;

import com.ridewise.exception.EntityNotFoundException;
import com.ridewise.model.Location;
import com.ridewise.model.Rider;
import com.ridewise.repository.InMemoryRiderRepository;
import com.ridewise.repository.RiderRepository;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service managing rider registration and retrieval.
 */
public class RiderService {

    private final RiderRepository riderRepository;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public RiderService(RiderRepository riderRepository) {
        this.riderRepository = Objects.requireNonNull(riderRepository, "RiderRepository cannot be null");
    }

    public RiderService() {
        this(new InMemoryRiderRepository());
    }

    /**
     * Registers a new rider with an auto-generated ID.
     */
    public Rider registerRider(String name, Location location) {
        String id = "R" + idCounter.getAndIncrement();
        return registerRider(id, name, location);
    }

    /**
     * Registers a new rider with a specified ID.
     */
    public Rider registerRider(String id, String name, Location location) {
        if (riderRepository.existsById(id)) {
            throw new IllegalArgumentException("Rider with ID '" + id + "' already exists");
        }
        Rider rider = new Rider(id, name, location);
        return riderRepository.save(rider);
    }

    /**
     * Retrieves a rider by their unique ID.
     *
     * @param id rider ID
     * @return the Rider entity
     * @throws EntityNotFoundException if no rider is found
     */
    public Rider getRiderById(String id) {
        return riderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rider not found with ID: " + id));
    }

    /**
     * Returns a list of all registered riders.
     */
    public List<Rider> getAllRiders() {
        return Collections.unmodifiableList(riderRepository.findAll());
    }
}
