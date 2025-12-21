package com.traffic.core;

import java.util.PriorityQueue;
import java.util.Collection;

public class TrafficScheduler {
    private PriorityQueue<LaneEntry> laneQueue;
    private boolean priorityModeActive = false ;

    public TrafficScheduler(Collection<LaneEntry> laneEntries) {
        laneQueue = new PriorityQueue<>(laneEntries);
    }

    public void CheckandUpdatePriority(LaneEntry laneEntry, int incomingCount, int priorityLaneCount) {
        laneQueue.remove(laneEntry);
        laneEntry.setVehicleCount(incomingCount);
        laneEntry.setPriorityLaneCount(priorityLaneCount);
        String roadId = laneEntry.getRoadId();


        /* ENUM for priority
            1 - Highest Priority
            5-Medium Priority
            10- Lowest Priority
        */
        if (roadId.equals("A")) {

            // Check AL2 status

            if (priorityLaneCount > 10) {

                laneEntry.setPriorityScore(1);

                priorityModeActive = true;

                System.out.println("ALERT: Road A Priority Lane (AL2) Overcrowded ! Count: " + priorityLaneCount +

                        " | AL1: " + incomingCount + " - HIGHEST PRIORITY activated");

            }

            // Priority mode ends when AL2 drops below 5

            else if (priorityLaneCount < 5 && priorityModeActive) {

                priorityModeActive = false;

                // Still check incoming lane for normal priority

                if (incomingCount > 10) {

                    laneEntry.setPriorityScore(1);

                    System.out.println("Road A: AL2 cleared, but AL1 overcrowded (" + incomingCount + ") - HIGH PRIORITY");

                } else if (incomingCount > 5) {

                    laneEntry.setPriorityScore(5);

                    System.out.println("Road A: AL2 cleared, AL1 medium traffic (" + incomingCount + ") - MEDIUM PRIORITY");

                } else {

                    laneEntry.setPriorityScore(10);

                    System.out.println("✓ Road A: AL2 cleared (" + priorityLaneCount + "), returning to normal - NORMAL PRIORITY");

                }

            }

            // AL2 has less than 10, but check incoming lane

            else {

                if (incomingCount > 10) {

                    laneEntry.setPriorityScore(1);

                    System.out.println("⚠️ Road A: AL1 overcrowded (" + incomingCount + ") - HIGH PRIORITY");

                } else if (incomingCount > 5) {

                    laneEntry.setPriorityScore(5);

                    System.out.println("Road A: AL1 medium traffic (" + incomingCount + ") - MEDIUM PRIORITY");

                } else {

                    laneEntry.setPriorityScore(10);

                    System.out.println("Road A: Normal traffic - NORMAL PRIORITY");

                }

            }}
        else {

            // Only check incoming lane (AL1 equivalent) for these roads

            if (incomingCount > 10) {

                laneEntry.setPriorityScore(1);

                System.out.println(" Road " + roadId + ": Overcrowded (" + incomingCount + ") - HIGH PRIORITY");

            } else if (incomingCount > 5) {

                laneEntry.setPriorityScore(5);

                System.out.println("Road " + roadId + ": Medium traffic (" + incomingCount + ") - MEDIUM PRIORITY");

            } else {

                laneEntry.setPriorityScore(10);

                System.out.println("Road " + roadId + ": Normal traffic - NORMAL PRIORITY");

            }

        }



        laneQueue.add(laneEntry);

    }

    public LaneEntry getNextLaneToServe(){  return laneQueue.peek();    }

    public String serverAndRotateLane(){
        LaneEntry servedLane = laneQueue.poll();

        if(servedLane == null) return "No lanes available." ;

        String servedId = servedLane.getRoadId();
        
        System.out.println(" Scheduler selected lane: " + servedId + " priority=" + servedLane.getPriorityScore() + ", count=" + servedLane.getVehicleCount() );
        
        laneQueue.add(servedLane);
        return servedId;
    }

      //Check function for AL2 Priority
    public boolean isPriorityModeActive() {

        return priorityModeActive;

    }

    public int calculateAverageVehicles(Collection<LaneEntry> entries) {

        int total = 0;
        int count = 0;
        for (LaneEntry entry : entries) {
            // If AL2 priority mode is active, don't include Road A in average
            if (priorityModeActive && entry.getRoadId().equals("A")) {
                continue;
            }
            total += entry.getVehicleCount();
            count++;
        }
        return count > 0 ? (int) Math.ceil((double) total / count) : 0;
    }
}

