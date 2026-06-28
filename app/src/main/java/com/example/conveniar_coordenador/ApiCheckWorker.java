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
import java.util.concurrent.atomic.AtomicBoolean;

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
        Log.d("WORKER_DEBUG", "Sincronização iniciada...");
        SharedPreferences prefs = SecurePrefsManager.get(getApplicationContext());

        if (prefs == null) {
            Log.e("WORKER_API", "Erro: SharedPreferences não disponível.");
            return Result.failure();
        }

        String user = prefs.getString("usuario_login", "");
        String pass = prefs.getString("senha_login", "");

        if (user.isEmpty() || pass.isEmpty()) {
            Log.e("WORKER_API", "Erro: Credenciais ausentes.");
            return Result.failure();
        }

        AppDAO dao = AppDatabase.getDatabase(getApplicationContext()).appDao();
        CountDownLatch latch = new CountDownLatch(3);
        AtomicBoolean hasError = new AtomicBoolean(false);

        TokenGenerator.gerarToken(user, pass, new TokenGenerator.TokenCallback() {
            @Override
            public void onTokenGerado(String novoToken) {
                prefs.edit().putString("token_acesso", novoToken).apply();

                // 1. Sincronizar Projetos
                Coordenador.getProjetos(novoToken, null, null, 1, 150, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("WORKER_DEBUG", "Falha API Projetos", e);
                        hasError.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String json = response.body().string();
                                Log.d("WORKER_DEBUG", "Projetos recebidos: " + json.substring(0, Math.min(json.length(), 100)));
                                JSONArray array = new JSONArray(json);
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    String id = obj.optString("codConvenio", obj.optString("codigoProjeto"));
                                    if (id.isEmpty()) continue;
                                    
                                    ProjetoEntity p = new ProjetoEntity();
                                    p.idProjeto = id;
                                    p.situacao = obj.optString("nomeStatus", "Ativo");
                                    p.jsonOriginal = obj.toString();
                                    dao.salvarProjeto(p);
                                }
                            } else {
                                Log.e("WORKER_DEBUG", "Erro na resposta de Projetos: " + response.code());
                                hasError.set(true);
                            }
                        } catch (Exception e) {
                            Log.e("WORKER_DEBUG", "Erro processar projetos", e);
                        } finally {
                            latch.countDown();
                        }
                    }
                });

                // 2. Sincronizar Pedidos de Compra/Serviço (flagMeusPedidos = false para ver tudo)
                Coordenador.getPedidosCompraServico(novoToken, null, null, null, null, null, null, null, null, false, null, null, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("WORKER_DEBUG", "Falha API Pedidos Compra", e);
                        hasError.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String json = response.body().string();
                                Log.d("WORKER_DEBUG", "Pedidos Compra recebidos: " + json.substring(0, Math.min(json.length(), 100)));
                                JSONArray array = new JSONArray(json);
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    String num = obj.optString("numeroPedido", obj.optString("numPedido", ""));
                                    if (num.isEmpty()) continue;

                                    PedidoEntity p = new PedidoEntity();
                                    p.numPedido = num;
                                    p.codPedido = obj.optInt("codPedido", obj.optInt("codigoPedido", 0));
                                    p.situacao = obj.optString("situacao", obj.optString("nomeStatus", "Pendente"));
                                    p.codProjeto = obj.optString("projeto", obj.optString("codProjeto", ""));
                                    p.jsonOriginal = obj.toString();
                                    dao.salvarPedido(p);
                                }
                            } else {
                                Log.e("WORKER_DEBUG", "Erro na resposta de Pedidos: " + response.code());
                                hasError.set(true);
                            }
                        } catch (Exception e) {
                            Log.e("WORKER_DEBUG", "Erro processar pedidos compra", e);
                        } finally {
                            latch.countDown();
                        }
                    }
                });

                // 3. Sincronizar Pedidos de Pagamento
                Coordenador.getPedidosPagamento(novoToken, null, null, null, null, null, null, null, null, false, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("WORKER_DEBUG", "Falha API Pedidos Pagamento", e);
                        hasError.set(true);
                        latch.countDown();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String json = response.body().string();
                                Log.d("WORKER_DEBUG", "Pagamentos recebidos: " + json.substring(0, Math.min(json.length(), 100)));
                                JSONArray array = new JSONArray(json);
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject obj = array.getJSONObject(i);
                                    String num = obj.optString("numeroPedido", obj.optString("numPedido", ""));
                                    if (num.isEmpty()) continue;

                                    PedidoPagamentoEntity p = new PedidoPagamentoEntity();
                                    p.numeroPedido = num;
                                    p.nomeTipoPedido = obj.optString("nomeTipoPedido", "Pagamento");
                                    p.nomeStatus = obj.optString("nomeStatus", "Pendente");
                                    p.nomeFavorecido = obj.optString("nomeFavorecido", "");
                                    p.jsonOriginal = obj.toString();
                                    dao.salvarPedidoPagamento(p);
                                }
                            } else {
                                Log.e("WORKER_DEBUG", "Erro na resposta de Pagamentos: " + response.code());
                                hasError.set(true);
                            }
                        } catch (Exception e) {
                            Log.e("WORKER_DEBUG", "Erro processar pagamentos", e);
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }

            @Override
            public void onErro(String erro) {
                Log.e("WORKER_DEBUG", "Erro na geração do token: " + erro);
                hasError.set(true);
                latch.countDown();
                latch.countDown();
                latch.countDown();
            }
        });

        try {
            latch.await(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return Result.failure();
        }

        return hasError.get() ? Result.retry() : Result.success();
    }
}
