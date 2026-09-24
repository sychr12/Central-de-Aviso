package com.example.intranet_adm.view.popup;

import com.example.intranet_adm.model.Popup;
import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.view.components.AppDialog;
import com.example.intranet_adm.view.components.AppIcon;
import com.example.intranet_adm.view.components.PopupMediaView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDate;

public class PopupView {

    private final IntranetAvisosClient client;

    private final VBox root;
    private final VBox listaPopups;

    private final Label statusLabel;
    private final Label quantidadeLabel;
    private final TextField buscaField = new TextField();
    private final ComboBox<String> criticidadeFiltro = new ComboBox<>();
    private final ComboBox<String> periodoFiltro = new ComboBox<>();
    private final ComboBox<String> anoFiltro = new ComboBox<>();
    private final ComboBox<String> ordemFiltro = new ComboBox<>();
    private final List<Popup> popupsCarregados = new ArrayList<>();

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
                "0 popups ativos"
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
        Node filtros = criarFiltros();
        Node areaLista = criarAreaLista();

        container.getChildren().addAll(
                cabecalho,
                filtros,
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

        atualizarButton.setGraphic(AppIcon.create(AppIcon.Type.REFRESH, 16));
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

    private Node criarFiltros() {
        buscaField.setPromptText("Buscar por título ou mensagem");
        buscaField.setMaxWidth(Double.MAX_VALUE);
        buscaField.textProperty().addListener(
                (observavel, anterior, atual) -> aplicarFiltros());

        criticidadeFiltro.getItems().addAll(
                "Todos os tipos", "Normal", "Atenção", "Urgente", "Crítico");
        criticidadeFiltro.setValue("Todos os tipos");
        criticidadeFiltro.setPrefWidth(140);
        criticidadeFiltro.setOnAction(event -> aplicarFiltros());

        periodoFiltro.getItems().addAll(
                "Todo o período", "Hoje", "Últimos 7 dias", "Últimos 30 dias");
        periodoFiltro.setValue("Todo o período");
        periodoFiltro.setPrefWidth(145);
        periodoFiltro.setOnAction(event -> aplicarFiltros());

        anoFiltro.getItems().add("Todos os anos");
        anoFiltro.setValue("Todos os anos");
        anoFiltro.setPrefWidth(120);
        anoFiltro.setOnAction(event -> aplicarFiltros());

        ordemFiltro.getItems().addAll(
                "Mais recentes", "Mais antigos");
        ordemFiltro.setValue("Mais recentes");
        ordemFiltro.setPrefWidth(135);
        ordemFiltro.setOnAction(event -> aplicarFiltros());

        VBox busca = criarCampoFiltro("BUSCAR", buscaField);
        VBox.setVgrow(buscaField, Priority.ALWAYS);
        HBox.setHgrow(busca, Priority.ALWAYS);
        VBox criticidade = criarCampoFiltro("TIPO", criticidadeFiltro);
        VBox periodo = criarCampoFiltro("PERÍODO", periodoFiltro);
        VBox ano = criarCampoFiltro("ANO", anoFiltro);
        VBox ordem = criarCampoFiltro("ORDENAR", ordemFiltro);

        Button limpar = new Button("Limpar");
        limpar.setGraphic(AppIcon.create(AppIcon.Type.CLOSE, 15));
        limpar.getStyleClass().addAll(
                "secondary-button", "popup-filter-clear");
        limpar.setMinWidth(92);
        limpar.setPrefWidth(92);
        limpar.setOnAction(event -> limparFiltros());
        VBox acao = criarCampoFiltro("FILTROS", limpar);
        acao.setMinWidth(92);
        acao.setPrefWidth(92);

        HBox filtros = new HBox(
                12, busca, criticidade, periodo, ano, ordem, acao);
        filtros.setAlignment(Pos.BOTTOM_LEFT);
        filtros.getStyleClass().addAll(
                "app-card", "history-filter-bar", "popup-filter-bar");
        return filtros;
    }

    private VBox criarCampoFiltro(String titulo, Control controle) {
        Label label = new Label(titulo);
        label.getStyleClass().add("history-filter-label");
        controle.getStyleClass().add("history-filter-control");
        if (controle == buscaField) {
            controle.getStyleClass().add("history-search-field");
        }
        VBox campo = new VBox(5, label, controle);
        campo.getStyleClass().add("history-filter-field");
        return campo;
    }

    private void limparFiltros() {
        buscaField.clear();
        criticidadeFiltro.setValue("Todos os tipos");
        periodoFiltro.setValue("Todo o período");
        anoFiltro.setValue("Todos os anos");
        ordemFiltro.setValue("Mais recentes");
        aplicarFiltros();
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
                "0 popups ativos"
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
        popupsCarregados.clear();
        if (popups != null) {
            popups.stream()
                    .filter(Popup::isAtivo)
                    .forEach(popupsCarregados::add);
        }
        atualizarAnosDisponiveis();
        aplicarFiltros();
    }

    private void aplicarFiltros() {
        String busca = normalizar(buscaField.getText());
        String criticidade = criticidadeFiltro.getValue();

        List<Popup> filtrados = popupsCarregados.stream()
                .filter(popup -> busca.isBlank()
                        || normalizar(popup.getTitulo()).contains(busca)
                        || normalizar(popup.getMensagem()).contains(busca))
                .filter(popup -> "Todos os tipos".equals(criticidade)
                        || criticidade == null
                        || classificarTipo(popup).equals(criticidade))
                .filter(this::correspondePeriodo)
                .filter(this::correspondeAno)
                .toList();

        List<Popup> ordenados = new ArrayList<>(filtrados);
        boolean maisAntigos = "Mais antigos".equals(ordemFiltro.getValue());
        Comparator<LocalDate> comparadorData = maisAntigos
                ? Comparator.nullsLast(Comparator.naturalOrder())
                : Comparator.nullsLast(Comparator.reverseOrder());
        ordenados.sort(Comparator
                .comparing(
                        (Popup popup) -> dataPublicacao(popup),
                        comparadorData)
                .thenComparing(
                        (Popup popup) -> textoOuPadrao(popup.getId(), ""),
                        maisAntigos
                                ? Comparator.<String>naturalOrder()
                                : Comparator.<String>reverseOrder()));
        renderizarPopups(ordenados);
    }

    private boolean correspondePeriodo(Popup popup) {
        String filtro = periodoFiltro.getValue();
        if (filtro == null || "Todo o período".equals(filtro)) return true;
        LocalDate data = dataPublicacao(popup);
        if (data == null) return false;

        LocalDate hoje = LocalDate.now();
        return switch (filtro) {
            case "Hoje" -> data.equals(hoje);
            case "Últimos 7 dias" -> !data.isBefore(hoje.minusDays(6));
            case "Últimos 30 dias" -> !data.isBefore(hoje.minusDays(29));
            default -> true;
        };
    }

    private boolean correspondeAno(Popup popup) {
        String filtro = anoFiltro.getValue();
        if (filtro == null || "Todos os anos".equals(filtro)) return true;
        LocalDate data = dataPublicacao(popup);
        return data != null && data.getYear() == Integer.parseInt(filtro);
    }

    private void atualizarAnosDisponiveis() {
        String selecaoAtual = anoFiltro.getValue();
        List<String> opcoes = new ArrayList<>(List.of("Todos os anos"));
        TreeSet<Integer> anos = new TreeSet<>(Comparator.reverseOrder());
        anos.add(LocalDate.now().getYear());
        for (Popup popup : popupsCarregados) {
            LocalDate data = dataPublicacao(popup);
            if (data != null) anos.add(data.getYear());
        }
        for (Integer ano : anos) opcoes.add(String.valueOf(ano));
        anoFiltro.getItems().setAll(opcoes);
        anoFiltro.setValue(opcoes.contains(selecaoAtual)
                ? selecaoAtual : "Todos os anos");
        anoFiltro.setVisibleRowCount(Math.min(8, opcoes.size()));
    }

    private LocalDate dataPublicacao(Popup popup) {
        String valor = popup.getDataPublicacao();
        if (!temConteudo(valor) || valor.length() < 10) return null;
        try {
            return LocalDate.parse(valor.substring(0, 10));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String classificarTipo(Popup popup) {
        String criticidade = normalizar(popup.getCriticidade());
        String prioridade = normalizar(popup.getPrioridade());
        if (criticidade.contains("critical") || criticidade.contains("critic")
                || prioridade.contains("immediate") || prioridade.contains("imediat")) {
            return "Crítico";
        }
        if (prioridade.contains("urgent") || criticidade.equals("high")
                || criticidade.equals("alta")) {
            return "Urgente";
        }
        if (criticidade.contains("moderate") || criticidade.contains("moderad")
                || prioridade.equals("high") || prioridade.equals("alta")) {
            return "Atenção";
        }
        return "Normal";
    }

    private void renderizarPopups(List<Popup> popups) {
        listaPopups.getChildren().clear();
        int total = popupsCarregados.size();
        int exibidos = popups.size();

        quantidadeLabel.setText(total == exibidos
                ? total + (total == 1 ? " popup ativo" : " popups ativos")
                : "Exibindo " + exibidos + " de " + total);

        if (popups.isEmpty()) {
            statusLabel.setText(total == 0
                    ? "Nenhum popup ativo no momento."
                    : "Nenhum popup ativo corresponde aos filtros.");
            listaPopups.getChildren().add(criarEstadoVazio());
            return;
        }

        statusLabel.setText("Popups ativos na intranet");
        for (Popup popup : popups) {
            listaPopups.getChildren().add(criarCardPopup(popup));
        }
    }

    // ============================================================
    // CARD DO POPUP
    // ============================================================

    private Node criarCardPopup(
            Popup popup
    ) {
        String tema = temaPopup(popup);
        VBox card = new VBox();
        card.getStyleClass().addAll(
                "popup-card", "popup-library-card",
                "popup-library-theme-" + tema);

        Region barra = new Region();
        barra.getStyleClass().add("popup-library-topbar");

        StackPane icone = new StackPane(
                AppIcon.create(AppIcon.Type.BUILDING, 17));
        icone.getStyleClass().add("popup-library-brand-icon");

        Label titulo = new Label(textoOuPadrao(
                popup.getTitulo(), "Sem título"));
        titulo.setWrapText(true);
        titulo.setMaxWidth(Double.MAX_VALUE);
        titulo.getStyleClass().add("popup-library-title");

        Label classificacao = new Label(
                criticidadeExibida(popup) + " · " + prioridadeExibida(popup));
        classificacao.getStyleClass().add("popup-library-badge");

        VBox identificacao = new VBox(5, titulo, classificacao);
        identificacao.setMinWidth(0);
        HBox.setHgrow(identificacao, Priority.ALWAYS);

        Label anexo = criarIndicadorAnexo(popup.getImagem());
        Label status = criarStatus(popup.isAtivo());

        HBox cabecalho = new HBox(
                12, icone, identificacao, anexo, status);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.getStyleClass().add("popup-library-header");

        VBox conteudoMensagem = new VBox(9);
        conteudoMensagem.setMinWidth(0);
        HBox.setHgrow(conteudoMensagem, Priority.ALWAYS);

        Label mensagem = new Label(textoOuPadrao(
                popup.getMensagem(), "Sem mensagem informada."));
        mensagem.setWrapText(true);
        mensagem.setMaxWidth(Double.MAX_VALUE);
        mensagem.getStyleClass().add("popup-library-message");
        conteudoMensagem.getChildren().add(mensagem);

        if (temConteudo(popup.getImagem())) {
            if (PopupMediaView.ehImagem(
                    popup.getImagem(), popup.getImagemMimeType())) {
                conteudoMensagem.getChildren().add(PopupMediaView.criar(
                        popup.getImagem(), 360, 210, "popup-library-media"));
            } else {
                String tipo = tipoAnexo(popup.getImagem());
                Button abrirAnexo = new Button("Abrir " + tipo);
                abrirAnexo.getStyleClass().add("popup-library-link");
                abrirAnexo.setGraphic(AppIcon.create(AppIcon.Type.LINK, 14));
                abrirAnexo.setOnAction(
                        event -> abrirAnexo(popup.getImagem()));
                conteudoMensagem.getChildren().add(abrirAnexo);
            }
        }

        if (linkSeguro(popup.getLink())) {
            Hyperlink saibaMais = new Hyperlink("Saiba mais");
            saibaMais.setGraphic(
                    AppIcon.create(AppIcon.Type.EXTERNAL_LINK, 14));
            saibaMais.getStyleClass().add("popup-library-link");
            saibaMais.setOnAction(event -> abrirAnexo(popup.getLink()));
            conteudoMensagem.getChildren().add(saibaMais);
        }

        StackPane alertaIcone = new StackPane(AppIcon.create(
                "normal".equals(tema)
                        ? AppIcon.Type.INFO : AppIcon.Type.WARNING,
                18));
        alertaIcone.getStyleClass().add("popup-library-alert-icon");
        HBox mensagemBox = new HBox(
                12, alertaIcone, conteudoMensagem);
        mensagemBox.setAlignment(Pos.TOP_LEFT);
        mensagemBox.getStyleClass().add("popup-library-message-box");

        HBox datas = new HBox(9);
        if (temConteudo(popup.getDataExpiracao())) {
            Node expiracao = criarDataResumo(
                    AppIcon.Type.CLOCK, "EXPIRA",
                    formatarData(popup.getDataExpiracao()));
            HBox.setHgrow(expiracao, Priority.ALWAYS);
            datas.getChildren().add(expiracao);
        }
        datas.getStyleClass().add("popup-library-dates");

        VBox corpo = new VBox(10, mensagemBox);
        if (!datas.getChildren().isEmpty()) {
            corpo.getChildren().add(datas);
        }
        corpo.getStyleClass().add("popup-library-body");

        HBox acoes = criarAcoes(popup);
        acoes.getStyleClass().add("popup-library-actions");

        card.getChildren().addAll(barra, cabecalho, corpo, acoes);
        return card;
    }

    private Node criarDataResumo(
            AppIcon.Type tipo, String rotulo, String valor) {
        StackPane icone = new StackPane(AppIcon.create(tipo, 14));
        icone.getStyleClass().add("popup-library-date-icon");
        VBox textos = new VBox(1,
                criarTexto(rotulo, "popup-library-date-label"),
                criarTexto(valor, "popup-library-date-value"));
        HBox card = new HBox(8, icone, textos);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(Double.MAX_VALUE);
        card.getStyleClass().add("popup-library-date-card");
        return card;
    }

    private Label criarTexto(String texto, String estilo) {
        Label label = new Label(texto);
        label.getStyleClass().add(estilo);
        return label;
    }

    private String formatarData(String valor) {
        if (!temConteudo(valor)) return "Não informada";
        String data = valor.trim().replace("T", " ").replace("Z", "");
        if (data.length() >= 16
                && data.startsWith("-", 4)
                && data.startsWith("-", 7)) {
            return data.substring(8, 10) + "/" + data.substring(5, 7)
                    + "/" + data.substring(0, 4) + data.substring(10, 16);
        }
        return data;
    }

    private String temaPopup(Popup popup) {
        String criticidade = normalizar(popup.getCriticidade());
        if (criticidade.contains("critical") || criticidade.contains("critic")) {
            return "critical";
        }
        if (criticidade.equals("high") || criticidade.equals("alta")) {
            return "high";
        }
        if (criticidade.contains("moderate") || criticidade.contains("moderad")) {
            return "moderate";
        }
        if (criticidade.equals("low") || criticidade.equals("baixa")) {
            return "low";
        }
        return "normal";
    }

    private String criticidadeExibida(Popup popup) {
        return switch (temaPopup(popup)) {
            case "critical" -> "Crítica";
            case "high" -> "Alta";
            case "moderate" -> "Moderada";
            case "low" -> "Baixa";
            default -> "Informativa";
        };
    }

    private String prioridadeExibida(Popup popup) {
        String prioridade = normalizar(popup.getPrioridade());
        if (prioridade.contains("immediate") || prioridade.contains("imediat")) {
            return "Imediata";
        }
        if (prioridade.contains("urgent")) return "Urgente";
        if (prioridade.equals("high") || prioridade.equals("alta")) return "Alta";
        if (prioridade.equals("low") || prioridade.equals("baixa")) return "Baixa";
        return "Normal";
    }

    private String textoOuPadrao(String valor, String padrao) {
        return temConteudo(valor) ? valor.trim() : padrao;
    }

    private boolean temConteudo(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String normalizar(String valor) {
        if (valor == null) return "";
        return java.text.Normalizer.normalize(
                        valor, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private boolean linkSeguro(String endereco) {
        if (!temConteudo(endereco)) return false;
        try {
            String esquema = URI.create(endereco.trim()).getScheme();
            return "http".equalsIgnoreCase(esquema)
                    || "https".equalsIgnoreCase(esquema);
        } catch (Exception ignored) {
            return false;
        }
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
        indicador.setText(possuiAnexo ? "ANEXO" : "");
        if (possuiAnexo) indicador.setGraphic(AppIcon.create(AppIcon.Type.LINK, 15));
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

        statusButton.setGraphic(AppIcon.create(
                popup.isAtivo() ? AppIcon.Type.CLOSE : AppIcon.Type.CHECK, 15));
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

        excluirButton.setGraphic(AppIcon.create(AppIcon.Type.TRASH, 15));
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
        var owner = root.getScene() == null
                ? null : root.getScene().getWindow();
        if (AppDialog.confirmarExclusao(
                owner,
                "Excluir popup",
                "Excluir este popup?",
                "O aviso será removido definitivamente da Intranet.",
                "POPUP SELECIONADO",
                textoOuPadrao(popup.getTitulo(), "Sem título"),
                "Excluir popup")) {
            excluirPopup(popup);
        }
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

        icone.setText("");
        icone.setGraphic(AppIcon.create(AppIcon.Type.POPUP, 34));
        icone.getStyleClass().add(
                "empty-icon"
        );

        Label titulo = new Label(
                "Nenhum popup ativo"
        );

        titulo.getStyleClass().add(
                "empty-title"
        );

        Label descricao = new Label(
                "Os avisos publicados e ativos aparecerão aqui."
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
