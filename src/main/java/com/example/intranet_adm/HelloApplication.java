/// Olá! Este é o ponto de entrada da aplicação.
/// Ele inicia o JavaFX e abre a tela principal.
/// Este arquivo depende das telas e configurações do projeto.
/// Se a estrutura inicial da aplicação mudar, este arquivo também pode precisar ser atualizado. =)

package com.example.intranet_adm;

import com.example.intranet_adm.view.CentralAvisosView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {
        // Usando a view programática (recomendado)
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