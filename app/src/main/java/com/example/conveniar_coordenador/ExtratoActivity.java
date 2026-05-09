package com.example.conveniar_coordenador;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;

public class ExtratoActivity extends AppCompatActivity {

    private EditText edtDataInicio;
    private EditText edtDataFim;
    private Spinner spinnerProjetos;
    private Button btnGerar;
    private ListView listExtrato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_extrato);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializando os componentes da tela
        edtDataInicio = findViewById(R.id.edt_data_inicio);
        edtDataFim = findViewById(R.id.edt_data_fim);
        spinnerProjetos = findViewById(R.id.spinner_projetos);
        btnGerar = findViewById(R.id.btn_gerar_extrato);
        listExtrato = findViewById(R.id.list_extrato);

        configurarMenuLateral();
        configurarFiltros();
    }

    private void configurarMenuLateral() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ImageView btnMenu = findViewById(R.id.menu_header);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        }

        NavigationView navigationView = findViewById(R.id.navigation_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.opc_extrato) {
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                }

                if (id == R.id.opc_sair) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    drawer.closeDrawer(GravityCompat.START);
                    finish();
                    return true;
                }
                return true;
            });
        }
    }

    private void configurarFiltros() {
        // 1. Populando o Spinner com dados falsos (Mock)
        String[] projetosFalsos = {
                "Selecione um projeto...",
                "Projeto 01 - Desenvolvimento de Software",
                "Projeto 02 - Pesquisa Agrícola",
                "Projeto 03 - Extensão Universitária"
        };
        ArrayAdapter<String> adapterProjetos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, projetosFalsos);
        spinnerProjetos.setAdapter(adapterProjetos);

        // 2. Configurando o clique nos campos de data para abrir o Calendário
        edtDataInicio.setOnClickListener(v -> abrirCalendario(edtDataInicio));
        edtDataFim.setOnClickListener(v -> abrirCalendario(edtDataFim));

        // 3. Ação do Botão "Gerar Extrato"
        btnGerar.setOnClickListener(v -> {
            String projetoSelecionado = spinnerProjetos.getSelectedItem().toString();
            String dataInicio = edtDataInicio.getText().toString();
            String dataFim = edtDataFim.getText().toString();

            // Validação simples
            if (projetoSelecionado.equals("Selecione um projeto...") || dataInicio.isEmpty() || dataFim.isEmpty()) {
                Toast.makeText(this, "Preencha todos os filtros antes de gerar!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Buscando dados de " + dataInicio + " até " + dataFim, Toast.LENGTH_LONG).show();
                // O próximo passo será colocar os dados financeiros na lista (listExtrato) aqui dentro
            }
        });
    }

    // Método auxiliar para criar e mostrar o calendário
    private void abrirCalendario(EditText campoData) {
        Calendar calendario = Calendar.getInstance();
        int ano = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Formata a data para DD/MM/AAAA (ex: 05/08/2026)
            String dataFormatada = String.format("%02d/%02d/%04d", dayOfMonth, (month + 1), year);
            campoData.setText(dataFormatada);
        }, ano, mes, dia);

        dialog.show();
    }
}