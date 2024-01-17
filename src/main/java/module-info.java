module com.example.gogreenredo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens com.example.gogreenredo to javafx.fxml;
    exports com.example.gogreenredo;
}