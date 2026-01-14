package ao.co.isptec.aplm.locationads.service;

import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Polling de Notificações como Fallback do FCM
 * 
 * Quando o Firebase Cloud Messaging não está disponível,
 * este serviço verifica periodicamente por novas notificações.
 * 
 * Intervalo: A cada 5 minutos (configurável)
 */
public class NotificationPoller {
    
    private static final String TAG = "NotificationPoller";
    private static final String CHANNEL_ID = "locationads_notifications";
    private static final long POLL_INTERVAL = 5 * 60 * 1000; // 5 minutos
    
    private Context context;
    private Handler handler;
    private boolean isPolling = false;
    private Set<Integer> notifiedIds = new HashSet<>(); // IDs já notificados
    private int notificationId = 1000;
    
    private Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPolling) {
                pollForNotifications();
                handler.postDelayed(this, POLL_INTERVAL);
            }
        }
    };
    
    public NotificationPoller(Context context) {
        this.context = context.getApplicationContext();
        this.handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();
    }
    
    /**
     * Cria canal de notificação (Android 8+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Anúncios LocationAds",
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notificações de novos anúncios");
            
            android.app.NotificationManager manager = 
                    context.getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Inicia o polling periódico
     */
    public void startPolling() {
        if (!isPolling) {
            Log.d(TAG, "▶️ Iniciando polling de notificações");
            isPolling = true;
            handler.post(pollRunnable);
        }
    }
    
    /**
     * Para o polling
     */
    public void stopPolling() {
        Log.d(TAG, "⏹️ Parando polling de notificações");
        isPolling = false;
        handler.removeCallbacks(pollRunnable);
    }
    
    /**
     * Verifica por novas notificações na API
     */
    private void pollForNotifications() {
        Log.d(TAG, "🔍 Verificando novas notificações...");
        
        ApiService apiService = ApiClient.getInstance(context).getApiService();
        
        apiService.getNotifications().enqueue(new Callback<List<Ads>>() {
            @Override
            public void onResponse(Call<List<Ads>> call, Response<List<Ads>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Ads> notifications = response.body();
                    Log.d(TAG, "📬 " + notifications.size() + " notificações encontradas");
                    
                    for (Ads ad : notifications) {
                        if (ad.getId() != null && !notifiedIds.contains(ad.getId())) {
                            showNotification(ad);
                            notifiedIds.add(ad.getId());
                        }
                    }
                } else {
                    Log.w(TAG, "⚠️ Resposta inválida: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<List<Ads>> call, Throwable t) {
                Log.e(TAG, "❌ Erro ao buscar notificações: " + t.getMessage());
            }
        });
    }
    
    /**
     * Exibe notificação para um anúncio
     */
    private void showNotification(Ads ad) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_ads)
                    .setContentTitle(ad.getTitulo() != null ? ad.getTitulo() : "Novo Anúncio")
                    .setContentText(ad.getConteudo() != null ? 
                            ad.getConteudo().substring(0, Math.min(100, ad.getConteudo().length())) : 
                            "Clique para ver mais")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(notificationId++, builder.build());
            
            Log.d(TAG, "🔔 Notificação exibida: " + ad.getTitulo());
            
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Sem permissão para notificações: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao exibir notificação: " + e.getMessage());
        }
    }
    
    /**
     * Limpa cache de IDs notificados
     */
    public void clearNotifiedCache() {
        notifiedIds.clear();
    }
}
