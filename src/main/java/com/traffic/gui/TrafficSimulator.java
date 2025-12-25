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
import java.util.*;
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

        List<LaneEntry> entries = Arrays.asList(laneEntryA, laneEntryB, laneEntryC, laneEntryD);
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
                startFilePolling();
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

        // Initialize file reading positions
        lastReadPosition.put(LANE_A_FILE, 0L);
        lastReadPosition.put(LANE_B_FILE, 0L);
        lastReadPosition.put(LANE_C_FILE, 0L);
        lastReadPosition.put(LANE_D_FILE, 0L);

        System.out.println("========================================");
        System.out.println("Traffic Simulator Started (FILE MODE)");
        System.out.println("Run TrafficGeneratorProcess.java first!");
        System.out.println("========================================");
    }

    //FIX: Clear old data files
    private void clearOldFiles() {
        new File(LANE_A_FILE).delete();
        new File(LANE_B_FILE).delete();
        new File(LANE_C_FILE).delete();
        new File(LANE_D_FILE).delete();

    }

    private void startFilePolling() {
        if (filePollingTimeline != null) filePollingTimeline.stop();

        filePollingTimeline = new Timeline(new KeyFrame(Duration.millis(300), e -> {
            if (simulationRunning) readNewVehicles();
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
            if (totalRead > 0) updateCount();
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
                if (processVehicleLine(line, lane, roadId)) vehiclesRead++;
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

            lane.enqueueToLane(laneNumber, vehicleId);

            // Add visual waiting car
            trafficGenerator.addWaitingCar(roadId, laneNumber);

            System.out.println( vehicleId + "Road " + roadId + " Lane " + laneNumber);
            updateFileStatus("Last: " + vehicleId, Color.GREEN);
            return true;

        } catch (Exception e) {
            System.err.println("Error processing line: " + line);
            return false;
        }
    }

    private void updateFileStatus(String message, Color color) {
        Platform.runLater(() -> {
            fileStatusText.setText("FILE " + message);
            fileStatusText.setFill(color);
        });
    }
    //Simulator
    private void startSimulationLoop() {
        if (simulationTimeline != null) simulationTimeline.stop();

        simulationTimeline = new Timeline(new KeyFrame(Duration.seconds(6), e -> {
            if (simulationRunning) processTrafficCycle();
        }));

        simulationTimeline.setCycleCount(Timeline.INDEFINITE);
        simulationTimeline.play();
    }

    // FIXED: Separate method for processing traffic cycle
    private void processTrafficCycle() {
        // Update vehicle counts
        laneEntryA.setVehicleCount(laneA.incomingSize());
        laneEntryB.setVehicleCount(laneB.incomingSize());
        laneEntryC.setVehicleCount(laneC.incomingSize());
        laneEntryD.setVehicleCount(laneD.incomingSize());

        // Update priorities
        trafficScheduler.CheckandUpdatePriority(laneEntryA, laneA.incomingSize(), laneA.prioritySize());
        trafficScheduler.CheckandUpdatePriority(laneEntryB, laneB.incomingSize(), 0);
        trafficScheduler.CheckandUpdatePriority(laneEntryC, laneC.incomingSize(), 0);
        trafficScheduler.CheckandUpdatePriority(laneEntryD, laneD.incomingSize(), 0);

        String nextLane = trafficScheduler.serverAndRotateLane().trim();

        System.out.println("\n=== Serving Lane: " + nextLane + " ===");
        System.out.println("Queue sizes - A(L1:" + laneA.incomingSize() + " L2:" + laneA.prioritySize() + " L3:" + laneA.leftTurnSize() + ")");
        System.out.println("                B(L1:" + laneB.incomingSize() + " L3:" + laneB.leftTurnSize() + ")");
        System.out.println("                C(L1:" + laneC.incomingSize() + " L3:" + laneC.leftTurnSize() + ")");
        System.out.println("                D(L1:" + laneD.incomingSize() + " L3:" + laneD.leftTurnSize() + ")");

            // Set all lights to red
            trafficLightA.setState(TrafficLight.State.RED);
            trafficLightB.setState(TrafficLight.State.RED);
            trafficLightC.setState(TrafficLight.State.RED);
            trafficLightD.setState(TrafficLight.State.RED);

        TrafficLight currentLight = null;
        Lane currentLane = null;

        switch (nextLane) {
            case "A": currentLight = trafficLightA; currentLane = laneA; break;
            case "B": currentLight = trafficLightB; currentLane = laneB; break;
            case "C": currentLight = trafficLightC; currentLane = laneC; break;
            case "D": currentLight = trafficLightD; currentLane = laneD; break;
        }

        if (currentLight == null || currentLane == null) return;

        final TrafficLight light = currentLight;
        final Lane lane = currentLane;
        final String laneName = nextLane;

        // Wait before turning green
        PauseTransition pause = new PauseTransition(Duration.millis(800));
        pause.setOnFinished(e -> {
            light.setState(TrafficLight.State.GREEN);

            // Calculate cars to serve
            int carsToServe;
            boolean servingPriority = false;

            if (laneName.equals("A") && lane.prioritySize() > 0 && trafficScheduler.isPriorityModeActive()) {
                carsToServe = Math.min(3, lane.prioritySize());
                servingPriority = true;
                System.out.println(">>> PRIORITY MODE: Serving " + carsToServe + " from AL2");
            } else {
                List<LaneEntry> entries = Arrays.asList(laneEntryA, laneEntryB, laneEntryC, laneEntryD);
                int avg = trafficScheduler.calculateAverageVehicles(entries);
                int available = lane.incomingSize() + lane.leftTurnSize();
                carsToServe = Math.min(3, Math.max(1, Math.min(avg, available)));
                System.out.println("Serving " + carsToServe + " cars");
            }

            // Distribute cars
            int carsL1 = 0, carsL2 = 0, carsL3 = 0;

            if (servingPriority) {
                carsL2 = Math.min(carsToServe, lane.prioritySize());
            } else {
                int l1Avail = lane.incomingSize();
                int l3Avail = lane.leftTurnSize();
                carsL1 = Math.min(carsToServe / 2, l1Avail);
                carsL3 = Math.min(carsToServe - carsL1, l3Avail);
                if (carsL3 == 0) carsL1 = Math.min(carsToServe, l1Avail);
            }

            System.out.println("Release: L1=" + carsL1 + " L2=" + carsL2 + " L3=" + carsL3);

            // Release cars with delays
            int carIndex = 0;

            // L2 (Priority)
            for (int i = 0; i < carsL2; i++) {
                String id = lane.dequeueFromPriority();
                if (id != null) {
                    final int delay = carIndex++ * 1000;
                    new Timeline(new KeyFrame(Duration.millis(delay), ev -> {
                        trafficGenerator.releaseWaitingCars(laneName, 2, 1);
                        trafficGenerator.createAndAnimateCar(laneName, 2);
                    })).play();
                }
            }

            // L1
            for (int i = 0; i < carsL1; i++) {
                String id = lane.dequeueFromIncoming();
                if (id != null) {
                    final int delay = carIndex++ * 1000;
                    new Timeline(new KeyFrame(Duration.millis(delay), ev -> {
                        trafficGenerator.releaseWaitingCars(laneName, 1, 1);
                        trafficGenerator.createAndAnimateCar(laneName, 1);
                    })).play();
                }
            }

            // L3
            for (int i = 0; i < carsL3; i++) {
                String id = lane.dequeueFromLeftTurn();
                if (id != null) {
                    final int delay = carIndex++ * 1000;
                    new Timeline(new KeyFrame(Duration.millis(delay), ev -> {
                        trafficGenerator.releaseWaitingCars(laneName, 3, 1);
                        trafficGenerator.createAndAnimateCar(laneName, 3);
                    })).play();
                }
            }

            int totalCars = carsL1 + carsL2 + carsL3;
            int greenDuration = 2000 + (totalCars * 900);

            // Light transition
            new Timeline(new KeyFrame(Duration.millis(greenDuration), ev -> {
                light.setState(TrafficLight.State.YELLOW);
                new Timeline(new KeyFrame(Duration.millis(1200), ev2 -> {
                    light.setState(TrafficLight.State.RED);
                })).play();
            })).play();

            updateCount();
        });
        pause.play();
    }

    private void updateCount() {
        Platform.runLater(() -> {
            countA.setText("L1:" + laneA.incomingSize() + " L2:" + laneA.prioritySize() + " L3:" + laneA.leftTurnSize());
            countB.setText("L1:" + laneB.incomingSize() + " L3:" + laneB.leftTurnSize());
            countC.setText("L1:" + laneC.incomingSize() + " L3:" + laneC.leftTurnSize());
            countD.setText("L1:" + laneD.incomingSize() + " L3:" + laneD.leftTurnSize());
        });
    }

    private void buildJunctionUI() {
        Rectangle background = new Rectangle(WINDOW_WIDTH, WINDOW_HEIGHT);
        background.setFill(Color.web("#f3f3f3"));
        root.getChildren().add(background);

        Rectangle grassTL = makeGrass(0, 0, centerX - JUNCTION_SIZE/2 - 60, centerY - JUNCTION_SIZE/2 - 60);
        Rectangle grassTR = makeGrass(centerX + JUNCTION_SIZE/2 + 60, 0, WINDOW_WIDTH - (centerX + JUNCTION_SIZE/2 + 60), centerY - JUNCTION_SIZE/2 - 60);
        Rectangle grassBL = makeGrass(0, centerY + JUNCTION_SIZE/2 + 60, centerX - JUNCTION_SIZE/2 - 60, WINDOW_HEIGHT - (centerY + JUNCTION_SIZE/2 + 60));
        Rectangle grassBR = makeGrass(centerX + JUNCTION_SIZE/2 + 60, centerY + JUNCTION_SIZE/2 + 60, WINDOW_WIDTH - (centerX + JUNCTION_SIZE/2 + 60), WINDOW_HEIGHT - (centerY + JUNCTION_SIZE/2 + 60));
        root.getChildren().addAll(grassTL, grassTR, grassBL, grassBR);

        // Junction
        Rectangle junction = new Rectangle(JUNCTION_SIZE, JUNCTION_SIZE, Color.web("#4b4b4b"));
        junction.setX(centerX - JUNCTION_SIZE / 2);
        junction.setY(centerY - JUNCTION_SIZE / 2);
        root.getChildren().add(junction);

        double roadThickness = LANE_WIDTH * 3;

        Rectangle roadA = new Rectangle(roadThickness, ROAD_LENGTH);
        roadA.setX(centerX - roadThickness/2);
        roadA.setY(centerY - JUNCTION_SIZE/2 - ROAD_LENGTH);
        roadA.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadA);

        Rectangle roadB = new Rectangle(roadThickness, ROAD_LENGTH);
        roadB.setX(centerX - roadThickness/2);
        roadB.setY(centerY + JUNCTION_SIZE/2);
        roadB.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadB);

        Rectangle roadC = new Rectangle(ROAD_LENGTH, roadThickness);
        roadC.setX(centerX + JUNCTION_SIZE/2);
        roadC.setY(centerY - roadThickness/2);
        roadC.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadC);

        Rectangle roadD = new Rectangle(ROAD_LENGTH, roadThickness);
        roadD.setX(centerX - JUNCTION_SIZE/2 - ROAD_LENGTH);
        roadD.setY(centerY - roadThickness/2);
        roadD.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadD);

        drawLaneMarkings(centerX - roadThickness/2, centerY - JUNCTION_SIZE/2 - ROAD_LENGTH, roadThickness, ROAD_LENGTH, true);
        drawLaneMarkings(centerX - roadThickness/2, centerY + JUNCTION_SIZE/2, roadThickness, ROAD_LENGTH, true);
        drawLaneMarkings(centerX + JUNCTION_SIZE/2, centerY - roadThickness/2, ROAD_LENGTH, roadThickness, false);
        drawLaneMarkings(centerX - JUNCTION_SIZE/2 - ROAD_LENGTH, centerY - roadThickness/2, ROAD_LENGTH, roadThickness, false);

        drawCrosswalks();

        lightA = new Circle(centerX, centerY - JUNCTION_SIZE/2 - 20, LIGHT_SIZE, Color.RED);
        lightB = new Circle(centerX, centerY + JUNCTION_SIZE/2 + 20, LIGHT_SIZE, Color.RED);
        lightC = new Circle(centerX + JUNCTION_SIZE/2 + 20, centerY, LIGHT_SIZE, Color.RED);
        lightD = new Circle(centerX - JUNCTION_SIZE/2 - 20, centerY, LIGHT_SIZE, Color.RED);
        root.getChildren().addAll(lightA, lightB, lightC, lightD);

        trafficLightA = new TrafficLight(lightA);
        trafficLightB = new TrafficLight(lightB);
        trafficLightC = new TrafficLight(lightC);
        trafficLightD = new TrafficLight(lightD);

        // Labels
        root.getChildren().addAll(
            new Text(centerX - 12, centerY - ROAD_LENGTH - 30, "Road A"),
            new Text(centerX - 12, centerY + ROAD_LENGTH + LANE_WIDTH + 10, "Road B"),
            new Text(centerX + ROAD_LENGTH + 40, centerY + 4, "Road C"),
            new Text(centerX - ROAD_LENGTH - 40, centerY + 4, "Road D")
        );

        countA = new Text(centerX - 60, centerY - JUNCTION_SIZE/2 - ROAD_LENGTH + 30, "L1:0 L2:0 L3:0");
        countB = new Text(centerX - 60, centerY + JUNCTION_SIZE/2 + ROAD_LENGTH - 10, "L1:0 L3:0");
        countC = new Text(centerX + JUNCTION_SIZE/2 + ROAD_LENGTH - 80, centerY - 20, "L1:0 L3:0");
        countD = new Text(centerX - JUNCTION_SIZE/2 - ROAD_LENGTH + 20, centerY - 20, "L1:0 L3:0");
        root.getChildren().addAll(countA, countB, countC, countD, simulationPane);
    }

    private Rectangle makeGrass(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(w, h);
        r.setX(x);
        r.setY(y);
        r.setFill(Color.web("#b6d68b"));
        return r;
    }

    private void drawLaneMarkings(double x, double y, double w, double h, boolean vertical) {
        for (int i = 1; i < 3; i++) {
            Line l;
            if (vertical) {
                double markX = x + w/3 * i;
                l = new Line(markX, y, markX, y + h);
            } else {
                double markY = y + h/3 * i;
                l = new Line(x, markY, x + w, markY);
            }
            l.getStrokeDashArray().addAll(8.0, 10.0);
            l.setStrokeWidth(2);
            l.setStroke(Color.web("#eeeeee"));
            root.getChildren().add(l);
        }
    }

    private void drawCrosswalks() {
        double roadWidth = LANE_WIDTH * 3;
        drawCrosswalk(centerX - roadWidth/2, centerY - JUNCTION_SIZE/2 - 12, roadWidth, 8, true);
        drawCrosswalk(centerX - roadWidth/2, centerY + JUNCTION_SIZE/2 + 4, roadWidth, 8, true);
        drawCrosswalk(centerX + JUNCTION_SIZE/2 + 4, centerY - roadWidth/2, 8, roadWidth, false);
        drawCrosswalk(centerX - JUNCTION_SIZE/2 - 12, centerY - roadWidth/2, 8, roadWidth, false);
    }

    private void drawCrosswalk(double x, double y, double w, double h, boolean horizontal) {
        int stripes = horizontal ? (int)(w / 20) : (int)(h / 20);
        for (int i = 0; i < stripes; i++) {
            Rectangle stripe = new Rectangle(horizontal ? 15 : w, horizontal ? h : 15);
            stripe.setX(horizontal ? x + i * 20 : x);
            stripe.setY(horizontal ? y : y + i * 20);
            stripe.setFill(Color.WHITE);
            root.getChildren().add(stripe);
        }
    }
}