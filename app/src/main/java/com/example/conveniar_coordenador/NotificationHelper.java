package com.example.conveniar_coordenador;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private final Context context;
    private static final String CHANNEL_ID = "meu_canal_principal";

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    // Cria o canal de notificação (Necessário para Android 8.0+)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Avisos Gerais";
            String description = "Canal utilizado para notificações gerais do app";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Constrói e dispara a notificação
    public void enviarNotificacao(String titulo, String mensagem) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Substitua pelo ícone do seu app
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // A notificação some quando o usuário clica nela

        // Verifica a permissão para Android 13+ antes de enviar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Se não tem permissão, aborte ou solicite a permissão na Activity
                return;
            }
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        // O ID (neste caso, baseado no tempo) permite enviar múltiplas notificações distintas.
        int notificationId = (int) System.currentTimeMillis();
        notificationManagerCompat.notify(notificationId, builder.build());
    }
}
