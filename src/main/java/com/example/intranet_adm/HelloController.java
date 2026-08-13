package com.example.intranet_adm;

import javafx.fxml.FXML;
import com.example.intranet_adm.model.Aviso;
import com.example.intranet_adm.service.AvisoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class HelloController {
    @FXML private TextField tituloField;
    @FXML private TextField autorField;
    @FXML private TextArea mensagemField;
    @FXML private ListView<Aviso> avisoListView;
    @FXML private Label statusLabel;

    private final AvisoService avisoService = new AvisoService();
    private final ObservableList<Aviso> avisosObservaveis = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        avisosObservaveis.setAll(avisoService.listarTodos());
    }

    @SuppressWarnings("unchecked")
    public void configurarControles(Parent root) {
        tituloField = (TextField) root.lookup("#tituloField");
        autorField = (TextField) root.lookup("#autorField");
        mensagemField = (TextArea) root.lookup("#mensagemField");
        avisoListView = (ListView<Aviso>) root.lookup("#avisosListView");
        statusLabel = (Label) root.lookup("#statusLabel");
        avisoListView.setItems(avisosObservaveis);
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
        avisoService.adicionar(titulo.trim(), mensagem.trim(), autor == null || autor.isBlank() ? "Anônimo" : autor.trim());
        avisosObservaveis.setAll(avisoService.listarTodos());
        tituloField.clear(); autorField.clear(); mensagemField.clear();
        statusLabel.setText("Aviso publicado com sucesso!");
    }
}
