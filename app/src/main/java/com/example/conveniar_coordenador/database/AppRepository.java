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

    // Usamos um ExecutorService para rodar tarefas pesadas de banco de dados fora da Thread da UI
    private ExecutorService executorService;

    public AppRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        dao = db.appDao();

        // O Room já sabe que LiveData roda em background, então é seguro chamar direto
        todosProjetos = dao.getTodosProjetosLive();
        todosPedidos = dao.getTodosPedidosLive();

        executorService = Executors.newSingleThreadExecutor();
    }

    // A Activity vai chamar este método para desenhar a tela
    public LiveData<List<ProjetoEntity>> getTodosProjetos() {
        return todosProjetos;
    }

    public LiveData<List<PedidoEntity>> getTodosPedidos() {
        return todosPedidos;
    }

    public LiveData<List<PedidoEntity>> getPagamentoBolsa() {
        return dao.getPedidosPorTipoLive("Pagamento Bolsa");
    }

    // 2. Pagamento Pessoa Física
    public LiveData<List<PedidoEntity>> getPagamentoPessoaFisica() {
        return dao.getPedidosPorTipoLive("Pagamento Pessoa Física");
    }

    // 3. Pagamento Pessoa Jurídica
    public LiveData<List<PedidoEntity>> getPagamentoPessoaJuridica() {
        return dao.getPedidosPorTipoLive("Pagamento Pessoa Jurídica");
    }

    // 4. Pedido de Adiantamento
    public LiveData<List<PedidoEntity>> getPedidoAdiantamento() {
        return dao.getPedidosPorTipoLive("Pedido de Adiantamento");
    }

    // 5. Pedido de Reembolso
    public LiveData<List<PedidoEntity>> getPedidoReembolso() {
        return dao.getPedidosPorTipoLive("Pedido de Reembolso");
    }

    // 6. Pagamento Bolsa em Lote
    public LiveData<List<PedidoEntity>> getPagamentoBolsaEmLote() {
        return dao.getPedidosPorTipoLive("Pagamento Bolsa em Lote");
    }

    // 7. Entrada de Receita
    public LiveData<List<PedidoEntity>> getEntradaReceita() {
        return dao.getPedidosPorTipoLive("Entrada de Receita");
    }

    // 8. Pagamento Diária
    public LiveData<List<PedidoEntity>> getPagamentoDiaria() {
        return dao.getPedidosPorTipoLive("Pagamento Diária");
    }

    // 9. Pedido de Acerto de Adiantamento
    public LiveData<List<PedidoEntity>> getAcertoAdiantamento() {
        return dao.getPedidosPorTipoLive("Pedido de Acerto de Adiantamento");
    }

    // 10. Pedido de Reconhecimento Receita
    public LiveData<List<PedidoEntity>> getReconhecimentoReceita() {
        return dao.getPedidosPorTipoLive("Pedido de Reconhecimento Receita");
    }

    // 11. Pedido de Transferência entre Projetos
    public LiveData<List<PedidoEntity>> getTransferenciaProjetos() {
        return dao.getPedidosPorTipoLive("Pedido de Transferência entre Projetos");
    }

    // --- Futuramente, a lógica de sincronização da API (Coordenador) virá para cá ---
    // Exemplo de como você vai inserir dados usando o Repository sem travar a tela:
    public void salvarProjeto(ProjetoEntity projeto) {
        executorService.execute(() -> {
            dao.salvarProjeto(projeto);
        });
    }
}
