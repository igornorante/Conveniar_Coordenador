package com.example.conveniar_coordenador.DAO;

import com.example.conveniar_coordenador.model.Pedido;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {
    public static List<Pedido> fromJson(String json) throws JSONException {
        List<Pedido> pedidos = new ArrayList<>();
        JSONArray array = new JSONArray(json);

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            Pedido pedido = new Pedido(
                obj.optInt("numeroPedido"),
                obj.optString("dataPedido"),
                obj.optString("projeto"),
                obj.optString("produto"),
                obj.optString("fornecedor"),
                obj.optString("nomeStatus"),
                obj.optDouble("valor")
            );
            pedidos.add(pedido);
        }
        return pedidos;
    }
}