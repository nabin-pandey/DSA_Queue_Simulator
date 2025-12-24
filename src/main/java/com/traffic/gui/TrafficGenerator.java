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

        if (laneNumber == 2) {
            car.setFill(Color.GOLD);
        } else {
            car.setFill(getRandomCarColor());
        }

        double laneOffset = LANE_WIDTH / 2.0;
        List<Rectangle> waitingList = null;

        switch (laneName) {
            case "A":
                if (laneNumber == 1) {
                    waitingList = waitingCarsA_L1;
                    // L1
                    car.setX(centerX + LANE_WIDTH / 2 - 10);
                    car.setY(centerY - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45));
                } else if (laneNumber == 2) {
                    waitingList = waitingCarsA_L2;
                    // L2 is the middle lane (priority)
                    car.setX(centerX - LANE_WIDTH / 2 - 10);
                    car.setY(centerY - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45));
                    car.setFill(Color.GOLD);
                } else if (laneNumber == 3) {
                    waitingList = waitingCarsA_L3;
                    // L3 - leftmost lane for left turn
                    car.setX(centerX - LANE_WIDTH * 1.5 - 10);
                    car.setY(centerY - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45));
                }
                break;

            case "B":
                if (laneNumber == 1) {
                    waitingList = waitingCarsB_L1;
                    // L1 - rightmost lane
                    car.setX(centerX - LANE_WIDTH / 2 - 10);
                    car.setY(centerY + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45));
                    car.setRotate(180);
                } else if (laneNumber == 3) {
                    waitingList = waitingCarsB_L3;
                    // L3 -  leftmost lane
                    car.setX(centerX + LANE_WIDTH / 2 - 10);
                    car.setY(centerY + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45));
                    car.setRotate(180);
                }
                break;

            case "C":
                if (laneNumber == 1) {
                    waitingList = waitingCarsC_L1;
                    car.setX(centerX + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45));
                    car.setY(centerY + LANE_WIDTH / 2 - 17.5);
                    car.setRotate(90);
                } else if (laneNumber == 3) {
                    waitingList = waitingCarsC_L3;
                    car.setX(centerX + JUNCTION_SIZE / 2.0 + 60 + (waitingList.size() * 45));
                    car.setY(centerY - LANE_WIDTH / 2 - 17.5);
                    car.setRotate(90);
                }
                break;

            case "D": //horizontal to C
                if (laneNumber == 1) {
                    waitingList = waitingCarsD_L1;
                    car.setX(centerX - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45));
                    car.setY(centerY - LANE_WIDTH / 2 - 17.5);
                    car.setRotate(-90);
                } else if (laneNumber == 3) {
                    waitingList = waitingCarsD_L3;
                    car.setX(centerX - JUNCTION_SIZE / 2.0 - 60 - (waitingList.size() * 45));
                    car.setY(centerY + LANE_WIDTH / 2 - 17.5);
                    car.setRotate(-90);
                }
                break;
        }

        if (waitingList != null) {
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
            Rectangle car = waitingList.get(i);
            final int index = i;

            Platform.runLater(() -> {
                switch (laneName) {
                    case "A":
                        car.setY(centerY - JUNCTION_SIZE / 2.0 - 60 - (index * 45));
                        break;
                    case "B":
                        car.setY(centerY + JUNCTION_SIZE / 2.0 + 60 + (index * 45));
                        break;
                    case "C":
                        car.setX(centerX + JUNCTION_SIZE / 2.0 + 60 + (index * 45));
                        break;
                    case "D":
                        car.setX(centerX - JUNCTION_SIZE / 2.0 - 60 - (index * 45));
                        break;
                }
            });
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
        return null;
    }

    // Create and animate a car through the junction
    public void createAndAnimateCar(String laneName, int laneNumber) {
        Rectangle car = new Rectangle(20, 35);
        car.setArcHeight(8);
        car.setArcWidth(8);
        car.setStroke(Color.BLACK);
        car.setStrokeWidth(1);

        if (laneNumber == 2) {
            car.setFill(Color.GOLD);
        } else {
            car.setFill(getRandomCarColor());
        }

        int direction;

        // Lane 3 = LEFT-TURN ONLY
        if (laneNumber == 3) {
            direction = 1; // Left turn only
        } else {
            direction = random.nextInt(2); // 0=straight, 1=right
            if (direction == 1) direction = 2; // Map to right
        }

        double roadThickness = LANE_WIDTH * 3;
        double laneOffset = LANE_WIDTH / 2.0;

        Path path = new Path();
        MoveTo start = null;

        // FIXED: Proper paths for Lane 3 (left turn to corresponding L3)
        switch (laneName) {
            case "A": // From North
                path.getElements().add(new MoveTo(centerX + (laneNumber == 3 ? -LANE_WIDTH * 1.5 : laneNumber == 2 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2),
                                                  centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 50));
                path.getElements().add(new LineTo(centerX + (laneNumber == 3 ? -LANE_WIDTH * 1.5 : laneNumber == 2 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2),
                                                  centerY - JUNCTION_SIZE / 2.0));

                if (direction == 0) { // Straight
                    path.getElements().add(new LineTo(centerX + (laneNumber == 3 ? -LANE_WIDTH * 1.5 : laneNumber == 2 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2),
                                                      centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
                } else if (direction == 1) { // Left turn
                    double startX = centerX + (laneNumber == 3 ? -LANE_WIDTH * 1.5 : laneNumber == 2 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2);
                    path.getElements().add(new QuadCurveTo(startX, centerY, centerX + LANE_WIDTH / 2, centerY));
                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY));
                } else { // Right turn
                    double startX = centerX + (laneNumber == 3 ? -LANE_WIDTH * 1.5 : laneNumber == 2 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2);
                    path.getElements().add(new QuadCurveTo(startX, centerY, centerX - LANE_WIDTH / 2, centerY));
                    path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY));
                }
                break;

            case "B": // From South
                path.getElements().add(new MoveTo(centerX - (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2),
                                                  centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 50));
                path.getElements().add(new LineTo(centerX - (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2),
                                                  centerY + JUNCTION_SIZE / 2.0));

                if (direction == 0) { // Straight
                    path.getElements().add(new LineTo(centerX - (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2),
                                                      centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                } else if (direction == 1) { // Left turn
                    double startX = centerX - (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2);
                    path.getElements().add(new QuadCurveTo(startX, centerY, centerX - LANE_WIDTH / 2, centerY));
                    path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY));
                } else { // Right turn
                    double startX = centerX - (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2);
                    path.getElements().add(new QuadCurveTo(startX, centerY, centerX + LANE_WIDTH / 2, centerY));
                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY));
                }
                break;

            case "C": // From East
                path.getElements().add(new MoveTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 50,
                                                  centerY + (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2)));
                path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0,
                                                  centerY + (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2)));

                if (direction == 0) { // Straight
                    path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH,
                                                      centerY + (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2)));
                } else if (direction == 1) { // Left turn
                    double startY = centerY + (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2);
                    path.getElements().add(new QuadCurveTo(centerX, startY, centerX, centerY - LANE_WIDTH / 2));
                    path.getElements().add(new LineTo(centerX, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
                } else { // Right turn
                    double startY = centerY + (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2);
                    path.getElements().add(new QuadCurveTo(centerX, startY, centerX, centerY + LANE_WIDTH / 2));
                    path.getElements().add(new LineTo(centerX, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                }
                break;

            case "D": // From West
                path.getElements().add(new MoveTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 50,
                                                  centerY - (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2)));
                path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0,
                                                  centerY - (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2)));

                if (direction == 0) { // Straight
                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH,
                                                      centerY - (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2)));
                } else if (direction == 1) { // Left turn
                    double startY = centerY - (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2);
                    path.getElements().add(new QuadCurveTo(centerX, startY, centerX, centerY + LANE_WIDTH / 2));
                    path.getElements().add(new LineTo(centerX, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));
                } else { // Right turn
                    double startY = centerY - (laneNumber == 3 ? -LANE_WIDTH / 2 : LANE_WIDTH / 2);
                    path.getElements().add(new QuadCurveTo(centerX, startY, centerX, centerY - LANE_WIDTH / 2));
                    path.getElements().add(new LineTo(centerX, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
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
        Color[] colors = {
                Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE,
                Color.PURPLE, Color.YELLOW, Color.CYAN
        };
        return colors[random.nextInt(colors.length)];

    }
}