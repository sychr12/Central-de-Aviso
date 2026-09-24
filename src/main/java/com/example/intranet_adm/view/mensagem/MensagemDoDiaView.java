package com.example.intranet_adm.view.mensagem;

import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.view.components.AppIcon;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Tela "Mensagem do Dia".
 *
 * Segue o mesmo padrão visual das demais telas da Central de Avisos
 * (cards brancos com borda arredondada, mesma paleta de cores e
 * tipografia). Esta classe estava sendo referenciada em
 * CentralAvisosView, mas não existia no projeto, o que quebrava a
 * compilação.
 */
public class MensagemDoDiaView {

    private final IntranetAvisosClient client;

    private final VBox root = new VBox();

    private final TextArea mensagemArea = new TextArea();
    private final Label editorTitulo = new Label("Nova mensagem");
    private final Label editorDescricao = new Label(
            "Crie uma mensagem de destaque para os colaboradores.");
    private final Label modoEdicaoLabel = new Label("Modo de edição");
    private final Label contadorCaracteres = new Label("0 caracteres");
    private final Button salvarButton = new Button("Salvar mensagem");
    private final Button cancelarEdicaoButton = new Button("Cancelar edição");

    private final Label statusLabel = new Label();
    private final ListView<String> listaMensagens = new ListView<>();
    private final ComboBox<String> quantidadeComboBox = new ComboBox<>();
    private final Label resumoLista = new Label();
    private final Label totalMensagensLabel = new Label("—");
    private final AtomicBoolean carregandoLista = new AtomicBoolean();
    private final List<String> mensagensCarregadas = new ArrayList<>();
    private String mensagemEmEdicao;

    public MensagemDoDiaView(IntranetAvisosClient client) {
        this.client = client;

        construir();
    }

    // ============================================================
    // CRIAÇÃO ESTÁTICA (compatível com o restante da navegação)
    // ============================================================

    public static Node criar(Stage stage) {
        MensagemDoDiaView view =
                new MensagemDoDiaView(new IntranetAvisosClient());

        return view.getRoot();
    }

    // ============================================================
    // CONSTRUÇÃO DA TELA
    // ============================================================

    private void construir() {
        root.setSpacing(18);
        root.setPadding(new Insets(0));
        root.setFillWidth(true);
        root.getStyleClass().add("message-page");

        Label heroEyebrow = new Label("BIBLIOTECA EDITORIAL");
        heroEyebrow.getStyleClass().add("message-hero-eyebrow");
        Label heroTitulo = new Label("Palavras que movimentam o dia.");
        heroTitulo.getStyleClass().add("message-hero-title");
        Label heroDescricao = new Label(
                "Crie mensagens curtas, organize o acervo e mantenha a comunicação interna sempre inspiradora.");
        heroDescricao.setWrapText(true);
        heroDescricao.getStyleClass().add("message-hero-description");
        VBox heroTextos = new VBox(5, heroEyebrow, heroTitulo, heroDescricao);
        heroTextos.setMaxWidth(650);
        HBox.setHgrow(heroTextos, Priority.ALWAYS);

        StackPane heroIcone = new StackPane(AppIcon.create(AppIcon.Type.MESSAGE, 25));
        heroIcone.getStyleClass().add("message-hero-icon");
        totalMensagensLabel.getStyleClass().add("message-hero-metric-value");
        VBox metrica = criarMetrica(totalMensagensLabel, "NO ACERVO");
        Region espacoHero = new Region();
        HBox.setHgrow(espacoHero, Priority.ALWAYS);
        HBox hero = new HBox(18, heroIcone, heroTextos, espacoHero, metrica);
        hero.setAlignment(Pos.CENTER_LEFT);
        hero.getStyleClass().add("message-hero");

        editorTitulo.getStyleClass().add("message-editor-title");
        editorDescricao.setWrapText(true);
        editorDescricao.getStyleClass().add("message-editor-description");
        modoEdicaoLabel.getStyleClass().add("message-edit-mode");
        modoEdicaoLabel.setVisible(false);
        modoEdicaoLabel.managedProperty().bind(modoEdicaoLabel.visibleProperty());

        StackPane editorIcone = new StackPane(
                AppIcon.create(AppIcon.Type.MESSAGE, 20));
        editorIcone.getStyleClass().add("message-editor-icon");
        VBox textosCabecalho = new VBox(3, editorTitulo, editorDescricao);
        Region espacoCabecalho = new Region();
        HBox.setHgrow(espacoCabecalho, Priority.ALWAYS);
        HBox editorCabecalho = new HBox(
                12, editorIcone, textosCabecalho, espacoCabecalho, modoEdicaoLabel);
        editorCabecalho.setAlignment(Pos.CENTER_LEFT);
        editorCabecalho.getStyleClass().add("message-editor-header");

        Label mensagemLabel = new Label("Mensagem");
        mensagemLabel.getStyleClass().add("message-field-label");
        contadorCaracteres.getStyleClass().add("message-character-count");
        Region espacoCampo = new Region();
        HBox.setHgrow(espacoCampo, Priority.ALWAYS);
        HBox campoCabecalho = new HBox(
                8, mensagemLabel, espacoCampo, contadorCaracteres);
        campoCabecalho.setAlignment(Pos.CENTER_LEFT);

        mensagemArea.setPromptText("Digite a mensagem do dia");
        mensagemArea.setWrapText(true);
        mensagemArea.setPrefRowCount(5);
        mensagemArea.setMinHeight(132);
        mensagemArea.getStyleClass().add("form-field");
        mensagemArea.textProperty().addListener((observavel, anterior, atual) -> {
            int quantidade = atual == null ? 0 : atual.length();
            contadorCaracteres.setText(
                    quantidade == 1 ? "1 caractere" : quantidade + " caracteres");
        });

        salvarButton.setPrefHeight(40);
        salvarButton.getStyleClass().add("primary-button");
        salvarButton.setGraphic(AppIcon.create(AppIcon.Type.SAVE, 16));
        salvarButton.setOnAction(event -> salvar());

        cancelarEdicaoButton.setPrefHeight(40);
        cancelarEdicaoButton.getStyleClass().add("secondary-button");
        cancelarEdicaoButton.setGraphic(AppIcon.create(AppIcon.Type.CLOSE, 16));
        cancelarEdicaoButton.setOnAction(event -> cancelarEdicao());
        cancelarEdicaoButton.setVisible(false);
        cancelarEdicaoButton.managedProperty().bind(
                cancelarEdicaoButton.visibleProperty());

        Region espacoAcoes = new Region();
        HBox.setHgrow(espacoAcoes, Priority.ALWAYS);
        HBox botoes = new HBox(
                10, espacoAcoes, cancelarEdicaoButton, salvarButton);
        botoes.setAlignment(Pos.CENTER_LEFT);
        botoes.getStyleClass().add("message-editor-actions");

        statusLabel.setText("Nenhuma alteração salva ainda.");
        statusLabel.setTextFill(Color.web("#64748B"));
        statusLabel.getStyleClass().add("message-editor-status");

        VBox campoMensagem = new VBox(8, campoCabecalho, mensagemArea);

        Label bibliotecaTitulo = new Label("Mensagens salvas");
        bibliotecaTitulo.getStyleClass().add("card-title");
        Label bibliotecaDescricao = new Label(
                "Edite, reutilize ou remova as mensagens disponíveis.");
        bibliotecaDescricao.getStyleClass().add("message-library-description");
        VBox bibliotecaTitulos = new VBox(2, bibliotecaTitulo, bibliotecaDescricao);
        bibliotecaTitulos.getStyleClass().add("message-list-title-block");
        StackPane bibliotecaIcone = new StackPane(
                AppIcon.create(AppIcon.Type.MESSAGE, 17));
        bibliotecaIcone.getStyleClass().add("message-library-icon");
        HBox bibliotecaIdentidade = new HBox(11, bibliotecaIcone, bibliotecaTitulos);
        bibliotecaIdentidade.setAlignment(Pos.CENTER_LEFT);

        VBox editor = new VBox(
                15, editorCabecalho, campoMensagem, botoes, statusLabel);
        editor.getStyleClass().addAll("app-card", "message-editor-panel");
        editor.setMaxWidth(Double.MAX_VALUE);

        quantidadeComboBox.getItems().addAll(
                "10", "50", "100", "Todas");
        quantidadeComboBox.setValue("10");
        quantidadeComboBox.setPrefWidth(105);
        quantidadeComboBox.getStyleClass().add("message-limit-selector");
        quantidadeComboBox.setOnAction(event -> aplicarLimiteSelecionado());
        resumoLista.getStyleClass().add("message-list-summary");

        Label exibirLabel = new Label("Exibir");
        exibirLabel.getStyleClass().add("message-limit-label");
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox bibliotecaCabecalho = new HBox(
                12, bibliotecaIdentidade, resumoLista, espaco, exibirLabel, quantidadeComboBox);
        bibliotecaCabecalho.setAlignment(Pos.CENTER_LEFT);
        bibliotecaCabecalho.getStyleClass().add("message-list-header");

        configurarListaMensagens();
        VBox biblioteca = new VBox(10, bibliotecaCabecalho, listaMensagens);
        biblioteca.getStyleClass().addAll("app-card", "message-library-panel");
        biblioteca.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().addAll(hero, editor, biblioteca);
        carregarLista();
    }

    private VBox criarMetrica(Label valor, String legenda) {
        Label rotulo = new Label(legenda);
        rotulo.getStyleClass().add("message-hero-metric-label");
        VBox caixa = new VBox(1, valor, rotulo);
        caixa.setAlignment(Pos.CENTER);
        caixa.getStyleClass().add("message-hero-metric");
        return caixa;
    }

    // ============================================================
    // LISTA VIRTUALIZADA
    // ============================================================

    private void configurarListaMensagens() {
        listaMensagens.setMinHeight(280);
        listaMensagens.setPrefHeight(520);
        listaMensagens.setMaxHeight(620);
        listaMensagens.setFocusTraversable(false);
        listaMensagens.getStyleClass().add("message-saved-list");
        StackPane vazioIcone = new StackPane(AppIcon.create(AppIcon.Type.MESSAGE, 20));
        vazioIcone.getStyleClass().add("message-empty-icon");
        Label vazioTitulo = new Label("Seu acervo começa aqui");
        vazioTitulo.getStyleClass().add("message-empty-title");
        Label vazioDescricao = new Label(
                "Escreva a primeira mensagem acima e ela aparecerá nesta biblioteca.");
        vazioDescricao.getStyleClass().add("message-list-empty");
        VBox vazio = new VBox(7, vazioIcone, vazioTitulo, vazioDescricao);
        vazio.setAlignment(Pos.CENTER);
        vazio.getStyleClass().add("message-empty-state");
        listaMensagens.setPlaceholder(vazio);
        listaMensagens.setCellFactory(view -> new MensagemCell());
    }

    // ============================================================
    // SALVAR
    // ============================================================

    private void salvar() {

        String mensagem = mensagemArea.getText();

        if (mensagem == null || mensagem.isBlank()) {
            mostrarStatus("Informe uma mensagem.", false);
            return;
        }

        String conteudo = mensagem.trim();
        String mensagemOriginal = mensagemEmEdicao;
        boolean editando = mensagemOriginal != null;
        root.setDisable(true);
        statusLabel.setText("Salvando…");
        Thread thread = new Thread(() -> {

            try {

                if (mensagemOriginal == null) {
                    client.adicionarMensagemDoDia(conteudo);
                } else {
                    client.editarMensagemDoDia(mensagemOriginal, conteudo);
                }

                Platform.runLater(() -> {
                    resetarEditor();
                    mostrarStatus(
                            editando
                                    ? "Alterações salvas com sucesso."
                                    : "Mensagem salva com sucesso.",
                            true);
                    carregarLista();
                });

            } catch (Exception error) {

                Platform.runLater(() ->
                        mostrarStatus(
                                "Não foi possível salvar: " + error.getMessage(),
                                false
                        )
                );
            } finally {
                Platform.runLater(() -> root.setDisable(false));
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void carregarLista() {
        if (!carregandoLista.compareAndSet(false, true)) return;
        quantidadeComboBox.setDisable(true);
        resumoLista.setText("Carregando…");
        Thread thread = new Thread(() -> {
            try {
                List<String> mensagens = client.listarMensagensDoDia();
                List<String> recentesPrimeiro = new ArrayList<>(mensagens);
                java.util.Collections.reverse(recentesPrimeiro);
                Platform.runLater(() -> {
                    mensagensCarregadas.clear();
                    mensagensCarregadas.addAll(recentesPrimeiro);
                    aplicarLimiteSelecionado();
                });
            } catch (Exception error) {
                String detalhe = error.getMessage() == null
                        ? error.getClass().getSimpleName()
                        : error.getMessage();
                System.err.println("Não foi possível carregar as mensagens do dia: " + detalhe);
                Platform.runLater(() -> {
                    resumoLista.setText("Não foi possível carregar");
                    mostrarStatus("Não foi possível carregar as mensagens: " + detalhe, false);
                });
            } finally {
                carregandoLista.set(false);
                Platform.runLater(() -> quantidadeComboBox.setDisable(false));
            }
        }); thread.setDaemon(true); thread.start();
    }

    private void aplicarLimiteSelecionado() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::aplicarLimiteSelecionado);
            return;
        }
        int total = mensagensCarregadas.size();
        int quantidade = Math.min(obterLimiteSelecionado(), total);
        listaMensagens.getItems().setAll(
                mensagensCarregadas.subList(0, quantidade));
        resumoLista.setText("Exibindo " + quantidade + " de " + total);
        totalMensagensLabel.setText(String.valueOf(total));
    }

    private int obterLimiteSelecionado() {
        String valor = quantidadeComboBox.getValue();
        if (valor == null || "Todas".equals(valor)) return Integer.MAX_VALUE;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException ignored) {
            return 10;
        }
    }

    private void excluirMensagem(String item, Button botao) {
        botao.setDisable(true);
        Thread thread = new Thread(() -> {
            try {
                client.excluirMensagemDoDia(item);
                Platform.runLater(() -> {
                    if (item.equals(mensagemEmEdicao)) resetarEditor();
                    mostrarStatus("Mensagem excluída.", true);
                    carregarLista();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> mostrarStatus("Não foi possível excluir: " + ex.getMessage(), false));
                Platform.runLater(() -> botao.setDisable(false));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void confirmarExclusao(String item, Button botaoOrigem) {
        if (item == null || item.isBlank()) return;

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Excluir mensagem");
        dialogo.initModality(Modality.APPLICATION_MODAL);
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            dialogo.initOwner(root.getScene().getWindow());
        }

        ButtonType excluir = new ButtonType(
                "Excluir mensagem", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType(
                "Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        StackPane icone = new StackPane(AppIcon.create(AppIcon.Type.TRASH, 22));
        icone.getStyleClass().add("message-delete-icon");

        Label titulo = new Label("Excluir esta mensagem?");
        titulo.getStyleClass().add("message-delete-title");
        Label descricao = new Label(
                "Confirme se deseja remover definitivamente a mensagem selecionada.");
        descricao.setWrapText(true);
        descricao.getStyleClass().add("message-delete-description");
        VBox textos = new VBox(3, titulo, descricao);
        HBox cabecalho = new HBox(12, icone, textos);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.getStyleClass().add("message-delete-header");

        Label previaTitulo = new Label("MENSAGEM SELECIONADA");
        previaTitulo.getStyleClass().add("message-delete-preview-title");
        Label previa = new Label(item);
        previa.setWrapText(true);
        previa.setMaxWidth(Double.MAX_VALUE);
        previa.setMaxHeight(90);
        previa.setTooltip(new Tooltip(item));
        previa.getStyleClass().add("message-delete-preview-text");
        VBox cartaoPrevia = new VBox(7, previaTitulo, previa);
        cartaoPrevia.getStyleClass().add("message-delete-preview");

        Label aviso = new Label("Esta ação não poderá ser desfeita.");
        aviso.getStyleClass().add("message-delete-warning");

        VBox conteudo = new VBox(16, cabecalho, cartaoPrevia, aviso);
        conteudo.getStyleClass().add("message-delete-content");

        dialogo.getDialogPane().setContent(conteudo);
        dialogo.getDialogPane().getButtonTypes().setAll(cancelar, excluir);
        dialogo.getDialogPane().getStyleClass().add("message-delete-dialog");
        dialogo.getDialogPane().setMinWidth(500);

        var estiloBase = getClass().getResource(
                "/com/example/intranet_adm/style.css");
        var estiloRedesign = getClass().getResource(
                "/com/example/intranet_adm/redesign.css");
        if (estiloBase != null) {
            dialogo.getDialogPane().getStylesheets().add(
                    estiloBase.toExternalForm());
        }
        if (estiloRedesign != null) {
            dialogo.getDialogPane().getStylesheets().add(
                    estiloRedesign.toExternalForm());
        }

        Button botaoExcluir = (Button) dialogo.getDialogPane()
                .lookupButton(excluir);
        Button botaoCancelar = (Button) dialogo.getDialogPane()
                .lookupButton(cancelar);
        botaoExcluir.getStyleClass().add("danger-button");
        botaoCancelar.getStyleClass().add("secondary-button");
        botaoExcluir.setGraphic(AppIcon.create(AppIcon.Type.TRASH, 16));
        botaoCancelar.setGraphic(AppIcon.create(AppIcon.Type.CLOSE, 16));
        botaoExcluir.setDefaultButton(false);
        botaoCancelar.setDefaultButton(true);
        Platform.runLater(botaoCancelar::requestFocus);

        dialogo.showAndWait().ifPresent(resultado -> {
            if (resultado == excluir) {
                excluirMensagem(item, botaoOrigem);
            }
        });
    }

    private void iniciarEdicao(String item) {
        if (item == null || item.isBlank()) return;
        mensagemEmEdicao = item;
        mensagemArea.setText(item);
        atualizarModoEdicao();
        mensagemArea.requestFocus();
        mensagemArea.positionCaret(item.length());
        mostrarStatus("Faça as alterações e salve, ou cancele para voltar.", true);
    }

    private void cancelarEdicao() {
        if (mensagemEmEdicao == null) return;
        resetarEditor();
        mostrarStatusNeutro("Edição cancelada. Você pode criar uma nova mensagem.");
        mensagemArea.requestFocus();
    }

    private void resetarEditor() {
        mensagemEmEdicao = null;
        mensagemArea.clear();
        atualizarModoEdicao();
    }

    private void atualizarModoEdicao() {
        boolean editando = mensagemEmEdicao != null;
        editorTitulo.setText(editando ? "Editar mensagem" : "Nova mensagem");
        editorDescricao.setText(editando
                ? "Atualize o texto selecionado sem criar uma nova mensagem."
                : "Crie uma mensagem de destaque para os colaboradores.");
        salvarButton.setText(editando ? "Salvar alterações" : "Salvar mensagem");
        cancelarEdicaoButton.setVisible(editando);
        modoEdicaoLabel.setVisible(editando);
        listaMensagens.refresh();
    }

    private final class MensagemCell extends ListCell<String> {
        private final Label ordem = new Label();
        private final Label categoria = new Label("MENSAGEM DO DIA");
        private final Label texto = new Label();
        private final Button editar = new Button("Editar");
        private final Button excluir = new Button("Excluir");
        private final VBox conteudo = new VBox(5, categoria, texto);
        private final HBox acoes = new HBox(8, editar, excluir);
        private final HBox linha = new HBox(14, ordem, conteudo, acoes);

        private MensagemCell() {
            setText(null);
            setPadding(Insets.EMPTY);
            getStyleClass().add("message-saved-cell");

            texto.setWrapText(true);
            texto.setMinWidth(0);
            texto.setMaxWidth(Double.MAX_VALUE);
            texto.getStyleClass().add("message-saved-text");
            conteudo.setMinWidth(0);
            conteudo.setMaxWidth(Double.MAX_VALUE);
            conteudo.getStyleClass().add("message-saved-content");
            HBox.setHgrow(conteudo, Priority.ALWAYS);

            ordem.getStyleClass().add("message-saved-index");
            categoria.getStyleClass().add("message-saved-category");

            editar.getStyleClass().add("secondary-button");
            editar.setGraphic(AppIcon.create(AppIcon.Type.EDIT, 15));
            editar.setOnAction(event -> iniciarEdicao(getItem()));

            excluir.getStyleClass().add("danger-button");
            excluir.setGraphic(AppIcon.create(AppIcon.Type.TRASH, 15));
            excluir.setOnAction(event -> {
                String item = getItem();
                if (item != null) confirmarExclusao(item, excluir);
            });

            acoes.setAlignment(Pos.CENTER_RIGHT);
            acoes.getStyleClass().add("message-saved-actions");
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.setPadding(new Insets(15, 15, 15, 13));
            linha.getStyleClass().add("message-saved-row");
            linha.prefWidthProperty().bind(
                    listaMensagens.widthProperty().subtract(28));
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            linha.getStyleClass().remove("message-saved-row-editing");
            if (empty || item == null) {
                ordem.setText(null);
                texto.setText(null);
                setGraphic(null);
                return;
            }
            ordem.setText(String.format("%02d", getIndex() + 1));
            texto.setText(item);
            excluir.setDisable(false);
            boolean editando = item.equals(mensagemEmEdicao);
            editar.setDisable(editando);
            editar.setText(editando ? "Editando" : "Editar");
            if (editando) {
                linha.getStyleClass().add("message-saved-row-editing");
            }
            setGraphic(linha);
        }
    }

    private void mostrarStatus(String mensagem, boolean sucesso) {

        statusLabel.setText(mensagem);

        statusLabel.setTextFill(
                sucesso ? Color.web("#16A34A") : Color.web("#DC2626")
        );
    }

    private void mostrarStatusNeutro(String mensagem) {
        statusLabel.setText(mensagem);
        statusLabel.setTextFill(Color.web("#64748B"));
    }

    // ============================================================
    // ROOT
    // ============================================================

    public Node getView() {
        return root;
    }

    public VBox getRoot() {
        return root;
    }
}
