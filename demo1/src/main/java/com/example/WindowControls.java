package com.example;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.stage.Stage;

public class WindowControls {

    // Enable window dragging using a custom title bar
    public static void enableDrag(Stage stage, Node titleBar) {
        final double[] offset = new double[2];

        titleBar.setOnMousePressed(event -> {
            offset[0] = event.getSceneX();
            offset[1] = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - offset[0]);
            stage.setY(event.getScreenY() - offset[1]);
        });
    }

    // Minimize the window
    public static void minimize(Stage stage) {
        stage.setIconified(true);
    }


    // Exit the application
    public static void exit() {
        Platform.exit();
    }
}
