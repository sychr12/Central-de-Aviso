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

        if (dates.getPublicarEm() != null
                && dates.getExpirarEm() != null) {

            LocalDateTime publicacao = dates.getPublicarEm();
            LocalDateTime expiracao = dates.getExpirarEm();

            if (!expiracao.isAfter(publicacao)) {
                mostrarErro(
                        "Datas inválidas",
                        "A data de expiração deve ser posterior à publicação."
                );
                return false;
            }
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