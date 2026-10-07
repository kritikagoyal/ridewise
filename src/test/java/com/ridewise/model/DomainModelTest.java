package com.ridewise.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainModelTest {

    @Test
    @DisplayName("Location calculates Euclidean distance accurately")
    void testLocationDistance() {
        Location loc1 = new Location(0.0, 0.0);
        Location loc2 = new Location(3.0, 4.0);

        assertThat(loc1.distanceTo(loc2)).isEqualTo(5.0);
        assertThat(loc2.distanceTo(loc1)).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Rider creation and properties validation")
    void testRiderCreation() {
        Location loc = new Location(10.0, 20.0);
        Rider rider = new Rider("R1", "Alice", loc);

        assertThat(rider.getId()).isEqualTo("R1");
        assertThat(rider.getName()).isEqualTo("Alice");
        assertThat(rider.getLocation()).isEqualTo(loc);

        assertThatThrownBy(() -> new Rider(null, "Alice", loc))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Rider("R1", "  ", loc))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Driver creation, availability toggle, and activity counter")
    void testDriverCreationAndActivity() {
        Location loc = new Location(5.0, 5.0);
        Driver driver = new Driver("D1", "Bob", loc, VehicleType.CAR);

        assertThat(driver.getId()).isEqualTo("D1");
        assertThat(driver.getName()).isEqualTo("Bob");
        assertThat(driver.isAvailable()).isTrue();
        assertThat(driver.getVehicleType()).isEqualTo(VehicleType.CAR);
        assertThat(driver.getCompletedRidesCount()).isZero();

        driver.setAvailable(false);
        assertThat(driver.isAvailable()).isFalse();

        driver.incrementCompletedRides();
        assertThat(driver.getCompletedRidesCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ride lifecycle state transitions: REQUESTED -> ASSIGNED -> COMPLETED")
    void testRideLifecycle() {
        Rider rider = new Rider("R1", "Alice", new Location(0, 0));
        Driver driver = new Driver("D1", "Bob", new Location(1, 1));
        Location destination = new Location(3, 4);

        Ride ride = new Ride("RIDE-1", rider, rider.getLocation(), destination, 5.0);
        assertThat(ride.getStatus()).isEqualTo(RideStatus.REQUESTED);
        assertThat(ride.getDriver()).isNull();

        ride.assignDriver(driver);
        assertThat(ride.getStatus()).isEqualTo(RideStatus.ASSIGNED);
        assertThat(ride.getDriver()).isEqualTo(driver);

        FareReceipt receipt = new FareReceipt("RIDE-1", 120.50, LocalDateTime.now());
        ride.complete(receipt);
        assertThat(ride.getStatus()).isEqualTo(RideStatus.COMPLETED);
        assertThat(ride.getReceipt()).isEqualTo(receipt);

        // Cannot complete again
        assertThatThrownBy(() -> ride.complete(receipt))
                .isInstanceOf(IllegalStateException.class);

        // Cannot cancel a completed ride
        assertThatThrownBy(ride::cancel)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ride cancellation from ASSIGNED state")
    void testRideCancellation() {
        Rider rider = new Rider("R1", "Alice", new Location(0, 0));
        Driver driver = new Driver("D1", "Bob", new Location(1, 1));
        Ride ride = new Ride("RIDE-1", rider, driver, new Location(0, 0), new Location(3, 4), 5.0);

        assertThat(ride.getStatus()).isEqualTo(RideStatus.ASSIGNED);
        ride.cancel();
        assertThat(ride.getStatus()).isEqualTo(RideStatus.CANCELLED);
    }
}
