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
        // Vincula cada Card ao seu respectivo link do portal abrindo na WebView interna
        binding.cardCompra.setOnClickListener(v -> Navegar_Portal(0));
        binding.cardAdiantamento.setOnClickListener(v -> Navegar_Portal(1));
        
        if (binding.cardAcertoAdiantamento != null) {
            binding.cardAcertoAdiantamento.setOnClickListener(v -> Navegar_Portal(2));
        }
        
        binding.cardReembolso.setOnClickListener(v -> Navegar_Portal(3));
        binding.cardDiaria.setOnClickListener(v -> Navegar_Portal(4));
        
        if (binding.cardBolsa != null) {
            binding.cardBolsa.setOnClickListener(v -> Navegar_Portal(5));
        }
        
        binding.cardPagPf.setOnClickListener(v -> Navegar_Portal(7));
        binding.cardPagPj.setOnClickListener(v -> Navegar_Portal(6));
        
        if (binding.cardReceita != null) binding.cardReceita.setOnClickListener(v -> Navegar_Portal(8));
        if (binding.cardTransferencia != null) binding.cardTransferencia.setOnClickListener(v -> Navegar_Portal(9));
        if (binding.cardParecer != null) binding.cardParecer.setOnClickListener(v -> Navegar_Portal(10));
        if (binding.cardBolsaLote != null) binding.cardBolsaLote.setOnClickListener(v -> Navegar_Portal(11));
        if (binding.cardOrdem != null) binding.cardOrdem.setOnClickListener(v -> Navegar_Portal(12));
        if (binding.cardContratacao != null) binding.cardContratacao.setOnClickListener(v -> Navegar_Portal(13));
    }

    public void Navegar_Portal(int posicao) {
        String url;
        switch (posicao) {
            case 0: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoCompra.aspx"; break;
            case 1: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoAdiantamento.aspx"; break;
            case 2: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoAcertoAdiantamento.aspx"; break;
            case 3: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoReembolso.aspx"; break;
            case 4: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoDiaria.aspx"; break;
            case 5: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoBolsa.aspx"; break;
            case 6: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoPessoaJuridica.aspx"; break;
            case 7: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoPessoaFisica.aspx"; break;
            case 8: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/EntradaDeReceita.aspx"; break;
            case 9: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoTransferencia.aspx"; break;
            case 10: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoParecerTecnico.aspx"; break;
            case 11: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoBolsaLote.aspx"; break;
            case 12: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/OPCompraAF.aspx"; break;
            case 13: url = "https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoContratacao.aspx"; break;
            default:
                Toast.makeText(this, "Opção inválida", Toast.LENGTH_SHORT).show();
                return;
        }

        // Abrir na WebViewActivity interna do aplicativo passando o TOKEN
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("URL", url);
        intent.putExtra("TOKEN", token);
        startActivity(intent);
    }
}
