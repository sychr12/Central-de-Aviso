/// Olá! Este arquivo representa o modelo de um Aviso.
/// Ele define os dados que um aviso possui, como ID, título, mensagem, autor e data.
/// As outras classes utilizam este modelo para trabalhar com os avisos.
/// Se os dados do Aviso forem alterados, verifique as classes que utilizam este modelo. =)


package com.example.intranet_adm.model;

import java.time.LocalDate;

public class Aviso {
    private int id;
    private String titulo;
    private String mensagem;
    private String autor;
    private LocalDate dataPublicacao;

    public Aviso(int id, String titulo, String mensagem, String autor, LocalDate dataPublicacao) {
        this.id = id;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.autor = autor;
        this.dataPublicacao = dataPublicacao;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public LocalDate getDataPublicacao() { return dataPublicacao; }
    public void setDataPublicacao(LocalDate dataPublicacao) { this.dataPublicacao = dataPublicacao; }

    @Override
    public String toString() {
        return titulo + " - " + autor + " - " + dataPublicacao;
    }
}