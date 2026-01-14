package ao.co.isptec.aplm.locationads.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import ao.co.isptec.aplm.locationads.MainActivity;
import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import ao.co.isptec.aplm.locationads.network.singleton.TokenManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Firebase Cloud Messaging Service
 * 
 * Recebe notificações push do servidor Firebase.
 * Este serviço funciona mesmo com o app fechado.
 */
public class FCMService extends FirebaseMessagingService {
    
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "locationads_push";
    private static final String CHANNEL_NAME = "Notificações Push";
    
    private static int notificationId = 2000;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Chamado quando um novo token FCM é gerado
     * Isso acontece na primeira instalação ou quando o token é renovado
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "🔑 Novo token FCM: " + token.substring(0, Math.min(20, token.length())) + "...");
        
        // Enviar token para o backend
        sendTokenToServer(token);
    }

    /**
     * Chamado quando uma mensagem é recebida
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "📬 Mensagem recebida de: " + remoteMessage.getFrom());
        
        // Verificar se há dados
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "📦 Dados: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }
        
        // Verificar se há notificação
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "📢 Notificação: " + remoteMessage.getNotification().getBody());
            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            );
        }
    }

    /**
     * Processa mensagem de dados (payload customizado do backend)
     */
    private void handleDataMessage(Map<String, String> data) {
        String title = data.get("title");
        String body = data.get("body");
        String adId = data.get("adId");
        String type = data.get("type");
        
        Log.d(TAG, "📝 Tipo: " + type + ", AdId: " + adId);
        
        // Mostrar notificação
        if (title != null || body != null) {
            showNotification(
                    title != null ? title : "LocationAds",
                    body != null ? body : "Você tem uma nova notificação"
            );
        }
    }

    /**
     * Exibe uma notificação no sistema
     */
    private void showNotification(String title, String body) {
        // Intent para abrir o app ao clicar
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 
                0, 
                intent, 
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Construir notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_ads)
                .setContentTitle(title != null ? title : "LocationAds")
                .setContentText(body != null ? body : "Nova notificação")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        
        // Mostrar notificação
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(notificationId++, builder.build());
            Log.d(TAG, "🔔 Notificação exibida: " + title);
        }
    }

    /**
     * Cria canal de notificação (Android 8+)
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notificações push do LocationAds");
            channel.enableVibration(true);
            channel.enableLights(true);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Envia o token FCM para o backend
     */
    private void sendTokenToServer(String fcmToken) {
        String authToken = TokenManager.getInstance(this).getToken();
        
        if (authToken == null || authToken.isEmpty()) {
            Log.w(TAG, "⚠️ Usuário não autenticado, salvando token localmente");
            // Salvar token localmente para enviar depois do login
            getSharedPreferences("fcm_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("pending_fcm_token", fcmToken)
                    .apply();
            return;
        }
        
        Map<String, String> body = new HashMap<>();
        body.put("fcmToken", fcmToken);
        
        ApiService apiService = ApiClient.getInstance(this).getApiService();
        apiService.saveFcmToken("Bearer " + authToken, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Token FCM enviado ao servidor");
                    // Limpar token pendente
                    getSharedPreferences("fcm_prefs", MODE_PRIVATE)
                            .edit()
                            .remove("pending_fcm_token")
                            .apply();
                } else {
                    Log.e(TAG, "❌ Erro ao enviar token: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao enviar token: " + t.getMessage());
            }
        });
    }

    /**
     * Método estático para enviar token pendente após login
     */
    public static void sendPendingToken(Context context) {
        String pendingToken = context.getSharedPreferences("fcm_prefs", MODE_PRIVATE)
                .getString("pending_fcm_token", null);
        
        if (pendingToken != null) {
            Log.d(TAG, "📤 Enviando token FCM pendente...");
            
            String authToken = TokenManager.getInstance(context).getToken();
            if (authToken != null && !authToken.isEmpty()) {
                Map<String, String> body = new HashMap<>();
                body.put("fcmToken", pendingToken);
                
                ApiService apiService = ApiClient.getInstance(context).getApiService();
                apiService.saveFcmToken("Bearer " + authToken, body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "✅ Token pendente enviado!");
                            context.getSharedPreferences("fcm_prefs", MODE_PRIVATE)
                                    .edit()
                                    .remove("pending_fcm_token")
                                    .apply();
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "❌ Falha: " + t.getMessage());
                    }
                });
            }
        }
    }
}
