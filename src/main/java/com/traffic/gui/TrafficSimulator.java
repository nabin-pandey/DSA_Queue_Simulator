//Second wala

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
import java.util.concurrent.atomic.AtomicReference;


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

    //Generate random numbers
    private final Random random_generator = new Random();

    // root pane reference for helper methods
    private Pane root;

    // center values for convenience
    private double centerX;
    private double centerY;

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
        Rectangle roadA = new Rectangle(roadThickness, ROAD_LENGTH + JUNCTION_SIZE / 2.0);
        roadA.setX(centerX - roadThickness / 2.0);
        roadA.setY(centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH);
        roadA.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadA);

        // Road B (bottom -> up)
        Rectangle roadB = new Rectangle(roadThickness, ROAD_LENGTH + JUNCTION_SIZE / 2.0);
        roadB.setX(centerX - roadThickness / 2.0);
        roadB.setY(centerY + JUNCTION_SIZE / 2.0);
        roadB.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadB);

        // Road C (right -> left)
        Rectangle roadC = new Rectangle(ROAD_LENGTH + JUNCTION_SIZE / 2.0, roadThickness);
        roadC.setX(centerX + JUNCTION_SIZE / 2.0);
        roadC.setY(centerY - roadThickness / 2.0);
        roadC.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadC);

        // Road D (left -> right)
        Rectangle roadD = new Rectangle(ROAD_LENGTH + JUNCTION_SIZE / 2.0, roadThickness);
        roadD.setX(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH);
        roadD.setY(centerY - roadThickness / 2.0);
        roadD.setFill(Color.web("#3f3f3f"));
        root.getChildren().add(roadD);

        // lane markings for each road (draw simple dashed white separators)
        drawLaneMarkingsForVerticalRoad(centerX - roadThickness / 2.0, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH,
                roadThickness, ROAD_LENGTH + JUNCTION_SIZE / 2.0);
        drawLaneMarkingsForVerticalRoad(centerX - roadThickness / 2.0, centerY + JUNCTION_SIZE / 2.0,
                roadThickness, ROAD_LENGTH + JUNCTION_SIZE / 2.0);
        drawLaneMarkingsForHorizontalRoad(centerX + JUNCTION_SIZE / 2.0, centerY - roadThickness / 2.0,
                ROAD_LENGTH + JUNCTION_SIZE / 2.0, roadThickness);
        drawLaneMarkingsForHorizontalRoad(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH, centerY - roadThickness / 2.0,
                ROAD_LENGTH + JUNCTION_SIZE / 2.0, roadThickness);

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
        countA = new Text(centerX - 40, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 20, "Cars : 0");
        countB = new Text(centerX - 40, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 6, "Cars : 0");
        countC = new Text(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 60, centerY - 10, "Cars : 0");
        countD = new Text(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 10, centerY - 10, "Cars : 0");
        root.getChildren().addAll(countA, countB, countC, countD);
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
        // add small crosswalks at junction edge
        drawCrosswalkVertical(x, y + height - (JUNCTION_SIZE / 2.0));
        drawCrosswalkVertical(x, y + (JUNCTION_SIZE / 2.0) - 10); // top of junction for bottom road
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
        drawCrosswalkHorizontal(x + width - (JUNCTION_SIZE / 2.0), y);
        drawCrosswalkHorizontal(x + (JUNCTION_SIZE / 2.0) - 10, y);
    }

    private void drawCrosswalkVertical(double x, double startY) {
        // draw a few white rectangles
        double step = 8;
        for (int i = 0; i < 8; i++) {
            Rectangle r = new Rectangle(10, 4);
            r.setX(x - (LANE_WIDTH * 1.5) + 8);
            r.setY(startY + i * (step + 2));
            r.setFill(Color.WHITE);
            root.getChildren().add(r);
        }
    }

    private void drawCrosswalkHorizontal(double startX, double y) {
        double step = 8;
        for (int i = 0; i < 8; i++) {
            Rectangle r = new Rectangle(4, 10);
            r.setX(startX - i * (step + 2));
            r.setY(y - (LANE_WIDTH * 1.5) + 8);
            r.setFill(Color.WHITE);
            root.getChildren().add(r);
        }
    }

    private void startSimulationLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {

            // Use to generate random numbers of cars in each lane
            generateRandomTraffic();

            trafficScheduler.CheckandUpdatePriority(laneEntryA, laneA.totalSIze());
            trafficScheduler.CheckandUpdatePriority(laneEntryB, laneB.totalSIze());
            trafficScheduler.CheckandUpdatePriority(laneEntryC, laneC.totalSIze());
            trafficScheduler.CheckandUpdatePriority(laneEntryD, laneD.totalSIze());

            String next = trafficScheduler.getNextLaneToServe() != null ? trafficScheduler.serverAndRotateLane() : null;

            if (next != null) {
                // Color are all in Red
                setLightColor(lightA, Color.RED);
                setLightColor(lightB, Color.RED);
                setLightColor(lightC, Color.RED);
                setLightColor(lightD, Color.RED);

                AtomicReference<Circle> currentLight = new AtomicReference<>();
                Lane currentLane = null;

                switch (next) {
                    case "A":
                        currentLight.set(lightA);
                        currentLane = laneA;
                        break;
                    case " B":
                        currentLight.set(lightB);
                        currentLane = laneB;
                        break;
                    case "C":
                        currentLight.set(lightC);
                        currentLane = laneC;
                        break;
                    case " D":
                        currentLight.set(lightD);
                        currentLane = laneD;
                        break;
                    default:
                        break;
                }

                if (currentLight.get() != null && currentLane != null) {
                    setLightColor(currentLight.get(), Color.GREEN);

                    int sumIncoming = laneA.incomingSize() + laneB.incomingSize() + laneC.incomingSize() + laneD.incomingSize();
                    int n = 4;
                    int v = (int) Math.round((double) sumIncoming / n);
                    if (v < 1) v = 1;

                    if ("A".equals(next) && laneA.prioritySize() > 10) {
                        v = Math.max(v, laneA.prioritySize());
                    }

                    // Serve v cars from the selected lane and animate each served car
                    for (int i = 0; i < v; i++) {
                        String served = currentLane.dequeueFromIncoming();
                        if (served != null) {
                            System.out.println("Car Served ; " + served);
                            // Animate served car on JavaFX thread
                            String laneName = getLaneName(currentLane);
                            Platform.runLater(() -> createAndAnimateCar(laneName));
                        }
                    }

                    Timeline t = new Timeline(new KeyFrame((Duration.millis(700)), e2 -> {
                        setLightColor(currentLight.get(), Color.YELLOW);
                        new Timeline(new KeyFrame(Duration.millis(1000), e3 -> {
                            setLightColor(currentLight.get(), Color.RED);
                        })).play();
                    }));
                    t.play();

                }
            }

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

    // Create a circle "car" at the lane start and animate along a path through the junction.
    // This method uses the lane name and lane index to decide a path (straight/left/right).
    private void createAndAnimateCar(String laneName) {
        // determine lane spawn positions (three lanes per road)
        double roadThickness = LANE_WIDTH * 3;
        double laneThird = roadThickness / 3.0;

        Circle car = new Circle(8, Color.web("#ff3333"));
        car.setStroke(Color.web("#960000"));
        car.setStrokeWidth(1.0);

        Path path = new Path();

        // We'll choose the path based on laneName; within each road we position by laneThird:
        // For A: AL1 (left), AL2 (middle priority), AL3 (right) -> travel downward (to B)
        // For B: BL1 (closest center), BL2 (middle), BL3 (outer) -> travel upward (to A)
        // For C: CL1/CL2/CL3 -> travel left (to D)
        // For D: DL1/DL2/DL3 -> travel right (to C)
        // For simplicity we map spawn lane by alternating which lane index to use (rotate)
        int laneIndex = (int) (Math.abs(System.currentTimeMillis()) % 3); // 0,1,2

        if ("A".equals(laneName)) {
            // spawn at top in corresponding lane
            double spawnX = centerX - roadThickness / 2.0 + laneThird * (laneIndex + 0.5);
            double spawnY = centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 10;
            car.setCenterX(spawnX);
            car.setCenterY(spawnY);
            root.getChildren().add(car);

            // Decide movement: AL2 (index==1) is priority; we'll move straight,
            // AL1 and AL3 sometimes turn left/right for variety (deterministic)
            if (laneIndex == 1) {
                // straight to bottom (through junction)
                MoveTo m = new MoveTo(spawnX, spawnY);
                LineTo l1 = new LineTo(spawnX, centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 10);
                path.getElements().addAll(m, l1);
            } else if (laneIndex == 0) {
                // left-turn (to D)
                MoveTo m = new MoveTo(spawnX, spawnY);
                // curve into left road
                CubicCurveTo c = new CubicCurveTo(spawnX, centerY - 40, centerX - 120, centerY + 40, centerX - ROAD_LENGTH - JUNCTION_SIZE / 2.0 + 40, centerY);
                path.getElements().addAll(m, c);
            } else {
                // right-turn (to C)
                MoveTo m = new MoveTo(spawnX, spawnY);
                CubicCurveTo c = new CubicCurveTo(spawnX, centerY - 40, centerX + 120, centerY + 40, centerX + ROAD_LENGTH + JUNCTION_SIZE / 2.0 - 40, centerY);
                path.getElements().addAll(m, c);
            }
        } else if ("B".equals(laneName)) {
            double spawnX = centerX - roadThickness / 2.0 + laneThird * (laneIndex + 0.5);
            double spawnY = centerY + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 10;
            car.setCenterX(spawnX);
            car.setCenterY(spawnY);
            root.getChildren().add(car);

            if (laneIndex == 1) {
                MoveTo m = new MoveTo(spawnX, spawnY);
                LineTo l1 = new LineTo(spawnX, centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 10);
                path.getElements().addAll(m, l1);
            } else if (laneIndex == 0) {
                // left turn (to C)
                MoveTo m = new MoveTo(spawnX, spawnY);
                CubicCurveTo c = new CubicCurveTo(spawnX, centerY + 40, centerX + 120, centerY - 40, centerX + ROAD_LENGTH + JUNCTION_SIZE / 2.0 - 40, centerY);
                path.getElements().addAll(m, c);
            } else {
                // right turn (to D)
                MoveTo m = new MoveTo(spawnX, spawnY);
                CubicCurveTo c = new CubicCurveTo(spawnX, centerY + 40, centerX - 120, centerY - 40, centerX - ROAD_LENGTH - JUNCTION_SIZE / 2.0 + 40, centerY);
                path.getElements().addAll(m, c);
            }
        } else if ("C".equals(laneName)) {
            double spawnX = centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 10;
            double spawnY = centerY - roadThickness / 2.0 + laneThird * (laneIndex + 0.5);
            car.setCenterX(spawnX);
            car.setCenterY(spawnY);
            root.getChildren().add(car);

            if (laneIndex == 1) {
                MoveTo m = new MoveTo(spawnX, spawnY);
                LineTo l1 = new LineTo(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 10, spawnY);
                path.getElements().addAll(m, l1);
            } else if (laneIndex == 0) {
                // left turn (to A)
                MoveTo m = new MoveTo(spawnX, spawnY);
                CubicCurveTo c = new CubicCurveTo(centerX + 40, spawnY, centerX - 40, centerY - 120, centerX, centerY - ROAD_LENGTH - JUNCTION_SIZE / 2.0 + 40);
                path.getElements().addAll(m, c);
            } else {
                // right turn (to B)
                MoveTo m = new MoveTo(spawnX, spawnY);
                CubicCurveTo c = new CubicCurveTo(centerX + 40, spawnY, centerX - 40, centerY + 120, centerX, centerY + ROAD_LENGTH + JUNCTION_SIZE / 2.0 - 40);
                path.getElements().addAll(m, c);
            }
        } else if ("D".equals(laneName)) {
            double spawnX = centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH + 10;
            double spawnY = centerY - roadThickness / 2.0 + laneThird * (laneIndex + 0.5);
            car.setCenterX(spawnX);
            car.setCenterY(spawnY);
            root.getChildren().add(car);

            if (laneIndex == 1) {
                MoveTo m = new MoveTo(spawnX, spawnY);
                LineTo l1 = new LineTo(centerX + JUNCTION_SIZE / 2.0 + ROAD_LENGTH - 10, spawnY);
                path.getElements().addAll(m, l1);
            } else if (laneIndex == 0) {
                // left to B
                MoveTo m = new MoveTo(spawnX, spawnY);
                CubicCurveTo c = new CubicCurveTo(centerX - 40, spawnY, centerX + 40, centerY + 120, centerX, centerY + ROAD_LENGTH + JUNCTION_SIZE / 2.0 - 40);
                path.getElements().addAll(m, c);
            } else {
                // right to A
                MoveTo m = new MoveTo(spawnX, spawnY);
                CubicCurveTo c = new CubicCurveTo(centerX - 40, spawnY, centerX + 40, centerY - 120, centerX, centerY - ROAD_LENGTH - JUNCTION_SIZE / 2.0 + 40);
                path.getElements().addAll(m, c);
            }
        } else {
            // unknown lane - remove
            return;
        }

        // Run the PathTransition
        PathTransition pt = new PathTransition();
        pt.setPath(path);
        pt.setNode(car);

        // base duration depends on path length; choose visually pleasing speed
        pt.setDuration(Duration.seconds(3 + random_generator.nextDouble() * 2)); // 3-5 seconds
        pt.setOrientation(PathTransition.OrientationType.NONE);
        pt.setInterpolator(Interpolator.LINEAR);

        pt.setOnFinished(evt -> {
            // remove car from display when finished
            root.getChildren().remove(car);
        });

        pt.play();
    }

    private void updateCount() {
        Platform.runLater(() -> {
            countA.setText("Cars : " + laneA.incomingSize() + " AL-2:" + laneA.prioritySize());
            countB.setText("Cars : " + laneB.incomingSize());
            countC.setText("Cars : " + laneC.incomingSize());
            countD.setText("Cars : " + laneD.incomingSize());
        });
    }

    private void generateRandomTraffic() {
        if (random_generator.nextDouble() < 0.35) laneA.enqueueToLane("A - " + System.currentTimeMillis() % 1000);

        if (random_generator.nextDouble() < 0.35) laneB.enqueueToLane("B - " + System.currentTimeMillis() % 1000);
        if (random_generator.nextDouble() < 0.35) laneC.enqueueToLane("C - " + System.currentTimeMillis() % 1000);
        if (random_generator.nextDouble() < 0.35) laneD.enqueueToLane("D - " + System.currentTimeMillis() % 1000);

        // AL2 priority check (enqueue some priority vehicles)
        if (random_generator.nextDouble() < 0.12) laneA.enqueueToLane(2, " AL-2 : " + System.currentTimeMillis() % 1000);
    }
}
