package com.example.intranet_adm.model;

import java.util.Objects;

public class Popup {

    private String id;
    private String titulo;
    private String mensagem;
    private boolean ativo;
    private String modelo;
    private String tamanho;
    private String paginas;

    public Popup(
            String id,
            String titulo,
            String mensagem,
            boolean ativo,
            String modelo,
            String tamanho,
            String paginas) {

        this.id = id;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.ativo = ativo;
        this.modelo = modelo;
        this.tamanho = tamanho;
        this.paginas = paginas;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public String getPaginas() {
        return paginas;
    }

    public void setPaginas(String paginas) {
        this.paginas = paginas;
    }

    @Override
    public String toString() {
        return titulo + " - " + (ativo ? "Ativo" : "Inativo");
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Popup outro)) {
            return false;
        }

        return Objects.equals(id, outro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}