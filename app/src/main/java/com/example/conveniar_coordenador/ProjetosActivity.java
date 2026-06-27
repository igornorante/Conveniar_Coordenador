package com.example.conveniar_coordenador;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.conveniar_coordenador.DAO.ProjetoDAO;
import com.example.conveniar_coordenador.databinding.ActivityProjetosBinding;
import com.example.conveniar_coordenador.model.Projeto;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ProjetosActivity extends BaseActivity {

    private ActivityProjetosBinding binding;
    private ProjetosAdapter adapter;
    private final List<Projeto> listaProjetos = new ArrayList<>();
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProjetosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupDrawer();

        // Configuração do RecyclerView
        binding.recyclerProjetos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjetosAdapter(listaProjetos);
        binding.recyclerProjetos.setAdapter(adapter);

        carregarProjetos();
    }

    private void carregarProjetos() {
        if (token == null) return;

        Coordenador.getProjetos(token, null, null, 1, 100, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ProjetosActivity.this, "Erro de conexão", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        List<Projeto> projetos = ProjetoDAO.fromJson(json);
                        runOnUiThread(() -> atualizarLista(projetos));
                    } catch (Exception e) {
                        Log.e("PROJETOS", "Erro ao processar JSON", e);
                    }
                }
            }
        });
    }

    private void atualizarLista(List<Projeto> projetos) {
        listaProjetos.clear();
        listaProjetos.addAll(projetos);
        adapter.notifyDataSetChanged();

        // Atualiza o bloco de resumo (Dashboard Style)
        double saldoTotal = 0;
        for (Projeto p : projetos) {
            saldoTotal += p.getSaldo();
        }

        binding.txtTotalSaldoProjetos.setText(fmt.format(saldoTotal));
        binding.txtTotalProjetosCount.setText(projetos.size() + " projetos encontrados");
    }
}
