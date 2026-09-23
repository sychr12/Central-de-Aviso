package com.example.intranet_adm.view.configuracao;

import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.view.components.AppIcon;
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
        descricao.setTextFill(Color.web("#64748B"));

        VBox conexaoCard = criarCardConexao();
        VBox sistemaCard = criarCardSistema();

        root.getChildren().addAll(
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

        titulo.setTextFill(Color.web("#172B4D"));

        return titulo;
    }

    // ============================================================
    // CARD DE CONEXÃO
    // ============================================================

    private VBox criarCardConexao() {

        VBox card = criarCard();
        card.getStyleClass().add(\u0022settings-connection-card\u0022);

        Label titulo = new Label("Conexão com a Intranet-IDAM");

        titulo.setGraphic(AppIcon.create(AppIcon.Type.SERVER, 20));
        titulo.setGraphicTextGap(10);
        titulo.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        18
                )
        );

        titulo.setTextFill(Color.web("#172B4D"));

        Label descricao = new Label(
                "Defina o endereço utilizado pela Central de Avisos para "
                        + "se comunicar com o servidor."
        );

        descricao.setWrapText(true);
        descricao.setTextFill(Color.web("#64748B"));

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

        testarButton.setGraphic(AppIcon.create(AppIcon.Type.REFRESH, 16));
        testarButton.setOnAction(event ->
                testarConexao()
        );

        Button salvarButton = new Button("Salvar configuração");

        salvarButton.setPrefHeight(40);
        salvarButton.getStyleClass().add("primary-button");

        salvarButton.setGraphic(AppIcon.create(AppIcon.Type.SAVE, 16));
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
                Color.web("#64748B")
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
        card.getStyleClass().add(\u0022settings-system-card\u0022);

        Label titulo = new Label("Informações do sistema");

        titulo.setGraphic(AppIcon.create(AppIcon.Type.INFO, 19));
        titulo.setGraphicTextGap(10);
        titulo.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        18
                )
        );

        titulo.setTextFill(
                Color.web("#172B4D")
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
                Color.web("#64748B")
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
                Color.web("#334155")
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
        if (url == null || url.isBlank()) { mostrarStatus("Informe uma URL antes de testar.", false); return; }
        root.setDisable(true);
        statusLabel.setText("Testando conexão…");
        javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
            @Override protected String call() { return client.checkServerStatus(url.trim()); }
        };
        task.setOnSucceeded(event -> {
            root.setDisable(false);
            boolean online = "online".equals(task.getValue());
            mostrarStatus(online ? "Servidor acessível. Salve para usar este endereço." : "Não foi possível conectar: " + task.getValue(), online);
        });
        task.setOnFailed(event -> { root.setDisable(false); mostrarStatus("Falha ao testar conexão.", false); });
        Thread worker = new Thread(task, "testar-conexao");
        worker.setDaemon(true);
        worker.start();
    }

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
