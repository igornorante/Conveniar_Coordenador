package com.example.conveniar_coordenador.database;

import android.content.Context;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppRepository {

    private AppDAO dao;
    private LiveData<List<ProjetoEntity>> todosProjetos;
    private LiveData<List<PedidoEntity>> todosPedidos;

    private LiveData<List<PedidoPagamentoEntity>> todosPedidosPagamento;

    // Usamos um ExecutorService para rodar tarefas pesadas de banco de dados fora da Thread da UI
    private ExecutorService executorService;

    public AppRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        dao = db.appDao();

        // O Room já sabe que LiveData roda em background, então é seguro chamar direto
        todosProjetos = dao.getTodosProjetosLive();
        todosPedidos = dao.getTodosPedidosLive();
        todosPedidosPagamento = dao.getTodosPedidosPagamentoLive();

        executorService = Executors.newSingleThreadExecutor();
    }

    // A Activity vai chamar este método para desenhar a tela
    public LiveData<List<ProjetoEntity>> getTodosProjetos() {
        return todosProjetos;
    }

    public LiveData<List<PedidoEntity>> getTodosPedidos() {
        return todosPedidos;
    }

    public LiveData<List<PedidoPagamentoEntity>> getTodosPedidosPagamento(){
        return todosPedidosPagamento;
    }

    //

    public LiveData<List<PedidoPagamentoEntity>> getPagamentoBolsa() {
        return dao.getPedidosPagamentoPorTipoLive("Pagamento Bolsa");
    }

    public LiveData<List<PedidoPagamentoEntity>> getPagamentoPessoaFisica() {
        return dao.getPedidosPagamentoPorTipoLive("Pagamento Pessoa Física");
    }

    public LiveData<List<PedidoPagamentoEntity>> getPagamentoPessoaJuridica() {
        return dao.getPedidosPagamentoPorTipoLive("Pagamento Pessoa Jurídica");
    }

    public LiveData<List<PedidoPagamentoEntity>> getPedidoAdiantamento() {
        return dao.getPedidosPagamentoPorTipoLive("Pedido de Adiantamento");
    }

    public LiveData<List<PedidoPagamentoEntity>> getPedidoReembolso() {
        return dao.getPedidosPagamentoPorTipoLive("Pedido de Reembolso");
    }

    public LiveData<List<PedidoPagamentoEntity>> getPagamentoBolsaEmLote() {
        return dao.getPedidosPagamentoPorTipoLive("Pagamento Bolsa em Lote");
    }

    public LiveData<List<PedidoPagamentoEntity>> getEntradaReceita() {
        return dao.getPedidosPagamentoPorTipoLive("Entrada de Receita");
    }

    public LiveData<List<PedidoPagamentoEntity>> getPagamentoDiaria() {
        return dao.getPedidosPagamentoPorTipoLive("Pagamento Diária");
    }

    public LiveData<List<PedidoPagamentoEntity>> getAcertoAdiantamento() {
        return dao.getPedidosPagamentoPorTipoLive("Pedido de Acerto de Adiantamento");
    }

    public LiveData<List<PedidoPagamentoEntity>> getReconhecimentoReceita() {
        return dao.getPedidosPagamentoPorTipoLive("Pedido de Reconhecimento Receita");
    }

    public LiveData<List<PedidoPagamentoEntity>> getTransferenciaProjetos() {
        return dao.getPedidosPagamentoPorTipoLive("Pedido de Transferência entre Projetos");
    }

    // --- Futuramente, a lógica de sincronização da API (Coordenador) virá para cá ---
    // Exemplo de como você vai inserir dados usando o Repository sem travar a tela:
    public void salvarProjeto(ProjetoEntity projeto) {
        executorService.execute(() -> {
            dao.salvarProjeto(projeto);
        });
    }
}
