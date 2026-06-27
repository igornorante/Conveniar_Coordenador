package com.example.conveniar_coordenador.database;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.conveniar_coordenador.SecurePrefsManager;

// Se você adicionar mais tabelas no futuro, basta incluí-las no array 'entities'
@Database(entities = {ProjetoEntity.class, PedidoEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AppDAO appDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    SharedPreferences prefs = SecurePrefsManager.get(context);
                    String usuario = "desconhecido";

                    if (prefs != null) {
                        // Limpa caracteres especiais do nome de usuário para evitar erros no nome do arquivo
                        usuario = prefs.getString("usuario_login", "desconhecido").replaceAll("[^a-zA-Z0-9]", "_");
                    }

                    // 2. Define o nome do arquivo dinamicamente (Ex: conveniar_db_joao)
                    String nomeArquivoBanco = "conveniar_db_" + usuario;

                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, nomeArquivoBanco)
                            // Evita que o app trave caso você mude a estrutura do banco no futuro (apaga e recria)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void encerrarConexao() {
        if (INSTANCE != null) {
            if (INSTANCE.isOpen()) {
                INSTANCE.close();
            }
            // "Mata" a instância atual para que o próximo usuário que logar crie uma nova
            INSTANCE = null;
        }
    }
}
