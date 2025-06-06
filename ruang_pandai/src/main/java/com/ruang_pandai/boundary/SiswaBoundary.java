package com.ruang_pandai.boundary;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import com.ruang_pandai.controller.SiswaController;
import com.ruang_pandai.entity.Jadwal;
import com.ruang_pandai.entity.Sesi; 
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

    // Elemen UI untuk halaman Sesi Saya
    private VBox sesiSayaView;
    private TabPane sesiTabPane;
    private ListView<Sesi> lvSesiAkanDatang = new ListView<>();
    private ListView<Sesi> lvSesiSelesai = new ListView<>();


    // Struktur layout utama 
    private BorderPane mainLayout;
    private VBox searchView;
    private VBox tutorProfileView;

    // Tombol navigasi 
    private final String navButtonStyle = "-fx-background-color: transparent; -fx-text-fill: #333333; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 8 10;";
    private final String navButtonHoverStyle = "-fx-background-color: #e0e0e0; -fx-cursor: hand; -fx-text-fill: #333333; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 8 10;";
    private final String navButtonActiveStyle = "-fx-background-color: #e0e0e0; -fx-text-fill:rgb(7, 12, 15); -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 8 10;";

    private Button btnCariTutorSideMenu;
    private Button btnSesiSayaSideMenu;

    private static final String CLEAR_SELECTION_TEXT = "Kosongkan";
    private File selectedProofFile = null;

    public SiswaBoundary(Stage primaryStage, SiswaController siswaController) {
        this.primaryStage = primaryStage;
        this.siswaController = siswaController;
    }

    public Scene createScene() {
        mainLayout = new BorderPane();
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        VBox sideMenu = createSideMenu();
        mainLayout.setLeft(sideMenu);
        BorderPane.setMargin(sideMenu, new Insets(0, 20, 0, 0));

        searchView = createSearchView();
        tutorProfileView = createTutorProfileView();
        sesiSayaView = createSesiSayaView();

        mainLayout.setCenter(searchView);
        tutorProfileView.setVisible(false);
        sesiSayaView.setVisible(false);

        setActiveSideMenuButton(btnCariTutorSideMenu);

        mainLayout.setBottom(createFooter());

        return new Scene(mainLayout, 1000, 700);
    }

    private VBox createSideMenu() {
        VBox sideMenu = new VBox(10);
        sideMenu.setPadding(new Insets(20));
        sideMenu.setPrefWidth(200);
        sideMenu.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5px;");

        Label appTitle = new Label("Ruang Pandai");
        appTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill:rgb(9, 9, 10);");
        sideMenu.getChildren().add(appTitle);
        sideMenu.getChildren().add(new Separator());

        btnCariTutorSideMenu = new Button("Cari Tutor");
        btnSesiSayaSideMenu = new Button("Sesi Saya");

        btnCariTutorSideMenu.setPrefWidth(Double.MAX_VALUE);
        btnSesiSayaSideMenu.setPrefWidth(Double.MAX_VALUE);

        btnCariTutorSideMenu.setStyle(navButtonStyle);
        btnSesiSayaSideMenu.setStyle(navButtonStyle);

        btnCariTutorSideMenu.setOnMouseEntered(e -> { if (!mainLayout.getCenter().equals(searchView) && !btnCariTutorSideMenu.getStyle().equals(navButtonActiveStyle) ) btnCariTutorSideMenu.setStyle(navButtonHoverStyle); });
        btnCariTutorSideMenu.setOnMouseExited(e -> { if (!mainLayout.getCenter().equals(searchView) && !btnCariTutorSideMenu.getStyle().equals(navButtonActiveStyle)) btnCariTutorSideMenu.setStyle(navButtonStyle); });
        btnSesiSayaSideMenu.setOnMouseEntered(e -> { if (!mainLayout.getCenter().equals(sesiSayaView) && !btnSesiSayaSideMenu.getStyle().equals(navButtonActiveStyle)) btnSesiSayaSideMenu.setStyle(navButtonHoverStyle); });
        btnSesiSayaSideMenu.setOnMouseExited(e -> { if (!mainLayout.getCenter().equals(sesiSayaView) && !btnSesiSayaSideMenu.getStyle().equals(navButtonActiveStyle)) btnSesiSayaSideMenu.setStyle(navButtonStyle); });


        btnCariTutorSideMenu.setOnAction(e -> {
            showSearchView();
            setActiveSideMenuButton(btnCariTutorSideMenu);
        });
        btnSesiSayaSideMenu.setOnAction(e -> {
            showSesiSayaView();
            setActiveSideMenuButton(btnSesiSayaSideMenu);
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
        VBox searchViewLayout = new VBox(20);
        searchViewLayout.setPadding(new Insets(0));

        HBox mainHeader = new HBox();
        mainHeader.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Cari Tutor");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        mainHeader.getChildren().add(title);
        searchViewLayout.getChildren().add(mainHeader);

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

        Button btnClearDate = new Button("X");
        btnClearDate.setOnAction(e -> dpTanggal.setValue(null));
        btnClearDate.setTooltip(new Tooltip("Kosongkan Tanggal"));

        HBox.setHgrow(dpTanggal, Priority.ALWAYS);
        dpTanggal.setMaxWidth(Double.MAX_VALUE);
        datePickerContainer.getChildren().addAll(dpTanggal, btnClearDate);

        VBox timePickerLayout = new VBox(5);
        timePickerLayout.setAlignment(Pos.CENTER_LEFT);
        cbWaktuMulai.getItems().add(CLEAR_SELECTION_TEXT);
        cbWaktuMulai.getItems().addAll(
            "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00",
            "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00"
        );
        cbWaktuMulai.setPromptText("Pilih waktu");
        cbWaktuMulai.setPrefWidth(Double.MAX_VALUE);
        timePickerLayout.getChildren().addAll(cbWaktuMulai);

        dateAndTimePicker.getChildren().addAll(datePickerContainer, timePickerLayout);
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
        searchViewLayout.getChildren().add(searchContainer);

        return searchViewLayout;
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
                        Label starIcon = new Label("⭐"); // Star emoji
                        Label ratingLabel = new Label(String.valueOf(tutor.getRating()));
                        ratingBox.getChildren().addAll(starIcon, ratingLabel);
                    }

                    info.getChildren().clear(); // Clear previous content
                    info.getChildren().addAll(nameLabel, subjectLabel, ratingBox);

                    Button btnLihatTutor = new Button("Lihat Tutor");
                    btnLihatTutor.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-font-weight: bold; -fx-background-radius: 5px;");
                    btnLihatTutor.setOnAction(event -> showTutorProfile(tutor));

                    HBox.setHgrow(info, Priority.ALWAYS); // Make info VBox grow
                    card.getChildren().addAll(profileImage, info, btnLihatTutor);

                    setGraphic(card);
                    setText(null); // Important for ListCell
                }
            }
        });
    }

    private VBox createTutorProfileView() {
        VBox profileViewLayout = new VBox(20); 
        profileViewLayout.setPadding(new Insets(20));
        profileViewLayout.setStyle("-fx-background-color: #f5f5f5;");

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
        profileViewLayout.getChildren().add(header);

        VBox content = new VBox(20);
        ScrollPane contentScrollPane = new ScrollPane(content);
        contentScrollPane.setFitToWidth(true);
        contentScrollPane.setFitToHeight(true);
        contentScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(contentScrollPane, Priority.ALWAYS);

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

        VBox schedulesSection = new VBox(10);
        schedulesSection.setPadding(new Insets(20));
        schedulesSection.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px;");
        VBox.setVgrow(schedulesSection, Priority.ALWAYS);

        Label schedulesTitle = new Label("Jadwal Tersedia");
        schedulesTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        schedulesSection.getChildren().add(schedulesTitle);

        vbAvailableSchedules = new VBox(10);
        ScrollPane schedulesScrollPane = new ScrollPane();
        schedulesScrollPane.setContent(vbAvailableSchedules);
        schedulesScrollPane.setFitToWidth(true);
        schedulesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        schedulesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        schedulesSection.getChildren().add(schedulesScrollPane);
        VBox.setVgrow(schedulesScrollPane, Priority.ALWAYS);

        content.getChildren().addAll(tutorInfoCard, schedulesSection);
        profileViewLayout.getChildren().add(contentScrollPane);

        return profileViewLayout;
    }

    private HBox createFooter() {
        HBox footerLayout = new HBox();
        footerLayout.setPadding(new Insets(10));
        footerLayout.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 5px;");
        return footerLayout;
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

        if (noSubject && noName && noMinRating && noDate && noTime) {
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
            e.printStackTrace(); 
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
        List<Jadwal> jadwalList = tutor.getJadwal();

        if (jadwalList == null || jadwalList.isEmpty()) {
            Label noScheduleLabel = new Label("Tidak ada jadwal tersedia untuk tutor ini.");
            noScheduleLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #555555;");
            vbAvailableSchedules.getChildren().add(noScheduleLabel);
        } else {
            for (Jadwal jadwal : jadwalList) {
                VBox scheduleCard = new VBox(5);
                scheduleCard.setPadding(new Insets(15));
                scheduleCard.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8px; -fx-border-color: #e0e0e0; -fx-border-radius: 8px;");

                Label courseLabel = new Label(jadwal.getMataPelajaran());
                courseLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

                String tanggalLengkap = jadwal.getTanggal();
                LocalDate date = LocalDate.parse(tanggalLengkap);
                String monthName = date.getMonth().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
                String dayName = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id", "ID"));
                Label dateLabel = new Label(dayName + ", " + date.getDayOfMonth() + " " + monthName + " " + date.getYear());

                dateLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
                Label timeLabel = new Label(jadwal.getJamMulai() + " - " + jadwal.getJamSelesai());
                timeLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                Button btnPesanSesi = new Button("Pesan Sesi");
                btnPesanSesi.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px;");
                btnPesanSesi.setOnAction(e -> dialogPemesananSesi(tutor, jadwal));

                scheduleCard.getChildren().addAll(courseLabel, dateLabel, timeLabel, btnPesanSesi);
                vbAvailableSchedules.getChildren().add(scheduleCard);
            }
        }

        tutorProfileView.setVisible(true);
        searchView.setVisible(false);
        if (sesiSayaView != null) sesiSayaView.setVisible(false);
        mainLayout.setCenter(tutorProfileView);
    }

    private void showSearchView() {
        if (tutorProfileView != null) tutorProfileView.setVisible(false);
        if (sesiSayaView != null) sesiSayaView.setVisible(false);
        searchView.setVisible(true);
        mainLayout.setCenter(searchView);
    }

    private VBox createSesiSayaView() {
        sesiSayaView = new VBox(10); 
        sesiSayaView.setPadding(new Insets(0)); 

        Label title = new Label("Sesi Saya");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        HBox headerPane = new HBox(title);
        headerPane.setPadding(new Insets(0,0,10,0));

        sesiTabPane = new TabPane();

        Tab tabAkanDatang = new Tab("Akan Datang");
        tabAkanDatang.setClosable(false);
        VBox contentAkanDatang = new VBox(10);
        contentAkanDatang.setPadding(new Insets(10));
        setupSesiListView(lvSesiAkanDatang, "Akan Datang");
        contentAkanDatang.getChildren().add(lvSesiAkanDatang);
        VBox.setVgrow(lvSesiAkanDatang, Priority.ALWAYS);
        tabAkanDatang.setContent(contentAkanDatang);

        Tab tabSelesai = new Tab("Selesai");
        tabSelesai.setClosable(false);
        VBox contentSelesai = new VBox(10);
        contentSelesai.setPadding(new Insets(10));
        setupSesiListView(lvSesiSelesai, "Selesai");
        contentSelesai.getChildren().add(lvSesiSelesai);
        VBox.setVgrow(lvSesiSelesai, Priority.ALWAYS);
        tabSelesai.setContent(contentSelesai);

        sesiTabPane.getTabs().addAll(tabAkanDatang, tabSelesai);
        VBox.setVgrow(sesiTabPane, Priority.ALWAYS);

        sesiSayaView.getChildren().addAll(headerPane, sesiTabPane);
        populateSesiData(); 
        return sesiSayaView;
    }

    private void showSesiSayaView() {
        mainLayout.setCenter(sesiSayaView);
        searchView.setVisible(false);
        tutorProfileView.setVisible(false);
        sesiSayaView.setVisible(true);
        populateSesiData(); 
    }

    private void setupSesiListView(ListView<Sesi> listView, String statusSesiTab) {
        listView.setCellFactory(lv -> new ListCell<Sesi>() {
            @Override
            protected void updateItem(Sesi sesi, boolean empty) {
                super.updateItem(sesi, empty);
                if (empty || sesi == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    VBox cardContent = new VBox(8);
                    cardContent.setPadding(new Insets(12));
                    cardContent.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #e0e0e0; -fx-border-radius: 8px; -fx-background-radius: 8px;");

                    String namaTutor = siswaController.getTutorById(sesi.getIdTutor()).getNama();
                    Jadwal jadwalTerkait = siswaController.getJadwalById(sesi.getIdJadwal());
                    String hariTanggal = siswaController.getHariTanggalJadwal(sesi.getIdJadwal()); 
                    String pukul = siswaController.getJamJadwal(sesi.getIdJadwal()); 

                    Label lblMapel = new Label(jadwalTerkait.getMataPelajaran());
                    lblMapel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #333;");

                    Label lblTutor = new Label("Tutor: " + namaTutor);
                    lblTutor.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

                    Label lblTanggal = new Label(hariTanggal); 
                    Label lblWaktu = new Label(pukul);
                    lblTanggal.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
                    lblWaktu.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");

                    cardContent.getChildren().addAll(lblMapel, lblTutor, lblTanggal, lblWaktu);

                    if ("Akan Datang".equals(statusSesiTab)) {
                        HBox actionButtons = new HBox(10);
                        actionButtons.setAlignment(Pos.CENTER_RIGHT);
                        actionButtons.setPadding(new Insets(10, 0, 0, 0));

                        Button btnBatalkan = new Button("Batalkan Sesi");
                        Button btnJadwalUlang = new Button("Jadwal Ulang Sesi");

                        btnBatalkan.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 6 12;");
                        btnJadwalUlang.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 6 12;");

                        btnBatalkan.setOnAction(e -> dialogPembatalan(sesi));
                        btnJadwalUlang.setOnAction(e -> dialogJadwalUlangSesi(sesi));

                        actionButtons.getChildren().addAll(btnJadwalUlang, btnBatalkan);
                        cardContent.getChildren().add(actionButtons);
                    } else if ("Selesai".equals(statusSesiTab)) {
                        Button btnLihatDetail = new Button("Lihat Sesi");
                        btnLihatDetail.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-padding: 6 12;");
                        btnLihatDetail.setOnAction(e -> {
                            dialogDetailSesi(sesi);
                        });
                        HBox detailButtonBox = new HBox(btnLihatDetail);
                        detailButtonBox.setAlignment(Pos.CENTER_RIGHT);
                        detailButtonBox.setPadding(new Insets(10, 0, 0, 0));
                        cardContent.getChildren().add(detailButtonBox);
                    }
                    setGraphic(cardContent);
                }
            }
        });
        listView.setPlaceholder(new Label("Tidak ada sesi dalam kategori ini."));
    }

    private void populateSesiData() {
        // asumsi kita sebagai siswa dengan id pengguna P1
        ObservableList<Sesi> sesiAkanDatangList = FXCollections.observableArrayList(
            siswaController.getSesiByIdSiswaAndStatus("P1", "AKAN DATANG")
        );
        lvSesiAkanDatang.setItems(sesiAkanDatangList);

        ObservableList<Sesi> sesiSelesaiList = FXCollections.observableArrayList(
            siswaController.getSesiByIdSiswaAndStatus("P1", "SELESAI")
        );
        lvSesiSelesai.setItems(sesiSelesaiList);
    }

    private void dialogPembatalan(Sesi sesi) {
        Jadwal jadwalTerkait = siswaController.getJadwalById(sesi.getIdJadwal());

        if (jadwalTerkait == null) {
            AlertHelper.showError("Gagal mendapatkan detail jadwal untuk sesi ini.");
            return;
        }

        try {
            LocalDate tanggalSesi = LocalDate.parse(jadwalTerkait.getTanggal());
            LocalTime jamSesi = LocalTime.parse(jadwalTerkait.getJamMulai());
            LocalDateTime waktuMulaiSesi = LocalDateTime.of(tanggalSesi, jamSesi);

            // Hitung selisih jam dari sekarang ke waktu mulai sesi
            long hoursUntilSession = Duration.between(LocalDateTime.now(), waktuMulaiSesi).toHours();

            if (hoursUntilSession < 12) {
                AlertHelper.showError("Gagal membatalkan sesi.\nSesi hanya dapat dibatalkan paling lambat \n12 jam sebelum dimulai");
                return;
            }
        } catch (Exception e) {
            AlertHelper.showError("Terjadi kesalahan saat memvalidasi waktu sesi");
            e.printStackTrace();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Konfirmasi Pembatalan Sesi");
        applyDialogStyles(dialog.getDialogPane());
        dialog.getDialogPane().setHeaderText(null);

        Label message = new Label("Yakin ingin membatalkan sesi ini?");
        message.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(message);

        ButtonType konfirmasiButton = new ButtonType("Konfirmasi", ButtonBar.ButtonData.OK_DONE);
        ButtonType batalButton = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(konfirmasiButton, batalButton);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == konfirmasiButton) {
            
            boolean success = siswaController.batalkanSesi(sesi.getIdSesi());
            
            if (success) {
                AlertHelper.showInfo("Pembatalan sesi berhasil");
                populateSesiData();
            } else {
                AlertHelper.showError("Terjadi kesalahan saat membatalkan sesi di database");
            }
        }
    }

    private void dialogJadwalUlangSesi(Sesi sesi) {
        Jadwal jadwalLama = siswaController.getJadwalById(sesi.getIdJadwal());

        if (jadwalLama == null) {
            AlertHelper.showError("Gagal mendapatkan detail jadwal untuk sesi ini");
            return;
        }

        try {
            LocalDate tanggalSesi = LocalDate.parse(jadwalLama.getTanggal());
            LocalTime jamSesi = LocalTime.parse(jadwalLama.getJamMulai());
            LocalDateTime waktuMulaiSesi = LocalDateTime.of(tanggalSesi, jamSesi);

            long hoursUntilSession = Duration.between(LocalDateTime.now(), waktuMulaiSesi).toHours();

            if (hoursUntilSession < 12) {
                AlertHelper.showError("Gagal menjadwal ulang.\nSesi hanya dapat dijadwal ulang diubah paling lambat \n12 jam sebelum dimulai");
                return;
            }
        } catch (Exception e) {
            AlertHelper.showError("Terjadi kesalahan saat memvalidasi waktu sesi");
            e.printStackTrace();
            return;
        }

        Dialog<Jadwal> dialog = new Dialog<>();
        dialog.setTitle("Jadwal Ulang Sesi");
        dialog.setHeaderText("Pilih jadwal baru yang tersedia untuk tutor ini.");
        applyDialogStyles(dialog.getDialogPane());

        Tutor tutor = siswaController.getTutorById(sesi.getIdTutor());
        tutor.setJadwal(siswaController.getJadwalByTutorId(sesi.getIdTutor()));
        
        if (tutor == null || tutor.getJadwal() == null || tutor.getJadwal().isEmpty()) {
            AlertHelper.showError("Tidak ada jadwal lain yang tersedia untuk tutor ini.");
            return;
        }
        
        List<Jadwal> jadwalTersedia = tutor.getJadwal().stream()
                .filter(j -> !j.getIdJadwal().equals(sesi.getIdJadwal()))
                .collect(Collectors.toList());

        if (jadwalTersedia.isEmpty()) {
            AlertHelper.showError("Tidak ada jadwal lain yang tersedia untuk tutor ini.");
            return;
        }

        ComboBox<Jadwal> cbJadwalBaru = new ComboBox<>(FXCollections.observableArrayList(jadwalTersedia));
        cbJadwalBaru.setPrefWidth(350);
        cbJadwalBaru.setPromptText("Pilih jadwal baru...");

        String formatJadwal = "%s, %s - %s";
        cbJadwalBaru.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Jadwal jadwal, boolean empty) {
                super.updateItem(jadwal, empty);
                setText(empty || jadwal == null ? null : String.format(formatJadwal, siswaController.getHariTanggalJadwal(jadwal.getIdJadwal()), jadwal.getJamMulai(), jadwal.getJamSelesai()));
            }
        });

        cbJadwalBaru.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Jadwal jadwal, boolean empty) {
                super.updateItem(jadwal, empty);
                setText(empty || jadwal == null ? null : String.format(formatJadwal, siswaController.getHariTanggalJadwal(jadwal.getIdJadwal()), jadwal.getJamMulai(), jadwal.getJamSelesai()));
            }
        });

        VBox content = new VBox(10, new Label("Pilih Jadwal Baru:"), cbJadwalBaru);
        content.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(content);

        ButtonType lanjutkanButtonType = new ButtonType("Lanjutkan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(lanjutkanButtonType, ButtonType.CANCEL);

        Node lanjutkanButtonNode = dialog.getDialogPane().lookupButton(lanjutkanButtonType);
        lanjutkanButtonNode.setDisable(true);
        cbJadwalBaru.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            lanjutkanButtonNode.setDisable(newVal == null);
        });

        dialog.setResultConverter(dialogButton -> (dialogButton == lanjutkanButtonType) ? cbJadwalBaru.getValue() : null);

        Optional<Jadwal> result = dialog.showAndWait();
        result.ifPresent(jadwalBaru -> {
            dialogKonfirmasiJadwalUlang(sesi, jadwalBaru);
        });
    }

    private void dialogKonfirmasiJadwalUlang(Sesi sesi, Jadwal jadwalBaru) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Konfirmasi Penjadwalan Ulang Sesi");
        applyDialogStyles(dialog.getDialogPane());
        dialog.getDialogPane().setHeaderText(null);

        String infoJadwalBaru = siswaController.getHariTanggalJadwal(jadwalBaru.getIdJadwal()) +
                                " pukul " + siswaController.getJamJadwal(jadwalBaru.getIdJadwal());
        Label message = new Label("Yakin ingin menjadwal ulang sesi ini ke jadwal baru?\n\n" + infoJadwalBaru);
        message.setStyle("-fx-text-alignment: center;");

        VBox content = new VBox(10, message);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER);
        dialog.getDialogPane().setContent(content);

        ButtonType konfirmasiButton = new ButtonType("Konfirmasi", ButtonBar.ButtonData.OK_DONE);
        ButtonType batalButton = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(konfirmasiButton, batalButton);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == konfirmasiButton) {
            boolean success = siswaController.jadwalUlangSesi(sesi.getIdSesi(), jadwalBaru.getIdJadwal());
            
            if (success) {
                AlertHelper.showInfo("Penjadwalan ulang sesi berhasil.");
                populateSesiData();
            } else {
                AlertHelper.showError("Gagal menjadwal ulang sesi pada tahap akhir.");
            }
        } else {
            dialogJadwalUlangSesi(sesi);
        }
    }
    
    private void dialogDetailSesi(Sesi sesi) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detail Sesi");
        applyDialogStyles(dialog.getDialogPane());

        VBox mainDialogContent = new VBox(15);
        mainDialogContent.setPadding(new Insets(20));
        mainDialogContent.setPrefWidth(500);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabelDialog = new Label("Detail Sesi");
        titleLabelDialog.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #333333;");
        VBox headerContent = new VBox(5);
        headerContent.getChildren().addAll(titleLabelDialog);

        HBox bodyContent = new HBox(30);

        VBox leftInfoPane = new VBox(10);
        leftInfoPane.setAlignment(Pos.TOP_LEFT);

        String tutorName = siswaController.getTutorById(sesi.getIdTutor()).getNama(); 
        String mataPelajaran = siswaController.getJadwalById(sesi.getIdJadwal()).getMataPelajaran(); 
        String hariTanggal = siswaController.getHariTanggalJadwal(sesi.getIdJadwal()); 
        String pukul = siswaController.getJamJadwal(sesi.getIdJadwal()); 

        Label tutorLabel = new Label("Tutor: " + tutorName);
        tutorLabel.setStyle("-fx-font-size: 16px;");
        Label mapelLabel = new Label("Mata Pelajaran: " + mataPelajaran);
        mapelLabel.setStyle("-fx-font-size: 16px;");
        Label hariTanggalLabel = new Label("Hari, Tanggal: " + hariTanggal);
        hariTanggalLabel.setStyle("-fx-font-size: 16px;");
        Label pukulLabel = new Label("Pukul: " + pukul);
        pukulLabel.setStyle("-fx-font-size: 16px;");

        leftInfoPane.getChildren().addAll(tutorLabel, mapelLabel, hariTanggalLabel, pukulLabel);

        VBox rightInfoPane = new VBox(10);
        rightInfoPane.setAlignment(Pos.TOP_LEFT);

        String ratingValue = "4.9"; // asumsi rating sesi
        String ulasanText = "Gacor Banget"; // asumsi ulasan sesi

        HBox ratingDisplay = new HBox(5);
        ratingDisplay.setAlignment(Pos.CENTER_LEFT);
        Label starEmoji = new Label("⭐");
        starEmoji.setStyle("-fx-font-size: 20px;");
        Label ratingText = new Label(ratingValue);
        ratingText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        ratingDisplay.getChildren().addAll(starEmoji, ratingText);

        Label ulasanTitleLabel = new Label("Ulasan:");
        ulasanTitleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label ulasanValueLabel = new Label(ulasanText);
        ulasanValueLabel.setStyle("-fx-font-size: 16px;");
        ulasanValueLabel.setWrapText(true);

        rightInfoPane.getChildren().addAll(ratingDisplay, ulasanTitleLabel, ulasanValueLabel);
        
        HBox.setHgrow(leftInfoPane, Priority.ALWAYS); 
        bodyContent.getChildren().addAll(leftInfoPane, rightInfoPane);

        mainDialogContent.getChildren().addAll(headerContent, new Separator(), bodyContent);
        dialog.getDialogPane().setContent(mainDialogContent);
        dialog.getDialogPane().getButtonTypes().clear();

        Window window = dialog.getDialogPane().getScene().getWindow();
        if (window != null) {
            window.setOnCloseRequest(event -> {
            });
        } else {
             System.out.println("Debug: Dialog window is null before showAndWait");
        }

        dialog.showAndWait();
    }
    
    // Dialog Pemesanan
    private void dialogPemesananSesi(Tutor tutor, Jadwal jadwal) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Pesan Sesi");
        dialog.setHeaderText("Konfirmasi Pemesanan Sesi");
        applyDialogStyles(dialog.getDialogPane());


        ButtonType pesanButtonType = new ButtonType("Pesan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(pesanButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Tutor:"), 0, 0);
        grid.add(new Label(tutor.getNama()), 1, 0);

        grid.add(new Label("Mata Pelajaran:"), 0, 1);
        grid.add(new Label(jadwal.getMataPelajaran()), 1, 1);

        String tanggalLengkap = jadwal.getTanggal();
        LocalDate date = LocalDate.parse(tanggalLengkap);
        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, new Locale("id","ID"));
        String hariFormatted = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("id","ID"));


        grid.add(new Label("Hari/Tanggal:"), 0, 2);
        grid.add(new Label(hariFormatted + ", " + date.getDayOfMonth() + " " + monthName + " " + date.getYear()), 1, 2);

        grid.add(new Label("Waktu:"), 0, 3);
        grid.add(new Label(jadwal.getJamMulai() + " - " + jadwal.getJamSelesai()), 1, 3);

        Label metodePembayaranLabel = new Label("Metode Pembayaran:");
        ComboBox<String> cbMetodePembayaran = new ComboBox<>();
        cbMetodePembayaran.getItems().addAll("Transfer Bank", "Gopay", "OVO");
        cbMetodePembayaran.setPromptText("Pilih metode pembayaran");
        cbMetodePembayaran.setPrefWidth(200);
        grid.add(metodePembayaranLabel, 0, 4);
        grid.add(cbMetodePembayaran, 1, 4);
        
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogBtn -> {
            if (dialogBtn == pesanButtonType) {
                if (cbMetodePembayaran.getValue() == null || cbMetodePembayaran.getValue().isEmpty()) {
                    AlertHelper.showError("Metode pembayaran harus dipilih.");
                    return null; 
                }
                return dialogBtn;
            }
            return null;
        });

        Optional<ButtonType> result = dialog.showAndWait();
        result.ifPresent(buttonType -> {
            if (buttonType == pesanButtonType) {
                dialogKonfirmasiPembayaran(tutor, jadwal, cbMetodePembayaran.getValue());
            }
        });
    }

    private void dialogKonfirmasiPembayaran(Tutor tutor, Jadwal jadwal, String paymentMethod) {
        Dialog<ButtonType> confirmationDialog = new Dialog<>();
        confirmationDialog.setTitle("Konfirmasi Pembayaran");
        confirmationDialog.setHeaderText("Pembayaran Pemesanan Sesi");
        applyDialogStyles(confirmationDialog.getDialogPane());

        ButtonType bayarButtonType = new ButtonType("Bayar", ButtonBar.ButtonData.OK_DONE);
        ButtonType batalButtonType = new ButtonType("Batal", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmationDialog.getDialogPane().getButtonTypes().addAll(bayarButtonType, batalButtonType);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);

        // Dummy price for now
        Label hargaLabel = new Label("Total Harga: Rp. 100.000"); // TODO: Get actual price
        Label metodeLabel = new Label("Metode Pembayaran: " + paymentMethod);
        hargaLabel.setStyle("-fx-font-size: 14px;");
        metodeLabel.setStyle("-fx-font-size: 14px;");

        content.getChildren().addAll(hargaLabel, metodeLabel);
        confirmationDialog.getDialogPane().setContent(content);

        Optional<ButtonType> result = confirmationDialog.showAndWait();
        result.ifPresent(buttonType -> {
            if (buttonType == bayarButtonType) {
                dialogBuktiPembayaran(tutor, jadwal, paymentMethod);
            } else if (buttonType == batalButtonType) {
                dialogPemesananSesi(tutor, jadwal); 
            }
        });
    }


    private void dialogBuktiPembayaran(Tutor tutor, Jadwal jadwal, String paymentMethod) {
        Dialog<ButtonType> uploadDialog = new Dialog<>();
        uploadDialog.setTitle("Unggah Bukti Pembayaran");
        uploadDialog.setHeaderText("Bukti Pembayaran");
        applyDialogStyles(uploadDialog.getDialogPane());

        ButtonType lanjutkanButtonType = new ButtonType("Lanjutkan", ButtonBar.ButtonData.OK_DONE);
        uploadDialog.getDialogPane().getButtonTypes().addAll(lanjutkanButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.CENTER_LEFT);

        Label instructionLabel = new Label("Silakan masukkan bukti pembayaran:");
        instructionLabel.setStyle("-fx-font-size: 14px;");

        HBox fileChooserBox = new HBox(10);
        fileChooserBox.setAlignment(Pos.CENTER_LEFT);
        Button btnPilihFile = new Button("Pilih File");
        Label lblNamaFile = new Label("Tidak ada file dipilih.");
        lblNamaFile.setStyle("-fx-font-style: italic;");
        selectedProofFile = null; 

        btnPilihFile.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Pilih Bukti Pembayaran");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null) {
                selectedProofFile = file;
                lblNamaFile.setText(file.getName());
                lblNamaFile.setStyle(""); 
            } else {
                selectedProofFile = null;
                lblNamaFile.setText("Tidak ada file dipilih.");
                lblNamaFile.setStyle("-fx-font-style: italic;");
            }
        });
        
        Label paperclipIcon = new Label("📎"); 
        paperclipIcon.setStyle("-fx-font-size: 18px;");
        fileChooserBox.getChildren().addAll(paperclipIcon, btnPilihFile, lblNamaFile);
        
        content.getChildren().addAll(instructionLabel, fileChooserBox);
        uploadDialog.getDialogPane().setContent(content);

        Node lanjutkanButton = uploadDialog.getDialogPane().lookupButton(lanjutkanButtonType);
        lanjutkanButton.setDisable(true); 

        lblNamaFile.textProperty().addListener((obs, oldText, newText) -> {
            lanjutkanButton.setDisable(selectedProofFile == null);
        });


        Optional<ButtonType> result = uploadDialog.showAndWait();
        result.ifPresent(buttonType -> {
            if (buttonType == lanjutkanButtonType) {
                if (selectedProofFile != null) {
                    // asumsi kita sebagai siswa dengan id P1 dan total pembayaran 100.000
                    siswaController.pesanSesi("P1", tutor.getIdPengguna(), jadwal.getIdJadwal(), paymentMethod, 100000);
                    AlertHelper.showInfo("Pemesanan Sesi Berhasil!");
                } else {
                    AlertHelper.showError("Silakan pilih file bukti pembayaran."); 
                }
            }
        });
    }
    
    private void applyDialogStyles(DialogPane dialogPane) {
        dialogPane.setStyle("-fx-font-size: 13px;"); 
    }
}