package com.example.conveniar_coordenador;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.conveniar_coordenador.DAO.ProjetoDAO;
import com.example.conveniar_coordenador.model.ConsultaItem;
import com.example.conveniar_coordenador.model.Projeto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ConsultaActivity extends BaseActivity {

    private static final int TIPO_COMPRA_SERVICO = 0;
    private static final int TIPO_PAGAMENTO = 1;

    private final List<Projeto> listaProjetos = new ArrayList<>();
    private final List<ConsultaItem> listaResultados = new ArrayList<>();
    
    private Spinner spinnerTipoConsulta, spinnerProjetos, spinnerSituacao, spinnerTipoPedido;
    private EditText edtNumPedido, edtDataInicio, edtDataFim;
    // Campos exclusivos de Compra/Serviço
    private EditText edtProduto, edtNumProcesso, edtNumAfOs, edtFornecedor, edtSolicitante;
    // Campos exclusivos de Pagamento
    private EditText edtFavorecido, edtValorPedido;
    // Layouts condicionais
    private LinearLayout layoutCamposCompra, layoutCamposPagamento;
    
    private CheckBox chkListarTodos, chkMeusPedidos;
    private Button btnConsultar;
    private ListView listView;
    private ConsultaAdapter adapterResultados;

    private int tipoConsultaAtual = TIPO_COMPRA_SERVICO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configura automaticamente o menu lateral
        setupDrawer();

        inicializarComponentes();
        configurarTipoConsulta();
        configurarDatas();
        
        carregarProjetos();
        configurarSituacoes();
        configurarTiposPedido();

        btnConsultar.setOnClickListener(v -> realizarConsulta());
    }

    private void inicializarComponentes() {
        spinnerTipoConsulta = findViewById(R.id.spinner_tipo_consulta);
        spinnerProjetos = findViewById(R.id.spinner_projetos);
        spinnerSituacao = findViewById(R.id.spinner_situacao);
        spinnerTipoPedido = findViewById(R.id.spinner_tipo_pedido);
        
        edtNumPedido = findViewById(R.id.edt_num_pedido);
        edtDataInicio = findViewById(R.id.edt_data_inicio);
        edtDataFim = findViewById(R.id.edt_data_fim);
        edtProduto = findViewById(R.id.edt_produto);
        edtNumProcesso = findViewById(R.id.edt_num_processo);
        edtNumAfOs = findViewById(R.id.edt_num_af_os);
        edtFornecedor = findViewById(R.id.edt_fornecedor);
        edtSolicitante = findViewById(R.id.edt_solicitante);
        edtFavorecido = findViewById(R.id.edt_favorecido);
        edtValorPedido = findViewById(R.id.edt_valor_pedido);
        
        layoutCamposCompra = findViewById(R.id.layout_campos_compra);
        layoutCamposPagamento = findViewById(R.id.layout_campos_pagamento);
        
        chkListarTodos = findViewById(R.id.chk_listar_todos);
        chkMeusPedidos = findViewById(R.id.chk_meus_pedidos);
        btnConsultar = findViewById(R.id.btn_consultar);
        listView = findViewById(R.id.list_consultas);

        // Adapter customizado
        adapterResultados = new ConsultaAdapter(this, listaResultados);
        listView.setAdapter(adapterResultados);
        listView.setDivider(null);
        listView.setDividerHeight(0);
    }

    private void configurarTipoConsulta() {
        List<String> tipos = new ArrayList<>();
        tipos.add("Pedidos de Compra/Serviço");
        tipos.add("Pedidos de Pagamento");

        ArrayAdapter<String> adapter = criarSpinnerAdapter(tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoConsulta.setAdapter(adapter);

        spinnerTipoConsulta.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoConsultaAtual = position;
                atualizarCamposVisiveis();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void atualizarCamposVisiveis() {
        if (tipoConsultaAtual == TIPO_COMPRA_SERVICO) {
            layoutCamposCompra.setVisibility(View.VISIBLE);
            layoutCamposPagamento.setVisibility(View.GONE);
        } else {
            layoutCamposCompra.setVisibility(View.GONE);
            layoutCamposPagamento.setVisibility(View.VISIBLE);
        }
        // Limpa os resultados ao trocar de tipo
        listaResultados.clear();
        adapterResultados.notifyDataSetChanged();
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
        
        ArrayAdapter<String> adapter = criarSpinnerAdapter(nomes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProjetos.setAdapter(adapter);
    }

    private void configurarSituacoes() {
        List<String> situacoes = new ArrayList<>();
        situacoes.add("Todas as Situações");
        situacoes.add("Registrado");
        situacoes.add("Pendente");
        situacoes.add("Aprovado");
        situacoes.add("Em Processamento");
        situacoes.add("Pago");
        situacoes.add("Encerrado");
        situacoes.add("Cancelado");
        
        ArrayAdapter<String> adapter = criarSpinnerAdapter(situacoes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSituacao.setAdapter(adapter);
    }

    private void configurarTiposPedido() {
        List<String> tipos = new ArrayList<>();
        tipos.add("Todos os Tipos");
        tipos.add("Pagamento de Adiantamento");
        tipos.add("Pagamento de Bolsa");
        tipos.add("Pagamento de Diária");
        tipos.add("Pagamento de Pessoa Física");
        tipos.add("Pagamento de Pessoa Jurídica");
        tipos.add("Pagamento de Reembolso");
        tipos.add("Pagamento de Transferência");

        ArrayAdapter<String> adapter = criarSpinnerAdapter(tipos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoPedido.setAdapter(adapter);
    }

    private ArrayAdapter<String> criarSpinnerAdapter(List<String> itens) {
        return new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, itens) {
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
    }

    private void realizarConsulta() {
        if (tipoConsultaAtual == TIPO_COMPRA_SERVICO) {
            realizarConsultaCompra();
        } else {
            realizarConsultaPagamento();
        }
    }

    private void realizarConsultaCompra() {
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
                        Log.d("FLUXO_API", "Resposta Compra: " + json.substring(0, Math.min(json.length(), 500)));
                        
                        JSONArray array = new JSONArray(json);
                        runOnUiThread(() -> {
                            listaResultados.clear();
                            for (int i = 0; i < array.length(); i++) {
                                try {
                                    JSONObject obj = array.getJSONObject(i);
                                    String num = String.valueOf(obj.optInt("numeroPedido", 0));
                                    String data = formatarData(obj.optString("dataPedido", ""));
                                    String proj = obj.optString("projeto", "");
                                    String prod = obj.optString("produto", "");
                                    String forn = obj.optString("fornecedor", "");
                                    String status = obj.optString("nomeStatus", "");
                                    double valor = obj.optDouble("valor", 0.0);

                                    listaResultados.add(new ConsultaItem(
                                            num, status, prod, proj, forn, "Fornecedor", data, valor, false));
                                } catch (Exception e) {
                                    Log.e("FLUXO_API", "Erro item compra: " + e.getMessage());
                                }
                            }
                            adapterResultados.notifyDataSetChanged();
                            if (listaResultados.isEmpty()) {
                                Toast.makeText(ConsultaActivity.this, "Nenhum pedido de compra encontrado", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e("FLUXO_API", "Erro ao processar pedidos compra: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(ConsultaActivity.this, "Erro ao processar dados", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(ConsultaActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void realizarConsultaPagamento() {
        String numPedido = edtNumPedido.getText().toString();
        String codProjetoFinal = "";
        if (!chkListarTodos.isChecked() && spinnerProjetos.getSelectedItemPosition() > 0) {
            codProjetoFinal = String.valueOf(listaProjetos.get(spinnerProjetos.getSelectedItemPosition() - 1).getCodConvenio());
        }

        String dataInicio = converterDataParaApi(edtDataInicio.getText().toString());
        String dataFim = converterDataParaApi(edtDataFim.getText().toString());
        String tipoPedido = spinnerTipoPedido.getSelectedItemPosition() > 0 ? spinnerTipoPedido.getSelectedItem().toString() : "";
        String favorecido = edtFavorecido.getText().toString();
        String valorPedido = edtValorPedido.getText().toString();
        String situacao = spinnerSituacao.getSelectedItemPosition() > 0 ? spinnerSituacao.getSelectedItem().toString() : "";
        boolean meusPedidos = chkMeusPedidos.isChecked();

        Coordenador.getPedidosPagamento(token, tipoPedido, codProjetoFinal, numPedido, favorecido, dataInicio, valorPedido, situacao, dataFim, meusPedidos, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ConsultaActivity.this, "Erro na consulta: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        Log.d("FLUXO_API", "Resposta Pagamento: " + json.substring(0, Math.min(json.length(), 500)));
                        
                        JSONArray array = new JSONArray(json);
                        runOnUiThread(() -> {
                            listaResultados.clear();
                            for (int i = 0; i < array.length(); i++) {
                                try {
                                    JSONObject obj = array.getJSONObject(i);
                                    Log.d("FLUXO_API", "Item pagamento [" + i + "]: " + obj.toString().substring(0, Math.min(obj.toString().length(), 300)));
                                    
                                    String num = String.valueOf(obj.optInt("codPedido", obj.optInt("numeroPedido", 0)));
                                    String tipoPed = obj.optString("nomeTipoPedido", "");
                                    String data = formatarData(obj.optString("dataPedido", ""));
                                    String proj = obj.optString("projeto", "");
                                    String favo = obj.optString("nomeFavorecido", "");
                                    String status = obj.optString("nomeStatus", "");
                                    double valor = obj.optDouble("valorPedido", 0.0);

                                    listaResultados.add(new ConsultaItem(
                                            num, status, tipoPed, proj, favo, "Favorecido", data, valor, true));
                                } catch (Exception e) {
                                    Log.e("FLUXO_API", "Erro item pagamento: " + e.getMessage());
                                }
                            }
                            adapterResultados.notifyDataSetChanged();
                            if (listaResultados.isEmpty()) {
                                Toast.makeText(ConsultaActivity.this, "Nenhum pedido de pagamento encontrado", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e("FLUXO_API", "Erro ao processar pagamentos: " + e.getMessage());
                        runOnUiThread(() -> Toast.makeText(ConsultaActivity.this, "Erro ao processar dados", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(ConsultaActivity.this, "Erro: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    /** Formata data ISO (2025-12-18T14:20:00) para DD/MM/AAAA */
    private String formatarData(String dataIso) {
        if (dataIso == null || dataIso.isEmpty()) return "";
        try {
            // Remove parte do horário se existir
            String dataParte = dataIso.contains("T") ? dataIso.split("T")[0] : dataIso;
            
            if (dataParte.contains("-")) {
                String[] partes = dataParte.split("-");
                if (partes.length == 3) {
                    return partes[2] + "/" + partes[1] + "/" + partes[0];
                }
            }
        } catch (Exception e) {
            Log.e("FLUXO_API", "Erro formatando data: " + dataIso);
        }
        return dataIso;
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
}
