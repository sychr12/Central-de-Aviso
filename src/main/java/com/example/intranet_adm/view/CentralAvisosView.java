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
import javafx.animation.PauseTransition;
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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXCheckbox;
import io.github.palexdev.materialfx.controls.MFXDatePicker;
import io.github.palexdev.materialfx.controls.MFXScrollPane;
import io.github.palexdev.materialfx.controls.MFXTextField;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public final class CentralAvisosView {

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

    private final AvisoService service =
            new AvisoService();

    private final BorderPane root =
            new BorderPane();

    private final VBox content =
            new VBox(CARD_SPACING);

    private final List<MFXButton> botoesNavegacao =
            new ArrayList<>();

    private MFXTextField tituloField;

    private TextArea descricaoArea;

    private ComboBox<String> ativoCombo;
    private ComboBox<String> podeFecharCombo;

    private TextArea mensagemArea;

    private ComboBox<String> prioridadeCombo;

    private MFXTextField linkIntranetField;

    private Label feedbackLabel;

    private MFXButton enviarButton;

    private MFXCheckbox agendarDataCheck;

    private VBox datasContainer;

    private MFXDatePicker publicarDataPicker;

    private MFXTextField publicarHoraField;

    private MFXDatePicker expirarDataPicker;

    private MFXTextField expirarHoraField;

    private Path imagemSelecionadaPath;

    private Label imagemNomeLabel;

    private ImageView imagemPreview;

    private MFXButton removerImagemButton;

    private Timeline monitorTimeline;

    private Label monitorOnlineLabel;

    private Label monitorAcessosHojeLabel;

    private Label monitorTotalVisitantesLabel;

    private Label monitorUltimaConexaoLabel;

    private Label monitorStatusLabel;

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

    private void configurarEstilos() {

        root.getStyleClass()
                .add("central-root");
    }

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

    private MFXScrollPane criarScrollArea() {

        MFXScrollPane scroll =
                new MFXScrollPane(content);

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

    private MFXButton criarNavButton(
            String texto,
            Runnable acao,
            boolean ativo) {

        MFXButton button =
                new MFXButton(texto);

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.getStyleClass().add(
                ativo ? "central-nav-active" : "central-nav"
        );

        botoesNavegacao.add(button);

        button.setOnAction(
                e -> {
                    ativarNavegacao(button);
                    acao.run();
                }
        );

        return button;
    }

    private void ativarNavegacao(MFXButton selecionado) {

        for (MFXButton botao : botoesNavegacao) {
            botao.getStyleClass().remove("central-nav-active");
            if (!botao.getStyleClass().contains("central-nav")) {
                botao.getStyleClass().add("central-nav");
            }
        }

        selecionado.getStyleClass().remove("central-nav");
        selecionado.getStyleClass().add("central-nav-active");
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

    private void prepararTela(
            String titulo) {

        pararMonitor();

        content.getChildren()
                .clear();

        Label contexto =
                new Label("CENTRAL DE COMUNICAÇÃO");

        contexto.getStyleClass().add("page-eyebrow");

        Label cabecalho =
                new Label(titulo);

        cabecalho.getStyleClass()
                .add("central-page-title");

        Label descricao = new Label(descricaoDaPagina(titulo));

        descricao.getStyleClass().add("page-description");

        VBox tituloPagina = new VBox(5, contexto, cabecalho, descricao);

        tituloPagina.getStyleClass().add("page-heading");

        content.getChildren()
                .add(tituloPagina);
    }

    private String descricaoDaPagina(String titulo) {
        return switch (titulo) {
            case "Novo Aviso" -> "Crie uma comunicação clara e escolha como ela será exibida na Intranet.";
            case "Popups" -> "Acompanhe, pesquise e gerencie os avisos publicados.";
            case "Histórico" -> "Consulte o registro de comunicações enviadas pela Central.";
            case "Acessando Agora" -> "Veja em tempo real a movimentação de visitantes na Intranet.";
            case "Mensagem do Dia" -> "Defina a mensagem de destaque exibida aos colaboradores.";
            case "Configurações" -> "Configure a conexão segura com o servidor da Intranet.";
            default -> "Gerencie as comunicações da sua equipe em um só lugar.";
        };
    }

    private void novoAviso() {

        prepararTela(
                "Novo Aviso"
        );

        VBox card =
                criarCardFormulario();

        Label tituloFormulario = new Label("Detalhes do aviso");
        tituloFormulario.getStyleClass().add("form-title");

        Label ajudaFormulario = new Label(
                "Preencha as informações abaixo. Os campos principais serão enviados para todos os usuários da Intranet."
        );
        ajudaFormulario.setWrapText(true);
        ajudaFormulario.getStyleClass().add("form-description");

        VBox cabecalhoFormulario = new VBox(4, tituloFormulario, ajudaFormulario);
        cabecalhoFormulario.getStyleClass().add("form-heading");

        GridPane grid =
                criarGridFormulario();

        configurarCamposFormulario();

        adicionarCamposBasicos(grid);

        adicionarCampoImagem(grid);

        adicionarCampoLink(grid);

        card.getChildren().addAll(cabecalhoFormulario, grid);

        card.getChildren()
                .add(
                        criarAreaAcoes()
                );

        HBox editor = new HBox(22, card, criarResumoPublicacao());

        editor.getStyleClass().add("editor-layout");

        HBox.setHgrow(card, Priority.ALWAYS);

        card.setMaxWidth(Double.MAX_VALUE);

        content.getChildren().add(editor);
    }

    private VBox criarResumoPublicacao() {

        Label estado = new Label("RASCUNHO");
        estado.getStyleClass().add("summary-status");

        Label titulo = new Label("Pronto para comunicar?");
        titulo.getStyleClass().add("summary-title");

        Label descricao = new Label(
                "Revise o conteúdo antes de enviar o aviso para a Intranet."
        );
        descricao.setWrapText(true);
        descricao.getStyleClass().add("summary-description");

        Label guia = new Label("ANTES DE PUBLICAR");
        guia.getStyleClass().add("summary-eyebrow");

        Label dicaTitulo = new Label("✓  Use um título objetivo e fácil de identificar.");
        Label dicaMensagem = new Label("✓  Informe o prazo e a ação esperada, quando houver.");
        Label dicaLink = new Label("✓  Inclua um link para orientar os usuários.");

        dicaTitulo.getStyleClass().add("summary-tip");
        dicaMensagem.getStyleClass().add("summary-tip");
        dicaLink.getStyleClass().add("summary-tip");

        VBox resumo = new VBox(
                14,
                estado,
                titulo,
                descricao,
                guia,
                dicaTitulo,
                dicaMensagem,
                dicaLink
        );

        resumo.getStyleClass().add("publication-summary");
        resumo.setPrefWidth(270);
        resumo.setMinWidth(240);

        return resumo;
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

    private MFXButton criarBotao(
            String texto,
            boolean primario) {

        MFXButton button =
                new MFXButton(texto);

        button.getStyleClass()
                .add(
                        primario
                                ? "send-button"
                                : "soft-button"
                );

        return button;
    }

    private void configurarCamposFormulario() {

        tituloField =
                new MFXTextField();

        tituloField.setPromptText(
                "Digite um título para o aviso"
        );

        descricaoArea =
                new TextArea();

        descricaoArea.setPromptText(
                "Digite a mensagem que deseja anunciar"
        );

        descricaoArea.setPrefRowCount(6);

        descricaoArea.setWrapText(true);

        mensagemArea =
                descricaoArea;

        ativoCombo =
                new ComboBox<>();

        ativoCombo.getItems().addAll(
                "Sim",
                "Não"
        );

        ativoCombo.getSelectionModel().select("Sim");

        podeFecharCombo =
                new ComboBox<>();

        podeFecharCombo.getItems().addAll(
                "Sim",
                "Não"
        );

        podeFecharCombo.getSelectionModel().select("Sim");

        prioridadeCombo =
                new ComboBox<>();

        prioridadeCombo.getItems().addAll(
                "🔴  Urgente",
                "🟠  Importante",
                "🔵  Normal"
        );

        prioridadeCombo.getSelectionModel().select(0);

        linkIntranetField =
                new MFXTextField();

        linkIntranetField.setPromptText(
                "https://intranet.empresa.com/comunicados/manutencao"
        );

        configurarLarguraCamposFormulario();
    }

    private void configurarLarguraCamposFormulario() {

        List<Control> campos = List.of(
                tituloField,
                descricaoArea,
                ativoCombo,
                podeFecharCombo,
                prioridadeCombo,
                linkIntranetField
        );

        for (Control campo : campos) {
            campo.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(campo, Priority.ALWAYS);
        }
    }

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
                criarLabel("Ativo"),
                0,
                row
        );

        grid.add(
                ativoCombo,
                0,
                ++row
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

    private void adicionarCampoDatas(
            GridPane grid,
            int rowInicial) {

        int row =
                rowInicial + 1;

        agendarDataCheck =
                new MFXCheckbox(
                        "Agendar data de publicação/expiração"
                );

        agendarDataCheck.setSelected(false);

        agendarDataCheck.getStyleClass()
                .add("central-label");

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

        // --- LAYOUT CORRIGIDO COM ALINHAMENTO VERTICAL PERFEITO ---
        HBox publicarRow = new HBox(publicarDataPicker, publicarHoraField);
        HBox expirarRow = new HBox(expirarDataPicker, expirarHoraField);

        publicarRow.setSpacing(12);
        expirarRow.setSpacing(12);

        // CENTER alinha perfeitamente o DatePicker e o TextField na mesma linha
        publicarRow.setAlignment(Pos.CENTER_LEFT);
        publicarRow.setStyle("-fx-alignment: center-left;");

        expirarRow.setAlignment(Pos.CENTER_LEFT);
        expirarRow.setStyle("-fx-alignment: center-left;");

        // --- ADICIONE ISSO PARA SUBIR O CAMPO DE HORA ---
        publicarHoraField.setTranslateY(-4);
        expirarHoraField.setTranslateY(-4);

        // ---------------------------------------------------------

        VBox publicarBox =
                new VBox(
                        0,
                        criarLabel(
                                "Data de início da visibilidade"
                        ),
                        publicarRow
                );

        VBox expirarBox =
                new VBox(
                        0,
                        criarLabel(
                                "Data de término da visibilidade"
                        ),
                        expirarRow
                );

        HBox dates =
                new HBox(
                        30,
                        publicarBox,
                        expirarBox
                );

        dates.getStyleClass()
                .add("dates-row");

        Label agendarAjuda =
                new Label(
                        "Desmarcado: o aviso é publicado imediatamente " +
                                "e não expira sozinho."
                );

        agendarAjuda.getStyleClass()
                .add("muted");

        datasContainer =
                new VBox(
                        0,
                        dates
                );

        datasContainer.setVisible(false);

        datasContainer.setManaged(false);

        agendarDataCheck.selectedProperty()
                .addListener(
                        (obs, antigo, marcado) -> {

                            datasContainer.setVisible(
                                    marcado
                            );

                            datasContainer.setManaged(
                                    marcado
                            );
                        }
                );

        VBox agendarBox =
                new VBox(
                        6,
                        agendarDataCheck,
                        agendarAjuda,
                        datasContainer
                );

        grid.add(
                agendarBox,
                0,
                row,
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

        MFXButton selecionarImagemButton =
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
            MFXButton selecionarButton,
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

    private MFXDatePicker criarDatePicker(
            LocalDate valorInicial) {

        // MFXDatePicker não tem construtor com LocalDate: o valor é
        // definido depois via setValue()/getValue(), assim como um
        // DatePicker comum.
        MFXDatePicker picker =
                new MFXDatePicker();

        if (valorInicial != null) {
            picker.setValue(valorInicial);
        }

        picker.setPromptText(
                "dd/mm/aaaa"
        );

        picker.setPrefWidth(140);

        forcarCorDoPlaceholder(picker, "dd/mm/aaaa");

        return picker;
    }

    // ============================================================
    // MÉTODO CORRIGIDO: CAMPO DE HORA MAIOR E SEM RETÂNGULO
    // ============================================================
    private MFXTextField criarCampoHora(
            String valorInicial) {

        MFXTextField campo =
                new MFXTextField(
                        valorInicial
                );

        campo.setPromptText(
                "HH:mm"
        );

        // --- CORREÇÃO DO TAMANHO E DO RETÂNGULO ---
        campo.setPrefWidth(100);
        campo.setMinWidth(85);
        campo.setMaxWidth(120);

        // Aplica transparência direta no código para garantir que o retângulo suma
        // Aplica transparência e empurra o campo 4px para CIMA para alinhar com o DatePicker
        campo.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0 0 4 0; -fx-translate-y: -8px;");
        // -----------------------------

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

        forcarCorDoPlaceholder(campo, "HH:mm");

        return campo;
    }

    /**
     * Corrige à força a cor do texto de placeholder ("dica") dos campos
     * MaterialFX (MFXTextField, MFXDatePicker...).
     * <p>
     * Tentativas anteriores usando seletores de style class fixos
     * (".floating-text", ".prompt-text" etc.) não funcionaram porque esses
     * nomes internos variam entre versões da biblioteca MaterialFX — a
     * partir da 11.14.0, por exemplo, o "Theming System" foi reescrito por
     * completo.
     * <p>
     * Em vez de depender do nome da style class, aqui percorremos toda a
     * árvore de nós internos do campo procurando o nó de texto (Text ou
     * Label) cujo conteúdo é EXATAMENTE igual ao placeholder configurado
     * (por exemplo "HH:mm" ou "dd/mm/aaaa"). Isso funciona independente de
     * como a skin da versão instalada nomeia ou organiza esse nó, porque a
     * única coisa garantida é que o texto exibido bate com o promptText.
     * <p>
     * Como a skin de alguns controles MaterialFX só termina de montar os
     * nós internos um pouco depois do primeiro layout, a busca é repetida
     * algumas vezes (imediatamente, e depois em pequenos atrasos) até
     * achar e colorir o nó, ou desistir após ~1.5s.
     */
    private void forcarCorDoPlaceholder(Control campo, String textoPlaceholder) {

        String corHex = "#969e8e"; // equivalente a -fx-ink-subtle

        int[] tentativas = {0};
        int maxTentativas = 8;

        Runnable[] aplicarRef = new Runnable[1];

        aplicarRef[0] = () -> {

            boolean achou =
                    colorirNoComTexto(
                            campo,
                            textoPlaceholder,
                            corHex
                    );

            tentativas[0]++;

            if (!achou && tentativas[0] < maxTentativas) {

                PauseTransition espera =
                        new PauseTransition(
                                Duration.millis(200)
                        );

                espera.setOnFinished(
                        e -> aplicarRef[0].run()
                );

                espera.play();
            }
        };

        campo.sceneProperty().addListener(
                (obs, cenaAntiga, novaCena) -> {

                    if (novaCena != null) {
                        tentativas[0] = 0;
                        Platform.runLater(aplicarRef[0]);
                    }
                }
        );

        // Reaplica quando o foco muda: em alguns modos (floating label
        // "INLINE"/"BORDER"), o nó de texto do placeholder pode ser
        // recriado/reposicionado ao focar e desfocar o campo.
        campo.focusedProperty().addListener(
                (obs, focadoAntes, focadoAgora) -> {

                    tentativas[0] = 0;
                    Platform.runLater(aplicarRef[0]);
                }
        );

        if (campo.getScene() != null) {
            Platform.runLater(aplicarRef[0]);
        }
    }

    /**
     * Procura recursivamente, dentro da árvore de nós de "raiz", um nó de
     * texto (Text ou Label) cujo conteúdo seja exatamente igual a "alvo" e
     * aplica a cor informada. Retorna true se encontrou e coloriu.
     */
    private boolean colorirNoComTexto(
            Node raiz,
            String alvo,
            String corHex) {

        if (raiz instanceof Text texto) {

            if (alvo.equals(texto.getText())) {

                texto.setStyle(
                        "-fx-fill: " + corHex + ";"
                );

                return true;
            }
        }

        if (raiz instanceof Label label) {

            if (alvo.equals(label.getText())) {

                label.setStyle(
                        "-fx-text-fill: " + corHex + ";"
                );

                return true;
            }
        }

        if (raiz instanceof Parent pai) {

            for (Node filho : pai.getChildrenUnmodifiable()) {

                if (colorirNoComTexto(filho, alvo, corHex)) {
                    return true;
                }
            }
        }

        return false;
    }

    private HBox criarAreaAcoes() {

        HBox actions =
                new HBox(12);

        actions.setAlignment(
                Pos.CENTER_RIGHT
        );

        actions.getStyleClass().add("form-actions");

        MFXButton previewButton =
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

        if (agendarDataCheck.isSelected() && expiraEm == null) {

            feedbackLabel.setText(
                    "Informe a data e o horário de término ou desmarque o agendamento."
            );

            return false;
        }

        if (expiraEm != null && !expiraEm.isAfter(publicaEm)) {

            feedbackLabel.setText(
                    "A expiração deve ser depois da publicação."
            );

            return false;
        }

        return true;
    }

    private LocalDateTime obterDataPublicacao() {

        if (
                agendarDataCheck == null ||
                        !agendarDataCheck.isSelected()
        ) {
            return LocalDateTime.now();
        }

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

        if (
                agendarDataCheck == null ||
                        !agendarDataCheck.isSelected()
        ) {
            return null;
        }

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

        LocalDateTime publicarEm =
                obterDataPublicacao();

        LocalDateTime expirarEm =
                obterDataExpiracao();

        boolean ativo =
                "Sim".equals(ativoCombo.getValue());

        boolean podeFechar =
                "Sim".equals(podeFecharCombo.getValue());

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
                                        .comAtivo(
                                                ativo
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

        HBox header =
                new HBox(10);

        header.setAlignment(
                Pos.CENTER_LEFT
        );

        Label icon =
                new Label("❕");

        icon.getStyleClass()
                .add("popup-icon");

        Label titulo =
                new Label(
                        tituloField.getText()
                );

        titulo.getStyleClass()
                .add("popup-title");

        header.getChildren()
                .addAll(
                        icon,
                        titulo
                );

        Label mensagem =
                new Label(
                        mensagemArea.getText()
                );

        mensagem.setWrapText(true);

        mensagem.setMaxWidth(
                Double.MAX_VALUE
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
                "Sim".equals(podeFecharCombo.getValue())
        ) {

            MFXButton fechar =
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

            aviso.getStyleClass()
                    .add("muted");

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

        int width = 460;

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

        MFXButton todos =
                criarBotao(
                        "Todos",
                        false
                );

        MFXButton ativos =
                criarBotao(
                        "🟢 Ativos",
                        false
                );

        MFXButton desativados =
                criarBotao(
                        "⚪ Desativados",
                        false
                );

        MFXButton atualizar =
                criarBotao(
                        "↻ Atualizar",
                        false
                );

        MFXTextField pesquisa =
                new MFXTextField();

        pesquisa.setPromptText(
                "Pesquisar popup..."
        );

        pesquisa.setPrefWidth(
                380
        );

        VBox lista =
                new VBox(14);

        lista.setFillWidth(true);

        MFXScrollPane scroll =
                new MFXScrollPane(lista);

        scroll.setFitToWidth(true);

        scroll.setHbarPolicy(
                MFXScrollPane.ScrollBarPolicy.NEVER
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

        Label mensagem =
                new Label(
                        popup.mensagem()
                );

        mensagem.setWrapText(true);

        mensagem.setMaxWidth(600);

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

        MFXButton visualizar =
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

        MFXButton alterarStatus;

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

        MFXButton excluir =
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

        MFXButton abrirNext =
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

        HBox botoes =
                new HBox(10);

        botoes.getChildren()
                .addAll(
                        visualizar,
                        alterarStatus,
                        excluir,
                        abrirNext
                );

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

        MFXButton atualizar =
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

    private void monitorAcessos() {

        prepararTela(
                "Acessando Agora"
        );

        VBox card =
                new VBox(10);

        card.getStyleClass()
                .add("dashboard-live-card");

        card.setAlignment(
                Pos.CENTER
        );

        card.setPadding(
                new Insets(24)
        );

        Label chip = new Label("●  AO VIVO");

        chip.getStyleClass().add("dashboard-chip");

        Label icon = new Label("👁");

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
                        "Pessoas navegando na Intranet agora"
                );

        legenda.getStyleClass()
                .add("dashboard-label");

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
                        chip,
                        icon,
                        monitorOnlineLabel,
                        legenda,
                        liveRow
                );

        content.getChildren()
                .add(card);

        HBox stats =
                new HBox(12);

        stats.setMaxWidth(Double.MAX_VALUE);

        stats.getStyleClass().add("dashboard-stats");

        monitorAcessosHojeLabel =
                criarStatValor();

        monitorTotalVisitantesLabel =
                criarStatValor();

        monitorUltimaConexaoLabel =
                criarStatValor();

        stats.getChildren()
                .addAll(
                        criarStatCard(
                                "◷",
                                "Acessos hoje",
                                monitorAcessosHojeLabel
                        ),
                        criarStatCard(
                                "◉",
                                "Total de visitantes",
                                monitorTotalVisitantesLabel
                        ),
                        criarStatCard(
                                "↗",
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
            String icone,
            String texto,
            Label valor) {

        Label simbolo = new Label(icone);

        simbolo.getStyleClass().add("stat-icon");

        Label rotulo =
                new Label(texto);

        rotulo.getStyleClass()
                .add("muted");

        VBox dados = new VBox(5, valor, rotulo);

        HBox box = new HBox(14, simbolo, dados);

        box.setAlignment(Pos.CENTER_LEFT);

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

    private void configuracoes() {

        prepararTela("Configurações");

        VBox config = new VBox(14);

        config.getStyleClass()
                .add("form-card");

        MFXTextField endereco = new MFXTextField(
                IntranetAvisosClient.baseUrl()
        );

        endereco.setMaxWidth(Double.MAX_VALUE);

        endereco.setPromptText(
                "https://intranet.exemplo.gov.br"
        );

        Label feedback = new Label();

        feedback.getStyleClass()
                .add("muted");

        MFXButton salvar = criarBotao(
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
            MFXTextField endereco,
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
            MFXTextField endereco,
            Label feedback,
            MFXButton salvar) {

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