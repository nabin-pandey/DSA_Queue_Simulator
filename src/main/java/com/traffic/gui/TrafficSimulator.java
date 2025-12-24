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
import java.io.*;
import java.util.*;
import java.nio.file.* ;

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
    private Text fileStatusText;

    // Counters for passed cars
    private int passedCountA = 0;
    private int passedCountB = 0;
    private int passedCountC = 0;
    private int passedCountD = 0;
    private int passedPriorityA = 0 ; //for the count of the priority lane

    // UI components
    private Pane root;
    private double centerX;
    private double centerY;
    private final Pane simulationPane = new Pane();

    //Generate random numbers
    private final Random random_generator = new Random();

    // Traffic generator
    private TrafficGenerator trafficGenerator;

    //To read the files from the TrafficGenerator.
    private static final String Lane_A_File = "lanea.txt";
    private static final String Lane_B_File = "laneb.txt";
    private static final String Lane_C_File = "lanec.txt";
    private static final String Lane_D_File = "laned.txt";

    //Read the Files
    private Map<String, Long> lastReadPositiion = new HashMap<>();
    private Timeline filePollingTimeline ;

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

        //Inititating the reading of the position

        lastReadPositiion.put(Lane_A_File, 0L);
        lastReadPositiion.put(Lane_B_File, 0L);
        lastReadPositiion.put(Lane_C_File, 0L);
        lastReadPositiion.put(Lane_D_File, 0L);

        System.out.println("Traffic Simulator Started : ");
        System.out.println("Reading the files : ");
        System.out.println(" - "+Lane_A_File);
        System.out.println(" - "+Lane_B_File);
        System.out.println(" - "+Lane_C_File);
        System.out.println(" - "+Lane_D_File);
    }

    private void startFilePolling() {
        filePollingTimeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            //From the Generator files to Simulator
            readNewVehicles();
        }));
        filePollingTimeline.setCycleCount(Timeline.INDEFINITE);
        filePollingTimeline.play();
        updateFileStatus("Polling Files...", Color.GREEN);
    }

    private void readNewVehicles() {
        try {
            readVehicles(Lane_A_File, laneA, "A");
            readVehicles(Lane_B_File, laneB, "B");
            readVehicles(Lane_C_File, laneC, "C");
            readVehicles(Lane_D_File, laneD, "D");
        }catch (Exception error){
            System.out.println("Error reading files from the files : " + error.getMessage());
            updateFileStatus("File reading error", Color.RED);
        }
    }

    private void readVehicles(String filename, Lane lane ,String roadId){
        File file = new  File(filename);
        if(!file.exists()){
            return ;
        }
        try(RandomAccessFile random_file = new RandomAccessFile(file, "r")){
            long fileLength = random_file.length();
            long lastPosition = lastReadPositiion.getOrDefault(filename, 0L);

            //For the case when the file gets cleated or reset.
            if(lastPosition > fileLength){
                lastPosition = 0L ;
            }
            //It will be for the seeking of the last read position
            random_file.seek(lastPosition);

            String line ;
            while((line = random_file.readLine()) != null){
                processVehicleLine(line, lane, roadId);
            }

            //Will update the last read Position in the file
            lastReadPositiion.put(filename, random_file.getFilePointer());

        } catch (IOException e) {
            System.err.println("Error reading files from the files : " + e.getMessage());
        }
    }

    private void processVehicleLine(String line, Lane lane, String roadId){
        try{
            String[] parts = line.split(",");
            if(parts.length != 3){
                System.err.println("Invalid Line format : "+line);
                return;
            }

            String vehicleId = parts[0].trim();
            int laneNumber = Integer.parseInt(parts[1].trim());
            long timestamp = Long.parseLong(parts[2].trim());

            //Will add the vehilce in the proper Lane
            lane.enqueueToLane(laneNumber , vehicleId);

            trafficGenerator.addWaitingCar(roadId , laneNumber);

            String laneType = laneNumber == 1 ? "Normal" : (laneNumber == 2 ? "Priority" : "LeftTurn");

            System.out.println("Read from the file : " + vehicleId + "Lane" + laneNumber);

            updateFileStatus("Last Read : " + vehicleId + "lane" + laneNumber, Color.GREEN);
        } catch (Exception e) {
            System.err.println("Error processing line : " + line +"-"+ e.getMessage());
        }
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

    private void updateFileStatus(String message, Color color) {
        Platform.runLater(() -> {
            fileStatusText.setText(message);
            fileStatusText.setFill(color);
        });
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
            trafficScheduler.CheckandUpdatePriority(laneEntryA, laneA.incomingSize(), laneA.prioritySize());
            trafficScheduler.CheckandUpdatePriority(laneEntryB, laneB.incomingSize(),0);
            trafficScheduler.CheckandUpdatePriority(laneEntryC, laneC.incomingSize(),0);
            trafficScheduler.CheckandUpdatePriority(laneEntryD, laneD.incomingSize(),0);



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
                int carsToServe;
                boolean servingPriorityLane = false;
                // FIXED: Check if we should serve AL2 priority lane
                if (finalLaneName.equals("A") && finalCurrentLane.prioritySize() > 0 &&
                        trafficScheduler.isPriorityModeActive()) {
                    // Serve from AL2 until it drops below 5
                    carsToServe = Math.min(finalCurrentLane.prioritySize(), 6);
                    servingPriorityLane = true;
                    System.out.println("SERVING PRIORITY LANE AL2! Cars: " + carsToServe);

                }
                // FIXED: Use formula |V| = avg vehicles across lanen
                else {
                    List<LaneEntry> entries = new ArrayList<>();
                    entries.add(laneEntryA);
                    entries.add(laneEntryB);
                    entries.add(laneEntryC);
                    entries.add(laneEntryD);
                    int avgVehicles = trafficScheduler.calculateAverageVehicles(entries);
                    carsToServe = Math.max(1, Math.min(avgVehicles, finalCurrentLane.incomingSize()));
                    System.out.println("Average vehicles: " + avgVehicles + " | Serving: " + carsToServe);
                }
                final boolean isServingPriority = servingPriorityLane;
                trafficGenerator.releaseWaitingCars(finalLaneName , 3 , 1);

                //Fixing the generation of car from the all 3lanes
                int carsFromL1 = 0, carsFromL2 = 0 , carsFromL3 = 0;

                if(isServingPriority && finalLaneName.equals("A")){
                    //Server from AL2 Lane
                    carsFromL2 = Math.min(carsToServe, finalCurrentLane.prioritySize());
                }else{
                    //Generated Incoming cars will be distributed across L1 and L3
                    int l1Available = finalCurrentLane.incomingSize();
                    int l3Available = finalCurrentLane.leftTurnSize();

                    //Serve L1
                    carsFromL1 = Math.min(carsToServe / 2, l1Available);
                    //ThenL3
                    carsFromL3 = Math.min(carsToServe - carsFromL1, l3Available);
                }
                System.out.println("Serving L1 = " + carsFromL1 + "L2 = "+carsFromL2+"L3 = "+carsFromL3);

                //Release waiting cars virtually
                if(carsFromL1 > 0) trafficGenerator.releaseWaitingCars(finalLaneName,1, carsFromL1);
                if(carsFromL2 > 0) trafficGenerator.releaseWaitingCars(finalLaneName,2,carsFromL2);
                if(carsFromL3 > 0) trafficGenerator.releaseWaitingCars(finalLaneName, 3, carsFromL3);

                int carIndex = 0 ;
                //This one is for the Lane2 priority
                for(int i = 0 ; i < carsFromL2 ; i++){
                    String served = finalCurrentLane.dequeueFromPriority();
                    if(served != null) {
                        passedPriorityA++;
                        System.out.println("Served from AL2 : " + served);

                        final int delay = carIndex * 400;
                        carIndex++;

                        Timeline carTimeline = new Timeline(new KeyFrame(Duration.millis(delay), e2 -> {
                            trafficGenerator.createAndAnimateCar(finalLaneName, 2);
                        }));
                        carTimeline.play();
                    }
                }
                //Serve from the Lane 1
                for (int i = 0 ; i < carsFromL1 ; i++){
                    String served = finalCurrentLane.dequeueFromIncoming();
                    if(served != null) {
                        switch (finalLaneName){
                            case "A": passedCountA++ ; break ;
                            case "B" : passedCountB++ ; break ;
                            case "C" : passedCountC++ ; break ;
                            case "D" : passedCountD++ ; break ;

                        }
                        System.out.println("Served from L1 : " + served);
                        final int delay = carIndex * 400;
                        carIndex++;
                        Timeline carTimeline = new Timeline(new KeyFrame(Duration.millis(delay), e2 -> {
                            trafficGenerator.createAndAnimateCar(finalLaneName,1);
                        }));
                        carTimeline.play();
                    }
                }

                //THis is for the Lane 3
                for (int i = 0 ; i < carsFromL3 ; i++){
                    String served = finalCurrentLane.dequeueFromleftLane();
                    if(served != null) {
                        switch (finalLaneName){
                            case "A": passedCountA++ ; break ;
                            case "B" : passedCountB++ ; break ;
                            case "C" : passedCountC++ ; break ;
                            case "D" : passedCountD++ ; break ;

                        }
                        System.out.println("Served from L3 : " + served);
                        final int delay = carIndex * 400;
                        carIndex++;
                        Timeline carTimeline = new Timeline(new KeyFrame(Duration.millis(delay), e2 -> {
                            trafficGenerator.createAndAnimateCar(finalLaneName,3);
                        }));
                        carTimeline.play();
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
            }
        })) ;

        updateCount();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        }

    private void updateCount() {
                Platform.runLater(() -> {
                    countA.setText("AL1:" + laneA.incomingSize() + " | Passed:" + passedCountA);
                    countB.setText("AL2:" + laneB.incomingSize() + " | Passed:" + passedCountB);
                    countC.setText("AL3:" + laneC.incomingSize() + " | Passed:" + passedCountC);
                    countD.setText("AL4:" + laneD.incomingSize() + " | Passed:" + passedCountD);
                });
            }
        }
