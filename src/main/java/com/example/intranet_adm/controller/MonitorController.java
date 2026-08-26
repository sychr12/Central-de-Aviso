package com.example.intranet_adm.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MonitorController {

    @FXML
    private Label monitorOnlineLabel;

    @FXML
    private Label monitorAcessosHojeLabel;

    @FXML
    private Label monitorTotalVisitantesLabel;

    @FXML
    private Label monitorUltimaConexaoLabel;

    @FXML
    public void initialize() {
        limparInformacoes();
    }

    public void atualizar(
            int onlineAgora,
            int acessosHoje,
            int totalVisitantes,
            String ultimaConexao) {

        if (monitorOnlineLabel != null) {
            monitorOnlineLabel.setText(String.valueOf(onlineAgora));
        }

        if (monitorAcessosHojeLabel != null) {
            monitorAcessosHojeLabel.setText(String.valueOf(acessosHoje));
        }

        if (monitorTotalVisitantesLabel != null) {
            monitorTotalVisitantesLabel.setText(
                    String.valueOf(totalVisitantes)
            );
        }

        if (monitorUltimaConexaoLabel != null) {
            monitorUltimaConexaoLabel.setText(
                    ultimaConexao == null || ultimaConexao.isBlank()
                            ? "Nenhuma conexão"
                            : ultimaConexao
            );
        }
    }

    private void limparInformacoes() {
        if (monitorOnlineLabel != null) {
            monitorOnlineLabel.setText("0");
        }

        if (monitorAcessosHojeLabel != null) {
            monitorAcessosHojeLabel.setText("0");
        }

        if (monitorTotalVisitantesLabel != null) {
            monitorTotalVisitantesLabel.setText("0");
        }

        if (monitorUltimaConexaoLabel != null) {
            monitorUltimaConexaoLabel.setText("Nenhuma conexão");
        }
    }
}