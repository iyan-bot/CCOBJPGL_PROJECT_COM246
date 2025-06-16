package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class SignupController {

    @FXML private TextField usernameField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;

    @FXML private RadioButton passengerRadio;
    @FXML private RadioButton riderRadio;
    @FXML private ToggleGroup roleGroup;

    @FXML private Button exitButton;
    @FXML private Button minButton;
    @FXML private Button maxButton;
    @FXML private HBox titleBar;

    @FXML private ImageView exitIcon;
    @FXML private ImageView minIcon;
    @FXML private ImageView maxIcon;

    private final boolean[] isMaximized = { false };
    private final double[] prevBounds = new double[4];

    @FXML
    private void initialize() {
        passengerRadio.setToggleGroup(roleGroup);
        riderRadio.setToggleGroup(roleGroup);

        // Set icons for window control buttons
        exitIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/x.png")));
        minIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/-.png")));
        maxIcon.setImage(new Image(getClass().getResourceAsStream("/com/example/max.png")));

        // Enable dragging after the window loads
        Platform.runLater(() -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            WindowControls.enableDrag(stage, titleBar);
        });
    }

    @FXML
    private void handleSignUp() {
        String username = usernameField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || phone.isEmpty() || password.isEmpty() || roleGroup.getSelectedToggle() == null) {
            showAlert("Missing Information", "Please fill in all fields.");
            return;
        }

        String filePath = passengerRadio.isSelected() ? "accounts_passenger.txt" : "accounts_rider.txt";
        UserDatabase.registerAccount(username, phone, password, filePath);
        showAlert("Success", "Account registered successfully!");

        SceneSwitcher.switchTo("login.fxml", usernameField.getScene());
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
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

    @FXML
    private void handleMaximizeRestore() {
        Stage stage = (Stage) maxButton.getScene().getWindow();
        WindowControls.toggleMaximize(stage, isMaximized, prevBounds);
    }
}
