package com.traffic.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);   // This starts JavaFX
    }

    @Override
    public void start(Stage stage) {
        Label label = new Label("JavaFX is working!");

        Scene scene = new Scene(label, 300, 150);

        stage.setTitle("Test Window");
        stage.setScene(scene);
        stage.show();   // Important: show the window
    }
}
