package com.example.intranet_adm.view.historico;

import com.example.intranet_adm.model.Aviso;
import com.example.intranet_adm.service.AvisoService;
import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.view.components.AppIcon;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class HistoricoView {

    private final AvisoService avisoService;
    private final IntranetAvisosClient client;

    private final VBox root = new VBox();
    private final VBox listaAvisos = new VBox(10);

    private final Label contadorLabel = new Label();
    private final Label statusLabel = new Label();
    private final TextField buscaField = new TextField();
    private final ComboBox<String> tipoFiltro = new ComboBox<>();
    private final ComboBox<String> periodoFiltro = new ComboBox<>();
    private final ComboBox<String> ordemFiltro = new ComboBox<>();
    private final List<Aviso> avisosCarregados = new ArrayList<>();

    private Consumer<String> onMessage;

    private static final DateTimeFormatter DATA_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public HistoricoView(AvisoService avisoService) {
        this(avisoService, new IntranetAvisosClient());
    }

    public HistoricoView(
            AvisoService avisoService,
            IntranetAvisosClient client) {

        if (avisoService == null || client == null) {
            throw new IllegalArgumentException(
                    "Os serviços do histórico não podem ser nulos.");
        }
        this.avisoService = avisoService;
        this.client = client;

        construir();
        atualizar();
    }

    // ============================================================
    // CONSTRUÇÃO
    // ============================================================

    private void construir() {

        root.setSpacing(14);
        root.setPadding(new Insets(0));
        root.setFillWidth(true);

        HBox cabecalho = criarCabecalho();
        HBox filtros = criarFiltros();

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
        statusLabel.getStyleClass().add("history-status-message");
        statusLabel.managedProperty().bind(
                statusLabel.textProperty().isNotEmpty());

        root.getChildren().addAll(
                cabecalho,
                filtros,
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

        atualizarButton.setGraphic(AppIcon.create(AppIcon.Type.REFRESH, 16));
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

    private HBox criarFiltros() {

        buscaField.setPromptText("Buscar por título, mensagem ou autor");
        buscaField.getStyleClass().add("history-search-field");
        buscaField.setMaxWidth(Double.MAX_VALUE);
        buscaField.textProperty().addListener(
                (observavel, anterior, atual) -> aplicarFiltros());

        tipoFiltro.getItems().addAll(
                "Todos os tipos", "Publicações", "Reaberturas");
        tipoFiltro.setValue("Todos os tipos");
        tipoFiltro.setPrefWidth(155);
        tipoFiltro.setVisibleRowCount(5);
        tipoFiltro.setOnAction(event -> aplicarFiltros());

        periodoFiltro.getItems().addAll(
                "Todo o período", "Hoje", "Últimos 7 dias", "Últimos 30 dias");
        periodoFiltro.setValue("Todo o período");
        periodoFiltro.setPrefWidth(160);
        periodoFiltro.setVisibleRowCount(5);
        periodoFiltro.setOnAction(event -> aplicarFiltros());

        ordemFiltro.getItems().addAll("Mais recentes", "Mais antigos");
        ordemFiltro.setValue("Mais recentes");
        ordemFiltro.setPrefWidth(140);
        ordemFiltro.setVisibleRowCount(5);
        ordemFiltro.setOnAction(event -> aplicarFiltros());

        VBox busca = criarCampoFiltro("BUSCAR", buscaField);
        HBox.setHgrow(busca, Priority.ALWAYS);
        VBox tipo = criarCampoFiltro("TIPO", tipoFiltro);
        VBox periodo = criarCampoFiltro("PERÍODO", periodoFiltro);
        VBox ordem = criarCampoFiltro("ORDENAR", ordemFiltro);

        Button limpar = new Button("Limpar");
        limpar.setGraphic(AppIcon.create(AppIcon.Type.CLOSE, 15));
        limpar.getStyleClass().add("secondary-button");
        limpar.setOnAction(event -> limparFiltros());

        VBox acao = criarCampoFiltro("FILTROS", limpar);
        HBox filtros = new HBox(12, busca, tipo, periodo, ordem, acao);
        filtros.setAlignment(Pos.BOTTOM_LEFT);
        filtros.getStyleClass().addAll("app-card", "history-filter-bar");
        return filtros;
    }

    private VBox criarCampoFiltro(String titulo, Control controle) {
        Label label = new Label(titulo);
        label.getStyleClass().add("history-filter-label");
        controle.getStyleClass().add("history-filter-control");
        VBox campo = new VBox(5, label, controle);
        campo.getStyleClass().add("history-filter-field");
        return campo;
    }

    private void limparFiltros() {
        buscaField.clear();
        tipoFiltro.setValue("Todos os tipos");
        periodoFiltro.setValue("Todo o período");
        ordemFiltro.setValue("Mais recentes");
        aplicarFiltros();
    }

    // ============================================================
    // ATUALIZAR
    // ============================================================

    public void atualizar() {

        try {
            avisosCarregados.clear();
            avisosCarregados.addAll(avisoService.listarTodos());
            statusLabel.setText("");
            aplicarFiltros();

        } catch (Exception error) {
            mostrarStatus(
                    "Não foi possível carregar o histórico.",
                    false
            );
        }
    }

    private void aplicarFiltros() {
        listaAvisos.getChildren().clear();

        String busca = normalizar(buscaField.getText());
        List<Aviso> filtrados = new ArrayList<>();

        for (Aviso aviso : avisosCarregados) {
            if (!correspondeBusca(aviso, busca)) continue;
            if (!correspondeTipo(aviso)) continue;
            if (!correspondePeriodo(aviso)) continue;
            filtrados.add(aviso);
        }

        boolean maisAntigos = "Mais antigos".equals(ordemFiltro.getValue());
        Comparator<LocalDate> comparadorData = maisAntigos
                ? Comparator.nullsLast(Comparator.naturalOrder())
                : Comparator.nullsLast(Comparator.reverseOrder());
        Comparator<Aviso> comparador = Comparator
                .comparing(Aviso::getDataPublicacao, comparadorData)
                .thenComparing(
                        Aviso::getId,
                        maisAntigos
                                ? Comparator.naturalOrder()
                                : Comparator.reverseOrder());
        filtrados.sort(comparador);

        contadorLabel.setText(
                "Exibindo " + filtrados.size() + " de "
                        + avisosCarregados.size() + " registros");

        if (filtrados.isEmpty()) {
            listaAvisos.getChildren().add(criarEstadoVazio());
            return;
        }

        for (Aviso aviso : filtrados) {
            listaAvisos.getChildren().add(criarAvisoCard(aviso));
        }
    }

    private boolean correspondeBusca(Aviso aviso, String busca) {
        if (busca.isBlank()) return true;
        return normalizar(aviso.getTitulo()).contains(busca)
                || normalizar(aviso.getMensagem()).contains(busca)
                || normalizar(aviso.getAutor()).contains(busca);
    }

    private boolean correspondeTipo(Aviso aviso) {
        String filtro = tipoFiltro.getValue();
        if (filtro == null || "Todos os tipos".equals(filtro)) return true;
        boolean reabertura = ehReabertura(aviso);
        return ("Reaberturas".equals(filtro) && reabertura)
                || ("Publicações".equals(filtro) && !reabertura);
    }

    private boolean correspondePeriodo(Aviso aviso) {
        String filtro = periodoFiltro.getValue();
        LocalDate data = aviso.getDataPublicacao();
        if (filtro == null || "Todo o período".equals(filtro)) return true;
        if (data == null) return false;

        LocalDate hoje = LocalDate.now();
        return switch (filtro) {
            case "Hoje" -> data.equals(hoje);
            case "Últimos 7 dias" -> !data.isBefore(hoje.minusDays(6));
            case "Últimos 30 dias" -> !data.isBefore(hoje.minusDays(29));
            default -> true;
        };
    }

    private boolean ehReabertura(Aviso aviso) {
        return normalizar(aviso.getAutor()).contains("reabertura");
    }

    private boolean filtrosAtivos() {
        return !normalizar(buscaField.getText()).isBlank()
                || !"Todos os tipos".equals(tipoFiltro.getValue())
                || !"Todo o período".equals(periodoFiltro.getValue());
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
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
        card.getStyleClass().add(\u0022history-card\u0022);

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
        titulo.getStyleClass().add("history-item-title");
        titulo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titulo, Priority.ALWAYS);

        Label tipoRegistro = new Label(
                ehReabertura(aviso) ? "REABERTURA" : "PUBLICAÇÃO");
        tipoRegistro.getStyleClass().addAll(
                "history-type-badge",
                ehReabertura(aviso)
                        ? "history-type-reopened"
                        : "history-type-published");
        HBox topo = new HBox(10, titulo, tipoRegistro);
        topo.setAlignment(Pos.CENTER_LEFT);

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
        mensagem.getStyleClass().add("history-item-message");

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
        informacoes.getStyleClass().add("history-item-info");

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
        idLabel.getStyleClass().add("history-item-id");

        // --------------------------------------------------------
        // BOTÃO
        // --------------------------------------------------------

        Button removerButton =
                new Button("Remover");

        removerButton.setPrefHeight(34);

        removerButton.setGraphic(AppIcon.create(AppIcon.Type.TRASH, 15));
        removerButton.getStyleClass().add(\u0022danger-button\u0022);
        removerButton.setOnAction(
                event -> confirmarRemocao(aviso)
        );

        Button reabrirButton = new Button("Reabrir popup");
        reabrirButton.setPrefHeight(34);
        reabrirButton.setGraphic(AppIcon.create(AppIcon.Type.REFRESH, 15));
        reabrirButton.getStyleClass().add("secondary-button");
        reabrirButton.setOnAction(
                event -> confirmarReabertura(aviso, reabrirButton));

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
                reabrirButton,
                removerButton
        );

        card.getChildren().addAll(
                topo,
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
                filtrosAtivos()
                        ? "Nenhum resultado encontrado"
                        : "Nenhum aviso encontrado"
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
                filtrosAtivos()
                        ? "Tente alterar ou limpar os filtros aplicados."
                        : "Os avisos criados aparecerão aqui."
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
        if (mostrarConfirmacao(aviso, true)) remover(aviso);
    }

    private void confirmarReabertura(Aviso aviso, Button botaoOrigem) {
        if (mostrarConfirmacao(aviso, false)) {
            reabrirPopup(aviso, botaoOrigem);
        }
    }

    private boolean mostrarConfirmacao(Aviso aviso, boolean exclusao) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(exclusao ? "Remover do histórico" : "Reabrir popup");
        dialogo.initModality(Modality.APPLICATION_MODAL);
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            dialogo.initOwner(root.getScene().getWindow());
        }

        ButtonType confirmar = new ButtonType(
                exclusao ? "Remover registro" : "Reabrir popup",
                ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType(
                "Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        AppIcon.Type tipoIcone = exclusao
                ? AppIcon.Type.TRASH : AppIcon.Type.REFRESH;
        StackPane icone = new StackPane(AppIcon.create(tipoIcone, 22));
        icone.getStyleClass().addAll(
                "history-confirm-icon",
                exclusao
                        ? "history-confirm-icon-danger"
                        : "history-confirm-icon-primary");

        Label titulo = new Label(
                exclusao
                        ? "Remover este registro?"
                        : "Reabrir este popup?");
        titulo.getStyleClass().add("history-confirm-title");
        Label descricao = new Label(
                exclusao
                        ? "O registro será removido somente deste histórico local."
                        : "Uma nova cópia ativa será publicada na Intranet.");
        descricao.setWrapText(true);
        descricao.getStyleClass().add("history-confirm-description");
        HBox cabecalho = new HBox(12, icone, new VBox(3, titulo, descricao));
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        Label previaTitulo = new Label(
                valorOuPadrao(aviso.getTitulo(), "Sem título"));
        previaTitulo.setWrapText(true);
        previaTitulo.getStyleClass().add("history-confirm-preview-title");
        Label previaMensagem = new Label(
                valorOuPadrao(aviso.getMensagem(), "Sem mensagem"));
        previaMensagem.setWrapText(true);
        previaMensagem.setMaxHeight(85);
        previaMensagem.setTooltip(new Tooltip(aviso.getMensagem()));
        previaMensagem.getStyleClass().add("history-confirm-preview-message");
        VBox previa = new VBox(6, previaTitulo, previaMensagem);
        previa.getStyleClass().add("history-confirm-preview");

        Label observacao = new Label(
                exclusao
                        ? "Esta ação não poderá ser desfeita."
                        : "O título e a mensagem serão reutilizados com configuração padrão.");
        observacao.setWrapText(true);
        observacao.getStyleClass().add(
                exclusao
                        ? "history-confirm-warning"
                        : "history-confirm-note");

        VBox conteudo = new VBox(16, cabecalho, previa, observacao);
        conteudo.getStyleClass().add("history-confirm-content");

        DialogPane painel = dialogo.getDialogPane();
        painel.setContent(conteudo);
        painel.getButtonTypes().setAll(cancelar, confirmar);
        painel.getStyleClass().add("history-confirm-dialog");
        painel.setMinWidth(510);
        adicionarEstilosDialogo(painel);

        Button botaoConfirmar = (Button) painel.lookupButton(confirmar);
        Button botaoCancelar = (Button) painel.lookupButton(cancelar);
        botaoConfirmar.getStyleClass().add(
                exclusao ? "danger-button" : "primary-button");
        botaoConfirmar.setGraphic(AppIcon.create(tipoIcone, 16));
        botaoCancelar.getStyleClass().add("secondary-button");
        botaoCancelar.setGraphic(AppIcon.create(AppIcon.Type.CLOSE, 16));
        botaoConfirmar.setDefaultButton(false);
        botaoCancelar.setDefaultButton(true);
        Platform.runLater(botaoCancelar::requestFocus);

        return dialogo.showAndWait().orElse(cancelar) == confirmar;
    }

    private void adicionarEstilosDialogo(DialogPane painel) {
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
    }

    private void reabrirPopup(Aviso aviso, Button botaoOrigem) {
        botaoOrigem.setDisable(true);
        mostrarStatusAviso("Publicando uma nova cópia do popup...");

        Thread thread = new Thread(() -> {
            try {
                client.enviar(aviso.getTitulo(), aviso.getMensagem());

                String alertaHistorico = null;
                try {
                    avisoService.adicionar(
                            aviso.getTitulo(),
                            aviso.getMensagem(),
                            "Central de Avisos · Reabertura");
                } catch (Exception errorHistorico) {
                    alertaHistorico =
                            "Popup reaberto, mas o novo registro não pôde ser salvo.";
                }

                String mensagemFinal = alertaHistorico;
                Platform.runLater(() -> {
                    atualizar();
                    if (mensagemFinal == null) {
                        mostrarStatus("Popup reaberto com sucesso.", true);
                        enviarMensagem("Popup reaberto com sucesso.");
                    } else {
                        mostrarStatusAviso(mensagemFinal);
                        enviarMensagem(mensagemFinal);
                    }
                });
            } catch (Exception error) {
                Platform.runLater(() -> mostrarStatus(
                        "Não foi possível reabrir o popup: "
                                + valorOuPadrao(
                                        error.getMessage(),
                                        "falha de comunicação."),
                        false));
            } finally {
                Platform.runLater(() -> botaoOrigem.setDisable(false));
            }
        }, "reabrir-popup-historico");
        thread.setDaemon(true);
        thread.start();
    }

    @Deprecated
    private void confirmarRemocaoLegada(Aviso aviso) {

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

            atualizar();
            mostrarStatus(
                    "Aviso removido com sucesso.",
                    true
            );
            enviarMensagem(
                    "Aviso removido com sucesso."
            );

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

    private void mostrarStatusAviso(String mensagem) {
        statusLabel.setText(mensagem);
        statusLabel.setTextFill(Color.web("#A8660B"));
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
