package com.javamessenger.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Java Messenger Client entry point.
 * <p>
 * Run with: {@code mvn javafx:run} (see pom.xml), or run this class's
 * {@code main} method directly from an IDE that is configured with the
 * JavaFX SDK on its module path.
 */
public class MainApp extends Application {

    public static final String HOST = "localhost";
    public static final int PORT = 5555;

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        stage.setTitle("Java Messenger");
        stage.setResizable(true);
        showLogin();
        stage.show();
    }

    public static void showLogin() throws IOException {
        switchScene("/fxml/login.fxml", 400, 520);
    }

    public static void showRegister() throws IOException {
        switchScene("/fxml/register.fxml", 400, 580);
    }

    public static void showMessenger() throws IOException {
        switchScene("/fxml/messenger.fxml", 960, 640);
    }

    private static void switchScene(String fxmlPath, double width, double height) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
        Parent root = loader.load();
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(MainApp.class.getResource("/css/style.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
