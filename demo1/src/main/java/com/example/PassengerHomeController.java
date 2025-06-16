package com.example;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PassengerHomeController {

    @FXML
    private Label welcomeLabel;

    private String username;

    public void setUsername(String username) {
        this.username = username;

        // Prevent NullPointerException in case welcomeLabel is not yet loaded
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + username + "!");
        }
    }

    @FXML
    private void initialize() {
        // You can put additional UI setup here if needed
        // This is automatically called after FXML is loaded
    }

    @FXML
    private void handleLogout() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
