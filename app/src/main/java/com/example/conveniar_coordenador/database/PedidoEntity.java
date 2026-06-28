package com.example.conveniar_coordenador.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pedidos")
public class PedidoEntity {

    @PrimaryKey
    @NonNull
    public String numPedido; // Ex: 123/2024

    public int codPedido; // ID interno numérico para links

    public String situacao;

    public String codProjeto;

    public boolean precisaAtencao;

    public String jsonOriginal;

    public PedidoEntity() {}
}
