package com.example.conveniar_coordenador;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SecurePrefsManager {

    // Usamos um nome de arquivo diferente do antigo para evitar conflito com os dados antigos em texto plano
    private static final String FILE_NAME = "SecureAppPrefs";

    public static SharedPreferences get(Context context) {
        try {
            // Cria a chave-mestre usando o Android Keystore (o local mais seguro do hardware)
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Retorna a instância do SharedPreferences Criptografado
            return EncryptedSharedPreferences.create(
                    context,
                    FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Criptografa as chaves
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM  // Criptografa os valores
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Retorna null se houver falha na geração da chave
        }
    }
}
