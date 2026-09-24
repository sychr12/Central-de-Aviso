package com.example.intranet_adm.view.historico;

import com.example.intranet_adm.model.Aviso;
import com.example.intranet_adm.model.Popup;
import com.example.intranet_adm.service.AvisoService;
import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.view.components.AppIcon;
import com.example.intranet_adm.view.components.PopupMediaView;

import javafx.application.Platform;
import javafx.concurrent.Task;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
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
    private final ComboBox<String> anoFiltro = new ComboBox<>();
    private final ComboBox<String> ordemFiltro = new ComboBox<>();
    private final List<Aviso> avisosCarregados = new ArrayList<>();
    private final Map<String, Popup> popupsPorConteudo = new HashMap<>();
    private long requisicaoMidias;

    private Consumer<String> onMessage;

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
                "Todos os tipos", "Normal", "Atenção", "Urgente", "Crítico");
        tipoFiltro.setValue("Todos os tipos");
        tipoFiltro.setPrefWidth(165);
        tipoFiltro.setVisibleRowCount(5);
        tipoFiltro.setOnAction(event -> aplicarFiltros());

        periodoFiltro.getItems().addAll(
                "Todo o período", "Hoje", "Últimos 7 dias", "Últimos 30 dias");
        periodoFiltro.setValue("Todo o período");
        periodoFiltro.setPrefWidth(160);
        periodoFiltro.setVisibleRowCount(5);
        periodoFiltro.setOnAction(event -> aplicarFiltros());

        anoFiltro.getItems().add("Todos os anos");
        anoFiltro.setValue("Todos os anos");
        anoFiltro.setPrefWidth(125);
        anoFiltro.setVisibleRowCount(6);
        anoFiltro.setOnAction(event -> aplicarFiltros());

        ordemFiltro.getItems().addAll("Mais recentes", "Mais antigos");
        ordemFiltro.setValue("Mais recentes");
        ordemFiltro.setPrefWidth(140);
        ordemFiltro.setVisibleRowCount(5);
        ordemFiltro.setOnAction(event -> aplicarFiltros());

        VBox busca = criarCampoFiltro("BUSCAR", buscaField);
        HBox.setHgrow(busca, Priority.ALWAYS);
        VBox tipo = criarCampoFiltro("TIPO", tipoFiltro);
        VBox periodo = criarCampoFiltro("PERÍODO", periodoFiltro);
        VBox ano = criarCampoFiltro("ANO", anoFiltro);
        VBox ordem = criarCampoFiltro("ORDENAR", ordemFiltro);

        Button limpar = new Button("Limpar");
        limpar.setGraphic(AppIcon.create(AppIcon.Type.CLOSE, 15));
        limpar.getStyleClass().add("secondary-button");
        limpar.setOnAction(event -> limparFiltros());

        VBox acao = criarCampoFiltro("FILTROS", limpar);
        HBox filtros = new HBox(12, busca, tipo, periodo, ano, ordem, acao);
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
        anoFiltro.setValue("Todos os anos");
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
            atualizarAnosDisponiveis();
            statusLabel.setText("");
            aplicarFiltros();
            carregarMidiasDoServidor();

        } catch (Exception error) {
            mostrarStatus(
                    "Não foi possível carregar o histórico.",
                    false
            );
        }
    }

    private void carregarMidiasDoServidor() {
        long requisicaoAtual = ++requisicaoMidias;
        Task<List<Popup>> tarefa = new Task<>() {
            @Override
            protected List<Popup> call() throws Exception {
                return client.listarPopups();
            }
        };
        tarefa.setOnSucceeded(event -> {
            if (requisicaoAtual != requisicaoMidias) return;
            popupsPorConteudo.clear();
            List<Popup> popups = tarefa.getValue();
            if (popups != null) {
                for (Popup popup : popups) {
                    String chave = chaveConteudo(
                            popup.getTitulo(), popup.getMensagem());
                    popupsPorConteudo.merge(chave, popup,
                            (atual, candidato) -> semImagem(atual)
                                    && !semImagem(candidato)
                                    ? candidato : atual);
                }
            }
            aplicarFiltros();
        });
        Thread thread = new Thread(tarefa, "historico-popup-media");
        thread.setDaemon(true);
        thread.start();
    }

    private String chaveConteudo(String titulo, String mensagem) {
        return normalizar(titulo) + "\u0000" + normalizarMensagem(mensagem);
    }

    private String normalizarMensagem(String mensagem) {
        return normalizar(mensagem)
                .replace("{{imagem}}", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private boolean semImagem(Popup popup) {
        return popup == null || popup.getImagem() == null
                || popup.getImagem().isBlank();
    }

    private void aplicarFiltros() {
        listaAvisos.getChildren().clear();

        String busca = normalizar(buscaField.getText());
        List<Aviso> filtrados = new ArrayList<>();

        for (Aviso aviso : avisosCarregados) {
            if (!correspondeBusca(aviso, busca)) continue;
            if (!correspondeTipo(aviso)) continue;
            if (!correspondePeriodo(aviso)) continue;
            if (!correspondeAno(aviso)) continue;
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
        return filtro.equals(classificarTipo(aviso));
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

    private void atualizarAnosDisponiveis() {
        String selecaoAtual = anoFiltro.getValue();
        List<String> opcoes = new ArrayList<>(List.of("Todos os anos"));

        TreeSet<Integer> anos = new TreeSet<>(Comparator.reverseOrder());
        anos.add(LocalDate.now().getYear());
        for (Aviso aviso : avisosCarregados) {
            if (aviso.getDataPublicacao() != null) {
                anos.add(aviso.getDataPublicacao().getYear());
            }
        }
        for (Integer ano : anos) {
            opcoes.add(String.valueOf(ano));
        }

        anoFiltro.getItems().setAll(opcoes);
        anoFiltro.setValue(opcoes.contains(selecaoAtual)
                ? selecaoAtual
                : "Todos os anos");
        anoFiltro.setVisibleRowCount(Math.min(8, opcoes.size()));
    }

    private boolean correspondeAno(Aviso aviso) {
        String filtro = anoFiltro.getValue();
        if (filtro == null || "Todos os anos".equals(filtro)) return true;

        LocalDate data = aviso.getDataPublicacao();
        return data != null && data.getYear() == Integer.parseInt(filtro);
    }

    private String classificarTipo(Aviso aviso) {
        String criticidade = normalizar(aviso.getCriticidade());
        String prioridade = normalizar(aviso.getPrioridade());

        if (criticidade.contains("crít") || criticidade.contains("crit")
                || prioridade.contains("imediat")) {
            return "Crítico";
        }
        if (prioridade.contains("urgent") || criticidade.equals("alta")) {
            return "Urgente";
        }
        if (criticidade.contains("moderad") || prioridade.equals("alta")) {
            return "Atenção";
        }
        return "Normal";
    }

    private boolean ehReabertura(Aviso aviso) {
        return normalizar(aviso.getAutor()).contains("reabertura");
    }

    private boolean filtrosAtivos() {
        return !normalizar(buscaField.getText()).isBlank()
                || !"Todos os tipos".equals(tipoFiltro.getValue())
                || !"Todo o período".equals(periodoFiltro.getValue())
                || !"Todos os anos".equals(anoFiltro.getValue());
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
    }

    // ============================================================
    // CARD DO AVISO
    // ============================================================

    private VBox criarAvisoCard(Aviso aviso) {
        String tema = temaHistorico(aviso);
        VBox card = new VBox();
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().addAll(
                "app-card", "history-card", "popup-library-card",
                "popup-library-theme-" + tema);

        Region barra = new Region();
        barra.getStyleClass().add("popup-library-topbar");

        StackPane icone = new StackPane(
                AppIcon.create(AppIcon.Type.BUILDING, 17));
        icone.getStyleClass().add("popup-library-brand-icon");

        Label titulo = new Label(valorOuPadrao(
                aviso.getTitulo(), "Sem título"));
        titulo.setWrapText(true);
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.getStyleClass().add("popup-library-title");

        Label classificacao = new Label(
                criticidadeExibida(aviso) + " · " + prioridadeExibida(aviso));
        classificacao.getStyleClass().add("popup-library-badge");
        VBox identificacao = new VBox(5, titulo, classificacao);
        identificacao.setMinWidth(0);
        HBox.setHgrow(identificacao, Priority.ALWAYS);

        Label tipoRegistro = new Label(
                ehReabertura(aviso) ? "REABERTURA" : "PUBLICAÇÃO");
        tipoRegistro.getStyleClass().add("history-library-kind");

        HBox cabecalho = new HBox(
                12, icone, identificacao, tipoRegistro);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.getStyleClass().add("popup-library-header");

        Label mensagem = new Label(valorOuPadrao(
                aviso.getMensagem(), "Sem mensagem informada."));
        mensagem.setWrapText(true);
        mensagem.setMaxWidth(Double.MAX_VALUE);
        mensagem.getStyleClass().add("popup-library-message");

        VBox conteudoMensagem = new VBox(9, mensagem);
        conteudoMensagem.setMinWidth(0);
        HBox.setHgrow(conteudoMensagem, Priority.ALWAYS);
        Popup popupRelacionado = popupsPorConteudo.get(chaveConteudo(
                aviso.getTitulo(), aviso.getMensagem()));
        if (popupRelacionado != null
                && PopupMediaView.ehImagem(
                        popupRelacionado.getImagem(),
                        popupRelacionado.getImagemMimeType())) {
            conteudoMensagem.getChildren().add(PopupMediaView.criar(
                    popupRelacionado.getImagem(), 360, 210,
                    "popup-library-media"));
        }

        StackPane alertaIcone = new StackPane(AppIcon.create(
                "normal".equals(tema)
                        ? AppIcon.Type.INFO : AppIcon.Type.WARNING,
                18));
        alertaIcone.getStyleClass().add("popup-library-alert-icon");
        HBox mensagemBox = new HBox(12, alertaIcone, conteudoMensagem);
        mensagemBox.setAlignment(Pos.TOP_LEFT);
        mensagemBox.getStyleClass().add("popup-library-message-box");

        String autor = valorOuPadrao(aviso.getAutor(), "Desconhecido");
        Node autorCard = criarMetaHistorico(
                AppIcon.Type.USERS, "AUTOR", autor);
        HBox.setHgrow(autorCard, Priority.ALWAYS);
        HBox metadados = new HBox(9, autorCard);
        metadados.getStyleClass().add("popup-library-dates");

        VBox corpo = new VBox(10, mensagemBox, metadados);
        corpo.getStyleClass().add("popup-library-body");

        Label idLabel = new Label("ID: " + aviso.getId());
        idLabel.getStyleClass().add("history-item-id");

        Button reabrirButton = new Button("Reabrir popup");
        reabrirButton.setPrefHeight(34);
        reabrirButton.setGraphic(AppIcon.create(AppIcon.Type.REFRESH, 15));
        reabrirButton.getStyleClass().add("secondary-button");
        reabrirButton.setOnAction(
                event -> confirmarReabertura(aviso, reabrirButton));

        Button removerButton = new Button("Remover");
        removerButton.setPrefHeight(34);
        removerButton.setGraphic(AppIcon.create(AppIcon.Type.TRASH, 15));
        removerButton.getStyleClass().add("danger-button");
        removerButton.setOnAction(event -> confirmarRemocao(aviso));

        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);
        HBox rodape = new HBox(
                9, idLabel, espacador, reabrirButton, removerButton);
        rodape.setAlignment(Pos.CENTER_LEFT);
        rodape.getStyleClass().add("popup-library-actions");

        card.getChildren().addAll(barra, cabecalho, corpo, rodape);
        return card;
    }

    private Node criarMetaHistorico(
            AppIcon.Type tipo, String rotulo, String valor) {
        StackPane icone = new StackPane(AppIcon.create(tipo, 14));
        icone.getStyleClass().add("popup-library-date-icon");
        Label titulo = new Label(rotulo);
        titulo.getStyleClass().add("popup-library-date-label");
        Label conteudo = new Label(valor);
        conteudo.getStyleClass().add("popup-library-date-value");
        VBox textos = new VBox(1, titulo, conteudo);
        HBox card = new HBox(8, icone, textos);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("popup-library-date-card");
        return card;
    }

    private String temaHistorico(Aviso aviso) {
        return switch (classificarTipo(aviso)) {
            case "Crítico" -> "critical";
            case "Urgente" -> "high";
            case "Atenção" -> "moderate";
            default -> "normal";
        };
    }

    private String criticidadeExibida(Aviso aviso) {
        String valor = normalizar(aviso.getCriticidade());
        if (valor.contains("critical") || valor.contains("critic")) return "Crítica";
        if (valor.equals("high") || valor.equals("alta")) return "Alta";
        if (valor.contains("moderate") || valor.contains("moderad")) return "Moderada";
        if (valor.equals("low") || valor.equals("baixa")) return "Baixa";
        return "Informativa";
    }

    private String prioridadeExibida(Aviso aviso) {
        String valor = normalizar(aviso.getPrioridade());
        if (valor.contains("immediate") || valor.contains("imediat")) return "Imediata";
        if (valor.contains("urgent")) return "Urgente";
        if (valor.equals("high") || valor.equals("alta")) return "Alta";
        if (valor.equals("low") || valor.equals("baixa")) return "Baixa";
        return "Normal";
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
                            "Central de Avisos · Reabertura",
                            aviso.getCriticidade(),
                            aviso.getPrioridade());
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
