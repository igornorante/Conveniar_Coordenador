package com.example.conveniar_coordenador;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.conveniar_coordenador.database.AppDAO;
import com.example.conveniar_coordenador.database.AppDatabase;
import com.example.conveniar_coordenador.database.PedidoEntity;
import com.example.conveniar_coordenador.database.PedidoPagamentoEntity;
import com.example.conveniar_coordenador.database.ProjetoEntity;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ApiCheckWorker extends Worker {

    public ApiCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("WORKER_DEBUG", "1. Worker INICIADO. Preparando para ler SharedPreferences.");
        SharedPreferences prefs = SecurePrefsManager.get(getApplicationContext());

        if (prefs == null) {
            Log.e("WORKER_API", "Falha ao iniciar o SharedPreferences Criptografado.");
            return Result.failure();
        }

        String user = prefs.getString("usuario_login", "");
        String pass = prefs.getString("senha_login", "");

        if (user.isEmpty() || pass.isEmpty()) {
            Log.e("WORKER_API", "Credenciais não encontradas. Abortando.");
            return Result.failure();
        }

        // Instancia o banco de dados e o DAO
        AppDAO dao = AppDatabase.getDatabase(getApplicationContext()).appDao();

        CountDownLatch latch = new CountDownLatch(1);
        final Result[] resultadoFinal = new Result[]{Result.retry()};

        TokenGenerator.gerarToken(user, pass, new TokenGenerator.TokenCallback() {
            @Override
            public void onTokenGerado(String novoToken) {
                prefs.edit().putString("token_acesso", novoToken).apply();

                // --- 1. BUSCA E PROCESSA OS PROJETOS ---
                Coordenador.getProjetos(novoToken, null, "Ativo", 1, 50, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("WORKER_API", "Erro ao buscar projetos: " + e.getMessage());
                        resultadoFinal[0] = Result.retry();
                        latch.countDown();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        Log.d("WORKER_DEBUG", "4. Resposta da API de Projetos recebida. Código: " + response.code());
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                //versão nova

                                String jsonProjetos = response.body().string();
                                Log.d("WORKER_DEBUG", "JSON de Projetos recebido: " + jsonProjetos.substring(0, Math.min(jsonProjetos.length(), 200)) + "...");
                                JSONArray arrayProjetos = new JSONArray(jsonProjetos);

                                boolean isPrimeiraSyncProjetos = prefs.getBoolean("primeira_sync_projetos", true);
                                List<String> nomesProjetosAlterados = new ArrayList<>(); // Lista para guardar os nomes

                                for (int i = 0; i < arrayProjetos.length(); i++) {
                                    JSONObject obj = arrayProjetos.getJSONObject(i);
                                    String idProjeto = obj.optString("codConvenio");
                                    String situacaoNova = obj.optString("nomeStatus", "Sem Status");
                                    String nomeProjeto = obj.optString("nomeConvenio", "Projeto " + idProjeto);

                                    if (idProjeto.isEmpty()) continue;

                                    ProjetoEntity projetoSalvo = dao.getProjetoById(idProjeto);
                                    boolean precisaAcao = false;

                                    if (projetoSalvo == null) {
                                        precisaAcao = true; // Projeto novo
                                        nomesProjetosAlterados.add(nomeProjeto);
                                    } else if (!projetoSalvo.situacao.equals(situacaoNova)) {
                                        precisaAcao = true; // Status mudou
                                        nomesProjetosAlterados.add(nomeProjeto);
                                    }

                                    ProjetoEntity novoProjeto = new ProjetoEntity();
                                    novoProjeto.idProjeto = idProjeto;
                                    novoProjeto.situacao = situacaoNova;
                                    novoProjeto.precisaAtencao = (projetoSalvo != null && projetoSalvo.precisaAtencao) || precisaAcao;
                                    novoProjeto.jsonOriginal = obj.toString();

                                    dao.salvarProjeto(novoProjeto);
                                }

                                // Dispara a notificação inteligente
                                if (!nomesProjetosAlterados.isEmpty() && !isPrimeiraSyncProjetos) {
                                    String mensagem;
                                    if (nomesProjetosAlterados.size() == 1) {
                                        mensagem = "O projeto '" + nomesProjetosAlterados.get(0) + "' teve alterações.";
                                    } else {
                                        mensagem = nomesProjetosAlterados.size() + " projetos tiveram alterações, incluindo '" + nomesProjetosAlterados.get(0) + "'.";
                                    }

                                    NotificationHelper notificacao = new NotificationHelper(getApplicationContext());
                                    notificacao.enviarNotificacao("Atualização em Projetos", mensagem);
                                }

                                if (isPrimeiraSyncProjetos) {
                                    prefs.edit().putBoolean("primeira_sync_projetos", false).apply();
                                }
                            }
                        } catch (Exception e) {
                            Log.e("WORKER_API", "Erro ao processar Projetos no banco: " + e.getMessage());
                        }

                        // --- 2. BUSCA E PROCESSA OS PEDIDOS ---
                        Coordenador.getPedidosCompraServico(
                                novoToken, null, null, null, null, null, null, null, null, true, null, null,
                                new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        Log.e("WORKER_API", "Erro ao buscar pedidos: " + e.getMessage());
                                        resultadoFinal[0] = Result.retry();
                                        latch.countDown();
                                    }

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response responsePedidos) throws IOException {
                                        try {
                                            if (responsePedidos.isSuccessful() && responsePedidos.body() != null) {
                                                Log.d("WORKER_DEBUG", "6. Resposta da API de Pedidos recebida. Código: " + responsePedidos.code());
                                                String jsonPedidos = responsePedidos.body().string();
                                                Log.d("WORKER_DEBUG", "JSON de Pedidos recebido: " + jsonPedidos.substring(0, Math.min(jsonPedidos.length(), 200)) + "...");
                                                JSONArray arrayPedidos = new JSONArray(jsonPedidos);

                                                boolean isPrimeiraSyncPedidos = prefs.getBoolean("primeira_sync_pedidos", true);
                                                List<String> nomesPedidosAlterados = new ArrayList<>(); // Lista para guardar os nomes


                                                //INSERIR AQUI AS CHAVES DO JSON QUE DEVEM VIRAR COLUNAS NO DB
                                                for (int i = 0; i < arrayPedidos.length(); i++) {
                                                    JSONObject obj = arrayPedidos.getJSONObject(i);
                                                    String numPedido = obj.optString("numeroPedido"); // Chave corrigida!
                                                    String situacaoNova = obj.optString("situacao", obj.optString("status", "semStatus")); // Mantenha assim se não soubermos a exata ainda
                                                    String nomePedido = obj.optString("produto", "Pedido #" + numPedido); // Chave do nome corrigida!
                                                    String codProjeto = obj.optString("projeto", "-1");

                                                    if (numPedido.isEmpty()) {
                                                        Log.w("WORKER_DEBUG", "ALERTA: Pedido ignorado porque 'numeroPedido' está vazio! Dados crus: " + obj.toString());
                                                        continue;
                                                    }

                                                    PedidoEntity pedidoSalvo = dao.getPedidoById(numPedido);
                                                    boolean precisaAcao = false;

                                                    if (pedidoSalvo == null) {
                                                        precisaAcao = true; // Pedido novo
                                                        nomesPedidosAlterados.add(nomePedido);
                                                    } else if (!pedidoSalvo.situacao.equals(situacaoNova)) {
                                                        precisaAcao = true; // Status mudou
                                                        nomesPedidosAlterados.add(nomePedido);
                                                    }

                                                    PedidoEntity novoPedido = new PedidoEntity();
                                                    novoPedido.numPedido = numPedido;
                                                    novoPedido.situacao = situacaoNova;
                                                    novoPedido.codProjeto = codProjeto;
                                                    novoPedido.precisaAtencao = (pedidoSalvo != null && pedidoSalvo.precisaAtencao) || precisaAcao;
                                                    novoPedido.jsonOriginal = obj.toString();

                                                    dao.salvarPedido(novoPedido);
                                                }

                                                if (!nomesPedidosAlterados.isEmpty() && !isPrimeiraSyncPedidos) {
                                                    String mensagem;
                                                    if (nomesPedidosAlterados.size() == 1) {
                                                        mensagem = "O pedido '" + nomesPedidosAlterados.get(0) + "' teve alterações.";
                                                    } else {
                                                        mensagem = nomesPedidosAlterados.size() + " pedidos tiveram alterações, incluindo '" + nomesPedidosAlterados.get(0) + "'.";
                                                    }

                                                    NotificationHelper notificacao = new NotificationHelper(getApplicationContext());
                                                    notificacao.enviarNotificacao("Atualização em Pedidos", mensagem);
                                                }

                                                if (isPrimeiraSyncPedidos) {
                                                    prefs.edit().putBoolean("primeira_sync_pedidos", false).apply();
                                                }

                                                resultadoFinal[0] = Result.success();

                                            } else {
                                                resultadoFinal[0] = Result.retry();
                                            }
                                        } catch (Exception e) {
                                            Log.e("WORKER_API", "Erro ao processar Pedidos no banco: " + e.getMessage());
                                            resultadoFinal[0] = Result.failure();
                                        } finally {
                                            latch.countDown();
                                        }
                                    }
                                }
                        );

                        // --- 3. BUSCA E PROCESSA OS PEDIDOS DE PAGAMENTO ---
                        // Passando os parâmetros na mesma ordem do seu método getPedidosPagamento:
                        // token, nomeTipoPedido, codProjeto, numPedido, nomeFavo, dataInicial, valorPedido, status, dataFinal, meusPedidos, callback
                        Coordenador.getPedidosPagamento(novoToken, null, null, null, null, null, null, null, null, true, new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Log.e("WORKER_API", "Erro ao buscar pedidos de pagamento: " + e.getMessage());
                                resultadoFinal[0] = Result.retry();
                                latch.countDown();
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                try {
                                    if (response.isSuccessful() && response.body() != null) {
                                        String jsonPagamentos = response.body().string();
                                        JSONArray arrayPagamentos = new JSONArray(jsonPagamentos);

                                        boolean isPrimeiraSync = prefs.getBoolean("primeira_sync_pagamentos", true);
                                        List<String> nomesAlterados = new ArrayList<>();

                                        for (int i = 0; i < arrayPagamentos.length(); i++) {
                                            JSONObject obj = arrayPagamentos.getJSONObject(i);

                                            // Lendo as chaves exatas do seu JSON
                                            String numeroPedido = obj.optString("numeroPedido");
                                            String nomeStatus = obj.optString("nomeStatus", "Sem Status");
                                            String tipoPedido = obj.optString("nomeTipoPedido", "Sem Tipo");
                                            String nomeFavorecido = obj.optString("nomeFavorecido", "Sem Favorecido");

                                            // Se não tiver ID válido, pula para evitar erro no banco
                                            if (numeroPedido.isEmpty() || numeroPedido.equals("0")) {
                                                Log.w("WORKER_DEBUG", "Pedido de Pagamento ignorado - numeroPedido inválido: " + obj.toString());
                                                continue;
                                            }

                                            PedidoPagamentoEntity pedidoSalvo = dao.getPedidoPagamentoById(numeroPedido);
                                            boolean precisaAcao = false;

                                            if (pedidoSalvo == null) {
                                                precisaAcao = true; // Novo pagamento
                                                nomesAlterados.add(numeroPedido + " (" + tipoPedido + ")");
                                            } else if (!pedidoSalvo.nomeStatus.equals(nomeStatus)) {
                                                precisaAcao = true; // Status mudou
                                                nomesAlterados.add(numeroPedido + " (" + tipoPedido + ")");
                                            }

                                            PedidoPagamentoEntity novoPagamento = new PedidoPagamentoEntity();
                                            novoPagamento.numeroPedido = numeroPedido;
                                            novoPagamento.nomeTipoPedido = tipoPedido;
                                            novoPagamento.nomeStatus = nomeStatus;
                                            novoPagamento.nomeFavorecido = nomeFavorecido;
                                            novoPagamento.jsonOriginal = obj.toString();
                                            novoPagamento.precisaAtencao = (pedidoSalvo != null && pedidoSalvo.precisaAtencao) || precisaAcao;

                                            dao.salvarPedidoPagamento(novoPagamento);
                                        }

                                        if (!nomesAlterados.isEmpty() && !isPrimeiraSync) {
                                            String msg = nomesAlterados.size() == 1
                                                    ? "Atualização no pagamento: " + nomesAlterados.get(0)
                                                    : nomesAlterados.size() + " pagamentos atualizados, incluindo: " + nomesAlterados.get(0);

                                            NotificationHelper notificacao = new NotificationHelper(getApplicationContext());
                                            notificacao.enviarNotificacao("Atualização de Pagamentos", msg);
                                        }

                                        if (isPrimeiraSync) {
                                            prefs.edit().putBoolean("primeira_sync_pagamentos", false).apply();
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("WORKER_API", "Erro ao processar Pedidos de Pagamento no banco: " + e.getMessage());
                                } finally {
                                    latch.countDown();
                                }
                            }
                        });

                    }
                });
            }

            @Override
            public void onErro(String erro) {
                Log.e("WORKER_API", "Falha ao gerar o token no background: " + erro);
                resultadoFinal[0] = Result.retry();
                latch.countDown();
            }
        });

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Result.failure();
        }

        return resultadoFinal[0];
    }
}