module com.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires okhttp3;
    requires com.google.gson;
    requires jdk.jsobject;

    opens com.example to javafx.fxml;
    exports com.example;
}
