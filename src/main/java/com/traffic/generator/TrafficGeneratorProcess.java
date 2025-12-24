package com.traffic.generator;

import java.io.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * This program runs independently and writes vehicle data to lane files:
 * - lanea.txt (Road A vehicles - L1, L2, L3)
 * - laneb.txt (Road B vehicles - L1, L3)
 * - lanec.txt (Road C vehicles - L1, L3)
 * - laned.txt (Road D vehicles - L1, L3)
 *
 * File Format per line:
 * VEHICLE_ID,LANE_NUMBER,TIMESTAMP
 *
 * Lane Numbers:
 * 1 = Normal incoming (straight/right)
 * 2 = Priority incoming (AL2 only - straight/right)
  3 = Left-turn only incoming
 */

public class TrafficGeneratorProcess {

    private static final String LANE_A_FILE = "lanea.txt";
    private static final String LANE_B_FILE = "laneb.txt";
    private static final String LANE_C_FILE = "lanec.txt";
    private static final String LANE_D_FILE = "laned.txt";

    private static final Random random = new Random();
    private static volatile boolean running = true;

    public static void main(String[] args) {

        System.out.println("  Traffic Generator Process Started");
        System.out.println("Lane Structure:");
        System.out.println("  L1 = Normal incoming (straight/right)");
        System.out.println("  L2 = Priority (AL2 only)");
        System.out.println("  L3 = Left-turn only");
        System.out.println("----------------------------------------");
        System.out.println("Writing to files:");
        System.out.println("  - " + LANE_A_FILE + " (L1, L2, L3)");
        System.out.println("  - " + LANE_B_FILE + " (L1, L3)");
        System.out.println("  - " + LANE_C_FILE + " (L1, L3)");
        System.out.println("  - " + LANE_D_FILE + " (L1, L3)");
        System.out.println("--------------------------------------------");
        System.out.println("Press Ctrl+C to stop\n");

        // Cleanup old files on start
        clearLaneFiles();

        // Shutdown hook for graceful exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            System.out.println("\n\n Traffic Generator shutting down...");
        }));

        // Main generation loop
        int cycleCount = 0;
        while (running) {
            try {
                cycleCount++;
                System.out.println("\n Cycle " + cycleCount );

                generateTraffic();

                // Generate new vehicles every 2.5 seconds (matching simulator)
                TimeUnit.MILLISECONDS.sleep(2500);

            } catch (InterruptedException e) {
                System.out.println("Generator interrupted");
                break;
            } catch (Exception e) {
                System.err.println("Error in generation cycle: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Traffic Generator stopped.");
    }

    /**
     * Generate random traffic following assignment specifications:
     *
     * Road A: L1 (normal), L2 (priority), L3 (left-turn)
     * Roads B,C,D: L1 (normal), L3 (left-turn)
     */
    private static void generateTraffic() {
        long timestamp = System.currentTimeMillis();

        // ROAD A (Has Priority Lane AL2)

        // AL1 - Normal incoming lane (straight/right)
        if (random.nextDouble() < 0.35) {
            String vehicleId = "A1-" + (timestamp % 10000);
            writeVehicleToFile(LANE_A_FILE, vehicleId, 1, timestamp);
            System.out.println("Generated: " + vehicleId + " → Road A, Lane 1 (Normal)");
        }

        // AL2 - PRIORITY incoming lane (straight/right)
        if (random.nextDouble() < 0.25) {
            String vehicleId = "A2-" + (timestamp % 10000);
            writeVehicleToFile(LANE_A_FILE, vehicleId, 2, timestamp);
            System.out.println("Generated: " + vehicleId + " → Road A, Lane 2 (PRIORITY)");
        }

        // AL3 - Left-turn ONLY lane
        if (random.nextDouble() < 0.15) {
            String vehicleId = "A3-" + (timestamp % 10000);
            writeVehicleToFile(LANE_A_FILE, vehicleId, 3, timestamp);
            System.out.println("Generated: " + vehicleId + " → Road A, Lane 3 (Left-turn)");
        }

//          ROAD B
            // BL1 - Normal incoming lane
        if (random.nextDouble() < 0.35) {
            String vehicleId = "B1-" + (timestamp % 10000);
            writeVehicleToFile(LANE_B_FILE, vehicleId, 1, timestamp);
            System.out.println("Generated: " + vehicleId + " → Road B, Lane 1 (Normal)");
        }

        // BL3 - Left-turn ONLY lane
        if (random.nextDouble() < 0.15) {
            String vehicleId = "B3-" + (timestamp % 10000);
            writeVehicleToFile(LANE_B_FILE, vehicleId, 3, timestamp);
            System.out.println("Generated: " + vehicleId + " → Road B, Lane 3 (Left-turn)");
        }

        //  ROAD C

        // CL1 - Normal incoming lane
        if (random.nextDouble() < 0.4) {
            String vehicleId = "C1-" + (timestamp % 10000);
            writeVehicleToFile(LANE_C_FILE, vehicleId, 1, timestamp);
            System.out.println("Generated: " + vehicleId + " → Road C, Lane 1 (Normal)");
        }

        // CL3 - Left-turn ONLY lane
        if (random.nextDouble() < 0.15) {
            String vehicleId = "C3-" + (timestamp % 10000);
            writeVehicleToFile(LANE_C_FILE, vehicleId, 3, timestamp);
            System.out.println("Generated: " + vehicleId + " → Road C, Lane 3 (Left-turn)");
        }

        //Road D

        // DL1 - Normal incoming lane
        if (random.nextDouble() < 0.3) {
            String vehicleId = "D1-" + (timestamp % 10000);
            writeVehicleToFile(LANE_D_FILE, vehicleId, 1, timestamp);
            System.out.println("Generated: " + vehicleId + " → Road D, Lane 1 (Normal)");
        }

        // DL3 - Left-turn ONLY lane
        if (random.nextDouble() < 0.15) {
            String vehicleId = "D3-" + (timestamp % 10000);
            writeVehicleToFile(LANE_D_FILE, vehicleId, 3, timestamp);
            System.out.println(" Generated: " + vehicleId + " → Road D, Lane 3 (Left-turn)");
        }
    }

    /**
     * Write vehicle data to lane file
     * Format: VEHICLE_ID,LANE_NUMBER,TIMESTAMP
     */
    private static void writeVehicleToFile(String filename, String vehicleId, int laneNumber, long timestamp) {
        try (FileWriter fw = new FileWriter(filename, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            // Write in CSV format: vehicleId,laneNumber,timestamp
            out.println(vehicleId + "," + laneNumber + "," + timestamp);

        } catch (IOException e) {
            System.err.println("Error writing to " + filename + ": " + e.getMessage());
        }
    }

    //Clear all lane files on startup

    private static void clearLaneFiles() {
        clearFile(LANE_A_FILE);
        clearFile(LANE_B_FILE);
        clearFile(LANE_C_FILE);
        clearFile(LANE_D_FILE);
        System.out.println(" Cleared all lane files\n");
    }


     // Clear a single file

    private static void clearFile(String filename) {
        try (FileWriter fw = new FileWriter(filename, false)) {
            // Opening with append=false clears the file
        } catch (IOException e) {
            System.err.println("Warning: Could not clear " + filename);
        }
    }
}