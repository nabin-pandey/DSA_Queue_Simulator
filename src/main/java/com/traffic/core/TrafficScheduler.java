
package com.traffic.core;

import java.util.Collection;
import java.util.PriorityQueue;

public class TrafficScheduler {

    private final PriorityQueue<LaneEntry> laneQueue;
    private boolean priorityModeActive = false;

    // Priority applies ONLY to Road A (AL2)
    private static final String PRIORITY_ROAD = "A";
    private static final int ENTER_PRIORITY_AT = 10;

    // Priority ends when AL2 <= 5
    private static final int EXIT_PRIORITY_AT = 5;

    public TrafficScheduler(Collection<LaneEntry> laneEntries) {
        laneQueue = new PriorityQueue<>(laneEntries);
    }

    public void CheckandUpdatePriority(LaneEntry laneEntry, int incomingCount, int priorityLaneCount) {
        laneQueue.remove(laneEntry);

        laneEntry.setVehicleCount(incomingCount);
        laneEntry.setPriorityLaneCount(priorityLaneCount);

        String roadId = laneEntry.getRoadId();

        // Priority for AL2 logic
        if (PRIORITY_ROAD.equals(roadId)) {


            if (!priorityModeActive && priorityLaneCount >= ENTER_PRIORITY_AT) {
                priorityModeActive = true;
                System.out.println("ALERT: Priority ON for Road A (AL2 >= " + ENTER_PRIORITY_AT + "). AL2=" + priorityLaneCount);
            }

            // off when AL2 drops to 5
            if (priorityModeActive && priorityLaneCount <= EXIT_PRIORITY_AT) {
                priorityModeActive = false;
                System.out.println("✓ Priority OFF for Road A (AL2 <= " + EXIT_PRIORITY_AT + "). AL2=" + priorityLaneCount);
            }


            if (priorityModeActive) {
                laneEntry.setPriorityScore(0);
            } else {
                // Normal scoring for A
                int totalA = incomingCount + priorityLaneCount;
                if (totalA > 10) laneEntry.setPriorityScore(1);
                else if (totalA > 5) laneEntry.setPriorityScore(5);
                else laneEntry.setPriorityScore(10);
            }

        } else {
            // Road B,C,D will stop and road A only moves
            if (priorityModeActive) {
                laneEntry.setPriorityScore(50);
            } else {
                // Normal scoring for B/C/D
                int total = incomingCount + priorityLaneCount;
                if (total > 10) laneEntry.setPriorityScore(1);
                else if (total > 5) laneEntry.setPriorityScore(5);
                else if (total > 0) laneEntry.setPriorityScore(10);
                else laneEntry.setPriorityScore(15);
            }
        }

        laneQueue.add(laneEntry);
    }

    public String serverAndRotateLane() {
        // If A-priority mode is active, alwys  serve A
        if (priorityModeActive) return PRIORITY_ROAD;

        LaneEntry servedLane = laneQueue.poll();
        if (servedLane == null) {
            System.err.println("ERROR: No lanes available in queue!");
            return "A";
        }

        String servedId = servedLane.getRoadId();

        // Re-add for next cycle
        laneQueue.add(servedLane);
        return servedId;
    }

    public boolean isPriorityModeActive() {
        return priorityModeActive;
    }
}
