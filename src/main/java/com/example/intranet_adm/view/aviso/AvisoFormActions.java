package com.example.intranet_adm.view.aviso;

import com.example.intranet_adm.service.AvisoService;
import com.example.intranet_adm.service.IntranetAvisosClient;
import javafx.scene.control.Alert;

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
        Alert alert = new Alert(
                Alert.AlertType.INFORMATION
        );

        alert.setTitle("Central de Avisos");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);

        alert.showAndWait();
    }

    private void mostrarErro(
            String titulo,
            String mensagem
    ) {
        Alert alert = new Alert(
                Alert.AlertType.ERROR
        );

        alert.setTitle("Central de Avisos");
        alert.setHeaderText(titulo);
        alert.setContentText(
                mensagem == null
                        ? "Ocorreu um erro."
                        : mensagem
        );

        alert.showAndWait();
    }
}
