package com.traffic.gui;

import com.traffic.core.Lane;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrafficGenerator {

    private static final int LANE_WIDTH = 50;
    private static final int JUNCTION_SIZE = LANE_WIDTH * 4;
    private static final int ROAD_LENGTH = 300;

    private final Lane laneA;
    private final Lane laneB;
    private final Lane laneC;
    private final Lane laneD;

    private final Pane simulationPane;
    private final Random random = new Random();

    private final double centerX;
    private final double centerY;

    // Store waiting cars for each lane
    private final List<Rectangle> waitingCarsA = new ArrayList<>();
    private final List<Rectangle> waitingCarsB = new ArrayList<>();
    private final List<Rectangle> waitingCarsC = new ArrayList<>();
    private final List<Rectangle> waitingCarsD = new ArrayList<>();

    public TrafficGenerator(Lane laneA, Lane laneB, Lane laneC, Lane laneD,
                            Pane simulationPane, double centerX, double centerY) {
        this.laneA = laneA;
        this.laneB = laneB;
        this.laneC = laneC;
        this.laneD = laneD;
        this.simulationPane = simulationPane;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    // Generate random traffic for all lanes
    public void generateRandomTraffic() {
        if (random.nextDouble() < 0.4) {
            laneA.enqueueToLane(1,"A1-" + System.currentTimeMillis() % 1000);
            addWaitingCar("A");
        }
        //AL2 - Priority Lane
        if (random.nextDouble() < 0.25) {
            laneA.enqueueToLane(2, "A2-" + System.currentTimeMillis() % 1000);
            addWaitingCar("A");
        }
        if (random.nextDouble() < 0.35) {
            laneB.enqueueToLane(1,"B1-" + System.currentTimeMillis() % 1000);
            addWaitingCar("B");
        }
        if (random.nextDouble() < 0.5) {
            laneC.enqueueToLane(1,"C1-" + System.currentTimeMillis() % 1000);
            addWaitingCar("C");
        }
        if (random.nextDouble() < 0.3) {
            laneD.enqueueToLane(1,"D1-" + System.currentTimeMillis() % 1000);
            addWaitingCar("D");
        }


    }

    // Add a waiting car visual at the stop line
    private void addWaitingCar(String laneName) {
        Rectangle waitingCar = new Rectangle(20, 35, getRandomCarColor());
        waitingCar.setArcHeight(8);
        waitingCar.setArcWidth(8);
        waitingCar.setStrokeWidth(1);

        double laneOffset = LANE_WIDTH / 2.0;
        List<Rectangle> waitingList = null;

        switch (laneName) {
            case "A":
                waitingList = waitingCarsA;
                double xA = centerX - laneOffset;
                double yA = centerY - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45);
                waitingCar.setX(xA - 10);
                waitingCar.setY(yA);
                break;

            case "B":
                waitingList = waitingCarsB;
                double xB = centerX + laneOffset;
                double yB = centerY + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45);
                waitingCar.setX(xB - 10);
                waitingCar.setY(yB);
                break;

            case "C":
                waitingList = waitingCarsC;
                double xC = centerX + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45);
                double yC = centerY - laneOffset;
                waitingCar.setX(xC);
                waitingCar.setY(yC - 17.5);
                break;

            case "D":
                waitingList = waitingCarsD;
                double xD = centerX - JUNCTION_SIZE / 2.0 - 40 - (waitingList.size() * 45);
                double yD = centerY + laneOffset;
                waitingCar.setX(xD);
                waitingCar.setY(yD - 17.5);
                break;
        }

        if (waitingList != null) {
            waitingList.add(waitingCar);
            Platform.runLater(() -> simulationPane.getChildren().add(waitingCar));
        }
    }

    // Remove and animate waiting cars when light turns green
    public void releaseWaitingCars(String laneName, int count) {
        List<Rectangle> waitingList = getWaitingList(laneName);

        if (waitingList != null && !waitingList.isEmpty()) {
            int carsToRelease = Math.min(count, waitingList.size());

            for (int i = 0; i < carsToRelease; i++) {
                if (!waitingList.isEmpty()) {
                    Rectangle waitingCar = waitingList.remove(0);
                    Platform.runLater(() -> simulationPane.getChildren().remove(waitingCar));
                    repositionWaitingCars(laneName);
                }
            }
        }
    }

    // Reposition waiting cars after one is removed
    private void repositionWaitingCars(String laneName) {
        List<Rectangle> waitingList = getWaitingList(laneName);
        if (waitingList == null) return;

        double laneOffset = LANE_WIDTH / 2.0;

        for (int i = 0; i < waitingList.size(); i++) {
            Rectangle car = waitingList.get(i);

            switch (laneName) {
                case "A":
                    double yA = centerY - JUNCTION_SIZE / 2.0 - 60 - (i * 45);
                    car.setY(yA);
                    break;

                case "B":
                    double yB = centerY + JUNCTION_SIZE / 2.0 + 60 + (i * 45);
                    car.setY(yB);
                    break;

                case "C":
                    double xC = centerX + JUNCTION_SIZE / 2.0 + 60 + (i * 45);
                    car.setX(xC);
                    break;

                case "D":
                    double xD = centerX - JUNCTION_SIZE / 2.0 - 60 - (i * 45);
                    car.setX(xD);
                    break;
            }
        }
    }

    // Get the waiting list for a lane
    private List<Rectangle> getWaitingList(String laneName) {
        switch (laneName) {
            case "A": return waitingCarsA;
            case "B": return waitingCarsB;
            case "C": return waitingCarsC;
            case "D": return waitingCarsD;
            default: return null;
        }
    }

    // Create and animate a car through the junction
    public void createAndAnimateCar(String laneName) {
        System.out.println("Creating car animation for lane: " + laneName);
        Rectangle car = new Rectangle(20, 35, getRandomCarColor());
        car.setArcHeight(8);
        car.setArcWidth(8);
        car.setStroke(Color.BLACK);
        car.setStrokeWidth(1);

        // Randomly choose direction: 0=straight, 1=left, 2=right
        int direction = random.nextInt(3);

        double roadThickness = LANE_WIDTH * 3;
        double laneOffset = LANE_WIDTH / 2.0;

        Path path = new Path();
        MoveTo start = null;

        switch(laneName) {
            case "A": // From top going down
                start = new MoveTo(centerX - laneOffset, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 50);
                path.getElements().add(start);
                path.getElements().add(new LineTo(centerX - laneOffset, centerY - JUNCTION_SIZE / 2.0));

                if (direction == 0) { // Straight to B
                    path.getElements().add(new LineTo(centerX - laneOffset, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
                } else if (direction == 1) { // Left to C
                    path.getElements().add(new QuadCurveTo(
                            centerX - laneOffset, centerY,
                            centerX + laneOffset, centerY));
                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY));
                } else { // Right to D
                    path.getElements().add(new QuadCurveTo(
                            centerX - laneOffset, centerY,
                            centerX - roadThickness / 2.0 - 30, centerY));
                    path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY));
                }
                break;

            case "B": // From bottom going up
                start = new MoveTo(centerX + laneOffset, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 50);
                path.getElements().add(start);
                path.getElements().add(new LineTo(centerX + laneOffset, centerY + JUNCTION_SIZE / 2.0));

                if (direction == 0) { // Straight to A
                    path.getElements().add(new LineTo(centerX + laneOffset, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                } else if (direction == 1) { // Left to D
                    path.getElements().add(new QuadCurveTo(
                            centerX + laneOffset, centerY,
                            centerX - laneOffset, centerY));
                    path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY));
                } else { // Right to C
                    path.getElements().add(new QuadCurveTo(
                            centerX + laneOffset, centerY,
                            centerX + roadThickness / 2.0 + 30, centerY));
                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY));
                }
                break;

            case "C": // From right going left
                start = new MoveTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 50, centerY - laneOffset);
                path.getElements().add(start);
                path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0, centerY - laneOffset));

                if (direction == 0) { // Straight to D
                    path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY - laneOffset));
                } else if (direction == 1) { // Left to B
                    path.getElements().add(new QuadCurveTo(
                            centerX, centerY - laneOffset,
                            centerX, centerY + laneOffset));
                    path.getElements().add(new LineTo(centerX, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
                } else { // Right to A
                    path.getElements().add(new QuadCurveTo(
                            centerX, centerY - laneOffset,
                            centerX, centerY - roadThickness / 2.0 - 30));
                    path.getElements().add(new LineTo(centerX, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                }
                break;

            case "D": // From left going right
                start = new MoveTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 50, centerY + laneOffset);
                path.getElements().add(start);
                path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0, centerY + laneOffset));

                if (direction == 0) { // Straight to C
                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY + laneOffset));
                } else if (direction == 1) { // Left to A
                    path.getElements().add(new QuadCurveTo(
                            centerX, centerY + laneOffset,
                            centerX, centerY - laneOffset));
                    path.getElements().add(new LineTo(centerX, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                } else { // Right to B
                    path.getElements().add(new QuadCurveTo(
                            centerX, centerY + laneOffset,
                            centerX, centerY + roadThickness / 2.0 + 30));
                    path.getElements().add(new LineTo(centerX, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
                }
                break;
        }

        simulationPane.getChildren().add(car);
        PathTransition pt = new PathTransition(Duration.millis(2000), path, car);
        pt.setOnFinished(event -> simulationPane.getChildren().remove(car));
        pt.play();
    }

    private Color getRandomCarColor() {
        Color[] colors = {
                Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE,
                Color.PURPLE, Color.YELLOW, Color.CYAN
        };
        return colors[random.nextInt(colors.length)];
    }
}