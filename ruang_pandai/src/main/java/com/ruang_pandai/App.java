package com.ruang_pandai;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;


import com.ruang_pandai.boundary.TutorBoundary;
import com.ruang_pandai.controller.TutorController;
import com.ruang_pandai.database.DatabaseInitializer;

public class App extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage){
        primaryStage = stage; 
        primaryStage.setTitle("Ruang Pandai");

        TutorController tutorController = new TutorController();
        TutorBoundary tutorBoundary = new TutorBoundary(primaryStage, tutorController);

        primaryStage.setScene(tutorBoundary.createScene());
        primaryStage.show();
    }

    public static void switchScene(Scene scene){
        if (Platform.isFxApplicationThread()){
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            primaryStage.setFullScreen(true);
        } else {
            Platform.runLater(() -> primaryStage.setScene(scene));
            primaryStage.setMaximized(true);
            primaryStage.setFullScreen(true);
        }
    }
    
    public static void main (String[] args){
        DatabaseInitializer.initialize();
        launch(args);
    }

}