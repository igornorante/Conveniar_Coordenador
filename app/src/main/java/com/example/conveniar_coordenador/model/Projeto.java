package com.example.conveniar_coordenador.model;

public class Projeto {

    private int codConvenio;
    private String nomeConvenio;
    private double saldo;
    private String nomeStatus;
    private String coordenador;
    private String dataVigencia;

    public Projeto(int codConvenio, String nomeConvenio, double saldo, String nomeStatus, String coordenador, String dataVigencia) {
        this.codConvenio = codConvenio;
        this.nomeConvenio = nomeConvenio;
        this.saldo = saldo;
        this.nomeStatus = nomeStatus;
        this.coordenador = coordenador;
        this.dataVigencia = dataVigencia;
    }

    public int getCodConvenio() {
        return codConvenio;
    }

    public String getNomeConvenio() {
        return nomeConvenio;
    }

    public double getSaldo() {
        return saldo;
    }

    public String getNomeStatus() {
        return nomeStatus;
    }

    public String getCoordenador() {
        return coordenador;
    }

    public String getDataVigencia() {
        return dataVigencia;
    }

    @Override
    public String toString() {
        return codConvenio + " - " + nomeConvenio;
    }
}