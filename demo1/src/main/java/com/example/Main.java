package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/login.fxml"));
        
        // Load the FXML
        Scene scene = new Scene(loader.load());

        // 💡 Make scene transparent so rounded corners can show
        scene.setFill(Color.TRANSPARENT);

        // 💡 Remove window decorations (title bar, border)
        stage.initStyle(StageStyle.TRANSPARENT);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
