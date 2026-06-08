package com.example.conveniar_coordenador;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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

        CountDownLatch latch = new CountDownLatch(1);
        final Result[] resultadoFinal = new Result[]{Result.retry()};

        // 2. Gera um novo token
        TokenGenerator.gerarToken(user, pass, new TokenGenerator.TokenCallback() {
            @Override
            public void onTokenGerado(String novoToken) {
                prefs.edit().putString("token_acesso", novoToken).apply();

                // 3. Busca os PROJETOS
                Coordenador.getProjetos(novoToken, null, "Ativo", 1, 50, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("WORKER_API", "Erro ao buscar projetos: " + e.getMessage());
                        resultadoFinal[0] = Result.retry();
                        latch.countDown(); // Libera o Worker (Falha na rede)
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                String jsonProjetos = response.body().string();
                                String jsonProjetosAntigo = prefs.getString("ultima_resposta_projetos", "");

                                if (!jsonProjetos.equals(jsonProjetosAntigo)) {
                                    prefs.edit().putString("ultima_resposta_projetos", jsonProjetos).apply();

                                    if (!jsonProjetosAntigo.isEmpty()) {
                                        NotificationHelper notificacao = new NotificationHelper(getApplicationContext());
                                        notificacao.enviarNotificacao(
                                                "Novos Projetos",
                                                "Sua lista de projetos ativos foi atualizada."
                                        );
                                    }
                                }
                            } else {
                                Log.e("WORKER_API", "Falha na requisição de projetos. Código: " + response.code());
                            }
                        } catch (Exception e) {
                            Log.e("WORKER_API", "Erro ao processar JSON de Projetos: " + e.getMessage());
                        }

                        // 4. AGORA BUSCA OS PEDIDOS (Usamos "true" no 10º parâmetro para flagMeusPedidos)
                        Coordenador.getPedidosCompraServico(
                                novoToken, null, null, null, null, null, null, null, null, true, null, null,
                                new Callback() {
                                    @Override
                                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                        Log.e("WORKER_API", "Erro ao buscar pedidos: " + e.getMessage());
                                        resultadoFinal[0] = Result.retry();
                                        latch.countDown(); // Libera o Worker
                                    }

                                    @Override
                                    public void onResponse(@NonNull Call call, @NonNull Response responsePedidos) throws IOException {
                                        try {
                                            if (responsePedidos.isSuccessful() && responsePedidos.body() != null) {
                                                String jsonPedidos = responsePedidos.body().string();
                                                String jsonPedidosAntigo = prefs.getString("ultima_resposta_pedidos", "");

                                                if (!jsonPedidos.equals(jsonPedidosAntigo)) {
                                                    prefs.edit().putString("ultima_resposta_pedidos", jsonPedidos).apply();

                                                    if (!jsonPedidosAntigo.isEmpty()) {
                                                        NotificationHelper notificacao = new NotificationHelper(getApplicationContext());
                                                        notificacao.enviarNotificacao(
                                                                "Atualização em Pedidos",
                                                                "Houve uma alteração nos seus pedidos."
                                                        );
                                                    }
                                                }
                                                resultadoFinal[0] = Result.success(); // Ambas as consultas foram feitas
                                            } else {
                                                Log.e("WORKER_API", "Falha na requisição de pedidos. Código: " + responsePedidos.code());
                                                resultadoFinal[0] = Result.retry();
                                            }
                                        } catch (Exception e) {
                                            Log.e("WORKER_API", "Erro ao processar JSON de Pedidos: " + e.getMessage());
                                            resultadoFinal[0] = Result.failure();
                                        } finally {
                                            // FINALIZA A CORRENTE E LIBERA O WORKER
                                            latch.countDown();
                                        }
                                    }
                                }
                        );
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