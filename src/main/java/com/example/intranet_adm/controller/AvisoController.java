package com.example.intranet_adm.controller;

import com.example.intranet_adm.model.Aviso;
import com.example.intranet_adm.service.AvisoService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class AvisoController {

    private final AvisoService avisoService = new AvisoService();

    private final ObservableList<Aviso> avisos =
            FXCollections.observableArrayList();

    @FXML
    private ListView<Aviso> avisosListView;

    @FXML
    public void initialize() {
        carregarAvisos();
    }

    private void carregarAvisos() {
        if (avisosListView == null) {
            return;
        }

        avisos.setAll(avisoService.listarTodos());
        avisosListView.setItems(avisos);
    }

    public void atualizarLista() {
        carregarAvisos();
    }

    public ObservableList<Aviso> getAvisos() {
        return avisos;
    }
}