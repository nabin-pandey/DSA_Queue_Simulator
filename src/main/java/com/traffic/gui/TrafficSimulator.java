package com.traffic.gui;


import com.traffic.core.Lane;
import com.traffic.core.LaneEntry;
import com.traffic.core.TrafficScheduler;
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

//Debug
import java.util.concurrent.atomic.AtomicReference;

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


    //Lane Objects
    private Lane laneA;
    private Lane laneB;
    private Lane laneC ;
    private Lane laneD ;


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

        //Call Scheduler from TrafficScheduler
        trafficScheduler = new TrafficScheduler(entries) ;

        Pane root = new Pane();
        root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        double centerX = WINDOW_WIDTH / 2.0 ;
        double centerY = WINDOW_HEIGHT / 2.0 ;

        //Road and Junction
        Rectangle junction = new Rectangle(JUNCTION_SIZE, JUNCTION_SIZE);
        junction.setX(centerX - JUNCTION_SIZE / 2.0);
        junction.setY(centerY - JUNCTION_SIZE / 2.0 - ROAD_LENGTH);
        junction.setFill(Color.DARKGRAY);
        root.getChildren().add(junction) ;


        Rectangle roadA = new Rectangle(JUNCTION_SIZE, ROAD_LENGTH);
        roadA.setX(centerX - JUNCTION_SIZE / 2.0);
        roadA.setY(centerY - ROAD_LENGTH / 2.0);
        roadA.setFill(Color.GRAY);
        root.getChildren().add(roadA) ;

        Rectangle roadB = new Rectangle(JUNCTION_SIZE, ROAD_LENGTH);
        roadA.setX(centerX - JUNCTION_SIZE / 2.0);
        roadA.setY(centerY - ROAD_LENGTH / 2.0);
        roadA.setFill(Color.GRAY);
        root.getChildren().add(roadB) ;

        Rectangle roadC = new Rectangle(JUNCTION_SIZE, ROAD_LENGTH);
        roadA.setX(centerX - JUNCTION_SIZE / 2.0);
        roadA.setY(centerY - ROAD_LENGTH / 2.0);
        roadA.setFill(Color.GRAY);
        root.getChildren().add(roadC) ;

        Rectangle roadD = new Rectangle(JUNCTION_SIZE, ROAD_LENGTH);
        roadA.setX(centerX - JUNCTION_SIZE / 2.0 - ROAD_LENGTH);
        roadA.setY(centerY - ROAD_LENGTH / 2.0);
        roadA.setFill(Color.GRAY);
        root.getChildren().add(roadD) ;

        //Traffic Lights
        lightA = new Circle(centerX + JUNCTION_SIZE / 2.0 - LIGHT_SIZE, centerY - JUNCTION_SIZE / 2.0 - 5, LIGHT_SIZE, Color.RED);
        lightB = new Circle(centerX - JUNCTION_SIZE / 2.0 + LIGHT_SIZE, centerY + JUNCTION_SIZE / 2.0 + 5, LIGHT_SIZE, Color.RED);
        lightC = new Circle(centerX + JUNCTION_SIZE / 2.0 + 5, centerY + JUNCTION_SIZE / 2.0 - LIGHT_SIZE, LIGHT_SIZE, Color.RED);
        lightD = new Circle(centerX - JUNCTION_SIZE / 2.0 - 5, centerY - JUNCTION_SIZE / 2.0 + LIGHT_SIZE, LIGHT_SIZE, Color.RED);
        root.getChildren().addAll(lightA, lightB, lightC, lightD);

        //Labels
            Text LabelA = new Text(centerX - 12 , centerY - ROAD_LENGTH - LANE_WIDTH / 2.0 + 8 , "Road A");
            Text LabelB = new Text(centerX - 12 , centerY + ROAD_LENGTH + LANE_WIDTH, "Road B");
            Text LabelC = new Text(centerX + ROAD_LENGTH + LANE_WIDTH / 2.0 - 10 , centerY + 4, "Road C");
            Text LabelD = new Text(centerX - ROAD_LENGTH - LANE_WIDTH + 6 , centerY + 4, "Road D");
            root.getChildren().addAll(LabelA, LabelB, LabelC, LabelD);

        //CountA
        countA = new Text(centerX - LANE_WIDTH / 2.0 , centerY - JUNCTION_SIZE/ 2.0 - ROAD_LENGTH + 24 , "Cars: 0");
        countB = new Text(centerX - LANE_WIDTH / 2.0 , centerY+ JUNCTION_SIZE / 20.0 + ROAD_LENGTH - 6, "Cars : 0");
        countC = new Text(centerX + JUNCTION_SIZE/ 2.0 + ROAD_LENGTH - 60 , centerY + LANE_WIDTH /2.0 , "Cars : 0" );
        countD = new Text(centerX - JUNCTION_SIZE/ 2.0 - ROAD_LENGTH + 10 , centerY + LANE_WIDTH /2.0 , "Cars : 0" );
        root.getChildren().addAll(countA, countB, countC, countD);

        //Simple Button
        Button startButton = new Button("Start");
        startButton.setLayoutX(20);
        startButton.setLayoutY(WINDOW_HEIGHT -  60);
        root.getChildren().add(startButton);

        startButton.setOnAction(e ->{
            startButton.setDisable(true);
            startSimulationLoop() ;
        });


        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle("DSA Queue Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void startSimulationLoop(){
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {

            //Use to generate random numbers of cars in each lane
            generateRandomTraffic();

            trafficScheduler.CheckandUpdatePriority(laneEntryA, laneA.totalSIze());
            trafficScheduler.CheckandUpdatePriority(laneEntryB, laneB.totalSIze());
            trafficScheduler.CheckandUpdatePriority(laneEntryC, laneC.totalSIze());
            trafficScheduler.CheckandUpdatePriority(laneEntryD, laneD.totalSIze());

            String next = trafficScheduler.getNextLaneToServe() != null ? trafficScheduler.serverAndRotateLane() : null ;

            if(next != null){
                //Color are all in Red
                setLightColor(lightA , Color.RED);
                setLightColor(lightB , Color.RED);
                setLightColor(lightC , Color.RED);
                setLightColor(lightD , Color.RED);

                AtomicReference<Circle> currentLight = new AtomicReference<>();
                Lane currentLane = null ;

                switch (next){
                    case"A" : currentLight.set(lightA); currentLane = laneA ;
                    break ;
                    case " B" : currentLight.set(lightB); currentLane = laneB ;
                break;
                    case"C" : currentLight.set(lightC); currentLane = laneC ;
                    break ;

                    case " D": currentLight.set(lightD); currentLane = laneD ;
                    default : break ;

                }

                if(currentLight.get() != null && currentLane != null ){
                 setLightColor(currentLight.get(), Color.GREEN);

                 int sumIncoming = laneA.incomingSize() + laneB.incomingSize() + laneC.incomingSize() + laneD.incomingSize();
                 int n = 4 ;
                int v = (int) Math.round((double) sumIncoming / n) ;
                if(v  <1) v = 1;

                if("A".equals(next) && laneA.prioritySize() > 10 ){
                    v = Math.max( v , laneA.prioritySize());
                }
                 for (int i = 0 ; i < v ; i++){
                        String served = currentLane.dequeueFromIncoming();
                        if(served != null) System.out.println("Car Served ; " +served);

                 }

                 Timeline t = new Timeline(new KeyFrame((Duration.millis(700)) , e2 -> {
                     setLightColor(currentLight.get(), Color.YELLOW);
                     new Timeline(new KeyFrame(Duration.millis(1000) , e3 -> {
                         setLightColor(currentLight.get(), Color.RED);
                     })).play();
                 })) ;
                 t.play();



                }
           }

        }));
        //Function definition is remaining will do it later on
                updateCount();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void setLightColor(Circle light , Color color){
        if(light == null ) return ;
        Platform.runLater(() -> light.setFill(color));
    }

   private void generateRandomTraffic(){
       if(random_generator.nextDouble() < 0.35) laneA.enqueueToLane("A - "+ System.currentTimeMillis()%1000);

       if(random_generator.nextDouble() < 0.35) laneB.enqueueToLane("B - " +System.currentTimeMillis()%1000);
       if(random_generator.nextDouble() < 0.35) laneC.enqueueToLane("C - " +System.currentTimeMillis()%1000);
       if(random_generator.nextDouble() < 0.35) laneD.enqueueToLane("D - " +System.currentTimeMillis()%1000);

       //Yo chai AL2 ko priority check garney
       if(random_generator.nextDouble()  < 0.12) laneA.enqueueToLane(2, " AL-2 : " +System.currentTimeMillis()%1000);

   }

   //Incoming basis ma update huncha
   private void updateCount(){
        Platform.runLater(() ->{
            countA.setText("Cars : " + laneA.incomingSize() + "AL-2:"+ laneA.prioritySize());
            countB.setText("Cars : " + laneB.incomingSize());
            countC.setText("Cars : " + laneC.incomingSize());
            countD.setText("Cars : " + laneD.incomingSize());
        });
   }
}
