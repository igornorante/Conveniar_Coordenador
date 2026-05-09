package com.example.conveniar_coordenador;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.conveniar_coordenador.model.Projeto;
import com.google.android.material.navigation.NavigationView;

import com.example.conveniar_coordenador.DAO.ProjetoDAO;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProjetosActivity extends AppCompatActivity {

    private String token;
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

        token = getIntent().getStringExtra("TOKEN");

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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ImageView btnMenu = findViewById(R.id.menu_header);

        //Tratamento de clique das opções do menu lateral
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            //Opção Sair
            if (id == R.id.opc_sair) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);

                drawer.closeDrawer(GravityCompat.START);
                finish();
                return true;
            }
            else if (id == R.id.opc_projetos) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            else if (id == R.id.opc_pedidos) {
                Intent intent = new Intent(this, PedidosActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);

                drawer.closeDrawer(GravityCompat.START);
                return true;
            }

            else if(id == R.id.opc_saldo){
                Intent intent = new Intent(this, SaldoActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);

                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            return true;
        });

        btnMenu.setOnClickListener(v -> {
            drawer.openDrawer(GravityCompat.START);
        });

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

                        Log.d("FLUXO_API", "Quantidade de projetos recebidos: " + projetosRecebidos.size());

                        for (Projeto projeto : projetosRecebidos) {
                            Log.d("FLUXO_API",
                                    "Projeto -> codProjeto: " + projeto.getCodConvenio()
                                            + " | nome: " + projeto.getNomeConvenio()
                                            + " | status: " + projeto.getNomeStatus()
                                            + " | saldo: " + projeto.getSaldo());
                        }

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