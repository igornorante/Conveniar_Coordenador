package com.example.conveniar_coordenador.model;

public class PendenciaStatus {
    private String nome;
    private int quantidade;
    private String tipo; // "envio", "aprovacao", "ajuste"

    public PendenciaStatus(String nome, int quantidade, String tipo) {
        this.nome = nome;
        this.quantidade = quantidade;
        this.tipo = tipo;
    }

    public String getNome() { return nome; }
    public int getQuantidade() { return quantidade; }
    public String getTipo() { return tipo; }
}
