package com.example.intranet_adm.model;

public class EstatisticasAcesso {

    private int onlineAgora;
    private int acessosHoje;
    private int totalVisitantes;
    private String ultimaConexao;

    public EstatisticasAcesso(
            int onlineAgora,
            int acessosHoje,
            int totalVisitantes,
            String ultimaConexao) {

        this.onlineAgora = onlineAgora;
        this.acessosHoje = acessosHoje;
        this.totalVisitantes = totalVisitantes;
        this.ultimaConexao = ultimaConexao;
    }

    public int getOnlineAgora() {
        return onlineAgora;
    }

    public void setOnlineAgora(int onlineAgora) {
        this.onlineAgora = onlineAgora;
    }

    public int getAcessosHoje() {
        return acessosHoje;
    }

    public void setAcessosHoje(int acessosHoje) {
        this.acessosHoje = acessosHoje;
    }

    public int getTotalVisitantes() {
        return totalVisitantes;
    }

    public void setTotalVisitantes(int totalVisitantes) {
        this.totalVisitantes = totalVisitantes;
    }

    public String getUltimaConexao() {
        return ultimaConexao;
    }

    public void setUltimaConexao(String ultimaConexao) {
        this.ultimaConexao = ultimaConexao;
    }

    @Override
    public String toString() {
        return "EstatisticasAcesso{" +
                "onlineAgora=" + onlineAgora +
                ", acessosHoje=" + acessosHoje +
                ", totalVisitantes=" + totalVisitantes +
                ", ultimaConexao='" + ultimaConexao + '\'' +
                '}';
    }
}