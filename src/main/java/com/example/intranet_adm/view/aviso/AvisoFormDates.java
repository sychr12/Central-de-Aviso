package com.example.intranet_adm.view.aviso;

import javafx.scene.control.DatePicker;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class AvisoFormDates {

    private final DatePicker dataPublicacao;
    private final TextField horaPublicacao;

    private final DatePicker dataExpiracao;
    private final TextField horaExpiracao;
    private final CheckBox publicarImediatamente;
    private final CheckBox popupTemporario;

    public AvisoFormDates() {
        dataPublicacao = new DatePicker();
        horaPublicacao = new TextField();

        dataExpiracao = new DatePicker();
        horaExpiracao = new TextField();
        publicarImediatamente = new CheckBox("Publicar imediatamente");
        popupTemporario = new CheckBox("Popup temporário");

        horaPublicacao.setPromptText("HH:mm");
        horaExpiracao.setPromptText("HH:mm");

        horaPublicacao.setText("00:00");
        horaExpiracao.setText("23:59");
        horaPublicacao.setPrefWidth(78); horaPublicacao.setMaxWidth(90);
        horaExpiracao.setPrefWidth(78); horaExpiracao.setMaxWidth(90);

        publicarImediatamente.setSelected(true);
        publicarImediatamente.selectedProperty().addListener(
                (observavel, anterior, selecionado) -> atualizarDisponibilidade());
        popupTemporario.selectedProperty().addListener(
                (observavel, anterior, selecionado) -> atualizarDisponibilidade());

        configurarEstilos();
        atualizarDisponibilidade();
    }

    private void configurarEstilos() {
        dataPublicacao.getStyleClass().add("form-field");
        horaPublicacao.getStyleClass().add("form-field");

        dataExpiracao.getStyleClass().add("form-field");
        horaExpiracao.getStyleClass().add("form-field");
        publicarImediatamente.getStyleClass().add("publication-option");
        popupTemporario.getStyleClass().add("publication-option");
    }

    public VBox criarLayout() {
        VBox container = new VBox(12);
        container.getStyleClass().add("notice-dates-layout");

        Label publicacaoLabel = new Label("Publicação");
        Label expiracaoLabel = new Label("Expiração");
        publicacaoLabel.getStyleClass().add("notice-field-label");
        expiracaoLabel.getStyleClass().add("notice-field-label");

        Label ajuda = new Label("Escolha quando o aviso será exibido e se deve expirar automaticamente.");
        ajuda.setWrapText(true);
        ajuda.getStyleClass().add("card-description");
        HBox opcoes = new HBox(18, publicarImediatamente, popupTemporario);
        opcoes.getStyleClass().add("notice-toggle-row");

        HBox publicacaoCampos = new HBox(8, dataPublicacao, horaPublicacao);
        HBox expiracaoCampos = new HBox(8, dataExpiracao, horaExpiracao);
        dataPublicacao.setMaxWidth(Double.MAX_VALUE);
        dataExpiracao.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(dataPublicacao, Priority.ALWAYS);
        HBox.setHgrow(dataExpiracao, Priority.ALWAYS);
        VBox publicacao = new VBox(6, publicacaoLabel, publicacaoCampos);
        VBox expiracao = new VBox(6, expiracaoLabel, expiracaoCampos);
        publicacao.getStyleClass().add("notice-date-group");
        expiracao.getStyleClass().add("notice-date-group");
        HBox linha = new HBox(12, publicacao, expiracao);
        HBox.setHgrow(publicacao, Priority.ALWAYS);
        HBox.setHgrow(expiracao, Priority.ALWAYS);
        container.getChildren().addAll(ajuda, opcoes, linha);

        return container;
    }

    public DatePicker getDataPublicacao() {
        return dataPublicacao;
    }

    public TextField getHoraPublicacao() {
        return horaPublicacao;
    }

    public DatePicker getDataExpiracao() {
        return dataExpiracao;
    }

    public TextField getHoraExpiracao() {
        return horaExpiracao;
    }

    public LocalDateTime getPublicarEm() {
        if (publicarImediatamente.isSelected()) {
            return null;
        }

        dataPublicacao.commitValue();
        if (dataPublicacao.getValue() == null) throw new IllegalArgumentException("Selecione a data de publicação.");

        LocalTime hora = parseHora(horaPublicacao.getText(), LocalTime.MIDNIGHT);

        return LocalDateTime.of(
                dataPublicacao.getValue(),
                hora
        );
    }

    public LocalDateTime getExpirarEm() {
        if (!popupTemporario.isSelected()) {
            return null;
        }

        dataExpiracao.commitValue();
        if (dataExpiracao.getValue() == null) throw new IllegalArgumentException("Selecione a data de expiração.");

        LocalTime hora = parseHora(horaExpiracao.getText(), LocalTime.of(23, 59));

        return LocalDateTime.of(
                dataExpiracao.getValue(),
                hora
        );
    }

    private LocalTime parseHora(String valor, LocalTime padrao) {
        if (valor == null || valor.isBlank()) {
            return padrao;
        }

        try {
            return LocalTime.parse(valor);
        } catch (Exception ignored) {
            throw new IllegalArgumentException("Horário inválido. Use HH:mm (por exemplo, 14:30).");
        }
    }

    public void limpar() {
        publicarImediatamente.setSelected(true);
        popupTemporario.setSelected(false);
        dataPublicacao.setValue(null);
        horaPublicacao.setText("00:00");

        dataExpiracao.setValue(null);
        horaExpiracao.setText("23:59");
    }

    private void atualizarDisponibilidade() {
        boolean agendado = !publicarImediatamente.isSelected();
        dataPublicacao.setDisable(!agendado);
        horaPublicacao.setDisable(!agendado);

        boolean temporario = popupTemporario.isSelected();
        dataExpiracao.setDisable(!temporario);
        horaExpiracao.setDisable(!temporario);
    }
}
