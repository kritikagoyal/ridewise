package com.ridewise.service;

import com.ridewise.exception.EntityNotFoundException;
import com.ridewise.exception.InvalidOperationException;
import com.ridewise.exception.NoDriverAvailableException;
import com.ridewise.model.Driver;
import com.ridewise.model.FareReceipt;
import com.ridewise.model.Location;
import com.ridewise.model.Ride;
import com.ridewise.model.RideStatus;
import com.ridewise.model.Rider;
import com.ridewise.repository.InMemoryRideRepository;
import com.ridewise.repository.RideRepository;
import com.ridewise.strategy.FareStrategy;
import com.ridewise.strategy.RideMatchingStrategy;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service orchestrating ride requests, driver assignment, ride completion, and fare calculation.
 */
public class RideService {

    private final RideRepository rideRepository;
    private final RiderService riderService;
    private final DriverService driverService;
    private RideMatchingStrategy matchingStrategy;
    private FareStrategy fareStrategy;

    private final AtomicInteger rideIdCounter = new AtomicInteger(1);

    /**
     * Constructor as specified in LLD brief for dependency injection.
     */
    public RideService(RideMatchingStrategy matchingStrategy, FareStrategy fareStrategy) {
        this(new InMemoryRideRepository(), new RiderService(), new DriverService(), matchingStrategy, fareStrategy);
    }

    public RideService(RiderService riderService,
                       DriverService driverService,
                       RideMatchingStrategy matchingStrategy,
                       FareStrategy fareStrategy) {
        this(new InMemoryRideRepository(), riderService, driverService, matchingStrategy, fareStrategy);
    }

    public RideService(RideRepository rideRepository,
                       RiderService riderService,
                       DriverService driverService,
                       RideMatchingStrategy matchingStrategy,
                       FareStrategy fareStrategy) {
        this.rideRepository = Objects.requireNonNull(rideRepository, "RideRepository cannot be null");
        this.riderService = Objects.requireNonNull(riderService, "RiderService cannot be null");
        this.driverService = Objects.requireNonNull(driverService, "DriverService cannot be null");
        this.matchingStrategy = Objects.requireNonNull(matchingStrategy, "RideMatchingStrategy cannot be null");
        this.fareStrategy = Objects.requireNonNull(fareStrategy, "FareStrategy cannot be null");
    }

    /**
     * Requests a new ride for a rider to a given destination.
     * Matches and assigns an available driver using the configured RideMatchingStrategy.
     *
     * @param riderId     ID of the rider requesting the ride
     * @param destination Target drop-off coordinates
     * @return Assigned Ride entity
     */
    public Ride requestRide(String riderId, Location destination) {
        Rider rider = riderService.getRiderById(riderId);
        Objects.requireNonNull(destination, "Destination location cannot be null");

        double distance = rider.getLocation().distanceTo(destination);
        String rideId = "RIDE-" + rideIdCounter.getAndIncrement();
        Ride ride = new Ride(rideId, rider, rider.getLocation(), destination, distance);

        List<Driver> availableDrivers = driverService.getAvailableDrivers();
        Driver matchedDriver = matchingStrategy.findDriver(rider, availableDrivers);

        if (matchedDriver == null) {
            rideRepository.save(ride);
            throw new NoDriverAvailableException("No available drivers found for rider: " + rider.getName());
        }

        // Driver assigned: mark driver unavailable and transition ride state
        matchedDriver.setAvailable(false);
        ride.assignDriver(matchedDriver);
        return rideRepository.save(ride);
    }

    /**
     * Completes an active ride, calculates the fare using the FareStrategy, and generates a FareReceipt.
     *
     * @param rideId ID of the ride to complete
     * @return generated FareReceipt
     */
    public FareReceipt completeRide(String rideId) {
        Ride ride = getRideById(rideId);

        if (ride.getStatus() != RideStatus.ASSIGNED) {
            throw new InvalidOperationException("Cannot complete ride in state: " + ride.getStatus());
        }

        double fareAmount = fareStrategy.calculateFare(ride);
        FareReceipt receipt = new FareReceipt(ride.getId(), fareAmount, LocalDateTime.now());
        ride.complete(receipt);

        // Update driver state: increment completed count, update location, mark available
        Driver driver = ride.getDriver();
        if (driver != null) {
            driver.incrementCompletedRides();
            driver.setCurrentLocation(ride.getDestinationLocation());
            driver.setAvailable(true);
        }

        // Update rider location to destination
        ride.getRider().setLocation(ride.getDestinationLocation());

        rideRepository.save(ride);
        return receipt;
    }

    /**
     * Cancels an existing ride and frees the driver if already assigned.
     */
    public void cancelRide(String rideId) {
        Ride ride = getRideById(rideId);

        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new InvalidOperationException("Cannot cancel a completed ride");
        }

        Driver driver = ride.getDriver();
        if (driver != null) {
            driver.setAvailable(true);
        }

        ride.cancel();
        rideRepository.save(ride);
    }

    /**
     * Retrieves a ride by ID.
     */
    public Ride getRideById(String rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new EntityNotFoundException("Ride not found with ID: " + rideId));
    }

    /**
     * Retrieves all recorded rides.
     */
    public List<Ride> getAllRides() {
        return Collections.unmodifiableList(rideRepository.findAll());
    }

    // Strategy Getters and Setters for runtime reconfiguration

    public RideMatchingStrategy getMatchingStrategy() {
        return matchingStrategy;
    }

    public void setMatchingStrategy(RideMatchingStrategy matchingStrategy) {
        this.matchingStrategy = Objects.requireNonNull(matchingStrategy, "RideMatchingStrategy cannot be null");
    }

    public FareStrategy getFareStrategy() {
        return fareStrategy;
    }

    public void setFareStrategy(FareStrategy fareStrategy) {
        this.fareStrategy = Objects.requireNonNull(fareStrategy, "FareStrategy cannot be null");
    }

    public RiderService getRiderService() {
        return riderService;
    }

    public DriverService getDriverService() {
        return driverService;
    }
}
