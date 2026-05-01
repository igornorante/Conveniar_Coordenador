package com.example.conveniar_coordenador;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

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

            //Opção Sair
            if (id == R.id.opc_sair) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);

                drawer.closeDrawer(GravityCompat.START);
                finish();
                return true;
            }
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
    }
}