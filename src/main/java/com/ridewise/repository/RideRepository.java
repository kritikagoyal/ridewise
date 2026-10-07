package com.ridewise.repository;

import com.ridewise.model.Ride;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Ride entity persistence.
 */
public interface RideRepository {
    Ride save(Ride ride);
    Optional<Ride> findById(String id);
    List<Ride> findAll();
}
