package com.example.conveniar_coordenador.model;

public class ExtratoItem {

    public enum Tipo {
        SALDO_PROJETO,
        RUBRICA_HEADER,
        LANCAMENTO,
        SALDO_RUBRICA,
        SEM_LANCAMENTO
    }

    private Tipo tipo;
    private String titulo;
    private String subtitulo;
    private String valor;
    private boolean debito;

    public ExtratoItem(Tipo tipo, String titulo, String subtitulo, String valor, boolean debito) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.valor = valor;
        this.debito = debito;
    }

    public Tipo getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getSubtitulo() { return subtitulo; }
    public String getValor() { return valor; }
    public boolean isDebito() { return debito; }
}
