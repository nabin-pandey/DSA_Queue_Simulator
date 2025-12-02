package com.traffic.core;

public class VehicleQueue {
    private CarNode front ;
    private CarNode rear;
    private int size;

    public VehicleQueue() {
        this.front = null ;
        this.rear = null ;
        this.size = 0;

    }

    public CarNode getFront(){
        return front;
    }
    public CarNode getRear(){
        return rear;
    }
    public int getSize(){
        return size ;
    }
    public void setSize(int size){
        this.size = size ;
        return ;
    }

    public void enqueue(String vehicleId){
        CarNode newNode = new CarNode(vehicleId) ;

        if(rear == null){
            this.front = newNode ;
            this.rear = newNode ;

        }else{
            rear.setNext(newNode) ;
            rear = newNode ;
        }
            this.size++ ;
        System.out.println( "Vehicle : " + vehicleId + "enqueued : " + this.size);
    }



    public void dequeue(){

        if(isEmpty()){
            System.out.println("Vehicle Queue is Empty");
            return ;
        }

        String removedID = return this.front.getVehicleId() ;
        this.front = this.front.getNext() ;
        this.size-- ;

        System.out.println("vehicle : " + removedID + " dequeued : " + this.size);
    }

    public boolean isEmpty(){
        return this.front == null ;
    }





}
