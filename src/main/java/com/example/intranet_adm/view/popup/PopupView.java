package com.example.intranet_adm.view.popup;

import com.example.intranet_adm.model.Popup;
import com.example.intranet_adm.service.IntranetAvisosClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.util.List;
import java.util.function.Consumer;
import java.awt.Desktop;
import java.net.URI;

public class PopupView {

    private final IntranetAvisosClient client;

    private final VBox root;
    private final VBox listaPopups;

    private final Label statusLabel;
    private final Label quantidadeLabel;

    private Consumer<String> mensagemCallback;

    // ============================================================
    // CRIAÇÃO DA VIEW
    // ============================================================

    /**
     * Método utilizado pelo menu para criar a tela.
     */
    public static VBox criar(IntranetAvisosClient client) {
        PopupView view = new PopupView(client);
        return view.getRoot();
    }

    // ============================================================
    // CONSTRUTOR
    // ============================================================

    public PopupView(IntranetAvisosClient client) {

        if (client == null) {
            throw new IllegalArgumentException(
                    "IntranetAvisosClient não pode ser null."
            );
        }

        this.client = client;

        this.listaPopups = new VBox(12);
        this.listaPopups.setFillWidth(true);

        this.statusLabel = new Label(
                "Carregando popups..."
        );

        this.quantidadeLabel = new Label(
                "0 popups"
        );

        this.root = criarTela();

        carregarPopups();
    }

    // ============================================================
    // TELA PRINCIPAL
    // ============================================================

    private VBox criarTela() {

        VBox container = new VBox(20);

        container.setPadding(
                new Insets(0)
        );

        container.setFillWidth(true);

        Node cabecalho = criarCabecalho();
        Node areaLista = criarAreaLista();

        container.getChildren().addAll(
                cabecalho,
                areaLista
        );

        VBox.setVgrow(
                areaLista,
                Priority.ALWAYS
        );

        return container;
    }

    // ============================================================
    // CABEÇALHO
    // ============================================================

    private Node criarCabecalho() {

        VBox cabecalho = new VBox(6);

        Label titulo = new Label(
                "POPUPS"
        );

        titulo.getStyleClass().add(
                "page-title"
        );

        Label descricao = new Label(
                "Acompanhe, pesquise e gerencie os avisos publicados."
        );

        descricao.getStyleClass().add(
                "page-subtitle"
        );

        HBox informacoes = new HBox(10);

        informacoes.setAlignment(
                Pos.CENTER_LEFT
        );

        quantidadeLabel.getStyleClass().add(
                "popup-count"
        );

        Button atualizarButton = new Button(
                "Atualizar"
        );

        atualizarButton.getStyleClass().add(
                "secondary-button"
        );

        atualizarButton.setOnAction(
                event -> carregarPopups()
        );

        Region espacador = new Region();

        HBox.setHgrow(
                espacador,
                Priority.ALWAYS
        );

        informacoes.getChildren().addAll(
                quantidadeLabel,
                espacador,
                atualizarButton
        );

        cabecalho.getChildren().addAll(
                informacoes
        );

        return cabecalho;
    }

    // ============================================================
    // ÁREA DE LISTAGEM
    // ============================================================

    private Node criarAreaLista() {

        VBox area = new VBox(12);

        statusLabel.getStyleClass().add(
                "status-label"
        );

        ScrollPane scrollPane = new ScrollPane(
                listaPopups
        );

        scrollPane.setFitToWidth(true);

        scrollPane.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        scrollPane.setVbarPolicy(
                ScrollPane.ScrollBarPolicy.AS_NEEDED
        );

        scrollPane.getStyleClass().add(
                "popup-scroll"
        );

        VBox.setVgrow(
                scrollPane,
                Priority.ALWAYS
        );

        area.getChildren().addAll(
                statusLabel,
                scrollPane
        );

        VBox.setVgrow(
                scrollPane,
                Priority.ALWAYS
        );

        return area;
    }

    // ============================================================
    // CARREGAMENTO
    // ============================================================

    public void carregarPopups() {

        listaPopups.getChildren().clear();

        statusLabel.setText(
                "Carregando popups..."
        );

        quantidadeLabel.setText(
                "0 popups"
        );

        Thread thread = new Thread(() -> {

            try {

                List<Popup> popups =
                        client.listarPopups();

                Platform.runLater(() ->
                        exibirPopups(popups)
                );

            } catch (Exception error) {

                Platform.runLater(() ->
                        mostrarErro(error)
                );
            }

        });

        thread.setDaemon(true);
        thread.start();
    }

    // ============================================================
    // EXIBIR POPUPS
    // ============================================================

    private void exibirPopups(
            List<Popup> popups
    ) {

        listaPopups.getChildren().clear();

        if (popups == null || popups.isEmpty()) {

            quantidadeLabel.setText(
                    "0 popups"
            );

            statusLabel.setText(
                    "Nenhum popup encontrado."
            );

            listaPopups.getChildren().add(
                    criarEstadoVazio()
            );

            return;
        }

        quantidadeLabel.setText(
                popups.size() +
                        (
                                popups.size() == 1
                                        ? " popup"
                                        : " popups"
                        )
        );

        statusLabel.setText(
                "Popups cadastrados na intranet"
        );

        for (
                Popup popup :
                popups
        ) {

            listaPopups.getChildren().add(
                    criarCardPopup(popup)
            );
        }
    }

    // ============================================================
    // CARD DO POPUP
    // ============================================================

    private Node criarCardPopup(
            Popup popup
    ) {

        VBox card = new VBox(12);

        card.setPadding(
                new Insets(18)
        );

        card.getStyleClass().add(
                "popup-card"
        );

        // --------------------------------------------------------
        // CABEÇALHO
        // --------------------------------------------------------

        HBox cabecalho = new HBox(10);

        cabecalho.setAlignment(
                Pos.CENTER_LEFT
        );

        Label titulo = new Label(
                popup.getTitulo()
        );

        titulo.getStyleClass().add(
                "popup-title"
        );

        Label status = criarStatus(
                popup.isAtivo()
        );

        Label anexo = criarIndicadorAnexo(popup.getImagem());

        Region espacador = new Region();

        HBox.setHgrow(
                espacador,
                Priority.ALWAYS
        );

        cabecalho.getChildren().addAll(
                titulo,
                espacador,
                anexo,
                status
        );

        // --------------------------------------------------------
        // MENSAGEM
        // --------------------------------------------------------

        Label mensagem = new Label(
                popup.getMensagem()
        );

        mensagem.setWrapText(true);

        mensagem.getStyleClass().add(
                "popup-message"
        );

        ImageView miniatura = new ImageView();
        miniatura.setFitWidth(92); miniatura.setFitHeight(58); miniatura.setPreserveRatio(true);
        if (popup.getImagem() != null && !popup.getImagem().isBlank()) {
            try {
                // Carrega a miniatura de forma síncrona para ela já aparecer
                // quando o histórico terminar de montar o card.
                Image imagem = new Image(popup.getImagem(), 92, 58, true, true, false);
                if (!imagem.isError()) {
                    miniatura.setImage(imagem);
                } else {
                    System.err.println("Não foi possível carregar a imagem do popup: " + popup.getImagem());
                }
            } catch (Exception error) {
                System.err.println("Erro ao carregar a imagem do popup: " + error.getMessage());
            }
        }

        // --------------------------------------------------------
        // INFORMAÇÕES
        // --------------------------------------------------------

        HBox informacoes = new HBox(15);

        informacoes.setAlignment(
                Pos.CENTER_LEFT
        );

        Label modelo = criarInformacao(
                "Modelo",
                popup.getModelo()
        );

        Label tamanho = criarInformacao(
                "Tamanho",
                popup.getTamanho()
        );

        Label paginas = criarInformacao(
                "Páginas",
                popup.getPaginas()
        );

        informacoes.getChildren().addAll(
                modelo,
                tamanho,
                paginas
        );

        // --------------------------------------------------------
        // AÇÕES
        // --------------------------------------------------------

        HBox acoes = criarAcoes(
                popup
        );

        VBox conteudo = new VBox(8, mensagem);
        if (popup.getImagem() != null && !popup.getImagem().isBlank() && !ehImagem(popup.getImagem())) {
            String tipo = tipoAnexo(popup.getImagem());
            Button abrirAnexo = new Button("Abrir " + tipo);
            abrirAnexo.getStyleClass().add("secondary-button");
            abrirAnexo.setOnAction(event -> abrirAnexo(popup.getImagem()));
            conteudo.getChildren().add(abrirAnexo);
        }
        HBox resumo = new HBox(12, miniatura, conteudo);
        card.getChildren().addAll(
                cabecalho,
                resumo,
                informacoes,
                new Separator(),
                acoes
        );

        return card;
    }

    private boolean ehImagem(String url) {
        String valor = url.toLowerCase();
        return valor.startsWith("data:image/") || valor.matches(".*\\.(png|jpe?g|gif|webp|bmp)(\\?.*)?$");
    }

    private String tipoAnexo(String url) {
        String valor = url.toLowerCase();
        if (valor.contains("pdf")) return "PDF";
        if (valor.matches(".*\\.(mp4|webm|ogv|mov)(\\?.*)?$")) return "vídeo";
        if (valor.matches(".*\\.(mp3|ogg|wav)(\\?.*)?$")) return "áudio";
        return "anexo";
    }

    private void abrirAnexo(String url) {
        try {
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception error) {
            System.err.println("Não foi possível abrir o anexo: " + error.getMessage());
        }
    }

    private Label criarIndicadorAnexo(String imagem) {
        boolean possuiAnexo = imagem != null && !imagem.isBlank();
        Label indicador = new Label(possuiAnexo ? "📎  ANEXO" : "");
        indicador.getStyleClass().add(possuiAnexo ? "popup-attachment" : "popup-attachment-empty");
        indicador.setTooltip(new Tooltip(possuiAnexo ? "Este popup possui uma imagem ou arquivo anexado." : ""));
        return indicador;
    }

    // ============================================================
    // STATUS
    // ============================================================

    private Label criarStatus(
            boolean ativo
    ) {

        Label status = new Label(
                ativo
                        ? "ATIVO"
                        : "INATIVO"
        );

        status.getStyleClass().add(
                ativo
                        ? "popup-status-active"
                        : "popup-status-inactive"
        );

        return status;
    }

    // ============================================================
    // INFORMAÇÃO
    // ============================================================

    private Label criarInformacao(
            String nome,
            String valor
    ) {

        String texto =
                nome +
                        ": " +
                        (
                                valor == null ||
                                        valor.isBlank()
                                        ? "-"
                                        : valor
                        );

        Label label = new Label(
                texto
        );

        label.getStyleClass().add(
                "popup-information"
        );

        return label;
    }

    // ============================================================
    // AÇÕES
    // ============================================================

    private HBox criarAcoes(
            Popup popup
    ) {

        HBox acoes = new HBox(8);

        acoes.setAlignment(
                Pos.CENTER_RIGHT
        );

        Button statusButton = new Button(
                popup.isAtivo()
                        ? "Desativar"
                        : "Ativar"
        );

        statusButton.getStyleClass().add(
                popup.isAtivo()
                        ? "warning-button"
                        : "success-button"
        );

        statusButton.setOnAction(
                event ->
                        alterarStatus(
                                popup,
                                statusButton
                        )
        );

        Button excluirButton = new Button(
                "Excluir"
        );

        excluirButton.getStyleClass().add(
                "danger-button"
        );

        excluirButton.setOnAction(
                event ->
                        confirmarExclusao(popup)
        );

        acoes.getChildren().addAll(
                statusButton,
                excluirButton
        );

        return acoes;
    }

    // ============================================================
    // ALTERAR STATUS
    // ============================================================

    private void alterarStatus(
            Popup popup,
            Button button
    ) {

        button.setDisable(true);

        boolean novoStatus =
                !popup.isAtivo();

        Thread thread = new Thread(() -> {

            try {

                client.alterarStatusPopup(
                        popup.getId(),
                        novoStatus
                );

                Platform.runLater(() -> {

                    mostrarMensagem(
                            novoStatus
                                    ? "Popup ativado com sucesso."
                                    : "Popup desativado com sucesso."
                    );

                    carregarPopups();
                });

            } catch (Exception error) {

                Platform.runLater(() -> {

                    button.setDisable(false);

                    mostrarErro(error);
                });
            }

        });

        thread.setDaemon(true);
        thread.start();
    }

    // ============================================================
    // EXCLUSÃO
    // ============================================================

    private void confirmarExclusao(
            Popup popup
    ) {

        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION
        );

        alerta.setTitle(
                "Excluir Popup"
        );

        alerta.setHeaderText(
                "Deseja excluir este popup?"
        );

        alerta.setContentText(
                "O popup \"" +
                        popup.getTitulo() +
                        "\" será removido da intranet."
        );

        ButtonType confirmar =
                new ButtonType(
                        "Excluir",
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
                        excluirPopup(popup);
                    }
                }
        );
    }

    private void excluirPopup(
            Popup popup
    ) {

        Thread thread = new Thread(() -> {

            try {

                client.excluirPopup(
                        popup.getId()
                );

                Platform.runLater(() -> {

                    mostrarMensagem(
                            "Popup excluído com sucesso."
                    );

                    carregarPopups();
                });

            } catch (Exception error) {

                Platform.runLater(() ->
                        mostrarErro(error)
                );
            }

        });

        thread.setDaemon(true);
        thread.start();
    }

    // ============================================================
    // ESTADO VAZIO
    // ============================================================

    private VBox criarEstadoVazio() {

        VBox vazio = new VBox(10);

        vazio.setAlignment(
                Pos.CENTER
        );

        vazio.setPadding(
                new Insets(50)
        );

        Label icone = new Label("◉");

        icone.getStyleClass().add(
                "empty-icon"
        );

        Label titulo = new Label(
                "Nenhum popup encontrado"
        );

        titulo.getStyleClass().add(
                "empty-title"
        );

        Label descricao = new Label(
                "Os avisos criados na Central aparecerão aqui."
        );

        descricao.getStyleClass().add(
                "empty-description"
        );

        vazio.getChildren().addAll(
                icone,
                titulo,
                descricao
        );

        return vazio;
    }

    // ============================================================
    // MENSAGENS
    // ============================================================

    private void mostrarMensagem(
            String mensagem
    ) {

        statusLabel.setText(
                mensagem
        );

        if (mensagemCallback != null) {

            mensagemCallback.accept(
                    mensagem
            );
        }
    }

    private void mostrarErro(
            Exception error
    ) {

        String mensagem =
                error.getMessage() == null
                        ? "Erro desconhecido."
                        : error.getMessage();

        statusLabel.setText(
                "Erro: " + mensagem
        );

        if (mensagemCallback != null) {

            mensagemCallback.accept(
                    "Erro: " + mensagem
            );
        }
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public VBox getRoot() {
        return root;
    }

    public void setMensagemCallback(
            Consumer<String> callback
    ) {

        this.mensagemCallback = callback;
    }

    public void atualizar() {
        carregarPopups();
    }
}
