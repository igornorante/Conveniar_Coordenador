package com.example.conveniar_coordenador;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.MediatorLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.conveniar_coordenador.database.AppRepository;
import com.example.conveniar_coordenador.database.PedidoEntity;
import com.example.conveniar_coordenador.database.PedidoPagamentoEntity;
import com.example.conveniar_coordenador.databinding.ActivityPendenciasBinding;
import com.example.conveniar_coordenador.model.PendenciaGrupo;
import com.example.conveniar_coordenador.model.PendenciaStatus;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class PendenciasActivity extends BaseActivity {

    private ActivityPendenciasBinding binding;
    private AppRepository repository;
    private boolean isAbaMinhas = true;

    private final MediatorLiveData<List<PendenciaGrupo>> pendenciasLiveData = new MediatorLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPendenciasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent, (v, insets) -> {
            v.setPadding(insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
            return insets;
        });

        repository = new AppRepository(this);
        setupDrawer();
        setupTabs();
        setupRecyclerView();
        iniciarObservacaoDados();
        
        // Estado inicial
        updateTabUI(true);
    }

    private void setupRecyclerView() {
        binding.recyclerPendencias.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerPendencias.setAdapter(new PendenciasAdapter(new ArrayList<>(), token));
    }

    private void setupTabs() {
        binding.tabSuasPendencias.setOnClickListener(v -> updateTabUI(true));
        binding.tabPendenciasFundacao.setOnClickListener(v -> updateTabUI(false));
    }

    private void iniciarObservacaoDados() {
        pendenciasLiveData.addSource(repository.getTodosPedidos(), pedidos -> processarDados());
        pendenciasLiveData.addSource(repository.getTodosPedidosPagamento(), pagamentos -> processarDados());

        pendenciasLiveData.observe(this, grupos -> {
            binding.recyclerPendencias.setAdapter(new PendenciasAdapter(grupos, token));

            int total = 0, ajustes = 0, envios = 0, aprovacoes = 0;
            for (PendenciaGrupo g : grupos) {
                total += g.getTotal();
                for (PendenciaStatus s : g.getStatusList()) {
                    if ("ajuste".equals(s.getTipo())) ajustes += s.getQuantidade();
                    else if ("envio".equals(s.getTipo())) envios += s.getQuantidade();
                    else aprovacoes += s.getQuantidade();
                }
            }

            binding.txtTotalPendenciasResumo.setText(String.valueOf(total));
            binding.txtResumoAjustes.setText(String.valueOf(ajustes));
            binding.txtResumoEnvios.setText(String.valueOf(envios));
            binding.txtResumoAprovacoes.setText(String.valueOf(aprovacoes));
        });
    }

    private void processarDados() {
        List<PedidoEntity> pedidos = repository.getTodosPedidos().getValue();
        List<PedidoPagamentoEntity> pagamentos = repository.getTodosPedidosPagamento().getValue();

        if (pedidos == null && pagamentos == null) return;

        Map<String, Map<String, Integer>> mapaAgrupado = new TreeMap<>();
        processarPedidos(pedidos, mapaAgrupado);
        processarPagamentos(pagamentos, mapaAgrupado);

        List<PendenciaGrupo> resultado = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : mapaAgrupado.entrySet()) {
            List<PendenciaStatus> statusList = new ArrayList<>();
            int totalGrupo = 0;
            for (Map.Entry<String, Integer> sEntry : entry.getValue().entrySet()) {
                statusList.add(new PendenciaStatus(sEntry.getKey(), sEntry.getValue(), mapearTipoStatus(sEntry.getKey())));
                totalGrupo += sEntry.getValue();
            }
            resultado.add(new PendenciaGrupo(entry.getKey(), totalGrupo, statusList));
        }
        pendenciasLiveData.setValue(resultado);
    }

    private void processarPedidos(List<PedidoEntity> list, Map<String, Map<String, Integer>> mapa) {
        if (list == null) return;
        for (PedidoEntity p : list) {
            if (isStatusFinalizado(p.situacao)) continue;
            String grupo = !isAbaMinhas ? "[" + extrairFundacao(p.jsonOriginal) + "] Compra/Serviço" : "Pedido de Compra/Serviço";
            agrupar(mapa, grupo, p.situacao);
        }
    }

    private void processarPagamentos(List<PedidoPagamentoEntity> list, Map<String, Map<String, Integer>> mapa) {
        if (list == null) return;
        for (PedidoPagamentoEntity p : list) {
            if (isStatusFinalizado(p.nomeStatus)) continue;
            String tipo = p.nomeTipoPedido != null ? p.nomeTipoPedido : "Outros";
            String grupo = !isAbaMinhas ? "[" + extrairFundacao(p.jsonOriginal) + "] " + tipo : tipo;
            agrupar(mapa, grupo, p.nomeStatus);
        }
    }

    private String extrairFundacao(String json) {
        try {
            if (json == null || json.isEmpty()) return "Geral";
            JSONObject obj = new JSONObject(json);
            return obj.optString("nomeFundacao", obj.optString("siglaFundacao", "Geral"));
        } catch (Exception e) { return "Geral"; }
    }

    private void agrupar(Map<String, Map<String, Integer>> mapa, String grupo, String status) {
        String s = (status == null || status.isEmpty()) ? "Pendente" : status;
        mapa.computeIfAbsent(grupo, k -> new HashMap<>()).merge(s, 1, Integer::sum);
    }

    private boolean isStatusFinalizado(String status) {
        if (status == null) return false;
        String s = status.toLowerCase();
        return s.contains("finalizado") || s.contains("cancelado") || s.contains("reprovado") || s.contains("concluído");
    }

    private String mapearTipoStatus(String status) {
        String s = status.toLowerCase();
        if (s.contains("devolvido") || s.contains("ajuste")) return "ajuste";
        if (s.contains("envio") || s.contains("registrado")) return "envio";
        return "aprovacao";
    }

    private void updateTabUI(boolean minhasSelected) {
        this.isAbaMinhas = minhasSelected;
        int activeColor = ContextCompat.getColor(this, R.color.azul_conveniar);
        int inactiveTextColor = Color.parseColor("#64748B");

        // Aba Minhas
        binding.tabSuasPendencias.setTextColor(minhasSelected ? Color.WHITE : inactiveTextColor);
        binding.tabSuasPendencias.setBackgroundResource(minhasSelected ? R.drawable.borda_arredondada_status : 0);
        if (minhasSelected) {
            binding.tabSuasPendencias.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            binding.tabSuasPendencias.setElevation(4f);
        } else {
            binding.tabSuasPendencias.setBackground(null);
            binding.tabSuasPendencias.setElevation(0f);
        }
        binding.tabSuasPendencias.setTypeface(null, minhasSelected ? Typeface.BOLD : Typeface.NORMAL);

        // Aba Fundação
        binding.tabPendenciasFundacao.setTextColor(!minhasSelected ? Color.WHITE : inactiveTextColor);
        binding.tabPendenciasFundacao.setBackgroundResource(!minhasSelected ? R.drawable.borda_arredondada_status : 0);
        if (!minhasSelected) {
            binding.tabPendenciasFundacao.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
            binding.tabPendenciasFundacao.setElevation(4f);
        } else {
            binding.tabPendenciasFundacao.setBackground(null);
            binding.tabPendenciasFundacao.setElevation(0f);
        }
        binding.tabPendenciasFundacao.setTypeface(null, !minhasSelected ? Typeface.BOLD : Typeface.NORMAL);

        processarDados();
    }
}