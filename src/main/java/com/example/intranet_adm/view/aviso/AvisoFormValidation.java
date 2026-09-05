package com.example.intranet_adm.view.aviso;

import javafx.scene.control.Alert;

import java.time.LocalDateTime;

public class AvisoFormValidation {

    public boolean validar(
            AvisoFormFields fields,
            AvisoFormDates dates
    ) {

        if (fields.getTitulo().isBlank()) {
            mostrarErro(
                    "Título obrigatório",
                    "Digite um título para o aviso."
            );
            return false;
        }

        if (fields.getMensagem().isBlank()) {
            mostrarErro(
                    "Mensagem obrigatória",
                    "Digite a mensagem do aviso."
            );
            return false;
        }

        try {
            LocalDateTime publicacao = dates.getPublicarEm();
            LocalDateTime expiracao = dates.getExpirarEm();
            LocalDateTime agora = LocalDateTime.now();
            if (publicacao != null && !publicacao.isAfter(agora)) throw new IllegalArgumentException("A publicação agendada deve estar no futuro.");
            if (expiracao != null && !expiracao.isAfter(publicacao == null ? agora : publicacao)) throw new IllegalArgumentException("A expiração deve ser posterior à publicação.");
            String link = fields.getLink();
            if (link != null) {
                java.net.URI uri = java.net.URI.create(link);
                if (uri.getHost() == null || !("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))) throw new IllegalArgumentException("Informe um link HTTP ou HTTPS válido.");
            }
        } catch (RuntimeException error) {
            mostrarErro("Revise os campos", error.getMessage());
            return false;
        }
        return true;
    }

    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Validação");
        alert.setHeaderText(titulo);
        alert.setContentText(mensagem);

        alert.showAndWait();
    }
}