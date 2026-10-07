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
import com.ridewise.model.VehicleType;
import com.ridewise.strategy.DefaultFareStrategy;
import com.ridewise.strategy.LeastActiveDriverStrategy;
import com.ridewise.strategy.NearestDriverStrategy;
import com.ridewise.strategy.PeakHourFareStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceTest {

    private RiderService riderService;
    private DriverService driverService;
    private RideService rideService;

    @BeforeEach
    void setUp() {
        riderService = new RiderService();
        driverService = new DriverService();
        rideService = new RideService(
                riderService,
                driverService,
                new NearestDriverStrategy(),
                new DefaultFareStrategy(50.0, 10.0)
        );
    }

    @Test
    @DisplayName("Rider registration and lookup")
    void testRiderService() {
        Rider rider1 = riderService.registerRider("Alice", new Location(0, 0));
        assertThat(rider1.getId()).isNotNull();
        assertThat(rider1.getName()).isEqualTo("Alice");

        Rider fetched = riderService.getRiderById(rider1.getId());
        assertThat(fetched).isEqualTo(rider1);

        assertThatThrownBy(() -> riderService.getRiderById("NON_EXISTENT"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Driver registration, availability filtering and updates")
    void testDriverService() {
        Driver d1 = driverService.registerDriver("Bob", new Location(1, 1), VehicleType.CAR);
        Driver d2 = driverService.registerDriver("Charlie", new Location(2, 2), VehicleType.BIKE);

        List<Driver> available = driverService.getAvailableDrivers();
        assertThat(available).containsExactlyInAnyOrder(d1, d2);

        driverService.updateDriverAvailability(d1.getId(), false);
        assertThat(driverService.getAvailableDrivers()).containsExactly(d2);
    }

    @Test
    @DisplayName("Complete ride lifecycle: request -> assign -> complete with fare receipt")
    void testFullRideLifecycle() {
        Rider rider = riderService.registerRider("Alice", new Location(0, 0));
        Driver driver = driverService.registerDriver("Bob", new Location(1, 1), VehicleType.CAR);

        Location destination = new Location(3, 4); // distance = 5.0 km
        Ride ride = rideService.requestRide(rider.getId(), destination);

        assertThat(ride.getStatus()).isEqualTo(RideStatus.ASSIGNED);
        assertThat(ride.getDriver()).isEqualTo(driver);
        assertThat(driver.isAvailable()).isFalse();

        // Complete ride
        FareReceipt receipt = rideService.completeRide(ride.getId());
        assertThat(receipt).isNotNull();
        assertThat(ride.getStatus()).isEqualTo(RideStatus.COMPLETED);

        // Base fare = 50 + (5.0 * 10) = 100 * CAR(1.5) = 150.0
        assertThat(receipt.getAmount()).isEqualTo(150.0);

        // Driver is freed, location updated, rides incremented
        assertThat(driver.isAvailable()).isTrue();
        assertThat(driver.getCurrentLocation()).isEqualTo(destination);
        assertThat(driver.getCompletedRidesCount()).isEqualTo(1);

        // Rider location updated to destination
        assertThat(rider.getLocation()).isEqualTo(destination);

        // Attempting to complete already completed ride fails
        assertThatThrownBy(() -> rideService.completeRide(ride.getId()))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    @DisplayName("Requesting a ride when no drivers are available throws NoDriverAvailableException")
    void testNoDriverAvailable() {
        Rider rider = riderService.registerRider("Alice", new Location(0, 0));
        // No drivers registered

        assertThatThrownBy(() -> rideService.requestRide(rider.getId(), new Location(5, 5)))
                .isInstanceOf(NoDriverAvailableException.class);
    }

    @Test
    @DisplayName("Dynamic strategy switching at runtime")
    void testDynamicStrategySwitching() {
        Rider rider = riderService.registerRider("Alice", new Location(0, 0));

        // D1 closer but completed 3 rides
        Driver d1 = driverService.registerDriver("D1", new Location(1, 1), VehicleType.BIKE);
        d1.incrementCompletedRides();
        d1.incrementCompletedRides();
        d1.incrementCompletedRides();

        // D2 farther but completed 0 rides
        Driver d2 = driverService.registerDriver("D2", new Location(5, 5), VehicleType.BIKE);

        // Switch to LeastActiveDriverStrategy
        rideService.setMatchingStrategy(new LeastActiveDriverStrategy());
        // Switch to PeakHourFareStrategy
        rideService.setFareStrategy(new PeakHourFareStrategy(new DefaultFareStrategy(50.0, 10.0), 2.0));

        Ride ride = rideService.requestRide(rider.getId(), new Location(0, 10)); // distance = 10.0
        assertThat(ride.getDriver()).isEqualTo(d2); // D2 picked because it has fewer rides

        FareReceipt receipt = rideService.completeRide(ride.getId());
        // (50 + 10 * 10) * 1.0 = 150 * 2.0 surge = 300.0
        assertThat(receipt.getAmount()).isEqualTo(300.0);
    }

    @Test
    @DisplayName("Ride cancellation frees driver and updates status")
    void testRideCancellation() {
        Rider rider = riderService.registerRider("Alice", new Location(0, 0));
        Driver driver = driverService.registerDriver("Bob", new Location(1, 1), VehicleType.CAR);

        Ride ride = rideService.requestRide(rider.getId(), new Location(3, 4));
        assertThat(driver.isAvailable()).isFalse();

        rideService.cancelRide(ride.getId());
        assertThat(ride.getStatus()).isEqualTo(RideStatus.CANCELLED);
        assertThat(driver.isAvailable()).isTrue();
    }
}
