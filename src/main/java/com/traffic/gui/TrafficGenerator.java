
package com.traffic.gui;

import com.traffic.core.Lane;
import javafx.animation.Interpolator;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Duration;

import java.util.*;

public class TrafficGenerator {

    private static final int LANE_WIDTH = 50;
    private static final int JUNCTION_SIZE = LANE_WIDTH * 4;
    private static final int ROAD_LENGTH = 300;

    private final Lane laneA, laneB, laneC, laneD;
    private final Pane simulationPane;
    private final double centerX, centerY;
    private final Random random = new Random();

    // Waiting queues for all roads/laneNumbers
    private final Map<String, List<Rectangle>> waiting = new HashMap<>();


    private final Map<String, double[]> laneOffsetsByRoad = new HashMap<>();

    public TrafficGenerator(Lane laneA, Lane laneB, Lane laneC, Lane laneD,
                            Pane simulationPane, double centerX, double centerY) {

        this.laneA = laneA;
        this.laneB = laneB;
        this.laneC = laneC;
        this.laneD = laneD;
        this.simulationPane = simulationPane;
        this.centerX = centerX;
        this.centerY = centerY;

        // init waiting lists
        for (String r : Arrays.asList("A", "B", "C", "D")) {
            for (int l = 1; l <= 3; l++) {
                waiting.put(r + l, new ArrayList<>());
            }
        }

        //  lane placement
        laneOffsetsByRoad.put("A", new double[]{0, -LANE_WIDTH, 0, +LANE_WIDTH}); // AL1 left, AL2 mid, AL3 right
        laneOffsetsByRoad.put("B", new double[]{0, +LANE_WIDTH, 0, -LANE_WIDTH}); // BL1 right, BL2 mid, BL3 left
        laneOffsetsByRoad.put("C", new double[]{0, -LANE_WIDTH, 0, +LANE_WIDTH}); // CL1 top, CL2 mid, CL3 bottom
        laneOffsetsByRoad.put("D", new double[]{0, +LANE_WIDTH, 0, -LANE_WIDTH}); // DL1 bottom, DL2 mid, DL3 top
    }

    // ---------------- Add waiting car ----------------
    public void addWaitingCar(String roadId, int laneNumber, String vehicleId) {

        //lane1 is accept-only; never draw it as a source queue
        if (laneNumber == 1) return;

        List<Rectangle> list = waiting.get(roadId + laneNumber);
        if (list == null) return;

        Rectangle car = new Rectangle(20, 35);
        car.setArcWidth(5);
        car.setArcHeight(5);
        car.setFill(laneNumber == 2 ? Color.GOLD : getRandomCarColor());
        car.setStroke(Color.BLACK);
        car.setUserData(vehicleId);

        int pos = list.size();
        double gap = 45;
        double off = laneOffset(roadId, laneNumber);

        double cx = 0, cy = 0, rot = 0;
        switch (roadId) {
            case "A":
                // A is top, cars queue upward
                cx = centerX + off;
                cy = (centerY - (JUNCTION_SIZE / 2.0) - 40) - (pos * gap);
                rot = 180;
                break;

            case "B":
                // B is bottom, cars queue downward
                cx = centerX + off;
                cy = (centerY + (JUNCTION_SIZE / 2.0) + 40) + (pos * gap);
                rot = 0;
                break;

            case "C":
                // C is right, cars queue to the right
                cx = (centerX + (JUNCTION_SIZE / 2.0) + 40) + (pos * gap);
                cy = centerY + off;
                rot = -90;
                break;

            case "D":
                // D is left, cars queue to the left
                cx = (centerX - (JUNCTION_SIZE / 2.0) - 40) - (pos * gap);
                cy = centerY + off;
                rot = 90;
                break;

            default:
                return;
        }

        car.setX(cx - 10);
        car.setY(cy - 17.5);
        car.setRotate(rot);

        list.add(car);
        Platform.runLater(() -> simulationPane.getChildren().add(car));
    }

    //Release waiting cars
    public void releaseWaitingCars(String roadId, int laneNumber, int count) {

        // accept lane never serves as a source
        if (laneNumber == 1) return;

        List<Rectangle> list = waiting.get(roadId + laneNumber);

        // correct null/empty check
        if (list == null || list.isEmpty()) return;

        Lane laneObj = getLaneByRoad(roadId);
        int toRelease = Math.min(count, list.size());

        for (int i = 0; i < toRelease; i++) {
            Rectangle car = list.remove(0);

            // dequeue logical queue too (keeps sizes/priority correct)
            dequeueFromLane(laneObj, laneNumber);

            createAndAnimateCar(car, roadId);
        }

        reposition(roadId, laneNumber);
    }

    // Reposition of the stopped traffic
    private void reposition(String roadId, int laneNumber) {

        List<Rectangle> list = waiting.get(roadId + laneNumber);

        // correct null/empty check
        if (list == null || list.isEmpty()) return;

        double gap = 45;
        double off = laneOffset(roadId, laneNumber);

        for (int i = 0; i < list.size(); i++) {
            Rectangle car = list.get(i);
            int idx = i;

            Platform.runLater(() -> {
                double cx = 0, cy = 0;
                switch (roadId) {
                    case "A":
                        cx = centerX + off;
                        cy = (centerY - (JUNCTION_SIZE / 2.0) - 40) - (idx * gap);
                        break;
                    case "B":
                        cx = centerX + off;
                        cy = (centerY + (JUNCTION_SIZE / 2.0) + 40) + (idx * gap);
                        break;
                    case "C":
                        cx = (centerX + (JUNCTION_SIZE / 2.0) + 40) + (idx * gap);
                        cy = centerY + off;
                        break;
                    case "D":
                        cx = (centerX - (JUNCTION_SIZE / 2.0) - 40) - (idx * gap);
                        cy = centerY + off;
                        break;
                    default:
                        return;
                }
                car.setTranslateX(0);
                car.setTranslateY(0);
                car.setX(cx - 10);
                car.setY(cy - 17.5);
            });
        }
    }

    //Car will animation as it recive
    private void createAndAnimateCar(Rectangle car, String srcRoad) {
        String vid = (car.getUserData() == null) ? "" : car.getUserData().toString();

        // Generator writes: "B2->A1-123"
        String dstRoad = parseDstRoad(vid);
        if (dstRoad == null) dstRoad = randomIncomingRoad();

        // Destination must always be incoming lane 1 (accept lane)
        double[] end = destinationPoint(dstRoad, 1);

        Path path = new Path();
        double startX = car.getX() + 10;
        double startY = car.getY() + 17.5;

        path.getElements().add(new MoveTo(startX, startY));

        boolean straight = isStraight(srcRoad, dstRoad);
        if (straight) {
            path.getElements().add(new LineTo(end[0], end[1]));
        } else {
            path.getElements().add(new QuadCurveTo(centerX, centerY, end[0], end[1]));
        }

        PathTransition pt = new PathTransition(Duration.seconds(3.5), path, car);
        pt.setInterpolator(Interpolator.LINEAR);
        pt.setOnFinished(e -> Platform.runLater(() -> simulationPane.getChildren().remove(car)));
        pt.play();
    }

    private String parseDstRoad(String id) {
        try {
            int arrow = id.indexOf("->");
            if (arrow < 0) return null;

            String right = id.substring(arrow + 2).trim(); // "A1-123"
            int dash = right.indexOf("-");
            if (dash >= 0) right = right.substring(0, dash);

            if (right.length() < 2) return null;
            return right.substring(0, 1);
        } catch (Exception e) {
            return null;
        }
    }

    // Designated point to move the car in that lane.
    private double[] destinationPoint(String road, int lane) {
        double off = laneOffset(road, lane);
        switch (road) {
            case "A":
                return new double[]{centerX + off, centerY - (JUNCTION_SIZE / 2.0) - ROAD_LENGTH};
            case "B":
                return new double[]{centerX + off, centerY + (JUNCTION_SIZE / 2.0) + ROAD_LENGTH};
            case "C":
                return new double[]{centerX + (JUNCTION_SIZE / 2.0) + ROAD_LENGTH, centerY + off};
            case "D":
                return new double[]{centerX - (JUNCTION_SIZE / 2.0) - ROAD_LENGTH, centerY + off};
            default:
                return new double[]{centerX, centerY};
        }
    }

    private boolean isStraight(String src, String dst) {
        return (src.equals("A") && dst.equals("B"))
                || (src.equals("B") && dst.equals("A"))
                || (src.equals("C") && dst.equals("D"))
                || (src.equals("D") && dst.equals("C"));
    }

    private String randomIncomingRoad() {
        String[] r = {"A", "B", "C", "D"};
        return r[random.nextInt(r.length)];
    }

    private double laneOffset(String roadId, int laneNumber) {
        double[] offsets = laneOffsetsByRoad.get(roadId);
        if (offsets == null) offsets = new double[]{0, +LANE_WIDTH, 0, -LANE_WIDTH};
        if (laneNumber < 1 || laneNumber > 3) return 0;
        return offsets[laneNumber];
    }


    private Lane getLaneByRoad(String r) {
        switch (r) {
            case "A":
                return laneA;
            case "B":
                return laneB;
            case "C":
                return laneC;
            case "D":
                return laneD;
            default:
                return laneA;
        }
    }

    //Fixed Dequeue of car stopped in red light
    private void dequeueFromLane(Lane lane, int laneNumber) {
        if (lane == null) return;
        switch (laneNumber) {
            case 1:
                lane.dequeueFromIncoming();
                break;
            case 2:
                lane.dequeueFromPriority();
                break;
            case 3:
                lane.dequeueFromLeftTurn();
                break;
            default:
                break;
        }
    }

    private Color getRandomCarColor() {
        Color[] colors = {Color.BLUE, Color.RED, Color.GREEN, Color.ORANGE, Color.PURPLE, Color.CYAN, Color.PINK};
        return colors[random.nextInt(colors.length)];
    }
}
