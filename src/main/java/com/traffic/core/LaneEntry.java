package com.traffic.core;

public class LaneEntry implements Comparable<LaneEntry> {

    private String roadId;
    private int priorityScore ;
    private int vehicleCount ;
    private int priorityLaneCount ;


    public LaneEntry(String roadId, int initialCount) {
        this.roadId = roadId;
        this.priorityScore = 10;
        this.vehicleCount = initialCount;
        this.priorityLaneCount = 0 ;
    }

    // Getters setters
    public String getRoadId() {
        return roadId;
    }

    public int getPriorityScore() {
        return priorityScore;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }


    public int getPriorityLaneCount() {
        return priorityLaneCount;
    }

    public void setPriorityScore(int score) {
        this.priorityScore = score;
    }
    public void setVehicleCount(int count) {
        this.vehicleCount = count;
    }
    public void setPriorityLaneCount(int count) {
        this.priorityLaneCount = count;
    }
    @Override
    public int compareTo(LaneEntry other) {
        // Lower priorityScore = higher priority
        int scoreCmp = Integer.compare(this.priorityScore, other.priorityScore);
        if (scoreCmp != 0) return scoreCmp;

        //  If same score, higher total vehicles first
        int thisTotalVehicles = this.vehicleCount + this.priorityLaneCount;
        int otherTotalVehicles = other.vehicleCount + other.priorityLaneCount;
        return Integer.compare(otherTotalVehicles, thisTotalVehicles);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LaneEntry laneEntry = (LaneEntry) o;
        return roadId.equals(laneEntry.roadId);
    }

    @Override
    public int hashCode() {
        return roadId.hashCode();
    }

    @Override
    public String toString() {
        return "LaneEntry{" +
               "road=" + roadId +
               ", priority=" + priorityScore +
               ", L3=" + vehicleCount +
               ", L2=" + priorityLaneCount +
               ", total=" + (vehicleCount + priorityLaneCount) +
               '}';
    }
}