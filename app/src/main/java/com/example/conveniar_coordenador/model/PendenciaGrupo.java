package com.example.conveniar_coordenador.model;

import java.util.List;

public class PendenciaGrupo {
    private String titulo;
    private int total;
    private List<PendenciaStatus> statusList;

    public PendenciaGrupo(String titulo, int total, List<PendenciaStatus> statusList) {
        this.titulo = titulo;
        this.total = total;
        this.statusList = statusList;
    }

    public String getTitulo() { return titulo; }
    public int getTotal() { return total; }
    public List<PendenciaStatus> getStatusList() { return statusList; }
}
