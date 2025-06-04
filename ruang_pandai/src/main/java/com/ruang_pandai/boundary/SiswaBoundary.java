package com.ruang_pandai.boundary;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import com.ruang_pandai.controller.SiswaController;
import com.ruang_pandai.entity.Jadwal;
import com.ruang_pandai.entity.Tutor;
import com.ruang_pandai.util.AlertHelper;

public class SiswaBoundary {
    private final Stage primaryStage;
    private final SiswaController siswaController;

    // Elemen UI untuk halaman Cari Tutor
    private ListView<Tutor> tutorListView = new ListView<>();
    private ComboBox<String> cbMataPelajaran = new ComboBox<>();
    private DatePicker dpTanggal = new DatePicker();
    private ComboBox<String> cbWaktuMulai = new ComboBox<>();
    private TextField tfNamaTutorSearch = new TextField();
    private Slider sliderRating = new Slider(0, 5, 0);
    private Label tutorListPlaceholderLabel;

    // Elemen UI untuk halaman Profil Tutor
    private Label lblProfileTutorName;
    private Label lblProfileTutorSubject;
    private Label lblProfileTutorEducation;
    private Label lblProfileTutorRating;
    private VBox vbAvailableSchedules;
    private Circle imgProfilePlaceholder;

    // Struktur layout utama
    private BorderPane mainLayout;
    private VBox searchView; // Halaman pencarian tutor
    private VBox tutorProfileView; // Halaman profil tutor

    // Gaya tombol navigasi
    private final String navButtonStyle = "-fx-background-color: transparent; -fx-text-fill: #333333; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 8 10;";
    private final String navButtonHoverStyle = "-fx-background-color: #e0e0e0; -fx-cursor: hand; -fx-text-fill: #333333; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 8 10;";
    private final String navButtonActiveStyle = "-fx-background-color: #e0e0e0; -fx-text-fill:rgb(7, 12, 15); -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 8 10;";

    private Button btnCariTutorSideMenu; // Referensi untuk tombol menu
    private Button btnSesiSayaSideMenu;  // Referensi untuk tombol menu

    private static final String CLEAR_SELECTION_TEXT = "Kosongkan"; // Placeholder untuk ComboBox waktu

    public SiswaBoundary(Stage primaryStage, SiswaController siswaController) {
        this.primaryStage = primaryStage;
        this.siswaController = siswaController;
    }

    public Scene createScene() {
        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;"); //

        // Menu samping
        VBox sideMenu = createSideMenu();
        mainLayout.setLeft(sideMenu);
        BorderPane.setMargin(sideMenu, new Insets(0, 20, 0, 0));

        // Halaman pencarian default
        searchView = createSearchView();
        mainLayout.setCenter(searchView);
        setActiveSideMenuButton(btnCariTutorSideMenu);

        // Halaman profil tutor
        tutorProfileView = createTutorProfileView();
        tutorProfileView.setVisible(false);

        mainLayout.setBottom(createFooter());

        return new Scene(mainLayout, 1000, 700);
    }

    private VBox createSideMenu() {
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(20));
        sideMenu.setPrefWidth(200);
        sideMenu.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5px;"); //

        Label appTitle = new Label("Ruang Pandai");
        appTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill:rgb(9, 9, 10);"); //
        sideMenu.getChildren().add(appTitle);
        sideMenu.getChildren().add(new Separator());

        btnCariTutorSideMenu = new Button("Cari Tutor");
        btnSesiSayaSideMenu = new Button("Sesi Saya");

        btnCariTutorSideMenu.setPrefWidth(Double.MAX_VALUE);
        btnSesiSayaSideMenu.setPrefWidth(Double.MAX_VALUE);

        btnCariTutorSideMenu.setStyle(navButtonStyle);
        btnSesiSayaSideMenu.setStyle(navButtonStyle);

        btnCariTutorSideMenu.setOnMouseEntered(e -> { if (!btnCariTutorSideMenu.getStyle().equals(navButtonActiveStyle)) btnCariTutorSideMenu.setStyle(navButtonHoverStyle); });
        btnCariTutorSideMenu.setOnMouseExited(e -> { if (!btnCariTutorSideMenu.getStyle().equals(navButtonActiveStyle)) btnCariTutorSideMenu.setStyle(navButtonStyle); });
        btnSesiSayaSideMenu.setOnMouseEntered(e -> { if (!btnSesiSayaSideMenu.getStyle().equals(navButtonActiveStyle)) btnSesiSayaSideMenu.setStyle(navButtonHoverStyle); });
        btnSesiSayaSideMenu.setOnMouseExited(e -> { if (!btnSesiSayaSideMenu.getStyle().equals(navButtonActiveStyle)) btnSesiSayaSideMenu.setStyle(navButtonStyle); });

        btnCariTutorSideMenu.setOnAction(e -> {
            showSearchView();
            setActiveSideMenuButton(btnCariTutorSideMenu);
        });
        btnSesiSayaSideMenu.setOnAction(e -> {
            AlertHelper.showInfo("Fitur 'Sesi Saya' belum diimplementasikan.");
            setActiveSideMenuButton(btnSesiSayaSideMenu);
            // If you implement the "Sesi Saya" view, switch to it here
            // showSesiSayaView();
        });

        sideMenu.getChildren().addAll(btnCariTutorSideMenu, btnSesiSayaSideMenu);
        return sideMenu;
    }

    private void setActiveSideMenuButton(Button activeButton) {
        btnCariTutorSideMenu.setStyle(navButtonStyle);
        btnSesiSayaSideMenu.setStyle(navButtonStyle);
        activeButton.setStyle(navButtonActiveStyle);
    }


    private VBox createSearchView() {
        VBox searchView = new VBox(20);
        searchView.setPadding(new Insets(0));

        // Header
        HBox mainHeader = new HBox();
        mainHeader.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Cari Tutor");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        mainHeader.getChildren().add(title);
        searchView.getChildren().add(mainHeader);

        // Konten pencarian
        HBox searchContainer = new HBox(20);
        searchContainer.setPadding(new Insets(20));

        VBox searchFormLayout = new VBox(15);
        searchFormLayout.setPadding(new Insets(20));
        searchFormLayout.setPrefWidth(300);
        searchFormLayout.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5px;");

        searchFormLayout.getChildren().add(new Label("Masukkan nama tutor..."));
        tfNamaTutorSearch.setPromptText("Cari nama tutor...");
        searchFormLayout.getChildren().add(tfNamaTutorSearch);

        searchFormLayout.getChildren().add(new Label("Mata Pelajaran:"));
        cbMataPelajaran.getItems().addAll("Matematika", "Fisika", "Bahasa Inggris", "Bahasa Indonesia");
        cbMataPelajaran.setPromptText("Pilih mata pelajaran");
        cbMataPelajaran.setPrefWidth(Double.MAX_VALUE);
        searchFormLayout.getChildren().add(cbMataPelajaran);

        searchFormLayout.getChildren().add(new Label("Rating:"));
        sliderRating.setShowTickLabels(true);
        sliderRating.setShowTickMarks(true);
        sliderRating.setMajorTickUnit(1);
        sliderRating.setBlockIncrement(1);
        sliderRating.setSnapToTicks(true);
        HBox ratingDisplay = new HBox(5);
        ratingDisplay.setAlignment(Pos.CENTER_LEFT);
        Label ratingValueLabel = new Label(String.format("%.0f", sliderRating.getValue()));
        sliderRating.valueProperty().addListener((obs, oldVal, newVal) -> ratingValueLabel.setText(String.format("%.0f", newVal)));
        ratingDisplay.getChildren().addAll(sliderRating, ratingValueLabel);
        searchFormLayout.getChildren().add(ratingDisplay);

        searchFormLayout.getChildren().add(new Label("Jadwal:"));
        VBox dateAndTimePicker = new VBox(5);
        HBox datePickerContainer = new HBox(5);
        datePickerContainer.setAlignment(Pos.CENTER_LEFT);

        // dpTanggal.setValue(LocalDate.now()); // Commented out to start with no default date
        Button btnClearDate = new Button("X");
        btnClearDate.setOnAction(e -> dpTanggal.setValue(null));
        btnClearDate.setTooltip(new Tooltip("Kosongkan Tanggal"));


        HBox.setHgrow(dpTanggal, Priority.ALWAYS);
        dpTanggal.setMaxWidth(Double.MAX_VALUE);
        datePickerContainer.getChildren().addAll(dpTanggal, btnClearDate);


        VBox timePicker = new VBox(5); // Changed from HBox as it only contains cbWaktuMulai
        timePicker.setAlignment(Pos.CENTER_LEFT);
        cbWaktuMulai.getItems().add(CLEAR_SELECTION_TEXT); // Add "Kosongkan" option for time
        cbWaktuMulai.getItems().addAll(
            "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
            "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00"
        );
        cbWaktuMulai.setPromptText("Pilih waktu");
        cbWaktuMulai.setPrefWidth(Double.MAX_VALUE);
        timePicker.getChildren().addAll(cbWaktuMulai);

        dateAndTimePicker.getChildren().addAll(datePickerContainer, timePicker);
        searchFormLayout.getChildren().add(dateAndTimePicker);

        Button btnCari = new Button("Cari");
        btnCari.setStyle("-fx-background-color:rgb(10, 11, 11); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px;");
        btnCari.setPrefWidth(Double.MAX_VALUE);
        btnCari.setOnAction(e -> searchTutors());
        searchFormLayout.getChildren().add(btnCari);

        VBox searchResultsLayout = new VBox(10);
        searchResultsLayout.setPadding(new Insets(20));
        searchResultsLayout.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5px;");

        Label resultsTitle = new Label("Daftar Tutor");
        resultsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        searchResultsLayout.getChildren().add(resultsTitle);

        setupTutorListView();
        tutorListPlaceholderLabel = new Label("Silakan isi filter pencarian.");
        tutorListPlaceholderLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #777777;");
        tutorListView.setPlaceholder(tutorListPlaceholderLabel);
        tutorListView.setItems(FXCollections.observableArrayList());


        searchResultsLayout.getChildren().add(tutorListView);
        VBox.setVgrow(tutorListView, Priority.ALWAYS);

        searchContainer.getChildren().addAll(searchFormLayout, searchResultsLayout);
        HBox.setHgrow(searchResultsLayout, Priority.ALWAYS);
        searchView.getChildren().add(searchContainer);

        return searchView;
    }

    private void setupTutorListView() {
        tutorListView.setCellFactory(lv -> new ListCell<Tutor>() {
            @Override
            protected void updateItem(Tutor tutor, boolean empty) {
                super.updateItem(tutor, empty);
                if (empty || tutor == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox card = new HBox(15);
                    card.setPadding(new Insets(10));
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setStyle("-fx-background-color: #f9f9f9; -fx-background-radius: 8px; -fx-border-color: #e0e0e0; -fx-border-radius: 8px;");

                    Circle profileImage = new Circle(30);
                    profileImage.setFill(Color.LIGHTGRAY);

                    VBox info = new VBox(5);
                    Label nameLabel = new Label(tutor.getNama());
                    nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333333;");

                    Label subjectLabel = new Label("Tutor " + tutor.getMataPelajaran());
                    subjectLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555555;");

                    HBox ratingBox = new HBox(5);
                    ratingBox.setAlignment(Pos.CENTER_LEFT);
                    if (tutor.getRating() != null) {
                        Label starIcon = new Label("⭐");
                        Label ratingLabel = new Label(String.valueOf(tutor.getRating()));
                        ratingBox.getChildren().addAll(starIcon, ratingLabel);
                    }

                    info.getChildren().clear();
                    info.getChildren().addAll(nameLabel, subjectLabel, ratingBox);

                    Button btnLihatTutor = new Button("Lihat Tutor");
                    btnLihatTutor.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 5px;");
                    btnLihatTutor.setOnAction(event -> showTutorProfile(tutor));

                    HBox.setHgrow(info, Priority.ALWAYS);
                    card.getChildren().addAll(profileImage, info, btnLihatTutor);

                    setGraphic(card);
                    setText(null);
                }
            }
        });
    }

    private VBox createTutorProfileView() {
        VBox profileView = new VBox(20);
        profileView.setPadding(new Insets(20));
        profileView.setStyle("-fx-background-color: #f5f5f5;");

        // Header dengan tombol kembali
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        Button btnBack = new Button("← Kembali");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-font-weight: bold;");
        btnBack.setOnAction(e -> {
            showSearchView();
            setActiveSideMenuButton(btnCariTutorSideMenu);
        });

        Label title = new Label("Profil Tutor");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        header.getChildren().addAll(btnBack, title);
        profileView.getChildren().add(header);

        // Konten utama: Profil Tutor dan Jadwal Tersedia
        VBox content = new VBox(20);
        // Making the content VBox itself scrollable if the profile + schedules become too long
        ScrollPane contentScrollPane = new ScrollPane(content);
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setFitToHeight(true); // Allows it to take available height
        contentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(contentScrollPane, Priority.ALWAYS);


        // Profil tutor
        HBox tutorInfoCard = new HBox(20);
        tutorInfoCard.setPadding(new Insets(20));
        tutorInfoCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px;");
        tutorInfoCard.setAlignment(Pos.CENTER_LEFT);

        imgProfilePlaceholder = new Circle(40);
        imgProfilePlaceholder.setFill(Color.LIGHTGRAY);

        VBox basicInfo = new VBox(5);
        lblProfileTutorName = new Label("Nama Tutor");
        lblProfileTutorName.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        lblProfileTutorSubject = new Label("Tutor Mata Pelajaran");
        lblProfileTutorSubject.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        lblProfileTutorEducation = new Label("Pendidikan: S1 Universitas X");
        lblProfileTutorEducation.setStyle("-fx-font-size: 14px; -fx-text-fill: #555555;");

        HBox profileRatingBox = new HBox(5);
        profileRatingBox.setAlignment(Pos.CENTER_LEFT);
        Label starIcon = new Label("⭐");
        lblProfileTutorRating = new Label("4.9");
        lblProfileTutorRating.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        profileRatingBox.getChildren().addAll(starIcon, lblProfileTutorRating);

        basicInfo.getChildren().addAll(lblProfileTutorName, lblProfileTutorSubject, lblProfileTutorEducation, profileRatingBox);
        tutorInfoCard.getChildren().addAll(imgProfilePlaceholder, basicInfo);
        HBox.setHgrow(basicInfo, Priority.ALWAYS);

        // Bagian Jadwal Tersedia
        VBox schedulesSection = new VBox(10);
        schedulesSection.setPadding(new Insets(20));
        schedulesSection.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px;");
        VBox.setVgrow(schedulesSection, Priority.ALWAYS); // Allow schedules section to grow

        Label schedulesTitle = new Label("Jadwal Tersedia");
        schedulesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        schedulesSection.getChildren().add(schedulesTitle);

        vbAvailableSchedules = new VBox(10); // This VBox will contain the schedule cards
        ScrollPane schedulesScrollPane = new ScrollPane();
        schedulesScrollPane.setContent(vbAvailableSchedules);
        schedulesScrollPane.setFitToWidth(true);
        schedulesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        schedulesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // schedulesScrollPane.setPrefHeight(300); // Optionally set a preferred height

        schedulesSection.getChildren().add(schedulesScrollPane);
        VBox.setVgrow(schedulesScrollPane, Priority.ALWAYS); // Make ScrollPane grow within schedulesSection


        content.getChildren().addAll(tutorInfoCard, schedulesSection);
        // profileView.getChildren().add(content); // Content is now inside contentScrollPane
        profileView.getChildren().add(contentScrollPane); // Add the main scrollable content area

        return profileView;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(10));
        footer.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5px;");
        // Label status = new Label("Status: Terhubung"); // Removed as per latest provided file
        // footer.getChildren().add(status);
        return footer;
    }

    private void searchTutors() {
        String mataPelajaran = cbMataPelajaran.getValue();
        String namaTutor = tfNamaTutorSearch.getText();
        int minRating = (int) sliderRating.getValue();
        LocalDate tanggal = dpTanggal.getValue();
        String waktuMulai = cbWaktuMulai.getValue();
        if (CLEAR_SELECTION_TEXT.equals(waktuMulai)) {
            waktuMulai = null;
        }


        boolean noSubject = (mataPelajaran == null || mataPelajaran.isEmpty());
        boolean noName = (namaTutor == null || namaTutor.trim().isEmpty());
        boolean noMinRating = (minRating == 0);
        boolean noDate = (tanggal == null);
        boolean noTime = (waktuMulai == null || waktuMulai.isEmpty());


        if (noSubject && noName && noMinRating && noDate && noTime) { // Check all filters
            tutorListView.setItems(FXCollections.observableArrayList());
            tutorListPlaceholderLabel.setText("Silakan isi filter pencarian.");
            tutorListView.setPlaceholder(tutorListPlaceholderLabel);
            AlertHelper.showInfo("Silakan masukkan kriteria pencarian untuk menampilkan hasil.");
            return;
        }

        try {
            List<Tutor> tutors = siswaController.cariTutor(mataPelajaran, minRating, namaTutor, tanggal, waktuMulai);

            ObservableList<Tutor> data = FXCollections.observableArrayList(tutors);
            tutorListView.setItems(data);

            if (tutors.isEmpty()) {
                tutorListPlaceholderLabel.setText("Tidak ada tutor yang sesuai.");
                tutorListView.setPlaceholder(tutorListPlaceholderLabel);
            } else {
                tutorListView.setPlaceholder(null);
            }

        } catch (Exception e) {
            AlertHelper.showError("Error saat mencari tutor: " + e.getMessage());
            tutorListView.setItems(FXCollections.observableArrayList());
            if (tutorListPlaceholderLabel != null) {
                 tutorListPlaceholderLabel.setText("Terjadi kesalahan saat pencarian.");
                 tutorListView.setPlaceholder(tutorListPlaceholderLabel);
            }
        }
    }

    private void showTutorProfile(Tutor tutor) {
        lblProfileTutorName.setText(tutor.getNama());
        lblProfileTutorSubject.setText("Tutor " + tutor.getMataPelajaran());
        lblProfileTutorEducation.setText("Pendidikan: " + tutor.getPendidikan());
        lblProfileTutorRating.setText(tutor.getRating() != null ? String.valueOf(tutor.getRating()) : "N/A");

        vbAvailableSchedules.getChildren().clear();
        List<Jadwal> jadwalList = tutor.getJadwal(); // Changed from dummyJadwals

        if (jadwalList == null || jadwalList.isEmpty()) {
            Label noScheduleLabel = new Label("Tidak ada jadwal tersedia untuk tutor ini."); // Corrected text
            noScheduleLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #555555;"); // Corrected style
            // noScheduleLabel.setAlignment(Pos.CENTER); // Removed, VBox will handle alignment
            vbAvailableSchedules.getChildren().add(noScheduleLabel);
        } else {
            for (Jadwal jadwal : jadwalList) {
                VBox scheduleCard = new VBox(5);
                scheduleCard.setPadding(new Insets(15));
                scheduleCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #e0e0e0; -fx-border-radius: 8px;");

                Label courseLabel = new Label(jadwal.getMataPelajaran());
                courseLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

                String tanggalLengkap = jadwal.getTanggal(); // "YYYY-MM-DD"
                LocalDate date = LocalDate.parse(tanggalLengkap); // Parses "YYYY-MM-DD"

                int year = date.getYear();
                Month month = date.getMonth(); // Java Time Month enum
                int day = date.getDayOfMonth();

                // Format date string properly
                String monthName = month.toString().substring(0,1) + month.toString().substring(1).toLowerCase(); // Capitalize first letter
                Label dateLabel = new Label(jadwal.getHari().substring(0,1) + jadwal.getHari().substring(1).toLowerCase() + ", " + day + " " + monthName + " " + year);
                dateLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                Label timeLabel = new Label(jadwal.getJamMulai() + " - " + jadwal.getJamSelesai());
                timeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                Button btnPesanSesi = new Button("Pesan Sesi");
                btnPesanSesi.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
                btnPesanSesi.setOnAction(e -> showBookingDialog(tutor, jadwal)); // Connect to dialog

                scheduleCard.getChildren().addAll(courseLabel, dateLabel, timeLabel, btnPesanSesi);
                vbAvailableSchedules.getChildren().add(scheduleCard);
            }
        }

        tutorProfileView.setVisible(true);
        searchView.setVisible(false);
        mainLayout.setCenter(tutorProfileView);
    }

    private void showSearchView() {
        tutorProfileView.setVisible(false);
        searchView.setVisible(true);
        mainLayout.setCenter(searchView);
    }

    private void showBookingDialog(Tutor tutor, Jadwal jadwal) {
        Dialog<ButtonType> dialog = new Dialog<>(); // Use ButtonType for result
        dialog.setTitle("Pesan Sesi");
        dialog.setHeaderText("Konfirmasi Pemesanan Sesi");
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/ruang_pandai/styles/style.css") != null ? getClass().getResource("/com/ruang_pandai/styles/style.css").toExternalForm() : "");


        // Set the button types.
        ButtonType pesanButtonType = new ButtonType("Pesan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(pesanButtonType, ButtonType.CANCEL);

        // Create the content for the dialog.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10)); // Adjusted padding

        Label tutorLabel = new Label("Tutor:");
        Label tutorValue = new Label(tutor.getNama());
        grid.add(tutorLabel, 0, 0);
        grid.add(tutorValue, 1, 0);

        Label mapelLabel = new Label("Mata Pelajaran:");
        Label mapelValue = new Label(jadwal.getMataPelajaran());
        grid.add(mapelLabel, 0, 1);
        grid.add(mapelValue, 1, 1);

        String tanggalLengkap = jadwal.getTanggal();
        LocalDate date = LocalDate.parse(tanggalLengkap);
        String monthName = date.getMonth().toString().substring(0,1) + date.getMonth().toString().substring(1).toLowerCase();

        Label tanggalLabel = new Label("Hari/Tanggal:");
        Label tanggalValue = new Label(jadwal.getHari().substring(0, 1) + jadwal.getHari().substring(1).toLowerCase() + ", " + date.getDayOfMonth() + " " + monthName + " " + date.getYear());
        grid.add(tanggalLabel, 0, 2);
        grid.add(tanggalValue, 1, 2);

        Label waktuLabel = new Label("Waktu:");
        Label waktuValue = new Label(jadwal.getJamMulai() + " - " + jadwal.getJamSelesai());
        grid.add(waktuLabel, 0, 3);
        grid.add(waktuValue, 1, 3);

        Label metodePembayaranLabel = new Label("Metode Pembayaran:");
        ComboBox<String> cbMetodePembayaran = new ComboBox<>();
        cbMetodePembayaran.getItems().addAll("Transfer Bank", "Gopay", "OVO");
        cbMetodePembayaran.setPromptText("Pilih metode pembayaran");
        cbMetodePembayaran.setPrefWidth(200); // Set preferred width
        grid.add(metodePembayaranLabel, 0, 4);
        grid.add(cbMetodePembayaran, 1, 4);
        
        dialog.getDialogPane().setContent(grid);

        // Convert the result to a String or some other object when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == pesanButtonType) {
                if (cbMetodePembayaran.getValue() == null || cbMetodePembayaran.getValue().isEmpty()) {
                    AlertHelper.showError("Metode pembayaran harus dipilih.");
                    return null; // Prevent dialog from closing
                }
                // Here you would typically call a controller method to save the booking
                // For example: siswaController.pesanSesi(siswaLoggedIn, tutor, jadwal, cbMetodePembayaran.getValue());
                return dialogButton; // Return the button type itself
            }
            return null;
        });

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == pesanButtonType) {
                 // This check is technically redundant if the null return in resultConverter works
                if (cbMetodePembayaran.getValue() != null && !cbMetodePembayaran.getValue().isEmpty()) {
                    AlertHelper.showInfo("Pemesanan sesi untuk " + tutor.getNama() +
                                         " pada " + tanggalValue.getText() +
                                         " pukul " + waktuValue.getText() +
                                         " dengan metode " + cbMetodePembayaran.getValue() + " berhasil.");
                }
            }
        });
    }
}