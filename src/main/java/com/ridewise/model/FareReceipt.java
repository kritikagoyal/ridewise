package com.ridewise.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Receipt generated upon ride completion.
 */
public class FareReceipt {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String rideId;
    private final double amount;
    private final LocalDateTime generatedAt;

    public FareReceipt(String rideId, double amount, LocalDateTime generatedAt) {
        if (rideId == null || rideId.isBlank()) {
            throw new IllegalArgumentException("Ride ID cannot be null or blank");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Fare amount cannot be negative");
        }
        this.rideId = rideId;
        this.amount = amount;
        this.generatedAt = Objects.requireNonNull(generatedAt, "Generated time cannot be null");
    }

    public FareReceipt(String rideId, double amount) {
        this(rideId, amount, LocalDateTime.now());
    }

    public String getRideId() {
        return rideId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FareReceipt that = (FareReceipt) o;
        return Double.compare(that.amount, amount) == 0 &&
                Objects.equals(rideId, that.rideId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rideId, amount);
    }

    @Override
    public String toString() {
        return "FareReceipt{" +
                "rideId='" + rideId + '\'' +
                ", amount=" + String.format("%.2f", amount) +
                ", generatedAt=" + generatedAt.format(FORMATTER) +
                '}';
    }
}
