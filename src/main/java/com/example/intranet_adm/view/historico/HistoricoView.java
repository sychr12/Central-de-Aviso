package com.example.intranet_adm.view.historico;

import com.example.intranet_adm.model.Aviso;
import com.example.intranet_adm.service.AvisoService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class HistoricoView {

    private final AvisoService avisoService;

    private final VBox root = new VBox();
    private final VBox listaAvisos = new VBox(10);

    private final Label contadorLabel = new Label();
    private final Label statusLabel = new Label();

    private Consumer<String> onMessage;

    private static final DateTimeFormatter DATA_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public HistoricoView(AvisoService avisoService) {
        this.avisoService = avisoService;

        construir();
        atualizar();
    }

    // ============================================================
    // CONSTRUÇÃO
    // ============================================================

    private void construir() {

        root.setSpacing(20);
        root.setPadding(new Insets(0));
        root.setFillWidth(true);

        Label titulo = criarTitulo();

        Label descricao = new Label(
                "Consulte os avisos criados anteriormente."
        );

        descricao.setFont(
                Font.font("System", 14)
        );

        descricao.setTextFill(
                Color.web("#64748B")
        );

        HBox cabecalho = criarCabecalho();

        ScrollPane scrollPane = new ScrollPane(
                listaAvisos
        );

        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );
        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.setStyle(
                """
                -fx-background-color: transparent;
                -fx-background: transparent;
                """
        );

        VBox.setVgrow(
                scrollPane,
                Priority.ALWAYS
        );

        statusLabel.setText("");
        statusLabel.setTextFill(
                Color.web("#64748B")
        );

        root.getChildren().addAll(
                cabecalho,
                statusLabel,
                scrollPane
        );
    }

    // ============================================================
    // TÍTULO
    // ============================================================

    private Label criarTitulo() {

        Label titulo = new Label(
                "Histórico de Avisos"
        );

        titulo.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        28
                )
        );

        titulo.setTextFill(
                Color.web("#172B4D")
        );

        return titulo;
    }

    // ============================================================
    // CABEÇALHO
    // ============================================================

    private HBox criarCabecalho() {

        HBox cabecalho = new HBox(10);

        cabecalho.setAlignment(
                Pos.CENTER_LEFT
        );

        contadorLabel.setFont(
                Font.font(
                        "System",
                        FontWeight.NORMAL,
                        14
                )
        );

        contadorLabel.setTextFill(
                Color.web("#64748B")
        );

        Region espacador = new Region();

        HBox.setHgrow(
                espacador,
                Priority.ALWAYS
        );

        Button atualizarButton =
                new Button("Atualizar");

        atualizarButton.setPrefHeight(38);
        atualizarButton.getStyleClass().add("secondary-button");

        atualizarButton.setOnAction(
                event -> atualizar()
        );

        cabecalho.getChildren().addAll(
                contadorLabel,
                espacador,
                atualizarButton
        );

        return cabecalho;
    }

    // ============================================================
    // ATUALIZAR
    // ============================================================

    public void atualizar() {

        listaAvisos.getChildren().clear();

        List<Aviso> avisos;

        try {

            avisos = avisoService.listarTodos();

        } catch (Exception error) {

            mostrarStatus(
                    "Não foi possível carregar o histórico.",
                    false
            );

            return;
        }

        contadorLabel.setText(
                avisos.size()
                        + (avisos.size() == 1
                        ? " aviso encontrado"
                        : " avisos encontrados")
        );

        if (avisos.isEmpty()) {

            listaAvisos.getChildren().add(
                    criarEstadoVazio()
            );

            return;
        }

        for (Aviso aviso : avisos) {

            listaAvisos.getChildren().add(
                    criarAvisoCard(aviso)
            );
        }
    }

    // ============================================================
    // CARD DO AVISO
    // ============================================================

    private VBox criarAvisoCard(Aviso aviso) {

        VBox card = new VBox(10);

        card.setPadding(
                new Insets(18)
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.getStyleClass().add("app-card");

        // --------------------------------------------------------
        // TÍTULO
        // --------------------------------------------------------

        Label titulo = new Label(
                valorOuPadrao(
                        aviso.getTitulo(),
                        "Sem título"
                )
        );

        titulo.setWrapText(true);

        titulo.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        17
                )
        );

        titulo.setTextFill(
                Color.web("#172B4D")
        );

        // --------------------------------------------------------
        // MENSAGEM
        // --------------------------------------------------------

        Label mensagem = new Label(
                valorOuPadrao(
                        aviso.getMensagem(),
                        ""
                )
        );

        mensagem.setWrapText(true);

        mensagem.setFont(
                Font.font(
                        "System",
                        14
                )
        );

        mensagem.setTextFill(
                Color.web("#4B5563")
        );

        // --------------------------------------------------------
        // INFORMAÇÕES
        // --------------------------------------------------------

        String autor = valorOuPadrao(
                aviso.getAutor(),
                "Desconhecido"
        );

        String data = aviso.getDataPublicacao() == null
                ? "Data desconhecida"
                : aviso.getDataPublicacao()
                .format(DATA_FORMATTER);

        Label informacoes = new Label(
                "Autor: " + autor
                        + "  •  Publicado em: " + data
        );

        informacoes.setFont(
                Font.font(
                        "System",
                        12
                )
        );

        informacoes.setTextFill(
                Color.web("#64748B")
        );

        // --------------------------------------------------------
        // ID
        // --------------------------------------------------------

        Label idLabel = new Label(
                "ID: " + aviso.getId()
        );

        idLabel.setFont(
                Font.font(
                        "System",
                        11
                )
        );

        idLabel.setTextFill(
                Color.web("#9CA3AF")
        );

        // --------------------------------------------------------
        // BOTÃO
        // --------------------------------------------------------

        Button removerButton =
                new Button("Remover");

        removerButton.setPrefHeight(34);

        removerButton.setOnAction(
                event -> confirmarRemocao(aviso)
        );

        HBox rodape = new HBox(10);

        rodape.setAlignment(
                Pos.CENTER_LEFT
        );

        Region espacador = new Region();

        HBox.setHgrow(
                espacador,
                Priority.ALWAYS
        );

        rodape.getChildren().addAll(
                idLabel,
                espacador,
                removerButton
        );

        card.getChildren().addAll(
                titulo,
                mensagem,
                informacoes,
                rodape
        );

        return card;
    }

    // ============================================================
    // ESTADO VAZIO
    // ============================================================

    private VBox criarEstadoVazio() {

        VBox vazio = new VBox(8);

        vazio.setAlignment(
                Pos.CENTER
        );

        vazio.setPadding(
                new Insets(50)
        );

        Label titulo = new Label(
                "Nenhum aviso encontrado"
        );

        titulo.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        18
                )
        );

        titulo.setTextFill(
                Color.web("#334155")
        );

        Label descricao = new Label(
                "Os avisos criados aparecerão aqui."
        );

        descricao.setFont(
                Font.font(
                        "System",
                        14
                )
        );

        descricao.setTextFill(
                Color.web("#9CA3AF")
        );

        vazio.getChildren().addAll(
                titulo,
                descricao
        );

        return vazio;
    }

    // ============================================================
    // CONFIRMAR REMOÇÃO
    // ============================================================

    private void confirmarRemocao(Aviso aviso) {

        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alerta.setTitle(
                "Remover aviso"
        );

        alerta.setHeaderText(
                "Deseja remover este aviso?"
        );

        alerta.setContentText(
                "O aviso \"" +
                        valorOuPadrao(
                                aviso.getTitulo(),
                                "Sem título"
                        )
                        + "\" será removido do histórico."
        );

        ButtonType confirmar =
                new ButtonType(
                        "Remover",
                        ButtonBar.ButtonData.OK_DONE
                );

        ButtonType cancelar =
                new ButtonType(
                        "Cancelar",
                        ButtonBar.ButtonData.CANCEL_CLOSE
                );

        alerta.getButtonTypes().setAll(
                confirmar,
                cancelar
        );

        alerta.showAndWait().ifPresent(
                resultado -> {

                    if (resultado == confirmar) {
                        remover(aviso);
                    }
                }
        );
    }

    // ============================================================
    // REMOVER
    // ============================================================

    private void remover(Aviso aviso) {

        try {

            boolean removido =
                    avisoService.remover(
                            aviso.getId()
                    );

            if (!removido) {

                mostrarStatus(
                        "O aviso não foi encontrado.",
                        false
                );

                atualizar();

                return;
            }

            mostrarStatus(
                    "Aviso removido com sucesso.",
                    true
            );

            enviarMensagem(
                    "Aviso removido com sucesso."
            );

            atualizar();

        } catch (Exception error) {

            mostrarStatus(
                    "Não foi possível remover o aviso: "
                            + error.getMessage(),
                    false
            );
        }
    }

    // ============================================================
    // STATUS
    // ============================================================

    private void mostrarStatus(
            String mensagem,
            boolean sucesso
    ) {

        statusLabel.setText(
                mensagem
        );

        statusLabel.setTextFill(
                sucesso
                        ? Color.web("#16A34A")
                        : Color.web("#DC2626")
        );
    }

    // ============================================================
    // MENSAGEM PARA A VIEW PRINCIPAL
    // ============================================================

    public void setOnMessage(
            Consumer<String> onMessage
    ) {

        this.onMessage = onMessage;
    }

    private void enviarMensagem(
            String mensagem
    ) {

        if (onMessage != null) {
            onMessage.accept(mensagem);
        }
    }

    // ============================================================
    // VIEW
    // ============================================================

    public Node getView() {
        return root;
    }

    public VBox getRoot() {
        return root;
    }

    // ============================================================
    // AUXILIAR
    // ============================================================

    private static String valorOuPadrao(
            String valor,
            String padrao
    ) {

        if (valor == null || valor.isBlank()) {
            return padrao;
        }

        return valor;
    }
}
