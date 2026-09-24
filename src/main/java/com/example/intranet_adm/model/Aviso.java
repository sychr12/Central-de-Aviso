package com.example.intranet_adm.model;

import java.time.LocalDate;

public class Aviso {

    private int id;
    private String titulo;
    private String mensagem;
    private String autor;
    private LocalDate dataPublicacao;
    private String criticidade;
    private String prioridade;

    public Aviso(
            int id,
            String titulo,
            String mensagem,
            String autor,
            LocalDate dataPublicacao) {

        this(id, titulo, mensagem, autor, dataPublicacao, "Informativa", "Normal");
    }

    public Aviso(
            int id,
            String titulo,
            String mensagem,
            String autor,
            LocalDate dataPublicacao,
            String criticidade,
            String prioridade) {

        this.id = id;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.autor = autor;
        this.dataPublicacao = dataPublicacao;
        this.criticidade = valorOuPadrao(criticidade, "Informativa");
        this.prioridade = valorOuPadrao(prioridade, "Normal");
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDate getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDate dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public String getCriticidade() {
        return criticidade;
    }

    public void setCriticidade(String criticidade) {
        this.criticidade = valorOuPadrao(criticidade, "Informativa");
    }

    public String getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(String prioridade) {
        this.prioridade = valorOuPadrao(prioridade, "Normal");
    }

    private static String valorOuPadrao(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }

    @Override
    public String toString() {
        return titulo + " - " + autor + " - " + dataPublicacao;
    }
}
