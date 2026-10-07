package com.ridewise;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class MainTest {

    @Test
    @DisplayName("Main console app handles full interactive workflow: 1 -> 2 -> 3 -> 4 -> 5 -> 6 -> 7")
    void testMainConsoleAppWorkflow() {
        String simulatedInput = String.join("\n",
                // 1. Add Rider: Alice at (0, 0)
                "1",
                "Alice",
                "0.0",
                "0.0",
                // 2. Add Driver: Bob at (1, 1), CAR (option 3)
                "2",
                "Bob",
                "1.0",
                "1.0",
                "3",
                // 3. View Available Drivers
                "3",
                // 4. Request Ride: R1 to (3, 4)
                "4",
                "R1",
                "3.0",
                "4.0",
                // 5. Complete Ride: RIDE-1
                "5",
                "RIDE-1",
                // 6. View Rides
                "6",
                // 7. Exit
                "7"
        ) + "\n";

        ByteArrayInputStream in = new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out, true, StandardCharsets.UTF_8);

        Main app = new Main(in, printStream);
        app.run();

        String output = out.toString(StandardCharsets.UTF_8);

        assertThat(output).contains("Rider registered successfully! ID: R1, Name: Alice");
        assertThat(output).contains("Driver registered successfully! ID: D1, Name: Bob");
        assertThat(output).contains("Bob");
        assertThat(output).contains("Ride matched and assigned successfully!");
        assertThat(output).contains("FARE RECEIPT");
        assertThat(output).contains("Total Fare   : $150.00");
        assertThat(output).contains("All Rides");
        assertThat(output).contains("COMPLETED");
        assertThat(output).contains("Thank you for using RideWise. Goodbye!");
    }
}
