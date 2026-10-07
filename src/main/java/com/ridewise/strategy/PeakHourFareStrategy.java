package com.ridewise.strategy;

import com.ridewise.model.Ride;

import java.util.Objects;

/**
 * Peak hour surge pricing strategy that scales the base strategy's calculation by a surge factor.
 */
public class PeakHourFareStrategy implements FareStrategy {

    public static final double DEFAULT_SURGE_MULTIPLIER = 1.5;

    private final FareStrategy baseStrategy;
    private final double surgeMultiplier;

    public PeakHourFareStrategy() {
        this(new DefaultFareStrategy(), DEFAULT_SURGE_MULTIPLIER);
    }

    public PeakHourFareStrategy(double surgeMultiplier) {
        this(new DefaultFareStrategy(), surgeMultiplier);
    }

    public PeakHourFareStrategy(FareStrategy baseStrategy, double surgeMultiplier) {
        this.baseStrategy = Objects.requireNonNull(baseStrategy, "Base fare strategy cannot be null");
        if (surgeMultiplier <= 0) {
            throw new IllegalArgumentException("Surge multiplier must be positive");
        }
        this.surgeMultiplier = surgeMultiplier;
    }

    @Override
    public double calculateFare(Ride ride) {
        double baseCalculated = baseStrategy.calculateFare(ride);
        double surgeFare = baseCalculated * surgeMultiplier;
        return Math.round(surgeFare * 100.0) / 100.0;
    }

    public FareStrategy getBaseStrategy() {
        return baseStrategy;
    }

    public double getSurgeMultiplier() {
        return surgeMultiplier;
    }
}
