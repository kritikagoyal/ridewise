package com.ridewise.repository;

import com.ridewise.model.Rider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe implementation of RiderRepository.
 */
public class InMemoryRiderRepository implements RiderRepository {
    private final Map<String, Rider> storage = new ConcurrentHashMap<>();

    @Override
    public Rider save(Rider rider) {
        Objects.requireNonNull(rider, "Rider cannot be null");
        storage.put(rider.getId(), rider);
        return rider;
    }

    @Override
    public Optional<Rider> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Rider> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public boolean existsById(String id) {
        return id != null && storage.containsKey(id);
    }
}
