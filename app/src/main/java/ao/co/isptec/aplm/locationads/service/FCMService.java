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

import java.util.Map;

import ao.co.isptec.aplm.locationads.MainActivity;
import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.ViewAds;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import ao.co.isptec.aplm.locationads.network.singleton.TokenManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Serviço Firebase Cloud Messaging para receber notificações push
 * 
 * Funciona em conjunto com o backend que envia notificações
 * quando há novos anúncios na localização do utilizador
 */
public class FCMService extends FirebaseMessagingService {
    
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "AdsNotificationChannel";
    private static final String CHANNEL_NAME = "Anúncios";
    
    private int notificationId = 1000;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    
    /**
     * Chamado quando um novo token FCM é gerado
     * Envia o token para o backend para associar ao utilizador
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "🔑 Novo FCM Token: " + token);
        
        // Enviar token para o servidor
        sendTokenToServer(token);
    }
    
    /**
     * Chamado quando uma mensagem é recebida
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "📬 Mensagem recebida de: " + remoteMessage.getFrom());
        
        // Verificar se tem dados
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "📦 Dados: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }
        
        // Verificar se tem notificação (quando app está em foreground)
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "🔔 Notificação: " + remoteMessage.getNotification().getBody());
            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    remoteMessage.getData()
            );
        }
    }
    
    /**
     * Processa mensagem de dados (custom payload do backend)
     */
    private void handleDataMessage(Map<String, String> data) {
        String tipo = data.get("tipo");
        String titulo = data.get("titulo");
        String conteudo = data.get("conteudo");
        String mensagemId = data.get("mensagemId");
        
        if (tipo != null && tipo.equals("novo_anuncio")) {
            showNotification(titulo, conteudo, data);
        }
    }
    
    /**
     * Mostra notificação do sistema
     */
    private void showNotification(String title, String body, Map<String, String> data) {
        // Intent para abrir quando clicar
        Intent intent;
        
        if (data != null && data.containsKey("mensagemId")) {
            // Abrir anúncio específico
            intent = new Intent(this, ViewAds.class);
            try {
                intent.putExtra("ad_id", Integer.parseInt(data.get("mensagemId")));
            } catch (NumberFormatException e) {
                intent = new Intent(this, MainActivity.class);
            }
        } else {
            // Abrir MainActivity
            intent = new Intent(this, MainActivity.class);
        }
        
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 
                notificationId, 
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Construir notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_ads)
                .setContentTitle(title != null ? title : "Novo anúncio")
                .setContentText(body != null ? body : "Há um novo anúncio na sua área")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);
        
        // Mostrar notificação
        NotificationManager notificationManager = 
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(notificationId++, builder.build());
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
            channel.setDescription("Notificações de anúncios na sua localização");
            channel.enableLights(true);
            channel.enableVibration(true);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Envia o token FCM para o servidor
     */
    private void sendTokenToServer(String fcmToken) {
        try {
            String authToken = TokenManager.getInstance(this).getToken();
            
            if (authToken == null || authToken.isEmpty()) {
                Log.w(TAG, "⚠️ Utilizador não autenticado, token FCM será enviado após login");
                // Guardar token para enviar depois do login
                getSharedPreferences("fcm_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("pending_fcm_token", fcmToken)
                        .apply();
                return;
            }
            
            ApiService apiService = ApiClient.getInstance(this).getApiService();
            
            java.util.HashMap<String, String> body = new java.util.HashMap<>();
            body.put("fcmToken", fcmToken);
            
            apiService.updateFcmToken(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ FCM Token enviado com sucesso");
                        // Limpar token pendente
                        getSharedPreferences("fcm_prefs", MODE_PRIVATE)
                                .edit()
                                .remove("pending_fcm_token")
                                .apply();
                    } else {
                        Log.e(TAG, "❌ Erro ao enviar FCM Token: " + response.code());
                    }
                }
                
                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e(TAG, "❌ Falha ao enviar FCM Token", t);
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Erro ao enviar token", e);
        }
    }
    
    /**
     * Método estático para enviar token pendente após login
     * Chamar após o utilizador fazer login com sucesso
     */
    public static void sendPendingToken(Context context) {
        String pendingToken = context.getSharedPreferences("fcm_prefs", MODE_PRIVATE)
                .getString("pending_fcm_token", null);
        
        if (pendingToken != null) {
            Log.d(TAG, "📤 Enviando FCM token pendente...");
            new FCMService().sendTokenToServer(pendingToken);
        }
    }
}
