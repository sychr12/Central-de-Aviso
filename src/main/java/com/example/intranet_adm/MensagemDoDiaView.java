package com.example.intranet_adm;

import com.example.intranet_adm.service.IntranetAvisosClient;
import javafx.application.Platform;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public final class MensagemDoDiaView {

    private MensagemDoDiaView() {
        // Impede a criação de objetos dessa classe
    }

    public static Node criar(Stage stage) {

        // =========================================================
        // CONTAINER PRINCIPAL
        // =========================================================

        VBox root = new VBox(20);

        root.setPadding(new Insets(26));

        root.getStyleClass().add("central-content");


        // =========================================================
        // TÍTULO
        // =========================================================

        Label titulo = new Label("Mensagem do Dia");

        titulo.getStyleClass().add("central-page-title");


        Label descricao = new Label(
                "Gerencie as mensagens utilizadas como mensagem do dia."
        );

        descricao.getStyleClass().add("muted");


        // =========================================================
        // MENSAGEM DE HOJE
        // =========================================================

        VBox mensagemAtualCard = new VBox(12);

        mensagemAtualCard.getStyleClass().add("form-card");


        Label tituloAtual = new Label(
                "Mensagem de hoje"
        );

        tituloAtual.getStyleClass().add("central-label");


        Label mensagemAtual = new Label(
                DailyMessages.getMessageOfTheDay()
        );

        mensagemAtual.setWrapText(true);

        mensagemAtual.setMaxWidth(Double.MAX_VALUE);

        mensagemAtual.getStyleClass().add("popup-title");


        mensagemAtualCard.getChildren().addAll(
                tituloAtual,
                mensagemAtual
        );


        // =========================================================
        // ADICIONAR NOVA MENSAGEM
        // =========================================================

        VBox adicionarCard = new VBox(12);

        adicionarCard.getStyleClass().add("form-card");


        Label adicionarTitulo = new Label(
                "Adicionar nova mensagem"
        );

        adicionarTitulo.getStyleClass().add("central-label");


        TextArea novaMensagem = new TextArea();

        novaMensagem.setPromptText(
                "Digite aqui a nova mensagem..."
        );

        novaMensagem.setWrapText(true);

        novaMensagem.setPrefRowCount(4);

        novaMensagem.setMaxWidth(Double.MAX_VALUE);


        Label feedback = new Label();

        feedback.getStyleClass().add("success-label");

        feedback.setWrapText(true);


        // =========================================================
        // LISTA DE MENSAGENS
        // =========================================================

        ListView<String> lista = new ListView<>();

        lista.setPrefHeight(300);

        lista.setMaxHeight(300);

        atualizarLista(lista);


        // =========================================================
        // BOTÃO ADICIONAR
        // =========================================================

        Button adicionar = new Button(
                "＋  Adicionar mensagem"
        );

        adicionar.getStyleClass().add("send-button");


        adicionar.setOnAction(event -> {

            String texto = novaMensagem.getText();

            if (texto == null || texto.isBlank()) {

                feedback.setText(
                        "Digite uma mensagem antes de adicionar."
                );

                return;
            }


            String mensagem = texto.trim();


            adicionar.setDisable(true);
            feedback.setText("Publicando na intranet...");

            Thread envio = new Thread(() -> {
                try {
                    new IntranetAvisosClient().atualizarMensagemDoDia(mensagem);
                    Platform.runLater(() -> {
                        DailyMessages.addMessage(mensagem);
                        novaMensagem.clear();
                        mensagemAtual.setText(mensagem);
                        atualizarLista(lista);
                        feedback.setText("Mensagem publicada na intranet com sucesso!");
                        adicionar.setDisable(false);
                    });
                } catch (Exception error) {
                    Platform.runLater(() -> {
                        feedback.setText("Não foi possível publicar: " + error.getMessage());
                        adicionar.setDisable(false);
                    });
                }
            }, "publicar-mensagem-do-dia");
            envio.setDaemon(true);
            envio.start();
        });


        adicionarCard.getChildren().addAll(
                adicionarTitulo,
                novaMensagem,
                adicionar,
                feedback
        );


        // =========================================================
        // CARD DA LISTA
        // =========================================================

        VBox listaCard = new VBox(12);

        listaCard.getStyleClass().add("form-card");


        Label listaTitulo = new Label(
                "Mensagens cadastradas"
        );

        listaTitulo.getStyleClass().add("central-label");


        // =========================================================
        // BOTÃO REMOVER
        // =========================================================

        Button remover = new Button(
                "🗑  Remover mensagem"
        );

        remover.getStyleClass().add("soft-button");


        remover.setOnAction(event -> {

            String selecionada =
                    lista.getSelectionModel()
                            .getSelectedItem();


            if (selecionada == null) {

                feedback.setText(
                        "Selecione uma mensagem para remover."
                );

                return;
            }


            boolean removida =
                    DailyMessages.removeMessage(
                            selecionada
                    );


            if (!removida) {

                feedback.setText(
                        "Não foi possível remover a mensagem."
                );

                return;
            }


            // Atualiza a lista
            atualizarLista(lista);


            // Atualiza mensagem atual
            mensagemAtual.setText(
                    DailyMessages.getMessageOfTheDay()
            );


            // Limpa seleção
            lista.getSelectionModel().clearSelection();


            // Feedback
            feedback.setText(
                    "✓ Mensagem removida com sucesso."
            );
        });


        // =========================================================
        // BOTÕES
        // =========================================================

        HBox botoes = new HBox(10);

        botoes.setAlignment(
                Pos.CENTER_RIGHT
        );

        botoes.getChildren().add(
                remover
        );


        // =========================================================
        // MONTA CARD DA LISTA
        // =========================================================

        listaCard.getChildren().addAll(
                listaTitulo,
                lista,
                botoes
        );


        // =========================================================
        // MONTA TELA
        // =========================================================

        root.getChildren().addAll(
                titulo,
                descricao,
                mensagemAtualCard,
                adicionarCard,
                listaCard
        );


        return root;
    }


    // =============================================================
    // ATUALIZA LISTA
    // =============================================================

    private static void atualizarLista(
            ListView<String> lista
    ) {

        lista.getItems().setAll(
                DailyMessages.getMessages()
        );
    }
}
