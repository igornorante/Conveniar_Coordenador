package com.example.conveniar_coordenador;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.conveniar_coordenador.DAO.ProjetoDAO;
import com.example.conveniar_coordenador.model.Projeto;
import com.google.android.material.navigation.NavigationView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaldoActivity extends AppCompatActivity {

    private String token;

    private Spinner spinnerProjetos;
    private Button btnConsultar;

    private TextView txtNomeProjeto;
    private TextView txtCoordenador;
    private TextView txtVigencia;
    private TextView txtStatus;
    private TextView txtSaldo;

    private final List<Projeto> listaProjetos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_saldo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        token = getIntent().getStringExtra("TOKEN");

        spinnerProjetos = findViewById(R.id.spinner_projetos);
        btnConsultar = findViewById(R.id.btn_consultar);

        txtNomeProjeto = findViewById(R.id.txt_nome_projeto);
        txtCoordenador = findViewById(R.id.txt_coordenador);
        txtVigencia = findViewById(R.id.txt_vigencia);
        txtStatus = findViewById(R.id.txt_status);
        txtSaldo = findViewById(R.id.txt_saldo);

        btnConsultar.setOnClickListener(v -> exibirProjetoSelecionado());
    }

    @Override
    protected void onResume() {
        super.onResume();

        configurarMenuLateral();

        if (token == null || token.isEmpty()) {
            Log.e("FLUXO_API", "Token não encontrado");
            return;
        }

        carregarProjetos();
    }

    private void configurarMenuLateral() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ImageView btnMenu = findViewById(R.id.menu_header);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        btnMenu.setOnClickListener(v ->
                drawer.openDrawer(GravityCompat.START)
        );

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.opc_projetos) {
                Intent intent = new Intent(this, ProjetosActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
            } else if (id == R.id.opc_extrato) {
                Intent intent = new Intent(this, ExtratoActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
            } else if (id == R.id.opc_saldo) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.opc_consultas) {
                Intent intent = new Intent(this, ConsultaActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
            } else if (id == R.id.opc_pedidos) {
                Intent intent = new Intent(this, PedidosActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
            } else if (id == R.id.opc_sair) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finishAffinity();
                return true;
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void carregarProjetos() {
        Coordenador.getProjetos(token, null, "Ativo", 1, 50, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                Log.e("FLUXO_API", "Erro ao buscar projetos: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response)
                    throws java.io.IOException {

                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();

                    try {
                        List<Projeto> projetos = ProjetoDAO.fromJson(json);

                        runOnUiThread(() -> {
                            listaProjetos.clear();
                            listaProjetos.addAll(projetos);
                            preencherSpinner();
                        });

                    } catch (Exception e) {
                        Log.e("FLUXO_API", "Erro ao processar projetos: " + e.getMessage());
                    }

                } else {
                    Log.e("FLUXO_API",
                            "Falha ao carregar projetos. Código: " + response.code());
                }
            }
        });
    }

    private void preencherSpinner() {
        List<String> nomes = new ArrayList<>();

        for (Projeto projeto : listaProjetos) {
            nomes.add(projeto.getCodConvenio() + " - " + projeto.getNomeConvenio());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nomes
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerProjetos.setAdapter(adapter);

        if (!listaProjetos.isEmpty()) {
            exibirProjetoSelecionado();
        }
    }

    private void exibirProjetoSelecionado() {
        if (listaProjetos.isEmpty()) return;

        Projeto projeto = listaProjetos.get(
                spinnerProjetos.getSelectedItemPosition()
        );

        NumberFormat formatoMoeda =
                NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        txtNomeProjeto.setText(
                projeto.getCodConvenio() + " - " + projeto.getNomeConvenio()
        );

        txtCoordenador.setText("Coordenador: " + projeto.getCoordenador());

        txtVigencia.setText("Vigência: " + projeto.getDataVigencia());

        txtStatus.setText("Status: " + projeto.getNomeStatus());

        txtSaldo.setText(formatoMoeda.format(projeto.getSaldo()));
    }
}