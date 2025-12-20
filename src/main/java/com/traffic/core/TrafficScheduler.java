package com.traffic.core;

import java.util.PriorityQueue;
import java.util.Collection;

public class TrafficScheduler {
    private PriorityQueue<LaneEntry> laneQueue;

    public TrafficScheduler(Collection<LaneEntry> laneEntries) {
        laneQueue = new PriorityQueue<>(laneEntries);
    }

    public void CheckandUpdatePriority(LaneEntry laneEntry, int currentCarCount) {
        laneQueue.remove(laneEntry);
        laneEntry.setVehicleCount(currentCarCount);
        String roadId = laneEntry.getRoadId();


        /* ENUM for priority
            1 - Highest Priority
            5-Medium Priority
            10- Lowest Priority
        */
        if (roadId.equals("A")) {
            if (currentCarCount > 10) {
                laneEntry.setPriorityScore(1);
                System.out.println("AL2 PRIORITY LANE OVERCROWDED! Count: " + currentCarCount);
            } else if (currentCarCount > 5) {
                laneEntry.setPriorityScore(5);
            } else {
                laneEntry.setPriorityScore(10);
            }
        } else {
            // Roads B, C, D - never get highest priority
            if (currentCarCount > 10) {
                laneEntry.setPriorityScore(5); // Medium priority only
                System.out.println("Road " + roadId + " crowded: " + currentCarCount);
            } else {
                laneEntry.setPriorityScore(10);
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
}
