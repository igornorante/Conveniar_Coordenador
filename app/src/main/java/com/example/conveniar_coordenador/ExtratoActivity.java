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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.conveniar_coordenador.model.Projeto;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ExtratoActivity extends AppCompatActivity {

    private EditText edtDataInicio;
    private EditText edtDataFim;
    private Spinner spinnerProjetos;
    private Button btnGerar;
    private ListView listExtrato;

    private String token;
    private List<Projeto> listaProjetos = new ArrayList<>();
    private List<String> nomesProjetos = new ArrayList<>();
    private ArrayAdapter<String> adapterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_extrato);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        token = getIntent().getStringExtra("TOKEN");

        edtDataInicio = findViewById(R.id.edt_data_inicio);
        edtDataFim = findViewById(R.id.edt_data_fim);
        spinnerProjetos = findViewById(R.id.spinner_projetos);
        btnGerar = findViewById(R.id.btn_gerar_extrato);
        listExtrato = findViewById(R.id.list_extrato);

        edtDataInicio.setTextColor(Color.BLACK);
        edtDataFim.setTextColor(Color.BLACK);

        configurarMenuLateral();
        configurarFiltros();

        if (token != null && !token.isEmpty()) {
            carregarProjetosNoSpinner();
        }
    }

    private void carregarProjetosNoSpinner() {
        nomesProjetos.add("Selecione um projeto...");
        Coordenador.getProjetos(token, null, "Ativo", 1, 50, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ExtratoActivity.this, "Erro ao carregar projetos.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        JSONArray array = new JSONArray(json);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            Projeto projeto = new Projeto(
                                    obj.optInt("codConvenio"),
                                    obj.optString("nomeConvenio"),
                                    obj.optDouble("saldo"),
                                    obj.optString("nomeStatus"),
                                    obj.optString("coordenador"),
                                    obj.optString("dataVigencia")
                            );
                            listaProjetos.add(projeto);
                            nomesProjetos.add(projeto.getNomeConvenio());
                        }
                        runOnUiThread(() -> adapterSpinner.notifyDataSetChanged());
                    } catch (Exception e) {
                        Log.e("FLUXO_API", "Erro JSON Spinner: " + e.getMessage());
                    }
                }
            }
        });
    }

    private void configurarFiltros() {
        adapterSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, nomesProjetos) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                ((TextView) v).setTextColor(Color.BLACK);
                return v;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                return v;
            }
        };

        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProjetos.setAdapter(adapterSpinner);

        edtDataInicio.setOnClickListener(v -> abrirCalendario(edtDataInicio));
        edtDataFim.setOnClickListener(v -> abrirCalendario(edtDataFim));

        btnGerar.setOnClickListener(v -> {
            int posicaoSelecionada = spinnerProjetos.getSelectedItemPosition();
            String dataInicio = edtDataInicio.getText().toString();
            String dataFim = edtDataFim.getText().toString();

            if (posicaoSelecionada <= 0 || dataInicio.isEmpty() || dataFim.isEmpty()) {
                Toast.makeText(this, "Preencha todos os filtros!", Toast.LENGTH_SHORT).show();
            } else {
                
                // Validação de intervalo de datas para evitar erro 500 na API
                try {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US);
                    java.util.Date dateInicio = sdf.parse(dataInicio);
                    java.util.Date dateFim = sdf.parse(dataFim);
                    
                    if (dateInicio != null && dateFim != null && dateInicio.after(dateFim)) {
                        Toast.makeText(this, "A data de início não pode ser maior que a data final.", Toast.LENGTH_LONG).show();
                        return; // Interrompe a chamada à API
                    }
                } catch (Exception e) {
                    Log.e("DEBUG_EXTRATO", "Erro ao validar datas: " + e.getMessage());
                }

                Projeto projetoSelecionado = listaProjetos.get(posicaoSelecionada - 1);

                Log.d("DEBUG_EXTRATO", "ID Enviado para a API: " + projetoSelecionado.getCodConvenio());

                String dataInicioApi = converterDataParaApi(dataInicio);
                String dataFimApi = converterDataParaApi(dataFim);

                buscarExtratoNaApi(projetoSelecionado.getCodConvenio(), dataInicioApi, dataFimApi);
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

    private void abrirCalendario(EditText campoData) {
        Calendar calendario = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String dataFormatada = String.format(java.util.Locale.US, "%02d/%02d/%04d", dayOfMonth, (month + 1), year);
            campoData.setText(dataFormatada);
            campoData.setTextColor(Color.BLACK);
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void buscarExtratoNaApi(int codProjeto, String dataInicio, String dataFim) {
        Coordenador.getExtratoSimplificado(token, codProjeto, dataInicio, dataFim, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ExtratoActivity.this, "Erro de rede.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final int codigoResposta = response.code();
                final String corpoResposta = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    Log.e("DEBUG_API", "Problema, Codigo: " + codigoResposta);
                    Log.e("DEBUG_API", "Motivo do servidor: " + corpoResposta);
                }

                runOnUiThread(() -> {
                    if (codigoResposta == 200) {
                        Log.d("JSON_COMPLETO", "Resposta 200: " + corpoResposta);
                        try {
                            JSONObject respostaApp = new JSONObject(corpoResposta);
                            JSONArray rubricas = respostaApp.optJSONArray("rubricas");
                            List<String> listaItensExtrato = new ArrayList<>();

                            // Extrai saldo total do projeto
                            String saldoTotal = respostaApp.optString("saldoFinalProjeto", "0.00");
                            listaItensExtrato.add("SALDO TOTAL DO PROJETO: R$ " + saldoTotal + "\n");

                            if (rubricas != null) {
                                for (int i = 0; i < rubricas.length(); i++) {
                                    JSONObject objRubrica = rubricas.getJSONObject(i);
                                    JSONArray lancamentos = objRubrica.optJSONArray("lancamentos");

                                    // Só exibe a rubrica (categoria) se ela tiver lançamentos dentro
                                    if (lancamentos != null && lancamentos.length() > 0) {

                                        // Adiciona cabeçalho da rubrica
                                        String nomeRubrica = objRubrica.optString("nomeRubrica", "Sem Categoria");
                                        listaItensExtrato.add("=== " + nomeRubrica.toUpperCase() + " ===");

                                        for (int j = 0; j < lancamentos.length(); j++) {
                                            JSONObject lanc = lancamentos.getJSONObject(j);

                                            String data = lanc.optString("dataPagamento", "--/--/----");
                                            if (data.equals("null") || data.isEmpty()) data = "--/--/----";

                                            String historicoLimpo = lanc.optString("historico")
                                                    .replaceAll("<[^>]*>", "")
                                                    .replace(", ,", "")
                                                    .trim();
                                            if (historicoLimpo.equals("null")) historicoLimpo = "";

                                            String tipo = lanc.optString("tipo", "Sem tipo");
                                            if (tipo.equals("null")) tipo = "Sem tipo";

                                            String documento = lanc.optString("numeroDocumento", "S/N");
                                            if (documento.equals("null")) documento = "S/N";

                                            // Trata valores nulos ou vazios no débito/crédito
                                            String valorDebito = lanc.optString("debito", "0.00");
                                            if (valorDebito.isEmpty() || valorDebito.equals("null")) valorDebito = "0.00";

                                            String valorCredito = lanc.optString("credito", "0.00");
                                            if (valorCredito.isEmpty() || valorCredito.equals("null")) valorCredito = "0.00";

                                            // Formata string de exibição com indentação visual (espaços)
                                            String texto = "    Data: " + data + "\n" +
                                                    "    Doc: " + documento + " | Tipo: " + tipo + "\n" +
                                                    "    Histórico: " + historicoLimpo + "\n" +
                                                    "    Débito: R$ " + valorDebito + " | Crédito: R$ " + valorCredito;

                                            listaItensExtrato.add(texto);
                                        }
                                    }
                                }
                            }

                            // Atualiza a tela
                            ArrayAdapter<String> adapterExtrato = new ArrayAdapter<String>(ExtratoActivity.this, android.R.layout.simple_list_item_1, listaItensExtrato) {
                                @Override
                                public View getView(int pos, View convert, ViewGroup par) {
                                    View v = super.getView(pos, convert, par);
                                    TextView t = v.findViewById(android.R.id.text1);
                                    t.setTextColor(Color.BLACK);
                                    return v;
                                }
                            };
                            listExtrato.setAdapter(adapterExtrato);

                        } catch (Exception e) {
                            Log.e("FLUXO_API", "Erro JSON: " + e.getMessage());
                            Toast.makeText(ExtratoActivity.this, "Erro ao processar dados do extrato.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ExtratoActivity.this, "Erro " + codigoResposta, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void configurarMenuLateral() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ImageView btnMenu = findViewById(R.id.menu_header);
        if (btnMenu != null) btnMenu.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        NavigationView nv = findViewById(R.id.navigation_view);
        if (nv != null) {
            nv.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.opc_projetos) {
                    Intent intent = new Intent(this, ProjetosActivity.class);
                    intent.putExtra("TOKEN", token);
                    startActivity(intent);
                } else if (id == R.id.opc_extrato) {
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.opc_saldo) {
                    Intent intent = new Intent(this, SaldoActivity.class);
                    intent.putExtra("TOKEN", token);
                    startActivity(intent);
                } else if (id == R.id.opc_consultas) {
                    Intent intent = new Intent(this, ConsultaActivity.class);
                    intent.putExtra("TOKEN", token);
                    startActivity(intent);
                } else if (id == R.id.opc_pedidos) {
                    Intent intent = new Intent(this, PedidosActivity.class);
                    intent.putExtra("TOKEN", token);
                    startActivity(intent);
                } else if (id == R.id.opc_sair) {
                    startActivity(new Intent(this, LoginActivity.class));
                    finishAffinity();
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }
}