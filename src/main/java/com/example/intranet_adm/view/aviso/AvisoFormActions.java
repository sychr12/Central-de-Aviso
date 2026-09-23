package com.example.intranet_adm.view.aviso;

import com.example.intranet_adm.service.AvisoService;
import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.view.components.AppIcon;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public class AvisoFormActions {

    private final AvisoFormFields fields;
    private final AvisoFormDates dates;
    private final AvisoFormImage image;
    private final AvisoFormValidation validation;

    private final IntranetAvisosClient client;
    private final AvisoService avisoService;

    public AvisoFormActions(
            AvisoFormFields fields,
            AvisoFormDates dates,
            AvisoFormImage image,
            AvisoFormValidation validation,
            IntranetAvisosClient client,
            AvisoService avisoService
    ) {
        this.fields = fields;
        this.dates = dates;
        this.image = image;
        this.validation = validation;
        this.client = client;
        this.avisoService = avisoService;
    }

    private final javafx.beans.property.BooleanProperty enviando = new javafx.beans.property.SimpleBooleanProperty(false);

    public javafx.beans.property.ReadOnlyBooleanProperty enviandoProperty() { return enviando; }

    public void enviar() {
        if (enviando.get()) return;

        if (!validation.validar(fields, dates)) {
            return;
        }

        try {

            IntranetAvisosClient.AvisoConfig config =
                    new IntranetAvisosClient.AvisoConfig(
                            fields.getTitulo(),
                            fields.getMensagem()
                    )
                            .comPrioridade(
                                    converterPrioridade(
                                            fields.getPrioridade()
                                    )
                            )
                            .comCriticidade(
                                    converterCriticidade(
                                            fields.getCriticidade()
                                    )
                            )
                            .comLink(
                                    fields.getLink()
                            )
                            .comImagem(
                                    image.getImagemSelecionada()
                            )
                            .comPublicarEm(
                                    dates.getPublicarEm()
                            )
                            .comExpirarEm(
                                    dates.getExpirarEm()
                            )
                            .comAtivo(true)
                            .comPodeFechar(true);

            enviando.set(true);
            javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
                @Override protected String call() throws Exception {
                    client.enviar(config);
                    try {
                        if (avisoService != null) avisoService.adicionar(config.getTitulo(), config.getMensagem(), "Central de Avisos");
                        return null;
                    } catch (Exception error) {
                        return "O aviso foi publicado, mas o histórico local não pôde ser salvo. Não envie novamente.";
                    }
                }
            };
            task.setOnSucceeded(event -> {
                enviando.set(false);
                limpar();
                mostrarSucesso("Aviso publicado", task.getValue() == null ? "A comunicação foi enviada com sucesso." : task.getValue());
            });
            task.setOnFailed(event -> {
                enviando.set(false);
                mostrarErro("Não foi possível confirmar o envio", task.getException().getMessage() + "\nConfira os popups antes de tentar novamente.");
            });
            Thread worker = new Thread(task, "publicar-aviso");
            worker.setDaemon(true);
            worker.start();
        } catch (Exception error) {

        mostrarErro(
                    "Erro ao enviar aviso",
                    (error.getMessage() == null ? "Falha de comunicação com o IDAM." : error.getMessage())
                            + "\nEndpoint: " + IntranetAvisosClient.endpoint()
            );
        }
    }

    private String converterPrioridade(String prioridade) {

        if (prioridade == null) {
            return "normal";
        }

        return switch (prioridade.toLowerCase()) {
            case "baixa" -> "low";
            case "alta" -> "high";
            case "urgente" -> "urgent";
            case "imediata" -> "immediate";
            default -> "normal";
        };
    }

    private String converterCriticidade(String criticidade) {
        if (criticidade == null) return "informative";
        return switch (criticidade.toLowerCase()) {
            case "baixa" -> "low";
            case "moderada" -> "moderate";
            case "alta" -> "high";
            case "crítica" -> "critical";
            default -> "informative";
        };
    }

    private void limpar() {
        fields.limpar();
        dates.limpar();
        image.limpar();
    }

    private void mostrarSucesso(
            String titulo,
            String mensagem
    ) {
        mostrarResultado(titulo, mensagem, true);
    }

    private void mostrarErro(
            String titulo,
            String mensagem
    ) {
        mostrarResultado(
                titulo,
                mensagem == null ? "Ocorreu um erro." : mensagem,
                false
        );
    }

    private void mostrarResultado(
            String titulo,
            String mensagem,
            boolean sucesso
    ) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(sucesso ? "Publicação concluída" : "Falha na publicação");
        dialogo.initModality(Modality.APPLICATION_MODAL);
        if (fields.getTituloField().getScene() != null
                && fields.getTituloField().getScene().getWindow() != null) {
            dialogo.initOwner(fields.getTituloField().getScene().getWindow());
        }

        ButtonType concluir = new ButtonType(
                "Entendi", ButtonBar.ButtonData.OK_DONE);

        AppIcon.Type tipoIcone = sucesso
                ? AppIcon.Type.SEND : AppIcon.Type.INFO;
        StackPane icone = new StackPane(AppIcon.create(tipoIcone, 24));
        icone.getStyleClass().addAll(
                "publish-result-icon",
                sucesso ? "publish-result-icon-success" : "publish-result-icon-error"
        );

        Label contexto = new Label(
                sucesso ? "PUBLICAÇÃO CONCLUÍDA" : "ATENÇÃO");
        contexto.getStyleClass().addAll(
                "publish-result-eyebrow",
                sucesso ? "publish-result-eyebrow-success" : "publish-result-eyebrow-error"
        );

        Label tituloLabel = new Label(titulo);
        tituloLabel.setWrapText(true);
        tituloLabel.getStyleClass().add("publish-result-title");

        VBox titulos = new VBox(3, contexto, tituloLabel);
        HBox cabecalho = new HBox(13, icone, titulos);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        Label descricao = new Label(mensagem);
        descricao.setWrapText(true);
        descricao.setMaxWidth(Double.MAX_VALUE);
        descricao.getStyleClass().add("publish-result-description");

        Label observacao = new Label(
                sucesso
                        ? "O aviso seguirá a programação e as regras definidas no formulário."
                        : "Revise as informações acima antes de tentar publicar novamente."
        );
        observacao.setWrapText(true);
        observacao.setMaxWidth(Double.MAX_VALUE);
        observacao.getStyleClass().addAll(
                "publish-result-note",
                sucesso ? "publish-result-note-success" : "publish-result-note-error"
        );

        VBox conteudo = new VBox(17, cabecalho, descricao, observacao);
        conteudo.getStyleClass().add("publish-result-content");

        DialogPane painel = dialogo.getDialogPane();
        painel.setContent(conteudo);
        painel.getButtonTypes().setAll(concluir);
        painel.getStyleClass().add("publish-result-dialog");
        painel.setMinWidth(500);

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

        Button botaoConcluir = (Button) painel.lookupButton(concluir);
        botaoConcluir.getStyleClass().add("primary-button");
        botaoConcluir.setGraphic(AppIcon.create(
                sucesso ? AppIcon.Type.SEND : AppIcon.Type.CLOSE, 16));
        botaoConcluir.setDefaultButton(true);
        Platform.runLater(botaoConcluir::requestFocus);

        dialogo.showAndWait();
    }
}
