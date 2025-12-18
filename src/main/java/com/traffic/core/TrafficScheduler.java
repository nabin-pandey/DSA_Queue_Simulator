package com.traffic.core;

import java.util.PriorityQueue;
import java.util.Collection;

public class TrafficScheduler {
    private PriorityQueue<LaneEntry> laneQueue;

    public TrafficScheduler(Collection<LaneEntry> laneEntries) {
        laneQueue = new PriorityQueue<>(laneEntries);
    }

    public void CheckandUpdatePriority(LaneEntry laneA, int currentCarCount) {
        laneQueue.remove(laneA);

        laneA.setVehicleCount(currentCarCount);

        //Priority Lane Logic (Lower score = Higher priority in PriorityQueue)
        if(currentCarCount > 10){
            laneA.setPriorityScore(1); // HIGH PRIORITY (low score)
            System.out.println("Alert : Road : " + laneA.getRoadId() + " is overcrowded" + "(Count :"+ currentCarCount +") set to HIGH PRIORITY" );
        }else if(currentCarCount > 5){
            laneA.setPriorityScore(5); // MEDIUM PRIORITY
            System.out.println("Road : "+ laneA.getRoadId() +  "(Count :"+ currentCarCount +") set to MEDIUM PRIORITY");
        }else{
            laneA.setPriorityScore(10); // LOW PRIORITY (high score)
            System.out.println("Road " + laneA.getRoadId() + " set to NORMAL PRIORITY");
        }

        laneQueue.add(laneA);
    }

    public LaneEntry getNextLaneToServe(){  return laneQueue.peek();    }

    public String serverAndRotateLane(){
        LaneEntry servedLane = laneQueue.poll();

        if(servedLane == null) return "No lanes available." ;

        String servedId = servedLane.getRoadId();
        
        System.out.println(">>> Scheduler selected lane: " + servedId + " (priority=" + servedLane.getPriorityScore() + ", count=" + servedLane.getVehicleCount() + ")");
        
        laneQueue.add(servedLane);
        return servedId;
    }
}
