package com.example.conveniar_coordenador;

import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/*
    Importante: é necessário sobescrever onTokenGerado e onErro
 */
public class TokenGenerator {


    public interface TokenCallback {
        void onTokenGerado(String token);
        void onErro(String mensagem);
    }

    public static void gerarToken(String username, String password, TokenCallback callback) {
        String url = BuildConfig.BASE_URL + "token/coordenador";
        String apiKey = BuildConfig.API_KEY;

        // 1. Auntenticação
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        String basicAuthHeader = "Basic " + encodedCredentials;

        // 2. Conexão, importante, na versão atual do código estamos ignorando o certificado do site
        // mais sobre isso na Classe ApiClient
        OkHttpClient client = ApiClient.getInstance();


        // 3. Monta a requisição com todos os headers
        Request request = new Request.Builder()
                .url(url)
                .addHeader("accept", "application/json")
                .addHeader("X-API-KEY", apiKey)
                .addHeader("Authorization", basicAuthHeader)
                .get()
                .build();

        //Caso estejam com problemas talvez seja uma boa descomentar essa linha
        //Log.d("API_TOKEN", "Enviando requisição ignorando SSL...");

        // 4. Executa a requisição em segundo plano
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("API_TOKEN", "Falha na requisição: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        // Pega o corpo da resposta como String
                        String jsonResposta = response.body().string();

                        //caso tenham problemas talvez seja uma boa descomentar essa linha
                        //Log.d("API_DEBUG", "JSON Bruto do Servidor: " + jsonResposta);

                        // Converte a String para um Objeto JSON do Android
                        JSONObject jsonObject = new JSONObject(jsonResposta);

                        // Nota: Acreditam que na documentação estava acessToken com um 'c' só? levou um tempo pra achar esse erro
                        String tokenExtraido = jsonObject.getString("accessToken");

                        // Devolve o token
                        callback.onTokenGerado(tokenExtraido);

                    } catch (Exception e) {
                        callback.onErro("Erro ao ler o JSON: " + e.getMessage());
                    }
                } else {
                    Log.e("API_TOKEN", "Erro do servidor. Código: " + response.code());
                }
            }
        });
    }
}