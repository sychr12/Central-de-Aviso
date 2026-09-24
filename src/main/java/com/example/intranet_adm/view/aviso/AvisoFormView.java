package com.example.intranet_adm.view.aviso;

import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.service.AvisoService;
import com.example.intranet_adm.view.components.AppIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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
        container.getStyleClass().add(\u0022new-notice-layout\u0022);

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
        previewButton.setOnAction(event -> {
            javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
            dialog.setTitle("Prévia do aviso");
            dialog.getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
            VBox body = criarCard("PRÉVIA", criarLabel(previewBanner.getText(), "preview-banner-info"), criarLabel(previewTitle.getText(), "preview-title"), criarLabel(previewCopy.getText(), "preview-copy"));
            body.setPrefWidth(480);
            dialog.getDialogPane().setContent(body);
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/example/intranet_adm/style.css").toExternalForm());
            if (root.getScene() != null) dialog.initOwner(root.getScene().getWindow());
            dialog.showAndWait();
        });

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
        detalhes.getStyleClass().add(\u0022notice-editor-card\u0022);
        programacao.getStyleClass().add(\u0022notice-editor-card\u0022);
        imagem.getStyleClass().add(\u0022notice-editor-card\u0022);
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
        botoes.getStyleClass().add(\u0022notice-actions\u0022);

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

    private VBox criarCard(String titulo, Node... conteudo) {
        Label heading = new Label(titulo);
        heading.getStyleClass().add("card-title");
        VBox card = new VBox(14);
        card.setPadding(new Insets(18));
        card.getStyleClass().add("app-card");
        card.getChildren().add(heading);
        card.getChildren().addAll(conteudo);
        return card;
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
