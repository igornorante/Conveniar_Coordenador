package com.example.conveniar_coordenador.DAO;

import android.util.Log;

import com.example.conveniar_coordenador.model.Projeto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProjetoDAO {

    public static List<Projeto> fromJson(String json) throws Exception {
        JSONArray array = new JSONArray(json);

        List<Projeto> projetos = new ArrayList<>();

        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);

            Log.d("FLUXO_API", "Projeto JSON: " + obj.toString());

            Projeto projeto = new Projeto(
                    obj.optInt("codConvenio"),
                    obj.optString("nomeConvenio"),
                    obj.optDouble("saldo"),
                    obj.optString("nomeStatus"),
                    obj.optString("coordenador"),
                    obj.optString("dataVigencia")
            );

            projetos.add(projeto);
        }

        return projetos;
    }
}