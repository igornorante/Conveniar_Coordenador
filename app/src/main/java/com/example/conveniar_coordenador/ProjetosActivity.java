package com.example.conveniar_coordenador;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.conveniar_coordenador.DAO.ProjetoDAO;
import com.example.conveniar_coordenador.model.Projeto;

import java.util.ArrayList;
import java.util.List;

public class ProjetosActivity extends BaseActivity {

    private ListView listProjetos;
    private ArrayAdapter<Projeto> adapter;
    private final List<Projeto> listaProjetos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_projetos);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupDrawer();

        listProjetos = findViewById(R.id.list_projetos);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                listaProjetos
        );

        listProjetos.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (token == null || token.isEmpty()) {
            Log.e("FLUXO_API", "Token não encontrado");
            return;
        }

        carregarProjetos();
    }

    private void carregarProjetos() {
        Coordenador.getProjetos(token, null, "Ativo", 1, 50, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                Log.e("FLUXO_API", "Erro ao buscar projetos: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Log.d("FLUXO_API", "JSON bruto projetos: " + json);

                    try {
                        List<Projeto> projetosRecebidos = ProjetoDAO.fromJson(json);

                        runOnUiThread(() -> {
                            listaProjetos.clear();
                            listaProjetos.addAll(projetosRecebidos);
                            adapter.notifyDataSetChanged();
                        });

                    } catch (Exception e) {
                        Log.e("FLUXO_API", "Erro ao processar JSON: " + e.getMessage(), e);
                    }

                } else {
                    Log.e("FLUXO_API", "Falha ao carregar projetos. Código: " + response.code());
                }
            }
        });
    }
}
