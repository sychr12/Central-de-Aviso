/// Olá! Este é o ponto de entrada da aplicação.
/// Ele inicia o JavaFX e abre a tela principal.
/// Este arquivo depende das telas e configurações do projeto.
/// Se a estrutura inicial da aplicação mudar, este arquivo também pode precisar ser atualizado. =)

package com.example.intranet_adm;

import com.example.intranet_adm.view.CentralAvisosView;

import io.github.palexdev.materialfx.theming.JavaFXThemes;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import io.github.palexdev.materialfx.theming.UserAgentBuilder;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) {

        // Registra o tema base do MaterialFX (necessário a partir da 11.14.0:
        // sem isso, MFXButton/MFXTextField/MFXDatePicker/MFXCheckbox etc.
        // renderizam sem nenhum estilo, mesmo o padrão da biblioteca).
        // Precisa rodar uma única vez, antes de criar a primeira Scene.
        UserAgentBuilder.builder()
                .themes(JavaFXThemes.MODENA)
                .themes(MaterialFXStylesheets.forAssemble(true))
                .setDeploy(true)
                .setResolveAssets(true)
                .build()
                .setGlobal();

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