package com.example.intranet_adm.view.aviso;

import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.service.AvisoService;
import com.example.intranet_adm.view.components.AppIcon;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Locale;

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

        VBox container = new VBox(18);
        container.setMaxWidth(1160);
        container.getStyleClass().add(\u0022new-notice-layout\u0022);

        HBox heading = new HBox(12);
        heading.setAlignment(Pos.CENTER_LEFT);
        Label headingIcon = new Label("✦");
        headingIcon.getStyleClass().add("form-heading-icon");
        headingIcon.setText(\u0022\u0022);
        headingIcon.setGraphic(AppIcon.create(AppIcon.Type.MESSAGE, 20));
        VBox headingText = new VBox(3,
                criarLabel("Sua próxima comunicação", "form-heading"),
                criarLabel("Preencha o conteúdo, programe a publicação e revise antes de enviar.", "page-description"));
        Region headingSpacer = new Region();
        HBox.setHgrow(headingSpacer, Priority.ALWAYS);
        Button previewButton = new Button("◉  Visualizar Popup");
        previewButton.getStyleClass().add("secondary-button");
        previewButton.setText(\u0022Visualizar popup\u0022);
        previewButton.setGraphic(AppIcon.create(AppIcon.Type.EYE, 17));
        heading.getChildren().addAll(headingText, headingSpacer);
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

        VBox detalhes = criarCard("Detalhes do aviso", fields.criarLayout(), dates.criarLayout());

        VBox imagem = criarCard("Imagem (opcional)", image.criarLayout());

        previewBanner = criarLabel("⚠  INFORMATIVA · NORMAL", "preview-banner");
        previewTitle = criarLabel("Título do aviso", "preview-title");
        previewCopy = criarLabel("A mensagem aparecerá aqui conforme você preencher o formulário.", "preview-copy");
        VBox preview = criarCard("PREVIEW DO POPUP",
                previewBanner,
                previewTitle,
                previewCopy,
                criarLabel("Expira conforme a data configurada", "preview-meta"));
        fields.getTituloField().textProperty().addListener((o,a,b) -> previewTitle.setText(b == null || b.isBlank() ? "Título do aviso" : b));
        fields.getMensagemArea().textProperty().addListener((o,a,b) -> previewCopy.setText(b == null || b.isBlank() ? "A mensagem aparecerá aqui conforme você preencher o formulário." : b));
        preview.getStyleClass().add(\u0022notice-preview-panel\u0022);
        preview.getChildren().clear();
        Label previewHeading = criarLabel(\u0022Pré-visualização do aviso\u0022,
                \u0022notice-preview-heading\u0022);
        Label popupBrand = criarLabel(\u0022Central de Avisos\u0022,
                \u0022notice-popup-brand\u0022);
        popupBrand.setGraphic(AppIcon.create(AppIcon.Type.BELL, 17));
        popupBrand.setGraphicTextGap(8);
        Label popupClose = new Label();
        popupClose.setGraphic(AppIcon.create(AppIcon.Type.CLOSE, 15));
        popupClose.getStyleClass().add(\u0022notice-popup-close\u0022);
        Region popupSpacer = new Region();
        HBox.setHgrow(popupSpacer, Priority.ALWAYS);
        HBox popupTopbar = new HBox(8, popupBrand, popupSpacer, popupClose);
        popupTopbar.setAlignment(Pos.CENTER_LEFT);
        popupTopbar.getStyleClass().add(\u0022notice-popup-topbar\u0022);

        StackPane popupIcon = new StackPane(AppIcon.create(AppIcon.Type.INFO, 25));
        popupIcon.getStyleClass().add(\u0022notice-popup-icon\u0022);
        previewBanner.setWrapText(false);
        previewBanner.setMinWidth(Region.USE_PREF_SIZE);
        HBox popupStatus = new HBox(10, popupIcon, previewBanner);
        popupStatus.setAlignment(Pos.CENTER_LEFT);
        ImageView popupImage = new ImageView();
        popupImage.imageProperty().bind(image.getPreview().imageProperty());
        popupImage.fitWidthProperty().bind(preview.widthProperty().subtract(72));
        popupImage.setFitHeight(155);
        popupImage.setPreserveRatio(true);
        popupImage.setSmooth(true);
        StackPane popupImageFrame = new StackPane(popupImage);
        popupImageFrame.getStyleClass().add(\u0022notice-popup-image-frame\u0022);
        popupImageFrame.visibleProperty().bind(popupImage.imageProperty().isNotNull());
        popupImageFrame.managedProperty().bind(popupImageFrame.visibleProperty());

        Label popupAttachment = criarLabel(\u0022\u0022, \u0022notice-popup-attachment\u0022);
        popupAttachment.setGraphic(AppIcon.create(AppIcon.Type.IMAGE, 16));
        popupAttachment.setWrapText(true);
        popupAttachment.setMinWidth(0);
        popupAttachment.setVisible(false);
        popupAttachment.managedProperty().bind(popupAttachment.visibleProperty());
        image.imagemSelecionadaProperty().addListener((observavel, anterior, arquivo) -> {
            boolean pdf = arquivo != null && arquivo.getFileName().toString()
                    .toLowerCase(Locale.ROOT).endsWith(\u0022.pdf\u0022);
            popupAttachment.setText(pdf
                    ? \u0022PDF anexado: \u0022 + arquivo.getFileName() : \u0022\u0022);
            popupAttachment.setVisible(pdf);
        });

        Label popupLink = criarLabel(\u0022\u0022, \u0022notice-popup-link\u0022);
        popupLink.setGraphic(AppIcon.create(AppIcon.Type.LINK, 16));
        popupLink.setWrapText(true);
        popupLink.setMinWidth(0);
        popupLink.setMaxWidth(Double.MAX_VALUE);
        popupLink.setVisible(false);
        popupLink.managedProperty().bind(popupLink.visibleProperty());
        fields.getLinkField().textProperty().addListener((observavel, anterior, valor) -> {
            String link = valor == null ? \u0022\u0022 : valor.trim();
            popupLink.setText(link.isEmpty() ? \u0022\u0022 : \u0022Saiba mais: \u0022 + link);
            popupLink.setVisible(!link.isEmpty());
        });
        Label popupMeta = criarLabel(\u0022Expira conforme a data configurada\u0022,
                \u0022preview-meta\u0022);
        Label popupAcknowledge = criarLabel(\u0022Entendi\u0022,
                \u0022notice-popup-acknowledge\u0022);
        popupAcknowledge.setMaxWidth(Double.MAX_VALUE);
        popupAcknowledge.setAlignment(Pos.CENTER);
        VBox popupBody = new VBox(12, popupStatus, previewTitle, previewCopy,
                popupImageFrame, popupAttachment, popupLink, popupMeta, popupAcknowledge);
        popupBody.getStyleClass().add(\u0022notice-popup-body\u0022);
        VBox popupWindow = new VBox(popupTopbar, popupBody);
        popupWindow.getStyleClass().add(\u0022notice-popup-window\u0022);
        preview.getChildren().addAll(previewHeading, popupWindow);
        fields.getCriticidadeComboBox().valueProperty().addListener((o,a,b) -> atualizarBanner());
        fields.getPrioridadeComboBox().valueProperty().addListener((o,a,b) -> atualizarBanner());
        atualizarBanner();
        VBox editor = new VBox(18, detalhes, imagem);
        detalhes.getStyleClass().add(\u0022notice-editor-card\u0022);
        imagem.getStyleClass().add(\u0022notice-editor-card\u0022);
        editor.setMinWidth(0);
        editor.setPrefWidth(600);
        preview.setMinWidth(250);
        preview.setPrefWidth(300);
        preview.setMaxWidth(340);
        preview.setMaxHeight(Region.USE_PREF_SIZE);
        HBox columns = new HBox(22, editor, preview);
        columns.getStyleClass().add(\u0022notice-workspace\u0022);
        HBox.setHgrow(columns.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(preview, Priority.ALWAYS);

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

        container.getChildren().addAll(heading, columns, botoes);

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
