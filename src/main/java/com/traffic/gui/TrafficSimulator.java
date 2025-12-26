
package com.traffic.gui;

import com.traffic.core.Lane;
import com.traffic.core.LaneEntry;
import com.traffic.core.TrafficScheduler;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class TrafficSimulator extends Application {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;
    private static final int LANE_WIDTH = 50;
    private static final int JUNCTION_SIZE = LANE_WIDTH * 4;
    private static final int LIGHT_SIZE = 10;
    private static final int ROAD_LENGTH = 300;

    private static final String LANE_A_FILE = "lanea.txt";
    private static final String LANE_B_FILE = "laneb.txt";
    private static final String LANE_C_FILE = "lanec.txt";
    private static final String LANE_D_FILE = "laned.txt";

    private Pane root;
    private final Pane simulationPane = new Pane();

    private double centerX, centerY;

    private Circle lightA, lightB, lightC, lightD;
    private TrafficLight trafficLightA, trafficLightB, trafficLightC, trafficLightD;

    private Lane laneA, laneB, laneC, laneD;
    private LaneEntry laneEntryA, laneEntryB, laneEntryC, laneEntryD;
    private TrafficScheduler trafficScheduler;

    private Text countA, countB, countC, countD, fileStatusText, debugText;

    private TrafficGenerator trafficGenerator;

    private final Map<String, Long> lastReadPosition = new HashMap<>();
    private Timeline filePollingTimeline;

    private boolean simulationRunning = false;
    private boolean isProcessingCycle = false;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        laneA = new Lane("A");
        laneB = new Lane("B");
        laneC = new Lane("C");
        laneD = new Lane("D");

        laneEntryA = new LaneEntry("A", 0);
        laneEntryB = new LaneEntry("B", 0);
        laneEntryC = new LaneEntry("C", 0);
        laneEntryD = new LaneEntry("D", 0);

        trafficScheduler = new TrafficScheduler(Arrays.asList(laneEntryA, laneEntryB, laneEntryC, laneEntryD));

        root = new Pane();
        root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        centerX = WINDOW_WIDTH / 2.0;
        centerY = WINDOW_HEIGHT / 2.0;

        trafficGenerator = new TrafficGenerator(laneA, laneB, laneC, laneD, simulationPane, centerX, centerY);

        buildJunctionUI();

        Button startButton = new Button("Start Simulation");
        startButton.setLayoutX(20);
        startButton.setLayoutY(WINDOW_HEIGHT - 60);
        root.getChildren().add(startButton);

        fileStatusText = new Text("Mode: FILE - Waiting to start...");
        fileStatusText.setLayoutX(20);
        fileStatusText.setLayoutY(WINDOW_HEIGHT - 30);
        fileStatusText.setFill(Color.BLUE);
        root.getChildren().add(fileStatusText);

        // Debug text to show current green light
        debugText = new Text("Current Green: NONE");
        debugText.setLayoutX(WINDOW_WIDTH - 200);
        debugText.setLayoutY(WINDOW_HEIGHT - 30);
        debugText.setFill(Color.DARKGREEN);
        root.getChildren().add(debugText);

        startButton.setOnAction(e -> {
            if (simulationRunning) return;

            if (!checkGeneratorFiles()) {
                updateFileStatus("ERROR: Run TrafficGeneratorProcess first!", Color.RED);
                return;
            }

            simulationRunning = true;
            startButton.setDisable(true);

            // Read from current end
            resetFilePositionsToEnd();

            startFilePolling();

            // Start chained traffic loop
            startSimulationLoop();

            updateFileStatus("Running...", Color.GREEN);
        });

        stage.setTitle("Traffic Simulator");
        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }

    private boolean checkGeneratorFiles() {
        return new File(LANE_A_FILE).exists()
                && new File(LANE_B_FILE).exists()
                && new File(LANE_C_FILE).exists()
                && new File(LANE_D_FILE).exists();
    }

    private void resetFilePositionsToEnd() {
        for (String f : Arrays.asList(LANE_A_FILE, LANE_B_FILE, LANE_C_FILE, LANE_D_FILE)) {
            File file = new File(f);
            lastReadPosition.put(f, file.exists() ? file.length() : 0L);
        }
    }

    private void startFilePolling() {
        if (filePollingTimeline != null) filePollingTimeline.stop();

        filePollingTimeline = new Timeline(new KeyFrame(Duration.millis(800), e -> {
            if (simulationRunning) readNewVehicles();
        }));
        filePollingTimeline.setCycleCount(Timeline.INDEFINITE);
        filePollingTimeline.play();
    }

    private void readNewVehicles() {
        int read = 0;
        read += readVehicles(LANE_A_FILE, laneA, "A");
        read += readVehicles(LANE_B_FILE, laneB, "B");
        read += readVehicles(LANE_C_FILE, laneC, "C");
        read += readVehicles(LANE_D_FILE, laneD, "D");
        if (read > 0) updateCount();
    }

    private int readVehicles(String filename, Lane lane, String roadId) {
        File file = new File(filename);
        if (!file.exists()) return 0;

        int count = 0;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long len = raf.length();
            long last = lastReadPosition.getOrDefault(filename, 0L);
            if (last > len) last = 0L;
            if (last == len) return 0;

            raf.seek(last);

            String line;
            while ((line = raf.readLine()) != null) {
                if (processLine(line, lane, roadId)) count++;
            }

            lastReadPosition.put(filename, raf.getFilePointer());
        } catch (IOException ignored) {
        }
        return count;
    }

    private boolean processLine(String line, Lane lane, String roadId) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 2) return false;

            String vehicleId = parts[0].trim();
            int laneNumber = Integer.parseInt(parts[1].trim());

            //lane1 are never sources allow only 2 or 3
            if (laneNumber != 2 && laneNumber != 3) return false;

            // source should match "A2" prefix
            if (!vehicleId.startsWith(roadId + laneNumber)) return false;

            lane.enqueueToLane(laneNumber, vehicleId);
            trafficGenerator.addWaitingCar(roadId, laneNumber, vehicleId);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    //Simulator
    private void startSimulationLoop() {
        runTrafficCycleChained();
    }

    private void runTrafficCycleChained() {
        if (!simulationRunning || isProcessingCycle) return;

        isProcessingCycle = true;

        try {
            //  Calculate BOTH priority and non-priority for ALL roads
            //Priority Size - Lane2 , LeftTurnSize -Lane 3

            int aPriority = laneA.prioritySize();
            int aNonPriority = laneA.leftTurnSize();

            int bPriority = laneB.prioritySize();
            int bNonPriority = laneB.leftTurnSize();

            int cPriority = laneC.prioritySize();
            int cNonPriority = laneC.leftTurnSize();

            int dPriority = laneD.prioritySize();
            int dNonPriority = laneD.leftTurnSize();

            trafficScheduler.CheckandUpdatePriority(laneEntryA, aNonPriority, aPriority);
            trafficScheduler.CheckandUpdatePriority(laneEntryB, bNonPriority, bPriority);
            trafficScheduler.CheckandUpdatePriority(laneEntryC, cNonPriority, cPriority);
            trafficScheduler.CheckandUpdatePriority(laneEntryD, dNonPriority, dPriority);

            // rely on scheduler rotation
            String nextRoad = trafficScheduler.serverAndRotateLane();

            // Debug output
            System.out.println(" Cycle Start - Selected Road: " + nextRoad);
            System.out.println("   A: L2=" + aPriority + " L3=" + aNonPriority);
            System.out.println("   B: L2=" + bPriority + " L3=" + bNonPriority);
            System.out.println("   C: L2=" + cPriority + " L3=" + cNonPriority);
            System.out.println("   D: L2=" + dPriority + " L3=" + dNonPriority);

            // set all lights red
            trafficLightA.setState(TrafficLight.State.RED);
            trafficLightB.setState(TrafficLight.State.RED);
            trafficLightC.setState(TrafficLight.State.RED);
            trafficLightD.setState(TrafficLight.State.RED);

            TrafficLight selected = getLight(nextRoad);
            selected.setState(TrafficLight.State.GREEN);

            //  debug display
            updateDebugText("Current Green: " + nextRoad);

            // release ONLY lanes 2 and 3
            Lane laneObj = getLane(nextRoad);
            int released = 0;

            if (laneObj.prioritySize() > 0) {
                trafficGenerator.releaseWaitingCars(nextRoad, 2, 1);
                released++;
                System.out.println("   Released 1 car from " + nextRoad + " L2");
            }

            if (laneObj.leftTurnSize() > 0) {
                trafficGenerator.releaseWaitingCars(nextRoad, 3, 1);
                released++;
                System.out.println("   Released 1 car from " + nextRoad + " L3");
            }

            if (released == 0) {
                System.out.println("   No cars to release from " + nextRoad);
            }

            PauseTransition green = new PauseTransition(Duration.seconds(4));
            green.setOnFinished(e -> {
                selected.setState(TrafficLight.State.YELLOW);

                PauseTransition yellow = new PauseTransition(Duration.seconds(1.5));
                yellow.setOnFinished(ev -> {
                    selected.setState(TrafficLight.State.RED);
                    isProcessingCycle = false;

                    // Update counts
                    updateCount();

                    // Schedule next cycle immediately
                    Platform.runLater(this::runTrafficCycleChained);
                });
                yellow.play();
            });
            green.play();

        } catch (Exception ex) {
            System.err.println(" Error in traffic cycle: " + ex.getMessage());
            ex.printStackTrace();
            isProcessingCycle = false;

            // Retry after error
            PauseTransition retry = new PauseTransition(Duration.seconds(2));
            retry.setOnFinished(e -> Platform.runLater(this::runTrafficCycleChained));
            retry.play();
        }
    }

    private TrafficLight getLight(String road) {
        switch (road) {
            case "A": return trafficLightA;
            case "B": return trafficLightB;
            case "C": return trafficLightC;
            case "D": return trafficLightD;
            default: return trafficLightA;
        }
    }

    private Lane getLane(String road) {
        switch (road) {
            case "A": return laneA;
            case "B": return laneB;
            case "C": return laneC;
            case "D": return laneD;
            default: return laneA;
        }
    }

    private void updateCount() {
        Platform.runLater(() -> {
            countA.setText("A L2:" + laneA.prioritySize() + " L3:" + laneA.leftTurnSize());
            countB.setText("B L2:" + laneB.prioritySize() + " L3:" + laneB.leftTurnSize());
            countC.setText("C L2:" + laneC.prioritySize() + " L3:" + laneC.leftTurnSize());
            countD.setText("D L2:" + laneD.prioritySize() + " L3:" + laneD.leftTurnSize());
        });
    }


    //Fixed File Reading
    private void updateFileStatus(String msg, Color c) {
        Platform.runLater(() -> {
            fileStatusText.setText(msg);
            fileStatusText.setFill(c);
        });
    }

    private void updateDebugText(String msg) {
        Platform.runLater(() -> debugText.setText(msg));
    }

    //For UI
    private void buildJunctionUI() {

        root.getChildren().add(new Rectangle(WINDOW_WIDTH, WINDOW_HEIGHT, Color.web("#f3f3f3")));

        Rectangle junction = new Rectangle(JUNCTION_SIZE, JUNCTION_SIZE, Color.web("#4b4b4b"));
        junction.setX(centerX - JUNCTION_SIZE / 2);
        junction.setY(centerY - JUNCTION_SIZE / 2);
        root.getChildren().add(junction);

        double roadThickness = LANE_WIDTH * 3;

        root.getChildren().addAll(
                createRoad(centerX - roadThickness / 2, centerY - JUNCTION_SIZE / 2 - ROAD_LENGTH, roadThickness, ROAD_LENGTH),
                createRoad(centerX - roadThickness / 2, centerY + JUNCTION_SIZE / 2, roadThickness, ROAD_LENGTH),
                createRoad(centerX + JUNCTION_SIZE / 2, centerY - roadThickness / 2, ROAD_LENGTH, roadThickness),
                createRoad(centerX - JUNCTION_SIZE / 2 - ROAD_LENGTH, centerY - roadThickness / 2, ROAD_LENGTH, roadThickness)
        );

        drawLaneMarkings(centerX - roadThickness / 2, centerY - JUNCTION_SIZE / 2 - ROAD_LENGTH, roadThickness, ROAD_LENGTH, true);
        drawLaneMarkings(centerX - roadThickness / 2, centerY + JUNCTION_SIZE / 2, roadThickness, ROAD_LENGTH, true);
        drawLaneMarkings(centerX + JUNCTION_SIZE / 2, centerY - roadThickness / 2, ROAD_LENGTH, roadThickness, false);
        drawLaneMarkings(centerX - JUNCTION_SIZE / 2 - ROAD_LENGTH, centerY - roadThickness / 2, ROAD_LENGTH, roadThickness, false);

        lightA = new Circle(centerX, centerY - JUNCTION_SIZE / 2 - 25, LIGHT_SIZE, Color.RED);
        lightB = new Circle(centerX, centerY + JUNCTION_SIZE / 2 + 25, LIGHT_SIZE, Color.RED);
        lightC = new Circle(centerX + JUNCTION_SIZE / 2 + 25, centerY, LIGHT_SIZE, Color.RED);
        lightD = new Circle(centerX - JUNCTION_SIZE / 2 - 25, centerY, LIGHT_SIZE, Color.RED);

        root.getChildren().addAll(lightA, lightB, lightC, lightD);

        trafficLightA = new TrafficLight(lightA);
        trafficLightB = new TrafficLight(lightB);
        trafficLightC = new TrafficLight(lightC);
        trafficLightD = new TrafficLight(lightD);

        countA = new Text(centerX - 120, centerY - JUNCTION_SIZE / 2 - ROAD_LENGTH + 25, "A L2:0 L3:0");
        countB = new Text(centerX - 120, centerY + JUNCTION_SIZE / 2 + ROAD_LENGTH - 5, "B L2:0 L3:0");
        countC = new Text(centerX + JUNCTION_SIZE / 2 + ROAD_LENGTH - 120, centerY - 25, "C L2:0 L3:0");
        countD = new Text(centerX - JUNCTION_SIZE / 2 - ROAD_LENGTH + 10, centerY - 25, "D L2:0 L3:0");

        root.getChildren().addAll(countA, countB, countC, countD, simulationPane);
    }

    private Rectangle createRoad(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(w, h);
        r.setX(x);
        r.setY(y);
        r.setFill(Color.web("#3f3f3f"));
        return r;
    }

    private void drawLaneMarkings(double x, double y, double w, double h, boolean vertical) {
        for (int i = 1; i < 3; i++) {
            Line l = vertical
                    ? new Line(x + w / 3 * i, y, x + w / 3 * i, y + h)
                    : new Line(x, y + h / 3 * i, x + w, y + h / 3 * i);

            l.getStrokeDashArray().addAll(8.0, 10.0);
            l.setStrokeWidth(2);
            l.setStroke(Color.web("#eeeeee"));
            root.getChildren().add(l);
        }
    }
}
