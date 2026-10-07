package com.ridewise.strategy;

import com.ridewise.model.Driver;
import com.ridewise.model.Location;
import com.ridewise.model.Ride;
import com.ridewise.model.Rider;
import com.ridewise.model.VehicleType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StrategyTest {

    @Test
    @DisplayName("NearestDriverStrategy matches closest available driver")
    void testNearestDriverStrategy() {
        Rider rider = new Rider("R1", "Alice", new Location(0, 0));

        Driver dCloseButUnavailable = new Driver("D1", "Bob", new Location(1, 1));
        dCloseButUnavailable.setAvailable(false);

        Driver dFartherAvailable = new Driver("D2", "Charlie", new Location(3, 4)); // distance = 5
        Driver dFurthestAvailable = new Driver("D3", "David", new Location(6, 8)); // distance = 10

        RideMatchingStrategy strategy = new NearestDriverStrategy();

        Driver matched = strategy.findDriver(rider, List.of(dCloseButUnavailable, dFartherAvailable, dFurthestAvailable));
        assertThat(matched).isEqualTo(dFartherAvailable);

        // Empty or no available drivers
        assertThat(strategy.findDriver(rider, Collections.emptyList())).isNull();
        assertThat(strategy.findDriver(rider, List.of(dCloseButUnavailable))).isNull();
    }

    @Test
    @DisplayName("LeastActiveDriverStrategy matches driver with fewest completed rides and breaks ties by distance")
    void testLeastActiveDriverStrategy() {
        Rider rider = new Rider("R1", "Alice", new Location(0, 0));

        Driver dActive = new Driver("D1", "Bob", new Location(1, 1));
        dActive.incrementCompletedRides();
        dActive.incrementCompletedRides(); // 2 completed

        Driver dLessActiveFar = new Driver("D2", "Charlie", new Location(10, 10)); // 0 completed
        Driver dLessActiveNear = new Driver("D3", "David", new Location(2, 2)); // 0 completed, closer!

        RideMatchingStrategy strategy = new LeastActiveDriverStrategy();

        Driver matched = strategy.findDriver(rider, List.of(dActive, dLessActiveFar, dLessActiveNear));
        // Between D2 and D3, both have 0 rides, D3 is closer to rider
        assertThat(matched).isEqualTo(dLessActiveNear);
    }

    @Test
    @DisplayName("DefaultFareStrategy computes base + distance * perKm modulated by vehicle type")
    void testDefaultFareStrategy() {
        Rider rider = new Rider("R1", "Alice", new Location(0, 0));
        Driver carDriver = new Driver("D1", "Bob", new Location(0, 0), VehicleType.CAR); // multiplier 1.5
        Ride carRide = new Ride("RIDE-1", rider, carDriver, new Location(0, 0), new Location(0, 10), 10.0);

        FareStrategy defaultStrategy = new DefaultFareStrategy(50.0, 10.0);
        // Base = 50 + (10 * 10) = 150. Multiplier = 1.5 => 225.0
        double fare = defaultStrategy.calculateFare(carRide);
        assertThat(fare).isEqualTo(225.0);

        // Bike test: multiplier 1.0 => (50 + 100) * 1.0 = 150.0
        Driver bikeDriver = new Driver("D2", "Charlie", new Location(0, 0), VehicleType.BIKE);
        Ride bikeRide = new Ride("RIDE-2", rider, bikeDriver, new Location(0, 0), new Location(0, 10), 10.0);
        assertThat(defaultStrategy.calculateFare(bikeRide)).isEqualTo(150.0);
    }

    @Test
    @DisplayName("PeakHourFareStrategy applies surge multiplier accurately")
    void testPeakHourFareStrategy() {
        Rider rider = new Rider("R1", "Alice", new Location(0, 0));
        Driver bikeDriver = new Driver("D1", "Bob", new Location(0, 0), VehicleType.BIKE);
        Ride bikeRide = new Ride("RIDE-1", rider, bikeDriver, new Location(0, 0), new Location(0, 10), 10.0);

        FareStrategy defaultStrategy = new DefaultFareStrategy(50.0, 10.0); // 150.0
        FareStrategy surgeStrategy = new PeakHourFareStrategy(defaultStrategy, 2.0); // 150.0 * 2.0 = 300.0

        assertThat(surgeStrategy.calculateFare(bikeRide)).isEqualTo(300.0);
    }
}
