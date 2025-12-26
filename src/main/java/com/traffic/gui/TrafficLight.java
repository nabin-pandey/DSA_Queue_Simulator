
package com.traffic.gui;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class TrafficLight {

    public enum State { RED, YELLOW, GREEN }

    private final Circle circle;
    private State state = State.RED;

    public TrafficLight(Circle circle) {
        this.circle = circle;
        setState(State.RED);
    }
    public void setState(State s) {
        final State target = (s == null) ? State.RED : s; // s is  final variable for lambda expression
        this.state = target;

        Runnable updateUI = () -> {
            switch (target) {
                case GREEN:
                    circle.setFill(Color.GREEN);
                    break;
                case YELLOW:
                    circle.setFill(Color.YELLOW);
                    break;
                case RED:
                default:
                    circle.setFill(Color.RED);
                    break;
            }
        };

        if (Platform.isFxApplicationThread()) {
            updateUI.run();
        } else {
            Platform.runLater(updateUI);
        }
    }


}
