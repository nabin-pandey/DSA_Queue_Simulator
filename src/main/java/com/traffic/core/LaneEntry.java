package com.traffic.core;

public class LaneEntry implements Comparable<LaneEntry> {

    private String roadId;
    private int priorityScore ;
    private int vehicleCount ;

    public LaneEntry(String roadId, int initialCount) {
        this.roadId = roadId;
        this.priorityScore = 1;
        this.vehicleCount = initialCount;
    }

    public String getRoadId() { return roadId; }
    public int getPriorityScore() { return priorityScore; }
    public int getVehicleCount() { return vehicleCount; }

    public void setPriorityScore(int score) { this.priorityScore = score; }
    public void setVehicleCount(int count) { this.vehicleCount = count; }

    @Override
    public int compareTo(LaneEntry other) {
        return Integer.compare(this.priorityScore, other.priorityScore);
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        LaneEntry laneEntry = (LaneEntry) o;
        return roadId.equals(laneEntry.roadId);
    }

    @Override
    public int hashCode() {
        return roadId.hashCode();
    }
}
