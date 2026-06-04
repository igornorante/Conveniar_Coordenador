package com.example.conveniar_coordenador;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView txt_politica = findViewById(R.id.txt_politica);
        TextView txt_informacoes = findViewById(R.id.txt_informacoes);
        Button botao_entrar = findViewById(R.id.login_botao_entrar);

        txt_politica.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, PoliticaActivity.class);
            startActivity(intent);
        });

        txt_informacoes.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, InformacoesActivity.class);
            startActivity(intent);
        });

        botao_entrar.setOnClickListener(v -> {

            EditText usuario = findViewById(R.id.usuario);
            EditText senha = findViewById(R.id.senha);

            String usuarioDigitado = usuario.getText().toString();
            String senhaDigitada = senha.getText().toString();

            if (usuarioDigitado.isEmpty() || senhaDigitada.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Preencha usuário e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(LoginActivity.this, "Gerando token...", Toast.LENGTH_SHORT).show();

            TokenGenerator.gerarToken(usuarioDigitado, senhaDigitada, new TokenGenerator.TokenCallback() {

                @Override
                public void onTokenGerado(String token) {
                    Log.d("FLUXO_API", "Token gerado com sucesso: " + token);

                    Coordenador.getEventosUsuario(token, 1, 50, new okhttp3.Callback() {

                        @Override
                        public void onFailure(okhttp3.Call call, java.io.IOException e) {
                            Log.e("FLUXO_API", "Erro ao buscar eventos", e);
                        }

                        @Override
                        public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                            if (response.isSuccessful() && response.body() != null) {
                                String jsonEventos = response.body().string();
                                Log.d("FLUXO_API", "Eventos carregados: " + jsonEventos);

                                String nomeExtraido = "Usuário";
                                try {
                                    JSONArray jsonArray = new JSONArray(jsonEventos);
                                    if (jsonArray.length() > 0) {
                                        // Extrai o nome do coordenador do primeiro evento retornado
                                        nomeExtraido = jsonArray.getJSONObject(0).optString("nomeCoordenador", "Usuário");
                                    }
                                } catch (Exception e) {
                                    Log.e("FLUXO_API", "Erro ao processar nome do coordenador", e);
                                }

                                // Salva o nome no SharedPreferences para centralizar o acesso
                                getSharedPreferences("USUARIO", MODE_PRIVATE)
                                        .edit()
                                        .putString("NOME", nomeExtraido)
                                        .apply();

                                runOnUiThread(() -> {
                                    Intent intent = new Intent(LoginActivity.this, PedidosActivity.class);
                                    intent.putExtra("TOKEN", token);
                                    startActivity(intent);
                                });

                            } else {
                                Log.e("FLUXO_API", "Falha ao carregar eventos. Código: " + response.code());
                            }
                        }
                    });
                }

                @Override
                public void onErro(String mensagem) {
                    Log.e("FLUXO_API", "Erro no processo: " + mensagem);
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Erro: " + mensagem, Toast.LENGTH_LONG).show());
                }
            });
        });
    }
}
