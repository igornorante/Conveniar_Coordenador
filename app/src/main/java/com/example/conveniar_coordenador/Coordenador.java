package com.example.conveniar_coordenador;

import android.util.Log;

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

    public static void getProjetos(
            String token,
            String projeto,
            String situacao,
            int pagina,
            int limite,
            Callback callback
    ) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "projetos").newBuilder();

        if (projeto != null && !projeto.isEmpty()) {
            urlBuilder.addQueryParameter("projeto", projeto);
        }

        if (situacao != null && !situacao.isEmpty()) {
            urlBuilder.addQueryParameter("situacao", situacao);
        }

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

    public static void getExtratoSimplificado(
            String token,
            int codProjeto,
            String dataInicio,
            String dataFim,
            Callback callback
    ) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "extrato-simplificado").newBuilder();

        urlBuilder.addQueryParameter("CodProjeto", String.valueOf(codProjeto));
        urlBuilder.addQueryParameter("DataInicial", dataInicio);
        urlBuilder.addQueryParameter("DataFinal", dataFim);

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("X-API-KEY", API_KEY)
                .get()
                .build();

        Log.d("FLUXO_API", "URL EXATA DO APP: " + request.url().toString());

        ApiClient.getInstance().newCall(request).enqueue(callback);
    }

    public static void getPedidosCompraServico(
            String token,
            String numPedido,
            String codProjeto,
            String dataInicial,
            String dataFinal,
            String nomeProduto,
            String numProcessoCompra,
            String numAfOs,
            String fornecedor,
            boolean meusPedidos,
            String solicitante,
            String situacao,
            Callback callback
    ) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "itens-compra").newBuilder();

        if (numPedido != null && !numPedido.isEmpty()) urlBuilder.addQueryParameter("numPedido", numPedido);
        if (codProjeto != null && !codProjeto.isEmpty()) urlBuilder.addQueryParameter("codProjeto", codProjeto);
        if (dataInicial != null && !dataInicial.isEmpty()) urlBuilder.addQueryParameter("dataInicial", dataInicial);
        if (dataFinal != null && !dataFinal.isEmpty()) urlBuilder.addQueryParameter("dataFinal", dataFinal);
        if (nomeProduto != null && !nomeProduto.isEmpty()) urlBuilder.addQueryParameter("nomeProduto", nomeProduto);
        if (numProcessoCompra != null && !numProcessoCompra.isEmpty()) urlBuilder.addQueryParameter("numProcessoCompra", numProcessoCompra);
        // Nota: numAfOs, fornecedor, solicitante não aparecem como filtros no Swagger de itens-compra, mas manteremos a estrutura se a API suportar
        urlBuilder.addQueryParameter("flagMeusPedidos", meusPedidos ? "S" : "N");
        if (situacao != null && !situacao.isEmpty()) urlBuilder.addQueryParameter("situacao", situacao);

        urlBuilder.addQueryParameter("pagina", "1");
        urlBuilder.addQueryParameter("limite", "200");

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("X-API-KEY", API_KEY)
                .get()
                .build();

        Log.d("FLUXO_API", "URL BUSCA PEDIDOS: " + request.url().toString());

        ApiClient.getInstance().newCall(request).enqueue(callback);
    }

    public static void getPedidosPagamento(
            String token,
            String nomeTipoPedido, // nomeTipoPedido
            String codProjeto, //projeto
            String numPedido, //numeroPedido
            String nomeFavo, //nomeFavorecido
            String dataInicial, //dataPedido
            String valorPedido, //valorPedido
            String status, //nomeStatus
            String dataFinal, //isso não faz parte do JSON que recebemos, a explicação está na parte dos filtros logo abaixo
            // String codPedido, //codPedido não faz parte dos filtros no momento
            // String dataVen, //dataVencimento não faz parte dos filtros da API no momento
            // String dataEnvio, //dataEnvio não faz parte dos filtros da API no momento
            // String nomeCoord, //nomeCoordenador não faz parte dos filtros da API
            boolean meusPedidos,
            Callback callback
    ) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BASE_URL + "pedidos-pagamento").newBuilder();

        //FILTROS DE PESQUISA DA API
        if (numPedido != null && !numPedido.isEmpty()) urlBuilder.addQueryParameter("numPedido", numPedido);
        if (codProjeto != null && !codProjeto.isEmpty()) urlBuilder.addQueryParameter("codProjeto", codProjeto);

        //detalhe o filtro de data inicial muito provavelmente funciona como pagamento efetuado depois da data
        //inserida, mas o de data final não sei se é em relação ao vencimento do pagamento ou se só filtra
        //pedidos que foram pagos antes da data, enfim se isso for importante em algum momento é bom conferir
        if (dataInicial != null && !dataInicial.isEmpty()) urlBuilder.addQueryParameter("dataInicial", dataInicial);
        if (dataFinal != null && !dataFinal.isEmpty()) urlBuilder.addQueryParameter("dataFinal", dataFinal);

        if (nomeTipoPedido != null && !nomeTipoPedido.isEmpty()) urlBuilder.addQueryParameter("nomeTipoPedido", nomeTipoPedido);
        if (status != null && !status.isEmpty()) urlBuilder.addQueryParameter("situacao", status);
        if (nomeFavo != null && !nomeFavo.isEmpty()) urlBuilder.addQueryParameter("favorecido", nomeFavo);

        urlBuilder.addQueryParameter("flagMeusPedidos", meusPedidos ? "S" : "N");

        urlBuilder.addQueryParameter("pagina", "1");
        urlBuilder.addQueryParameter("limite", "250");

        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + token)
                .addHeader("X-API-KEY", API_KEY)
                .get()
                .build();

        Log.d("FLUXO_API", "URL BUSCA PEDIDOS PAGAMENTO: " + request.url().toString());

        ApiClient.getInstance().newCall(request).enqueue(callback);
    }

}