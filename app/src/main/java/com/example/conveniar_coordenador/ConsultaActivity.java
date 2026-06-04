package com.example.conveniar_coordenador;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class ConsultaActivity extends AppCompatActivity {

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);

        token = getIntent().getStringExtra("TOKEN");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ImageView btnMenu = findViewById(R.id.menu_header);

        btnMenu.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.opc_projetos) {
                Intent intent = new Intent(this, ProjetosActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
                finish();
            } else if (id == R.id.opc_extrato) {
                Intent intent = new Intent(this, ExtratoActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
                finish();
            } else if (id == R.id.opc_saldo) {
                Intent intent = new Intent(this, SaldoActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
                finish();
            } else if (id == R.id.opc_pedidos) {
                Intent intent = new Intent(this, PedidosActivity.class);
                intent.putExtra("TOKEN", token);
                startActivity(intent);
                finish();
            } else if (id == R.id.opc_consultas) {
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
    }
}