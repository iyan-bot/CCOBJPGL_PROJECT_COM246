package com.example;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.stage.Screen;
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

    // Toggle maximize or restore the window
    public static void toggleMaximize(Stage stage, boolean[] isMaximized, double[] prevBounds) {
        if (!isMaximized[0]) {
            prevBounds[0] = stage.getX();
            prevBounds[1] = stage.getY();
            prevBounds[2] = stage.getWidth();
            prevBounds[3] = stage.getHeight();

            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
        } else {
            stage.setX(prevBounds[0]);
            stage.setY(prevBounds[1]);
            stage.setWidth(prevBounds[2]);
            stage.setHeight(prevBounds[3]);
        }

        isMaximized[0] = !isMaximized[0];
    }

    // Exit the application
    public static void exit() {
        Platform.exit();
    }
}
