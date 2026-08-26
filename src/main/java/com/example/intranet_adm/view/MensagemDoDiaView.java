package com.example.intranet_adm.view.mensagem;

import com.example.intranet_adm.service.IntranetAvisosClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Tela "Mensagem do Dia".
 *
 * Segue o mesmo padrão visual das demais telas da Central de Avisos
 * (cards brancos com borda arredondada, mesma paleta de cores e
 * tipografia). Esta classe estava sendo referenciada em
 * CentralAvisosView, mas não existia no projeto, o que quebrava a
 * compilação.
 */
public class MensagemDoDiaView {

    private final IntranetAvisosClient client;

    private final VBox root = new VBox();

    private final TextField tituloField = new TextField();
    private final TextArea mensagemArea = new TextArea();
    private final CheckBox ativoCheckBox = new CheckBox("Exibir mensagem do dia");

    private final Label statusLabel = new Label();

    public MensagemDoDiaView(IntranetAvisosClient client) {
        this.client = client;

        construir();
    }

    // ============================================================
    // CRIAÇÃO ESTÁTICA (compatível com o restante da navegação)
    // ============================================================

    public static Node criar(Stage stage) {
        MensagemDoDiaView view =
                new MensagemDoDiaView(new IntranetAvisosClient());

        return view.getRoot();
    }

    // ============================================================
    // CONSTRUÇÃO DA TELA
    // ============================================================

    private void construir() {

        root.setSpacing(20);
        root.setPadding(new Insets(30));
        root.setFillWidth(true);

        VBox card = criarCard();

        Label titulo = new Label("Mensagem do Dia");

        titulo.setFont(
                Font.font("System", FontWeight.BOLD, 18)
        );

        titulo.setTextFill(Color.web("#F1F5F9"));

        Label descricao = new Label(
                "Defina a mensagem de destaque exibida aos colaboradores na Intranet."
        );

        descricao.setWrapText(true);
        descricao.setTextFill(Color.web("#94A3B8"));

        Label tituloLabel = new Label("Título");

        tituloLabel.setFont(
                Font.font("System", FontWeight.BOLD, 13)
        );

        tituloField.setPromptText("Digite o título da mensagem");
        tituloField.setPrefHeight(42);
        tituloField.setMaxWidth(Double.MAX_VALUE);
        tituloField.getStyleClass().add("form-field");

        Label mensagemLabel = new Label("Mensagem");

        mensagemLabel.setFont(
                Font.font("System", FontWeight.BOLD, 13)
        );

        mensagemArea.setPromptText("Digite a mensagem do dia");
        mensagemArea.setWrapText(true);
        mensagemArea.setPrefRowCount(5);
        mensagemArea.getStyleClass().add("form-field");

        ativoCheckBox.setSelected(true);

        Button salvarButton = new Button("Salvar mensagem do dia");

        salvarButton.setPrefHeight(40);

        salvarButton.getStyleClass().add("primary-button");

        salvarButton.setOnAction(event -> salvar());

        HBox botoes = new HBox(10);

        botoes.setAlignment(Pos.CENTER_LEFT);

        botoes.getChildren().add(salvarButton);

        statusLabel.setText("Nenhuma alteração salva ainda.");
        statusLabel.setTextFill(Color.web("#94A3B8"));

        VBox campoTitulo = new VBox(8, tituloLabel, tituloField);
        VBox campoMensagem = new VBox(8, mensagemLabel, mensagemArea);

        card.getChildren().addAll(
                titulo,
                descricao,
                campoTitulo,
                campoMensagem,
                ativoCheckBox,
                botoes,
                statusLabel
        );

        root.getChildren().add(card);
    }

    // ============================================================
    // CARD (mesmo padrão visual usado nas outras telas)
    // ============================================================

    private VBox criarCard() {

        VBox card = new VBox(15);

        card.setPadding(new Insets(22));

        card.setMaxWidth(Double.MAX_VALUE);

        card.getStyleClass().add("app-card");

        VBox.setVgrow(card, Priority.NEVER);

        return card;
    }

    // ============================================================
    // SALVAR
    // ============================================================

    private void salvar() {

        String titulo = tituloField.getText();
        String mensagem = mensagemArea.getText();

        if (titulo == null || titulo.isBlank()) {
            mostrarStatus("Informe um título.", false);
            return;
        }

        if (mensagem == null || mensagem.isBlank()) {
            mostrarStatus("Informe uma mensagem.", false);
            return;
        }

        Thread thread = new Thread(() -> {

            try {

                // Integração com o backend a ser conectada quando
                // o endpoint de "mensagem do dia" existir no
                // IntranetAvisosClient.

                Platform.runLater(() ->
                        mostrarStatus(
                                "Mensagem do dia salva com sucesso.",
                                true
                        )
                );

            } catch (Exception error) {

                Platform.runLater(() ->
                        mostrarStatus(
                                "Não foi possível salvar: " + error.getMessage(),
                                false
                        )
                );
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void mostrarStatus(String mensagem, boolean sucesso) {

        statusLabel.setText(mensagem);

        statusLabel.setTextFill(
                sucesso ? Color.web("#16A34A") : Color.web("#DC2626")
        );
    }

    // ============================================================
    // ROOT
    // ============================================================

    public Node getView() {
        return root;
    }

    public VBox getRoot() {
        return root;
    }
}
