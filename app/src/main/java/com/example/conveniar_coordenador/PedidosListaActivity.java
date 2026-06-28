package com.example.conveniar_coordenador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.conveniar_coordenador.database.AppRepository;
import com.example.conveniar_coordenador.database.PedidoEntity;
import com.example.conveniar_coordenador.database.PedidoPagamentoEntity;
import com.example.conveniar_coordenador.databinding.ActivityPedidosListaBinding;
import java.util.ArrayList;
import java.util.List;

public class PedidosListaActivity extends BaseActivity {

    private ActivityPedidosListaBinding binding;
    private AppRepository repository;
    private PendenciaDetalheAdapter adapter;
    private List<Object> listaPedidos = new ArrayList<>();
    private String tipoNome;
    private String urlNovo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPedidosListaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new AppRepository(this);
        setupDrawer();

        tipoNome = getIntent().getStringExtra("TIPO_NOME");
        urlNovo = getIntent().getStringExtra("URL_NOVO");

        binding.txtTituloTipo.setText(tipoNome);

        setupRecyclerView();
        carregarDados();

        binding.fabNovoPedido.setOnClickListener(v -> {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra("URL", urlNovo);
            intent.putExtra("TOKEN", token);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        binding.recyclerPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendenciaDetalheAdapter(listaPedidos, token);
        binding.recyclerPedidos.setAdapter(adapter);
    }

    private void carregarDados() {
        if ("Compra/Serviço".equals(tipoNome) || "Pedido de Compra/Serviço".equals(tipoNome)) {
            repository.getTodosPedidos().observe(this, pedidos -> {
                listaPedidos.clear();
                if (pedidos != null) {
                    listaPedidos.addAll(pedidos);
                }
                adapter.notifyDataSetChanged();
                binding.txtContadorLista.setText(listaPedidos.size() + " pedidos encontrados");
            });
        } else {
            repository.getTodosPedidosPagamento().observe(this, pagamentos -> {
                listaPedidos.clear();
                if (pagamentos != null) {
                    for (PedidoPagamentoEntity p : pagamentos) {
                        if (tipoNome.equalsIgnoreCase(p.nomeTipoPedido)) {
                            listaPedidos.add(p);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                binding.txtContadorLista.setText(listaPedidos.size() + " pedidos encontrados");
            });
        }
    }
}
