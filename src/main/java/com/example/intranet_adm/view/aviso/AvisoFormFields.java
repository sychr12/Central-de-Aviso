package com.example.intranet_adm.view.aviso;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AvisoFormFields {

    private final TextField tituloField;
    private final ComboBox<String> criticidadeComboBox;
    private final ComboBox<String> prioridadeComboBox;
    private final TextArea mensagemArea;
    private final TextField linkField;

    public AvisoFormFields() {
        tituloField = new TextField();
        tituloField.setPromptText("Digite o título do aviso");

        criticidadeComboBox = new ComboBox<>();
        criticidadeComboBox.getItems().addAll(
                "Informativa", "Baixa", "Moderada", "Alta", "Crítica"
        );
        criticidadeComboBox.setValue("Informativa");

        prioridadeComboBox = new ComboBox<>();
        prioridadeComboBox.getItems().addAll(
                "Baixa", "Normal", "Alta", "Urgente", "Imediata"
        );
        prioridadeComboBox.setValue("Normal");

        mensagemArea = new TextArea();
        mensagemArea.setPromptText("Digite a mensagem do aviso");
        mensagemArea.setWrapText(true);
        mensagemArea.setPrefRowCount(6);

        linkField = new TextField();
        linkField.setPromptText("https://exemplo.com");

        configurarEstilos();
        configurarCores(criticidadeComboBox, true);
        configurarCores(prioridadeComboBox, false);
    }

    private void configurarEstilos() {
        tituloField.getStyleClass().add("form-field");
        criticidadeComboBox.getStyleClass().add("form-field");
        prioridadeComboBox.getStyleClass().add("form-field");
        mensagemArea.getStyleClass().add("form-field");
        linkField.getStyleClass().add("form-field");
    }

    public VBox criarLayout() {
        VBox container = new VBox(12);
        container.getStyleClass().add("notice-fields-layout");

        Label tituloLabel = new Label("Título");
        Label criticidadeLabel = new Label("Criticidade");
        Label prioridadeLabel = new Label("Prioridade");
        Label mensagemLabel = new Label("Mensagem");
        Label linkLabel = new Label("Link");
        tituloLabel.getStyleClass().add("notice-field-label");
        criticidadeLabel.getStyleClass().add("notice-field-label");
        prioridadeLabel.getStyleClass().add("notice-field-label");
        mensagemLabel.getStyleClass().add("notice-field-label");
        linkLabel.getStyleClass().add("notice-field-label");

        tituloField.setMaxWidth(Double.MAX_VALUE);
        mensagemArea.setMaxWidth(Double.MAX_VALUE);
        linkField.setMaxWidth(Double.MAX_VALUE);
        prioridadeComboBox.setMaxWidth(Double.MAX_VALUE);
        criticidadeComboBox.setMaxWidth(Double.MAX_VALUE);

        VBox titulo = new VBox(6, tituloLabel, tituloField);
        VBox criticidade = new VBox(6, criticidadeLabel, criticidadeComboBox);
        VBox prioridade = new VBox(6, prioridadeLabel, prioridadeComboBox);
        HBox niveis = new HBox(12, criticidade, prioridade);
        titulo.getStyleClass().add("notice-field-group");
        criticidade.getStyleClass().add("notice-field-group");
        prioridade.getStyleClass().add("notice-field-group");
        niveis.getStyleClass().add("notice-level-row");
        HBox.setHgrow(titulo, Priority.ALWAYS);
        HBox.setHgrow(criticidade, Priority.ALWAYS);
        HBox.setHgrow(prioridade, Priority.ALWAYS);

        VBox mensagem = new VBox(6, mensagemLabel, mensagemArea);
        VBox link = new VBox(6, linkLabel, linkField);
        mensagem.getStyleClass().add("notice-field-group");
        link.getStyleClass().add("notice-field-group");
        container.getChildren().addAll(titulo, niveis, mensagem, link);

        return container;
    }

    public TextField getTituloField() {
        return tituloField;
    }

    public ComboBox<String> getPrioridadeComboBox() {
        return prioridadeComboBox;
    }

    private void configurarCores(
            ComboBox<String> combo,
            boolean criticidade
    ) {
        combo.setCellFactory(lista -> criarCelulaColorida(criticidade));
        combo.setButtonCell(criarCelulaColorida(criticidade));
    }

    private ListCell<String> criarCelulaColorida(boolean criticidade) {
        return new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setStyle(empty ? "" : "-fx-text-fill: " + corDoNivel(item, criticidade) + "; -fx-font-weight: bold;");
            }
        };
    }

    private String corDoNivel(String nivel, boolean criticidade) {
        if (nivel == null) return "#334155";
        return switch (nivel) {
            case "Informativa" -> "#2454C6";
            case "Baixa" -> criticidade ? "#167047" : "#64748B";
            case "Moderada" -> "#946200";
            case "Alta" -> "#B45309";
            case "Crítica", "Urgente", "Imediata" -> "#BE123C";
            default -> "#2454C6";
        };
    }

    public ComboBox<String> getCriticidadeComboBox() {
        return criticidadeComboBox;
    }

    public TextArea getMensagemArea() {
        return mensagemArea;
    }

    public TextField getLinkField() {
        return linkField;
    }

    public String getTitulo() {
        return tituloField.getText().trim();
    }

    public String getMensagem() {
        return mensagemArea.getText().trim();
    }

    public String getPrioridade() {
        return prioridadeComboBox.getValue();
    }

    public String getCriticidade() {
        return criticidadeComboBox.getValue();
    }

    public String getLink() {
        String link = linkField.getText();

        if (link == null) {
            return null;
        }

        link = link.trim();

        return link.isEmpty() ? null : link;
    }

    public void limpar() {
        tituloField.clear();
        mensagemArea.clear();
        linkField.clear();
        criticidadeComboBox.setValue("Informativa");
        prioridadeComboBox.setValue("Normal");
    }
}
