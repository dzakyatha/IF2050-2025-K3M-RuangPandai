package com.ruang_pandai;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;


import com.ruang_pandai.database.DatabaseInitializer;
import com.ruang_pandai.boundary.MainBoundary;
import com.ruang_pandai.boundary.SiswaBoundary;
import com.ruang_pandai.controller.SiswaController;
import com.ruang_pandai.database.DatabaseInitializer;

public class App extends Application {

    public static Stage primaryStage;

    @Override
    public void start(Stage stage){
        primaryStage = stage; 
        primaryStage.setTitle("Ruang Pandai");

        // SiswaController siswaController = new SiswaController();
        // SiswaBoundary siswaBoundary = new SiswaBoundary(primaryStage, siswaController);

        // Buat instance dari MainBoundary sebagai halaman utama
        MainBoundary mainBoundary = new MainBoundary(primaryStage);

        // Tampilkan Scene dari MainBoundary
        primaryStage.setScene(mainBoundary.createScene());
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