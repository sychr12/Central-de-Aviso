package com.example.intranet_adm.view.aviso;

import com.example.intranet_adm.service.IntranetAvisosClient;
import javafx.scene.control.Alert;

public class AvisoFormActions {

    private final AvisoFormFields fields;
    private final AvisoFormDates dates;
    private final AvisoFormImage image;
    private final AvisoFormValidation validation;

    private final IntranetAvisosClient client;

    public AvisoFormActions(
            AvisoFormFields fields,
            AvisoFormDates dates,
            AvisoFormImage image,
            AvisoFormValidation validation,
            IntranetAvisosClient client
    ) {
        this.fields = fields;
        this.dates = dates;
        this.image = image;
        this.validation = validation;
        this.client = client;
    }

    public void enviar() {

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

            client.enviar(config);

            mostrarSucesso(
                    "Aviso enviado",
                    "O aviso foi enviado com sucesso."
            );

            limpar();

        } catch (Exception error) {

            mostrarErro(
                    "Erro ao enviar aviso",
                    error.getMessage()
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
