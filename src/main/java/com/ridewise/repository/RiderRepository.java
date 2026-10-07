package com.ridewise.repository;

import com.ridewise.model.Rider;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Rider entity persistence.
 */
public interface RiderRepository {
    Rider save(Rider rider);
    Optional<Rider> findById(String id);
    List<Rider> findAll();
    boolean existsById(String id);
}
