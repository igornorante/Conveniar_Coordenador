package com.example.conveniar_coordenador;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.MediatorLiveData;

import com.example.conveniar_coordenador.DAO.ProjetoDAO;
import com.example.conveniar_coordenador.database.AppRepository;
import com.example.conveniar_coordenador.database.PedidoEntity;
import com.example.conveniar_coordenador.database.PedidoPagamentoEntity;
import com.example.conveniar_coordenador.databinding.ActivityPrincipalBinding;
import com.example.conveniar_coordenador.model.Projeto;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class PrincipalActivity extends BaseActivity {

    private ActivityPrincipalBinding binding;
    private AppRepository repository;
    private final Locale ptBr = new Locale("pt", "BR");
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(ptBr);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPrincipalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new AppRepository(this);
        setupDrawer();
        configurarBotoesRapidos();
        carregarDadosDashboard();
        iniciarObservacaoPendencias();
    }

    private void configurarBotoesRapidos() {
        binding.btnPedidos.setOnClickListener(v -> startActivity(new Intent(this, PedidosActivity.class).putExtra("TOKEN", token)));
        binding.btnExtrato.setOnClickListener(v -> startActivity(new Intent(this, ExtratoActivity.class).putExtra("TOKEN", token)));
        binding.btnProjetos.setOnClickListener(v -> startActivity(new Intent(this, ProjetosActivity.class).putExtra("TOKEN", token)));
        binding.btnPendencias.setOnClickListener(v -> startActivity(new Intent(this, PendenciasActivity.class).putExtra("TOKEN", token)));
    }

    private void iniciarObservacaoPendencias() {
        MediatorLiveData<Integer> totalPendencias = new MediatorLiveData<>();
        
        totalPendencias.addSource(repository.getTodosPedidos(), pedidos -> totalPendencias.setValue(calcularTotal(pedidos, repository.getTodosPedidosPagamento().getValue())));
        totalPendencias.addSource(repository.getTodosPedidosPagamento(), pagamentos -> totalPendencias.setValue(calcularTotal(repository.getTodosPedidos().getValue(), pagamentos)));

        totalPendencias.observe(this, total -> {
            binding.dashCountPendencias.setText(String.valueOf(total));
            processarResumoPendencias(repository.getTodosPedidos().getValue(), repository.getTodosPedidosPagamento().getValue());
        });

        repository.getTodosPedidosPagamento().observe(this, pagamentos -> {
            if (pagamentos != null && !pagamentos.isEmpty()) {
                popularAtividadeLocal(pagamentos);
            }
        });
    }

    private void processarResumoPendencias(List<PedidoEntity> pedidos, List<PedidoPagamentoEntity> pagamentos) {
        Map<String, Integer> resumo = new HashMap<>();
        if (pedidos != null) {
            for (PedidoEntity p : pedidos) {
                if (!isStatusFinalizado(p.situacao)) {
                    resumo.put("Compra/Serviço", resumo.getOrDefault("Compra/Serviço", 0) + 1);
                }
            }
        }
        if (pagamentos != null) {
            for (PedidoPagamentoEntity p : pagamentos) {
                if (!isStatusFinalizado(p.nomeStatus)) {
                    String tipo = p.nomeTipoPedido != null ? p.nomeTipoPedido : "Outros";
                    resumo.put(tipo, resumo.getOrDefault(tipo, 0) + 1);
                }
            }
        }

        LinearLayout container = binding.layoutResumoPendencias;
        container.removeAllViews();

        if (resumo.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Nenhuma pendência ativa.");
            empty.setPadding(32, 32, 32, 32);
            empty.setGravity(android.view.Gravity.CENTER);
            empty.setTextColor(Color.GRAY);
            container.addView(empty);
            return;
        }

        for (Map.Entry<String, Integer> entry : resumo.entrySet()) {
            View item = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, container, false);
            TextView t1 = item.findViewById(android.R.id.text1);
            TextView t2 = item.findViewById(android.R.id.text2);
            t1.setText(entry.getKey());
            t1.setTypeface(null, Typeface.BOLD);
            t2.setText(entry.getValue() + " pendência(s)");
            t2.setTextColor(Color.parseColor("#E67E22"));
            container.addView(item);
        }
    }

    private int calcularTotal(List<PedidoEntity> pedidos, List<PedidoPagamentoEntity> pagamentos) {
        int total = 0;
        if (pedidos != null) {
            for (PedidoEntity p : pedidos) if (!isStatusFinalizado(p.situacao)) total++;
        }
        if (pagamentos != null) {
            for (PedidoPagamentoEntity p : pagamentos) if (!isStatusFinalizado(p.nomeStatus)) total++;
        }
        return total;
    }

    private boolean isStatusFinalizado(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("finalizado") || s.contains("cancelado") || s.contains("reprovado") || s.contains("concluído");
    }

    private void popularAtividadeLocal(List<PedidoPagamentoEntity> pagamentos) {
        LinearLayout container = binding.layoutRecent;
        container.removeAllViews();
        int limite = Math.min(3, pagamentos.size());
        for (int i = 0; i < limite; i++) {
            PedidoPagamentoEntity p = pagamentos.get(pagamentos.size() - 1 - i);
            View item = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, container, false);
            ((TextView)item.findViewById(android.R.id.text1)).setText("Pedido #" + p.numeroPedido);
            ((TextView)item.findViewById(android.R.id.text1)).setTypeface(null, Typeface.BOLD);
            ((TextView)item.findViewById(android.R.id.text2)).setText(p.nomeTipoPedido + " | " + p.nomeStatus);
            container.addView(item);
        }
    }

    private void carregarDadosDashboard() {
        if (token == null) return;
        Coordenador.getProjetos(token, null, null, 1, 100, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(PrincipalActivity.this, "Falha ao sincronizar dados", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String json = response.body().string();
                        List<Projeto> projetos = ProjetoDAO.fromJson(json);
                        runOnUiThread(() -> atualizarDashboard(projetos));
                    } catch (Exception e) {
                        Log.e("DASHBOARD", "Erro JSON", e);
                    }
                }
            }
        });
    }

    private void atualizarDashboard(List<Projeto> projetos) {
        if (projetos.isEmpty()) return;
        double saldoTotal = 0, saldoMaximo = 0;
        int countVencendo = 0, countBaixoSaldo = 0;
        Map<String, Integer> statusMap = new HashMap<>();
        List<Projeto> proximosVencimentos = new ArrayList<>();
        Calendar cal90Dias = Calendar.getInstance();
        cal90Dias.add(Calendar.DAY_OF_YEAR, 90);

        for (Projeto p : projetos) {
            saldoTotal += p.getSaldo();
            if (p.getSaldo() > saldoMaximo) saldoMaximo = p.getSaldo();
            if (p.getSaldo() < 5000) countBaixoSaldo++;
            String status = p.getNomeStatus() != null ? p.getNomeStatus() : "Outros";
            statusMap.put(status, Objects.requireNonNull(statusMap.getOrDefault(status, 0)) + 1);
            try {
                Date dataVenc = dateFormat.parse(p.getDataVigencia());
                if (dataVenc != null && dataVenc.before(cal90Dias.getTime())) {
                    countVencendo++;
                    proximosVencimentos.add(p);
                }
            } catch (Exception ignored) {}
        }

        binding.dashTotalSaldo.setText(fmt.format(saldoTotal));
        binding.dashAvgSaldo.setText(fmt.format(saldoTotal / projetos.size()));
        binding.dashMaxSaldo.setText(fmt.format(saldoMaximo));
        binding.dashCountAtivos.setText(String.valueOf(projetos.size()));
        binding.dashCountVencendo.setText(String.valueOf(countVencendo));
        binding.dashCountCritico.setText(String.valueOf(countBaixoSaldo));

        List<Projeto> topSaldos = new ArrayList<>(projetos);
        topSaldos.sort((p1, p2) -> Double.compare(p2.getSaldo(), p1.getSaldo()));
        configurarBarChart(topSaldos);
        configurarPieChart(statusMap);
        preencherListaVencimentos(proximosVencimentos);
    }

    private void configurarBarChart(List<Projeto> projetos) {
        BarChart barChart = binding.dashBarChart;
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int limite = Math.min(5, projetos.size());
        for (int i = 0; i < limite; i++) {
            Projeto p = projetos.get(i);
            entries.add(new BarEntry(i, (float) p.getSaldo()));
            String label = p.getNomeConvenio().length() > 8 ? p.getNomeConvenio().substring(0, 6) + ".." : p.getNomeConvenio();
            labels.add(label);
        }
        BarDataSet dataSet = new BarDataSet(entries, "Saldo por Projeto");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(10f);
        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getXAxis().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
    }

    private void configurarPieChart(Map<String, Integer> statusMap) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.PASTEL_COLORS);
        dataSet.setSliceSpace(2f);
        PieData data = new PieData(dataSet);
        binding.dashChartStatus.setData(data);
        binding.dashChartStatus.setHoleRadius(40f);
        binding.dashChartStatus.setCenterText("Status");
        binding.dashChartStatus.getDescription().setEnabled(false);
        binding.dashChartStatus.animateXY(800, 800);
        binding.dashChartStatus.invalidate();
    }

    private void preencherListaVencimentos(List<Projeto> projetos) {
        LinearLayout container = binding.layoutVencimentos;
        container.removeAllViews();
        if (projetos.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Nenhum projeto com vencimento próximo.");
            empty.setPadding(20, 20, 20, 20);
            empty.setTextColor(Color.GRAY);
            container.addView(empty);
            return;
        }
        for (int i = 0; i < Math.min(3, projetos.size()); i++) {
            Projeto p = projetos.get(i);
            View item = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, container, false);
            ((TextView)item.findViewById(android.R.id.text1)).setText(p.getNomeConvenio());
            ((TextView)item.findViewById(android.R.id.text2)).setText(String.format("Vence em: %s", p.getDataVigencia()));
            ((TextView)item.findViewById(android.R.id.text2)).setTextColor(Color.RED);
            container.addView(item);
        }
    }
}
