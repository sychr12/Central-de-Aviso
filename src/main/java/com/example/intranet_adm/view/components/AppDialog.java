package com.example.intranet_adm.view.components;

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
import javafx.stage.Window;

public final class AppDialog {

    private AppDialog() {
    }

    public static boolean confirmarExclusao(
            Window owner,
            String tituloJanela,
            String titulo,
            String descricao,
            String rotuloPrevia,
            String previa,
            String textoConfirmar) {
        return mostrarConfirmacao(
                owner, tituloJanela, titulo, descricao,
                rotuloPrevia, previa, textoConfirmar,
                AppIcon.Type.TRASH, true);
    }

    public static boolean mostrarConfirmacao(
            Window owner,
            String tituloJanela,
            String titulo,
            String descricao,
            String rotuloPrevia,
            String previa,
            String textoConfirmar,
            AppIcon.Type icone,
            boolean destrutivo) {
        Dialog<ButtonType> dialogo = criarDialogo(owner, tituloJanela);
        ButtonType confirmar = new ButtonType(
                textoConfirmar, ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType(
                "Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        DialogPane painel = dialogo.getDialogPane();
        painel.setContent(criarConteudo(
                titulo, descricao, rotuloPrevia, previa,
                icone, destrutivo ? "danger" : "primary"));
        painel.getButtonTypes().setAll(cancelar, confirmar);

        Button botaoConfirmar = (Button) painel.lookupButton(confirmar);
        Button botaoCancelar = (Button) painel.lookupButton(cancelar);
        botaoConfirmar.getStyleClass().add(
                destrutivo ? "danger-button" : "primary-button");
        botaoConfirmar.setGraphic(AppIcon.create(icone, 16));
        botaoCancelar.getStyleClass().add("secondary-button");
        botaoCancelar.setGraphic(AppIcon.create(AppIcon.Type.CLOSE, 16));
        botaoConfirmar.setDefaultButton(false);
        botaoCancelar.setDefaultButton(true);
        Platform.runLater(botaoCancelar::requestFocus);

        return dialogo.showAndWait().orElse(cancelar) == confirmar;
    }

    public static void mostrarErro(
            Window owner,
            String tituloJanela,
            String titulo,
            String mensagem) {
        Dialog<ButtonType> dialogo = criarDialogo(owner, tituloJanela);
        ButtonType fechar = new ButtonType(
                "Entendi", ButtonBar.ButtonData.OK_DONE);
        DialogPane painel = dialogo.getDialogPane();
        painel.setContent(criarConteudo(
                titulo, mensagem, null, null,
                AppIcon.Type.WARNING, "danger"));
        painel.getButtonTypes().setAll(fechar);

        Button botaoFechar = (Button) painel.lookupButton(fechar);
        botaoFechar.getStyleClass().add("primary-button");
        botaoFechar.setGraphic(AppIcon.create(AppIcon.Type.CHECK, 16));
        botaoFechar.setDefaultButton(true);
        Platform.runLater(botaoFechar::requestFocus);
        dialogo.showAndWait();
    }

    private static Dialog<ButtonType> criarDialogo(
            Window owner, String tituloJanela) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(tituloJanela);
        dialogo.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) dialogo.initOwner(owner);

        DialogPane painel = dialogo.getDialogPane();
        painel.getStyleClass().add("app-dialog");
        painel.setMinWidth(500);
        adicionarEstilos(painel);
        return dialogo;
    }

    private static VBox criarConteudo(
            String titulo,
            String descricao,
            String rotuloPrevia,
            String previa,
            AppIcon.Type tipoIcone,
            String tom) {
        StackPane icone = new StackPane(AppIcon.create(tipoIcone, 22));
        icone.getStyleClass().addAll("app-dialog-icon", "app-dialog-icon-" + tom);

        Label tituloLabel = new Label(titulo);
        tituloLabel.setWrapText(true);
        tituloLabel.getStyleClass().add("app-dialog-title");
        Label descricaoLabel = new Label(descricao);
        descricaoLabel.setWrapText(true);
        descricaoLabel.getStyleClass().add("app-dialog-description");
        VBox textos = new VBox(3, tituloLabel, descricaoLabel);
        HBox cabecalho = new HBox(12, icone, textos);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        VBox conteudo = new VBox(16, cabecalho);
        conteudo.getStyleClass().add("app-dialog-content");

        if (previa != null && !previa.isBlank()) {
            Label rotulo = new Label(
                    rotuloPrevia == null ? "ITEM SELECIONADO" : rotuloPrevia);
            rotulo.getStyleClass().add("app-dialog-preview-label");
            Label valor = new Label(previa);
            valor.setWrapText(true);
            valor.setMaxWidth(Double.MAX_VALUE);
            valor.getStyleClass().add("app-dialog-preview-value");
            VBox cartao = new VBox(6, rotulo, valor);
            cartao.getStyleClass().add("app-dialog-preview");
            conteudo.getChildren().add(cartao);
        }
        return conteudo;
    }

    private static void adicionarEstilos(DialogPane painel) {
        var base = AppDialog.class.getResource(
                "/com/example/intranet_adm/style.css");
        var redesign = AppDialog.class.getResource(
                "/com/example/intranet_adm/redesign.css");
        if (base != null) painel.getStylesheets().add(base.toExternalForm());
        if (redesign != null) painel.getStylesheets().add(redesign.toExternalForm());
    }
}
