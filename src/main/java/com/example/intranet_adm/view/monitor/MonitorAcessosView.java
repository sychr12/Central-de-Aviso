package com.example.intranet_adm.view.monitor;

import com.example.intranet_adm.model.EstatisticasAcesso;
import com.example.intranet_adm.service.IntranetAvisosClient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.function.Consumer;

public class MonitorAcessosView {

    private final IntranetAvisosClient client;

    private final BorderPane root = new BorderPane();

    private final Label onlineAgoraLabel = new Label("0");
    private final Label acessosHojeLabel = new Label("0");
    private final Label totalVisitantesLabel = new Label("0");
    private final Label ultimaConexaoLabel = new Label("—");

    private final Label statusLabel = new Label();

    private Timeline atualizacaoAutomatica;

    private Consumer<String> onMessage;

    public MonitorAcessosView(
            IntranetAvisosClient client
    ) {

        this.client = client;

        construir();
        iniciarAtualizacaoAutomatica();
        atualizar();
    }

    // ============================================================
    // CONSTRUÇÃO
    // ============================================================

    private void construir() {

        root.setPadding(new Insets(0));

        VBox conteudo = new VBox(20);

        conteudo.setFillWidth(true);

        Label titulo = criarTitulo();

        Label descricao = new Label(
                "Acompanhe os acessos e visitantes da Intranet em tempo real."
        );

        descricao.setFont(
                Font.font(
                        "System",
                        14
                )
        );

        descricao.setTextFill(
                Color.web("#94A3B8")
        );

        HBox cabecalho = criarCabecalho();

        HBox estatisticas = criarEstatisticas();

        VBox conexao = criarCardConexao();

        conteudo.getChildren().addAll(
                titulo,
                descricao,
                cabecalho,
                estatisticas,
                conexao
        );

        VBox.setVgrow(
                conexao,
                Priority.NEVER
        );

        root.setCenter(
                conteudo
        );
    }

    // ============================================================
    // TÍTULO
    // ============================================================

    private Label criarTitulo() {

        Label titulo = new Label(
                "Monitoramento de Acessos"
        );

        titulo.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        28
                )
        );

        titulo.setTextFill(
                Color.web("#F1F5F9")
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

        statusLabel.setText(
                "Atualizando..."
        );

        statusLabel.setFont(
                Font.font(
                        "System",
                        13
                )
        );

        statusLabel.setTextFill(
                Color.web("#94A3B8")
        );

        Region espacador = new Region();

        HBox.setHgrow(
                espacador,
                Priority.ALWAYS
        );

        Label atualizacao = new Label(
                "Atualização automática"
        );

        atualizacao.setFont(
                Font.font(
                        "System",
                        12
                )
        );

        atualizacao.setTextFill(
                Color.web("#9CA3AF")
        );

        cabecalho.getChildren().addAll(
                statusLabel,
                espacador,
                atualizacao
        );

        return cabecalho;
    }

    // ============================================================
    // ESTATÍSTICAS
    // ============================================================

    private HBox criarEstatisticas() {

        HBox container = new HBox(15);

        container.setFillHeight(true);

        VBox onlineCard = criarCardEstatistica(
                "Online agora",
                onlineAgoraLabel,
                "Usuários conectados"
        );

        VBox hojeCard = criarCardEstatistica(
                "Acessos hoje",
                acessosHojeLabel,
                "Acessos registrados"
        );

        VBox totalCard = criarCardEstatistica(
                "Total de visitantes",
                totalVisitantesLabel,
                "Visitantes registrados"
        );

        HBox.setHgrow(
                onlineCard,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                hojeCard,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                totalCard,
                Priority.ALWAYS
        );

        container.getChildren().addAll(
                onlineCard,
                hojeCard,
                totalCard
        );

        return container;
    }

    // ============================================================
    // CARD DE ESTATÍSTICA
    // ============================================================

    private VBox criarCardEstatistica(
            String titulo,
            Label valor,
            String descricao
    ) {

        VBox card = new VBox(8);

        card.setPadding(
                new Insets(20)
        );

        card.setMinHeight(130);

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.getStyleClass().add("app-card");

        Label tituloLabel = new Label(
                titulo
        );

        tituloLabel.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        14
                )
        );

        tituloLabel.setTextFill(
                Color.web("#CBD5E1")
        );

        valor.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        30
                )
        );

        valor.setTextFill(
                Color.web("#F1F5F9")
        );

        Label descricaoLabel = new Label(
                descricao
        );

        descricaoLabel.setFont(
                Font.font(
                        "System",
                        12
                )
        );

        descricaoLabel.setTextFill(
                Color.web("#9CA3AF")
        );

        card.getChildren().addAll(
                tituloLabel,
                valor,
                descricaoLabel
        );

        return card;
    }

    // ============================================================
    // CARD DE CONEXÃO
    // ============================================================

    private VBox criarCardConexao() {

        VBox card = new VBox(15);

        card.setPadding(
                new Insets(20)
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );

        card.getStyleClass().add("app-card");

        Label titulo = new Label(
                "Última atividade"
        );

        titulo.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        17
                )
        );

        titulo.setTextFill(
                Color.web("#F1F5F9")
        );

        HBox linha = new HBox(10);

        linha.setAlignment(
                Pos.CENTER_LEFT
        );

        Label descricao = new Label(
                "Última conexão registrada:"
        );

        descricao.setFont(
                Font.font(
                        "System",
                        13
                )
        );

        descricao.setTextFill(
                Color.web("#94A3B8")
        );

        ultimaConexaoLabel.setFont(
                Font.font(
                        "System",
                        FontWeight.BOLD,
                        13
                )
        );

        ultimaConexaoLabel.setTextFill(
                Color.web("#CBD5E1")
        );

        linha.getChildren().addAll(
                descricao,
                ultimaConexaoLabel
        );

        card.getChildren().addAll(
                titulo,
                linha
        );

        return card;
    }

    // ============================================================
    // ATUALIZAR DADOS
    // ============================================================

    public void atualizar() {

        statusLabel.setText(
                "Atualizando dados..."
        );

        statusLabel.setTextFill(
                Color.web("#94A3B8")
        );

        Thread thread = new Thread(
                () -> {

                    try {

                        EstatisticasAcesso dados =
                                client.buscarEstatisticasAcesso();

                        javafx.application.Platform.runLater(
                                () -> aplicarDados(dados)
                        );

                    } catch (Exception error) {

                        javafx.application.Platform.runLater(
                                () -> mostrarErro(error)
                        );
                    }
                }
        );

        thread.setDaemon(true);
        thread.start();
    }

    // ============================================================
    // APLICAR DADOS
    // ============================================================

    private void aplicarDados(
            EstatisticasAcesso dados
    ) {

        onlineAgoraLabel.setText(
                String.valueOf(
                        dados.getOnlineAgora()
                )
        );

        acessosHojeLabel.setText(
                String.valueOf(
                        dados.getAcessosHoje()
                )
        );

        totalVisitantesLabel.setText(
                String.valueOf(
                        dados.getTotalVisitantes()
                )
        );

        String ultimaConexao =
                dados.getUltimaConexao();

        if (ultimaConexao == null
                || ultimaConexao.isBlank()) {

            ultimaConexaoLabel.setText(
                    "Nenhuma informação"
            );

        } else {

            ultimaConexaoLabel.setText(
                    ultimaConexao
            );
        }

        statusLabel.setText(
                "Dados atualizados"
        );

        statusLabel.setTextFill(
                Color.web("#16A34A")
        );
    }

    // ============================================================
    // ERRO
    // ============================================================

    private void mostrarErro(
            Exception error
    ) {

        statusLabel.setText(
                "Não foi possível atualizar os dados."
        );

        statusLabel.setTextFill(
                Color.web("#DC2626")
        );

        enviarMensagem(
                "Erro ao atualizar monitoramento: "
                        + error.getMessage()
        );
    }

    // ============================================================
    // ATUALIZAÇÃO AUTOMÁTICA
    // ============================================================

    private void iniciarAtualizacaoAutomatica() {

        atualizacaoAutomatica =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(30),
                                event -> atualizar()
                        )
                );

        atualizacaoAutomatica.setCycleCount(
                Timeline.INDEFINITE
        );

        atualizacaoAutomatica.play();
    }

    // ============================================================
    // PARAR ATUALIZAÇÃO
    // ============================================================

    public void pararAtualizacaoAutomatica() {

        if (atualizacaoAutomatica != null) {

            atualizacaoAutomatica.stop();
        }
    }

    // ============================================================
    // MENSAGEM
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

            onMessage.accept(
                    mensagem
            );
        }
    }

    // ============================================================
    // VIEW
    // ============================================================

    public Node getView() {
        return root;
    }

    public BorderPane getRoot() {
        return root;
    }
}
