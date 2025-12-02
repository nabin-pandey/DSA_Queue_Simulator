package com.traffic.gui;

import javafx.application.Application;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;


public class TrafficSimulator extends Application {

    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 800;
    private static final int LANE_WIDTH = 50;
    private static final int JUNCTION_SIZE = LANE_WIDTH * 4 ;
    private static final int  LIGHT_SIZE = 15;
    private static final int ROAD_LENGTH = 300 ;

    //Traffic Light Circles(Red is shown initially)
    private Circle lightA;
    private Circle lightB;
    private Circle lightC;
    private Circle lightD;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

    }
}
