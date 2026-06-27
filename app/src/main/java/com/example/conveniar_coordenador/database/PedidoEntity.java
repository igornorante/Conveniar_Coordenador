package com.example.conveniar_coordenador.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pedidos")
public class PedidoEntity {

    @PrimaryKey
    @NonNull
    public String numPedido; // ID único do pedido

    public String situacao;

    public boolean precisaAtencao; // Para a sua View de Atenção

    public String jsonOriginal;

    public PedidoEntity() {}
}
