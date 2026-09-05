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
import javafx.scene.control.Separator;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.paint.Paint;
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

    private final TextArea mensagemArea = new TextArea();
    private final CheckBox ativoCheckBox = new CheckBox("Exibir mensagem do dia");

    private final Label statusLabel = new Label();
    private final VBox listaMensagens = new VBox(8);
    private String mensagemEmEdicao;

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
        root.setPadding(new Insets(0));
        root.setFillWidth(true);

        VBox card = criarCard();

        Label titulo = new Label("Mensagem do Dia");

        titulo.setFont(
                Font.font("System", FontWeight.BOLD, 18)
        );

        titulo.setTextFill(Color.web("#172B4D"));

        Label descricao = new Label(
                "Defina a mensagem de destaque exibida aos colaboradores na Intranet."
        );

        descricao.setWrapText(true);
        descricao.setTextFill(Color.web("#64748B"));

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
        statusLabel.setTextFill(Color.web("#64748B"));

        VBox campoMensagem = new VBox(8, mensagemLabel, mensagemArea);

        card.getChildren().addAll(
                titulo,
                descricao,
                campoMensagem,
                ativoCheckBox,
                botoes,
                statusLabel,
                new Label("Mensagens salvas"),
                listaMensagens
        );

        root.getChildren().add(card);
        carregarLista();
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

        String mensagem = mensagemArea.getText();

        if (mensagem == null || mensagem.isBlank()) {
            mostrarStatus("Informe uma mensagem.", false);
            return;
        }

        boolean ativo = ativoCheckBox.isSelected();
        String conteudo = mensagem.trim();
        root.setDisable(true);
        statusLabel.setText("Salvando…");
        Thread thread = new Thread(() -> {

            try {

                if (mensagemEmEdicao == null) client.adicionarMensagemDoDia(conteudo);
                else { client.editarMensagemDoDia(mensagemEmEdicao, conteudo); mensagemEmEdicao = null; }

                Platform.runLater(() ->
                        mostrarStatus(
                                "Mensagem do dia salva com sucesso.",
                                true
                        )
                );
                Platform.runLater(this::carregarLista);

            } catch (Exception error) {

                Platform.runLater(() ->
                        mostrarStatus(
                                "Não foi possível salvar: " + error.getMessage(),
                                false
                        )
                );
            } finally {
                Platform.runLater(() -> root.setDisable(false));
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void carregarLista() {
        Thread thread = new Thread(() -> {
            try {
                List<String> mensagens = client.listarMensagensDoDia();
                Platform.runLater(() -> {
                    listaMensagens.getChildren().clear();
                    List<String> recentesPrimeiro = new ArrayList<>(mensagens);
                    java.util.Collections.reverse(recentesPrimeiro);
                    for (String item : recentesPrimeiro) {
                        Label texto = new Label(item); texto.setWrapText(true); texto.setMaxWidth(Double.MAX_VALUE); texto.setStyle("-fx-text-fill: #172B4D; -fx-font-size: 13px; -fx-font-weight: bold;"); HBox.setHgrow(texto, Priority.ALWAYS);
                        Button editar = new Button("Editar"); editar.getStyleClass().add("secondary-button"); editar.setOnAction(e -> { mensagemEmEdicao = item; mensagemArea.setText(item); });
                        Button excluir = new Button("Excluir"); excluir.getStyleClass().add("danger-button"); excluir.setOnAction(e -> excluirMensagem(item, excluir));
                        HBox linha = new HBox(12, texto, editar, excluir); linha.setAlignment(Pos.CENTER_LEFT); linha.setPadding(new Insets(12)); linha.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 10; -fx-border-color: #E2E8F0; -fx-border-radius: 10;");
                        listaMensagens.getChildren().add(linha);
                    }
                });
            } catch (Exception ignored) { }
        }); thread.setDaemon(true); thread.start();
    }

    private void excluirMensagem(String item, Button botao) {
        botao.setDisable(true);
        Thread thread = new Thread(() -> {
            try {
                client.excluirMensagemDoDia(item);
                Platform.runLater(this::carregarLista);
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarStatus("Não foi possível excluir: " + ex.getMessage(), false));
                Platform.runLater(() -> botao.setDisable(false));
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
