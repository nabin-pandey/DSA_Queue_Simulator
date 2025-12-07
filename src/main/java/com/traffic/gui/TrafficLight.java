package com.traffic.gui;

import javafx.application.Platform;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import java.awt.*;

public class TrafficLight {
    public enum State {RED, YELLOW, GREEN}

    private final Circle circle;
    private State state = State.RED;  //Fixed the initial state to RED earlier it wasn't declared.

    public TrafficLight(Circle circle) {
        this.circle = circle ;
        setState(State.RED) ;

    }



    public State getState() {   return state; }

    public void setState(State s) {

        this.state = s;

        Platform.runLater(() -> {
            switch (s){
                case GREEN : circle.setFill(Color.GREEN);
                break ;

                case YELLOW: circle.setFill(Color.YELLOW);
                break ;

                case RED:
                default:
                    circle.setFill(Color.RED);
                    break ;
            }
        });

    }

    public Circle getCircle() { return circle; }

}