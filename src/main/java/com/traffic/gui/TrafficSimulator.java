package com.traffic.gui;


import com.traffic.core.Lane;
import com.traffic.core.LaneEntry;
import com.traffic.core.TrafficScheduler;
import com.traffic.core.VehicleQueue;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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
    private static final int JUNCTION_SIZE = LANE_WIDTH * 4 ;
    private static final int  LIGHT_SIZE = 15;
    private static final int ROAD_LENGTH = 300 ;

    //Traffic Light Circle n(Red is shown initially)
    private Circle lightA;
    private Circle lightB;
    private Circle lightC;
    private Circle lightD;


    private LaneEntry laneEntryA;
    private LaneEntry laneEntryB;
    private LaneEntry laneEntryC;
    private LaneEntry laneEntryD;

    private TrafficScheduler trafficScheduler;

    //Displaying counts
    private Text counta;
    private Text countb;
    private Text countc;
    private Text countd;

    //Generate  random numbers
    private final Random  random_generator = new Random();



    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        laneA = new Lane("A");
        laneB = new Lane("B") ;
        laneC = new Lane("C") ;
        laneD = new Lane("D") ;


        //Lane Entries. Initial Count : 0
        laneEntryA = new LaneEntry("A" , 0);
        laneEntryB = new LaneEntry("B" , 0);
        laneEntryC = new LaneEntry("C" , 0);
        laneEntryD = new LaneEntry("D" , 0);


        List<LaneEntry> entries = new ArrayList<>() ;
        entries.add(laneEntryA);
        entries.add(laneEntryB);
        entries.add(laneEntryC);
        entries.add(laneEntryD);

        scheduler = new TrafficScheduler(entries) ;

        Pane root = new Pane();
        root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        double Cnetre

    }
}
