package com.example.conveniar_coordenador.DAO;

import android.util.Log;
import com.example.conveniar_coordenador.model.PendenciaGrupo;
import com.example.conveniar_coordenador.model.PendenciaStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PendenciaDAO {

    public static PendenciaGrupo parseListaParaGrupo(String titulo, String json) {
        try {
            JSONArray array = null;
            
            // Verifica se o JSON é um Array direto ou um Objeto que contém a lista
            if (json.trim().startsWith("{")) {
                JSONObject obj = new JSONObject(json);
                if (obj.has("data")) array = obj.getJSONArray("data");
                else if (obj.has("itens")) array = obj.getJSONArray("itens");
                else if (obj.has("pedidos")) array = obj.getJSONArray("pedidos");
            } else if (json.trim().startsWith("[")) {
                array = new JSONArray(json);
            }

            if (array == null || array.length() == 0) {
                return new PendenciaGrupo(titulo, 0, new ArrayList<>());
            }

            int total = array.length();
            Map<String, Integer> contagemMap = new HashMap<>();
            
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                // Tenta buscar o status por diferentes chaves comuns na API
                String status = item.optString("nomeStatus", item.optString("situacao", "Outros"));
                
                int count = contagemMap.getOrDefault(status, 0);
                contagemMap.put(status, count + 1);
            }

            List<PendenciaStatus> statusList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : contagemMap.entrySet()) {
                String nomeStatus = entry.getKey();
                String tipo = mapearTipoStatus(nomeStatus);
                statusList.add(new PendenciaStatus(nomeStatus, entry.getValue(), tipo));
            }

            return new PendenciaGrupo(titulo, total, statusList);

        } catch (Exception e) {
            Log.e("PENDENCIA_DAO", "Erro ao processar " + titulo + ": " + e.getMessage());
            return new PendenciaGrupo(titulo, 0, new ArrayList<>());
        }
    }

    private static String mapearTipoStatus(String status) {
        String s = status.toLowerCase();
        if (s.contains("envio") || s.contains("aberto")) return "envio";
        if (s.contains("aprova") || s.contains("analise")) return "aprovacao";
        if (s.contains("devolvido") || s.contains("ajuste") || s.contains("rejeitado")) return "ajuste";
        return "outros";
    }
}
