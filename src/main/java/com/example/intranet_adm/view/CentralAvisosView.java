/// Olá! Este arquivo é responsável pela interface da Central de Avisos.
/// Ele cria e organiza os campos, botões, listas e componentes visuais.
/// Ele se conecta ao Controller e aos Services do projeto.
/// Se os campos ou funcionalidades da tela forem alterados, verifique as classes relacionadas. =)

package com.example.intranet_adm.view;

import com.example.intranet_adm.model.Aviso;
import com.example.intranet_adm.service.AvisoService;
import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.util.DailyMessages;

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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class CentralAvisosView {

    // ============================================================
    // CONFIGURAÇÕES
    // ============================================================

    private static final int SIDEBAR_WIDTH = 230;
    private static final int CARD_SPACING = 16;

    private static final int IMAGE_PREVIEW_WIDTH = 360;
    private static final int IMAGE_PREVIEW_HEIGHT = 180;

    private static final int MONITOR_INTERVAL_SECONDS = 5;

    private static final Pattern HORA_PATTERN =
            Pattern.compile("([01]\\d|2[0-3]):[0-5]\\d");

    private static final DateTimeFormatter FORMATO_HORA =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ============================================================
    // SERVIÇOS
    // ============================================================

    private final AvisoService service =
            new AvisoService();

    // ============================================================
    // LAYOUT PRINCIPAL
    // ============================================================

    private final BorderPane root =
            new BorderPane();

    private final VBox content =
            new VBox(CARD_SPACING);

    // ============================================================
    // CAMPOS DO FORMULÁRIO
    // ============================================================

    private TextField tituloField;

    private TextArea descricaoArea;

    private ComboBox<String> modeloCombo;
    private ComboBox<String> tamanhoCombo;
    private ComboBox<String> iconeCombo;
    private ComboBox<String> mostrarDatasCombo;
    private ComboBox<String> ativoCombo;
    private ComboBox<String> podeFecharCombo;

    private ColorPicker corFundoPicker;
    private ColorPicker corTextoPicker;
    private ColorPicker corDestaquePicker;

    private CheckBox paginaCentralCheck;
    private CheckBox paginaLoginCheck;
    private CheckBox paginaHelpdeskCheck;

    private TextArea mensagemArea;

    private ComboBox<String> prioridadeCombo;

    private TextField linkIntranetField;

    // ============================================================
    // FEEDBACK
    // ============================================================

    private Label feedbackLabel;

    private Button enviarButton;

    // ============================================================
    // DATAS
    // ============================================================

    private DatePicker publicarDataPicker;

    private TextField publicarHoraField;

    private DatePicker expirarDataPicker;

    private TextField expirarHoraField;

    // ============================================================
    // IMAGEM
    // ============================================================

    private Path imagemSelecionadaPath;

    private Label imagemNomeLabel;

    private ImageView imagemPreview;

    private Button removerImagemButton;

    // ============================================================
    // MONITOR
    // ============================================================

    private Timeline monitorTimeline;

    private Label monitorOnlineLabel;

    private Label monitorAcessosHojeLabel;

    private Label monitorTotalVisitantesLabel;

    private Label monitorUltimaConexaoLabel;

    private Label monitorStatusLabel;

    // ============================================================
    // CRIAR VIEW
    // ============================================================

    public static Parent criar(Stage stage) {
        return new CentralAvisosView()
                .montar(stage);
    }

    private Parent montar(Stage stage) {

        configurarEstilos();

        configurarLayout(stage);

        novoAviso();

        return root;
    }

    // ============================================================
    // ESTILOS
    // ============================================================

    private void configurarEstilos() {

        root.getStyleClass()
                .add("central-root");
    }

    // ============================================================
    // LAYOUT
    // ============================================================

    private void configurarLayout(Stage stage) {

        root.setLeft(
                criarMenu(stage)
        );

        root.setTop(
                criarCabecalho()
        );

        root.setCenter(
                criarScrollArea()
        );
    }

    private ScrollPane criarScrollArea() {

        ScrollPane scroll =
                new ScrollPane(content);

        scroll.setFitToWidth(true);

        scroll.getStyleClass()
                .add("central-scroll");

        content.setPadding(
                new Insets(26)
        );

        content.getStyleClass()
                .add("central-content");

        return scroll;
    }

    // ============================================================
    // CABEÇALHO
    // ============================================================

    private Node criarCabecalho() {

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

        bar.getChildren().addAll(
                new Label("🔔"),
                titulo,
                spacer,
                new Label("—     □     ×")
        );

        return bar;
    }

    // ============================================================
    // MENU
    // ============================================================

    private Node criarMenu(Stage stage) {

        VBox menu =
                new VBox(10);

        menu.setPrefWidth(
                SIDEBAR_WIDTH
        );

        menu.getStyleClass()
                .add("central-sidebar");

        menu.getChildren().addAll(

                criarLogo(),

                criarTituloMenu(),

                criarSubtituloMenu(),

                criarNavButton(
                        "⊕   Novo Aviso",
                        this::novoAviso,
                        true
                ),

                criarNavButton(
                        "◉   Popups",
                        this::popups,
                        false
                ),

                criarNavButton(
                        "◷   Histórico",
                        this::historico,
                        false
                ),

                criarNavButton(
                        "👁   Acessando Agora",
                        this::monitorAcessos,
                        false
                ),

                criarNavButton(
                        "☀   Mensagem do Dia",
                        () -> abrirMensagemDoDia(stage),
                        false
                ),

                criarNavButton(
                        "⚙   Configurações",
                        this::configuracoes,
                        false
                )
        );

        Region spacer =
                new Region();

        VBox.setVgrow(
                spacer,
                Priority.ALWAYS
        );

        menu.getChildren()
                .add(spacer);

        menu.getChildren().addAll(

                criarNavButton(
                        "⇥   Sair",
                        stage::close,
                        false
                ),

                criarStatusLabel()
        );

        return menu;
    }

    private Label criarLogo() {

        Label logo =
                new Label("🔔");

        logo.getStyleClass()
                .add("central-logo");

        return logo;
    }

    private Label criarTituloMenu() {

        Label titulo =
                new Label("Central de Avisos");

        titulo.getStyleClass()
                .add("central-brand");

        return titulo;
    }

    private Label criarSubtituloMenu() {

        Label subtitulo =
                new Label("Popup para Intranet");

        subtitulo.getStyleClass()
                .add("sidebar-subtitle");

        return subtitulo;
    }

    private Button criarNavButton(
            String texto,
            Runnable acao,
            boolean ativo) {

        Button button =
                new Button(texto);

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.getStyleClass()
                .add(
                        ativo
                                ? "central-nav-active"
                                : "central-nav"
                );

        button.setOnAction(
                e -> acao.run()
        );

        return button;
    }

    private Label criarStatusLabel() {

        Label status =
                new Label(
                        "●  Configure o servidor\n\n" +
                                "Versão 1.0.0"
                );

        status.getStyleClass()
                .add("connection");

        return status;
    }

    // ============================================================
    // PREPARAR TELA
    // ============================================================

    private void prepararTela(
            String titulo) {

        pararMonitor();

        content.getChildren()
                .clear();

        Label cabecalho =
                new Label(titulo);

        cabecalho.getStyleClass()
                .add("central-page-title");

        content.getChildren()
                .add(cabecalho);
    }

    // ============================================================
    // NOVO AVISO
    // ============================================================

    private void novoAviso() {

        prepararTela(
                "Novo Aviso"
        );

        VBox card =
                criarCardFormulario();

        GridPane grid =
                criarGridFormulario();

        configurarCamposFormulario();

        adicionarCamposBasicos(grid);

        adicionarCustomizacao(grid);

        adicionarCampoImagem(grid);

        adicionarCampoLink(grid);

        card.getChildren()
                .add(grid);

        card.getChildren()
                .add(
                        criarAreaAcoes()
                );

        content.getChildren()
                .add(card);
    }

    private VBox criarCardFormulario() {

        VBox card =
                new VBox(
                        CARD_SPACING
                );

        card.getStyleClass()
                .add("form-card");

        return card;
    }

    private GridPane criarGridFormulario() {

        GridPane grid =
                new GridPane();

        grid.setHgap(16);

        grid.setVgap(10);

        grid.getColumnConstraints()
                .addAll(
                        criarColuna(70),
                        criarColuna(30)
                );

        return grid;
    }

    private ColumnConstraints criarColuna(
            int porcentagem) {

        ColumnConstraints col =
                new ColumnConstraints();

        col.setPercentWidth(
                porcentagem
        );

        return col;
    }

    private Label criarLabel(
            String texto) {

        Label label =
                new Label(texto);

        label.getStyleClass()
                .add("central-label");

        return label;
    }

    private Button criarBotao(
            String texto,
            boolean primario) {

        Button button =
                new Button(texto);

        button.getStyleClass()
                .add(
                        primario
                                ? "send-button"
                                : "soft-button"
                );

        return button;
    }

    // ============================================================
    // CAMPOS
    // ============================================================

    private void configurarCamposFormulario() {

        tituloField =
                new TextField(
                        "Alerta de Instabilidade de Internet"
                );

        descricaoArea =
                new TextArea(
                        "⚠ Alerta de Instabilidade de Internet\n\n" +
                                "Informamos que estamos enfrentando instabilidades no acesso à internet, " +
                                "o que pode ocasionar lentidão ou interrupções temporárias em sistemas e serviços " +
                                "que dependem de conexão externa.\n\n" +
                                "Nossa equipe técnica já está atuando para identificar e solucionar a ocorrência " +
                                "o mais breve possível."
                );

        descricaoArea.setPrefRowCount(6);

        descricaoArea.setWrapText(true);

        mensagemArea =
                descricaoArea;

        modeloCombo =
                new ComboBox<>();

        modeloCombo.getItems().addAll(
                "Problema",
                "Informativo",
                "Sucesso",
                "Manutenção"
        );

        modeloCombo.getSelectionModel()
                .select("Problema");

        tamanhoCombo =
                new ComboBox<>();

        tamanhoCombo.getItems().addAll(
                "Pequeno",
                "Médio",
                "Grande"
        );

        tamanhoCombo.getSelectionModel()
                .select("Médio");

        iconeCombo =
                new ComboBox<>();

        iconeCombo.getItems().addAll(
                "Sem ícone",
                "⚙ Configuração",
                "ⓘ Informação",
                "⚠ Alerta",
                "❕ Problema"
        );

        iconeCombo.getSelectionModel()
                .select("❕ Problema");

        mostrarDatasCombo =
                new ComboBox<>();

        mostrarDatasCombo.getItems().addAll(
                "Sim",
                "Não"
        );

        mostrarDatasCombo.getSelectionModel()
                .select("Não");

        ativoCombo =
                new ComboBox<>();

        ativoCombo.getItems().addAll(
                "Sim",
                "Não"
        );

        ativoCombo.getSelectionModel()
                .select("Sim");

        podeFecharCombo =
                new ComboBox<>();

        podeFecharCombo.getItems().addAll(
                "Sim",
                "Não"
        );

        podeFecharCombo.getSelectionModel()
                .select("Sim");

        prioridadeCombo =
                new ComboBox<>();

        prioridadeCombo.getItems().addAll(
                "🔴  Urgente",
                "🟠  Importante",
                "🔵  Normal"
        );

        prioridadeCombo.getSelectionModel()
                .select(0);

        corFundoPicker =
                new ColorPicker(
                        Color.WHITE
                );

        corTextoPicker =
                new ColorPicker(
                        Color.web("#20242A")
                );

        corDestaquePicker =
                new ColorPicker(
                        Color.web("#F0B90B")
                );

        paginaCentralCheck =
                new CheckBox(
                        "Página central"
                );

        paginaLoginCheck =
                new CheckBox(
                        "Página de login"
                );

        paginaHelpdeskCheck =
                new CheckBox(
                        "Página de helpdesk"
                );

        paginaCentralCheck.setSelected(true);

        paginaLoginCheck.setSelected(true);

        paginaHelpdeskCheck.setSelected(true);

        linkIntranetField =
                new TextField(
                        "https://intranet.empresa.com/comunicados/manutencao"
                );
    }

    // ============================================================
    // CAMPOS BÁSICOS
    // ============================================================

    private void adicionarCamposBasicos(
            GridPane grid) {

        int row = 0;

        grid.getColumnConstraints()
                .clear();

        grid.getColumnConstraints()
                .addAll(
                        criarColuna(70),
                        criarColuna(30)
                );

        grid.add(
                criarLabel("Modelo"),
                0,
                row
        );

        grid.add(
                criarLabel("Ativo"),
                1,
                row
        );

        grid.add(
                modeloCombo,
                0,
                ++row
        );

        grid.add(
                ativoCombo,
                1,
                row
        );

        grid.add(
                criarLabel("Nome"),
                0,
                ++row
        );

        grid.add(
                tituloField,
                0,
                ++row,
                2,
                1
        );

        grid.add(
                criarLabel("Descrição"),
                0,
                ++row,
                2,
                1
        );

        grid.add(
                descricaoArea,
                0,
                ++row,
                2,
                1
        );

        grid.add(
                criarLabel("Prioridade"),
                0,
                ++row
        );

        grid.add(
                prioridadeCombo,
                0,
                ++row
        );

        adicionarCampoDatas(
                grid,
                row
        );
    }

    // ============================================================
    // DATAS
    // ============================================================

    private void adicionarCampoDatas(
            GridPane grid,
            int rowInicial) {

        int row =
                rowInicial + 1;

        publicarDataPicker =
                criarDatePicker(
                        LocalDate.now()
                );

        publicarHoraField =
                criarCampoHora(
                        LocalTime.now()
                                .withSecond(0)
                                .withNano(0)
                                .format(FORMATO_HORA)
                );

        expirarDataPicker =
                criarDatePicker(null);

        expirarHoraField =
                criarCampoHora("");

        HBox publicarRow =
                new HBox(
                        8,
                        publicarDataPicker,
                        publicarHoraField
                );

        HBox expirarRow =
                new HBox(
                        8,
                        expirarDataPicker,
                        expirarHoraField
                );

        publicarRow.setAlignment(
                Pos.CENTER_LEFT
        );

        expirarRow.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox publicarBox =
                new VBox(
                        7,
                        criarLabel(
                                "Data de início da visibilidade"
                        ),
                        publicarRow
                );

        VBox expirarBox =
                new VBox(
                        7,
                        criarLabel(
                                "Data de término da visibilidade"
                        ),
                        expirarRow
                );

        HBox dates =
                new HBox(
                        24,
                        publicarBox,
                        expirarBox
                );

        dates.getStyleClass()
                .add("dates-row");

        grid.add(
                dates,
                0,
                row,
                2,
                1
        );

        grid.add(
                criarLabel(
                        "Mostrar nas páginas"
                ),
                0,
                ++row,
                2,
                1
        );

        HBox paginas =
                new HBox(
                        18,
                        paginaCentralCheck,
                        paginaLoginCheck,
                        paginaHelpdeskCheck
                );

        paginas.setAlignment(
                Pos.CENTER_LEFT
        );

        grid.add(
                paginas,
                0,
                ++row,
                2,
                1
        );

        grid.add(
                criarLabel(
                        "Alerta pode ser fechado"
                ),
                0,
                ++row
        );

        grid.add(
                podeFecharCombo,
                0,
                ++row
        );
    }

    // ============================================================
    // CUSTOMIZAÇÃO
    // ============================================================

    private void adicionarCustomizacao(
            GridPane grid) {

        int row =
                grid.getRowCount();

        Separator separator =
                new Separator();

        grid.add(
                separator,
                0,
                row++,
                2,
                1
        );

        grid.add(
                criarLabel(
                        "CUSTOMIZAÇÃO"
                ),
                0,
                row++,
                2,
                1
        );

        GridPane custom =
                new GridPane();

        custom.setHgap(16);

        custom.setVgap(10);

        custom.getColumnConstraints()
                .addAll(
                        criarColuna(33),
                        criarColuna(33),
                        criarColuna(34)
                );

        custom.add(
                criarLabel("Tamanho"),
                0,
                0
        );

        custom.add(
                criarLabel("Ícone"),
                1,
                0
        );

        custom.add(
                criarLabel("Mostrar datas"),
                2,
                0
        );

        custom.add(
                tamanhoCombo,
                0,
                1
        );

        custom.add(
                iconeCombo,
                1,
                1
        );

        custom.add(
                mostrarDatasCombo,
                2,
                1
        );

        custom.add(
                criarLabel("Cor de fundo"),
                0,
                2
        );

        custom.add(
                criarLabel("Cor do texto"),
                1,
                2
        );

        custom.add(
                criarLabel("Cor de destaque"),
                2,
                2
        );

        custom.add(
                corFundoPicker,
                0,
                3
        );

        custom.add(
                corTextoPicker,
                1,
                3
        );

        custom.add(
                corDestaquePicker,
                2,
                3
        );

        grid.add(
                custom,
                0,
                row,
                2,
                1
        );
    }

    // ============================================================
    // IMAGEM
    // ============================================================

    private void adicionarCampoImagem(
            GridPane grid) {

        int row =
                grid.getRowCount();

        FileChooser imagemChooser =
                new FileChooser();

        imagemChooser.setTitle(
                "Selecionar imagem do aviso"
        );

        imagemChooser
                .getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Imagens",
                                "*.jpg",
                                "*.jpeg",
                                "*.png",
                                "*.gif",
                                "*.webp"
                        )
                );

        Button selecionarImagemButton =
                criarBotao(
                        "🖼  Selecionar imagem",
                        false
                );

        imagemNomeLabel =
                new Label(
                        "Nenhuma imagem selecionada"
                );

        imagemNomeLabel.getStyleClass()
                .add("muted");

        removerImagemButton =
                criarBotao(
                        "✕",
                        false
                );

        removerImagemButton.setVisible(false);

        removerImagemButton.setManaged(false);

        imagemPreview =
                new ImageView();

        imagemPreview.setFitWidth(
                IMAGE_PREVIEW_WIDTH
        );

        imagemPreview.setFitHeight(
                IMAGE_PREVIEW_HEIGHT
        );

        imagemPreview.setPreserveRatio(true);

        imagemPreview.setSmooth(true);

        imagemPreview.setVisible(false);

        imagemPreview.setManaged(false);

        configurarEventosImagem(
                selecionarImagemButton,
                imagemChooser
        );

        HBox imagemRow =
                new HBox(
                        12,
                        selecionarImagemButton,
                        imagemNomeLabel,
                        removerImagemButton
                );

        imagemRow.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox imagemField =
                new VBox(
                        12,
                        imagemRow,
                        imagemPreview
                );

        imagemField.getStyleClass()
                .add("image-upload-field");

        grid.add(
                criarLabel(
                        "Imagem do aviso (opcional)"
                ),
                0,
                row,
                2,
                1
        );

        grid.add(
                imagemField,
                0,
                ++row,
                2,
                1
        );
    }

    private void configurarEventosImagem(
            Button selecionarButton,
            FileChooser chooser) {

        selecionarButton.setOnAction(e -> {

            File arquivo =
                    chooser.showOpenDialog(
                            selecionarButton
                                    .getScene()
                                    .getWindow()
                    );

            if (arquivo != null) {

                imagemSelecionadaPath =
                        arquivo.toPath();

                imagemNomeLabel.setText(
                        arquivo.getName()
                );

                imagemPreview.setImage(
                        new Image(
                                arquivo
                                        .toURI()
                                        .toString(),
                                IMAGE_PREVIEW_WIDTH,
                                IMAGE_PREVIEW_HEIGHT,
                                true,
                                true
                        )
                );

                imagemPreview.setVisible(true);

                imagemPreview.setManaged(true);

                removerImagemButton.setVisible(true);

                removerImagemButton.setManaged(true);
            }
        });

        removerImagemButton.setOnAction(
                e -> limparCamposImagem()
        );
    }

    // ============================================================
    // LINK
    // ============================================================

    private void adicionarCampoLink(
            GridPane grid) {

        int row =
                grid.getRowCount();

        grid.add(
                criarLabel(
                        "Link da Intranet"
                ),
                0,
                row,
                2,
                1
        );

        grid.add(
                linkIntranetField,
                0,
                ++row,
                2,
                1
        );
    }

    // ============================================================
    // DATA PICKER
    // ============================================================

    private DatePicker criarDatePicker(
            LocalDate valorInicial) {

        DatePicker picker =
                new DatePicker(
                        valorInicial
                );

        picker.setPromptText(
                "dd/mm/aaaa"
        );

        picker.setPrefWidth(140);

        return picker;
    }

    // ============================================================
    // CAMPO HORA
    // ============================================================

    private TextField criarCampoHora(
            String valorInicial) {

        TextField campo =
                new TextField(
                        valorInicial
                );

        campo.setPromptText(
                "HH:mm"
        );

        campo.setPrefWidth(70);

        UnaryOperator<TextFormatter.Change> filtro =
                change -> {

                    String novoTexto =
                            change.getControlNewText();

                    if (
                            novoTexto.length() <= 5 &&
                                    novoTexto.matches("[0-9:]*")
                    ) {
                        return change;
                    }

                    return null;
                };

        campo.setTextFormatter(
                new TextFormatter<>(filtro)
        );

        return campo;
    }

    // ============================================================
    // AÇÕES
    // ============================================================

    private HBox criarAreaAcoes() {

        HBox actions =
                new HBox(12);

        actions.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button previewButton =
                criarBotao(
                        "◉  Visualizar Popup",
                        false
                );

        previewButton.setOnAction(
                e -> visualizarPopup()
        );

        enviarButton =
                criarBotao(
                        "➤  Enviar Aviso",
                        true
                );

        enviarButton.setOnAction(
                e -> enviarAviso()
        );

        feedbackLabel =
                new Label();

        feedbackLabel.getStyleClass()
                .add("success-label");

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        actions.getChildren()
                .addAll(
                        feedbackLabel,
                        spacer,
                        previewButton,
                        enviarButton
                );

        return actions;
    }

    // ============================================================
    // ENVIO
    // ============================================================

    private void enviarAviso() {

        feedbackLabel.setText("");

        if (validarCampos()) {
            realizarEnvio();
        }
    }

    private boolean validarCampos() {

        if (
                tituloField.getText().isBlank() ||
                        mensagemArea.getText().isBlank()
        ) {

            feedbackLabel.setText(
                    "Preencha título e mensagem."
            );

            return false;
        }

        LocalDateTime publicaEm =
                obterDataPublicacao();

        LocalDateTime expiraEm =
                obterDataExpiracao();

        if (publicaEm == null) {

            feedbackLabel.setText(
                    "Informe uma data e horário de publicação válidos (HH:mm)."
            );

            return false;
        }

        if (
                expiraEm != null &&
                        !expiraEm.isAfter(publicaEm)
        ) {

            feedbackLabel.setText(
                    "A expiração deve ser depois da publicação."
            );

            return false;
        }

        return true;
    }

    private LocalDateTime obterDataPublicacao() {

        LocalDate data =
                publicarDataPicker.getValue();

        String horaStr =
                publicarHoraField
                        .getText()
                        .trim();

        if (
                data == null ||
                        horaStr.isBlank()
        ) {
            return null;
        }

        try {

            return LocalDateTime.of(
                    data,
                    LocalTime.parse(
                            horaStr,
                            FORMATO_HORA
                    )
            );

        } catch (Exception e) {

            return null;
        }
    }

    private LocalDateTime obterDataExpiracao() {

        LocalDate data =
                expirarDataPicker.getValue();

        String horaStr =
                expirarHoraField
                        .getText()
                        .trim();

        if (
                data == null ||
                        horaStr.isBlank()
        ) {
            return null;
        }

        try {

            return LocalDateTime.of(
                    data,
                    LocalTime.parse(
                            horaStr,
                            FORMATO_HORA
                    )
            );

        } catch (Exception e) {

            return null;
        }
    }

    private static boolean horaValida(
            String valor) {

        return valor != null &&
                HORA_PATTERN
                        .matcher(valor.trim())
                        .matches();
    }

    // ============================================================
    // REALIZAR ENVIO
    // ============================================================

    private void realizarEnvio() {

        String titulo =
                tituloField
                        .getText()
                        .trim();

        String mensagem =
                mensagemArea
                        .getText()
                        .trim();

        String link =
                linkIntranetField
                        .getText()
                        .trim();

        String nivel =
                determinarPrioridade();

        String corFundo =
                colorToHex(
                        corFundoPicker.getValue()
                );

        String corTexto =
                colorToHex(
                        corTextoPicker.getValue()
                );

        String corDestaque =
                colorToHex(
                        corDestaquePicker.getValue()
                );

        LocalDateTime publicarEm =
                obterDataPublicacao();

        LocalDateTime expirarEm =
                obterDataExpiracao();

        boolean ativo =
                "Sim".equals(
                        ativoCombo.getValue()
                );

        boolean mostrarDatas =
                "Sim".equals(
                        mostrarDatasCombo.getValue()
                );

        boolean podeFechar =
                "Sim".equals(
                        podeFecharCombo.getValue()
                );

        boolean paginaCentral =
                paginaCentralCheck.isSelected();

        boolean paginaLogin =
                paginaLoginCheck.isSelected();

        boolean paginaHelpdesk =
                paginaHelpdeskCheck.isSelected();

        enviarButton.setDisable(true);

        feedbackLabel.setText(
                "Enviando para a Intranet-IDAM..."
        );

        Thread envio =
                new Thread(() -> {

                    try {

                        IntranetAvisosClient.AvisoConfig config =
                                new IntranetAvisosClient.AvisoConfig(
                                        titulo,
                                        mensagem
                                )
                                        .comPrioridade(nivel)
                                        .comLink(link)
                                        .comImagem(
                                                imagemSelecionadaPath
                                        )
                                        .comPublicarEm(
                                                publicarEm
                                        )
                                        .comExpirarEm(
                                                expirarEm
                                        )
                                        .comModelo(
                                                modeloCombo.getValue()
                                        )
                                        .comTamanho(
                                                tamanhoCombo.getValue()
                                        )
                                        .comIcone(
                                                obterIconeReal(
                                                        iconeCombo.getValue()
                                                )
                                        )
                                        .comMostrarDatas(
                                                mostrarDatas
                                        )
                                        .comCorFundo(
                                                corFundo
                                        )
                                        .comCorTexto(
                                                corTexto
                                        )
                                        .comCorDestaque(
                                                corDestaque
                                        )
                                        .comAtivo(
                                                ativo
                                        )
                                        .comPaginas(
                                                paginaCentral,
                                                paginaLogin,
                                                paginaHelpdesk
                                        )
                                        .comPodeFechar(
                                                podeFechar
                                        );

                        new IntranetAvisosClient()
                                .enviar(config);

                        Platform.runLater(() -> {

                            service.adicionar(
                                    titulo,
                                    mensagem,
                                    "Todos"
                            );

                            feedbackLabel.setText(
                                    "✓ Popup enviado para a Intranet-IDAM!"
                            );

                            enviarButton.setDisable(
                                    false
                            );

                            limparCamposImagem();
                        });

                    } catch (Exception e) {

                        Platform.runLater(() -> {

                            feedbackLabel.setText(
                                    "Falha ao enviar: " +
                                            e.getMessage()
                            );

                            enviarButton.setDisable(
                                    false
                            );
                        });
                    }

                }, "envio-aviso-intranet");

        envio.setDaemon(true);

        envio.start();
    }

    // ============================================================
    // PRIORIDADE
    // ============================================================

    private String determinarPrioridade() {

        String valor =
                prioridadeCombo.getValue();

        if (valor == null) {
            return "normal";
        }

        if (valor.contains("Urgente")) {
            return "urgente";
        }

        if (valor.contains("Importante")) {
            return "importante";
        }

        return "normal";
    }

    // ============================================================
    // COR
    // ============================================================

    private String colorToHex(
            Color color) {

        if (color == null) {
            return "0xffffffff";
        }

        return String.format(
                "0x%02x%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255),
                (int) (color.getOpacity() * 255)
        );
    }

    // ============================================================
    // ÍCONE
    // ============================================================

    private String obterIconeReal(
            String valor) {

        if (valor == null) {
            return "❕";
        }

        if (valor.contains("⚙")) {
            return "⚙";
        }

        if (valor.contains("ⓘ")) {
            return "ⓘ";
        }

        if (valor.contains("⚠")) {
            return "⚠";
        }

        if (valor.contains("❕")) {
            return "❕";
        }

        return "❕";
    }

    // ============================================================
    // LIMPAR IMAGEM
    // ============================================================

    private void limparCamposImagem() {

        imagemSelecionadaPath =
                null;

        if (imagemNomeLabel != null) {

            imagemNomeLabel.setText(
                    "Nenhuma imagem selecionada"
            );
        }

        if (imagemPreview != null) {

            imagemPreview.setImage(null);

            imagemPreview.setVisible(false);

            imagemPreview.setManaged(false);
        }

        if (removerImagemButton != null) {

            removerImagemButton.setVisible(false);

            removerImagemButton.setManaged(false);
        }
    }

    // ============================================================
    // PREVIEW
    // ============================================================

    private void visualizarPopup() {

        Stage stage =
                new Stage();

        stage.initModality(
                Modality.APPLICATION_MODAL
        );

        VBox box =
                criarPopupPreview();

        Scene scene =
                criarPopupScene(box);

        stage.setTitle(
                "Pré-visualização do Popup"
        );

        stage.setScene(scene);

        stage.showAndWait();
    }

    private VBox criarPopupPreview() {

        VBox box =
                new VBox(15);

        box.getStyleClass()
                .add("popup-preview");

        box.setPadding(
                new Insets(24)
        );

        box.setStyle(
                "-fx-background-color: " +
                        corFundoPicker
                                .getValue()
                                .toString() +
                        ";"
        );

        HBox header =
                new HBox(10);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        Label icon =
                new Label(
                        obterIconeReal(
                                iconeCombo.getValue()
                        )
                );

        icon.getStyleClass()
                .add("popup-icon");

        icon.setStyle(
                "-fx-text-fill: " +
                        corDestaquePicker
                                .getValue()
                                .toString() +
                        ";"
        );

        VBox texts =
                new VBox(4);

        Label modelo =
                new Label(
                        modeloCombo.getValue()
                );

        modelo.setStyle(
                "-fx-text-fill: " +
                        corDestaquePicker
                                .getValue()
                                .toString() +
                        "; -fx-font-weight: bold;"
        );

        Label titulo =
                new Label(
                        tituloField.getText()
                );

        titulo.getStyleClass()
                .add("popup-title");

        titulo.setStyle(
                "-fx-text-fill: " +
                        corTextoPicker
                                .getValue()
                                .toString() +
                        ";"
        );

        texts.getChildren()
                .addAll(
                        modelo,
                        titulo
                );

        header.getChildren()
                .addAll(
                        icon,
                        texts
                );

        Label mensagem =
                new Label(
                        mensagemArea.getText()
                );

        mensagem.setWrapText(true);

        mensagem.setMaxWidth(
                Double.MAX_VALUE
        );

        mensagem.setStyle(
                "-fx-text-fill: " +
                        corTextoPicker
                                .getValue()
                                .toString() +
                        ";"
        );

        box.getChildren()
                .add(header);

        if (imagemSelecionadaPath != null) {

            ImageView preview =
                    new ImageView(
                            new Image(
                                    imagemSelecionadaPath
                                            .toUri()
                                            .toString(),
                                    360,
                                    0,
                                    true,
                                    true
                            )
                    );

            box.getChildren()
                    .add(preview);
        }

        box.getChildren()
                .add(mensagem);

        if (
                "Sim".equals(
                        mostrarDatasCombo.getValue()
                )
        ) {

            String inicio =
                    publicarDataPicker
                            .getValue() == null
                            ? "—"
                            :
                            publicarDataPicker
                                    .getValue()
                                    .format(
                                            FORMATO_DATA
                                    ) +
                                    " " +
                                    publicarHoraField
                                            .getText();

            String fim =
                    expirarDataPicker
                            .getValue() == null
                            ? "—"
                            :
                            expirarDataPicker
                                    .getValue()
                                    .format(
                                            FORMATO_DATA
                                    ) +
                                    " " +
                                    expirarHoraField
                                            .getText();

            Label datas =
                    new Label(
                            "Visível de " +
                                    inicio +
                                    (
                                            expirarDataPicker
                                                    .getValue() == null
                                                    ? ""
                                                    : " até " + fim
                                    )
                    );

            datas.setStyle(
                    "-fx-text-fill: " +
                            corTextoPicker
                                    .getValue()
                                    .toString() +
                            "; -fx-opacity: 0.75;"
            );

            box.getChildren()
                    .add(datas);
        }

        if (
                "Sim".equals(
                        podeFecharCombo.getValue()
                )
        ) {

            Button fechar =
                    criarBotao(
                            "Fechar",
                            false
                    );

            fechar.setOnAction(
                    e ->
                            ((Stage)
                                    fechar
                                            .getScene()
                                            .getWindow()
                            ).close()
            );

            box.getChildren()
                    .add(fechar);

        } else {

            Label aviso =
                    new Label(
                            "Este alerta não pode ser fechado pelo usuário."
                    );

            aviso.setStyle(
                    "-fx-text-fill: " +
                            corDestaquePicker
                                    .getValue()
                                    .toString() +
                            "; -fx-font-weight: bold;"
            );

            box.getChildren()
                    .add(aviso);
        }

        return box;
    }

    private Scene criarPopupScene(
            VBox box) {

        int height =
                imagemSelecionadaPath != null
                        ? 460
                        : 340;

        if (
                "Grande".equals(
                        tamanhoCombo.getValue()
                )
        ) {

            height =
                    imagemSelecionadaPath != null
                            ? 560
                            : 420;
        }

        if (
                "Pequeno".equals(
                        tamanhoCombo.getValue()
                )
        ) {

            height =
                    imagemSelecionadaPath != null
                            ? 400
                            : 300;
        }

        int width =
                "Grande".equals(
                        tamanhoCombo.getValue()
                )
                        ? 560
                        :
                        "Pequeno".equals(
                                tamanhoCombo.getValue()
                        )
                                ? 360
                                : 460;

        Scene scene =
                new Scene(
                        box,
                        width,
                        height
                );

        var css =
                getClass().getResource(
                        "/com/example/intranet_adm/style.css"
                );

        if (css != null) {
            scene.getStylesheets()
                    .add(css.toExternalForm());
        }

        return scene;
    }

    // ============================================================
    // POPUPS
    // ============================================================

    private void popups() {

        prepararTela("Popups");

        Label descricao =
                new Label(
                        "Gerencie os popups ativos e desativados da Intranet."
                );

        descricao.getStyleClass()
                .add("central-description");

        Label statusServer =
                new Label(
                        "🟡 Verificando conexão..."
                );

        statusServer.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-padding: 0 0 10 0;"
        );

        Thread verificar =
                new Thread(() -> {

                    try {

                        IntranetAvisosClient client =
                                new IntranetAvisosClient();

                        String status =
                                client.checkServerStatus();

                        Platform.runLater(() -> {

                            if (
                                    "online".equalsIgnoreCase(
                                            status
                                    )
                            ) {

                                statusServer.setText(
                                        "🟢 Servidor conectado: " +
                                                IntranetAvisosClient.baseUrl()
                                );

                                statusServer.setStyle(
                                        "-fx-text-fill: #1e9b78;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-padding: 0 0 10 0;"
                                );

                            } else {

                                statusServer.setText(
                                        "🔴 Servidor offline: " +
                                                IntranetAvisosClient.baseUrl()
                                );

                                statusServer.setStyle(
                                        "-fx-text-fill: #d64545;" +
                                                "-fx-font-weight: bold;" +
                                                "-fx-padding: 0 0 10 0;"
                                );
                            }
                        });

                    } catch (Exception e) {

                        Platform.runLater(() -> {

                            statusServer.setText(
                                    "🔴 Erro ao conectar: " +
                                            IntranetAvisosClient.baseUrl()
                            );

                            statusServer.setStyle(
                                    "-fx-text-fill: #d64545;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-padding: 0 0 10 0;"
                            );
                        });
                    }

                }, "verificar-servidor");

        verificar.setDaemon(true);

        verificar.start();

        HBox filtros =
                new HBox(10);

        filtros.setAlignment(
                Pos.CENTER_LEFT
        );

        Button todos =
                criarBotao(
                        "Todos",
                        false
                );

        Button ativos =
                criarBotao(
                        "🟢 Ativos",
                        false
                );

        Button desativados =
                criarBotao(
                        "⚪ Desativados",
                        false
                );

        Button atualizar =
                criarBotao(
                        "↻ Atualizar",
                        false
                );

        TextField pesquisa =
                new TextField();

        pesquisa.setPromptText(
                "Pesquisar popup..."
        );

        pesquisa.setPrefWidth(
                380
        );

        VBox lista =
                new VBox(14);

        lista.setFillWidth(true);

        ScrollPane scroll =
                new ScrollPane(lista);

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        VBox.setVgrow(
                scroll,
                Priority.ALWAYS
        );

        filtros.getChildren()
                .addAll(
                        todos,
                        ativos,
                        desativados,
                        atualizar
                );

        content.getChildren()
                .addAll(
                        descricao,
                        statusServer,
                        filtros,
                        pesquisa,
                        scroll
                );

        IntranetAvisosClient client =
                new IntranetAvisosClient();

        todos.setOnAction(
                e ->
                        carregarPopups(
                                lista,
                                pesquisa.getText(),
                                "TODOS",
                                client
                        )
        );

        ativos.setOnAction(
                e ->
                        carregarPopups(
                                lista,
                                pesquisa.getText(),
                                "ATIVOS",
                                client
                        )
        );

        desativados.setOnAction(
                e ->
                        carregarPopups(
                                lista,
                                pesquisa.getText(),
                                "DESATIVADOS",
                                client
                        )
        );

        atualizar.setOnAction(
                e ->
                        carregarPopups(
                                lista,
                                pesquisa.getText(),
                                "TODOS",
                                client
                        )
        );

        pesquisa.textProperty()
                .addListener(
                        (obs, antigo, novo) ->
                                carregarPopups(
                                        lista,
                                        novo,
                                        "TODOS",
                                        client
                                )
                );

        carregarPopups(
                lista,
                "",
                "TODOS",
                client
        );
    }

    // ============================================================
    // CARREGAR POPUPS
    // ============================================================

    private void carregarPopups(
            VBox lista,
            String pesquisa,
            String filtro,
            IntranetAvisosClient client) {

        lista.getChildren()
                .setAll(
                        new Label(
                                "Carregando popups..."
                        )
                );

        Thread carregar =
                new Thread(() -> {

                    try {

                        String status =
                                client.checkServerStatus();

                        if (
                                !"online".equalsIgnoreCase(
                                        status
                                )
                        ) {

                            Platform.runLater(() -> {

                                lista.getChildren()
                                        .clear();

                                Label mensagem =
                                        new Label(
                                                "❌ Servidor da Intranet " +
                                                        "não está acessível!\n\n" +
                                                        "Verifique se o servidor está rodando em:\n" +
                                                        IntranetAvisosClient.baseUrl()
                                        );

                                mensagem.setWrapText(true);

                                mensagem.setStyle(
                                        "-fx-text-fill: #d64545;" +
                                                "-fx-font-weight: bold;"
                                );

                                lista.getChildren()
                                        .add(mensagem);
                            });

                            return;
                        }

                        List<IntranetAvisosClient.Popup> popups =
                                client.listarPopups();

                        String termo =
                                pesquisa == null
                                        ? ""
                                        : pesquisa
                                        .trim()
                                        .toLowerCase();

                        List<IntranetAvisosClient.Popup> filtrados =
                                popups.stream()
                                        .filter(popup -> {

                                            boolean statusOk =
                                                    filtro.equals("TODOS") ||
                                                            (
                                                                    filtro.equals("ATIVOS") &&
                                                                            popup.ativo()
                                                            ) ||
                                                            (
                                                                    filtro.equals("DESATIVADOS") &&
                                                                            !popup.ativo()
                                                            );

                                            boolean pesquisaOk =
                                                    termo.isBlank() ||
                                                            popup.titulo()
                                                                    .toLowerCase()
                                                                    .contains(termo) ||
                                                            popup.mensagem()
                                                                    .toLowerCase()
                                                                    .contains(termo);

                                            return statusOk &&
                                                    pesquisaOk;
                                        })
                                        .toList();

                        Platform.runLater(() -> {

                            lista.getChildren()
                                    .clear();

                            if (filtrados.isEmpty()) {

                                String texto =
                                        filtro.equals("ATIVOS")
                                                ? "Nenhum popup ativo."
                                                :
                                                filtro.equals("DESATIVADOS")
                                                        ? "Nenhum popup desativado."
                                                        :
                                                        "Nenhum popup encontrado.";

                                Label vazio =
                                        new Label(texto);

                                vazio.getStyleClass()
                                        .add("muted");

                                lista.getChildren()
                                        .add(vazio);

                                return;
                            }

                            for (
                                    IntranetAvisosClient.Popup popup :
                                    filtrados
                            ) {

                                lista.getChildren()
                                        .add(
                                                criarCardPopup(
                                                        popup,
                                                        client,
                                                        lista,
                                                        pesquisa,
                                                        filtro
                                                )
                                        );
                            }
                        });

                    } catch (Exception erro) {

                        Platform.runLater(() -> {

                            lista.getChildren()
                                    .clear();

                            Label mensagem =
                                    new Label(
                                            "❌ Erro ao carregar os popups:\n\n" +
                                                    erro.getMessage()
                                    );

                            mensagem.setWrapText(true);

                            mensagem.setStyle(
                                    "-fx-text-fill: #d64545;"
                            );

                            lista.getChildren()
                                    .add(mensagem);
                        });
                    }

                }, "carregar-popups");

        carregar.setDaemon(true);

        carregar.start();
    }

    // ============================================================
    // CARD DO POPUP
    // ============================================================

    private VBox criarCardPopup(
            IntranetAvisosClient.Popup popup,
            IntranetAvisosClient client,
            VBox lista,
            String pesquisa,
            String filtro) {

        VBox card =
                new VBox(10);

        card.getStyleClass()
                .add("form-card");

        Label titulo =
                new Label(
                        popup.titulo()
                );

        titulo.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;"
        );

        // --------------------------------------------------------
        // STATUS
        // --------------------------------------------------------

        Label status =
                new Label(
                        popup.ativo()
                                ? "🟢 ATIVO"
                                : "⚪ DESATIVADO"
                );

        if (popup.ativo()) {

            status.setStyle(
                    "-fx-text-fill: #1e9b78;" +
                            "-fx-font-weight: bold;"
            );

        } else {

            status.setStyle(
                    "-fx-text-fill: #8e94a0;" +
                            "-fx-font-weight: bold;"
            );
        }

        // --------------------------------------------------------
        // MENSAGEM
        // --------------------------------------------------------

        Label mensagem =
                new Label(
                        popup.mensagem()
                );

        mensagem.setWrapText(true);

        mensagem.setMaxWidth(600);

        // --------------------------------------------------------
        // DETALHES
        // --------------------------------------------------------

        Label detalhes =
                new Label(
                        "Modelo: " +
                                popup.modelo() +
                                "   •   Tamanho: " +
                                popup.tamanho() +
                                "\nPáginas: " +
                                popup.paginas()
                );

        detalhes.getStyleClass()
                .add("muted");

        // --------------------------------------------------------
        // VISUALIZAR
        // --------------------------------------------------------

        Button visualizar =
                criarBotao(
                        "👁 Visualizar",
                        false
                );

        visualizar.setOnAction(e -> {

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setTitle(
                    "Visualizar Popup"
            );

            alert.setHeaderText(
                    popup.titulo()
            );

            alert.setContentText(
                    "📋 DETALHES DO POPUP\n\n" +
                            "ID: " +
                            popup.id() +
                            "\n\n" +
                            "Título: " +
                            popup.titulo() +
                            "\n\n" +
                            "Mensagem: " +
                            popup.mensagem() +
                            "\n\n" +
                            "Status: " +
                            (
                                    popup.ativo()
                                            ? "🟢 ATIVO"
                                            : "⚪ DESATIVADO"
                            ) +
                            "\n\n" +
                            "Modelo: " +
                            popup.modelo() +
                            "\n" +
                            "Tamanho: " +
                            popup.tamanho() +
                            "\n" +
                            "Páginas: " +
                            popup.paginas()
            );

            alert.showAndWait();
        });

        // --------------------------------------------------------
        // ALTERAR STATUS
        // --------------------------------------------------------

        Button alterarStatus;

        if (popup.ativo()) {

            alterarStatus =
                    criarBotao(
                            "⏸ Desativar",
                            false
                    );

            alterarStatus.setStyle(
                    "-fx-text-fill: #d64545;"
            );

        } else {

            alterarStatus =
                    criarBotao(
                            "▶ Ativar",
                            false
                    );

            alterarStatus.setStyle(
                    "-fx-text-fill: #1e9b78;"
            );
        }

        alterarStatus.setOnAction(e -> {

            boolean novoStatus =
                    !popup.ativo();

            String acao =
                    novoStatus
                            ? "ativar"
                            : "desativar";

            Alert confirmacao =
                    new Alert(
                            Alert.AlertType.CONFIRMATION
                    );

            confirmacao.setTitle(
                    "Alterar status do popup"
            );

            confirmacao.setHeaderText(
                    "Deseja " +
                            acao +
                            " este popup?"
            );

            confirmacao.setContentText(
                    "Popup: " +
                            popup.titulo() +
                            "\n\n" +
                            "A alteração será realizada " +
                            "diretamente no servidor."
            );

            confirmacao.showAndWait()
                    .filter(
                            resultado ->
                                    resultado ==
                                            ButtonType.OK
                    )
                    .ifPresent(resultado -> {

                        alterarStatus.setDisable(
                                true
                        );

                        Thread thread =
                                new Thread(() -> {

                                    try {

                                        client.alterarStatusPopup(
                                                popup.id(),
                                                novoStatus
                                        );

                                        Platform.runLater(() -> {

                                            carregarPopups(
                                                    lista,
                                                    pesquisa,
                                                    filtro,
                                                    client
                                            );

                                            mostrarInfo(
                                                    "Popup atualizado",
                                                    "O popup \"" +
                                                            popup.titulo() +
                                                            "\" foi " +
                                                            (
                                                                    novoStatus
                                                                            ? "ativado"
                                                                            : "desativado"
                                                            ) +
                                                            " no servidor."
                                            );
                                        });

                                    } catch (Exception erro) {

                                        Platform.runLater(() -> {

                                            alterarStatus.setDisable(
                                                    false
                                            );

                                            mostrarErro(
                                                    "Erro ao alterar popup",
                                                    erro.getMessage()
                                            );
                                        });
                                    }

                                }, "alterar-status-popup");

                        thread.setDaemon(true);

                        thread.start();
                    });
        });

        // --------------------------------------------------------
        // DELETE
        // --------------------------------------------------------

        Button excluir =
                criarBotao(
                        "🗑 Excluir",
                        false
                );

        excluir.setStyle(
                "-fx-text-fill: #d64545;"
        );

        excluir.setOnAction(e -> {

            Alert confirmacao =
                    new Alert(
                            Alert.AlertType.CONFIRMATION
                    );

            confirmacao.setTitle(
                    "Excluir popup"
            );

            confirmacao.setHeaderText(
                    "Excluir permanentemente?"
            );

            confirmacao.setContentText(
                    "O popup \"" +
                            popup.titulo() +
                            "\" será removido permanentemente " +
                            "do servidor.\n\n" +
                            "Essa ação não poderá ser desfeita."
            );

            confirmacao.showAndWait()
                    .filter(
                            resultado ->
                                    resultado ==
                                            ButtonType.OK
                    )
                    .ifPresent(resultado -> {

                        excluir.setDisable(
                                true
                        );

                        Thread thread =
                                new Thread(() -> {

                                    try {

                                        client.excluirPopup(
                                                popup.id()
                                        );

                                        Platform.runLater(() -> {

                                            carregarPopups(
                                                    lista,
                                                    pesquisa,
                                                    filtro,
                                                    client
                                            );

                                            mostrarInfo(
                                                    "Popup excluído",
                                                    "O popup \"" +
                                                            popup.titulo() +
                                                            "\" foi excluído permanentemente."
                                            );
                                        });

                                    } catch (Exception erro) {

                                        Platform.runLater(() -> {

                                            excluir.setDisable(
                                                    false
                                            );

                                            mostrarErro(
                                                    "Erro ao excluir popup",
                                                    erro.getMessage()
                                            );
                                        });
                                    }

                                }, "excluir-popup");

                        thread.setDaemon(true);

                        thread.start();
                    });
        });

        // --------------------------------------------------------
        // ABRIR NEXT
        // --------------------------------------------------------

        Button abrirNext =
                criarBotao(
                        "🌐 Abrir Next.js",
                        false
                );

        abrirNext.setOnAction(e -> {

            try {

                java.awt.Desktop
                        .getDesktop()
                        .browse(
                                new URI(
                                        IntranetAvisosClient
                                                .baseUrl()
                                )
                        );

            } catch (Exception ex) {

                mostrarErro(
                        "Erro ao abrir navegador",
                        ex.getMessage()
                );
            }
        });

        // --------------------------------------------------------
        // BOTÕES
        // --------------------------------------------------------

        HBox botoes =
                new HBox(10);

        botoes.getChildren()
                .addAll(
                        visualizar,
                        alterarStatus,
                        excluir,
                        abrirNext
                );

        // --------------------------------------------------------
        // INFO
        // --------------------------------------------------------

        Label info =
                new Label(
                        "💡 Status e exclusão são " +
                                "sincronizados com o servidor Next.js."
                );

        info.setStyle(
                "-fx-text-fill: #8e94a0;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 6 0 0 0;"
        );

        card.getChildren()
                .addAll(
                        titulo,
                        status,
                        mensagem,
                        detalhes,
                        botoes,
                        info
                );

        return card;
    }

    // ============================================================
    // HISTÓRICO
    // ============================================================

    private void historico() {

        prepararTela(
                "Histórico de Avisos"
        );

        VBox table =
                new VBox(0);

        table.getStyleClass()
                .add("history-table");

        table.getChildren()
                .add(
                        criarLinhaTabela(
                                "Título",
                                "Prioridade",
                                "Destinatários",
                                "Enviado em",
                                "Status",
                                true
                        )
                );

        for (
                Aviso aviso :
                service.listarTodos()
        ) {

            table.getChildren()
                    .add(
                            criarLinhaTabela(
                                    aviso.getTitulo(),
                                    "🔵 Normal",
                                    aviso.getAutor(),
                                    aviso.getDataPublicacao()
                                            .format(
                                                    FORMATO_DATA
                                            ),
                                    "✓ Enviado",
                                    false
                            )
                    );
        }

        content.getChildren()
                .add(table);

        Button atualizar =
                criarBotao(
                        "⟳  Atualizar",
                        false
                );

        atualizar.setOnAction(
                e -> historico()
        );

        content.getChildren()
                .add(atualizar);
    }

    private Node criarLinhaTabela(
            String a,
            String b,
            String c,
            String d,
            String e,
            boolean header) {

        GridPane grid =
                new GridPane();

        grid.getStyleClass()
                .add(
                        header
                                ? "table-head"
                                : "table-row"
                );

        for (int i = 0; i < 5; i++) {

            grid.getColumnConstraints()
                    .add(
                            criarColuna(20)
                    );
        }

        grid.add(
                new Label(a),
                0,
                0
        );

        grid.add(
                new Label(b),
                1,
                0
        );

        grid.add(
                new Label(c),
                2,
                0
        );

        grid.add(
                new Label(d),
                3,
                0
        );

        Label status =
                new Label(e);

        if (!header) {

            status.getStyleClass()
                    .add("status-sent");
        }

        grid.add(
                status,
                4,
                0
        );

        return grid;
    }

    // ============================================================
    // MONITORAMENTO
    // ============================================================

    private void monitorAcessos() {

        prepararTela(
                "Acessando Agora"
        );

        VBox card =
                new VBox(8);

        card.getStyleClass()
                .add("form-card");

        card.setAlignment(
                Pos.CENTER
        );

        card.setPadding(
                new Insets(40)
        );

        Label icon =
                new Label("👁");

        icon.getStyleClass()
                .add("popup-icon");

        monitorOnlineLabel =
                new Label("--");

        monitorOnlineLabel
                .getStyleClass()
                .add(
                        "live-counter-number"
                );

        Label legenda =
                new Label(
                        "pessoas vendo a intranet agora"
                );

        legenda.getStyleClass()
                .add("muted");

        HBox liveRow =
                new HBox(6);

        liveRow.setAlignment(
                Pos.CENTER
        );

        Label liveDot =
                new Label("●");

        liveDot.getStyleClass()
                .add("status-sent");

        monitorStatusLabel =
                new Label(
                        "Conectando ao servidor..."
                );

        monitorStatusLabel
                .getStyleClass()
                .add("muted");

        liveRow.getChildren()
                .addAll(
                        liveDot,
                        monitorStatusLabel
                );

        card.getChildren()
                .addAll(
                        icon,
                        monitorOnlineLabel,
                        legenda,
                        liveRow
                );

        content.getChildren()
                .add(card);

        HBox stats =
                new HBox(12);

        monitorAcessosHojeLabel =
                criarStatValor();

        monitorTotalVisitantesLabel =
                criarStatValor();

        monitorUltimaConexaoLabel =
                criarStatValor();

        stats.getChildren()
                .addAll(
                        criarStatCard(
                                "Acessos hoje",
                                monitorAcessosHojeLabel
                        ),
                        criarStatCard(
                                "Total de visitantes",
                                monitorTotalVisitantesLabel
                        ),
                        criarStatCard(
                                "Última conexão",
                                monitorUltimaConexaoLabel
                        )
                );

        content.getChildren()
                .add(stats);

        iniciarMonitoramento();
    }

    private Label criarStatValor() {

        Label label =
                new Label("--");

        label.getStyleClass()
                .add("stat-number");

        return label;
    }

    private Node criarStatCard(
            String texto,
            Label valor) {

        Label rotulo =
                new Label(texto);

        rotulo.getStyleClass()
                .add("muted");

        VBox box =
                new VBox(
                        6,
                        valor,
                        rotulo
                );

        box.getStyleClass()
                .add("stat-card");

        HBox.setHgrow(
                box,
                Priority.ALWAYS
        );

        return box;
    }

    private void iniciarMonitoramento() {

        Runnable atualizar =
                criarAtualizadorMonitor();

        atualizar.run();

        monitorTimeline =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(
                                        MONITOR_INTERVAL_SECONDS
                                ),
                                e -> atualizar.run()
                        )
                );

        monitorTimeline.setCycleCount(
                Timeline.INDEFINITE
        );

        monitorTimeline.play();
    }

    private Runnable criarAtualizadorMonitor() {

        return () -> {

            Thread t =
                    new Thread(() -> {

                        try {

                            IntranetAvisosClient.EstatisticasAcesso stat =
                                    new IntranetAvisosClient()
                                            .buscarEstatisticasAcesso();

                            Platform.runLater(
                                    () ->
                                            atualizarInterfaceMonitor(
                                                    stat
                                            )
                            );

                        } catch (Exception e) {

                            Platform.runLater(() ->
                                    monitorStatusLabel
                                            .setText(
                                                    "Falha na conexão com o servidor"
                                            )
                            );
                        }

                    }, "monitor-acessos");

            t.setDaemon(true);

            t.start();
        };
    }

    private void atualizarInterfaceMonitor(
            IntranetAvisosClient.EstatisticasAcesso stat) {

        monitorOnlineLabel.setText(
                String.valueOf(
                        stat.onlineAgora()
                )
        );

        monitorAcessosHojeLabel.setText(
                String.valueOf(
                        stat.acessosHoje()
                )
        );

        monitorTotalVisitantesLabel.setText(
                String.valueOf(
                        stat.totalVisitantes()
                )
        );

        monitorUltimaConexaoLabel.setText(
                formatarUltimaConexao(
                        stat.ultimaConexao()
                )
        );

        monitorStatusLabel.setText(
                "Atualizado às " +
                        LocalTime.now()
                                .format(
                                        DateTimeFormatter.ofPattern(
                                                "HH:mm:ss"
                                        )
                                )
        );
    }

    private void pararMonitor() {

        if (monitorTimeline != null) {

            monitorTimeline.stop();

            monitorTimeline = null;
        }
    }

    // ============================================================
    // MENSAGEM DO DIA
    // ============================================================

    private void abrirMensagemDoDia(
            Stage stage) {

        prepararTela(
                "Mensagem do Dia"
        );

        Node tela =
                MensagemDoDiaView.criar(
                        stage
                );

        content.getChildren()
                .add(tela);
    }

    // ============================================================
    // CONFIGURAÇÕES
    // ============================================================

    private void configuracoes() {

        prepararTela("Configurações");

        VBox config = new VBox(14);

        config.getStyleClass()
                .add("form-card");

        TextField endereco = new TextField(
                IntranetAvisosClient.baseUrl()
        );

        endereco.setPromptText(
                "https://intranet.exemplo.gov.br"
        );

        Label feedback = new Label();

        feedback.getStyleClass()
                .add("muted");

        Button salvar = criarBotao(
                "Salvar conexão",
                true
        );

        salvar.setOnAction(
                event -> salvarConexao(
                        endereco,
                        feedback
                )
        );

        config.getChildren().addAll(

                criarLabel(
                        "Endereço do site da Intranet"
                ),

                endereco,

                criarLabel(
                        "Informe apenas a URL base da Intranet."
                ),

                salvar,

                feedback
        );

        content.getChildren()
                .add(config);
    }

    private void salvarConexao(
            TextField endereco,
            Label feedback) {

        String url = endereco.getText();

        if (url == null || url.isBlank()) {

            feedback.setText(
                    "Informe o endereço da Intranet."
            );

            return;
        }

        url = url.trim();

        try {

            IntranetAvisosClient.configurarBaseUrl(
                    url
            );

            feedback.setText(
                    "✓ Endereço salvo: " +
                            IntranetAvisosClient.baseUrl()
            );

        } catch (IllegalArgumentException error) {

            feedback.setText(
                    error.getMessage()
            );
        }
    }

    private void testarESalvarConexao(
            TextField endereco,
            Label feedback,
            Button salvar) {

        try {

            IntranetAvisosClient
                    .configurarBaseUrl(
                            endereco.getText()
                    );

        } catch (
                IllegalArgumentException error) {

            feedback.setText(
                    error.getMessage()
            );

            return;
        }

        salvar.setDisable(true);

        feedback.setText(
                "Testando conexão..."
        );

        Thread teste =
                new Thread(() -> {

                    try {

                        new IntranetAvisosClient()
                                .buscarEstatisticasAcesso();

                        Platform.runLater(() -> {

                            feedback.setText(
                                    "Conexão estabelecida com " +
                                            IntranetAvisosClient.baseUrl()
                            );

                            salvar.setDisable(false);
                        });

                    } catch (Exception error) {

                        Platform.runLater(() -> {

                            feedback.setText(
                                    "Não foi possível conectar: " +
                                            error.getMessage()
                            );

                            salvar.setDisable(false);
                        });
                    }

                }, "teste-conexao-intranet");

        teste.setDaemon(true);

        teste.start();
    }

    // ============================================================
    // MENSAGENS
    // ============================================================

    private void mostrarInfo(
            String titulo,
            String mensagem) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(titulo);

        alert.setHeaderText(null);

        alert.setContentText(
                mensagem
        );

        alert.showAndWait();
    }

    private void mostrarErro(
            String titulo,
            String mensagem) {

        Alert alert =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alert.setTitle(titulo);

        alert.setHeaderText(null);

        alert.setContentText(
                mensagem == null ||
                        mensagem.isBlank()
                        ? "Ocorreu um erro desconhecido."
                        : mensagem
        );

        alert.showAndWait();
    }

    // ============================================================
    // UTILITÁRIOS
    // ============================================================

    private static String formatarUltimaConexao(
            String isoUtc) {

        if (
                isoUtc == null ||
                        isoUtc.isBlank()
        ) {
            return "—";
        }

        try {

            return Instant.parse(
                            isoUtc
                    )
                    .atZone(
                            ZoneId.systemDefault()
                    )
                    .format(
                            DateTimeFormatter.ofPattern(
                                    "dd/MM HH:mm:ss"
                            )
                    );

        } catch (Exception e) {

            return "—";
        }
    }
}