package ao.co.isptec.aplm.locationads.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import ao.co.isptec.aplm.locationads.network.singleton.TokenManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Gestor de Notificações com FCM + Fallback para Polling
 * 
 * Estratégia:
 * 1. Tenta usar Firebase Cloud Messaging (FCM) - notificações em tempo real
 * 2. Se FCM falhar, usa polling como fallback - verifica a cada 5 minutos
 */
public class NotificationManager {
    
    private static final String TAG = "NotificationManager";
    
    private static NotificationManager instance;
    private Context context;
    private boolean fcmAvailable = false;
    private boolean pollingActive = false;
    private NotificationPoller poller;
    
    private NotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.poller = new NotificationPoller(this.context);
    }
    
    public static synchronized NotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManager(context);
        }
        return instance;
    }
    
    /**
     * Inicializa o sistema de notificações
     * Tenta FCM primeiro, com fallback para polling
     */
    public void initialize() {
        Log.d(TAG, "🚀 Inicializando sistema de notificações...");
        initializeFCM();
    }
    
    /**
     * Tenta inicializar Firebase Cloud Messaging
     */
    private void initializeFCM() {
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                String token = task.getResult();
                                Log.d(TAG, "✅ FCM disponível! Token obtido");
                                fcmAvailable = true;
                                
                                // Enviar token para o servidor
                                sendFcmTokenToServer(token);
                                
                                // Parar polling se estava ativo
                                stopPolling();
                                
                            } else {
                                Log.w(TAG, "⚠️ FCM indisponível, usando polling como fallback");
                                fcmAvailable = false;
                                startPolling();
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao inicializar FCM: " + e.getMessage());
            Log.w(TAG, "⚠️ Usando polling como fallback");
            fcmAvailable = false;
            startPolling();
        }
    }
    
    /**
     * Envia o token FCM para o backend
     */
    private void sendFcmTokenToServer(String fcmToken) {
        String authToken = TokenManager.getInstance(context).getToken();
        
        if (authToken == null || authToken.isEmpty()) {
            Log.w(TAG, "⚠️ Usuário não autenticado, token FCM será enviado após login");
            return;
        }
        
        Map<String, String> body = new HashMap<>();
        body.put("fcmToken", fcmToken);
        
        ApiService apiService = ApiClient.getInstance(context).getApiService();
        apiService.saveFcmToken("Bearer " + authToken, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Token FCM registrado no servidor");
                } else {
                    Log.e(TAG, "❌ Erro ao registrar token FCM: " + response.code());
                }
            }
            
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao enviar token FCM: " + t.getMessage());
            }
        });
    }
    
    /**
     * Inicia polling como fallback
     */
    private void startPolling() {
        if (!pollingActive) {
            Log.d(TAG, "🔄 Iniciando polling de notificações (fallback)");
            pollingActive = true;
            poller.startPolling();
        }
    }
    
    /**
     * Para o polling
     */
    public void stopPolling() {
        if (pollingActive) {
            Log.d(TAG, "⏹️ Parando polling (FCM ativo)");
            pollingActive = false;
            poller.stopPolling();
        }
    }
    
    /**
     * Verifica se FCM está disponível
     */
    public boolean isFcmAvailable() {
        return fcmAvailable;
    }
    
    /**
     * Verifica se polling está ativo
     */
    public boolean isPollingActive() {
        return pollingActive;
    }
    
    /**
     * Para todos os sistemas de notificação
     */
    public void shutdown() {
        stopPolling();
    }
}
