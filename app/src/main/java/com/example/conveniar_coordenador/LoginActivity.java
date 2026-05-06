package com.example.conveniar_coordenador;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        TextView txt_politica = (TextView)findViewById(R.id.txt_politica);
        TextView txt_informacoes = (TextView)findViewById(R.id.txt_informacoes);
        Button botao_entrar = (Button)findViewById(R.id.login_botao_entrar);

        txt_politica.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, PoliticaActivity.class);
            startActivity(intent);
        });

        txt_informacoes.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, InformacoesActivity.class);
            startActivity(intent);
        });

        botao_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, PedidosActivity.class);
                startActivity(intent);
            }
        });

    }
}

/*
    Estou deixando o código que usei para realizar o login aqui, depois basta adaptar para a tela de login que você fez:

    String usuarioDigitado = "...";
    String senhaDigitada = "...";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TokenGenerator.gerarToken(usuarioDigitado, senhaDigitada, new TokenGenerator.TokenCallback() {

            @Override
            public void onTokenGerado(String token) {
                Log.d("FLUXO_API", "Token gerado com sucesso: " + token);

                // Requisicao de eventos usando o token
                Coordenador.getEventosUsuario(token, 1, 50, new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, java.io.IOException e) {
                        Log.e("FLUXO_API", "Erro ao buscar eventos: " + e.getMessage());
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws java.io.IOException {
                        if (response.isSuccessful() && response.body() != null) {
                            String jsonEventos = response.body().string();
                            Log.d("FLUXO_API", "Eventos carregados: " + jsonEventos);

                            // Lembre-se: Para mostrar isso na tela, use runOnUiThread()
                        } else {
                            Log.e("FLUXO_API", "Falha ao carregar eventos. Código: " + response.code());
                        }
                    }
                });
            }

            @Override
            public void onErro(String mensagem) {
                Log.e("FLUXO_API", "Erro no processo: " + mensagem);
            }
        });
    }


 */