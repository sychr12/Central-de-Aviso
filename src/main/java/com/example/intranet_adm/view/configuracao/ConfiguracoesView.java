package com.example.intranet_adm.view.configuracao;

import com.example.intranet_adm.service.IntranetAvisosClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.function.Consumer;

public class ConfiguracoesView {

    private final IntranetAvisosClient client;

    private final VBox root = new VBox();

    private final TextField baseUrlField = new TextField();
    private final Label statusLabel = new Label();

    private Consumer<String> onMessage;

    public ConfiguracoesView(IntranetAvisosClient client) {
        this.client = client;

        construir();
    }

    // ============================================================
    // CONSTRUÇÃO DA TELA
    // ============================================================

    private void construir() {

        root.setSpacing(20);
        root.setPadding(new Insets(0));
        root.setFillWidth(true);

        Label titulo = criarTitulo();

        Label descricao = new Label(
                "Configure a conexão da Central de Avisos com a Intranet-IDAM."
        );

        descricao.setFont(Font.font("System", 14));
        descricao.setTextFill(Color.web("#94A3B8"));

        VBox conexaoCard = criarCardConexao();
        VBox sistemaCard = criarCardSistema();

        root.getChildren().addAll(
                titulo,
                descricao,
                conexaoCard,
                sistemaCard
        );
    }

    // ============================================================
    // TÍTULO
    // ============================================================

    private Label criarTitulo() {

        Label titulo = new Label("Configurações");

        titulo.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        28
                )
        );

        titulo.setTextFill(Color.web("#F1F5F9"));

        return titulo;
    }

    // ============================================================
    // CARD DE CONEXÃO
    // ============================================================

    private VBox criarCardConexao() {

        VBox card = criarCard();

        Label titulo = new Label("Conexão com a Intranet-IDAM");

        titulo.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        18
                )
        );

        titulo.setTextFill(Color.web("#F1F5F9"));

        Label descricao = new Label(
                "Defina o endereço utilizado pela Central de Avisos para "
                        + "se comunicar com o servidor."
        );

        descricao.setWrapText(true);
        descricao.setTextFill(Color.web("#94A3B8"));

        Label urlLabel = new Label("URL da Intranet-IDAM");

        urlLabel.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        13
                )
        );

        baseUrlField.setPromptText("http://localhost:3000");

        baseUrlField.setPrefHeight(42);

        baseUrlField.setMaxWidth(Double.MAX_VALUE);
        baseUrlField.getStyleClass().add("form-field");

        try {
            baseUrlField.setText(
                    IntranetAvisosClient.baseUrl()
            );
        } catch (Exception ignored) {
            baseUrlField.setText(
                    "http://localhost:3000"
            );
        }

        Button testarButton = new Button("Testar conexão");

        testarButton.setPrefHeight(40);
        testarButton.getStyleClass().add("secondary-button");

        testarButton.setOnAction(event ->
                testarConexao()
        );

        Button salvarButton = new Button("Salvar configuração");

        salvarButton.setPrefHeight(40);
        salvarButton.getStyleClass().add("primary-button");

        salvarButton.setOnAction(event ->
                salvarConfiguracao()
        );

        HBox botoes = new HBox(10);

        botoes.setAlignment(Pos.CENTER_LEFT);

        botoes.getChildren().addAll(
                testarButton,
                salvarButton
        );

        statusLabel.setText("Conexão não testada.");

        statusLabel.setTextFill(
                Color.web("#94A3B8")
        );

        VBox campo = new VBox(8);

        campo.getChildren().addAll(
                urlLabel,
                baseUrlField
        );

        card.getChildren().addAll(
                titulo,
                descricao,
                campo,
                botoes,
                statusLabel
        );

        return card;
    }

    // ============================================================
    // CARD DO SISTEMA
    // ============================================================

    private VBox criarCardSistema() {

        VBox card = criarCard();

        Label titulo = new Label("Informações do sistema");

        titulo.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        18
                )
        );

        titulo.setTextFill(
                Color.web("#F1F5F9")
        );

        Label aplicacao = criarInformacao(
                "Aplicação",
                "Central de Avisos / Intranet-IDAM"
        );

        Label tecnologia = criarInformacao(
                "Tecnologia",
                "JavaFX"
        );

        Label servidor = criarInformacao(
                "Servidor padrão",
                "http://localhost:3000"
        );

        Label observacao = new Label(
                "As configurações são armazenadas localmente "
                        + "para serem utilizadas nas próximas inicializações."
        );

        observacao.setWrapText(true);

        observacao.setTextFill(
                Color.web("#94A3B8")
        );

        card.getChildren().addAll(
                titulo,
                aplicacao,
                tecnologia,
                servidor,
                observacao
        );

        return card;
    }

    // ============================================================
    // INFORMAÇÃO
    // ============================================================

    private Label criarInformacao(
            String nome,
            String valor
    ) {

        Label label = new Label(
                nome + ": " + valor
        );

        label.setFont(
                Font.font(
                        "System",
                        14
                )
        );

        label.setTextFill(
                Color.web("#CBD5E1")
        );

        return label;
    }

    // ============================================================
    // CARD
    // ============================================================

    private VBox criarCard() {

        VBox card = new VBox(15);

        card.setPadding(
                new Insets(22)
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.getStyleClass().add("app-card");

        VBox.setVgrow(
                card,
                Priority.NEVER
        );

        return card;
    }

    // ============================================================
    // SALVAR CONFIGURAÇÃO
    // ============================================================

    private void salvarConfiguracao() {

        String url = baseUrlField.getText();

        if (url == null || url.isBlank()) {

            mostrarStatus(
                    "Informe uma URL válida.",
                    false
            );

            return;
        }

        try {

            IntranetAvisosClient.configurarBaseUrl(
                    url.trim()
            );

            mostrarStatus(
                    "Configuração salva com sucesso.",
                    true
            );

            enviarMensagem(
                    "URL da Intranet-IDAM atualizada."
            );

        } catch (Exception error) {

            mostrarStatus(
                    "Não foi possível salvar: "
                            + error.getMessage(),
                    false
            );
        }
    }

    // ============================================================
    // TESTAR CONEXÃO
    // ============================================================

    private void testarConexao() {

        String url = baseUrlField.getText();

        if (url == null || url.isBlank()) {

            mostrarStatus(
                    "Informe uma URL antes de testar.",
                    false
            );

            return;
        }

        try {

            IntranetAvisosClient.configurarBaseUrl(
                    url.trim()
            );

            statusLabel.setText(
                    "Testando conexão..."
            );

            statusLabel.setTextFill(
                Color.web("#94A3B8")
            );

            String status =
                    client.checkServerStatus();

            if ("online".equalsIgnoreCase(status)) {

                mostrarStatus(
                        "Servidor online e acessível.",
                        true
                );

            } else {

                mostrarStatus(
                        "Servidor offline: " + status,
                        false
                );
            }

        } catch (Exception error) {

            mostrarStatus(
                    "Erro ao testar conexão: "
                            + error.getMessage(),
                    false
            );
        }
    }

    // ============================================================
    // STATUS
    // ============================================================

    private void mostrarStatus(
            String mensagem,
            boolean sucesso
    ) {

        statusLabel.setText(mensagem);

        statusLabel.setTextFill(
                sucesso
                        ? Color.web("#16A34A")
                        : Color.web("#DC2626")
        );
    }

    // ============================================================
    // MENSAGEM PARA A VIEW PRINCIPAL
    // ============================================================

    private void enviarMensagem(String mensagem) {

        if (onMessage != null) {
            onMessage.accept(mensagem);
        }
    }

    public void setOnMessage(
            Consumer<String> onMessage
    ) {

        this.onMessage = onMessage;
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
