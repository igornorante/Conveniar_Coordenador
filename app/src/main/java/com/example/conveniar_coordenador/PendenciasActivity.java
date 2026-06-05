package com.example.conveniar_coordenador;

import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.conveniar_coordenador.model.PendenciaGrupo;
import com.example.conveniar_coordenador.model.PendenciaStatus;

import java.util.ArrayList;
import java.util.List;

public class PendenciasActivity extends BaseActivity {

    private ExpandableListView expListView;
    private ProgressBar progressBar;
    private PendenciasAdapter adapter;
    private List<PendenciaGrupo> listGrupos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pendencias);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupDrawer();

        expListView = findViewById(R.id.exp_list_pendencias);
        progressBar = findViewById(R.id.progress_pendencias);
        listGrupos = new ArrayList<>();
        adapter = new PendenciasAdapter(this, listGrupos);
        expListView.setAdapter(adapter);

        configurarTabs();
        
        // Força o carregamento dos mocks já que a API está fora
        carregarPendenciasMock();
    }

    private void configurarTabs() {
        TextView tabSuas = findViewById(R.id.tab_suas_pendencias);
        TextView tabFundacao = findViewById(R.id.tab_pendencias_fundacao);

        tabSuas.setOnClickListener(v -> {
            tabSuas.setTextColor(getResources().getColor(R.color.azul_conveniar));
            tabFundacao.setTextColor(0xFF999999);
            carregarPendenciasMock();
        });

        tabFundacao.setOnClickListener(v -> {
            tabFundacao.setTextColor(getResources().getColor(R.color.azul_conveniar));
            tabSuas.setTextColor(0xFF999999);
            listGrupos.clear();
            adapter.notifyDataSetChanged();
        });
    }

    private void carregarPendenciasMock() {
        listGrupos.clear();
        
        // 1. Acerto de adiantamento (Com sub-itens para o efeito expansível)
        List<PendenciaStatus> statusAcerto = new ArrayList<>();
        statusAcerto.add(new PendenciaStatus("Aguardando envio", 5, "envio"));
        statusAcerto.add(new PendenciaStatus("Aguardando aprovação", 5, "aprovacao"));
        statusAcerto.add(new PendenciaStatus("Devolvido para ajuste", 5, "ajuste"));
        listGrupos.add(new PendenciaGrupo("Acerto de adiantamento", 15, statusAcerto));

        // 2. Outras categorias conforme as imagens
        listGrupos.add(new PendenciaGrupo("Entrada de receita", 6, new ArrayList<>()));
        listGrupos.add(new PendenciaGrupo("Ordem de pagamento de AF/OS", 9, new ArrayList<>()));
        listGrupos.add(new PendenciaGrupo("Pagamento de Adiantamento", 12, new ArrayList<>()));
        listGrupos.add(new PendenciaGrupo("Pagamento de Bolsa", 10, new ArrayList<>()));
        listGrupos.add(new PendenciaGrupo("Pagamento de Bolsa em Lote", 3, new ArrayList<>()));
        listGrupos.add(new PendenciaGrupo("Pagamento de pessoa Física", 7, new ArrayList<>()));
        listGrupos.add(new PendenciaGrupo("Parecer Técnico", 8, new ArrayList<>()));
        listGrupos.add(new PendenciaGrupo("Pedido de Compra/Serviço", 10, new ArrayList<>()));

        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }
}
