package com.ruang_pandai.boundary;

import com.ruang_pandai.controller.SiswaController;
import com.ruang_pandai.controller.TutorController;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainBoundary {

    private final Stage primaryStage;

    public MainBoundary(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Scene createScene() {
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(50));
        mainLayout.setStyle("-fx-background-color: #ffffff;");

        Label title = new Label("Selamat datang di Ruang Pandai");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label subtitle = new Label("Pilih Role Anda");
        subtitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #555555;");
        VBox.setMargin(subtitle, new Insets(0, 0, 20, 0));

        HBox cardsContainer = new HBox(40);
        cardsContainer.setAlignment(Pos.CENTER);

        // Aksi saat tombol "Pilih" untuk Siswa diklik
        EventHandler<ActionEvent> pilihSiswaAction = event -> {
            SiswaController siswaController = new SiswaController();
            SiswaBoundary siswaBoundary = new SiswaBoundary(primaryStage, siswaController);
            primaryStage.setScene(siswaBoundary.createScene());
            primaryStage.setTitle("Ruang Pandai - Siswa");
        };

        // Aksi saat tombol "Pilih" untuk Tutor diklik
        EventHandler<ActionEvent> pilihTutorAction = event -> {
            TutorController tutorController = new TutorController();
            TutorBoundary tutorBoundary = new TutorBoundary(primaryStage, tutorController);
            primaryStage.setScene(tutorBoundary.createScene());
            primaryStage.setTitle("Ruang Pandai - Tutor");
        };

        VBox siswaCard = createRoleCard("Siswa", "Budi Santoso", pilihSiswaAction);
        VBox tutorCard = createRoleCard("Tutor", "Citra Dewi", pilihTutorAction);

        cardsContainer.getChildren().addAll(siswaCard, tutorCard);
        mainLayout.getChildren().addAll(title, subtitle, cardsContainer);

        return new Scene(mainLayout, 1000, 700);
    }

    private VBox createRoleCard(String role, String nama, EventHandler<ActionEvent> onPilihAction) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(25));
        card.setPrefSize(300, 180);
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e0e0e0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label roleLabel = new Label(role);
        roleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label namaLabel = new Label("Nama: " + nama);
        namaLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Button pilihButton = new Button("Pilih");
        pilihButton.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 5px;");
        pilihButton.setOnAction(onPilihAction);

        card.getChildren().addAll(roleLabel, namaLabel, pilihButton);
        return card;
    }
}

