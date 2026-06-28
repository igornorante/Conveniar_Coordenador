package com.example.conveniar_coordenador;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.conveniar_coordenador.database.AppRepository;
import com.example.conveniar_coordenador.database.PedidoEntity;
import com.example.conveniar_coordenador.database.PedidoPagamentoEntity;
import com.example.conveniar_coordenador.databinding.ActivityPendenciasDetalheBinding;
import java.util.ArrayList;
import java.util.List;

public class PendenciasDetalheActivity extends BaseActivity {

    private ActivityPendenciasDetalheBinding binding;
    private AppRepository repository;
    private PendenciaDetalheAdapter adapter;
    private List<Object> listaFiltrada = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPendenciasDetalheBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new AppRepository(this);
        setupDrawer();

        String tipo = getIntent().getStringExtra("TIPO");
        String status = getIntent().getStringExtra("STATUS");

        binding.txtDetalheTitulo.setText(tipo);
        binding.txtDetalheSubtitulo.setText("Status: " + status);

        configurarLista(tipo, status);
    }

    private void configurarLista(String tipo, String status) {
        binding.recyclerPedidosDetalhe.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendenciaDetalheAdapter(listaFiltrada, token);
        binding.recyclerPedidosDetalhe.setAdapter(adapter);

        if ("Pedido de Compra/Serviço".equals(tipo)) {
            repository.getTodosPedidos().observe(this, pedidos -> {
                listaFiltrada.clear();
                for (PedidoEntity p : pedidos) {
                    if (status != null && status.equals(p.situacao)) {
                        listaFiltrada.add(p);
                    }
                }
                adapter.notifyDataSetChanged();
            });
        } else {
            repository.getTodosPedidosPagamento().observe(this, pagamentos -> {
                listaFiltrada.clear();
                for (PedidoPagamentoEntity p : pagamentos) {
                    if (tipo != null && tipo.equals(p.nomeTipoPedido) && status != null && status.equals(p.nomeStatus)) {
                        listaFiltrada.add(p);
                    }
                }
                adapter.notifyDataSetChanged();
            });
        }
    }
}
