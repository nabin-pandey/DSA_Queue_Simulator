package com.traffic.core;

public class Lane {

    private final VehicleQueue lane1;
    private final VehicleQueue lane2;
    private final VehicleQueue lane3;
    private final String roadId ;

    public Lane(String roadId) {
        this.roadId = roadId;
        this.lane1 = new VehicleQueue();
        this.lane2 = new VehicleQueue();
        this.lane3 = new VehicleQueue();

    }

    public String getRoadId() { return roadId; }


    public void enqueueToLane( int laneIndex,String vehicleId){
        switch(laneIndex){
            case 1: lane1.enqueue(vehicleId); break;
            case 2: lane2.enqueue(vehicleId); break;
            case 3: lane3.enqueue(vehicleId); break;
            default: throw new IllegalArgumentException("Invalid Lane Index: "+laneIndex);
        }
    }

    //Dequeue from Incoming Queue, Traffic Light: Green
    public String dequeueFromIncoming(){
        return lane1.dequeue();
    }

    //Dequeuing from Priority Queue
    public String dequeueFromPriority(){
        return lane2.dequeue();
    }

    public String dequeueFromLeftTurn(){
        return lane3.dequeue();
    }


    //Returning Lane Size
    public int incomingSize(){ return lane1.getSize(); }
    public int prioritySize(){ return lane2.getSize(); }
    public int leftTurnSize(){ return lane3.getSize(); }

    //Total Size of the Vehicle that is in the queue.
    public int totalSIze(){ return incomingSize() + prioritySize() + leftTurnSize(); }

    public int getTotalVehicles() {
        return incomingSize() + prioritySize() + leftTurnSize() + totalSIze() ;
    }
}
