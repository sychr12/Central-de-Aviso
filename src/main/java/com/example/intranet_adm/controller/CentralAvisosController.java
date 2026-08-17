/// Olá! Este arquivo controla as ações da tela Central de Avisos.
/// Ele recebe os dados preenchidos pelo usuário, valida as informações
/// e chama o AvisoService para realizar as operações.
/// Alterações nos campos ou ações da tela podem exigir mudanças aqui também. =)

package com.example.intranet_adm.controller;

import com.example.intranet_adm.model.Aviso;
import com.example.intranet_adm.service.AvisoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CentralAvisosController {

    @FXML
    private TextField tituloField;

    @FXML
    private TextField autorField;

    @FXML
    private TextArea mensagemField;

    @FXML
    private ListView<Aviso> avisosListView;

    @FXML
    private Label statusLabel;

    private final AvisoService avisoService = new AvisoService();
    private final ObservableList<Aviso> avisosObservaveis = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        avisosObservaveis.setAll(avisoService.listarTodos());
        avisosListView.setItems(avisosObservaveis);
    }

    @FXML
    protected void onPublicarClick() {
        String titulo = tituloField.getText();
        String autor = autorField.getText();
        String mensagem = mensagemField.getText();

        if (titulo == null || titulo.isBlank() || mensagem == null || mensagem.isBlank()) {
            statusLabel.setText("Preencha ao menos o título e a mensagem.");
            return;
        }

        avisoService.adicionar(titulo.trim(), mensagem.trim(),
                autor == null || autor.isBlank() ? "Anônimo" : autor.trim());
        avisosObservaveis.setAll(avisoService.listarTodos());

        tituloField.clear();
        autorField.clear();
        mensagemField.clear();
        statusLabel.setText("Aviso publicado com sucesso!");
    }
}