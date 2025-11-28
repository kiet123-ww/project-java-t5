package com.tkbplanner;

import com.tkbplanner.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Entry point của ứng dụng TKB Planner Pro
 */
public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        MainWindow mainWindow = new MainWindow();
        mainWindow.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

