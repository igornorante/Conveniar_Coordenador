package com.example.conveniar_coordenador.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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


    // --- PEDIDOS ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void salvarPedido(PedidoEntity pedido);

    @Query("SELECT * FROM pedidos WHERE numPedido = :id")
    PedidoEntity getPedidoById(String id);

    @Query("SELECT * FROM pedidos WHERE precisaAtencao = 1")
    List<PedidoEntity> getPedidosComAtencao();

    @Query("UPDATE pedidos SET precisaAtencao = 0")
    void limparAtencaoPedidos();
}
