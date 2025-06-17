package com.example;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private Button exitButton;
    @FXML private Button minButton;
  
    @FXML private HBox titleBar;

    @FXML private ImageView exitIcon;
    @FXML private ImageView minIcon;
    
    @FXML private ImageView backgroundImage;

   
    private final double[] prevBounds = new double[4];

    @FXML
    private void initialize() {
        backgroundImage.setImage(new Image(getClass().getResourceAsStream("/com/example/background.png")));
        exitIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/x.png")));
        minIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/-.png")));
       

        // Enable drag after scene is loaded
        Platform.runLater(() -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            WindowControls.enableDrag(stage, titleBar);
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        try {
            if (UserDatabase.accountExists(username, password, "accounts_passenger.txt")) {
                loadPassengerHome(username);
            } else if (UserDatabase.accountExists(username, password, "accounts_rider.txt")) {
                loadRiderHome(username);
            } else {
                showAlert("Login Failed", "Invalid username or password.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load home screen.");
        }
    }

    private void loadPassengerHome(String username) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("passenger_home.fxml"));
        Scene scene = new Scene(loader.load());

        PassengerHomeController controller = loader.getController();
        controller.setUsername(username);

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private void loadRiderHome(String username) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("rider_home.fxml"));
        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void handleSignup() {
        SceneSwitcher.switchTo("signup.fxml", usernameField.getScene());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleExit() {
        WindowControls.exit();
    }

    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) minButton.getScene().getWindow();
        WindowControls.minimize(stage);
    }
}
