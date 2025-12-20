package com.traffic.gui;

import com.traffic.core.Lane;
import com.traffic.core.LaneEntry;
import com.traffic.core.TrafficScheduler;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.traffic.gui.TrafficLight;

public class TrafficSimulator extends Application {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;
    private static final int LANE_WIDTH = 50;
    private static final int JUNCTION_SIZE = LANE_WIDTH * 4;
    private static final int LIGHT_SIZE = 10;
    private static final int ROAD_LENGTH = 300;

    //Traffic Light wrapper
    private TrafficLight trafficLightA ;
    private TrafficLight trafficLightB ;
    private TrafficLight trafficLightC ;
    private TrafficLight trafficLightD ;


    // Traffic Light Circles
    private Circle lightA;
    private Circle lightB;
    private Circle lightC;
    private Circle lightD;

    // Lane Objects
    private Lane laneA;
    private Lane laneB;
    private Lane laneC;
    private Lane laneD;

    private LaneEntry laneEntryA;
    private LaneEntry laneEntryB;
    private LaneEntry laneEntryC;
    private LaneEntry laneEntryD;

    private TrafficScheduler trafficScheduler;

    // Displaying counts
    private Text countA;
    private Text countB;
    private Text countC;
    private Text countD;

    // Counters for passed cars
    private int passedCountA = 0;
    private int passedCountB = 0;
    private int passedCountC = 0;
    private int passedCountD = 0;

    // UI components
    private Pane root;
    private double centerX;
    private double centerY;
    private final Pane simulationPane = new Pane();

    //Generate random numbers
    private final Random random_generator = new Random();

    // Traffic generator
    private TrafficGenerator trafficGenerator;

    //For Lanes :L
    private int currentLaneIndex = 0;
    private static final String[] LANE_ORDER = {"A", "B", "C", "D"};

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        laneA = new Lane("A");
        laneB = new Lane("B");
        laneC = new Lane("C");
        laneD = new Lane("D");

        laneEntryA = new LaneEntry("A", 0);
        laneEntryB = new LaneEntry("B", 0);
        laneEntryC = new LaneEntry("C", 0);
        laneEntryD = new LaneEntry("D", 0);

        List<LaneEntry> entries = new ArrayList<>();
        entries.add(laneEntryA);
        entries.add(laneEntryB);
        entries.add(laneEntryC);
        entries.add(laneEntryD);

        trafficScheduler = new TrafficScheduler(entries);

        root = new Pane();
        root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        centerX = WINDOW_WIDTH / 2.0;
        centerY = WINDOW_HEIGHT / 2.0;

        // Initialize traffic generator
        trafficGenerator = new TrafficGenerator(laneA, laneB, laneC, laneD,
                simulationPane, centerX, centerY);

        buildJunctionUI();

        Button startButton = new Button("Start");
        startButton.setLayoutX(20);
        startButton.setLayoutY(WINDOW_HEIGHT - 60);
        root.getChildren().add(startButton);

        startButton.setOnAction(e -> {
            startButton.setDisable(true);
            startSimulationLoop();
        });

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("DSA Queue Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void buildJunctionUI() {
        // Background
        Rectangle background = new Rectangle(WINDOW_WIDTH, WINDOW_HEIGHT);
        background.setFill(Color.web("#f3f3f3"));
        root.getChildren().add(background);

        // Grass corners
        Rectangle grassTopLeft = makeGrass(0, 0, centerX - JUNCTION_SIZE / 2 - 60, centerY - JUNCTION_SIZE / 2 - 60);
        Rectangle grassTopRight = makeGrass(centerX + JUNCTION_SIZE / 2 + 60, 0,
                WINDOW_WIDTH - (centerX + JUNCTION_SIZE / 2 + 60), centerY - JUNCTION_SIZE / 2 - 60);
        Rectangle grassBottomLeft = makeGrass(0, centerY + JUNCTION_SIZE / 2 + 60,
                centerX - JUNCTION_SIZE / 2 - 60, WINDOW_HEIGHT - (centerY + JUNCTION_SIZE / 2 + 60));
        Rectangle grassBottomRight = makeGrass(centerX + JUNCTION_SIZE / 2 + 60, centerY + JUNCTION_SIZE / 2 + 60,
                WINDOW_WIDTH - (centerX + JUNCTION_SIZE / 2 + 60), WINDOW_HEIGHT - (centerY + JUNCTION_SIZE / 2 + 60));
        root.getChildren().addAll(grassTopLeft, grassTopRight, grassBottomLeft, grassBottomRight);

        // Center junction
        Rectangle junction = new Rectangle(JUNCTION_SIZE, JUNCTION_SIZE);
        junction.setX(centerX - JUNCTION_SIZE / 2.0);
        junction.setY(centerY - JUNCTION_SIZE / 2.0);
        junction.setFill(Color.web("#4b4b4b"));
        root.getChildren().add(junction);

        double roadThickness = LANE_WIDTH * 3;

        // Roads
        Rectangle roadA = new Rectangle(roadThickness, ROAD_LENGTH);
        roadA.setX(centerX - roadThickness / 2.0);
        roadA.setY(centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH);
        roadA.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadA);

        Rectangle roadB = new Rectangle(roadThickness, ROAD_LENGTH);
        roadB.setX(centerX - roadThickness / 2.0);
        roadB.setY(centerY + JUNCTION_SIZE / 2.0);
        roadB.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadB);

        Rectangle roadC = new Rectangle(ROAD_LENGTH, roadThickness);
        roadC.setX(centerX + JUNCTION_SIZE / 2.0);
        roadC.setY(centerY - roadThickness / 2.0);
        roadC.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadC);

        Rectangle roadD = new Rectangle(ROAD_LENGTH, roadThickness);
        roadD.setX(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH);
        roadD.setY(centerY - roadThickness / 2.0);
        roadD.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadD);

        // Junction overlay
        Rectangle junctionOverlay = new Rectangle(JUNCTION_SIZE, JUNCTION_SIZE);
        junctionOverlay.setX(centerX - JUNCTION_SIZE / 2.0);
        junctionOverlay.setY(centerY - JUNCTION_SIZE / 2.0);
        junctionOverlay.setFill(Color.web("#4b4b4b"));
        root.getChildren().add(junctionOverlay);

        // Lane markings
        drawLaneMarkingsForVerticalRoad(centerX - roadThickness / 2.0, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH,
                roadThickness, ROAD_LENGTH);
        drawLaneMarkingsForVerticalRoad(centerX - roadThickness / 2.0, centerY + JUNCTION_SIZE / 2.0,
                roadThickness, ROAD_LENGTH);
        drawLaneMarkingsForHorizontalRoad(centerX + JUNCTION_SIZE / 2.0, centerY - roadThickness / 2.0,
                ROAD_LENGTH, roadThickness);
        drawLaneMarkingsForHorizontalRoad(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY - roadThickness / 2.0,
                ROAD_LENGTH, roadThickness);

        drawAllCrossWalks();

        // Traffic Lights
        lightA = new Circle(centerX - (LANE_WIDTH) / 2.0, centerY - JUNCTION_SIZE / 2.0 - 20, LIGHT_SIZE, Color.RED);
        lightB = new Circle(centerX + (LANE_WIDTH) / 2.0, centerY + JUNCTION_SIZE / 2.0 + 20, LIGHT_SIZE, Color.RED);
        lightC = new Circle(centerX + JUNCTION_SIZE / 2.0 + 20, centerY - (LANE_WIDTH) / 2.0, LIGHT_SIZE, Color.RED);
        lightD = new Circle(centerX - JUNCTION_SIZE / 2.0 - 20, centerY + (LANE_WIDTH) / 2.0, LIGHT_SIZE, Color.RED);
        root.getChildren().addAll(lightA, lightB, lightC, lightD);

        //Fixed : Initializaton of Traffic :
        trafficLightA = new TrafficLight(lightA);
        trafficLightB = new TrafficLight(lightB);
        trafficLightC = new TrafficLight(lightC);
        trafficLightD = new TrafficLight(lightD);


        // Labels
        Text LabelA = new Text(centerX - 12, centerY - ROAD_LENGTH - LANE_WIDTH / 2.0 + 8, "Road A");
        Text LabelB = new Text(centerX - 12, centerY + ROAD_LENGTH + LANE_WIDTH, "Road B");
        Text LabelC = new Text(centerX + ROAD_LENGTH + LANE_WIDTH / 2.0 - 10, centerY + 4, "Road C");
        Text LabelD = new Text(centerX - ROAD_LENGTH - LANE_WIDTH + 6, centerY + 4, "Road D");
        root.getChildren().addAll(LabelA, LabelB, LabelC, LabelD);

        // Count displays
        countA = new Text(centerX - 60, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 20, "Queue:0 | Passed:0");
        countB = new Text(centerX - 60, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 6, "Queue:0 | Passed:0");
        countC = new Text(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 100, centerY - 35, "Queue:0 | Passed:0");
        countD = new Text(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 10, centerY - 35, "Queue:0 | Passed:0");
        root.getChildren().addAll(countA, countB, countC, countD);

        // Add simulation pane last
        root.getChildren().add(simulationPane);
    }

    private Rectangle makeGrass(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(w, h);
        r.setX(x);
        r.setY(y);
        r.setFill(Color.web("#b6d68b"));
        return r;
    }

    private void drawLaneMarkingsForVerticalRoad(double x, double y, double width, double height) {
        double third = width / 3.0;
        for (int i = 1; i < 3; i++) {
            double markX = x + third * i;
            Line l = new Line(markX, y, markX, y + height);
            l.getStrokeDashArray().addAll(8.0, 10.0);
            l.setStrokeWidth(2);
            l.setStroke(Color.web("#eeeeee"));
            root.getChildren().add(l);
        }
    }

    private void drawLaneMarkingsForHorizontalRoad(double x, double y, double width, double height) {
        double third = height / 3.0;
        for (int i = 1; i < 3; i++) {
            double markY = y + third * i;
            Line l = new Line(x, markY, x + width, markY);
            l.getStrokeDashArray().addAll(8.0, 10.0);
            l.setStrokeWidth(2);
            l.setStroke(Color.web("#eeeeee"));
            root.getChildren().add(l);
        }
    }

    private void drawAllCrossWalks() {
        double roadWidth = LANE_WIDTH * 3;

        double crosswalkAY = centerY - JUNCTION_SIZE / 2.0 - 12;
        double crosswalkAX = centerX - roadWidth / 2.0;
        drawCrosswalkHorizontalStripes(crosswalkAX, crosswalkAY, roadWidth);

        double crosswalkBY = centerY + JUNCTION_SIZE / 2.0 + 4;
        double crosswalkBX = centerX - roadWidth / 2.0;
        drawCrosswalkHorizontalStripes(crosswalkBX, crosswalkBY, roadWidth);

        double crosswalkCY = centerY - roadWidth / 2.0;
        double crosswalkCX = centerX + JUNCTION_SIZE / 2.0 + 4;
        drawCrosswalkVerticalStripes(crosswalkCX, crosswalkCY, roadWidth);

        double crosswalkDX = centerX - JUNCTION_SIZE / 2.0 - 12;
        double crosswalkDY = centerY - roadWidth / 2.0;
        drawCrosswalkVerticalStripes(crosswalkDX, crosswalkDY, roadWidth);
    }

    private void drawCrosswalkVerticalStripes(double x, double y, double roadHeight) {
        double stripeWidth = 8;
        double stripeHeight = 15;
        double spacing = 5;
        int numStripes = (int) (roadHeight / (stripeHeight + spacing));

        for (int i = 0; i < numStripes; i++) {
            Rectangle stripe = new Rectangle(stripeWidth, stripeHeight);
            stripe.setX(x);
            stripe.setY(y + i * (stripeHeight + spacing));
            stripe.setFill(Color.WHITE);
            root.getChildren().add(stripe);
        }
    }

    private void drawCrosswalkHorizontalStripes(double x, double y, double roadWidth) {
        double stripeWidth = 15;
        double stripeHeight = 8;
        double spacing = 5;
        int numStripes = (int) (roadWidth / (stripeWidth + spacing));

        for (int i = 0; i < numStripes; i++) {
            Rectangle stripe = new Rectangle(stripeWidth, stripeHeight);
            stripe.setX(x + i * (stripeWidth + spacing));
            stripe.setY(y);
            stripe.setFill(Color.WHITE);
            root.getChildren().add(stripe);
        }
    }

    private void startSimulationLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2.5), e -> {

            // Generate traffic using TrafficGenerator
            trafficGenerator.generateRandomTraffic();

            updateCount();

            //  lane serving: A -> B -> C -> D
            laneEntryA.setVehicleCount(laneA.incomingSize());
            laneEntryB.setVehicleCount(laneB.incomingSize());
            laneEntryC.setVehicleCount(laneC.incomingSize());
            laneEntryD.setVehicleCount(laneD.incomingSize());

// Update priority based on current counts
            trafficScheduler.CheckandUpdatePriority(laneEntryA, laneA.incomingSize());
            trafficScheduler.CheckandUpdatePriority(laneEntryB, laneB.incomingSize());
            trafficScheduler.CheckandUpdatePriority(laneEntryC, laneC.incomingSize());
            trafficScheduler.CheckandUpdatePriority(laneEntryD, laneD.incomingSize());



// Get the next lane to serve based on priority
            String next = trafficScheduler.serverAndRotateLane();
            if (next == null || next.contains("No lanes")) {
                next = LANE_ORDER[currentLaneIndex % 4];
                currentLaneIndex++;
                System.out.println("Using fallback rotation: " + next);
            }

            System.out.println("\nScheduler selected lane " + next + " Queue: A=" + laneA.incomingSize() +
                    " B=" + laneB.incomingSize() + " C=" + laneC.incomingSize() +
                    " D=" + laneD.incomingSize()  );

            // Set all lights to red
            trafficLightA.setState(TrafficLight.State.RED);
            trafficLightB.setState(TrafficLight.State.RED);
            trafficLightC.setState(TrafficLight.State.RED);
            trafficLightD.setState(TrafficLight.State.RED);

           TrafficLight currentTrafficLight = null ;
            Lane currentLane = null;

            String trimmedNext = next.trim();

            if (trimmedNext.equals("A")) {
                currentTrafficLight = trafficLightA;
                currentLane = laneA;
            } else if (trimmedNext.equals("B")) {
                currentTrafficLight = trafficLightB;
                currentLane = laneB;
            } else if (trimmedNext.equals("C")) {
                currentTrafficLight = trafficLightC;
                currentLane = laneC;
            } else if (trimmedNext.equals("D")) {
                currentTrafficLight = trafficLightD;
                currentLane = laneD;
            }

            final String finalLaneName = trimmedNext;
            final Lane finalCurrentLane = currentLane;

            if (currentTrafficLight != null && currentLane != null) {
                currentTrafficLight.setState(TrafficLight.State.GREEN);

                final TrafficLight finalTrafficLight = currentTrafficLight;
                int carsToServe ;

                // Serve up to 3 cars per green light

                if (finalCurrentLane.incomingSize() > 10) {
                    // For overcrowded lanes, serve more cars to clear the queue
                    // For overcrowded lanes, serve more cars
                    carsToServe = Math.min(finalCurrentLane.incomingSize(), 6); // Up to 6 for overcrowded
                    System.out.println("OVERLOADED LANE " + finalLaneName + "! Serving " + carsToServe + " cars");
                } else if (finalCurrentLane.incomingSize() > 5) {
                    // For medium traffic, serve moderate number of caes
                    carsToServe = Math.min(finalCurrentLane.incomingSize(), 4); // Up to 4
                } else {
                    // Normal traffic
                    carsToServe = Math.min(finalCurrentLane.incomingSize(), 3); // Up to 3
                }

                System.out.println("Will serve " + carsToServe + " cars from lane " + finalLaneName);

                // Release waiting cars visually BEFORE serving
                trafficGenerator.releaseWaitingCars(finalLaneName, carsToServe);

                // Serve cars with staggered animation
                for (int i = 0; i < carsToServe; i++) {
                    String served = finalCurrentLane.dequeueFromIncoming();
                    if (served != null) {
                        System.out.println("Car Served: " + served + " (Queue: " + finalCurrentLane.incomingSize() + ")");

                        switch (finalLaneName) {
                            case "A": passedCountA++; break;
                            case "B": passedCountB++; break;
                            case "C": passedCountC++; break;
                            case "D": passedCountD++; break;
                        }

                        // Stagger animations
                        final int delay = i * 400;
                        Timeline carTimeline = new Timeline(new KeyFrame(Duration.millis(delay), e2 -> {
                            trafficGenerator.createAndAnimateCar(finalLaneName);
                        }));
                        carTimeline.play();
                    }
                }

                //As there are cars moving green ligght will lit up till that
                int greenDuration = 1500 + (carsToServe * 300);
                // Green to yellow to red transition
                Timeline t = new Timeline(new KeyFrame(Duration.millis(greenDuration), e2 -> {
                    finalTrafficLight.setState(TrafficLight.State.YELLOW);
                    new Timeline(new KeyFrame(Duration.millis(700), e3 -> {
                        finalTrafficLight.setState(TrafficLight.State.RED);
                    })).play();
                }));
                t.play();
            }

            updateCount();
        }));

        updateCount();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

//    public void setLightColor(Circle light, Color color) {
//        if (light == null) return;
//        Platform.runLater(() -> light.setFill(color));
//    }

    private void updateCount() {
        Platform.runLater(() -> {
            countA.setText("Queue:" + laneA.incomingSize() + " | Passed:" + passedCountA);
            countB.setText("Queue:" + laneB.incomingSize() + " | Passed:" + passedCountB);
            countC.setText("Queue:" + laneC.incomingSize() + " | Passed:" + passedCountC);
            countD.setText("Queue:" + laneD.incomingSize() + " | Passed:" + passedCountD);
        });
    }
}