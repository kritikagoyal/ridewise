package com.ridewise.repository;

import com.ridewise.model.Driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory thread-safe implementation of DriverRepository.
 */
public class InMemoryDriverRepository implements DriverRepository {
    private final Map<String, Driver> storage = new ConcurrentHashMap<>();

    @Override
    public Driver save(Driver driver) {
        Objects.requireNonNull(driver, "Driver cannot be null");
        storage.put(driver.getId(), driver);
        return driver;
    }

    @Override
    public Optional<Driver> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Driver> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Driver> findAvailableDrivers() {
        return storage.values().stream()
                .filter(Driver::isAvailable)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsById(String id) {
        return id != null && storage.containsKey(id);
    }
}
