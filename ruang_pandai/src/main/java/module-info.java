module com.ruang_pandai {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.ruang_pandai to javafx.fxml;
    exports com.ruang_pandai;
}
