package com.example;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Button switchToSignup;

    @FXML
    void handleLogin(ActionEvent event) {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Check credentials against user database
            for (User user : User.loadUsers()) {
                if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                    loadHomeScreen();
                    return;
                }
            }
            showAlert("Login Failed", "Invalid username or password", Alert.AlertType.ERROR);
        } catch (IOException e) { e.printStackTrace();
            showAlert("Error", "Failed to access user database", Alert.AlertType.ERROR);
        }
    }

    private void loadHomeScreen() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("home.fxml"));
        Stage stage = (Stage) loginUsername.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Spotify LAN - Home");
    }

    @FXML
    void switchToSignup(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("signup.fxml"));
            Stage stage = (Stage) switchToSignup.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Spotify LAN - Sign Up");
        } catch (IOException e) {
            showAlert("Error", "Failed to load signup page", Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}