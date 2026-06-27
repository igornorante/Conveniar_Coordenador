package com.example.conveniar_coordenador.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;



/*
Quanto a divisão das colunas:
O que será utilizado na pesquisa pelo aplicativo deve ficar no seguinte formato:
public String nomeTipoPedido;

o resto das informações podem ser adquiridas a partir do JSON

 */

@Entity(tableName = "pedidos_pagamento")
public class PedidoPagamentoEntity {

    @PrimaryKey
    @NonNull
    public String numeroPedido = ""; // Chave primária (ID unico) (ex: 2731/2026)

    public String nomeTipoPedido; // Ex: Pagamento Bolsa
    public String nomeStatus; // A situação atual
    public String nomeFavorecido;

    public String jsonOriginal; // Guarda todos os outros campos (data, valor, etc)
    public boolean precisaAtencao; // O usuário já viu que teve mudança nesse item?

    public PedidoPagamentoEntity() {}
}
