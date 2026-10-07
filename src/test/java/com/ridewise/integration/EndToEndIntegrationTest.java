package com.ridewise.integration;

import com.ridewise.model.Driver;
import com.ridewise.model.FareReceipt;
import com.ridewise.model.Location;
import com.ridewise.model.Ride;
import com.ridewise.model.RideStatus;
import com.ridewise.model.Rider;
import com.ridewise.model.VehicleType;
import com.ridewise.service.DriverService;
import com.ridewise.service.RideService;
import com.ridewise.service.RiderService;
import com.ridewise.strategy.DefaultFareStrategy;
import com.ridewise.strategy.LeastActiveDriverStrategy;
import com.ridewise.strategy.NearestDriverStrategy;
import com.ridewise.strategy.PeakHourFareStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class EndToEndIntegrationTest {

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
    @DisplayName("MVP Workflow: Register -> Check Drivers -> Request -> Assign -> Complete -> Receipt")
    void testCompleteMvpFlow() {
        // 1. Register Rider
        Rider rider = riderService.registerRider("Alice", new Location(0.0, 0.0));
        assertThat(riderService.getRiderById(rider.getId())).isNotNull();

        // 2. Register Drivers
        Driver driver1 = driverService.registerDriver("Bob (Auto)", new Location(1.0, 1.0), VehicleType.AUTO);
        Driver driver2 = driverService.registerDriver("Charlie (Car)", new Location(5.0, 5.0), VehicleType.CAR);

        // 3. View Available Drivers
        List<Driver> availableDrivers = driverService.getAvailableDrivers();
        assertThat(availableDrivers).hasSize(2).contains(driver1, driver2);

        // 4. Request Ride (Nearest driver Bob at (1,1) should be chosen over Charlie at (5,5))
        Location dropOff = new Location(3.0, 4.0); // distance from (0,0) is 5.0 km
        Ride ride = rideService.requestRide(rider.getId(), dropOff);

        // 5 & 7. Ride Assigned to Bob
        assertThat(ride.getStatus()).isEqualTo(RideStatus.ASSIGNED);
        assertThat(ride.getDriver()).isEqualTo(driver1);
        assertThat(driver1.isAvailable()).isFalse();

        // Only Charlie is now available
        assertThat(driverService.getAvailableDrivers()).containsExactly(driver2);

        // 8 & 9. Complete Ride & Fare Receipt
        FareReceipt receipt = rideService.completeRide(ride.getId());
        assertThat(ride.getStatus()).isEqualTo(RideStatus.COMPLETED);
        assertThat(receipt.getRideId()).isEqualTo(ride.getId());

        // Calculation: (50 + 5.0 * 10) * AUTO(1.2) = 100 * 1.2 = 120.0
        assertThat(receipt.getAmount()).isCloseTo(120.0, within(0.01));

        // Driver 1 is now available at drop-off location with 1 completed trip
        assertThat(driver1.isAvailable()).isTrue();
        assertThat(driver1.getCurrentLocation()).isEqualTo(dropOff);
        assertThat(driver1.getCompletedRidesCount()).isEqualTo(1);

        // Rider location updated to drop-off point
        assertThat(rider.getLocation()).isEqualTo(dropOff);
    }

    @Test
    @DisplayName("Sequential trips with relocation and strategy switching")
    void testSequentialTripsWithLocationUpdates() {
        // Driver at (0, 0)
        Driver driver = driverService.registerDriver("Driver1", new Location(0.0, 0.0), VehicleType.CAR);

        // Rider 1 at (1, 1) requests trip to (10, 10)
        Rider rider1 = riderService.registerRider("Rider1", new Location(1.0, 1.0));
        Ride ride1 = rideService.requestRide(rider1.getId(), new Location(10.0, 10.0));
        rideService.completeRide(ride1.getId());

        // Driver is now at (10, 10)
        assertThat(driver.getCurrentLocation()).isEqualTo(new Location(10.0, 10.0));

        // Rider 2 at (10, 12) requests trip; driver is nearby and available
        Rider rider2 = riderService.registerRider("Rider2", new Location(10.0, 12.0));
        Ride ride2 = rideService.requestRide(rider2.getId(), new Location(10.0, 15.0));

        assertThat(ride2.getDriver()).isEqualTo(driver);
        rideService.completeRide(ride2.getId());

        assertThat(driver.getCompletedRidesCount()).isEqualTo(2);
        assertThat(driver.getCurrentLocation()).isEqualTo(new Location(10.0, 15.0));
    }

    @Test
    @DisplayName("LSP & OCP: Seamless swapping of matching and pricing strategies")
    void testStrategySwapping() {
        Rider rider = riderService.registerRider("Dave", new Location(0.0, 0.0));

        Driver veteran = driverService.registerDriver("Veteran", new Location(0.5, 0.5), VehicleType.BIKE);
        veteran.incrementCompletedRides();
        veteran.incrementCompletedRides();

        Driver rookie = driverService.registerDriver("Rookie", new Location(4.0, 4.0), VehicleType.BIKE);

        // Default strategy (Nearest): selects Veteran because distance is smaller
        Ride ride1 = rideService.requestRide(rider.getId(), new Location(0.0, 5.0));
        assertThat(ride1.getDriver()).isEqualTo(veteran);
        rideService.cancelRide(ride1.getId());

        // Switch to LeastActiveDriverStrategy: selects Rookie despite being farther away
        rideService.setMatchingStrategy(new LeastActiveDriverStrategy());
        // Switch to PeakHourFareStrategy (1.5x)
        rideService.setFareStrategy(new PeakHourFareStrategy(new DefaultFareStrategy(50.0, 10.0), 1.5));

        Ride ride2 = rideService.requestRide(rider.getId(), new Location(0.0, 5.0));
        assertThat(ride2.getDriver()).isEqualTo(rookie);

        FareReceipt receipt = rideService.completeRide(ride2.getId());
        // Distance 5.0 -> base = 50 + 50 = 100 * BIKE(1.0) * Surge(1.5) = 150.0
        assertThat(receipt.getAmount()).isCloseTo(150.0, within(0.01));
    }
}
