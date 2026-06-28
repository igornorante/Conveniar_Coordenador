package com.example.conveniar_coordenador;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {


    private static final int CODIGO_PERMISSAO_NOTIFICACAO = 100;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, CODIGO_PERMISSAO_NOTIFICACAO);
            }
        }

        // Configuração do Worker Periódico
        Constraints restricoes = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest apiCheckRequest = new PeriodicWorkRequest.Builder(
                ApiCheckWorker.class,
                15, TimeUnit.MINUTES
        ).setConstraints(restricoes).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "VerificacaoDeApi",
                ExistingPeriodicWorkPolicy.KEEP,
                apiCheckRequest
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView txt_politica = findViewById(R.id.txt_politica);
        TextView txt_informacoes = findViewById(R.id.txt_informacoes);
        Button botao_entrar = findViewById(R.id.login_botao_entrar);

        txt_politica.setOnClickListener(v -> startActivity(new Intent(this, PoliticaActivity.class)));
        txt_informacoes.setOnClickListener(v -> startActivity(new Intent(this, InformacoesActivity.class)));

        botao_entrar.setOnClickListener(v -> {
            EditText usuario = findViewById(R.id.usuario);
            EditText senha = findViewById(R.id.senha);

            String user = usuario.getText().toString();
            String pass = senha.getText().toString();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Preencha usuário e senha", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Autenticando...", Toast.LENGTH_SHORT).show();

            TokenGenerator.gerarToken(user, pass, new TokenGenerator.TokenCallback() {
                @Override
                public void onTokenGerado(String token) {
                    SharedPreferences securePrefs = SecurePrefsManager.get(LoginActivity.this);
                    if (securePrefs != null) {
                        securePrefs.edit()
                                .putString("usuario_login", user)
                                .putString("senha_login", pass)
                                .apply();
                    }

                    // Dispara sincronização imediata após login bem-sucedido
                    Constraints constraints = new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build();
                    OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(ApiCheckWorker.class)
                            .setConstraints(constraints)
                            .build();
                    WorkManager.getInstance(LoginActivity.this).enqueue(syncRequest);

                    Coordenador.getEventosUsuario(token, 1, 10, new okhttp3.Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            entrarNoApp(token, "Coordenador");
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String nomeExtraido = "Coordenador";
                            if (response.isSuccessful() && response.body() != null) {
                                try {
                                    String json = response.body().string();
                                    JSONArray array = new JSONArray(json);
                                    if (array.length() > 0) {
                                        nomeExtraido = array.getJSONObject(0).optString("nomeCoordenador", "Coordenador");
                                    }
                                } catch (Exception ignored) {}
                            }
                            entrarNoApp(token, nomeExtraido);
                        }
                    });
                }

                @Override
                public void onErro(String mensagem) {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, mensagem, Toast.LENGTH_LONG).show());
                }
            });
        });
    }

    private void entrarNoApp(String token, String nome) {
        getSharedPreferences("USUARIO", MODE_PRIVATE).edit().putString("NOME", nome).apply();
        runOnUiThread(() -> {
            Intent intent = new Intent(LoginActivity.this, PrincipalActivity.class);
            intent.putExtra("TOKEN", token);
            startActivity(intent);
            finish();
        });
    }
}
