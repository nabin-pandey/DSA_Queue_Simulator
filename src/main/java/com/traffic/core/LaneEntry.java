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
        // First compare by priority score (lower is higher priority)
        int scoreComparison = Integer.compare(this.priorityScore, other.priorityScore);
        if (scoreComparison != 0) {
            return scoreComparison;
        }
        // If scores are equal, prioritize by vehicle count (more vehicles = higher priority)
        return Integer.compare(other.vehicleCount, this.vehicleCount);
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
