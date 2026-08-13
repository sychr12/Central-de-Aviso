package com.example.intranet_adm;

import com.example.intranet_adm.model.Aviso;
import com.example.intranet_adm.service.AvisoService;
import com.example.intranet_adm.service.IntranetAvisosClient;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class CentralAvisosView {

    // ============================================================
    // CONSTANTES
    // ============================================================
    private static final int SIDEBAR_WIDTH = 230;
    private static final int CARD_SPACING = 16;
    private static final int FORM_SPACING = 10;
    private static final int IMAGE_PREVIEW_WIDTH = 360;
    private static final int IMAGE_PREVIEW_HEIGHT = 180;
    private static final int MONITOR_INTERVAL_SECONDS = 5;

    // ============================================================
    // SERVIÇOS E DEPENDÊNCIAS
    // ============================================================
    private final AvisoService service = new AvisoService();

    // ============================================================
    // COMPONENTES UI PRINCIPAIS
    // ============================================================
    private final BorderPane root = new BorderPane();
    private final VBox content = new VBox(CARD_SPACING);

    // ============================================================
    // COMPONENTES DO FORMULÁRIO
    // ============================================================
    private TextField tituloField;
    private TextArea mensagemArea;
    private ComboBox<String> prioridadeCombo;
    private TextField linkIntranetField;
    private Label feedbackLabel;
    private Button enviarButton;

    // ============================================================
    // COMPONENTES DE IMAGEM
    // ============================================================
    private Path imagemSelecionadaPath;
    private Label imagemNomeLabel;
    private ImageView imagemPreview;
    private Button removerImagemButton;

    // ============================================================
    // CONTROLE DE MONITORAMENTO
    // ============================================================
    private Timeline monitorTimeline;
    private Label monitorOnlineLabel;
    private Label monitorAcessosHojeLabel;
    private Label monitorTotalVisitantesLabel;
    private Label monitorUltimaConexaoLabel;
    private Label monitorStatusLabel;

    // ============================================================
    // MÉTODOS PÚBLICOS
    // ============================================================

    public static Parent criar(Stage stage) {
        return new CentralAvisosView().montar(stage);
    }

    // ============================================================
    // MONTAGEM PRINCIPAL
    // ============================================================

    private Parent montar(Stage stage) {
        configurarEstilos();
        configurarLayout(stage);
        novoAviso();
        return root;
    }

    private void configurarEstilos() {
        root.getStyleClass().add("central-root");
    }

    private void configurarLayout(Stage stage) {
        root.setLeft(criarMenu(stage));
        root.setTop(criarCabecalho());
        root.setCenter(criarScrollArea());
    }

    private ScrollPane criarScrollArea() {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("central-scroll");

        content.setPadding(new Insets(26));
        content.getStyleClass().add("central-content");

        return scroll;
    }

    // ============================================================
    // CABEÇALHO
    // ============================================================

    private Node criarCabecalho() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("central-topbar");

        Label titulo = new Label("Central de Avisos - Enviar Popup");
        titulo.getStyleClass().add("window-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(
                new Label("🔔"),
                titulo,
                spacer,
                new Label("—     □     ×")
        );

        return bar;
    }

    // ============================================================
    // MENU LATERAL
    // ============================================================

    private Node criarMenu(Stage stage) {
        VBox menu = new VBox(10);
        menu.setPrefWidth(SIDEBAR_WIDTH);
        menu.getStyleClass().add("central-sidebar");

        // Cabeçalho do menu
        menu.getChildren().addAll(
                criarLogo(),
                criarTituloMenu(),
                criarSubtituloMenu()
        );

        // Itens do menu
        menu.getChildren().addAll(
                criarNavButton("⊕   Novo Aviso", this::novoAviso, true),
                criarNavButton("◷   Histórico", this::historico, false),
                criarNavButton("👁   Acessando Agora", this::monitorAcessos, false),
                criarNavButton("☀   Mensagem do Dia", () -> abrirMensagemDoDia(stage), false),
                criarNavButton("⚙   Configurações", this::configuracoes, false)
        );

        // Espaçador
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        menu.getChildren().add(spacer);

        // Botão sair e status
        menu.getChildren().addAll(
                criarNavButton("⇥   Sair", stage::close, false),
                criarStatusLabel()
        );

        return menu;
    }

    private Label criarLogo() {
        Label logo = new Label("🔔");
        logo.getStyleClass().add("central-logo");
        return logo;
    }

    private Label criarTituloMenu() {
        Label titulo = new Label("Central de Avisos");
        titulo.getStyleClass().add("central-brand");
        return titulo;
    }

    private Label criarSubtituloMenu() {
        Label subtitulo = new Label("Popup para Intranet");
        subtitulo.getStyleClass().add("sidebar-subtitle");
        return subtitulo;
    }

    private Button criarNavButton(String texto, Runnable acao, boolean ativo) {
        Button button = new Button(texto);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add(ativo ? "central-nav-active" : "central-nav");
        button.setOnAction(e -> acao.run());
        return button;
    }

    private Label criarStatusLabel() {
        Label status = new Label("●  Configure o servidor\n\nVersão 1.0.0");
        status.getStyleClass().add("connection");
        return status;
    }

    // ============================================================
    // MÉTODOS AUXILIARES DE UI
    // ============================================================

    private void prepararTela(String titulo) {
        pararMonitor();
        content.getChildren().clear();

        Label cabecalho = new Label(titulo);
        cabecalho.getStyleClass().add("central-page-title");
        content.getChildren().add(cabecalho);
    }

    private Label criarLabel(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("central-label");
        return label;
    }

    private Button criarBotao(String texto, boolean primario) {
        Button button = new Button(texto);
        button.getStyleClass().add(primario ? "send-button" : "soft-button");
        return button;
    }

    private ColumnConstraints criarColuna(int porcentagem) {
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(porcentagem);
        return col;
    }

    // ============================================================
    // NOVO AVISO
    // ============================================================

    private void novoAviso() {
        prepararTela("Novo Aviso");

        VBox card = criarCardFormulario();
        GridPane grid = criarGridFormulario();

        // Configurar campos
        configurarCamposFormulario();

        // Adicionar todos os componentes ao grid
        adicionarCamposBasicos(grid);
        adicionarCampoImagem(grid);
        adicionarCampoLink(grid);
        adicionarCampoDestinatarios(grid);
        adicionarCampoDatas(grid);

        card.getChildren().add(grid);
        card.getChildren().add(criarAreaAcoes());

        content.getChildren().add(card);
    }

    private VBox criarCardFormulario() {
        VBox card = new VBox(CARD_SPACING);
        card.getStyleClass().add("form-card");
        return card;
    }

    private GridPane criarGridFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.getColumnConstraints().addAll(
                criarColuna(70),
                criarColuna(30)
        );
        return grid;
    }

    private void configurarCamposFormulario() {
        tituloField = new TextField("Manutenção Programada");

        prioridadeCombo = new ComboBox<>();
        prioridadeCombo.getItems().addAll(
                "🔴  Urgente",
                "🟠  Importante",
                "🔵  Normal"
        );
        prioridadeCombo.getSelectionModel().select(0);

        mensagemArea = new TextArea(
                "O sistema ficará indisponível hoje das 18h às 19h para manutenção.\n" +
                        "Agradecemos a compreensão!"
        );
        mensagemArea.setPrefRowCount(5);
        mensagemArea.setWrapText(true);

        linkIntranetField = new TextField(
                "https://intranet.empresa.com/comunicados/manutencao"
        );
    }

    private void adicionarCamposBasicos(GridPane grid) {
        int row = 0;

        grid.add(criarLabel("Título do Aviso"), 0, row);
        grid.add(criarLabel("Prioridade"), 1, row);
        grid.add(tituloField, 0, ++row);
        grid.add(prioridadeCombo, 1, row);

        grid.add(criarLabel("Mensagem do Aviso"), 0, ++row, 2, 1);
        grid.add(mensagemArea, 0, ++row, 2, 1);
    }

    private void adicionarCampoImagem(GridPane grid) {
        int row = grid.getRowCount();

        FileChooser imagemChooser = new FileChooser();
        imagemChooser.setTitle("Selecionar imagem do aviso");
        imagemChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp")
        );

        Button selecionarImagemButton = criarBotao("🖼  Selecionar imagem", false);
        imagemNomeLabel = new Label("Nenhuma imagem selecionada");
        imagemNomeLabel.getStyleClass().add("muted");

        removerImagemButton = criarBotao("✕", false);
        removerImagemButton.setVisible(false);
        removerImagemButton.setManaged(false);

        imagemPreview = new ImageView();
        imagemPreview.setFitWidth(IMAGE_PREVIEW_WIDTH);
        imagemPreview.setFitHeight(IMAGE_PREVIEW_HEIGHT);
        imagemPreview.setPreserveRatio(true);
        imagemPreview.setSmooth(true);
        imagemPreview.setVisible(false);
        imagemPreview.setManaged(false);

        configurarEventosImagem(selecionarImagemButton, imagemChooser);

        HBox imagemRow = new HBox(12, selecionarImagemButton, imagemNomeLabel, removerImagemButton);
        imagemRow.setAlignment(Pos.CENTER_LEFT);

        VBox imagemField = new VBox(12, imagemRow, imagemPreview);
        imagemField.getStyleClass().add("image-upload-field");

        grid.add(criarLabel("Imagem do aviso (opcional)"), 0, row, 2, 1);
        grid.add(imagemField, 0, ++row, 2, 1);
    }

    private void configurarEventosImagem(Button selecionarButton, FileChooser chooser) {
        selecionarButton.setOnAction(e -> {
            File arquivo = chooser.showOpenDialog(selecionarButton.getScene().getWindow());
            if (arquivo != null) {
                imagemSelecionadaPath = arquivo.toPath();
                imagemNomeLabel.setText(arquivo.getName());
                imagemPreview.setImage(new Image(arquivo.toURI().toString(),
                        IMAGE_PREVIEW_WIDTH, IMAGE_PREVIEW_HEIGHT, true, true));
                imagemPreview.setVisible(true);
                imagemPreview.setManaged(true);
                removerImagemButton.setVisible(true);
                removerImagemButton.setManaged(true);
            }
        });

        removerImagemButton.setOnAction(e -> {
            imagemSelecionadaPath = null;
            imagemNomeLabel.setText("Nenhuma imagem selecionada");
            imagemPreview.setImage(null);
            imagemPreview.setVisible(false);
            imagemPreview.setManaged(false);
            removerImagemButton.setVisible(false);
            removerImagemButton.setManaged(false);
        });
    }

    private void adicionarCampoLink(GridPane grid) {
        int row = grid.getRowCount();

        ComboBox<String> navegadorCombo = new ComboBox<>();
        navegadorCombo.getItems().addAll("Navegador padrão", "Google Chrome", "Microsoft Edge");
        navegadorCombo.getSelectionModel().selectFirst();

        grid.add(criarLabel("Link da Intranet"), 0, row);
        grid.add(criarLabel("Abrir no navegador"), 1, row);
        grid.add(linkIntranetField, 0, ++row);
        grid.add(navegadorCombo, 1, row);
    }

    private void adicionarCampoDestinatarios(GridPane grid) {
        int row = grid.getRowCount();

        ToggleGroup group = new ToggleGroup();

        RadioButton todos = new RadioButton("Todos os computadores");
        todos.setToggleGroup(group);
        todos.setSelected(true);

        RadioButton setores = new RadioButton("Setores específicos");
        setores.setToggleGroup(group);

        RadioButton grupos = new RadioButton("Grupos específicos");
        grupos.setToggleGroup(group);

        VBox destino = new VBox(7, criarLabel("Destinatários"), todos, setores, grupos);

        ComboBox<String> setorCombo = new ComboBox<>();
        setorCombo.setPromptText("Todos os setores");
        setorCombo.getItems().addAll("TI", "RH", "Financeiro", "Marketing");

        grid.add(destino, 0, row);
        grid.add(new VBox(7, criarLabel("Setor (quando aplicável)"), setorCombo), 1, row);
    }

    private void adicionarCampoDatas(GridPane grid) {
        int row = grid.getRowCount();

        HBox dates = new HBox(10,
                new TextField("20/05/2024"),
                new TextField("18:00"),
                new Label("   Expirar em (opcional)   "),
                new TextField("20/05/2024"),
                new TextField("19:00")
        );
        dates.getStyleClass().add("dates-row");

        grid.add(dates, 0, row, 2, 1);
    }

    private HBox criarAreaAcoes() {
        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button previewButton = criarBotao("◉  Visualizar Popup", false);
        previewButton.setOnAction(e -> visualizarPopup());

        enviarButton = criarBotao("➤  Enviar Aviso", true);
        enviarButton.setOnAction(e -> enviarAviso());

        feedbackLabel = new Label();
        feedbackLabel.getStyleClass().add("success-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        actions.getChildren().addAll(feedbackLabel, spacer, previewButton, enviarButton);
        return actions;
    }

    // ============================================================
    // ENVIO DE AVISO
    // ============================================================

    private void enviarAviso() {
        if (validarCampos()) {
            realizarEnvio();
        }
    }

    private boolean validarCampos() {
        if (tituloField.getText().isBlank() || mensagemArea.getText().isBlank()) {
            feedbackLabel.setText("Preencha título e mensagem.");
            return false;
        }
        return true;
    }

    private void realizarEnvio() {
        String titulo = tituloField.getText().trim();
        String mensagem = mensagemArea.getText().trim();
        String link = linkIntranetField.getText().trim();
        String nivel = determinarPrioridade();
        Path imagem = imagemSelecionadaPath;

        enviarButton.setDisable(true);
        feedbackLabel.setText("Enviando para a Intranet-IDAM...");

        Thread envio = new Thread(() -> {
            try {
                new IntranetAvisosClient().enviar(titulo, mensagem, nivel, link, imagem);
                Platform.runLater(() -> {
                    service.adicionar(titulo, mensagem, "Todos");
                    feedbackLabel.setText("✓ Popup enviado para a Intranet-IDAM!");
                    enviarButton.setDisable(false);
                    limparCamposImagem();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    feedbackLabel.setText("Falha ao enviar: " + e.getMessage());
                    enviarButton.setDisable(false);
                });
            }
        }, "envio-aviso-intranet");

        envio.setDaemon(true);
        envio.start();
    }

    private String determinarPrioridade() {
        String valor = prioridadeCombo.getValue();
        if (valor.contains("Urgente")) return "urgente";
        if (valor.contains("Importante")) return "importante";
        return "normal";
    }

    private void limparCamposImagem() {
        imagemSelecionadaPath = null;
        imagemNomeLabel.setText("Nenhuma imagem selecionada");
        imagemPreview.setImage(null);
        imagemPreview.setVisible(false);
        imagemPreview.setManaged(false);
        removerImagemButton.setVisible(false);
        removerImagemButton.setManaged(false);
    }

    // ============================================================
    // VISUALIZAÇÃO DE POPUP
    // ============================================================

    private void visualizarPopup() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        VBox box = criarPopupPreview();
        Scene scene = criarPopupScene(box);

        stage.setTitle("Pré-visualização do Popup");
        stage.setScene(scene);
        stage.showAndWait();
    }

    private VBox criarPopupPreview() {
        VBox box = new VBox(15);
        box.getStyleClass().add("popup-preview");

        Label icon = new Label("🔔");
        icon.getStyleClass().add("popup-icon");

        Label titulo = new Label(tituloField.getText());
        titulo.getStyleClass().add("popup-title");

        Label mensagem = new Label(mensagemArea.getText());
        mensagem.setWrapText(true);

        box.getChildren().addAll(icon, titulo);

        if (imagemSelecionadaPath != null) {
            ImageView preview = new ImageView(new Image(
                    imagemSelecionadaPath.toUri().toString(),
                    360, 0, true, true
            ));
            box.getChildren().add(preview);
        }

        box.getChildren().add(mensagem);

        Button botao = criarBotao("Ver comunicado", true);
        botao.setOnAction(e -> ((Stage) botao.getScene().getWindow()).close());
        box.getChildren().add(botao);

        return box;
    }

    private Scene criarPopupScene(VBox box) {
        int height = imagemSelecionadaPath != null ? 380 : 260;
        Scene scene = new Scene(box, 440, height);
        scene.getStylesheets().add(
                HelloApplication.class.getResource("style.css").toExternalForm()
        );
        return scene;
    }

    // ============================================================
    // HISTÓRICO
    // ============================================================

    private void historico() {
        prepararTela("Histórico de Avisos");

        VBox table = criarTabelaHistorico();
        content.getChildren().add(table);

        Button atualizar = criarBotao("⟳  Atualizar", false);
        atualizar.setOnAction(e -> historico());
        content.getChildren().add(atualizar);
    }

    private VBox criarTabelaHistorico() {
        VBox table = new VBox(0);
        table.getStyleClass().add("history-table");

        table.getChildren().add(criarLinhaTabela("Título", "Prioridade", "Destinatários", "Enviado em", "Status", true));

        for (Aviso aviso : service.listarTodos()) {
            table.getChildren().add(criarLinhaTabela(
                    aviso.getTitulo(),
                    "🔵 Normal",
                    aviso.getAutor(),
                    aviso.getDataPublicacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    "✓ Enviado",
                    false
            ));
        }

        return table;
    }

    private Node criarLinhaTabela(String a, String b, String c, String d, String e, boolean header) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add(header ? "table-head" : "table-row");

        for (int i = 0; i < 5; i++) {
            grid.getColumnConstraints().add(criarColuna(20));
        }

        grid.add(new Label(a), 0, 0);
        grid.add(new Label(b), 1, 0);
        grid.add(new Label(c), 2, 0);
        grid.add(new Label(d), 3, 0);

        Label status = new Label(e);
        if (!header) {
            status.getStyleClass().add("status-sent");
        }
        grid.add(status, 4, 0);

        return grid;
    }

    // ============================================================
    // MONITOR DE ACESSOS
    // ============================================================

    private void monitorAcessos() {
        prepararTela("Acessando Agora");

        VBox card = criarCardMonitor();
        content.getChildren().add(card);
        content.getChildren().add(criarStatsMonitor());

        iniciarMonitoramento();
    }

    private VBox criarCardMonitor() {
        VBox card = new VBox(8);
        card.getStyleClass().add("form-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));

        Label icon = new Label("👁");
        icon.getStyleClass().add("popup-icon");

        monitorOnlineLabel = new Label("--");
        monitorOnlineLabel.getStyleClass().add("live-counter-number");

        Label legenda = new Label("pessoas vendo a intranet agora");
        legenda.getStyleClass().add("muted");

        HBox liveRow = new HBox(6);
        liveRow.setAlignment(Pos.CENTER);

        Label liveDot = new Label("●");
        liveDot.getStyleClass().add("status-sent");

        monitorStatusLabel = new Label("Conectando ao servidor...");
        monitorStatusLabel.getStyleClass().add("muted");

        liveRow.getChildren().addAll(liveDot, monitorStatusLabel);

        card.getChildren().addAll(icon, monitorOnlineLabel, legenda, liveRow);
        return card;
    }

    private HBox criarStatsMonitor() {
        HBox stats = new HBox(12);

        monitorAcessosHojeLabel = criarStatValor();
        monitorTotalVisitantesLabel = criarStatValor();
        monitorUltimaConexaoLabel = criarStatValor();

        stats.getChildren().addAll(
                criarStatCard("Acessos hoje", monitorAcessosHojeLabel),
                criarStatCard("Total de visitantes", monitorTotalVisitantesLabel),
                criarStatCard("Última conexão", monitorUltimaConexaoLabel)
        );

        return stats;
    }

    private Label criarStatValor() {
        Label label = new Label("--");
        label.getStyleClass().add("stat-number");
        return label;
    }

    private Node criarStatCard(String texto, Label valor) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("muted");

        VBox box = new VBox(6, valor, rotulo);
        box.getStyleClass().add("stat-card");
        HBox.setHgrow(box, Priority.ALWAYS);

        return box;
    }

    private void iniciarMonitoramento() {
        Runnable atualizar = criarAtualizadorMonitor();
        atualizar.run();

        monitorTimeline = new Timeline(
                new KeyFrame(Duration.seconds(MONITOR_INTERVAL_SECONDS), e -> atualizar.run())
        );
        monitorTimeline.setCycleCount(Timeline.INDEFINITE);
        monitorTimeline.play();
    }

    private Runnable criarAtualizadorMonitor() {
        return () -> {
            Thread t = new Thread(() -> {
                try {
                    IntranetAvisosClient.EstatisticasAcesso stat =
                            new IntranetAvisosClient().buscarEstatisticasAcesso();

                    Platform.runLater(() -> atualizarInterfaceMonitor(stat));
                } catch (Exception e) {
                    Platform.runLater(() -> monitorStatusLabel.setText("Falha na conexão com o servidor"));
                }
            }, "monitor-acessos");
            t.setDaemon(true);
            t.start();
        };
    }

    private void atualizarInterfaceMonitor(IntranetAvisosClient.EstatisticasAcesso stat) {
        monitorOnlineLabel.setText(String.valueOf(stat.onlineAgora()));
        monitorAcessosHojeLabel.setText(String.valueOf(stat.acessosHoje()));
        monitorTotalVisitantesLabel.setText(String.valueOf(stat.totalVisitantes()));
        monitorUltimaConexaoLabel.setText(formatarUltimaConexao(stat.ultimaConexao()));
        monitorStatusLabel.setText("Atualizado às " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
    }

    private void pararMonitor() {
        if (monitorTimeline != null) {
            monitorTimeline.stop();
            monitorTimeline = null;
        }
    }

    // ============================================================
    // MENSAGEM DO DIA E CONFIGURAÇÕES
    // ============================================================

    private void abrirMensagemDoDia(Stage stage) {
        prepararTela("Mensagem do Dia");
        Node tela = MensagemDoDiaView.criar(stage);
        content.getChildren().add(tela);
    }

    private void configuracoes() {
        prepararTela("Configurações");

        VBox config = new VBox(14);
        config.getStyleClass().add("form-card");

        TextField endereco = new TextField(IntranetAvisosClient.baseUrl());
        endereco.setPromptText("https://intranet.exemplo.gov.br");
        Label feedback = new Label();
        feedback.getStyleClass().add("muted");

        Button salvar = criarBotao("Testar e salvar conexão", true);
        salvar.setOnAction(event -> testarESalvarConexao(endereco, feedback, salvar));

        config.getChildren().addAll(
                criarLabel("Endereço do site da Intranet"),
                endereco,
                criarLabel("Informe apenas a URL base. Os endpoints /api/avisos e /api/visitas são adicionados automaticamente."),
                salvar,
                feedback
        );

        content.getChildren().add(config);
    }

    private void testarESalvarConexao(TextField endereco, Label feedback, Button salvar) {
        try {
            IntranetAvisosClient.configurarBaseUrl(endereco.getText());
        } catch (IllegalArgumentException error) {
            feedback.setText(error.getMessage());
            return;
        }

        salvar.setDisable(true);
        feedback.setText("Testando conexão...");
        Thread teste = new Thread(() -> {
            try {
                new IntranetAvisosClient().buscarEstatisticasAcesso();
                Platform.runLater(() -> {
                    feedback.setText("Conexão estabelecida com " + IntranetAvisosClient.baseUrl());
                    salvar.setDisable(false);
                });
            } catch (Exception error) {
                Platform.runLater(() -> {
                    feedback.setText("Não foi possível conectar: " + error.getMessage());
                    salvar.setDisable(false);
                });
            }
        }, "teste-conexao-intranet");
        teste.setDaemon(true);
        teste.start();
    }

    // ============================================================
    // MÉTODOS UTILITÁRIOS
    // ============================================================

    private static String formatarUltimaConexao(String isoUtc) {
        if (isoUtc == null || isoUtc.isBlank()) {
            return "—";
        }

        try {
            return Instant.parse(isoUtc)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("dd/MM HH:mm:ss"));
        } catch (Exception e) {
            return "—";
        }
    }
}
