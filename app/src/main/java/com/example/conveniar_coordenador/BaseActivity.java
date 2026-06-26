package com.example.conveniar_coordenador;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Activity base para centralizar a lógica do Menu Lateral
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected String token;
    private ImageView imgPerfil;

    // Launcher para selecionar imagem da galeria
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    String localPath = saveImageLocally(uri);
                    if (localPath != null) {
                        getSharedPreferences("USUARIO", MODE_PRIVATE)
                                .edit()
                                .putString("FOTO_PERFIL_LOCAL", localPath)
                                .apply();

                        if (imgPerfil != null) {
                            imgPerfil.setImageURI(Uri.fromFile(new File(localPath)));
                        }
                        Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private String saveImageLocally(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            File file = new File(getFilesDir(), "profile_picture.jpg");
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    protected void setupDrawer() {
        token = getIntent().getStringExtra("TOKEN");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ImageView btnMenu = findViewById(R.id.menu_header);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        if (btnMenu != null && drawer != null) {
            btnMenu.setOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        }

        if (navigationView != null) {
            //Configura o Nome do Usuário no Header do Menu
            View headerView = navigationView.getHeaderView(0);
            TextView txtNome = headerView.findViewById(R.id.nome_usuario);
            imgPerfil = headerView.findViewById(R.id.img_perfil_lateral);
            View btnEditarFoto = headerView.findViewById(R.id.btn_editar_foto);

            // Carrega Nome
            String nomeSalvo = getSharedPreferences("USUARIO", MODE_PRIVATE).getString("NOME", "Usuário");
            if (txtNome != null) {
                txtNome.setText(nomeSalvo);
            }

            // Carrega Foto Salva do armazenamento interno
            String fotoLocal = getSharedPreferences("USUARIO", MODE_PRIVATE).getString("FOTO_PERFIL_LOCAL", null);
            if (fotoLocal != null && imgPerfil != null) {
                File file = new File(fotoLocal);
                if (file.exists()) {
                    imgPerfil.setImageURI(Uri.fromFile(file));
                }
            }

            // Configura o clique para editar foto
            if (btnEditarFoto != null) {
                btnEditarFoto.setOnClickListener(v -> mGetContent.launch("image/*"));
            }

            // Configura os Cliques do Menu Lateral
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.opc_pedidos && !(this instanceof PedidosActivity)) {
                    intent = new Intent(this, PedidosActivity.class);
                } else if (id == R.id.opc_projetos && !(this instanceof ProjetosActivity)) {
                    intent = new Intent(this, ProjetosActivity.class);
                } else if (id == R.id.opc_extrato && !(this instanceof ExtratoActivity)) {
                    intent = new Intent(this, ExtratoActivity.class);
                } else if (id == R.id.opc_saldo && !(this instanceof SaldoActivity)) {
                    intent = new Intent(this, SaldoActivity.class);
                } else if (id == R.id.opc_consultas && !(this instanceof ConsultaActivity)) {
                    intent = new Intent(this, ConsultaActivity.class);
                } else if (id == R.id.opc_inicio && !(this instanceof PrincipalActivity)) {
                    intent = new Intent(this, PrincipalActivity.class);
                } else if (id == R.id.opc_sair) {
                    intent = new Intent(this, LoginActivity.class);
                    finishAffinity();
                }

                if (intent != null) {
                    intent.putExtra("TOKEN", token);
                    startActivity(intent);
                }

                drawer.closeDrawer(GravityCompat.START);
                return true;
            });
        }
    }
}
