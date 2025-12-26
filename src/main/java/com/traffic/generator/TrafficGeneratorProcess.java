package com.traffic.generator;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TrafficGeneratorProcess {

    private static final String LANE_A_FILE = "lanea.txt";
    private static final String LANE_B_FILE = "laneb.txt";
    private static final String LANE_C_FILE = "lanec.txt";
    private static final String LANE_D_FILE = "laned.txt";

    private static final Random RNG = new Random();
    private static volatile boolean running = true;


    private static final int CYCLE_MS = 1000;          // 1 second
    private static final double EXTRA_CAR_PROB = 0.40; // chance of second car

    private static final List<SourceLane> SOURCES = Arrays.asList(
            // Road A outgoing
            new SourceLane("A", 2, Arrays.asList(new Dest("B", 1), new Dest("D", 1))),
            new SourceLane("A", 3, Arrays.asList(new Dest("C", 1))),

            // Road B outgoing
            new SourceLane("B", 2, Arrays.asList(new Dest("A", 1))),
            new SourceLane("B", 3, Arrays.asList(new Dest("D", 1))),

            // Road C outgoing
            new SourceLane("C", 2, Arrays.asList(new Dest("D", 1), new Dest("A", 1))),
            new SourceLane("C", 3, Arrays.asList(new Dest("B", 1))),

            // Road D outgoing
            new SourceLane("D", 2, Arrays.asList(new Dest("C", 1), new Dest("B", 1))),
            new SourceLane("D", 3, Arrays.asList(new Dest("A", 1)))
    );

    // round-robin pointer
    private static int rrIndex = 0;

    public static void main(String[] args) {
        System.out.println("TrafficGeneratorProcess STARTED");
        System.out.println("Press Ctrl+C to stop.\n");

        clearLaneFiles();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            System.out.println("\nTraffic Generator shutting down...");
        }));

        long seq = 0;
        while (running) {
            try {
                seq++;
                generateCycle(seq);
                TimeUnit.MILLISECONDS.sleep(CYCLE_MS);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                System.err.println("Generation error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Traffic Generator STOPPED");
    }

    private static void generateCycle(long seq) {
        long now = System.currentTimeMillis();

        // one vehicle from next outgoing lane in round-robin
        SourceLane s1 = SOURCES.get(rrIndex);
        rrIndex = (rrIndex + 1) % SOURCES.size();
        emitFromSource(s1, seq, now);


        if (RNG.nextDouble() < EXTRA_CAR_PROB) {
            SourceLane s2 = SOURCES.get(RNG.nextInt(SOURCES.size()));
            emitFromSource(s2, seq + 9999, now); // different suffix to reduce id collisions
        }
    }

    private static void emitFromSource(SourceLane src, long seq, long now) {
        //Will not generate vehicle Lane 1
        if (src.lane == 1) return;

        // itself choose destination from its allowed list
        Dest dst = src.dests.get(RNG.nextInt(src.dests.size()));

        //  car must move to  lane 1 not others
        if (dst.lane != 1) return;

        String id = src.road + src.lane + dst.road + dst.lane + "-" + (seq % 100000);
        writeVehicle(fileForRoad(src.road), id, src.lane, now);

        System.out.println("Generated: " + id + " (src " + src.road + src.lane + " -> " + dst.road + dst.lane + ")");
    }

    private static String fileForRoad(String road) {
        switch (road) {
            case "A": return LANE_A_FILE;
            case "B": return LANE_B_FILE;
            case "C": return LANE_C_FILE;
            case "D": return LANE_D_FILE;
            default: throw new IllegalArgumentException("Invalid road: " + road);
        }
    }

    private static synchronized void writeVehicle(String file, String id, int lane, long time) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(id + "," + lane + "," + time);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to " + file + ": " + e.getMessage());
        }
    }

    private static void clearLaneFiles() {
        delete(LANE_A_FILE);
        delete(LANE_B_FILE);
        delete(LANE_C_FILE);
        delete(LANE_D_FILE);
    }

    private static void delete(String f) {
        File file = new File(f);
        if (file.exists()) file.delete();
    }

    // Simple structs
    private static class Dest {
        final String road;
        final int lane; // must be 1
        Dest(String road, int lane) { this.road = road; this.lane = lane; }
    }

    private static class SourceLane {
        final String road;
        final int lane; // must be 2 or 3
        final List<Dest> dests;
        SourceLane(String road, int lane, List<Dest> dests) {
            this.road = road;
            this.lane = lane;
            this.dests = dests;
        }
    }
}
