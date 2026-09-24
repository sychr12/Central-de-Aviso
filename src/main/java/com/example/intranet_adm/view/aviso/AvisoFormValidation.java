package com.example.intranet_adm.view.aviso;

import com.example.intranet_adm.view.components.AppDialog;
import javafx.stage.Window;

import java.time.LocalDateTime;

public class AvisoFormValidation {

    public boolean validar(
            AvisoFormFields fields,
            AvisoFormDates dates
    ) {
        Window owner = fields.getTituloField().getScene() == null
                ? null : fields.getTituloField().getScene().getWindow();

        if (fields.getTitulo().isBlank()) {
            mostrarErro(
                    owner,
                    "Título obrigatório",
                    "Digite um título para o aviso."
            );
            return false;
        }

        if (fields.getMensagem().isBlank()) {
            mostrarErro(
                    owner,
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
            mostrarErro(owner, "Revise os campos", error.getMessage());
            return false;
        }
        return true;
    }

    private void mostrarErro(Window owner, String titulo, String mensagem) {
        AppDialog.mostrarErro(owner, "Validação", titulo, mensagem);
    }
}
