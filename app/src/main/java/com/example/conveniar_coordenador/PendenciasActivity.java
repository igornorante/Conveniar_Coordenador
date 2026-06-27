package com.example.conveniar_coordenador;

import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.conveniar_coordenador.databinding.ActivityPendenciasBinding;
import com.example.conveniar_coordenador.model.PendenciaGrupo;
import com.example.conveniar_coordenador.model.PendenciaStatus;
import java.util.ArrayList;
import java.util.List;

public class PendenciasActivity extends BaseActivity {

    private ActivityPendenciasBinding binding;
    private PendenciasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPendenciasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupDrawer();
        setupTabs();
        setupRecyclerView();
    }

    private void setupTabs() {
        binding.tabSuasPendencias.setOnClickListener(v -> {
            updateTabUI(true);
            adapter = new PendenciasAdapter(getSuasPendenciasMock());
            binding.recyclerPendencias.setAdapter(adapter);
        });

        binding.tabPendenciasFundacao.setOnClickListener(v -> {
            updateTabUI(false);
            adapter = new PendenciasAdapter(getPendenciasFundacaoMock());
            binding.recyclerPendencias.setAdapter(adapter);
        });
    }

    private void updateTabUI(boolean suasSelected) {
        int activeColor = ContextCompat.getColor(this, R.color.black);
        int inactiveColor = ContextCompat.getColor(this, R.color.cinza_texto);
        int divisorColor = ContextCompat.getColor(this, R.color.divisor);

        binding.txtTabSuas.setTextColor(suasSelected ? activeColor : inactiveColor);
        binding.txtTabSuas.setTypeface(null, suasSelected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        binding.indicatorSuas.setBackgroundColor(suasSelected ? activeColor : ContextCompat.getColor(this, android.R.color.transparent));

        binding.txtTabFundacao.setTextColor(!suasSelected ? activeColor : inactiveColor);
        binding.txtTabFundacao.setTypeface(null, !suasSelected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        binding.indicatorFundacao.setBackgroundColor(!suasSelected ? activeColor : ContextCompat.getColor(this, android.R.color.transparent));
        
        // Mantém a linha de divisão se não estiver selecionada
        if (suasSelected) {
             binding.indicatorFundacao.setBackgroundColor(divisorColor);
        } else {
             binding.indicatorSuas.setBackgroundColor(divisorColor);
        }
    }

    private void setupRecyclerView() {
        binding.recyclerPendencias.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendenciasAdapter(getSuasPendenciasMock());
        binding.recyclerPendencias.setAdapter(adapter);
    }

    private List<PendenciaGrupo> getSuasPendenciasMock() {
        List<PendenciaGrupo> grupos = new ArrayList<>();

        List<PendenciaStatus> status1 = new ArrayList<>();
        status1.add(new PendenciaStatus("Aguardando envio", 5, "envio"));
        status1.add(new PendenciaStatus("Aguardando aprovação", 5, "aprovacao"));
        status1.add(new PendenciaStatus("Devolvido para ajuste", 5, "ajuste"));
        grupos.add(new PendenciaGrupo("Acerto de adiantamento", 15, status1));

        grupos.add(new PendenciaGrupo("Pagamento de Bolsa", 10, new ArrayList<>()));
        grupos.add(new PendenciaGrupo("Pagamento de Bolsa em Lote", 3, new ArrayList<>()));
        grupos.add(new PendenciaGrupo("Pagamento de pessoa Física", 7, new ArrayList<>()));
        grupos.add(new PendenciaGrupo("Parecer Técnico", 8, new ArrayList<>()));
        grupos.add(new PendenciaGrupo("Pedido de Compra/Serviço", 10, new ArrayList<>()));

        return grupos;
    }

    private List<PendenciaGrupo> getPendenciasFundacaoMock() {
        List<PendenciaGrupo> grupos = new ArrayList<>();

        // Simulação de separação por fundação
        grupos.add(new PendenciaGrupo("[FUNDAÇÃO A] Acerto de adiantamento", 8, new ArrayList<>()));
        grupos.add(new PendenciaGrupo("[FUNDAÇÃO A] Pagamento de Bolsa", 4, new ArrayList<>()));
        
        grupos.add(new PendenciaGrupo("[FUNDAÇÃO B] Pedido de Compra/Serviço", 12, new ArrayList<>()));
        grupos.add(new PendenciaGrupo("[FUNDAÇÃO B] Parecer Técnico", 3, new ArrayList<>()));

        return grupos;
    }
}
