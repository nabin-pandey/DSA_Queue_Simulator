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
    private final double centerX, centerY;


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
            laneA.enqueueToLane(1, "A1-" + System.currentTimeMillis() % 1000);
            addWaitingCar("A", 1);
        }
        //AL2 - Priority Lane
        if (random.nextDouble() < 0.25) {
            laneA.enqueueToLane(2, "A2-" + System.currentTimeMillis() % 1000);
            addWaitingCar("A", 2);
        }
        if (random.nextDouble() < 0.15) {
            laneA.enqueueToLane(3, "A3-" + System.currentTimeMillis() % 10000);
            addWaitingCar("A", 3);
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
    public void addWaitingCar(String laneName, int laneNumber) {
        Rectangle car = new Rectangle(20, 35);
        car.setArcHeight(8);
        car.setArcWidth(8);
        car.setStroke(Color.BLACK);
        car.setStrokeWidth(1);
        car.setFill(laneNumber == 2 ? Color.GOLD : getRandomCarColor());

        if (laneNumber == 2) {
            car.setFill(Color.GOLD);
        } else {
            car.setFill(getRandomCarColor());
        }

        double laneOffset = LANE_WIDTH / 2.0;
        List<Rectangle> waitingList = null;
        double carX = 0, carY = 0;
        int rotation = 0;

        switch (laneName) {
            case "A": // North road going down
                double roadCenterX_A = centerX;
                if (laneNumber == 1) { // Right lane
                    waitingList = waitingCarsA_L1;
                    carX = roadCenterX_A + 50 - 10;
                } else if (laneNumber == 2) { // Middle lane (priority)
                    waitingList = waitingCarsA_L2;
                    carX = roadCenterX_A - 10;
                } else if (laneNumber == 3) { // Left lane
                    waitingList = waitingCarsA_L3;
                    carX = roadCenterX_A - 50 - 10;
                }
                carY = centerY - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45);
                break;

            case "B": // South road going up
                double roadCenterX_B = centerX;
                rotation = 180;
                if (laneNumber == 1) {
                    waitingList = waitingCarsB_L1;
                    carX = roadCenterX_B - 50 - 10;
                } else if (laneNumber == 3) { // Left lane
                    waitingList = waitingCarsB_L3;
                    carX = roadCenterX_B + 50 - 10;
                }
                carY = centerY + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45);
                break;

            case "C": // East road going left
                double roadCenterY_C = centerY;
                rotation = 90;
                if (laneNumber == 1) {
                    waitingList = waitingCarsC_L1;
                    carY = roadCenterY_C + 50 - 17.5;
                } else if (laneNumber == 3) { // Left lane
                    waitingList = waitingCarsC_L3;
                    carY = roadCenterY_C - 50 - 17.5;
                }
                carX = centerX + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45);
                break;

            case "D": // West road going right
                double roadCenterY_D = centerY;
                rotation = -90;
                if (laneNumber == 1) {
                    waitingList = waitingCarsD_L1;
                    carY = roadCenterY_D - 50 - 17.5;
                } else if (laneNumber == 3) { // Left lane
                    waitingList = waitingCarsD_L3;
                    carY = roadCenterY_D + 50 - 17.5;
                }
                carX = centerX - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45);
                break;
        }

        if (waitingList != null) {
            car.setX(carX);
            car.setY(carY);
            car.setRotate(rotation);
            waitingList.add(car);
            Platform.runLater(() -> simulationPane.getChildren().add(car));
        }
    }

    // Remove and animate waiting cars when light turns green
    public void releaseWaitingCars(String laneName, int laneNumber, int count) {
        List<Rectangle> waitingList = getWaitingList(laneName, laneNumber);

        if (waitingList != null && !waitingList.isEmpty()) {
            int toRelease = Math.min(count, waitingList.size());
            for (int i = 0; i < toRelease; i++) {
                if (!waitingList.isEmpty()) {
                    Rectangle car = waitingList.remove(0);
                    Platform.runLater(() -> simulationPane.getChildren().remove(car));
                }
            }
            repositionWaitingCars(laneName, laneNumber);
        }
    }

    private void repositionWaitingCars(String laneName, int laneNumber) {
        List<Rectangle> waitingList = getWaitingList(laneName, laneNumber);
        if (waitingList == null) return;

        double laneOffset = LANE_WIDTH / 2.0;

        for (int i = 0; i < waitingList.size(); i++) {
            final Rectangle car = waitingList.get(i);
            final int index = i;

            Platform.runLater(() -> {
                switch (laneName) {
                    case "A": car.setY(centerY - JUNCTION_SIZE / 2.0 - 60 - (index * 45)); break;
                    case "B": car.setY(centerY + JUNCTION_SIZE / 2.0 + 60 + (index * 45)); break;
                    case "C": car.setX(centerX + JUNCTION_SIZE / 2.0 + 60 + (index * 45)); break;
                    case "D": car.setX(centerX - JUNCTION_SIZE / 2.0 - 60 - (index * 45)); break;
                }
            });
        }
    }

    private List<Rectangle> getWaitingList(String laneName, int laneNumber) {
        switch (laneName + laneNumber) {
            case "A1": return waitingCarsA_L1;
            case "A2": return waitingCarsA_L2;
            case "A3": return waitingCarsA_L3;
            case "B1": return waitingCarsB_L1;
            case "B3": return waitingCarsB_L3;
            case "C1": return waitingCarsC_L1;
            case "C3": return waitingCarsC_L3;
            case "D1": return waitingCarsD_L1;
            case "D3": return waitingCarsD_L3;
            default: return null;
        }

    }

    // Create and animate a car through the junction
    public void createAndAnimateCar(String laneName, int laneNumber) {
        Rectangle car = new Rectangle(20, 35);
        car.setArcHeight(8);
        car.setArcWidth(8);
        car.setStroke(Color.BLACK);
        car.setStrokeWidth(1);
        car.setFill(laneNumber == 2 ? Color.GOLD : getRandomCarColor());

        int direction = (laneNumber == 3) ? 1 : random.nextInt(2); // L3=left, others=straight/right
        if (direction == 1 && laneNumber != 3) direction = 2; // Convert to right turn

        Path path = new Path();
        double startX = 0, startY = 0;

        // Calculate exact starting position based on lane
        switch (laneName) {
            case "A":
                startX = centerX + (laneNumber == 1 ? 50 : laneNumber == 2 ? 0 : -50);
                startY = centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 50;
                path.getElements().add(new MoveTo(startX, startY));
                path.getElements().add(new LineTo(startX, centerY - JUNCTION_SIZE / 2.0));

                if (direction == 0) { // Straight
                    path.getElements().add(new LineTo(startX, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
                } else if (direction == 1) {
                    path.getElements().add(new QuadCurveTo(startX, centerY, centerX + JUNCTION_SIZE / 2.0, centerY + 50));
                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY + 50));
                } else {
                    path.getElements().add(new QuadCurveTo(startX, centerY, centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY - 50));
                }
                break;

            case "B":
                startX = centerX - (laneNumber == 1 ? 50 : -50);
                startY = centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 50;
                path.getElements().add(new MoveTo(startX, startY));
                path.getElements().add(new LineTo(startX, centerY + JUNCTION_SIZE / 2.0));

                if (direction == 0) {
                    path.getElements().add(new LineTo(startX, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                } else if (direction == 1) {
                    path.getElements().add(new QuadCurveTo(startX, centerY, centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY - 50));
                } else {
                    path.getElements().add(new QuadCurveTo(startX, centerY, centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY + 50));
                }
                break;

            case "C":
                startX = centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 50;
                startY = centerY + (laneNumber == 1 ? 50 : -50);
                path.getElements().add(new MoveTo(startX, startY));
                path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0, startY));

                if (direction == 0) {
                    path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, startY));
                } else if (direction == 1) {
                    path.getElements().add(new QuadCurveTo(centerX, startY, centerX - 50, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
                } else {
                    path.getElements().add(new QuadCurveTo(centerX, startY, centerX + 50, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                }
                break;

            case "D":
                startX = centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 50;
                startY = centerY - (laneNumber == 1 ? 50 : -50);
                path.getElements().add(new MoveTo(startX, startY));
                path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0, startY));

                if (direction == 0) {
                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, startY));
                } else if (direction == 1) {
                    path.getElements().add(new QuadCurveTo(centerX, startY, centerX + 50, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                } else {
                    path.getElements().add(new QuadCurveTo(centerX, startY, centerX - 50, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
                }
                break;
        }

        // Add car to scene and animate
        Platform.runLater(() -> {
            simulationPane.getChildren().add(car);
            PathTransition pt = new PathTransition(Duration.millis(2000), path, car);
            pt.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
            pt.setOnFinished(event -> simulationPane.getChildren().remove(car));
            pt.play();
        });
    }

    private Color getRandomCarColor() {
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE,
                         Color.PURPLE, Color.YELLOW, Color.CYAN, Color.PINK};
        return colors[random.nextInt(colors.length)];

    }
}