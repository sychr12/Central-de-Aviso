package com.example.intranet_adm.view.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import io.github.palexdev.materialfx.controls.MFXButton;

import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;


public final class CentralMenu {

    private static final int SIDEBAR_WIDTH = 230;

    private final List<MFXButton> botoesNavegacao =
            new ArrayList<>();

    private final Stage stage;

    private final Runnable novoAvisoAction;
    private final Runnable popupsAction;
    private final Runnable historicoAction;
    private final Runnable monitorAction;
    private final Runnable mensagemDoDiaAction;
    private final Runnable configuracoesAction;

    /**
     * Cria o menu lateral.
     *
     * @param stage janela principal da aplicação
     * @param novoAvisoAction ação do botão Novo Aviso
     * @param popupsAction ação do botão Popups
     * @param historicoAction ação do botão Histórico
     * @param monitorAction ação do botão Acessando Agora
     * @param mensagemDoDiaAction ação do botão Mensagem do Dia
     * @param configuracoesAction ação do botão Configurações
     */
    public CentralMenu(
            Stage stage,
            Runnable novoAvisoAction,
            Runnable popupsAction,
            Runnable historicoAction,
            Runnable monitorAction,
            Runnable mensagemDoDiaAction,
            Runnable configuracoesAction) {

        this.stage = stage;
        this.novoAvisoAction = novoAvisoAction;
        this.popupsAction = popupsAction;
        this.historicoAction = historicoAction;
        this.monitorAction = monitorAction;
        this.mensagemDoDiaAction = mensagemDoDiaAction;
        this.configuracoesAction = configuracoesAction;
    }

    /**
     * Cria o menu lateral completo.
     *
     * @return Node contendo o menu
     */
    public Node criar() {

        VBox menu =
                new VBox(10);

        menu.setPrefWidth(
                SIDEBAR_WIDTH
        );

        menu.getStyleClass()
                .add("central-sidebar");

        menu.getChildren().addAll(

                criarLogo(),

                criarTituloMenu(),

                criarSubtituloMenu(),

                criarNavButton(
                        "⊕   Novo Aviso",
                        novoAvisoAction,
                        true
                ),

                criarNavButton(
                        "◉   Popups",
                        popupsAction,
                        false
                ),

                criarNavButton(
                        "◷   Histórico",
                        historicoAction,
                        false
                ),

                criarNavButton(
                        "👁   Acessando Agora",
                        monitorAction,
                        false
                ),

                criarNavButton(
                        "☀   Mensagem do Dia",
                        mensagemDoDiaAction,
                        false
                ),

                criarNavButton(
                        "⚙   Configurações",
                        configuracoesAction,
                        false
                )
        );

        Region spacer =
                new Region();

        VBox.setVgrow(
                spacer,
                Priority.ALWAYS
        );

        menu.getChildren()
                .add(spacer);

        menu.getChildren().addAll(

                criarNavButton(
                        "⇥   Sair",
                        stage::close,
                        false
                ),

                criarStatusLabel()
        );

        return menu;
    }

    /**
     * Cria o ícone/logo do menu.
     */
    private Label criarLogo() {

        Label logo =
                new Label("🔔");

        logo.getStyleClass()
                .add("central-logo");

        return logo;
    }

    /**
     * Cria o título principal do menu.
     */
    private Label criarTituloMenu() {

        Label titulo =
                new Label("Central de Avisos");

        titulo.getStyleClass()
                .add("central-brand");

        return titulo;
    }

    /**
     * Cria o subtítulo do menu.
     */
    private Label criarSubtituloMenu() {

        Label subtitulo =
                new Label("Popup para Intranet");

        subtitulo.getStyleClass()
                .add("sidebar-subtitle");

        return subtitulo;
    }

    /**
     * Cria um botão de navegação.
     *
     * @param texto texto exibido no botão
     * @param acao ação executada ao clicar
     * @param ativo indica se começa selecionado
     */
    private MFXButton criarNavButton(
            String texto,
            Runnable acao,
            boolean ativo) {

        MFXButton button =
                new MFXButton(texto);

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.getStyleClass().add(
                ativo
                        ? "central-nav-active"
                        : "central-nav"
        );

        botoesNavegacao.add(button);

        button.setOnAction(
                e -> {

                    ativarNavegacao(button);

                    if (acao != null) {
                        acao.run();
                    }
                }
        );

        return button;
    }

    /**
     * Atualiza o botão selecionado.
     */
    private void ativarNavegacao(
            MFXButton selecionado) {

        for (
                MFXButton botao :
                botoesNavegacao
        ) {

            botao.getStyleClass()
                    .remove(
                            "central-nav-active"
                    );

            if (
                    !botao.getStyleClass()
                            .contains(
                                    "central-nav"
                            )
            ) {

                botao.getStyleClass()
                        .add(
                                "central-nav"
                        );
            }
        }

        selecionado.getStyleClass()
                .remove(
                        "central-nav"
                );

        selecionado.getStyleClass()
                .add(
                        "central-nav-active"
                );
    }

    /**
     * Cria o indicador de conexão/status
     * exibido na parte inferior do menu.
     */
    private Label criarStatusLabel() {

        Label status =
                new Label(
                        "●  Configure o servidor\n\n" +
                                "Versão 1.0.0"
                );

        status.getStyleClass()
                .add("connection");

        return status;
    }
}