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
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class TrafficSimulator extends Application {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;
    private static final int LANE_WIDTH = 50;
    private static final int JUNCTION_SIZE = LANE_WIDTH * 4;
    private static final int LIGHT_SIZE = 10;
    private static final int ROAD_LENGTH = 300;


    //Wrappers for the TrafficLights
    private TrafficLight trafficLightA, trafficLightB, trafficLightC, trafficLightD;
    private Circle lightA, lightB, lightC, lightD;

    //Lane , LaneEntry
    private Lane laneA, laneB, laneC, laneD;
    private LaneEntry laneEntryA, laneEntryB, laneEntryC, laneEntryD;
    private TrafficScheduler trafficScheduler;

    //Display Counts
    private Text countA, countB, countC, countD, fileStatusText;

    private int passedCountA_L1 = 0, passedCountA_L2 = 0, passedCountA_L3 = 0;
    private int passedCountB_L1 = 0, passedCountB_L3 = 0;
    private int passedCountC_L1 = 0, passedCountC_L3 = 0;
    private int passedCountD_L1 = 0, passedCountD_L3 = 0;

    // UI components
    private Pane root;
    private double centerX;
    private double centerY;
    private final Pane simulationPane = new Pane();

    // Generate random numbers
    private final Random random_generator = new Random();

    // Traffic generator
    private TrafficGenerator trafficGenerator;

    // File reading
    private static final String LANE_A_FILE = "lanea.txt";
    private static final String LANE_B_FILE = "laneb.txt";
    private static final String LANE_C_FILE = "lanec.txt";
    private static final String LANE_D_FILE = "laned.txt";

    // Read the Files
    private Map<String, Long> lastReadPosition = new HashMap<>();
    private Timeline filePollingTimeline;
    private Timeline simulationTimeline;

    // FIXED: Default to file input mode
    private boolean useFileInput = true;
    private boolean simulationRunning = false;
    private boolean isProcessingCycle = false; // FIXED: Prevent overlapping cycles

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

        // Start button
        Button startButton = new Button("Start Simulation");
        startButton.setLayoutX(20);
        startButton.setLayoutY(WINDOW_HEIGHT - 60);
        root.getChildren().add(startButton);

        startButton.setOnAction(e -> {
            if (!simulationRunning) {
                startButton.setDisable(true);
                simulationRunning = true;

                // Start file polling first
                if (useFileInput) {
                    startFilePolling();
                }

                // Then start simulation loop
                startSimulationLoop();
            }
        });

        fileStatusText = new Text("Mode: FILE - Waiting to start...");
        fileStatusText.setLayoutX(20);
        fileStatusText.setLayoutY(WINDOW_HEIGHT - 30);
        fileStatusText.setFill(Color.BLUE);
        root.getChildren().add(fileStatusText);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("Traffic Simulator with File Input");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initialize the reading positions
        lastReadPosition.put(LANE_A_FILE, 0L);
        lastReadPosition.put(LANE_B_FILE, 0L);
        lastReadPosition.put(LANE_C_FILE, 0L);
        lastReadPosition.put(LANE_D_FILE, 0L);

        System.out.println("========================================");
        System.out.println("Traffic Simulator Started (FILE MODE)");
        System.out.println("Run TrafficGeneratorProcess.java first!");
        System.out.println("========================================");
    }

    //Fixed the file polling to increase the responsiveness
    private void startFilePolling() {
        if (filePollingTimeline != null) {
            filePollingTimeline.stop();
        }

        filePollingTimeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            if (useFileInput && simulationRunning) {
                readNewVehicles();
            }
        }));
        filePollingTimeline.setCycleCount(Timeline.INDEFINITE);
        filePollingTimeline.play();
        updateFileStatus("Polling Files...", Color.GREEN);
    }

    private void readNewVehicles() {
        if (!useFileInput || !simulationRunning) return;

        try {
            int totalRead = 0;
            totalRead += readVehicles(LANE_A_FILE, laneA, "A");
            totalRead += readVehicles(LANE_B_FILE, laneB, "B");
            totalRead += readVehicles(LANE_C_FILE, laneC, "C");
            totalRead += readVehicles(LANE_D_FILE, laneD, "D");

            if (totalRead > 0) {
                updateCount();
            }
        } catch (Exception error) {
            System.err.println("Error reading files: " + error.getMessage());
        }
    }

    private int readVehicles(String filename, Lane lane, String roadId) {
        File file = new File(filename);
        if (!file.exists()) return 0;

        int vehiclesRead = 0;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long fileLength = raf.length();
            long lastPosition = lastReadPosition.getOrDefault(filename, 0L);

            if (lastPosition > fileLength) lastPosition = 0L;
            if (lastPosition == fileLength) return 0;

            raf.seek(lastPosition);
            String line;

            while ((line = raf.readLine()) != null) {
                if (processVehicleLine(line, lane, roadId)) {
                    vehiclesRead++;
                }
            }

            lastReadPosition.put(filename, raf.getFilePointer());
        } catch (IOException e) {
            System.err.println("Error reading " + filename);
        }
        return vehiclesRead;
    }

    private boolean processVehicleLine(String line, Lane lane, String roadId) {
        try {
            String[] parts = line.split(",");
            if (parts.length != 3) return false;

            String vehicleId = parts[0].trim();
            int laneNumber = Integer.parseInt(parts[1].trim());
            long timestamp = Long.parseLong(parts[2].trim());

            // Add the vehicle to the proper lane
            lane.enqueueToLane(laneNumber, vehicleId);

            // Add visual waiting car
            trafficGenerator.addWaitingCar(roadId, laneNumber);

            System.out.println("File: " + vehicleId + " → " + roadId + " L" + laneNumber);
            updateFileStatus("Last: " + vehicleId, Color.GREEN);
            return true;

        } catch (Exception e) {
            System.err.println("Error processing line: " + line);
            return false;
        }
    }

    private void updateFileStatus(String message, Color color) {
        Platform.runLater(() -> {
            fileStatusText.setText("Mode: FILE | " + message);
            fileStatusText.setFill(color);
        });
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

        // Initialize Traffic Lights
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
        countA = new Text(centerX - 80, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 30,
                "L1:0 L2:0 L3:0");
        countB = new Text(centerX - 80, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 10,
                "L1:0 L3:0");
        countC = new Text(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 100, centerY - 20,
                "L1:0 L3:0");
        countD = new Text(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 20, centerY - 20,
                "L1:0 L3:0");

        root.getChildren().addAll(countA, countB, countC, countD);

        // Add simulation pane last
        root.getChildren().add(simulationPane);
    }

    private void startSimulationLoop() {
        if (simulationTimeline != null) {
            simulationTimeline.stop();
        }

        simulationTimeline = new Timeline(new KeyFrame(Duration.seconds(4), e -> {
            if (!simulationRunning || isProcessingCycle) return;

            processTrafficCycle();
        }));

        simulationTimeline.setCycleCount(Timeline.INDEFINITE);
        simulationTimeline.play();
    }

    // FIXED: Separate method for processing traffic cycle
    private void processTrafficCycle() {
        isProcessingCycle = true;

        laneEntryA.setVehicleCount(laneA.incomingSize());
        laneEntryB.setVehicleCount(laneB.incomingSize());
        laneEntryC.setVehicleCount(laneC.incomingSize());
        laneEntryD.setVehicleCount(laneD.incomingSize());

        trafficScheduler.CheckandUpdatePriority(laneEntryA, laneA.incomingSize(), laneA.prioritySize());
        trafficScheduler.CheckandUpdatePriority(laneEntryB, laneB.incomingSize(), 0);
        trafficScheduler.CheckandUpdatePriority(laneEntryC, laneC.incomingSize(), 0);
        trafficScheduler.CheckandUpdatePriority(laneEntryD, laneD.incomingSize(), 0);

        String next = trafficScheduler.serverAndRotateLane();

        System.out.println("\n=== Serving Lane: " + next + " ===");
        System.out.println("Queue sizes - A(L1:" + laneA.incomingSize() + " L2:" + laneA.prioritySize() + " L3:" + laneA.leftTurnSize() + ")");
        System.out.println("                B(L1:" + laneB.incomingSize() + " L3:" + laneB.leftTurnSize() + ")");
        System.out.println("                C(L1:" + laneC.incomingSize() + " L3:" + laneC.leftTurnSize() + ")");
        System.out.println("                D(L1:" + laneD.incomingSize() + " L3:" + laneD.leftTurnSize() + ")");

            // Set all lights to red
            trafficLightA.setState(TrafficLight.State.RED);
            trafficLightB.setState(TrafficLight.State.RED);
            trafficLightC.setState(TrafficLight.State.RED);
            trafficLightD.setState(TrafficLight.State.RED);

            TrafficLight currentTrafficLight = null;
            Lane currentLane = null;

            String trimmedNext = next.trim();

            switch (trimmedNext) {
                case "A": currentTrafficLight = trafficLightA; currentLane = laneA; break;
                case "B": currentTrafficLight = trafficLightB; currentLane = laneB; break;
                case "C": currentTrafficLight = trafficLightC; currentLane = laneC; break;
                case "D": currentTrafficLight = trafficLightD; currentLane = laneD; break;
            }

            final String finalLaneName = trimmedNext;
            final Lane finalCurrentLane = currentLane;

        if (currentTrafficLight != null && currentLane != null) {
            // FIXED: Delay green light slightly for visual clarity
            final TrafficLight finalTrafficLight = currentTrafficLight;

            Timeline greenLightDelay = new Timeline(new KeyFrame(Duration.millis(300), e1 -> {
                finalTrafficLight.setState(TrafficLight.State.GREEN);

                int carsToServe;
                boolean servingPriorityLane = false;

                // Check if we should serve AL2 priority lane
                if (finalLaneName.equals("A") && finalCurrentLane.prioritySize() > 0 &&
                        trafficScheduler.isPriorityModeActive()) {
                    carsToServe = Math.min(finalCurrentLane.prioritySize(), 3);
                    servingPriorityLane = true;
                    System.out.println("SERVING PRIORITY LANE AL2! Cars: " + carsToServe);
                } else {
                    List<LaneEntry> entries = new ArrayList<>();
                    entries.add(laneEntryA);
                    entries.add(laneEntryB);
                    entries.add(laneEntryC);
                    entries.add(laneEntryD);
                    int avgVehicles = trafficScheduler.calculateAverageVehicles(entries);
                    carsToServe = Math.max(1, Math.min(avgVehicles,
                            finalCurrentLane.incomingSize() + finalCurrentLane.leftTurnSize()));
                    System.out.println("Average vehicles: " + avgVehicles + " | Serving: " + carsToServe);
                }

                final boolean isServingPriority = servingPriorityLane;

                // Calculate cars to serve from each lane
                int carsFromL1 = 0, carsFromL2 = 0, carsFromL3 = 0;

                if (isServingPriority && finalLaneName.equals("A")) {
                    // Serve from AL2 Lane
                    carsFromL2 = Math.min(carsToServe, finalCurrentLane.prioritySize());
                } else {
                    // Distribute across L1 and L3
                    int l1Available = finalCurrentLane.incomingSize();
                    int l3Available = finalCurrentLane.leftTurnSize();

                    carsFromL1 = Math.min(carsToServe / 2, l1Available);
                    carsFromL3 = Math.min(carsToServe - carsFromL1, l3Available);

                    // If no L3 cars, serve all from L1
                    if (carsFromL3 == 0 && carsFromL1 < carsToServe) {
                        carsFromL1 = Math.min(carsToServe, l1Available);
                    }
                }

                System.out.println("Serving L1=" + carsFromL1 + " L2=" + carsFromL2 + " L3=" + carsFromL3);

                // FIXED: Stagger car releases for smooth animation
                int totalCars = carsFromL1 + carsFromL2 + carsFromL3;
                int carIndex = 0;

                // Serve from Lane 2 (priority)
                for (int i = 0; i < carsFromL2; i++) {
                    String served = finalCurrentLane.dequeueFromPriority();
                    if (served != null) {
                        passedCountA_L2++;
                        System.out.println("Served from AL2: " + served);

                        final int delay = carIndex * 700;
                        carIndex++;

                        Timeline carTimeline = new Timeline(new KeyFrame(Duration.millis(delay), e2 -> {
                            trafficGenerator.releaseWaitingCars(finalLaneName, 2, 1);
                            trafficGenerator.createAndAnimateCar(finalLaneName, 2);
                        }));
                        carTimeline.play();
                    }
                }

                // Serve from Lane 1
                for (int i = 0; i < carsFromL1; i++) {
                    String served = finalCurrentLane.dequeueFromIncoming();
                    if (served != null) {
                        switch (finalLaneName) {
                            case "A": passedCountA_L1++; break;
                            case "B": passedCountB_L1++; break;
                            case "C": passedCountC_L1++; break;
                            case "D": passedCountD_L1++; break;
                        }
                        System.out.println("Served from L1: " + served);

                        final int delay = carIndex * 700;
                        carIndex++;

                        Timeline carTimeline = new Timeline(new KeyFrame(Duration.millis(delay), e2 -> {
                            trafficGenerator.createAndAnimateCar(finalLaneName, 1);
                        }));
                        carTimeline.play();
                    }
                }

                // Serve from Lane 3
                for (int i = 0; i < carsFromL3; i++) {
                    String served = finalCurrentLane.dequeueFromLeftTurn();
                    if (served != null) {
                        switch (finalLaneName) {
                            case "A": passedCountA_L3++; break;
                            case "B": passedCountB_L3++; break;
                            case "C": passedCountC_L3++; break;
                            case "D": passedCountD_L3++; break;
                        }
                        System.out.println("Served from L3: " + served);

                        final int delay = carIndex * 700;
                        carIndex++;

                        Timeline carTimeline = new Timeline(new KeyFrame(Duration.millis(delay), e2 -> {
                            trafficGenerator.createAndAnimateCar(finalLaneName, 3);
                        }));
                        carTimeline.play();
                    }
                }

                // FIXED: Green light duration based on actual cars served
                int greenDuration = 1500 + (totalCars * 600);

                // Green to yellow to red transition
                Timeline transitionTimeline = new Timeline(new KeyFrame(Duration.millis(greenDuration), e2 -> {
                    finalTrafficLight.setState(TrafficLight.State.YELLOW);
                    Timeline yellowTimeline = new Timeline(new KeyFrame(Duration.millis(700), e3 -> {
                        finalTrafficLight.setState(TrafficLight.State.RED);
                    }));
                    yellowTimeline.play();
                }));
                transitionTimeline.play();

                updateCount();
            }));
            greenLightDelay.play();
        } else {
            isProcessingCycle = false;
        }
    }

    private void updateCount() {
        Platform.runLater(() -> {
            countA.setText("L1:" + laneA.incomingSize() + " L2:" + laneA.prioritySize() + " L3:" + laneA.leftTurnSize());
            countB.setText("L1:" + laneB.incomingSize() + " L3:" + laneB.leftTurnSize());
            countC.setText("L1:" + laneC.incomingSize() + " L3:" + laneC.leftTurnSize());
            countD.setText("L1:" + laneD.incomingSize() + " L3:" + laneD.leftTurnSize());
        });
    }

    // Helper methods for UI
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
}