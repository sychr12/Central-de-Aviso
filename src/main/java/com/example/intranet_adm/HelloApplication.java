package com.example.intranet_adm;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(CentralAvisosView.criar(stage), 1180, 720);
        scene.getStylesheets().add(HelloApplication.class.getResource("style.css").toExternalForm());

        stage.setTitle("Central de Avisos - Enviar Popup");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
