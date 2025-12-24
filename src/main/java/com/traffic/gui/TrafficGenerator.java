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
    private final List<Rectangle> waitingCarsA_L1 = new ArrayList<>();
    private final List<Rectangle> waitingCarsA_L2 = new ArrayList<>();
    private final List<Rectangle> waitingCarsA_L3 = new ArrayList<>();
    private final List<Rectangle> waitingCarsB_L1 = new ArrayList<>();
    private final List<Rectangle> waitingCarsB_L3 = new ArrayList<>();
    private final List<Rectangle> waitingCarsC_L1 = new ArrayList<>();
    private final List<Rectangle> waitingCarsC_L3 = new ArrayList<>();
    private final List<Rectangle> waitingCarsD_L1 = new ArrayList<>();
    private final List<Rectangle> waitingCarsD_L3 = new ArrayList<>();

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
            addWaitingCar("A",1);
        }
        //AL2 - Priority Lane
        if (random.nextDouble() < 0.25) {
            laneA.enqueueToLane(2, "A2-" + System.currentTimeMillis() % 1000);
            addWaitingCar("A",2);
        }
        if(random.nextDouble() < 0.15){
            laneA.enqueueToLane(3, "A3- "+System.currentTimeMillis()%10000);
            addWaitingCar("A" , 3);
        }
        if (random.nextDouble() < 0.35) {
            laneB.enqueueToLane(1, "B1-" + System.currentTimeMillis() % 10000);
            addWaitingCar("B", 1);
        }
        if (random.nextDouble() < 0.15) {
            laneB.enqueueToLane(3, "B3-" + System.currentTimeMillis() % 10000);
            addWaitingCar("B", 3);
        }

        // Road C
        if (random.nextDouble() < 0.4) {
            laneC.enqueueToLane(1, "C1-" + System.currentTimeMillis() % 10000);
            addWaitingCar("C", 1);
        }
        if (random.nextDouble() < 0.15) {
            laneC.enqueueToLane(3, "C3-" + System.currentTimeMillis() % 10000);
            addWaitingCar("C", 3);
        }

        // Road D
        if (random.nextDouble() < 0.3) {
            laneD.enqueueToLane(1, "D1-" + System.currentTimeMillis() % 10000);
            addWaitingCar("D", 1);
        }
        if (random.nextDouble() < 0.15) {
            laneD.enqueueToLane(3, "D3-" + System.currentTimeMillis() % 10000);
            addWaitingCar("D", 3);
        }
    }

    // Add a waiting car visual at the stop line
    public  void addWaitingCar(String laneName, int laneNumber) {
        Rectangle waitingCar = new Rectangle(20, 35, getRandomCarColor());
        waitingCar.setArcHeight(8);
        waitingCar.setArcWidth(8);
        waitingCar.setStrokeWidth(1);

        double laneOffset = LANE_WIDTH / 2.0;
        List<Rectangle> waitingList = null;

        switch (laneName) {
            case "A":
                if (laneNumber == 1) {
                    waitingList = waitingCarsA_L1;
                    double xA1 = centerX - laneOffset;
                    double yA1 = centerY - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45);
                    waitingCar.setX(xA1 - 10);
                    waitingCar.setY(yA1);
                } else if (laneNumber == 2) {
                    waitingList = waitingCarsA_L2;
                    double xA2 = centerX - laneOffset - LANE_WIDTH;
                    double yA2 = centerY - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45);
                    waitingCar.setX(xA2 - 10);
                    waitingCar.setY(yA2);
                    waitingCar.setFill(Color.GOLD); // Priority vehicles are gold
                } else if (laneNumber == 3) {
                    waitingList = waitingCarsA_L3;
                    double xA3 = centerX - laneOffset + LANE_WIDTH;
                    double yA3 = centerY - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45);
                    waitingCar.setX(xA3 - 10);
                    waitingCar.setY(yA3);
                }
                break;

            case "B":
                if (laneNumber == 1) {
                    waitingList = waitingCarsB_L1;
                    double xB1 = centerX + laneOffset;
                    double yB1 = centerY + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45);
                    waitingCar.setX(xB1 - 10);
                    waitingCar.setY(yB1);
                } else if (laneNumber == 3) {
                    waitingList = waitingCarsB_L3;
                    double xB3 = centerX + laneOffset - LANE_WIDTH;
                    double yB3 = centerY + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45);
                    waitingCar.setX(xB3 - 10);
                    waitingCar.setY(yB3);
                }
                break;

            case "C":
                if (laneNumber == 1) {
                    waitingList = waitingCarsC_L1;
                    double xC1 = centerX + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45);
                    double yC1 = centerY - laneOffset;
                    waitingCar.setX(xC1);
                    waitingCar.setY(yC1 - 17.5);
                } else if (laneNumber == 3) {
                    waitingList = waitingCarsC_L3;
                    double xC3 = centerX + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45);
                    double yC3 = centerY - laneOffset + LANE_WIDTH;
                    waitingCar.setX(xC3);
                    waitingCar.setY(yC3 - 17.5);
                }
                break;

            case "D":
                if (laneNumber == 1) {
                    waitingList = waitingCarsD_L1;
                    double xD1 = centerX - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45);
                    double yD1 = centerY + laneOffset;
                    waitingCar.setX(xD1);
                    waitingCar.setY(yD1 - 17.5);
                } else if (laneNumber == 3) {
                    waitingList = waitingCarsD_L3;
                    double xD3 = centerX - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45);
                    double yD3 = centerY + laneOffset - LANE_WIDTH;
                    waitingCar.setX(xD3);
                    waitingCar.setY(yD3 - 17.5);
                }
                break;
        }

        if (waitingList != null) {
            waitingList.add(waitingCar);
            Platform.runLater(() -> simulationPane.getChildren().add(waitingCar));
        }
    }

    // Remove and animate waiting cars when light turns green
    public void releaseWaitingCars(String laneName,int laneNumber, int count) {
        List<Rectangle> waitingList = getWaitingList(laneName,laneNumber);

        if (waitingList != null && !waitingList.isEmpty()) {
            int carsToRelease = Math.min(count, waitingList.size());

            for (int i = 0; i < carsToRelease; i++) {
                if (!waitingList.isEmpty()) {
                    Rectangle waitingCar = waitingList.remove(0);
                    Platform.runLater(() -> simulationPane.getChildren().remove(waitingCar));
                    repositionWaitingCars(laneName, laneNumber);
                }
            }
        }
    }

    private void repositionWaitingCars(String laneName, int laneNumber) {
        List<Rectangle> waitingList = getWaitingList(laneName, laneNumber);
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

    private List<Rectangle> getWaitingList(String laneName, int laneNumber) {
        switch (laneName) {
            case "A":
                if (laneNumber == 1) return waitingCarsA_L1;
                if (laneNumber == 2) return waitingCarsA_L2;
                if (laneNumber == 3) return waitingCarsA_L3;
                break;
            case "B":
                if (laneNumber == 1) return waitingCarsB_L1;
                if (laneNumber == 3) return waitingCarsB_L3;
                break;
            case "C":
                if (laneNumber == 1) return waitingCarsC_L1;
                if (laneNumber == 3) return waitingCarsC_L3;
                break;
            case "D":
                if (laneNumber == 1) return waitingCarsD_L1;
                if (laneNumber == 3) return waitingCarsD_L3;
                break;


        }
        return null ;
    }

    // Create and animate a car through the junction
    public void createAndAnimateCar(String laneName, int laneNumber) {
        System.out.println("Creating a car in: " + laneName + " Lane " + laneNumber);

        Rectangle car = new Rectangle(20, 35, getRandomCarColor());
        if (laneNumber == 2) {
            car.setFill(Color.GOLD); // Priority vehicles are gold
        }
        car.setArcHeight(8);
        car.setArcWidth(8);
        car.setStroke(Color.BLACK);
        car.setStrokeWidth(1);

        int direction;

        // Lane 3 = LEFT-TURN ONLY
        if (laneNumber == 3) {
            direction = 1; // Force left turn
        }
        // Lane 1 & 2 = Straight or Right (NOT left)
        else {
            direction = random.nextInt(2); // 0=straight, 1=right
            if (direction == 1) direction = 2; // Map to right turn
        }

        double roadThickness = LANE_WIDTH * 3;
        double laneOffset = LANE_WIDTH / 2.0;

        Path path = new Path();
        MoveTo start = null;

        switch(laneName) {
            case "A":
                path.getElements().add(new MoveTo(centerX - laneOffset, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 50));
                path.getElements().add(new LineTo(centerX - laneOffset, centerY - JUNCTION_SIZE / 2.0));

                if (direction == 0) { // Straight
                    path.getElements().add(new LineTo(centerX - laneOffset, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
                } else if (direction == 1) { // Left
                    path.getElements().add(new QuadCurveTo(centerX - laneOffset, centerY, centerX + laneOffset, centerY));
                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY));
                } else { // Right
                    path.getElements().add(new QuadCurveTo(centerX - laneOffset, centerY, centerX - roadThickness / 2.0 - 30, centerY));
                    path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY));
                }
                break;

            case "B":
                path.getElements().add(new MoveTo(centerX + laneOffset, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 50));
                path.getElements().add(new LineTo(centerX + laneOffset, centerY + JUNCTION_SIZE / 2.0));

                if (direction == 0) { // Straight
                    path.getElements().add(new LineTo(centerX + laneOffset, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                } else if (direction == 1) { // Left
                    path.getElements().add(new QuadCurveTo(centerX + laneOffset, centerY, centerX - laneOffset, centerY));
                    path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY));
                } else { // Right
                    path.getElements().add(new QuadCurveTo(centerX + laneOffset, centerY, centerX + roadThickness / 2.0 + 30, centerY));
                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY));
                }
                break;

            case "C":
                path.getElements().add(new MoveTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 50, centerY - laneOffset));
                path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0, centerY - laneOffset));

                if (direction == 0) { // Straight
                    path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY - laneOffset));
                } else if (direction == 1) { // Left
                    path.getElements().add(new QuadCurveTo(centerX, centerY - laneOffset, centerX, centerY + laneOffset));
                    path.getElements().add(new LineTo(centerX, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
                } else { // Right
                    path.getElements().add(new QuadCurveTo(centerX, centerY - laneOffset, centerX, centerY - roadThickness / 2.0 - 30));
                    path.getElements().add(new LineTo(centerX, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                }
                break;

            case "D":
                path.getElements().add(new MoveTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 50, centerY + laneOffset));
                path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0, centerY + laneOffset));

                if (direction == 0) { // Straight
                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY + laneOffset));
                } else if (direction == 1) { // Left
                    path.getElements().add(new QuadCurveTo(centerX, centerY + laneOffset, centerX, centerY - laneOffset));
                    path.getElements().add(new LineTo(centerX, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                } else { // Right
                    path.getElements().add(new QuadCurveTo(centerX, centerY + laneOffset, centerX, centerY + roadThickness / 2.0 + 30));
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