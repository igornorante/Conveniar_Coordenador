package com.example.conveniar_coordenador;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.conveniar_coordenador.databinding.ActivityPedidosBinding;

public class PedidosActivity extends BaseActivity {

    private ActivityPedidosBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityPedidosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Ajuste para respeitar a área da Status Bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Chama o método da BaseActivity que configura o Menu e o Nome
        setupDrawer();

        configurarCliques();
    }

    private void configurarCliques() {
        binding.cardCompra.setOnClickListener(v -> abrirListaPedidos("Pedido de Compra/Serviço", 0));
        binding.cardAdiantamento.setOnClickListener(v -> abrirListaPedidos("Pedido de Adiantamento", 1));
        
        if (binding.cardAcertoAdiantamento != null) {
            binding.cardAcertoAdiantamento.setOnClickListener(v -> abrirListaPedidos("Pedido de Acerto de Adiantamento", 2));
        }
        
        binding.cardReembolso.setOnClickListener(v -> abrirListaPedidos("Pedido de Reembolso", 3));
        binding.cardDiaria.setOnClickListener(v -> abrirListaPedidos("Pagamento Diária", 4));
        
        if (binding.cardBolsa != null) {
            binding.cardBolsa.setOnClickListener(v -> abrirListaPedidos("Pagamento Bolsa", 5));
        }
        
        binding.cardPagPf.setOnClickListener(v -> abrirListaPedidos("Pagamento Pessoa Física", 7));
        binding.cardPagPj.setOnClickListener(v -> abrirListaPedidos("Pagamento Pessoa Jurídica", 6));
        
        if (binding.cardReceita != null) binding.cardReceita.setOnClickListener(v -> abrirListaPedidos("Entrada de Receita", 8));
        if (binding.cardTransferencia != null) binding.cardTransferencia.setOnClickListener(v -> abrirListaPedidos("Pedido de Transferência entre Projetos", 9));
        if (binding.cardParecer != null) binding.cardParecer.setOnClickListener(v -> abrirListaPedidos("Pedido Parecer Técnico", 10));
        if (binding.cardBolsaLote != null) binding.cardBolsaLote.setOnClickListener(v -> abrirListaPedidos("Pagamento Bolsa em Lote", 11));
        if (binding.cardOrdem != null) binding.cardOrdem.setOnClickListener(v -> abrirListaPedidos("Ordens de Pagamento", 12));
        if (binding.cardContratacao != null) binding.cardContratacao.setOnClickListener(v -> abrirListaPedidos("Pedido Contratação", 13));
    }

    private void abrirListaPedidos(String nomeTipo, int posicaoUrl) {
        String urlNovo = getUrlPorPosicao(posicaoUrl);
        if (urlNovo.isEmpty()) {
            Toast.makeText(this, "Opção inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, PedidosListaActivity.class);
        intent.putExtra("TIPO_NOME", nomeTipo);
        intent.putExtra("URL_NOVO", urlNovo);
        intent.putExtra("TOKEN", token);
        startActivity(intent);
    }

    private String getUrlPorPosicao(int posicao) {
        switch (posicao) {
            case 0: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoCompra.aspx";
            case 1: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoAdiantamento.aspx";
            case 2: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoAcertoAdiantamento.aspx";
            case 3: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoReembolso.aspx";
            case 4: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoDiaria.aspx";
            case 5: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoBolsa.aspx";
            case 6: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoPessoaJuridica.aspx";
            case 7: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoPessoaFisica.aspx";
            case 8: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/EntradaDeReceita.aspx";
            case 9: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoTransferencia.aspx";
            case 10: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoParecerTecnico.aspx";
            case 11: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoBolsaLote.aspx";
            case 12: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/OPCompraAF.aspx";
            case 13: return "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoContratacao.aspx";
            default: return "";
        }
    }
}
