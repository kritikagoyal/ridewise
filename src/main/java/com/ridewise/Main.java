package com.ridewise;

import com.ridewise.exception.EntityNotFoundException;
import com.ridewise.exception.InvalidOperationException;
import com.ridewise.exception.NoDriverAvailableException;
import com.ridewise.exception.RideSharingException;
import com.ridewise.model.Driver;
import com.ridewise.model.FareReceipt;
import com.ridewise.model.Location;
import com.ridewise.model.Ride;
import com.ridewise.model.Rider;
import com.ridewise.model.VehicleType;
import com.ridewise.service.DriverService;
import com.ridewise.service.RideService;
import com.ridewise.service.RiderService;
import com.ridewise.strategy.DefaultFareStrategy;
import com.ridewise.strategy.NearestDriverStrategy;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * Console entrypoint for the RideWise application.
 */
public class Main {

    private final RiderService riderService;
    private final DriverService driverService;
    private final RideService rideService;
    private final Scanner scanner;
    private final PrintStream out;

    public Main(InputStream in, PrintStream out) {
        this.scanner = new Scanner(in);
        this.out = out;
        this.riderService = new RiderService();
        this.driverService = new DriverService();
        this.rideService = new RideService(
                this.riderService,
                this.driverService,
                new NearestDriverStrategy(),
                new DefaultFareStrategy()
        );
    }

    public static void main(String[] args) {
        Main app = new Main(System.in, System.out);
        app.run();
    }

    public void run() {
        out.println("=========================================");
        out.println("       WELCOME TO RIDEWISE CLI           ");
        out.println("=========================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readIntInput("Select an option: ");

            switch (choice) {
                case 1 -> handleAddRider();
                case 2 -> handleAddDriver();
                case 3 -> handleViewAvailableDrivers();
                case 4 -> handleRequestRide();
                case 5 -> handleCompleteRide();
                case 6 -> handleViewRides();
                case 7 -> {
                    out.println("\nThank you for using RideWise. Goodbye!");
                    running = false;
                }
                default -> out.println("Invalid selection. Please choose an option between 1 and 7.");
            }
        }
    }

    private void printMenu() {
        out.println("\n--- MAIN MENU ---");
        out.println("1. Add Rider");
        out.println("2. Add Driver");
        out.println("3. View Available Drivers");
        out.println("4. Request Ride");
        out.println("5. Complete Ride");
        out.println("6. View Rides");
        out.println("7. Exit");
    }

    private void handleAddRider() {
        out.println("\n--- Add Rider ---");
        String name = readStringInput("Enter Rider Name: ");
        double x = readDoubleInput("Enter Current X Coordinate: ");
        double y = readDoubleInput("Enter Current Y Coordinate: ");

        try {
            Rider rider = riderService.registerRider(name, new Location(x, y));
            out.printf("Rider registered successfully! ID: %s, Name: %s, Location: %s%n",
                    rider.getId(), rider.getName(), rider.getLocation());
        } catch (IllegalArgumentException e) {
            out.println("Failed to register rider: " + e.getMessage());
        }
    }

    private void handleAddDriver() {
        out.println("\n--- Add Driver ---");
        String name = readStringInput("Enter Driver Name: ");
        double x = readDoubleInput("Enter Current X Coordinate: ");
        double y = readDoubleInput("Enter Current Y Coordinate: ");

        out.println("Select Vehicle Type:");
        out.println("1. BIKE");
        out.println("2. AUTO");
        out.println("3. CAR");
        int vehicleChoice = readIntInput("Select (1-3): ");

        VehicleType vehicleType = switch (vehicleChoice) {
            case 1 -> VehicleType.BIKE;
            case 2 -> VehicleType.AUTO;
            case 3 -> VehicleType.CAR;
            default -> {
                out.println("Invalid vehicle type choice, defaulting to CAR.");
                yield VehicleType.CAR;
            }
        };

        try {
            Driver driver = driverService.registerDriver(name, new Location(x, y), vehicleType);
            out.printf("Driver registered successfully! ID: %s, Name: %s, Location: %s, Vehicle: %s%n",
                    driver.getId(), driver.getName(), driver.getCurrentLocation(), driver.getVehicleType());
        } catch (IllegalArgumentException e) {
            out.println("Failed to register driver: " + e.getMessage());
        }
    }

    private void handleViewAvailableDrivers() {
        out.println("\n--- Available Drivers ---");
        List<Driver> drivers = driverService.getAvailableDrivers();
        if (drivers.isEmpty()) {
            out.println("No drivers currently available.");
            return;
        }

        out.printf("%-6s | %-15s | %-16s | %-8s | %-15s%n", "ID", "Name", "Location", "Vehicle", "Completed Trips");
        out.println("----------------------------------------------------------------------");
        for (Driver d : drivers) {
            out.printf("%-6s | %-15s | %-16s | %-8s | %-15d%n",
                    d.getId(), d.getName(), d.getCurrentLocation(), d.getVehicleType(), d.getCompletedRidesCount());
        }
    }

    private void handleRequestRide() {
        out.println("\n--- Request Ride ---");
        String riderId = readStringInput("Enter Rider ID: ");
        double destX = readDoubleInput("Enter Destination X Coordinate: ");
        double destY = readDoubleInput("Enter Destination Y Coordinate: ");

        try {
            Ride ride = rideService.requestRide(riderId, new Location(destX, destY));
            out.println("\nRide matched and assigned successfully!");
            out.printf("Ride ID         : %s%n", ride.getId());
            out.printf("Rider           : %s (ID: %s)%n", ride.getRider().getName(), ride.getRider().getId());
            out.printf("Driver Assigned : %s (ID: %s, Vehicle: %s)%n",
                    ride.getDriver().getName(), ride.getDriver().getId(), ride.getDriver().getVehicleType());
            out.printf("Pickup Location : %s%n", ride.getStartLocation());
            out.printf("Destination     : %s%n", ride.getDestinationLocation());
            out.printf("Distance        : %.2f km%n", ride.getDistance());
            out.printf("Status          : %s%n", ride.getStatus());
        } catch (NoDriverAvailableException e) {
            out.println("Cannot assign ride: " + e.getMessage());
        } catch (EntityNotFoundException e) {
            out.println("Error: " + e.getMessage());
        } catch (RideSharingException e) {
            out.println("Ride booking failed: " + e.getMessage());
        }
    }

    private void handleCompleteRide() {
        out.println("\n--- Complete Ride ---");
        String rideId = readStringInput("Enter Ride ID to complete: ");

        try {
            FareReceipt receipt = rideService.completeRide(rideId);
            Ride ride = rideService.getRideById(rideId);

            out.println("\n=========================================");
            out.println("              FARE RECEIPT               ");
            out.println("=========================================");
            out.printf("Ride ID      : %s%n", receipt.getRideId());
            out.printf("Rider        : %s%n", ride.getRider().getName());
            out.printf("Driver       : %s (%s)%n", ride.getDriver().getName(), ride.getDriver().getVehicleType());
            out.printf("Distance     : %.2f km%n", ride.getDistance());
            out.printf("Total Fare   : $%.2f%n", receipt.getAmount());
            out.printf("Generated At : %s%n", receipt.getGeneratedAt());
            out.println("=========================================");
            out.println("Driver is now available for new bookings.");
        } catch (EntityNotFoundException | InvalidOperationException e) {
            out.println("Error: " + e.getMessage());
        } catch (RideSharingException e) {
            out.println("Failed to complete ride: " + e.getMessage());
        }
    }

    private void handleViewRides() {
        out.println("\n--- All Rides ---");
        List<Ride> rides = rideService.getAllRides();
        if (rides.isEmpty()) {
            out.println("No rides recorded yet.");
            return;
        }

        out.printf("%-8s | %-10s | %-10s | %-10s | %-10s | %-10s%n",
                "Ride ID", "Rider", "Driver", "Distance", "Status", "Fare");
        out.println("----------------------------------------------------------------------");
        for (Ride r : rides) {
            String driverName = r.getDriver() != null ? r.getDriver().getName() : "Unassigned";
            String fareStr = r.getReceipt() != null ? String.format("$%.2f", r.getReceipt().getAmount()) : "Pending";
            out.printf("%-8s | %-10s | %-10s | %-8.2f km | %-10s | %-10s%n",
                    r.getId(), r.getRider().getName(), driverName, r.getDistance(), r.getStatus(), fareStr);
        }
    }

    private String readStringInput(String prompt) {
        while (true) {
            out.print(prompt);
            if (!scanner.hasNextLine()) {
                return "";
            }
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            out.println("Input cannot be empty. Please try again.");
        }
    }

    private int readIntInput(String prompt) {
        while (true) {
            out.print(prompt);
            if (!scanner.hasNextLine()) {
                return -1;
            }
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                out.println("Invalid number format. Please enter an integer.");
            }
        }
    }

    private double readDoubleInput(String prompt) {
        while (true) {
            out.print(prompt);
            if (!scanner.hasNextLine()) {
                return 0.0;
            }
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                out.println("Invalid numeric coordinate. Please enter a valid number.");
            }
        }
    }
}
