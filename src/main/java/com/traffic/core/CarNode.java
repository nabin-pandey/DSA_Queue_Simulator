package com.traffic.core;

public class CarNode {

    private  String vehicleId;

    private CarNode next;

    public CarNode(String vehicleId) {
        this.vehicleId = vehicleId;
        this.next = null;
    }
    //Getters and Setters
    public String getVehicleId() {
        return vehicleId;
    }
    public CarNode getNext() {
        return next;
    }
    public void setNext(CarNode next) {
        this.next = next;
    }

}
