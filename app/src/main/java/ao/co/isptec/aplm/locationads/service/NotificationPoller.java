package ao.co.isptec.aplm.locationads.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.ViewAds;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Notificacao;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Serviço de polling para notificações
 * Verifica periodicamente o servidor por novas notificações
 * 
 * Nota: Esta é uma solução temporária. Para produção, use Firebase Cloud Messaging.
 */
public class NotificationPoller {
    
    private static final String TAG = "NotificationPoller";
    private static final String CHANNEL_ID = "AdsNotificationChannel";
    private static final String PREFS_NAME = "NotificationPrefs";
    private static final String KEY_SEEN_NOTIFICATIONS = "seen_notifications";
    
    // Intervalo de polling (em ms)
    private static final long POLL_INTERVAL = 60000; // 1 minuto
    
    private static NotificationPoller instance;
    private Context context;
    private ApiService apiService;
    private Handler handler;
    private Runnable pollingRunnable;
    private boolean isPolling = false;
    private SharedPreferences prefs;
    private int notificationId = 100;
    
    private NotificationPoller(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = ApiClient.getInstance(context).getApiService();
        this.handler = new Handler(Looper.getMainLooper());
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        createNotificationChannel();
        setupPollingRunnable();
    }
    
    public static synchronized NotificationPoller getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationPoller(context);
        }
        return instance;
    }
    
    /**
     * Cria o canal de notificação (necessário para Android 8+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Anúncios",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificações de novos anúncios na sua localização");
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Configura o runnable de polling
     */
    private void setupPollingRunnable() {
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPolling) {
                    checkForNotifications();
                    handler.postDelayed(this, POLL_INTERVAL);
                }
            }
        };
    }
    
    /**
     * Inicia o polling
     */
    public void startPolling() {
        if (!isPolling) {
            isPolling = true;
            handler.post(pollingRunnable);
            Log.d(TAG, "✅ Polling de notificações iniciado");
        }
    }
    
    /**
     * Para o polling
     */
    public void stopPolling() {
        isPolling = false;
        handler.removeCallbacks(pollingRunnable);
        Log.d(TAG, "⏹️ Polling de notificações parado");
    }
    
    /**
     * Verifica se há novas notificações
     */
    private void checkForNotifications() {
        Log.d(TAG, "🔍 Verificando notificações...");
        
        apiService.getNotifications().enqueue(new Callback<List<Notificacao>>() {
            @Override
            public void onResponse(@NonNull Call<List<Notificacao>> call,
                                   @NonNull Response<List<Notificacao>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Notificacao> notifications = response.body();
                    Log.d(TAG, "📬 Recebidas " + notifications.size() + " notificações");
                    
                    processNotifications(notifications);
                } else {
                    Log.e(TAG, "❌ Erro ao buscar notificações: " + response.code());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<List<Notificacao>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Falha ao buscar notificações", t);
            }
        });
    }
    
    /**
     * Processa as notificações recebidas
     */
    private void processNotifications(List<Notificacao> notifications) {
        Set<String> seenIds = getSeenNotificationIds();
        
        for (Notificacao notif : notifications) {
            String notifId = String.valueOf(notif.getId());
            
            // Verificar se já foi vista e se não foi lida
            if (!seenIds.contains(notifId) && !notif.isLido()) {
                showNotification(notif);
                seenIds.add(notifId);
            }
        }
        
        // Salvar IDs vistos
        saveSeenNotificationIds(seenIds);
    }
    
    /**
     * Mostra uma notificação do sistema
     */
    private void showNotification(Notificacao notif) {
        Log.d(TAG, "🔔 Mostrando notificação: " + notif.getTitulo());
        
        // Intent para abrir o anúncio quando clicar
        Intent intent = new Intent(context, ViewAds.class);
        intent.putExtra("ad_id", notif.getMensagemId());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, notif.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Construir notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_ads)
                .setContentTitle(notif.getTitulo() != null ? notif.getTitulo() : "Novo anúncio")
                .setContentText(notif.getConteudo() != null ? notif.getConteudo() : "Toque para ver")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Mostrar
        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId++, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Sem permissão para mostrar notificação", e);
        }
    }
    
    /**
     * Obtém IDs de notificações já vistas
     */
    private Set<String> getSeenNotificationIds() {
        return new HashSet<>(prefs.getStringSet(KEY_SEEN_NOTIFICATIONS, new HashSet<>()));
    }
    
    /**
     * Salva IDs de notificações vistas
     */
    private void saveSeenNotificationIds(Set<String> ids) {
        // Limitar a 1000 IDs para não crescer indefinidamente
        if (ids.size() > 1000) {
            Set<String> trimmed = new HashSet<>();
            int count = 0;
            for (String id : ids) {
                if (count++ >= 500) break;
                trimmed.add(id);
            }
            ids = trimmed;
        }
        
        prefs.edit().putStringSet(KEY_SEEN_NOTIFICATIONS, ids).apply();
    }
    
    /**
     * Força uma verificação imediata
     */
    public void checkNow() {
        checkForNotifications();
    }
    
    /**
     * Limpa o histórico de notificações vistas
     */
    public void clearHistory() {
        prefs.edit().remove(KEY_SEEN_NOTIFICATIONS).apply();
        Log.d(TAG, "🗑️ Histórico de notificações limpo");
    }
    
    /**
     * Verifica se está fazendo polling
     */
    public boolean isPolling() {
        return isPolling;
    }
}
