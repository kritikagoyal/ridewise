package com.ridewise.repository;

import com.ridewise.model.Driver;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Driver entity persistence.
 */
public interface DriverRepository {
    Driver save(Driver driver);
    Optional<Driver> findById(String id);
    List<Driver> findAll();
    List<Driver> findAvailableDrivers();
    boolean existsById(String id);
}
