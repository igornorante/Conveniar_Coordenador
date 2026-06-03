package com.example.conveniar_coordenador;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class PedidosActivity extends AppCompatActivity {
    private String token;
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

        token = getIntent().getStringExtra("TOKEN");

    }

    @Override
    protected void onResume() {
        super.onResume();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ImageView btnMenu = findViewById(R.id.menu_header);


        btnMenu.setOnClickListener(v -> {
            drawer.openDrawer(GravityCompat.START);
        });


        //Tratamento de clique das opções do menu lateral
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.opc_projetos) {
                Intent intent = new Intent(this, ProjetosActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
            } else if (id == R.id.opc_extrato) {
                Intent intent = new Intent(this, ExtratoActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
            } else if (id == R.id.opc_saldo) {
                Intent intent = new Intent(this, SaldoActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
            } else if (id == R.id.opc_consultas) {
                Intent intent = new Intent(this, ConsultaActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
            } else if (id == R.id.opc_pedidos) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.opc_sair) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finishAffinity();
                return true;
            }

            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        ListView lista_opcoes = (ListView) findViewById(R.id.list_opcoes_pedidos);

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
        lista.add(new ItemMenu("Parecer Técnico", R.drawable.icone_transferencia)); //Falta icone
        lista.add(new ItemMenu("Pagamento de Bolsa em Lote", R.drawable.icone_transferencia)); //Falta icone
        lista.add(new ItemMenu("Ordens de Pagamento de AF/OS", R.drawable.icone_transferencia)); //Falta icone
        lista.add(new ItemMenu("Contratação de Pessoas", R.drawable.icone_transferencia)); //Falta icone


        MenuAdapter adapter = new MenuAdapter(this, lista);
        lista_opcoes.setAdapter(adapter);

        lista_opcoes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Navegar_Portal(position);
            }
        });

    }

    public void Navegar_Portal(int posicao){
        Uri uri = null;

        switch (posicao){

            case 0: // Compra/Serviço
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoCompra.aspx");
                break;

            case 1: // Adiantamento
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoAdiantamento.aspx");
                break;

            case 2: // Acerto de Adiantamento
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoAcertoAdiantamento.aspx");
                break;

            case 3: // Reembolso
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoReembolso.aspx");
                break;

            case 4: // Pagamento de Diárias/Frete
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoDiaria.aspx");
                break;

            case 5: // Pagamento de Bolsa
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoBolsa.aspx");
                break;

            case 6: // Pessoa Jurídica
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoPessoaJuridica.aspx");
                break;

            case 7: // Pessoa Física
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoPessoaFisica.aspx");
                break;

            case 8: // Entrada de Receita
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/EntradaDeReceita.aspx");
                break;

            case 9: // Transferência Entre Projetos
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoTransferencia.aspx");
                break;

            case 10: // Parecer Técnico
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoParecerTecnico.aspx");
                break;

            case 11: // Bolsa em Lote
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PagamentoBolsaLote.aspx");
                break;

            case 12: // OP AF/OS
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/OPCompraAF.aspx");
                break;

            case 13: // Contratação de Pessoas
                uri = Uri.parse("https://cientec.conveniar.com.br/Coordenador/Forms/Pesquisador/PedidoContratacao.aspx");
                break;

            default:
                Toast.makeText(this, "Opção inválida", Toast.LENGTH_SHORT).show();
                return;
        }

        Intent it_site = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(it_site);
    }
}