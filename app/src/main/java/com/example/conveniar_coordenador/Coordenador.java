package com.example.conveniar_coordenador;

import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Request;


//Classe utilizada para organizar as requisições, pode evoluir para guardar dados do Coordenador
public class Coordenador {

    //private static final String BASE_URL = "https://api.conveniar.com.br/coordenador/";
    //private static final String API_KEY = "7e61b6bb-6841-415f-954e-5e2ba445cc7c";

    private static final String BASE_URL = BuildConfig.BASE_URL + "coordenador/";
    private static final String API_KEY = BuildConfig.API_KEY;



    // 1. Rota: /usuario
    public static void getUsuario(String token, Callback callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "usuario")
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("X-API-KEY", API_KEY)
                .get()
                .build();

        // Usa o cliente centralizado para fazer a chamada
        ApiClient.getInstance().newCall(request).enqueue(callback);
    }

    // 2. Rota: /eventos-usuario com parâmetros de paginação
    public static void getEventosUsuario(String token, int pagina, int limite, Callback callback) {
        // Monta a URL com os parâmetros de forma segura (sem concatenar string manualmente)
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "eventos-usuario").newBuilder();
        urlBuilder.addQueryParameter("pagina", String.valueOf(pagina));
        urlBuilder.addQueryParameter("limite", String.valueOf(limite));
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("X-API-KEY", API_KEY)
                .get()
                .build();

        ApiClient.getInstance().newCall(request).enqueue(callback);
    }


}
