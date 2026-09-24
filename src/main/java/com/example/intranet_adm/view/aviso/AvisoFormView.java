package com.example.intranet_adm.view.aviso;

import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.service.AvisoService;
import com.example.intranet_adm.view.components.AppIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Effect;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

public class AvisoFormView {

    private final AvisoFormFields fields;
    private final AvisoFormDates dates;
    private final AvisoFormImage image;
    private final AvisoFormValidation validation;
    private final AvisoFormActions actions;

    private final VBox root;
    private Label previewTitle;
    private Label previewCopy;
    private Label previewBanner;

    public AvisoFormView() {

        this(new IntranetAvisosClient(), new AvisoService());
    }

    public AvisoFormView(IntranetAvisosClient client, AvisoService avisoService) {

        fields = new AvisoFormFields();
        dates = new AvisoFormDates();
        image = new AvisoFormImage();
        validation = new AvisoFormValidation();

        actions = new AvisoFormActions(
                fields,
                dates,
                image,
                validation,
                client,
                avisoService
        );

        root = criarView();
    }

    private VBox criarView() {

        VBox container = new VBox(22);
        container.setMaxWidth(1240);
        container.getStyleClass().add("new-notice-layout");

        StackPane headingIcon = new StackPane(
                AppIcon.create(AppIcon.Type.MESSAGE, 27));
        headingIcon.getStyleClass().add("composer-hero-icon");

        Label eyebrow = criarLabel(
                "COMPOSITOR DE COMUNICAÇÕES", "composer-eyebrow");
        Label heroTitle = criarLabel(
                "Transforme informação em uma mensagem que chama atenção.",
                "composer-title");
        Label heroDescription = criarLabel(
                "Construa o aviso, defina o momento certo e revise tudo antes de publicar.",
                "composer-description");
        VBox headingText = new VBox(5, eyebrow, heroTitle, heroDescription);
        headingText.setMinWidth(0);
        HBox.setHgrow(headingText, Priority.ALWAYS);

        Region headingSpacer = new Region();
        HBox.setHgrow(headingSpacer, Priority.ALWAYS);

        Button previewButton = new Button("Abrir prévia");
        previewButton.getStyleClass().add("composer-preview-button");
        previewButton.setGraphic(AppIcon.create(AppIcon.Type.EYE, 17));
        VBox heroAction = new VBox(8,
                criarLabel("RASCUNHO LOCAL", "composer-draft-badge"),
                previewButton);
        heroAction.setAlignment(Pos.CENTER_RIGHT);

        HBox heroTop = new HBox(
                16, headingIcon, headingText, headingSpacer, heroAction);
        heroTop.setAlignment(Pos.CENTER_LEFT);

        HBox journey = new HBox(9,
                criarEtapa("01", "Conteúdo", "Mensagem e tom"),
                criarEtapa("02", "Programação", "Data e duração"),
                criarEtapa("03", "Revisão", "Prévia e envio"));
        journey.getStyleClass().add("composer-journey");
        for (Node etapa : journey.getChildren()) {
            HBox.setHgrow(etapa, Priority.ALWAYS);
        }

        VBox hero = new VBox(18, heroTop, journey);
        hero.getStyleClass().add("composer-hero");
        previewButton.setOnAction(event -> abrirPreviaDoPopup());

        VBox detalhes = criarSecao(
                "01",
                AppIcon.Type.EDIT,
                "Conteúdo e classificação",
                "Escreva a mensagem e escolha a intensidade com que ela deve aparecer.",
                fields.criarLayout());

        VBox programacao = criarSecao(
                "02",
                AppIcon.Type.HISTORY,
                "Programação inteligente",
                "Publique agora ou defina uma janela exata de exibição.",
                dates.criarLayout());

        VBox imagem = criarSecao(
                "03",
                AppIcon.Type.IMAGE,
                "Mídia e anexo",
                "Reforce a comunicação com uma imagem ou disponibilize um documento em PDF.",
                image.criarLayout());

        previewBanner = criarLabel("INFORMATIVA · NORMAL", "preview-banner");
        previewTitle = criarLabel("Título do aviso", "preview-title");
        previewCopy = criarLabel(
                "A mensagem aparecerá aqui conforme você preencher o formulário.",
                "preview-copy");
        fields.getTituloField().textProperty().addListener((o, a, b) ->
                previewTitle.setText(b == null || b.isBlank()
                        ? "Título do aviso" : b));
        fields.getMensagemArea().textProperty().addListener((o, a, b) ->
                previewCopy.setText(b == null || b.isBlank()
                        ? "A mensagem aparecerá aqui conforme você preencher o formulário."
                        : b));
        fields.getCriticidadeComboBox().valueProperty().addListener((o,a,b) -> atualizarBanner());
        fields.getPrioridadeComboBox().valueProperty().addListener((o,a,b) -> atualizarBanner());
        atualizarBanner();

        VBox editor = new VBox(16, detalhes, programacao, imagem);
        detalhes.getStyleClass().add("notice-editor-card");
        programacao.getStyleClass().add("notice-editor-card");
        imagem.getStyleClass().add("notice-editor-card");
        editor.setMinWidth(0);
        editor.setMaxWidth(Double.MAX_VALUE);

        Button limparButton = new Button("Limpar");
        limparButton.getStyleClass().add("secondary-button");
        limparButton.setOnAction(event -> {
            fields.limpar();
            dates.limpar();
            image.limpar();
        });

        Button enviarButton = new Button("Enviar aviso");

        enviarButton.getStyleClass()
                .add("primary-button");

        enviarButton.setOnAction(
                event -> actions.enviar()
        );

        limparButton.setGraphic(AppIcon.create(AppIcon.Type.CLOSE, 16));
        enviarButton.setGraphic(AppIcon.create(AppIcon.Type.SEND, 16));
        container.disableProperty().bind(actions.enviandoProperty());
        enviarButton.textProperty().bind(javafx.beans.binding.Bindings.when(actions.enviandoProperty()).then("Enviando…").otherwise("Publicar aviso"));
        HBox botoes = new HBox(10, limparButton, enviarButton);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.getStyleClass().add("notice-actions");

        StackPane actionIcon = new StackPane(
                AppIcon.create(AppIcon.Type.SEND, 22));
        actionIcon.getStyleClass().add("notice-action-icon");
        VBox actionText = new VBox(3,
                 criarLabel("Tudo pronto para comunicar?", "notice-action-title"),
                 criarLabel(
                         "Use o botão Abrir prévia acima e publique quando estiver seguro.",
                         "notice-action-description"));
        Region actionSpacer = new Region();
        HBox.setHgrow(actionSpacer, Priority.ALWAYS);
        HBox actionBar = new HBox(
                13, actionIcon, actionText, actionSpacer, botoes);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.getStyleClass().add("notice-action-bar");

        container.getChildren().addAll(hero, editor, actionBar);

        return container;
    }

    public Node getView() {
        return root;
    }

    public VBox getRoot() {
        return root;
    }

    public AvisoFormFields getFields() {
        return fields;
    }

    public AvisoFormDates getDates() {
        return dates;
    }

    public AvisoFormImage getImage() {
        return image;
    }

    public AvisoFormValidation getValidation() {
        return validation;
    }

    public AvisoFormActions getActions() {
        return actions;
    }

    public static Node criar() {
        return new AvisoFormView().getView();
    }

    private HBox criarEtapa(
            String numero,
            String titulo,
            String descricao) {

        Label numeroLabel = criarLabel(numero, "composer-step-number");
        VBox texto = new VBox(1,
                criarLabel(titulo, "composer-step-title"),
                criarLabel(descricao, "composer-step-description"));
        HBox etapa = new HBox(10, numeroLabel, texto);
        etapa.setAlignment(Pos.CENTER_LEFT);
        etapa.setMaxWidth(Double.MAX_VALUE);
        etapa.getStyleClass().add("composer-step");
        return etapa;
    }

    private VBox criarSecao(
            String numero,
            AppIcon.Type icone,
            String titulo,
            String descricao,
            Node... conteudo) {

        StackPane iconeContainer = new StackPane(AppIcon.create(icone, 20));
        iconeContainer.getStyleClass().add("notice-section-icon");

        VBox titulos = new VBox(3,
                criarLabel(titulo, "notice-section-title"),
                criarLabel(descricao, "notice-section-description"));
        titulos.setMinWidth(0);
        HBox.setHgrow(titulos, Priority.ALWAYS);

        Label numeroLabel = criarLabel(numero, "notice-section-number");
        HBox cabecalho = new HBox(
                12, iconeContainer, titulos, numeroLabel);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.getStyleClass().add("notice-section-header");

        VBox secao = new VBox(17, cabecalho);
        secao.setPadding(new Insets(20));
        secao.setMaxWidth(Double.MAX_VALUE);
        secao.getStyleClass().addAll("app-card", "notice-section");
        secao.getChildren().addAll(conteudo);
        return secao;
    }

    private void abrirPreviaDoPopup() {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Prévia do aviso na Intranet");
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.initStyle(StageStyle.TRANSPARENT);
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            dialogo.initOwner(root.getScene().getWindow());
        }

        ButtonType fecharTipo = new ButtonType(
                "Fechar", ButtonBar.ButtonData.CANCEL_CLOSE);
        DialogPane painel = dialogo.getDialogPane();
        painel.getButtonTypes().add(fecharTipo);
        painel.getStyleClass().add("intranet-preview-dialog");

        VBox popup = criarPopupDaIntranet(dialogo);
        ScrollPane rolagem = new ScrollPane(popup);
        rolagem.setFitToWidth(true);
        rolagem.setPannable(true);
        rolagem.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rolagem.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rolagem.setPrefViewportWidth(660);
        rolagem.setPrefViewportHeight(620);
        rolagem.getStyleClass().add("intranet-preview-scroll");
        painel.setContent(rolagem);
        painel.setPrefWidth(690);

        adicionarEstilosAoDialogo(painel);
        dialogo.setOnShown(event -> {
            Node barra = painel.lookup(".button-bar");
            if (barra != null) {
                barra.setVisible(false);
                barra.setManaged(false);
            }
            if (painel.getScene() != null) {
                painel.getScene().setFill(Color.TRANSPARENT);
            }
            ajustarRolagemDaPrevia(dialogo, painel, popup, rolagem);
        });

        Node raizDaJanela = root.getScene() == null
                ? null : root.getScene().getRoot();
        Effect efeitoAnterior = raizDaJanela == null
                ? null : raizDaJanela.getEffect();
        if (raizDaJanela != null) {
            ColorAdjust escurecer = new ColorAdjust();
            escurecer.setBrightness(-0.32);
            raizDaJanela.setEffect(escurecer);
        }

        try {
            dialogo.showAndWait();
        } finally {
            if (raizDaJanela != null) {
                raizDaJanela.setEffect(efeitoAnterior);
            }
        }
    }

    private void ajustarRolagemDaPrevia(
            Dialog<ButtonType> dialogo,
            DialogPane painel,
            VBox popup,
            ScrollPane rolagem) {
        if (painel.getScene() == null
                || !(painel.getScene().getWindow() instanceof Stage janela)) {
            return;
        }

        painel.applyCss();
        painel.layout();
        popup.applyCss();
        popup.autosize();

        Screen tela = Screen.getScreensForRectangle(
                        janela.getX(), janela.getY(),
                        Math.max(1, janela.getWidth()),
                        Math.max(1, janela.getHeight()))
                .stream()
                .findFirst()
                .orElse(Screen.getPrimary());

        double alturaConteudo = Math.max(
                popup.prefHeight(624), popup.getLayoutBounds().getHeight());
        double alturaDisponivel = Math.max(
                430, tela.getVisualBounds().getHeight() - 110);
        boolean precisaRolar = alturaConteudo > alturaDisponivel;

        rolagem.setVbarPolicy(precisaRolar
                ? ScrollPane.ScrollBarPolicy.AS_NEEDED
                : ScrollPane.ScrollBarPolicy.NEVER);
        rolagem.setPrefViewportHeight(
                Math.min(alturaConteudo + 2, alturaDisponivel));
        rolagem.setMaxHeight(alturaDisponivel);
        janela.sizeToScene();

        if (dialogo.getOwner() != null) {
            double centroX = dialogo.getOwner().getX()
                    + (dialogo.getOwner().getWidth() - janela.getWidth()) / 2;
            double centroY = dialogo.getOwner().getY()
                    + (dialogo.getOwner().getHeight() - janela.getHeight()) / 2;
            janela.setX(Math.max(tela.getVisualBounds().getMinX(), centroX));
            janela.setY(Math.max(tela.getVisualBounds().getMinY(), centroY));
        }
    }

    private VBox criarPopupDaIntranet(Dialog<ButtonType> dialogo) {
        String tema = temaDaPrevia();
        VBox popup = new VBox();
        popup.getStyleClass().addAll(
                "intranet-popup", "intranet-popup-theme-" + tema);

        Region barraSuperior = new Region();
        barraSuperior.getStyleClass().add("intranet-popup-topbar");

        StackPane instituicaoIcone = new StackPane(
                AppIcon.create(AppIcon.Type.BUILDING, 18));
        instituicaoIcone.getStyleClass().add("intranet-popup-brand-icon");

        String titulo = fields.getTitulo();
        Label tituloLabel = criarLabel(
                titulo.isBlank() ? "Título do aviso" : titulo,
                "intranet-popup-title");
        tituloLabel.setMaxWidth(Double.MAX_VALUE);

        Label classificacao = criarLabel(
                textoOuPadrao(fields.getCriticidade(), "Informativa")
                        + " - "
                        + textoOuPadrao(fields.getPrioridade(), "Normal"),
                "intranet-popup-badge");
        Label ativo = criarLabel("Ativo", "intranet-popup-active-text");
        ativo.setGraphic(AppIcon.create(AppIcon.Type.CHECK, 12));
        ativo.setGraphicTextGap(5);
        HBox metadados = new HBox(7, classificacao, ativo);
        metadados.setAlignment(Pos.CENTER_LEFT);

        VBox tituloBloco = new VBox(5, tituloLabel, metadados);
        tituloBloco.setMinWidth(0);
        HBox.setHgrow(tituloBloco, Priority.ALWAYS);

        Button fecharTopo = new Button();
        fecharTopo.setGraphic(AppIcon.create(AppIcon.Type.CLOSE, 15));
        fecharTopo.setTooltip(new Tooltip("Fechar prévia"));
        fecharTopo.getStyleClass().add("intranet-popup-close");
        fecharTopo.setOnAction(event -> dialogo.setResult(ButtonType.CLOSE));

        HBox cabecalhoLinha = new HBox(
                13, instituicaoIcone, tituloBloco, fecharTopo);
        cabecalhoLinha.setAlignment(Pos.TOP_LEFT);
        HBox cabecalho = new HBox(cabecalhoLinha);
        cabecalho.getStyleClass().add("intranet-popup-header");
        HBox.setHgrow(cabecalhoLinha, Priority.ALWAYS);

        VBox conteudoMensagem = criarConteudoDaMensagem();
        StackPane alertaIcone = new StackPane(AppIcon.create(
                "normal".equals(tema)
                        ? AppIcon.Type.INFO : AppIcon.Type.WARNING,
                20));
        alertaIcone.getStyleClass().add("intranet-popup-alert-icon");
        HBox blocoMensagem = new HBox(12, alertaIcone, conteudoMensagem);
        blocoMensagem.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(conteudoMensagem, Priority.ALWAYS);
        blocoMensagem.getStyleClass().add("intranet-popup-message-box");

        VBox corpo = new VBox(13, blocoMensagem);
        corpo.getStyleClass().add("intranet-popup-body");

        HBox datas = criarDatasDaPrevia();
        if (!datas.getChildren().isEmpty()) {
            corpo.getChildren().add(datas);
        }

        Region separador = new Region();
        separador.getStyleClass().add("intranet-popup-footer-separator");
        Button fecharRodape = new Button("Fechar");
        fecharRodape.getStyleClass().add("intranet-popup-footer-button");
        fecharRodape.setOnAction(event -> dialogo.setResult(ButtonType.CLOSE));
        Region espacoRodape = new Region();
        HBox.setHgrow(espacoRodape, Priority.ALWAYS);
        HBox rodape = new HBox(espacoRodape, fecharRodape);
        rodape.setAlignment(Pos.CENTER_RIGHT);
        VBox rodapeCompleto = new VBox(10, separador, rodape);
        rodapeCompleto.getStyleClass().add("intranet-popup-footer");
        corpo.getChildren().add(rodapeCompleto);

        popup.getChildren().addAll(barraSuperior, cabecalho, corpo);
        return popup;
    }

    private VBox criarConteudoDaMensagem() {
        VBox conteudo = new VBox(10);
        conteudo.setMinWidth(0);
        conteudo.getStyleClass().add("intranet-popup-message-content");

        String mensagem = fields.getMensagem();
        if (mensagem.isBlank()) {
            mensagem = "A mensagem aparecerá aqui conforme você preencher o formulário.";
        }

        Path arquivo = image.getImagemSelecionada();
        Pattern marcadorImagem = Pattern.compile(
                "\\[IMAGEM]|\\[FOTO]|\\[IMAGE]", Pattern.CASE_INSENSITIVE);
        var correspondencia = marcadorImagem.matcher(mensagem);

        if (arquivo != null && correspondencia.find()) {
            String antes = mensagem.substring(0, correspondencia.start()).trim();
            String depois = mensagem.substring(correspondencia.end()).trim();
            if (!antes.isBlank()) conteudo.getChildren().add(criarTextoMensagem(antes));
            conteudo.getChildren().add(criarMidiaDaPrevia(arquivo));
            if (!depois.isBlank()) conteudo.getChildren().add(criarTextoMensagem(depois));
        } else {
            conteudo.getChildren().add(criarTextoMensagem(mensagem));
            if (arquivo != null) {
                conteudo.getChildren().add(criarMidiaDaPrevia(arquivo));
            }
        }

        String endereco = fields.getLink();
        if (linkSeguro(endereco)) {
            Hyperlink link = new Hyperlink("Saiba mais");
            link.setGraphic(AppIcon.create(AppIcon.Type.EXTERNAL_LINK, 14));
            link.setGraphicTextGap(7);
            link.setTooltip(new Tooltip(endereco));
            link.getStyleClass().add("intranet-popup-link");
            link.setOnAction(event -> abrirLink(endereco));
            conteudo.getChildren().add(link);
        }

        return conteudo;
    }

    private Label criarTextoMensagem(String texto) {
        Label mensagem = criarLabel(texto, "intranet-popup-message");
        mensagem.setMaxWidth(Double.MAX_VALUE);
        return mensagem;
    }

    private Node criarMidiaDaPrevia(Path arquivo) {
        String nome = arquivo.getFileName() == null
                ? "Anexo selecionado" : arquivo.getFileName().toString();
        String minusculo = nome.toLowerCase(Locale.ROOT);

        if (minusculo.endsWith(".pdf")) {
            StackPane icone = new StackPane(AppIcon.create(AppIcon.Type.IMAGE, 20));
            icone.getStyleClass().add("intranet-popup-file-icon");
            VBox textos = new VBox(2,
                    criarLabel("Documento PDF", "intranet-popup-file-type"),
                    criarLabel(nome, "intranet-popup-file-name"));
            HBox arquivoCard = new HBox(11, icone, textos);
            arquivoCard.setAlignment(Pos.CENTER_LEFT);
            arquivoCard.getStyleClass().add("intranet-popup-file");
            return arquivoCard;
        }

        try {
            if (!Files.isRegularFile(arquivo)) {
                return criarLabel(
                        "Não foi possível carregar o anexo selecionado.",
                        "intranet-popup-media-error");
            }
            Image imagemPrevia = new Image(
                    arquivo.toUri().toString(), 560, 300, true, true);
            ImageView visualizacao = new ImageView(imagemPrevia);
            visualizacao.setPreserveRatio(true);
            visualizacao.setSmooth(true);
            visualizacao.setFitWidth(560);
            visualizacao.setFitHeight(300);

            StackPane imagemArea = new StackPane(visualizacao);
            imagemArea.getStyleClass().add("intranet-popup-media-image");
            Rectangle recorte = new Rectangle();
            recorte.widthProperty().bind(imagemArea.widthProperty());
            recorte.heightProperty().bind(imagemArea.heightProperty());
            recorte.setArcWidth(14);
            recorte.setArcHeight(14);
            imagemArea.setClip(recorte);

            VBox midia = new VBox(imagemArea);
            midia.getStyleClass().add("intranet-popup-media");
            return midia;
        } catch (Exception error) {
            return criarLabel(
                    "Não foi possível carregar o anexo selecionado.",
                    "intranet-popup-media-error");
        }
    }

    private HBox criarDatasDaPrevia() {
        HBox datas = new HBox(9);
        datas.getStyleClass().add("intranet-popup-dates");

        if (!dates.getDataExpiracao().isDisabled()) {
            String expiracao = formatarDataHora(
                    dates.getDataExpiracao().getValue(),
                    dates.getHoraExpiracao().getText(), "23:59");
            Node expira = criarDataCard(
                    AppIcon.Type.CLOCK, "EXPIRA", expiracao);
            datas.getChildren().add(expira);
            HBox.setHgrow(expira, Priority.ALWAYS);
        }
        return datas;
    }

    private HBox criarDataCard(
            AppIcon.Type tipo, String rotulo, String valor) {
        StackPane icone = new StackPane(AppIcon.create(tipo, 15));
        icone.getStyleClass().add("intranet-popup-date-icon");
        VBox texto = new VBox(1,
                criarLabel(rotulo, "intranet-popup-date-label"),
                criarLabel(valor, "intranet-popup-date-value"));
        HBox card = new HBox(9, icone, texto);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("intranet-popup-date-card");
        return card;
    }

    private String formatarDataHora(
            LocalDate data, String hora, String horaPadrao) {
        if (data == null) return "Data não definida";
        String valorHora = hora == null || hora.isBlank() ? horaPadrao : hora.trim();
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " " + valorHora;
    }

    private String temaDaPrevia() {
        String criticidade = textoOuPadrao(
                fields.getCriticidade(), "Informativa").toLowerCase(Locale.ROOT);
        return switch (criticidade) {
            case "crítica", "critica" -> "critical";
            case "alta" -> "high";
            case "moderada" -> "moderate";
            case "baixa" -> "low";
            default -> "normal";
        };
    }

    private String textoOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }

    private boolean linkSeguro(String endereco) {
        if (endereco == null || endereco.isBlank()) return false;
        try {
            String esquema = URI.create(endereco.trim()).getScheme();
            return "http".equalsIgnoreCase(esquema)
                    || "https".equalsIgnoreCase(esquema);
        } catch (Exception ignored) {
            return false;
        }
    }

    private void abrirLink(String endereco) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(endereco.trim()));
            }
        } catch (Exception ignored) {
            // A prévia continua funcional mesmo se o sistema não puder abrir o navegador.
        }
    }

    private void adicionarEstilosAoDialogo(DialogPane painel) {
        var estiloBase = getClass().getResource(
                "/com/example/intranet_adm/style.css");
        var estiloRedesign = getClass().getResource(
                "/com/example/intranet_adm/redesign.css");
        if (estiloBase != null) {
            painel.getStylesheets().add(estiloBase.toExternalForm());
        }
        if (estiloRedesign != null) {
            painel.getStylesheets().add(estiloRedesign.toExternalForm());
        }
    }

    private void atualizarBanner() {
        String criticidade = fields.getCriticidadeComboBox().getValue();
        String prioridade = fields.getPrioridadeComboBox().getValue();
        previewBanner.setText("⚠  " + (criticidade == null ? "Informativa" : criticidade).toUpperCase() + " · " + (prioridade == null ? "Normal" : prioridade).toUpperCase());
        previewBanner.getStyleClass().removeAll("preview-banner-info", "preview-banner-success", "preview-banner-warning", "preview-banner-danger");
        String estilo = "preview-banner-info";
        if ("Crítica".equals(criticidade) || "Imediata".equals(prioridade)) {
            estilo = "preview-banner-danger";
        } else if ("Alta".equals(criticidade) || "Urgente".equals(prioridade)) {
            estilo = "preview-banner-warning";
        } else if ("Baixa".equals(criticidade) && "Baixa".equals(prioridade)) {
            estilo = "preview-banner-success";
        }
        previewBanner.getStyleClass().add(estilo);
        String textoBanner = previewBanner.getText();
        int primeiroEspaco = textoBanner.indexOf(' ');
        if (primeiroEspaco >= 0) {
            previewBanner.setText(textoBanner.substring(primeiroEspaco + 1).trim());
        }
        previewBanner.setGraphic(AppIcon.create(AppIcon.Type.INFO, 15));
        previewBanner.setGraphicTextGap(7);
    }

    private Label criarLabel(String texto, String estilo) {
        Label label = new Label(texto);
        label.getStyleClass().add(estilo);
        label.setWrapText(true);
        return label;
    }

}
