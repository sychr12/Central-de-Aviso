package com.example.intranet_adm.view.aviso;

import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.service.AvisoService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

        VBox container = new VBox(18);
        container.setMaxWidth(1160);

        HBox heading = new HBox(12);
        heading.setAlignment(Pos.CENTER_LEFT);
        Label headingIcon = new Label("✦");
        headingIcon.getStyleClass().add("form-heading-icon");
        VBox headingText = new VBox(3,
                criarLabel("Sua próxima comunicação", "form-heading"),
                criarLabel("Preencha o conteúdo, programe a publicação e revise antes de enviar.", "page-description"));
        Region headingSpacer = new Region();
        HBox.setHgrow(headingSpacer, Priority.ALWAYS);
        Button previewButton = new Button("◉  Visualizar Popup");
        previewButton.getStyleClass().add("secondary-button");
        heading.getChildren().addAll(headingText, headingSpacer, previewButton);
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
        fields.getCriticidadeComboBox().valueProperty().addListener((o,a,b) -> atualizarBanner());
        fields.getPrioridadeComboBox().valueProperty().addListener((o,a,b) -> atualizarBanner());
        atualizarBanner();
        VBox editor = new VBox(18, detalhes, imagem);
        editor.setMinWidth(0);
        editor.setPrefWidth(600);
        preview.setMinWidth(250);
        preview.setPrefWidth(300);
        preview.setMaxWidth(340);
        preview.setMaxHeight(Region.USE_PREF_SIZE);
        HBox columns = new HBox(22, editor, preview);
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

        container.disableProperty().bind(actions.enviandoProperty());
        enviarButton.textProperty().bind(javafx.beans.binding.Bindings.when(actions.enviandoProperty()).then("Enviando…").otherwise("Publicar aviso"));
        HBox botoes = new HBox(10, limparButton, enviarButton);
        botoes.setAlignment(Pos.CENTER_RIGHT);

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
    }

    private Label criarLabel(String texto, String estilo) {
        Label label = new Label(texto);
        label.getStyleClass().add(estilo);
        label.setWrapText(true);
        return label;
    }

}
