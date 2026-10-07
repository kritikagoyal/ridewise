package com.ridewise.repository;

import com.ridewise.model.Ride;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe implementation of RideRepository.
 */
public class InMemoryRideRepository implements RideRepository {
    private final Map<String, Ride> storage = new ConcurrentHashMap<>();

    @Override
    public Ride save(Ride ride) {
        Objects.requireNonNull(ride, "Ride cannot be null");
        storage.put(ride.getId(), ride);
        return ride;
    }

    @Override
    public Optional<Ride> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Ride> findAll() {
        return new ArrayList<>(storage.values());
    }
}
