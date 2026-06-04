package com.example.conveniar_coordenador.model;

public class Pedido {
    private int codPedido;
    private String dataPedido;
    private String nomeProjeto;
    private String produto;
    private String fornecedor;
    private String status;
    private double valor;

    public Pedido(int codPedido, String dataPedido, String nomeProjeto, String produto, String fornecedor, String status, double valor) {
        this.codPedido = codPedido;
        this.dataPedido = dataPedido;
        this.nomeProjeto = nomeProjeto;
        this.produto = produto;
        this.fornecedor = fornecedor;
        this.status = status;
        this.valor = valor;
    }

    public int getCodPedido() { return codPedido; }
    public String getDataPedido() { return dataPedido; }
    public String getNomeProjeto() { return nomeProjeto; }
    public String getProduto() { return produto; }
    public String getFornecedor() { return fornecedor; }
    public String getStatus() { return status; }
    public double getValor() { return valor; }

    @Override
    public String toString() {
        return "Pedido #" + codPedido + " - " + status + "\n" +
               "Data: " + dataPedido + " | Valor: R$ " + String.format("%.2f", valor) + "\n" +
               "Projeto: " + nomeProjeto + "\n" +
               "Fornecedor: " + fornecedor;
    }
}