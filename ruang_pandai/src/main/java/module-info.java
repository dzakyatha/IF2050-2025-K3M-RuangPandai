module com.ruang_pandai {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;

    opens com.ruang_pandai to javafx.fxml;
    exports com.ruang_pandai;
}
