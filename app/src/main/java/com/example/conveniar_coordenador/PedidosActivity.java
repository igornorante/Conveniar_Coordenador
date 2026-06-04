package com.example.conveniar_coordenador;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class PedidosActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pedidos);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Chama o método da BaseActivity que configura o Menu e o Nome
        setupDrawer();

        configurarListaPedidos();
    }

    private void configurarListaPedidos() {
        ListView lista_opcoes = findViewById(R.id.list_opcoes_pedidos);
        List<ItemMenu> lista = new ArrayList<>();

        lista.add(new ItemMenu("Compra/Serviço", R.drawable.icone_compra));
        lista.add(new ItemMenu("Adiantamento", R.drawable.icone_adiantamento));
        lista.add(new ItemMenu("Acerto de Adiantamento", R.drawable.icone_acertoadiantamento));
        lista.add(new ItemMenu("Reembolso", R.drawable.icone_reembolso));
        lista.add(new ItemMenu("Pagamento de Diárias/Frete", R.drawable.icone_diaria));
        lista.add(new ItemMenu("Pagamento de Bolsa", R.drawable.icone_pag_bolsa));
        lista.add(new ItemMenu("Pagamento de Pessoa Jurídica", R.drawable.icone_pag_juridica));
        lista.add(new ItemMenu("Pagamento de Pessoa Física", R.drawable.icone_pag_fisica));
        lista.add(new ItemMenu("Entrada de Receita", R.drawable.icone_entradareceita));
        lista.add(new ItemMenu("Transferência Entre Projetos", R.drawable.icone_transferencia));
        lista.add(new ItemMenu("Parecer Técnico", R.drawable.icone_parecer));
        lista.add(new ItemMenu("Pagamento de Bolsa em Lote", R.drawable.icone_bolsalote));
        lista.add(new ItemMenu("Ordens de Pagamento de AF/OS", R.drawable.icone_ordem));
        lista.add(new ItemMenu("Contratação de Pessoas", R.drawable.icone_contratacao));

        MenuAdapter adapter = new MenuAdapter(this, lista);
        lista_opcoes.setAdapter(adapter);

        lista_opcoes.setOnItemClickListener((parent, view, position, id) -> Navegar_Portal(position));
    }

    public void Navegar_Portal(int posicao) {
        Uri uri = null;
        switch (posicao) {
            case 0: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoCompra.aspx"); break;
            case 1: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoAdiantamento.aspx"); break;
            case 2: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoAcertoAdiantamento.aspx"); break;
            case 3: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoReembolso.aspx"); break;
            case 4: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoDiaria.aspx"); break;
            case 5: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoBolsa.aspx"); break;
            case 6: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoPessoaJuridica.aspx"); break;
            case 7: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoPessoaFisica.aspx"); break;
            case 8: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/EntradaDeReceita.aspx"); break;
            case 9: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoTransferencia.aspx"); break;
            case 10: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoParecerTecnico.aspx"); break;
            case 11: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoBolsaLote.aspx"); break;
            case 12: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/OPCompraAF.aspx"); break;
            case 13: uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoContratacao.aspx"); break;
            default:
                Toast.makeText(this, "Opção inválida", Toast.LENGTH_SHORT).show();
                return;
        }
        Intent it_site = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(it_site);
    }
}
