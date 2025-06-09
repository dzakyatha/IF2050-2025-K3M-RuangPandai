package com.ruang_pandai.boundary;

import com.ruang_pandai.controller.TutorController;
import com.ruang_pandai.entity.Jadwal;
import com.ruang_pandai.util.AlertHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class TutorBoundary {

    private final Stage primaryStage;
    private final TutorController tutorController;

    private ObservableList<Jadwal> daftarJadwal;
    private FilteredList<Jadwal> filteredJadwalTersedia;
    private FilteredList<Jadwal> filteredJadwalDipesan;
    
    private final String currentTutorId = "P3";
    
    private TabPane tabPane;
    private ListView<Jadwal> lvJadwalTersedia = new ListView<>();
    private ListView<Jadwal> lvJadwalDipesan = new ListView<>();
    private Button btnTambahJadwal;

    public TutorBoundary(Stage primaryStage, TutorController tutorController) {
        this.primaryStage = primaryStage;
        this.tutorController = tutorController;
        this.daftarJadwal = FXCollections.observableArrayList(tutorController.lihatJadwal(currentTutorId));
        this.filteredJadwalTersedia = new FilteredList<>(daftarJadwal, j -> "TERSEDIA".equals(j.getStatusJadwal()));
        this.filteredJadwalDipesan = new FilteredList<>(daftarJadwal, j -> "DIPESAN".equals(j.getStatusJadwal()));
    }

    public Scene createScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");
        mainLayout.setPadding(new Insets(20));

        VBox sideMenu = createSideMenu();
        mainLayout.setLeft(sideMenu);
        BorderPane.setMargin(sideMenu, new Insets(0, 20, 0, 0));

        VBox kelolaJadwalView = createKelolaJadwalView();
        mainLayout.setCenter(kelolaJadwalView);

        refreshJadwalList();

        return new Scene(mainLayout, 1000, 700);
    }

    private VBox createSideMenu() {
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(20));
        sideMenu.setPrefWidth(220);
        sideMenu.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px;");

        Label appTitle = new Label("Ruang Pandai");
        appTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        Button btnKelolaJadwal = new Button("Kelola Jadwal");
        btnKelolaJadwal.setPrefWidth(Double.MAX_VALUE);
        btnKelolaJadwal.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 8 12; -fx-background-radius: 5px;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button btnKembali = new Button("Kembali ke menu utama");
        btnKembali.setPrefWidth(Double.MAX_VALUE);
        btnKembali.setStyle("-fx-background-color: #f4f4f4; -fx-text-fill: #333; -fx-font-size: 14px; -fx-alignment: CENTER; -fx-padding: 8 10; -fx-border-color: #ddd; -fx-border-radius: 5;");
        btnKembali.setOnAction(e -> {
            MainBoundary mainBoundary = new MainBoundary(primaryStage);
            primaryStage.setScene(mainBoundary.createScene());
            primaryStage.setTitle("Ruang Pandai");
        });

        sideMenu.getChildren().addAll(appTitle, separator, btnKelolaJadwal, spacer, btnKembali);
        return sideMenu;
    }

    private VBox createKelolaJadwalView() {
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(10));

        Label title = new Label("Kelola Jadwal");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        btnTambahJadwal = new Button("Tambah Jadwal");
        btnTambahJadwal.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 8 16;");
        btnTambahJadwal.setOnAction(e -> dialogTambahEditjadwal(null));
        HBox topBar = new HBox(btnTambahJadwal);
        topBar.setAlignment(Pos.CENTER_RIGHT);

        tabPane = new TabPane();
        Tab tabTersedia = new Tab("Jadwal Tersedia");
        Tab tabDipesan = new Tab("Jadwal Dipesan");

        setupJadwalListView(lvJadwalTersedia, true);
        setupJadwalListView(lvJadwalDipesan, false);

        lvJadwalTersedia.setItems(filteredJadwalTersedia);
        lvJadwalDipesan.setItems(filteredJadwalDipesan);
        
        lvJadwalTersedia.setPlaceholder(new Label("Tidak ada jadwal yang tersedia."));
        lvJadwalDipesan.setPlaceholder(new Label("Tidak ada jadwal yang sudah dipesan."));
        
        tabTersedia.setContent(lvJadwalTersedia);
        tabDipesan.setContent(lvJadwalDipesan);
        tabPane.getTabs().addAll(tabTersedia, tabDipesan);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            boolean isTersediaTab = newTab == tabTersedia;
            btnTambahJadwal.setVisible(isTersediaTab);
            btnTambahJadwal.setManaged(isTersediaTab);
        });

        VBox.setVgrow(tabPane, Priority.ALWAYS);
        mainContent.getChildren().addAll(title, topBar, tabPane);
        return mainContent;
    }
    
    private void setupJadwalListView(ListView<Jadwal> listView, boolean isEditable) {
    listView.setCellFactory(lv -> new ListCell<>() {
        @Override
        protected void updateItem(Jadwal jadwal, boolean empty) {
            super.updateItem(jadwal, empty);

            setText(null);

            if (empty || jadwal == null) {
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                setStyle("-fx-background-color: transparent; -fx-padding: 5 0;");

                VBox card = new VBox(10);
                card.setPadding(new Insets(15));
                card.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-radius: 8px; -fx-background-radius: 8px;");

                Label lblMapel = new Label(jadwal.getMataPelajaran());
                lblMapel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

                LocalDate date = LocalDate.parse(jadwal.getTanggal());
                String namaHari = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
                String tanggalFormatted = String.format("%s, %02d %s %d", namaHari, date.getDayOfMonth(), date.getMonth().getDisplayName(TextStyle.FULL, new Locale("id", "ID")), date.getYear());
                Label lblHariTanggal = new Label(tanggalFormatted);

                String jamFormatted = String.format("Pukul %s - %s WIB", jadwal.getJamMulai().substring(0, 5), jadwal.getJamSelesai().substring(0, 5));
                Label lblJam = new Label(jamFormatted);
                
                card.getChildren().addAll(lblMapel, lblHariTanggal, lblJam);

                if (isEditable) {
                    HBox actionButtons = new HBox(10);
                    actionButtons.setAlignment(Pos.CENTER_LEFT);
                    Button btnEdit = new Button("Edit");
                    Button btnHapus = new Button("Hapus");
                    btnEdit.setStyle("-fx-background-color: #f0f0f0; -fx-font-weight: bold;");
                    btnHapus.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");

                    btnEdit.setOnAction(e -> dialogTambahEditjadwal(jadwal));
                    btnHapus.setOnAction(e -> dialogHapusjadwal(jadwal));
                    
                    actionButtons.getChildren().addAll(btnEdit, btnHapus);
                    card.getChildren().add(actionButtons);
                }
                setGraphic(card);
            }
        }
    });
    listView.setStyle("-fx-background-color: transparent;");
}

    private void refreshJadwalList() {
        daftarJadwal.setAll(tutorController.lihatJadwal(currentTutorId));
        lvJadwalTersedia.setPlaceholder(new Label("Tidak ada jadwal yang tersedia."));
        lvJadwalDipesan.setPlaceholder(new Label("Tidak ada jadwal yang sudah dipesan."));
    }
    
    private void dialogTambahEditjadwal(Jadwal jadwal) {
        Dialog<Jadwal> dialog = new Dialog<>();
        boolean isEditMode = jadwal != null;
        dialog.setTitle(isEditMode ? "Edit Jadwal" : "Tambah Jadwal");
        dialog.setHeaderText(null);

        ComboBox<String> cbMapel = new ComboBox<>(FXCollections.observableArrayList("Matematika", "Fisika", "Bahasa Inggris", "Bahasa Indonesia"));
        cbMapel.setPromptText("Pilih Mata Pelajaran");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Pilih Tanggal");
        ComboBox<String> cbJamMulai = new ComboBox<>(FXCollections.observableArrayList(
            "07:00", "08:00", "09:00", "10:00", "11:00", 
            "12:00", "13:00", "14:00", "15:00", "16:00", 
            "17:00", "18:00", "19:00", "20:00", "21:00"));
        cbJamMulai.setPromptText("Pilih Jam Mulai");
        ComboBox<String> cbJamSelesai = new ComboBox<>(FXCollections.observableArrayList(
            "07:00", "08:00", "09:00", "10:00", "11:00", 
            "12:00", "13:00", "14:00", "15:00", "16:00", 
            "17:00", "18:00", "19:00", "20:00", "21:00"));
        cbJamSelesai.setPromptText("Pilih Jam Selesai");

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);

        if (isEditMode) {
            cbMapel.setValue(jadwal.getMataPelajaran());
            datePicker.setValue(LocalDate.parse(jadwal.getTanggal()));
            cbJamMulai.setValue(jadwal.getJamMulai());
            cbJamSelesai.setValue(jadwal.getJamSelesai());
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.add(new Label("Mata Pelajaran:"), 0, 0); grid.add(cbMapel, 1, 0);
        grid.add(new Label("Tanggal:"), 0, 1); grid.add(datePicker, 1, 1);
        grid.add(new Label("Jam Mulai:"), 0, 2); grid.add(cbJamMulai, 1, 2);
        grid.add(new Label("Jam Selesai:"), 0, 3); grid.add(cbJamSelesai, 1, 3);
        grid.add(errorLabel, 1, 4);
        dialog.getDialogPane().setContent(grid);

        ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

        Node submitButton = dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.setDisable(true);
        
        Runnable validateInputs = () -> {
            boolean allFilled = cbMapel.getValue() != null && datePicker.getValue() != null &&
                                cbJamMulai.getValue() != null && cbJamSelesai.getValue() != null;
            errorLabel.setVisible(false);
            if (!allFilled) {
                submitButton.setDisable(true);
                return;
            }

            // Validasi jam dan tanggal
            LocalDate tanggal = datePicker.getValue();
            LocalTime mulai = LocalTime.parse(cbJamMulai.getValue());
            LocalTime selesai = LocalTime.parse(cbJamSelesai.getValue());
            if (selesai.isBefore(mulai) || selesai.equals(mulai)) {
                errorLabel.setText("Jam mulai harus > jam selesai");
                errorLabel.setVisible(true);
                submitButton.setDisable(true);
                return;
            }
            if (tanggal.isBefore(LocalDate.now())) {
                errorLabel.setText("Tanggal sudah lewat");
                errorLabel.setVisible(true);
                submitButton.setDisable(true);
                return;
            }
            if (tanggal.isEqual(LocalDate.now()) && mulai.isBefore(LocalTime.now())) {
                errorLabel.setText("Jam mulai hari ini sudah lewat");
                errorLabel.setVisible(true);
                submitButton.setDisable(true);
                return;
            }
            
            if (isEditMode) {
                boolean isChanged = !Objects.equals(cbMapel.getValue(), jadwal.getMataPelajaran()) ||
                                    !Objects.equals(datePicker.getValue().toString(), jadwal.getTanggal()) ||
                                    !Objects.equals(cbJamMulai.getValue(), jadwal.getJamMulai()) ||
                                    !Objects.equals(cbJamSelesai.getValue(), jadwal.getJamSelesai());
                submitButton.setDisable(!isChanged);
            } else {
                submitButton.setDisable(false);
            }
        };

        cbMapel.valueProperty().addListener((obs, o, n) -> validateInputs.run());
        datePicker.valueProperty().addListener((obs, o, n) -> validateInputs.run());
        cbJamMulai.valueProperty().addListener((obs, o, n) -> validateInputs.run());
        cbJamSelesai.valueProperty().addListener((obs, o, n) -> validateInputs.run());
        if(isEditMode) validateInputs.run();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == submitButtonType) {
                DayOfWeek day = datePicker.getValue().getDayOfWeek();
                String hari = day.getDisplayName(TextStyle.FULL, new Locale("id", "ID")).toUpperCase();
                String id = isEditMode ? jadwal.getIdJadwal() : "J" + (daftarJadwal.stream().mapToInt(j -> Integer.parseInt(j.getIdJadwal().substring(1))).max().orElse(0) + 1);
                return new Jadwal(id, currentTutorId, cbMapel.getValue(), hari, datePicker.getValue().toString(), cbJamMulai.getValue(), cbJamSelesai.getValue(), "TERSEDIA");
            }
            return null;
        });

        Optional<Jadwal> result = dialog.showAndWait();
        result.ifPresent(newJadwal -> dialogKonfirmasi(isEditMode ? "Pembaruan" : "Penambahan", newJadwal));
    }

    private void dialogKonfirmasi(String actionType, Jadwal jadwal) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Konfirmasi " + actionType + " Jadwal");

        LocalDate date = LocalDate.parse(jadwal.getTanggal());
        String hari = jadwal.getHari().substring(0, 1) + jadwal.getHari().substring(1).toLowerCase();
        String infoJadwal = String.format("%s, %02d %s %d", hari, date.getDayOfMonth(), date.getMonth().getDisplayName(TextStyle.FULL, new Locale("id", "ID")), date.getYear());
        String pukul = jadwal.getJamMulai() + " - " + jadwal.getJamSelesai();
        dialog.setContentText("Jadwal baru: " + infoJadwal + "\nPukul: "+ pukul  +"\nYakin dengan " + actionType.toLowerCase() + " jadwal?");

        ButtonType konfirmasiButton = new ButtonType("Konfirmasi", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(konfirmasiButton, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == konfirmasiButton) {
            boolean success;
            if ("Penambahan".equals(actionType)) {
                success = tutorController.buatJadwal(jadwal);
            } else {
                success = tutorController.ubahJadwal(jadwal);
            }

            if (success) {
                AlertHelper.showInfo(actionType + " jadwal berhasil.");
                refreshJadwalList(); // Muat ulang data dari database
            } else {
                AlertHelper.showError(actionType + " jadwal gagal.");
            }
        }
    }

    private void dialogHapusjadwal(Jadwal jadwal) {
        if ("DIPESAN".equals(jadwal.getStatusJadwal())) {
            AlertHelper.showError("Jadwal ini sudah dipesan dan tidak dapat dihapus.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Konfirmasi Penghapusan Jadwal");
        dialog.setContentText("Yakin ingin menghapus jadwal ini?");

        ButtonType konfirmasiButton = new ButtonType("Konfirmasi", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(konfirmasiButton, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == konfirmasiButton) {
            boolean success = tutorController.hapusJadwal(jadwal.getIdJadwal());
            if (success) {
                AlertHelper.showInfo("Penghapusan jadwal berhasil.");
                refreshJadwalList(); // Muat ulang data dari database
            } else {
                AlertHelper.showError("Penghapusan jadwal gagal.");
            }
        }
    }
}
