package com.example.conveniar_coordenador.model;

import java.io.Serializable;

public class Projeto implements Serializable {

    private int codProjeto;
    private String nomeConvenio;
    private double saldo;
    private String nomeStatus;

    public Projeto(int codProjeto, String nomeConvenio, double saldo, String nomeStatus) {
        this.codProjeto = codProjeto;
        this.nomeConvenio = nomeConvenio;
        this.saldo = saldo;
        this.nomeStatus = nomeStatus;
    }

    public int getCodProjeto() {
        return codProjeto;
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

    @Override
    public String toString() {
        return nomeConvenio + "\nSaldo: " + saldo + " | Status: " + nomeStatus;
    }
}