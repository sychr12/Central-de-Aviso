package com.example.intranet_adm.view.mensagem;

import com.example.intranet_adm.service.IntranetAvisosClient;
import com.example.intranet_adm.view.components.AppIcon;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.scene.paint.Color;
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
    private final CheckBox ativoCheckBox = new CheckBox("Exibir mensagem do dia");
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
        root.setSpacing(16);
        root.setPadding(new Insets(0));
        root.setFillWidth(true);

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
        mensagemArea.setPrefRowCount(4);
        mensagemArea.getStyleClass().add("form-field");
        mensagemArea.textProperty().addListener((observavel, anterior, atual) -> {
            int quantidade = atual == null ? 0 : atual.length();
            contadorCaracteres.setText(
                    quantidade == 1 ? "1 caractere" : quantidade + " caracteres");
        });

        ativoCheckBox.setSelected(true);

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
                10, ativoCheckBox, espacoAcoes, cancelarEdicaoButton, salvarButton);
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

        VBox editor = new VBox(
                15, editorCabecalho, campoMensagem, botoes, statusLabel);
        editor.getStyleClass().addAll(\u0022app-card\u0022, \u0022message-editor-panel\u0022);

        quantidadeComboBox.getItems().addAll(
                \u002210\u0022, \u002250\u0022, \u0022100\u0022, \u0022Todas\u0022);
        quantidadeComboBox.setValue(\u002210\u0022);
        quantidadeComboBox.setPrefWidth(105);
        quantidadeComboBox.getStyleClass().add(\u0022message-limit-selector\u0022);
        quantidadeComboBox.setOnAction(event -> aplicarLimiteSelecionado());
        resumoLista.getStyleClass().add(\u0022message-list-summary\u0022);

        Label exibirLabel = new Label(\u0022Exibir\u0022);
        exibirLabel.getStyleClass().add(\u0022message-limit-label\u0022);
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox bibliotecaCabecalho = new HBox(
                12, bibliotecaTitulos, resumoLista, espaco, exibirLabel, quantidadeComboBox);
        bibliotecaCabecalho.setAlignment(Pos.CENTER_LEFT);
        bibliotecaCabecalho.getStyleClass().add(\u0022message-list-header\u0022);

        configurarListaMensagens();
        VBox biblioteca = new VBox(10, bibliotecaCabecalho, listaMensagens);
        biblioteca.getStyleClass().addAll(\u0022app-card\u0022, \u0022message-library-panel\u0022);
        editor.setMaxWidth(Double.MAX_VALUE);
        biblioteca.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().addAll(editor, biblioteca);
        carregarLista();
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
        Label vazio = new Label("Nenhuma mensagem salva.");
        vazio.getStyleClass().add("message-list-empty");
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
    }

    private int obterLimiteSelecionado() {
        String valor = quantidadeComboBox.getValue();
        if (valor == null || \u0022Todas\u0022.equals(valor)) return Integer.MAX_VALUE;
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
        ativoCheckBox.setSelected(true);
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
        private final Label texto = new Label();
        private final Button editar = new Button("Editar");
        private final Button excluir = new Button("Excluir");
        private final StackPane icone = new StackPane(
                AppIcon.create(AppIcon.Type.MESSAGE, 16));
        private final HBox acoes = new HBox(8, editar, excluir);
        private final HBox linha = new HBox(12, icone, texto, acoes);

        private MensagemCell() {
            setText(null);
            setPadding(Insets.EMPTY);
            getStyleClass().add("message-saved-cell");

            texto.setWrapText(true);
            texto.setMinWidth(0);
            texto.setMaxWidth(Double.MAX_VALUE);
            texto.getStyleClass().add("message-saved-text");
            HBox.setHgrow(texto, Priority.ALWAYS);

            icone.getStyleClass().add("message-saved-icon");

            editar.getStyleClass().add("secondary-button");
            editar.setGraphic(AppIcon.create(AppIcon.Type.EDIT, 15));
            editar.setOnAction(event -> iniciarEdicao(getItem()));

            excluir.getStyleClass().add("danger-button");
            excluir.setGraphic(AppIcon.create(AppIcon.Type.TRASH, 15));
            excluir.setOnAction(event -> {
                String item = getItem();
                if (item != null) excluirMensagem(item, excluir);
            });

            acoes.setAlignment(Pos.CENTER_RIGHT);
            acoes.getStyleClass().add("message-saved-actions");
            linha.setAlignment(Pos.CENTER_LEFT);
            linha.setPadding(new Insets(13, 14, 13, 12));
            linha.getStyleClass().add("message-saved-row");
            linha.prefWidthProperty().bind(
                    listaMensagens.widthProperty().subtract(28));
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            linha.getStyleClass().remove("message-saved-row-editing");
            if (empty || item == null) {
                texto.setText(null);
                setGraphic(null);
                return;
            }
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
