package com.example.intranet_adm.view;

import com.example.intranet_adm.service.AvisoService;
import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.view.aviso.AvisoFormView;
import com.example.intranet_adm.view.components.AppIcon;
import com.example.intranet_adm.view.configuracao.ConfiguracoesView;
import com.example.intranet_adm.view.historico.HistoricoView;
import com.example.intranet_adm.view.mensagem.MensagemDoDiaView;
import com.example.intranet_adm.view.monitor.MonitorAcessosView;
import com.example.intranet_adm.view.popup.PopupView;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXScrollPane;

import java.util.ArrayList;
import java.util.List;

public final class CentralAvisosView {

    private static final int SIDEBAR_WIDTH = 210;
    private static final int CARD_SPACING = 16;

    private final BorderPane root =
            new BorderPane();

    private final VBox content =
            new VBox(CARD_SPACING);

    private final List<MFXButton> botoesNavegacao =
            new ArrayList<>();

    // ============================================================
    // SERVIÇOS COMPARTILHADOS
    // ============================================================

    private final IntranetAvisosClient client;

    private final AvisoService avisoService;
    private MonitorAcessosView monitorView;
    private AvisoFormView formView;

    // ============================================================
    // CONSTRUTOR
    // ============================================================

    public CentralAvisosView() {

        client = new IntranetAvisosClient();

        avisoService = new AvisoService();
    }

    // ============================================================
    // CRIAR
    // ============================================================

    public static Parent criar(Stage stage) {

        return new CentralAvisosView()
                .montar(stage);
    }

    // ============================================================
    // MONTAR
    // ============================================================

    private Parent montar(Stage stage) {

        configurarEstilos();

        configurarLayout(stage);

        stage.setOnHidden(event -> { if (monitorView != null) monitorView.pararAtualizacaoAutomatica(); });
        novoAviso();

        return root;
    }

    // ============================================================
    // ESTILOS
    // ============================================================

    private void configurarEstilos() {
        root.getStylesheets().add(getClass()
                .getResource(\u0022/com/example/intranet_adm/redesign.css\u0022)
                .toExternalForm());

        if (!root.getStyleClass().contains("central-root")) {

            root.getStyleClass()
                    .add("central-root");
        }
    }

    // ============================================================
    // LAYOUT
    // ============================================================

    private void configurarLayout(Stage stage) {

        root.setLeft(
                criarMenu(stage)
        );

        root.setCenter(
                criarScrollArea()
        );
    }

    // ============================================================
    // ÁREA DE ROLAGEM
    // ============================================================

    private MFXScrollPane criarScrollArea() {

        MFXScrollPane scroll =
                new MFXScrollPane(content);

        scroll.setFitToWidth(true);

        scroll.getStyleClass()
                .add("central-scroll");

        content.setPadding(new Insets(18, 24, 28, 24));

        content.setFillWidth(true);

        content.getStyleClass()
                .add("central-content");

        return scroll;
    }

    // ============================================================
    // CABEÇALHO
    // ============================================================

    private Node criarCabecalho() {

        HBox bar =
                new HBox();

        bar.setAlignment(
                Pos.CENTER_LEFT
        );

        bar.setSpacing(10);

        bar.getStyleClass()
                .add("central-topbar");

        Label icone =
                new Label("✦");

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

        bar.getChildren()
                .addAll(
                        icone,
                        titulo,
                        spacer,
                        controles
                );

        return bar;
    }

    // ============================================================
    // MENU LATERAL
    // ============================================================

    private Node criarMenu(Stage stage) {

        VBox menu =
                new VBox(5);

        menu.setPrefWidth(
                SIDEBAR_WIDTH
        );

        menu.setMinWidth(
                SIDEBAR_WIDTH
        );

        menu.setMaxWidth(
                SIDEBAR_WIDTH
        );

        menu.getStyleClass()
                .add("central-sidebar");

        HBox marca = new HBox(10, criarLogo(), criarIdentidade());
        marca.setAlignment(Pos.CENTER_LEFT);
        marca.getStyleClass().add(\u0022central-brand-row\u0022);

        menu.getChildren().addAll(
                        marca,

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

        menu.getChildren().add(1, criarTituloSecao(\u0022COMUNICA\u00c7\u00c3O\u0022));
        menu.getChildren().add(5, criarTituloSecao(\u0022ADMINISTRA\u00c7\u00c3O\u0022));

        Region spacer =
                new Region();

        VBox.setVgrow(
                spacer,
                Priority.ALWAYS
        );

        menu.getChildren()
                .add(spacer);

        menu.getChildren()
                .add(
                        criarNavButton(
                                "⇥   Sair",
                                stage::close,
                                false
                        )
                );

        menu.getChildren()
                .add(
                        criarStatusLabel()
                );

        return menu;
    }

    // ============================================================
    // LOGO
    // ============================================================

    private Label criarLogo() {

        Label logo =
                new Label("✦");

        logo.getStyleClass()
                .add("central-logo");

        logo.setText(\u0022\u0022);
        logo.setGraphic(AppIcon.create(AppIcon.Type.BELL, 21));
        logo.setAlignment(Pos.CENTER);
        logo.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        logo.setGraphicTextGap(0);
        return logo;
    }

    // ============================================================
    // TÍTULO DO MENU
    // ============================================================

    private VBox criarIdentidade() {
        Label titulo = new Label("CENTRAL\nComunicação interna");
        titulo.getStyleClass().add("central-brand");
        titulo.setText(\u0022Central\nIntranet IDAM\u0022);
        VBox identidade = new VBox(titulo);
        identidade.getStyleClass().add("brand-block");
        return identidade;
    }

    // ============================================================
    // BOTÃO DE NAVEGAÇÃO
    // ============================================================

    private MFXButton criarNavButton(
            String texto,
            Runnable acao,
            boolean ativo
    ) {

        String rotulo = texto.substring(texto.indexOf(' ') + 1).trim();
        MFXButton button =
                new MFXButton(rotulo);

        button.setGraphic(AppIcon.create(iconeNavegacao(rotulo), 18));
        button.setGraphicTextGap(11);

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.getStyleClass()
                .add(
                        ativo
                                ? "central-nav-active"
                                : "central-nav"
                );

        botoesNavegacao.add(
                button
        );

        button.setOnAction(
                event -> {

                    ativarNavegacao(button);

                    acao.run();
                }
        );

        return button;
    }

    private Label criarTituloSecao(String texto) {
        Label titulo = new Label(texto);
        titulo.getStyleClass().add(\u0022sidebar-section-title\u0022);
        return titulo;
    }

    private AppIcon.Type iconeNavegacao(String rotulo) {
        if (rotulo.contains(\u0022Novo\u0022)) return AppIcon.Type.PLUS;
        if (rotulo.contains(\u0022Popups\u0022)) return AppIcon.Type.POPUP;
        if (rotulo.contains(\u0022Hist\u00f3rico\u0022)) return AppIcon.Type.HISTORY;
        if (rotulo.contains(\u0022Acessando\u0022)) return AppIcon.Type.USERS;
        if (rotulo.contains(\u0022Mensagem\u0022)) return AppIcon.Type.MESSAGE;
        if (rotulo.contains(\u0022Configura\u00e7\u00f5es\u0022)) return AppIcon.Type.SETTINGS;
        return AppIcon.Type.LOGOUT;
    }

    // ============================================================
    // ATIVAR NAVEGAÇÃO
    // ============================================================

    private void ativarNavegacao(
            MFXButton selecionado
    ) {

        for (
                MFXButton botao :
                botoesNavegacao
        ) {

            botao.getStyleClass()
                    .remove(
                            "central-nav-active"
                    );

            if (
                    !botao.getStyleClass()
                            .contains("central-nav")
            ) {

                botao.getStyleClass()
                        .add("central-nav");
            }
        }

        selecionado.getStyleClass()
                .remove("central-nav");

        selecionado.getStyleClass()
                .add("central-nav-active");
    }

    // ============================================================
    // STATUS
    // ============================================================

    private Label criarStatusLabel() {

        Label status =
                new Label(
                        "INTRANET  /  ADMINISTRAÇÃO\n\nCentral de Avisos · 1.0"
                );

        status.getStyleClass()
                .add("connection");

        return status;
    }

    // ============================================================
    // PREPARAR TELA
    // ============================================================

    private void prepararTela(
            String titulo
    ) {

        if (monitorView != null) monitorView.pausarAtualizacaoAutomatica();
        content.getChildren()
                .clear();

        String secao = titulo.equals(\u0022Configura\u00e7\u00f5es\u0022)
                || titulo.equals(\u0022Acessando Agora\u0022)
                ? \u0022Administra\u00e7\u00e3o\u0022 : \u0022Comunica\u00e7\u00e3o\u0022;
        HBox breadcrumb = new HBox(7,
                new Label(\u0022Intranet\u0022),
                new Label(\u0022/\u0022),
                new Label(secao),
                new Label(\u0022/\u0022),
                new Label(titulo));
        breadcrumb.setAlignment(Pos.CENTER_LEFT);
        breadcrumb.getStyleClass().add(\u0022breadcrumb\u0022);

        Label cabecalho =
                new Label(titulo);

        cabecalho.getStyleClass()
                .add("central-page-title");

        Label descricao =
                new Label(
                        descricaoDaPagina(titulo)
                );

        descricao.setWrapText(true);

        descricao.getStyleClass()
                .add("page-description");

        VBox tituloPagina =
                new VBox(
                        3,
                        breadcrumb,
                        cabecalho,
                        descricao
                );

        tituloPagina.getStyleClass()
                .add("page-heading");

        content.getChildren()
                .add(
                        tituloPagina
                );
    }

    // ============================================================
    // DESCRIÇÃO DAS PÁGINAS
    // ============================================================

    private String descricaoDaPagina(
            String titulo
    ) {

        return switch (titulo) {

            case "Novo Aviso" ->
                    "Crie uma comunicação clara e escolha como ela será exibida na Intranet.";

            case "Popups" ->
                    "Acompanhe, pesquise e gerencie os avisos publicados.";

            case "Histórico de Avisos" ->
                    "Consulte o registro de comunicações enviadas pela Central.";

            case "Acessando Agora" ->
                    "Veja em tempo real a movimentação de visitantes na Intranet.";

            case "Mensagem do Dia" ->
                    "Defina a mensagem de destaque exibida aos colaboradores.";

            case "Configurações" ->
                    "Configure a conexão segura com o servidor da Intranet.";

            default ->
                    "Gerencie as comunicações da sua equipe em um só lugar.";
        };
    }

    // ============================================================
    // NOVO AVISO
    // ============================================================

    private void novoAviso() {

        prepararTela(
                "Novo Aviso"
        );

        if (formView == null) formView = new AvisoFormView(client, avisoService);
        AvisoFormView avisoFormView = formView;

        content.getChildren()
                .add(
                        avisoFormView.getView()
                );
    }

    // ============================================================
    // POPUPS
    // ============================================================

    private void popups() {

        prepararTela(
                "Popups"
        );

        PopupView popupView =
                new PopupView(
                        client
                );

        content.getChildren()
                .add(
                        popupView.getRoot()
                );
    }

    // ============================================================
    // HISTÓRICO
    // ============================================================

    private void historico() {

        prepararTela(
                "Histórico de Avisos"
        );

        HistoricoView historicoView =
                new HistoricoView(
                        avisoService
                );

        content.getChildren()
                .add(
                        historicoView.getRoot()
                );
    }

    // ============================================================
    // MONITORAMENTO
    // ============================================================

    private void monitorAcessos() {

        content.getChildren()
                .clear();

        if (monitorView == null) {
            monitorView =
                    new MonitorAcessosView(
                            client
                    );
        } else {
            monitorView.retomarAtualizacaoAutomatica();
        }

        content.getChildren()
                .add(
                        monitorView.getRoot()
                );
    }

    // ============================================================
    // MENSAGEM DO DIA
    // ============================================================

    private void abrirMensagemDoDia(
            Stage stage
    ) {

        prepararTela(
                "Mensagem do Dia"
        );

        Node view =
                MensagemDoDiaView.criar(
                        stage
                );

        content.getChildren()
                .add(
                        view
                );
    }

    // ============================================================
    // CONFIGURAÇÕES
    // ============================================================

    private void configuracoes() {

        prepararTela(
                "Configurações"
        );

        /*
         * IMPORTANTE:
         *
         * ConfiguracoesView possui o construtor:
         *
         * public ConfiguracoesView(
         *     IntranetAvisosClient client
         * )
         *
         * Portanto o client compartilhado
         * precisa ser enviado aqui.
         */
        ConfiguracoesView configuracoesView =
                new ConfiguracoesView(
                        client
                );

        content.getChildren()
                .add(
                        configuracoesView.getRoot()
                );
    }
}
