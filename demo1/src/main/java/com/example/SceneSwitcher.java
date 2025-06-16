package com.example;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneSwitcher {
    public static void switchTo(String fxmlFile, Scene currentScene) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneSwitcher.class.getResource("/com/example/" + fxmlFile));
            Parent root = loader.load();
            currentScene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
