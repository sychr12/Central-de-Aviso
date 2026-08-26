package com.example.intranet_adm.view.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public final class CentralHeader {


    public Node criar() {

        HBox bar = new HBox();

        bar.setAlignment(
                Pos.CENTER_LEFT
        );

        bar.getStyleClass()
                .add("central-topbar");

        Label titulo =
                new Label(
                        "Central de Avisos - Enviar Popup"
                );

        titulo.getStyleClass()
                .add("window-title");

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        Label controles =
                new Label(
                        "—     □     ×"
                );

        bar.getChildren().addAll(
                new Label("🔔"),
                titulo,
                spacer,
                controles
        );

        return bar;
    }
}