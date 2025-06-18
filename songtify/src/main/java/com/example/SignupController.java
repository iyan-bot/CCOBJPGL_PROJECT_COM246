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

public class SignupController {
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button signupButton;

    @FXML
    void handleSignup(ActionEvent event) {
        String email = emailField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please fill all fields", Alert.AlertType.ERROR);
            return;
        }

        if (!email.contains("@")) {
            showAlert("Error", "Please enter a valid email", Alert.AlertType.ERROR);
            return;
        }

        try {
            if (User.userExists(username)) {
                showAlert("Error", "Username already exists", Alert.AlertType.ERROR);
                return;
            }

            // Save new user
            User newUser = new User(username, password);
            User.saveUser(newUser);

            showAlert("Success", "Account created successfully!", Alert.AlertType.INFORMATION);
            returnToLogin();
        } catch (IOException e) {
            showAlert("Error", "Failed to create account", Alert.AlertType.ERROR);
        }
    }

    private void returnToLogin() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Stage stage = (Stage) signupButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Spotify LAN - Login");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}