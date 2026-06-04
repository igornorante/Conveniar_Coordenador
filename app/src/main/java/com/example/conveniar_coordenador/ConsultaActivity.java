package com.example.conveniar_coordenador;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.conveniar_coordenador.DAO.PedidoDAO;
import com.example.conveniar_coordenador.DAO.ProjetoDAO;
import com.example.conveniar_coordenador.model.Pedido;
import com.example.conveniar_coordenador.model.Projeto;
import com.google.android.material.navigation.NavigationView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ConsultaActivity extends AppCompatActivity {

    private String token;
    private final List<Projeto> listaProjetos = new ArrayList<>();
    private final List<Pedido> listaPedidos = new ArrayList<>();
    
    private Spinner spinnerProjetos, spinnerSituacao;
    private EditText edtNumPedido, edtDataInicio, edtDataFim, edtProduto, edtNumProcesso, edtNumAfOs, edtFornecedor, edtSolicitante;
    private CheckBox chkListarTodos, chkMeusPedidos;
    private Button btnConsultar;
    private ListView listView;
    private ArrayAdapter<Pedido> adapterPedidos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);

        token = getIntent().getStringExtra("TOKEN");

        inicializarComponentes();
        configurarMenuLateral();
        configurarDatas();
        
        carregarProjetos();
        configurarSituacoes();

        btnConsultar.setOnClickListener(v -> realizarConsulta());
    }

    private void inicializarComponentes() {
        spinnerProjetos = findViewById(R.id.spinner_projetos);
        spinnerSituacao = findViewById(R.id.spinner_situacao);
        edtNumPedido = findViewById(R.id.edt_num_pedido);
        edtDataInicio = findViewById(R.id.edt_data_inicio);
        edtDataFim = findViewById(R.id.edt_data_fim);
        edtProduto = findViewById(R.id.edt_produto);
        edtNumProcesso = findViewById(R.id.edt_num_processo);
        edtNumAfOs = findViewById(R.id.edt_num_af_os);
        edtFornecedor = findViewById(R.id.edt_fornecedor);
        edtSolicitante = findViewById(R.id.edt_solicitante);
        chkListarTodos = findViewById(R.id.chk_listar_todos);
        chkMeusPedidos = findViewById(R.id.chk_meus_pedidos);
        btnConsultar = findViewById(R.id.btn_consultar);
        listView = findViewById(R.id.list_consultas);

        // Adaptador customizado para garantir cor preta no texto da lista (Modo Claro)
        adapterPedidos = new ArrayAdapter<Pedido>(this, android.R.layout.simple_list_item_1, listaPedidos) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(Color.BLACK); // Força cor preta
                return view;
            }
        };
        listView.setAdapter(adapterPedidos);
    }

    private void configurarDatas() {
        edtDataInicio.setOnClickListener(v -> abrirCalendario(edtDataInicio));
        edtDataFim.setOnClickListener(v -> abrirCalendario(edtDataFim));
    }

    private void abrirCalendario(EditText campoData) {
        Calendar calendario = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dataFormatada = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, (month + 1), year);
            campoData.setText(dataFormatada);
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void carregarProjetos() {
        Coordenador.getProjetos(token, null, "Ativo", 1, 100, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ConsultaActivity.this, "Erro ao carregar projetos", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        List<Projeto> projetos = ProjetoDAO.fromJson(json);
                        runOnUiThread(() -> {
                            listaProjetos.clear();
                            listaProjetos.addAll(projetos);
                            preencherSpinnerProjetos();
                        });
                    } catch (Exception e) {
                        Log.e("FLUXO_API", "Erro ao processar projetos: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void preencherSpinnerProjetos() {
        List<String> nomes = new ArrayList<>();
        nomes.add("Selecione um Projeto");
        for (Projeto p : listaProjetos) {
            nomes.add(p.getCodConvenio() + " - " + p.getNomeConvenio());
        }
        
        // Adaptador customizado para o Spinner para garantir cor preta
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nomes) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                return v;
            }
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProjetos.setAdapter(adapter);
    }

    private void configurarSituacoes() {
        List<String> situacoes = new ArrayList<>();
        situacoes.add("Selecione uma Situação");
        situacoes.add("Registrado");
        situacoes.add("Pendente");
        situacoes.add("Aprovado");
        situacoes.add("Em Processamento");
        
        // Adaptador customizado para o Spinner para garantir cor preta
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, situacoes) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                return v;
            }
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSituacao.setAdapter(adapter);
    }

    private void realizarConsulta() {
        String numPedido = edtNumPedido.getText().toString();
        String codProjetoFinal = "";
        if (!chkListarTodos.isChecked() && spinnerProjetos.getSelectedItemPosition() > 0) {
            codProjetoFinal = String.valueOf(listaProjetos.get(spinnerProjetos.getSelectedItemPosition() - 1).getCodConvenio());
        }
        
        String dataInicio = converterDataParaApi(edtDataInicio.getText().toString());
        String dataFim = converterDataParaApi(edtDataFim.getText().toString());
        String produto = edtProduto.getText().toString();
        String numProcesso = edtNumProcesso.getText().toString();
        String numAfOs = edtNumAfOs.getText().toString();
        String fornecedor = edtFornecedor.getText().toString();
        boolean meusPedidos = chkMeusPedidos.isChecked();
        String solicitante = edtSolicitante.getText().toString();
        String situacao = spinnerSituacao.getSelectedItemPosition() > 0 ? spinnerSituacao.getSelectedItem().toString() : "";

        Coordenador.getPedidosCompraServico(token, numPedido, codProjetoFinal, dataInicio, dataFim, produto, numProcesso, numAfOs, fornecedor, meusPedidos, solicitante, situacao, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ConsultaActivity.this, "Erro na consulta: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        List<Pedido> pedidos = PedidoDAO.fromJson(json);
                        runOnUiThread(() -> {
                            listaPedidos.clear();
                            listaPedidos.addAll(pedidos);
                            adapterPedidos.notifyDataSetChanged();
                            if (listaPedidos.isEmpty()) {
                                Toast.makeText(ConsultaActivity.this, "Nenhum pedido encontrado", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e("FLUXO_API", "Erro ao processar pedidos: " + e.getMessage());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(ConsultaActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private String converterDataParaApi(String dataBr) {
        if (dataBr != null && dataBr.contains("/")) {
            String[] partes = dataBr.split("/");
            if (partes.length == 3) {
                return partes[2] + "-" + partes[1] + "-" + partes[0];
            }
        }
        return dataBr;
    }

    private void configurarMenuLateral() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ImageView btnMenu = findViewById(R.id.menu_header);

        btnMenu.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.opc_projetos) {
                Intent intent = new Intent(this, ProjetosActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
                finish();
            } else if (id == R.id.opc_extrato) {
                Intent intent = new Intent(this, ExtratoActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
                finish();
            } else if (id == R.id.opc_saldo) {
                Intent intent = new Intent(this, SaldoActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
                finish();
            } else if (id == R.id.opc_pedidos) {
                Intent intent = new Intent(this, PedidosActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
                finish();
            } else if (id == R.id.opc_consultas) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
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
}