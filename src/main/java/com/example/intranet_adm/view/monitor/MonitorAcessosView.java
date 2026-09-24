package com.example.intranet_adm.view.monitor;

import com.example.intranet_adm.model.EstatisticasAcesso;
import com.example.intranet_adm.service.DhcpHostnameResolver;
import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.view.components.AppIcon;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** Painel de presença em tempo real, alinhado ao tema visual da Central. */
public final class MonitorAcessosView {
    private static final Duration INTERVALO_ATUALIZACAO = Duration.seconds(30);
    private static final int LIMITE_PONTOS_GRAFICO = 12;
    private static final DateTimeFormatter HORARIO = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final IntranetAvisosClient client;
    private final DhcpHostnameResolver dhcpHostnameResolver =
            new DhcpHostnameResolver();
    private final BorderPane root = new BorderPane();
    private final AtomicBoolean carregando = new AtomicBoolean();
    private final Label onlineAgora = criarValorMetrica();
    private final Label acessosHoje = criarValorMetrica();
    private final Label totalVisitantes = criarValorMetrica();
    private final Label estadoTempoReal = new Label("● Em tempo real");
    private final Label atualizadoEm = new Label("Aguardando atualização");
    private final Label quantidadeAtivos = new Label("0 ativos");
    private final Button atualizarButton = new Button("↻");
    private final VBox linhasVisitantes = new VBox();
    private final ScrollPane visitantesScrollPane = new ScrollPane(linhasVisitantes);
    private final ComboBox<String> quantidadeVisitantesComboBox = new ComboBox<>();
    private List<String[]> visitantesCarregados = List.of();
    private final XYChart.Series<String, Number> serieAtividade = new XYChart.Series<>();
    private final Label estadoGrafico = new Label("Aguardando a primeira leitura de atividade...");

    private Timeline atualizacaoAutomatica;
    private PauseTransition segundaLeituraInicial;
    private volatile boolean encerrado;
    private Consumer<String> onMessage;

    public MonitorAcessosView(IntranetAvisosClient client) {
        if (client == null) throw new IllegalArgumentException("O cliente da Intranet não pode ser nulo.");
        this.client = client;
        construirInterface();
        iniciarAtualizacaoAutomatica();
        atualizar();
    }

    private void construirInterface() {
        VBox pagina = new VBox(16);
        pagina.setPadding(new Insets(0, 0, 20, 0));
        pagina.getStyleClass().add("monitor-page");
        pagina.getChildren().addAll(criarCabecalho(), criarResumo(), criarGrafico(), criarTabelaVisitantes());
        root.setCenter(pagina);
    }

    private Node criarCabecalho() {
        HBox breadcrumb = new HBox(7,
                new Label("Intranet"),
                new Label("/"),
                new Label("Administração"),
                new Label("/"),
                new Label("Acessando agora"));
        breadcrumb.getStyleClass().add("breadcrumb");
        Label titulo = new Label("Acessando agora");
        titulo.getStyleClass().add("central-page-title");
        estadoTempoReal.getStyleClass().add("monitor-live");
        atualizadoEm.getStyleClass().add("monitor-updated");
        atualizarButton.getStyleClass().add("monitor-refresh");
        atualizarButton.setTooltip(new Tooltip("Atualizar agora"));
        atualizarButton.setAccessibleText("Atualizar dados de acesso");
        atualizarButton.setText("");
        atualizarButton.setGraphic(AppIcon.create(AppIcon.Type.REFRESH, 19));
        atualizarButton.setOnAction(event -> atualizar());
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox linhaTitulo = new HBox(10, titulo, estadoTempoReal, espaco, atualizadoEm, atualizarButton);
        linhaTitulo.setAlignment(Pos.CENTER_LEFT);
        linhaTitulo.getStyleClass().add("monitor-heading-row");
        Label descricao = new Label("Acompanhe em tempo real os dispositivos conectados à Intranet.");
        descricao.getStyleClass().add("page-description");
        VBox cabecalho = new VBox(3, breadcrumb, linhaTitulo, descricao);
        cabecalho.getStyleClass().add("page-heading");
        return cabecalho;
    }

    private Node criarResumo() {
        HBox resumo = new HBox(14,
                criarCartaoMetrica("◎", onlineAgora, "Dispositivos online"),
                criarCartaoMetrica("▥", acessosHoje, "Acessos hoje"),
                criarCartaoMetrica("♙", totalVisitantes, "Acessos registrados")
        );
        resumo.getStyleClass().add("monitor-stats");
        return resumo;
    }

    private VBox criarCartaoMetrica(String icone, Label valor, String descricao) {
        Label iconeLabel = new Label(icone);
        iconeLabel.setText("");
        AppIcon.Type tipo = descricao.contains("online")
                ? AppIcon.Type.USERS
                : descricao.contains("hoje")
                ? AppIcon.Type.HISTORY : AppIcon.Type.USERS;
        iconeLabel.setGraphic(AppIcon.create(tipo, 21));
        iconeLabel.getStyleClass().add("monitor-icon-small");
        StackPane iconeBox = new StackPane(iconeLabel);
        iconeBox.getStyleClass().add("monitor-icon-box");
        Label descricaoLabel = new Label(descricao);
        descricaoLabel.getStyleClass().add("monitor-stat-label");
        VBox textos = new VBox(1, valor, descricaoLabel);
        textos.setAlignment(Pos.CENTER_LEFT);
        HBox conteudo = new HBox(14, iconeBox, textos);
        conteudo.setAlignment(Pos.CENTER_LEFT);
        VBox cartao = new VBox(conteudo);
        cartao.setAlignment(Pos.CENTER_LEFT);
        cartao.getStyleClass().addAll("monitor-card", "monitor-stat-card");
        HBox.setHgrow(cartao, Priority.ALWAYS);
        cartao.setMaxWidth(Double.MAX_VALUE);
        return cartao;
    }

    private Node criarGrafico() {
        Label titulo = new Label("Atividade recente");
        titulo.getStyleClass().add("card-title");
        Label legenda = new Label("●  Dispositivos online");
        legenda.getStyleClass().add("monitor-chart-legend");
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox cabecalho = new HBox(8, titulo, espaco, legenda);
        cabecalho.setAlignment(Pos.CENTER_LEFT);

        CategoryAxis eixoTempo = new CategoryAxis();
        eixoTempo.setAnimated(false);
        NumberAxis eixoPessoas = new NumberAxis();
        eixoPessoas.setForceZeroInRange(true);
        eixoPessoas.setMinorTickVisible(false);
        eixoPessoas.setAnimated(false);
        LineChart<String, Number> grafico = new LineChart<>(eixoTempo, eixoPessoas);
        grafico.setLegendVisible(false);
        grafico.setAnimated(false);
        grafico.setCreateSymbols(true);
        grafico.setAlternativeColumnFillVisible(false);
        grafico.setAlternativeRowFillVisible(false);
        grafico.setHorizontalZeroLineVisible(false);
        grafico.setMinHeight(180);
        grafico.setPrefHeight(210);
        grafico.getStyleClass().add("monitor-chart");
        grafico.getData().add(serieAtividade);

        estadoGrafico.setWrapText(true);
        estadoGrafico.setMaxWidth(Double.MAX_VALUE);
        estadoGrafico.setAlignment(Pos.CENTER);
        estadoGrafico.getStyleClass().add("monitor-chart-empty");
        grafico.visibleProperty().bind(estadoGrafico.visibleProperty().not());
        StackPane areaGrafico = new StackPane(grafico, estadoGrafico);
        areaGrafico.setMinHeight(180);
        VBox cartao = new VBox(8, cabecalho, areaGrafico);
        cartao.getStyleClass().addAll("monitor-card", "monitor-chart-card");
        return cartao;
    }

    private Node criarTabelaVisitantes() {
        quantidadeVisitantesComboBox.getItems().addAll(
                "10", "50", "100", "Todos");
        quantidadeVisitantesComboBox.setValue("10");
        quantidadeVisitantesComboBox.setPrefWidth(100);
        quantidadeVisitantesComboBox.getStyleClass().add("list-limit-selector");
        quantidadeVisitantesComboBox.setOnAction(
                event -> preencherVisitantes(visitantesCarregados));
        Label exibirLabel = new Label("Exibir");
        exibirLabel.getStyleClass().add("list-limit-label");
        Label titulo = new Label("Dispositivos conectados");
        titulo.getStyleClass().add("card-title");
        quantidadeAtivos.getStyleClass().add("monitor-active-count");
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox cabecalho = new HBox(
                8, titulo, quantidadeAtivos, espaco, exibirLabel, quantidadeVisitantesComboBox);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        HBox colunas = criarLinhaTabela(
                new String[]{"Dispositivo", "Endereço IP", "Última atividade",
                        "Tempo conectado", "Status"}, "monitor-table-header");
        linhasVisitantes.getStyleClass().add("monitor-table-body");
        linhasVisitantes.setMaxWidth(Double.MAX_VALUE);
        visitantesScrollPane.setFitToWidth(true);
        visitantesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        visitantesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        visitantesScrollPane.setPannable(true);
        visitantesScrollPane.getStyleClass().add("monitor-users-scroll");
        preencherVisitantes(List.of());
        VBox cartao = new VBox(10, cabecalho, colunas, visitantesScrollPane);
        cartao.getStyleClass().addAll("monitor-card", "monitor-table-card");
        return cartao;
    }

    private HBox criarLinhaTabela(String[] valores, String classe) {
        HBox linha = new HBox();
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.getStyleClass().add(classe);
        for (int indice = 0; indice < valores.length; indice++) {
            String texto = indice < valores.length && valores[indice] != null && !valores[indice].isBlank()
                    ? valores[indice] : "—";
            Label celula = new Label(texto);
            celula.setMaxWidth(Double.MAX_VALUE);
            celula.setEllipsisString("…");
            celula.getStyleClass().add(
                    "monitor-table-header".equals(classe) ? "table-head" : "table-cell");
            if (indice == 0 && !"monitor-table-header".equals(classe)) {
                celula.getStyleClass().add("monitor-user-cell");
            }
            HBox.setHgrow(celula, Priority.ALWAYS);
            celula.prefWidthProperty().bind(linha.widthProperty().divide(
                    Math.max(1, valores.length)));
            linha.getChildren().add(celula);
        }
        return linha;
    }

    private void preencherVisitantes(List<String[]> pessoas) {
        pessoas = pessoas == null ? List.of() : pessoas;
        visitantesCarregados = List.copyOf(pessoas);
        linhasVisitantes.getChildren().clear();
        quantidadeAtivos.setText(pessoas.size() == 1 ? "1 ativo" : pessoas.size() + " ativos");
        int quantidade = Math.min(obterLimiteVisitantes(), pessoas.size());
        quantidadeAtivos.setText("Exibindo " + quantidade
                + " de " + pessoas.size());
        if (pessoas.isEmpty()) {
            Label vazio = new Label("Nenhum dispositivo conectado no momento.");
            vazio.getStyleClass().add("monitor-table-empty");
            vazio.setMaxWidth(Double.MAX_VALUE);
            vazio.setAlignment(Pos.CENTER);
            linhasVisitantes.getChildren().add(vazio);
            ajustarAlturaListaVisitantes(1);
            return;
        }

        for (int indice = 0; indice < quantidade; indice++) {
            String[] pessoa = pessoas.get(indice);
            String maquina = valorDispositivo(valor(pessoa, 6));
            String ip = valorIp(valor(pessoa, 5));
            String navegador = valor(pessoa, 4);
            HBox linha = criarLinhaDispositivo(
                    maquina,
                    ip,
                    formatarUltimaAtividade(valor(pessoa, 7)),
                    formatarTempo(valor(pessoa, 3)));
            if (!"—".equals(navegador)) {
                Tooltip.install(linha, new Tooltip("Navegador: " + navegador));
            }
            linha.setOnMouseClicked(event ->
                    notificar("Dispositivo selecionado: " + maquina + " (" + ip + ")"));
            if (indice % 2 == 1) {
                linha.getStyleClass().add("monitor-table-row-alt");
            }
            linhasVisitantes.getChildren().add(linha);
        }
        ajustarAlturaListaVisitantes(quantidade);
    }

    private void ajustarAlturaListaVisitantes(int quantidadeExibida) {
        int linhasVisiveis = Math.min(Math.max(quantidadeExibida, 1), 9);
        double altura = Math.max(70, linhasVisiveis * 49.0);
        visitantesScrollPane.setMinHeight(altura);
        visitantesScrollPane.setPrefHeight(altura);
        visitantesScrollPane.setMaxHeight(altura);
        visitantesScrollPane.setVvalue(0);
    }

    private HBox criarLinhaDispositivo(
            String maquina,
            String ip,
            String ultimaAtividade,
            String tempoConectado) {
        HBox linha = new HBox();
        linha.setAlignment(Pos.CENTER_LEFT);
        linha.getStyleClass().add("monitor-table-row");

        StackPane glifo = AppIcon.create(AppIcon.Type.SERVER, 16);
        StackPane icone = new StackPane(glifo);
        icone.getStyleClass().add("monitor-device-icon");
        Label nomeMaquina = new Label(maquina);
        nomeMaquina.setMaxWidth(Double.MAX_VALUE);
        nomeMaquina.setEllipsisString("…");
        nomeMaquina.getStyleClass().addAll("table-cell", "monitor-device-name");
        HBox dispositivo = new HBox(9, icone, nomeMaquina);
        dispositivo.setAlignment(Pos.CENTER_LEFT);
        dispositivo.getStyleClass().add("monitor-device-cell");

        Label enderecoIp = criarCelulaTabela(ip);
        enderecoIp.getStyleClass().add("monitor-ip-cell");
        Label atividade = criarCelulaTabela(ultimaAtividade);
        Label tempo = criarCelulaTabela(tempoConectado);

        Region ponto = new Region();
        ponto.getStyleClass().add("monitor-status-dot");
        Label textoStatus = new Label("Ativo agora");
        textoStatus.getStyleClass().add("monitor-status-text");
        HBox seloStatus = new HBox(6, ponto, textoStatus);
        seloStatus.setAlignment(Pos.CENTER_LEFT);
        seloStatus.getStyleClass().add("monitor-status-badge");
        HBox status = new HBox(seloStatus);
        status.setAlignment(Pos.CENTER_LEFT);

        List<Region> celulas = List.of(dispositivo, enderecoIp, atividade, tempo, status);
        for (Region celula : celulas) {
            celula.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(celula, Priority.ALWAYS);
            celula.prefWidthProperty().bind(linha.widthProperty().divide(celulas.size()));
        }
        linha.getChildren().addAll(celulas);
        return linha;
    }

    private Label criarCelulaTabela(String texto) {
        Label celula = new Label(texto);
        celula.setMaxWidth(Double.MAX_VALUE);
        celula.setEllipsisString("…");
        celula.getStyleClass().add("table-cell");
        return celula;
    }

    private String valorDispositivo(String valor) {
        return valorIndisponivel(valor) ? "Máquina não identificada" : valor;
    }

    private String valorIp(String valor) {
        return valorIndisponivel(valor) ? "IP não informado" : valor;
    }

    private boolean valorIndisponivel(String valor) {
        return valor == null || valor.isBlank() || "—".equals(valor)
                || "null".equalsIgnoreCase(valor)
                || "Não informado".equalsIgnoreCase(valor);
    }

    private int obterLimiteVisitantes() {
        String valorSelecionado = quantidadeVisitantesComboBox.getValue();
        if (valorSelecionado == null || "Todos".equals(valorSelecionado)) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(valorSelecionado);
        } catch (NumberFormatException ignored) {
            return 10;
        }
    }

    private String formatarUltimaAtividade(String valor) {
        if (valor == null || valor.isBlank() || "—".equals(valor)) return "—";
        try {
            return DATA_HORA.format(
                    Instant.parse(valor).atZone(ZoneId.systemDefault()));
        } catch (Exception ignored) {
            return valor;
        }
    }

    private String formatarTempo(String tempo) {
        if (tempo == null || tempo.isBlank() || "—".equals(tempo)) return "—";
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(\\d+)\\s*min", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(tempo.trim());
        if (!matcher.find()) return tempo;

        long totalMinutos = Long.parseLong(matcher.group(1));
        long dias = totalMinutos / (24 * 60);
        long horas = (totalMinutos % (24 * 60)) / 60;
        long minutos = totalMinutos % 60;

        if (dias > 0) return dias + " d " + horas + " h "
                + minutos + " min";
        if (horas > 0) return horas + " h " + minutos + " min";
        return minutos + " min";
    }

    private String valor(String[] valores, int indice) {
        if (valores == null || indice >= valores.length || valores[indice] == null || valores[indice].isBlank()) {
            return "—";
        }
        return valores[indice];
    }

    private Label criarValorMetrica() {
        Label valor = new Label("—");
        valor.getStyleClass().add("monitor-value");
        return valor;
    }

    private void iniciarAtualizacaoAutomatica() {
        atualizacaoAutomatica = new Timeline(
                new KeyFrame(INTERVALO_ATUALIZACAO, event -> atualizar()));
        atualizacaoAutomatica.setCycleCount(Animation.INDEFINITE);
        atualizacaoAutomatica.play();
    }

    public void atualizar() {
        if (encerrado || !carregando.compareAndSet(false, true)) return;
        definirCarregando(true);

        Thread worker = new Thread(() -> {
            try {
                EstatisticasAcesso estatisticas = client.buscarEstatisticasAcesso();
                List<String[]> visitantes = client.buscarVisitantes();
                Platform.runLater(() -> aplicarDados(estatisticas, visitantes));
                List<String[]> visitantesComMaquina = visitantes.stream()
                        .map(item -> Arrays.copyOf(item, item.length))
                        .toList();
                if (dhcpHostnameResolver.preencherNomes(visitantesComMaquina)) {
                    Platform.runLater(() -> {
                        if (!encerrado) preencherVisitantes(visitantesComMaquina);
                    });
                }
            } catch (Exception error) {
                Platform.runLater(() -> exibirFalha(error));
            } finally {
                carregando.set(false);
                Platform.runLater(() -> definirCarregando(false));
            }
        }, "monitor-acessos");
        worker.setDaemon(true);
        worker.start();
    }

    private void aplicarDados(EstatisticasAcesso estatisticas, List<String[]> visitantes) {
        if (encerrado) return;
        onlineAgora.setText(String.valueOf(estatisticas.getOnlineAgora()));
        acessosHoje.setText(String.valueOf(estatisticas.getAcessosHoje()));
        totalVisitantes.setText(String.valueOf(estatisticas.getTotalVisitantes()));
        preencherVisitantes(visitantes);
        adicionarPontoGrafico(estatisticas.getOnlineAgora());
        agendarSegundaLeituraInicial();
        estadoTempoReal.setText("● Em tempo real");
        estadoTempoReal.getStyleClass().remove("monitor-live-offline");
        atualizadoEm.setText("Atualizado às " + LocalTime.now().format(HORARIO));

        String ultimaConexao = estatisticas.getUltimaConexao();
        if (ultimaConexao != null && !ultimaConexao.isBlank()) {
            atualizadoEm.setTooltip(new Tooltip("Última conexão registrada: " + ultimaConexao));
        } else {
            atualizadoEm.setTooltip(null);
        }
        notificar("Dados de acesso atualizados.");
    }

    private void adicionarPontoGrafico(int quantidadeOnline) {
        serieAtividade.getData().add(
                new XYChart.Data<>(LocalTime.now().format(HORARIO), quantidadeOnline));
        if (serieAtividade.getData().size() == 1) {
            estadoGrafico.setText("" + quantidadeOnline
                    + " dispositivos online agora. Coletando a próxima leitura para mostrar a evolução.");
        }
        estadoGrafico.setVisible(serieAtividade.getData().size() < 2);
        if (serieAtividade.getData().size() > LIMITE_PONTOS_GRAFICO) {
            serieAtividade.getData().remove(0);
        }
    }

    private void agendarSegundaLeituraInicial() {
        if (serieAtividade.getData().size() != 1 || segundaLeituraInicial != null) return;
        segundaLeituraInicial = new PauseTransition(Duration.seconds(2));
        segundaLeituraInicial.setOnFinished(event -> {
            if (!encerrado && serieAtividade.getData().size() < 2) atualizar();
        });
        segundaLeituraInicial.play();
    }

    private void exibirFalha(Exception error) {
        if (encerrado) return;
        estadoTempoReal.setText("● Sem conexão");
        if (!estadoTempoReal.getStyleClass().contains("monitor-live-offline")) {
            estadoTempoReal.getStyleClass().add("monitor-live-offline");
        }
        atualizadoEm.setText("Não foi possível atualizar");
        atualizadoEm.setTooltip(new Tooltip(
                error.getMessage() == null ? "Falha de comunicação com o IDAM." : error.getMessage()));
        notificar("Não foi possível atualizar os acessos.");
    }

    private void definirCarregando(boolean valor) {
        if (encerrado) return;
        atualizarButton.setDisable(valor);
        if (valor) atualizadoEm.setText("Atualizando…");
    }

    private void notificar(String mensagem) {
        if (onMessage != null) onMessage.accept(mensagem);
    }

    public void pausarAtualizacaoAutomatica() {
        if (encerrado) return;
        if (atualizacaoAutomatica != null) atualizacaoAutomatica.pause();
        if (segundaLeituraInicial != null) segundaLeituraInicial.pause();
    }

    public void retomarAtualizacaoAutomatica() {
        if (encerrado) return;
        if (atualizacaoAutomatica != null) atualizacaoAutomatica.play();
        if (segundaLeituraInicial != null
                && serieAtividade.getData().size() < 2) {
            segundaLeituraInicial.play();
        }
        atualizar();
    }

    public void pararAtualizacaoAutomatica() {
        encerrado = true;
        if (atualizacaoAutomatica != null) atualizacaoAutomatica.stop();
        if (segundaLeituraInicial != null) segundaLeituraInicial.stop();
    }

    public void setOnMessage(Consumer<String> onMessage) { this.onMessage = onMessage; }
    public Node getView() { return root; }
    public BorderPane getRoot() { return root; }
}
