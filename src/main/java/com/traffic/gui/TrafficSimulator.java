

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



public class TrafficSimulator extends Application {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;
    private static final int LANE_WIDTH = 50;
    private static final int JUNCTION_SIZE = LANE_WIDTH * 4;
    private static final int LIGHT_SIZE = 10;
    private static final int ROAD_LENGTH = 300;

    //Traffic Light Circle (Red is shown initially)
    private Circle lightA;
    private Circle lightB;
    private Circle lightC;
    private Circle lightD;

    //Lane Objects
    private Lane laneA;
    private Lane laneB;
    private Lane laneC;
    private Lane laneD;

    private LaneEntry laneEntryA;
    private LaneEntry laneEntryB;
    private LaneEntry laneEntryC;
    private LaneEntry laneEntryD;

    private TrafficScheduler trafficScheduler;

    //Displaying counts
    private Text countA;
    private Text countB;
    private Text countC;
    private Text countD;

    //Counters for the passed Car:
    private int passedCountA = 0;
    private int passedCountB = 0;
    private int passedCountC = 0;
    private int passedCountD = 0;

    //Generate random numbers
    private final Random random_generator = new Random();

    // root pane reference for helper methods
    private Pane root;

    // center values for convenience
    private double centerX;
    private double centerY;

    private final Pane simulationPane = new Pane();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        laneA = new Lane("A");
        laneB = new Lane("B");
        laneC = new Lane("C");
        laneD = new Lane("D");

        //Lane Entries. Initial Count : 0
        laneEntryA = new LaneEntry("A", 0);
        laneEntryB = new LaneEntry("B", 0);
        laneEntryC = new LaneEntry("C", 0);
        laneEntryD = new LaneEntry("D", 0);

        List<LaneEntry> entries = new ArrayList<>();
        entries.add(laneEntryA);
        entries.add(laneEntryB);
        entries.add(laneEntryC);
        entries.add(laneEntryD);

        //Call Scheduler from TrafficScheduler
        trafficScheduler = new TrafficScheduler(entries);

        root = new Pane();
        root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        centerX = WINDOW_WIDTH / 2.0;
        centerY = WINDOW_HEIGHT / 2.0;

        // build scene (roads, junction, lights, labels)
        buildJunctionUI();

        //Simple Button
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

    // Build properly aligned roads, lanes, junction, lights and labels
    private void buildJunctionUI() {

        // background (light)
        Rectangle background = new Rectangle(WINDOW_WIDTH, WINDOW_HEIGHT);
        background.setFill(Color.web("#f3f3f3"));
        root.getChildren().add(background);

        // grass shoulders (corners)
        Rectangle grassTopLeft = makeGrass(0, 0, centerX - JUNCTION_SIZE / 2 - 60, centerY - JUNCTION_SIZE / 2 - 60);
        Rectangle grassTopRight = makeGrass(centerX + JUNCTION_SIZE / 2 + 60, 0,
                WINDOW_WIDTH - (centerX + JUNCTION_SIZE / 2 + 60), centerY - JUNCTION_SIZE / 2 - 60);
        Rectangle grassBottomLeft = makeGrass(0, centerY + JUNCTION_SIZE / 2 + 60,
                centerX - JUNCTION_SIZE / 2 - 60, WINDOW_HEIGHT - (centerY + JUNCTION_SIZE / 2 + 60));
        Rectangle grassBottomRight = makeGrass(centerX + JUNCTION_SIZE / 2 + 60, centerY + JUNCTION_SIZE / 2 + 60,
                WINDOW_WIDTH - (centerX + JUNCTION_SIZE / 2 + 60), WINDOW_HEIGHT - (centerY + JUNCTION_SIZE / 2 + 60));
        root.getChildren().addAll(grassTopLeft, grassTopRight, grassBottomLeft, grassBottomRight);

        // Center junction (square)
        Rectangle junction = new Rectangle(JUNCTION_SIZE, JUNCTION_SIZE);
        junction.setX(centerX - JUNCTION_SIZE / 2.0);
        junction.setY(centerY - JUNCTION_SIZE / 2.0);
        junction.setFill(Color.web("#4b4b4b"));
        root.getChildren().add(junction);

        // Create the 3-lane wide roads on each direction
        // Each road thickness = LANE_WIDTH * 3 (3 lanes)
        double roadThickness = LANE_WIDTH * 3;

        // Road A (top -> down)
        Rectangle roadA = new Rectangle(roadThickness, ROAD_LENGTH);
        roadA.setX(centerX - roadThickness / 2.0);
        roadA.setY(centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH);
        roadA.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadA);

        // Road B (bottom -> up)
        Rectangle roadB = new Rectangle(roadThickness, ROAD_LENGTH);
        roadB.setX(centerX - roadThickness / 2.0);
        roadB.setY(centerY + JUNCTION_SIZE / 2.0);
        roadB.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadB);

        // Road C (right -> left)
        Rectangle roadC = new Rectangle(ROAD_LENGTH , roadThickness);
        roadC.setX(centerX + JUNCTION_SIZE / 2.0);
        roadC.setY(centerY - roadThickness / 2.0);
        roadC.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadC);

        // Road D (left -> right)
        Rectangle roadD = new Rectangle(ROAD_LENGTH , roadThickness);
        roadD.setX(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH);
        roadD.setY(centerY - roadThickness / 2.0);
        roadD.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadD);

        // Draw junction FIRST (so roads are on top)

        Rectangle junctionOverlay = new Rectangle(JUNCTION_SIZE, JUNCTION_SIZE);

        junctionOverlay.setX(centerX - JUNCTION_SIZE / 2.0);

        junctionOverlay.setY(centerY - JUNCTION_SIZE / 2.0);

        junctionOverlay.setFill(Color.web("#4b4b4b"));

        root.getChildren().add(junctionOverlay);

        // lane markings for each road (draw simple dashed white separators)
        drawLaneMarkingsForVerticalRoad(centerX - roadThickness / 2.0, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH,
                roadThickness, ROAD_LENGTH );
        drawLaneMarkingsForVerticalRoad(centerX - roadThickness / 2.0, centerY + JUNCTION_SIZE / 2.0,
                roadThickness, ROAD_LENGTH );
        drawLaneMarkingsForHorizontalRoad(centerX + JUNCTION_SIZE / 2.0, centerY - roadThickness / 2.0,
                ROAD_LENGTH , roadThickness);
        drawLaneMarkingsForHorizontalRoad(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY - roadThickness / 2.0,
                ROAD_LENGTH , roadThickness);

        drawAllCrossWalks();


        // Traffic Lights - place them at corners near junction for each road (use your variable names)
        lightA = new Circle(centerX -  (LANE_WIDTH) / 2.0, centerY - JUNCTION_SIZE / 2.0 - 20, LIGHT_SIZE, Color.RED);
        lightB = new Circle(centerX + (LANE_WIDTH) / 2.0, centerY + JUNCTION_SIZE / 2.0 + 20, LIGHT_SIZE, Color.RED);
        lightC = new Circle(centerX + JUNCTION_SIZE / 2.0 + 20, centerY - (LANE_WIDTH) / 2.0, LIGHT_SIZE, Color.RED);
        lightD = new Circle(centerX - JUNCTION_SIZE / 2.0 - 20, centerY + (LANE_WIDTH) / 2.0, LIGHT_SIZE, Color.RED);
        root.getChildren().addAll(lightA, lightB, lightC, lightD);

        // Labels
        Text LabelA = new Text(centerX - 12, centerY - ROAD_LENGTH - LANE_WIDTH / 2.0 + 8, "Road A");
        Text LabelB = new Text(centerX - 12, centerY + ROAD_LENGTH + LANE_WIDTH, "Road B");
        Text LabelC = new Text(centerX + ROAD_LENGTH + LANE_WIDTH / 2.0 - 10, centerY + 4, "Road C");
        Text LabelD = new Text(centerX - ROAD_LENGTH - LANE_WIDTH + 6, centerY + 4, "Road D");
        root.getChildren().addAll(LabelA, LabelB, LabelC, LabelD);

        // Count Texts near each road entry
        countA = new Text(centerX - 60, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 20, "Queue : 0 | Passed : 0 ");
        countB = new Text(centerX - 60, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 6, "Queue : 0 | Passed : 0");
        countC = new Text(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 100, centerY - 35, "Queue : 0 | Passed : 0");
        countD = new Text(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 10, centerY - 35, "Queue : 0 | Passed : 0 " );
        root.getChildren().addAll(countA, countB, countC, countD);

        // Add simulation pane LAST so cars appear on top of roads
        root.getChildren().add(simulationPane);
    }

    // Make a grass rectangle
    private Rectangle makeGrass(double x, double y, double w, double h) {
        Rectangle r = new Rectangle(w, h);
        r.setX(x);
        r.setY(y);
        r.setFill(Color.web("#b6d68b"));
        return r;
    }

    // lane markings for vertical road: draw separators to represent lanes (3 lanes)
    private void drawLaneMarkingsForVerticalRoad(double x, double y, double width, double height) {
        double third = width / 3.0;
        for (int i = 1; i < 3; i++) {
            double markX = x + third * i;
            // dashed vertical line
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

    private void drawAllCrossWalks(){
        double roadWidth = LANE_WIDTH * 3 ;

        double crosswalkAY = centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH - 12 ;
        double crosswalkAX = centerX - roadWidth / 2.0  ;

        drawCrosswalkHorizontalStripes(crosswalkAX, crosswalkAY, roadWidth);

        double crosswalkBY = centerY + JUNCTION_SIZE / 2.0 + 4 ;
        double crosswalkBX = centerX - roadWidth / 2.0  ;

        drawCrosswalkHorizontalStripes(crosswalkBX , crosswalkBY , roadWidth);

        double crosswalkCY = centerY - roadWidth / 2.0  ;
        double crosswalkCX = centerX + JUNCTION_SIZE / 2.0 + 4 ;

        drawCrosswalkVerticalStripes(crosswalkCX, crosswalkCY, roadWidth);

        double crosswalkDX = centerX - JUNCTION_SIZE / 2.0  - 12 ;
        double crosswalkDY = centerY - roadWidth / 2.0;

        drawCrosswalkVerticalStripes(crosswalkDX, crosswalkDY , roadWidth);
    }

    private void drawCrosswalkVerticalStripes(double x , double y , double roadHeight) {

        double stripeWidth = 8;
        double stripeHeight = 15 ;
        double spacing = 5;

        // Draw stripes across the full width of the road
        int numStripes = (int) (roadHeight / (stripeHeight + spacing));

        for (int i = 0; i < numStripes; i++) {
            Rectangle stripe = new Rectangle(stripeWidth, stripeHeight);
         stripe.setX(x);
         stripe.setY(y  + i * (stripeHeight + spacing)) ;
         stripe.setFill(Color.WHITE);
            root.getChildren().add(stripe);
        }
    }

    private void drawCrosswalkHorizontalStripes(double x, double y , double roadWidth) {


        double stripeWidth = 15;
        double stripeHeight = 8;
        double spacing = 5;

        int numStripes = (int) (roadWidth / (stripeWidth + spacing)) ;

        for (int i = 0; i < numStripes; i++) {
            Rectangle stripe = new Rectangle(stripeWidth, stripeHeight);
            stripe.setX(x + i * (stripeWidth + spacing));
            stripe.setY(y);
            stripe.setFill(Color.WHITE);
//            stripe.setStroke(Color.web("#cccccc"));
//            stripe.setStrokeWidth(0.5);
            root.getChildren().add(stripe);
        }
    }

    private int currentLaneIndex = 0; // For round-robin serving

    private void startSimulationLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {

            // Use to generate random numbers of cars in each lane
            generateRandomTraffic();

            // Update display
            updateCount();
            updateWaitingCarsDisplay();

            // Round-robin serving: serve lanes in order A -> B -> C -> D -> A...
            String[] laneOrder = {"A", "B", "C", "D"};
            String next = laneOrder[currentLaneIndex];
            currentLaneIndex = (currentLaneIndex + 1) % 4;

            System.out.println("\n=== Cycle: Serving Lane " + next + " ===");
            System.out.println("Queue Sizes: A=" + laneA.incomingSize() + ", B=" + laneB.incomingSize() +
                             ", C=" + laneC.incomingSize() + ", D=" + laneD.incomingSize());

            if (next != null) {

                //Debug Output :
                System.out.println("Serving Lane : " + next);
                // Color are all in Red
                setLightColor(lightA, Color.RED);
                setLightColor(lightB, Color.RED);
                setLightColor(lightC, Color.RED);
                setLightColor(lightD, Color.RED);


                Circle currentLight = null;
                Lane currentLane = null;

                String trimmedNext = next.trim() ;
                System.out.println("DEBUG: Scheduler returned: '" + next + "' -> trimmed: '" + trimmedNext + "'");

                if(trimmedNext.equals("A")) {
                    currentLight = lightA;
                    currentLane = laneA;
                    System.out.println("Matched Lane A");
                }else if(trimmedNext.equals("B")) {
                    currentLight = lightB;
                    currentLane = laneB;
                    System.out.println("Matched Lane B");
                }else if(trimmedNext.equals("C")) {
                    currentLight = lightC;
                    currentLane = laneC;
                    System.out.println("Matched Lane C");
                }else if(trimmedNext.equals("D")) {
                    currentLight = lightD;
                    currentLane = laneD;
                    System.out.println("Matched Lane D");
                }else{
                    System.out.println("ERROR: No lane matched! trimmedNext='" + trimmedNext + "'");
                }

                final String finalLaneName = trimmedNext ;
                final Lane finalcurrentLane = currentLane ;

                if (currentLight  != null && currentLane != null) {
                    setLightColor(currentLight, Color.GREEN);

                    final Circle finalLight = currentLight ;

                    // Log current queue sizes before serving
                    System.out.println("Before serving - Lane " + finalLaneName + " incoming queue size: " + finalcurrentLane.incomingSize());

                    // Serve up to 3 cars per green light (2-3 second cycle)
                    int v = Math.min(finalcurrentLane.incomingSize(), 3);
                    System.out.println("Will serve " + v + " cars from lane " + finalLaneName);

                    // Serve v cars from the selected lane with staggered animation
                    for (int i = 0; i < v; i++) {
                        String served = finalcurrentLane.dequeueFromIncoming();
                        if (served != null) {
                            System.out.println("Car Served: " + served + " (Queue: " + finalcurrentLane.incomingSize() + ")");

                            switch (finalLaneName){
                                case "A" : passedCountA++ ; break ;
                                case "B" : passedCountB++ ; break ;
                                case "C" : passedCountC++ ; break ;
                                case "D" : passedCountD++ ; break ;
                            }

                            // Stagger car animations - 400ms between each
                            final int delay = i * 400;
                            Timeline carTimeline = new Timeline(new KeyFrame(Duration.millis(delay), e2 -> {
                                createAndAnimateCar(finalLaneName);
                            }));
                            carTimeline.play();
                        }
                    }

                    // Green light for 1.5 seconds, then yellow briefly
                    Timeline t = new Timeline(new KeyFrame(Duration.millis(1500), e2 -> {
                        setLightColor(finalLight, Color.YELLOW);
                        new Timeline(new KeyFrame(Duration.millis(300), e3 -> {
                            setLightColor(finalLight, Color.RED);
                        })).play();
                    }));
                    t.play();

                }
            }

            updateCount();

        }));
        updateCount();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private String getLaneName(Lane lane) {
        if (lane == laneA) return "A";
        if (lane == laneB) return "B";
        if (lane == laneC) return "C";
        if (lane == laneD) return "D";
        return "";
    }

    public void setLightColor(Circle light, Color color) {
        if (light == null) return;
        Platform.runLater(() -> light.setFill(color));
    }



    private void updateCount() {
        Platform.runLater(() -> {
            countA.setText("Queue : " + laneA.incomingSize() + " | Passed : " + passedCountA );
            countB.setText("Queue : " + laneB.incomingSize() + " | Passed : " + passedCountB);
            countC.setText("Queue : " + laneC.incomingSize() + " | Passed : " + passedCountC);
            countD.setText("Queue : " + laneD.incomingSize() + " | Passed : " + passedCountD);
        });
    }

    // Visualize waiting cars at each traffic light
    private void updateWaitingCarsDisplay() {
        Platform.runLater(() -> {
            // Clear existing waiting car visualizations
            simulationPane.getChildren().removeIf(node -> node.getUserData() != null && node.getUserData().equals("waiting"));

            // Show up to 5 waiting cars for each lane
            drawWaitingCars("A", laneA.incomingSize(), centerX - 15, centerY - JUNCTION_SIZE / 2.0 - 80, 0, -25);
            drawWaitingCars("B", laneB.incomingSize(), centerX + 15, centerY + JUNCTION_SIZE / 2.0 + 80, 0, 25);
            drawWaitingCars("C", laneC.incomingSize(), centerX + JUNCTION_SIZE / 2.0 + 80, centerY - 15, 25, 0);
            drawWaitingCars("D", laneD.incomingSize(), centerX - JUNCTION_SIZE / 2.0 - 80, centerY + 15, -25, 0);
        });
    }

    private void drawWaitingCars(String lane, int queueSize, double startX, double startY, double offsetX, double offsetY) {
        int carsToShow = Math.min(queueSize, 5);
        for (int i = 0; i < carsToShow; i++) {
            Rectangle waitingCar = new Rectangle(15, 25, Color.GRAY);
            waitingCar.setArcHeight(5);
            waitingCar.setArcWidth(5);
            waitingCar.setStroke(Color.DARKGRAY);
            waitingCar.setStrokeWidth(1);
            waitingCar.setX(startX + offsetX * i);
            waitingCar.setY(startY + offsetY * i);
            waitingCar.setUserData("waiting");
            simulationPane.getChildren().add(waitingCar);
        }
    }

    private void generateRandomTraffic() {
        // Reduced traffic generation rates to prevent overwhelming the system
        if (random_generator.nextDouble() < 0.4) laneA.enqueueToLane("A-" + System.currentTimeMillis() % 1000);
        if (random_generator.nextDouble() < 0.35) laneB.enqueueToLane("B-" + System.currentTimeMillis() % 1000);
        if (random_generator.nextDouble() < 0.5) laneC.enqueueToLane("C-" + System.currentTimeMillis() % 1000);
        if (random_generator.nextDouble() < 0.3) laneD.enqueueToLane("D-" + System.currentTimeMillis() % 1000);

        // AL2 priority check (enqueue some priority vehicles)
        if (random_generator.nextDouble() < 0.3) laneA.enqueueToLane(2, " AL2- " + System.currentTimeMillis() % 1000);
    }

        private void createAndAnimateCar(String laneName) {
            System.out.println("Creating car animation for lane: " + laneName);
            Rectangle car = new Rectangle(20, 35, getRandomCarColor());
            car.setArcHeight(8);
            car.setArcWidth(8);
            car.setStroke(Color.BLACK);
            car.setStrokeWidth(1);

            //Randomly choose the direction : Implemented the Path Transition.
            // ENUM : 0 = straight , 1 = left , 2= right

            int direction = random_generator.nextInt(3);

            double roadThickness = LANE_WIDTH * 3;
            double laneOffset = LANE_WIDTH / 2.0; // Middle of the Road

            Path path = new Path();
            MoveTo start = null ;

            switch(laneName){
                case "A"  :
                    start = new MoveTo(centerX - laneOffset, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH);
                    path.getElements().add(start);

                    path.getElements().add(new LineTo(centerX - laneOffset, centerY - JUNCTION_SIZE / 2.0));

                    //Straight To Lane B
                    if(direction == 0){
                        path.getElements().add(new LineTo(centerX - laneOffset , centerY - JUNCTION_SIZE / 2.0 + ROAD_LENGTH));
                    }else if(direction == 1){
                        path.getElements().add(new QuadCurveTo( centerX - laneOffset, centerY , centerX, centerY + laneOffset));

                        path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY + laneOffset));

                    } else { // Right to D

                        path.getElements().add(new QuadCurveTo(

                                centerX - laneOffset, centerY,

                                centerX - ROAD_LENGTH, centerY - laneOffset));

                        path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY - laneOffset));

                    }

                    break;

                case "B" :
                    start = new MoveTo(centerX + laneOffset, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH);

                    path.getElements().add(start);

                    path.getElements().add(new LineTo(centerX + laneOffset, centerY + JUNCTION_SIZE / 2.0));



                    if (direction == 0) { // Straight to A

                        path.getElements().add(new LineTo(centerX + laneOffset, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));

                    } else if (direction == 1) { // Left to D

                        path.getElements().add(new QuadCurveTo(

                                centerX + laneOffset, centerY,

                                centerX, centerY - laneOffset));

                        path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY - laneOffset));

                    } else { // Right to C

                        path.getElements().add(new QuadCurveTo(

                                centerX + laneOffset, centerY,

                                centerX + ROAD_LENGTH, centerY + laneOffset));

                        path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY + laneOffset));

                    }

                    break;



                case "C": // From right going left

                    start = new MoveTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY - laneOffset);

                    path.getElements().add(start);

                    path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0, centerY - laneOffset));



                    if (direction == 0) { // Straight to D

                        path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY - laneOffset));

                    } else if (direction == 1) { // Left to B

                        path.getElements().add(new QuadCurveTo(

                                centerX, centerY - laneOffset,

                                centerX + laneOffset, centerY));

                        path.getElements().add(new LineTo(centerX + laneOffset, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));

                    } else { // Right to A

                        path.getElements().add(new QuadCurveTo(

                                centerX, centerY - laneOffset,

                                centerX - laneOffset, centerY - ROAD_LENGTH));

                        path.getElements().add(new LineTo(centerX - laneOffset, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));

                    }

                    break;



                case "D": // From left going right

                    start = new MoveTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY - laneOffset);

                    path.getElements().add(start);

                    path.getElements().add(new LineTo(centerX - JUNCTION_SIZE / 2.0, centerY - laneOffset));



                    if (direction == 0) { // Straight to C

                        path.getElements().add(new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH, centerY - laneOffset));

                    } else if (direction == 1) { // Left to A

                        path.getElements().add(new QuadCurveTo(

                                centerX, centerY - laneOffset,

                                centerX - laneOffset, centerY));

                        path.getElements().add(new LineTo(centerX - laneOffset, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH));

                    } else { // Right to B

                        path.getElements().add(new QuadCurveTo(

                                centerX, centerY - laneOffset,

                                centerX + laneOffset, centerY));

                        path.getElements().add(new LineTo(centerX + laneOffset, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH));

                    }

                    break;

            }
            simulationPane.getChildren().add(car);
            PathTransition pt = new PathTransition(Duration.millis(2000), path, car);
            pt.setOnFinished(
                    event -> simulationPane.getChildren().remove(car));
            pt.play();
        }

        private Color getRandomCarColor(){
                Color[] colors = {
                Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.PURPLE, Color.YELLOW, Color.CYAN
                };
            return colors[random_generator.nextInt(colors.length)] ;

        }

}
