package com.example.conveniar_coordenador;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.conveniar_coordenador.DAO.ProjetoDAO;
import com.example.conveniar_coordenador.model.ExtratoItem;
import com.example.conveniar_coordenador.model.Projeto;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ExtratoActivity extends BaseActivity {

    private EditText edtDataInicio;
    private EditText edtDataFim;
    private Spinner spinnerProjetos;
    private Button btnGerar;
    private ListView listExtrato;

    private final List<Projeto> listaProjetos = new ArrayList<>();
    private final List<ExtratoItem> listaExtrato = new ArrayList<>();
    private ExtratoAdapter adapterExtrato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_extrato);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializando os componentes da tela
        edtDataInicio = findViewById(R.id.edt_data_inicio);
        edtDataFim = findViewById(R.id.edt_data_fim);
        spinnerProjetos = findViewById(R.id.spinner_projetos);
        btnGerar = findViewById(R.id.btn_gerar_extrato);
        listExtrato = findViewById(R.id.list_extrato);

        // Chama o método da BaseActivity que configura o Menu e o Nome
        setupDrawer();
        
        carregarProjetos();
        configurarFiltros();
    }

    private void carregarProjetos() {
        Coordenador.getProjetos(token, null, "Ativo", 1, 100, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ExtratoActivity.this, "Erro ao carregar projetos", Toast.LENGTH_SHORT).show());
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

    private void configurarFiltros() {
        // 1. Adapter customizado do Extrato
        adapterExtrato = new ExtratoAdapter(this, listaExtrato);
        listExtrato.setAdapter(adapterExtrato);
        listExtrato.setDivider(null);
        listExtrato.setDividerHeight(0);

        // 2. Configurando o clique nos campos de data para abrir o Calendário
        edtDataInicio.setOnClickListener(v -> abrirCalendario(edtDataInicio));
        edtDataFim.setOnClickListener(v -> abrirCalendario(edtDataFim));

        // 3. Ação do Botão "Gerar Extrato"
        btnGerar.setOnClickListener(v -> gerarExtrato());
    }

    private void gerarExtrato() {
        if (spinnerProjetos.getSelectedItemPosition() <= 0) {
            Toast.makeText(this, "Selecione um projeto válido!", Toast.LENGTH_SHORT).show();
            return;
        }

        String dataInicio = edtDataInicio.getText().toString();
        String dataFim = edtDataFim.getText().toString();

        if (dataInicio.isEmpty() || dataFim.isEmpty()) {
            Toast.makeText(this, "Preencha as datas de início e fim!", Toast.LENGTH_SHORT).show();
            return;
        }

        int codProjeto = listaProjetos.get(spinnerProjetos.getSelectedItemPosition() - 1).getCodConvenio();
        
        String dataInicioApi = converterDataParaApi(dataInicio);
        String dataFimApi = converterDataParaApi(dataFim);

        Coordenador.getExtratoSimplificado(token, codProjeto, dataInicioApi, dataFimApi, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(ExtratoActivity.this, "Erro ao gerar extrato: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        Log.d("FLUXO_API", "Resposta Extrato: " + json);

                        JSONObject resposta = new JSONObject(json);
                        String saldoAnterior = resposta.optString("saldoAnteriorProjeto", "0.00");
                        String saldoFinal = resposta.optString("saldoFinalProjeto", "0.00");
                        JSONArray rubricas = resposta.optJSONArray("rubricas");

                        runOnUiThread(() -> {
                            listaExtrato.clear();

                            // Saldo anterior do projeto
                            listaExtrato.add(new ExtratoItem(
                                    ExtratoItem.Tipo.SALDO_PROJETO, "Saldo Anterior", null, saldoAnterior, false));

                            if (rubricas != null) {
                                for (int i = 0; i < rubricas.length(); i++) {
                                    try {
                                        JSONObject rubrica = rubricas.getJSONObject(i);
                                        String nomeRubrica = rubrica.optString("nomeRubrica", "Rubrica");
                                        String saldoAntRubrica = rubrica.optString("saldoAnterior", "0.00");
                                        String saldoFimRubrica = rubrica.optString("saldoFinalRubrica", "0.00");
                                        JSONArray lancamentos = rubrica.optJSONArray("lancamentos");

                                        // Cabeçalho da rubrica
                                        listaExtrato.add(new ExtratoItem(
                                                ExtratoItem.Tipo.RUBRICA_HEADER, nomeRubrica, saldoAntRubrica, null, false));

                                        // Lançamentos da rubrica
                                        if (lancamentos != null && lancamentos.length() > 0) {
                                            for (int j = 0; j < lancamentos.length(); j++) {
                                                JSONObject lanc = lancamentos.getJSONObject(j);
                                                String dataLanc = lanc.optString("dataLancamento", "");
                                                String tipo = lanc.optString("tipo", "");
                                                String numDoc = lanc.optString("numeroDocumento", "");
                                                String debito = lanc.optString("debito", "");
                                                String credito = lanc.optString("credito", "");

                                                boolean isDebito = !debito.isEmpty();
                                                String valorMon = isDebito ? debito : credito;

                                                // Formato: numDoc|valorMonetario
                                                listaExtrato.add(new ExtratoItem(
                                                        ExtratoItem.Tipo.LANCAMENTO, dataLanc, tipo,
                                                        numDoc + "|" + valorMon, isDebito));
                                            }
                                        } else {
                                            listaExtrato.add(new ExtratoItem(
                                                    ExtratoItem.Tipo.SEM_LANCAMENTO, "Sem lançamentos neste período", null, null, false));
                                        }

                                        // Saldo final da rubrica
                                        listaExtrato.add(new ExtratoItem(
                                                ExtratoItem.Tipo.SALDO_RUBRICA, "Saldo Final Rubrica", null, saldoFimRubrica, false));

                                    } catch (Exception e) {
                                        Log.e("FLUXO_API", "Erro na rubrica " + i + ": " + e.getMessage());
                                    }
                                }
                            }

                            // Saldo final do projeto
                            listaExtrato.add(new ExtratoItem(
                                    ExtratoItem.Tipo.SALDO_PROJETO, "Saldo Final", null, saldoFinal, false));

                            adapterExtrato.notifyDataSetChanged();
                            if (listaExtrato.size() <= 2) {
                                Toast.makeText(ExtratoActivity.this, "Nenhum lançamento encontrado no período.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e("FLUXO_API", "Erro JSON Extrato: " + e.getMessage(), e);
                        runOnUiThread(() -> Toast.makeText(ExtratoActivity.this, "Erro ao processar dados.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(ExtratoActivity.this, "Erro na API: " + response.code(), Toast.LENGTH_SHORT).show());
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

    // Método auxiliar para criar e mostrar o calendário
    private void abrirCalendario(EditText campoData) {
        Calendar calendario = Calendar.getInstance();
        int ano = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Formata a data para DD/MM/AAAA (ex: 05/08/2026)
            String dataFormatada = String.format("%02d/%02d/%04d", dayOfMonth, (month + 1), year);
            campoData.setText(dataFormatada);
        }, ano, mes, dia);

        dialog.show();
    }
}
