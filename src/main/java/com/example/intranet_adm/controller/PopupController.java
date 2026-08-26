package com.example.intranet_adm.controller;

import com.example.intranet_adm.model.Popup;
import com.example.intranet_adm.service.IntranetAvisosClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PopupController {

    @FXML
    private ListView<Popup> popupListView;

    private final IntranetAvisosClient client =
            new IntranetAvisosClient();

    private final ObservableList<Popup> popups =
            FXCollections.observableArrayList();

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {

        if (popupListView != null) {
            popupListView.setItems(popups);

            popupListView.setCellFactory(listView ->
                    new javafx.scene.control.ListCell<>() {

                        @Override
                        protected void updateItem(
                                Popup popup,
                                boolean empty) {

                            super.updateItem(popup, empty);

                            if (empty || popup == null) {
                                setText(null);
                            } else {
                                setText(popup.toString());
                            }
                        }
                    }
            );
        }

        carregarPopups();
    }

    @FXML
    protected void onAtualizarClick() {
        carregarPopups();
    }

    @FXML
    protected void onAtivarClick() {

        Popup popup = popupSelecionado();

        if (popup == null) {
            mostrarErro("Selecione um popup.");
            return;
        }

        executar(() -> {
            try {
                client.ativarPopup(popup.getId());
                carregarPopups();
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }

                throw new RuntimeException(
                        "Não foi possível ativar o popup.",
                        e
                );
            }
        });
    }

    @FXML
    protected void onDesativarClick() {

        Popup popup = popupSelecionado();

        if (popup == null) {
            mostrarErro("Selecione um popup.");
            return;
        }

        executar(() -> {
            try {
                client.desativarPopup(popup.getId());
                carregarPopups();
            } catch (IOException | InterruptedException e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }

                throw new RuntimeException(
                        "Não foi possível desativar o popup.",
                        e
                );
            }
        });
    }

    @FXML
    protected void onExcluirClick() {

        Popup popup = popupSelecionado();

        if (popup == null) {
            mostrarErro("Selecione um popup.");
            return;
        }

        Alert confirmacao =
                new Alert(Alert.AlertType.CONFIRMATION);

        confirmacao.setTitle("Excluir popup");
        confirmacao.setHeaderText(
                "Deseja realmente excluir este popup?"
        );
        confirmacao.setContentText(
                popup.getTitulo()
        );

        confirmacao.showAndWait().ifPresent(resultado -> {

            if (resultado == ButtonType.OK) {

                executar(() -> {
                    try {
                        client.excluirPopup(popup.getId());
                        carregarPopups();
                    } catch (IOException | InterruptedException e) {
                        if (e instanceof InterruptedException) {
                            Thread.currentThread().interrupt();
                        }

                        throw new RuntimeException(
                                "Não foi possível excluir o popup.",
                                e
                        );
                    }
                });
            }
        });
    }

    private Popup popupSelecionado() {

        if (popupListView == null) {
            return null;
        }

        return popupListView
                .getSelectionModel()
                .getSelectedItem();
    }

    private void carregarPopups() {

        executor.submit(() -> {

            try {

                List<Popup> resposta =
                        client.listarPopups();

                Platform.runLater(() ->
                        popups.setAll(resposta)
                );

            } catch (IOException | InterruptedException e) {

                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }

                Platform.runLater(() ->
                        mostrarErro(
                                "Não foi possível carregar os popups:\n"
                                        + e.getMessage()
                        )
                );
            }
        });
    }

    private void executar(Runnable tarefa) {

        executor.submit(() -> {

            try {
                tarefa.run();

            } catch (Exception error) {

                Platform.runLater(() ->
                        mostrarErro(
                                error.getMessage() == null
                                        ? "Erro ao executar operação."
                                        : error.getMessage()
                        )
                );
            }
        });
    }

    private void mostrarErro(String mensagem) {

        Alert alert =
                new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Central de Avisos");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public void encerrar() {
        executor.shutdownNow();
    }
}