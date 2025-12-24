package com.traffic.generator;

import java.io.*;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
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

        // Main loop
        int cycle = 0;
        while (running) {
            try {
                cycle++;
                System.out.println("\n--- Cycle " + cycle + " ---");

                // Generate 1-2 vehicles per cycle
                generateTraffic();

                // Generate new vehicles every 2.5 seconds
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
        long time = System.currentTimeMillis();
        int generated = 0;

        // Road A
        if (random.nextDouble() < 0.3) {
            writeVehicle(LANE_A_FILE, "A1-" + (time % 10000), 1, time);
            generated++;
        }
        if (random.nextDouble() < 0.2) {
            writeVehicle(LANE_A_FILE, "A2-" + (time % 10000), 2, time);
            generated++;
        }
        if (random.nextDouble() < 0.15) {
            writeVehicle(LANE_A_FILE, "A3-" + (time % 10000), 3, time);
            generated++;
        }

        // Road B
        if (random.nextDouble() < 0.25) {
            writeVehicle(LANE_B_FILE, "B1-" + (time % 10000), 1, time);
            generated++;
        }
        if (random.nextDouble() < 0.1) {
            writeVehicle(LANE_B_FILE, "B3-" + (time % 10000), 3, time);
            generated++;
        }

        // Road C
        if (random.nextDouble() < 0.25) {
            writeVehicle(LANE_C_FILE, "C1-" + (time % 10000), 1, time);
            generated++;
        }
        if (random.nextDouble() < 0.1) {
            writeVehicle(LANE_C_FILE, "C3-" + (time % 10000), 3, time);
            generated++;
        }

        // Road D
        if (random.nextDouble() < 0.2) {
            writeVehicle(LANE_D_FILE, "D1-" + (time % 10000), 1, time);
            generated++;
        }
        if (random.nextDouble() < 0.1) {
            writeVehicle(LANE_D_FILE, "D3-" + (time % 10000), 3, time);
            generated++;
        }

        if (generated > 0) {
            System.out.println("Generated " + generated + " vehicles");
        }
    }

    private static synchronized void writeVehicle(String file, String id, int lane, long time) {
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(id + "," + lane + "," + time);
            bw.newLine();
            System.out.println("  " + id + " -> L" + lane);
        } catch (IOException e) {
            System.err.println("Error writing to " + file);
        }
    }

    //Clear all lane files on startup

    private static void clearLaneFiles() {
        new File(LANE_A_FILE).delete();
        new File(LANE_B_FILE).delete();
        new File(LANE_C_FILE).delete();
        new File(LANE_D_FILE).delete();
    }
}