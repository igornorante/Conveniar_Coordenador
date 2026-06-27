package com.example.conveniar_coordenador.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import androidx.lifecycle.LiveData;

import java.util.List;

@Dao
public interface AppDAO {

    // --- PROJETOS ---

    // Insere ou atualiza um projeto caso o ID já exista
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarProjeto(ProjetoEntity projeto);

    // Busca um projeto específico para comparar o status
    @Query("SELECT * FROM projetos WHERE idProjeto = :id")
    ProjetoEntity getProjetoById(String id);

    // Retorna apenas os projetos que o usuário precisa ver na "View de Atenção"
    @Query("SELECT * FROM projetos WHERE precisaAtencao = 1")
    List<ProjetoEntity> getProjetosComAtencao();

    // Marca todos os projetos como "vistos" (quando o usuário abrir a tela)
    @Query("UPDATE projetos SET precisaAtencao = 0")
    void limparAtencaoProjetos();

    @Query("SELECT * FROM projetos")
    LiveData<List<ProjetoEntity>> getTodosProjetosLive();


    // --- PEDIDOS ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarPedido(PedidoEntity pedido);

    @Query("SELECT * FROM pedidos WHERE numPedido = :id")
    PedidoEntity getPedidoById(String id);

    @Query("SELECT * FROM pedidos WHERE precisaAtencao = 1")
    List<PedidoEntity> getPedidosComAtencao();

    @Query("UPDATE pedidos SET precisaAtencao = 0")
    void limparAtencaoPedidos();

    // NOVO: Retorna uma lista "viva" de todos os pedidos
    @Query("SELECT * FROM pedidos")
    LiveData<List<PedidoEntity>> getTodosPedidosLive();

    // Retorna uma lista observável de pedidos filtrada pelo tipo exato
    @Query("SELECT * FROM pedidos WHERE situacao = :situacao")
    LiveData<List<PedidoEntity>> getPedidosPorSituacao(String situacao);
    // Registrado, Enviado, Aprovado, Em Cotação, Aguardando parecer técnico, Aguardando liberação de recurso, Aguardando emissão de AF/OS, Aguardando chegada da mercadoria, Recebido parcialmente, Devolvido, Finalizado, Cancelado, Reprovado



    // --- Pedidos de Compra ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarPedidoPagamento(PedidoPagamentoEntity pedido);

    @Query("SELECT * FROM pedidos_pagamento WHERE numeroPedido = :id")
    PedidoPagamentoEntity getPedidoPagamentoById(String id);

    // O método mágico que já filtra direto no banco de dados!
    @Query("SELECT * FROM pedidos_pagamento WHERE nomeTipoPedido = :tipo")
    LiveData<List<PedidoPagamentoEntity>> getPedidosPagamentoPorTipoLive(String tipo);
    //

    @Query("SELECT * FROM pedidos_pagamento")
    LiveData<List<PedidoPagamentoEntity>> getTodosPedidosPagamentoLive();

}
