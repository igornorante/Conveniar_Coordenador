package com.example.conveniar_coordenador.model;

public class ConsultaItem {
    private String numero;
    private String status;
    private String tipo;       // tipo do pedido (só pagamento)
    private String projeto;
    private String pessoa;     // fornecedor ou favorecido
    private String labelPessoa; // "Fornecedor" ou "Favorecido"
    private String data;
    private double valor;
    private boolean isPagamento;

    public ConsultaItem(String numero, String status, String tipo, String projeto,
                        String pessoa, String labelPessoa, String data, double valor, boolean isPagamento) {
        this.numero = numero;
        this.status = status;
        this.tipo = tipo;
        this.projeto = projeto;
        this.pessoa = pessoa;
        this.labelPessoa = labelPessoa;
        this.data = data;
        this.valor = valor;
        this.isPagamento = isPagamento;
    }

    public String getNumero() { return numero; }
    public String getStatus() { return status; }
    public String getTipo() { return tipo; }
    public String getProjeto() { return projeto; }
    public String getPessoa() { return pessoa; }
    public String getLabelPessoa() { return labelPessoa; }
    public String getData() { return data; }
    public double getValor() { return valor; }
    public boolean isPagamento() { return isPagamento; }
}
