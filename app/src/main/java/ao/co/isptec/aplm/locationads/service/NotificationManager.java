package ao.co.isptec.aplm.locationads.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;

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
 * 1. Tenta usar Firebase Cloud Messaging (FCM)
 * 2. Se FCM falhar ou não estiver disponível, usa polling como fallback
 * 
 * O FCM é preferido porque:
 * - Funciona com app fechado
 * - Notificações em tempo real
 * - Menor consumo de bateria
 * 
 * O Polling é usado como fallback quando:
 * - google-services.json não está configurado
 * - Firebase não está inicializado
 * - Erro ao obter token FCM
 */
public class NotificationManager {
    
    private static final String TAG = "NotificationManager";
    
    private static NotificationManager instance;
    private Context context;
    private boolean fcmAvailable = false;
    private boolean pollingActive = false;
    
    private NotificationManager(Context context) {
        this.context = context.getApplicationContext();
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
        
        // Tentar inicializar FCM
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
                                Log.d(TAG, "✅ FCM disponível! Token: " + token.substring(0, 20) + "...");
                                fcmAvailable = true;
                                
                                // Enviar token para o servidor
                                sendFcmTokenToServer(token);
                                
                                // Parar polling se estava ativo
                                stopPollingFallback();
                                
                            } else {
                                Log.w(TAG, "⚠️ FCM indisponível, usando polling como fallback");
                                fcmAvailable = false;
                                startPollingFallback();
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao inicializar FCM: " + e.getMessage());
            Log.w(TAG, "⚠️ Usando polling como fallback");
            fcmAvailable = false;
            startPollingFallback();
        }
    }
    
    /**
     * Inicia o polling como fallback
     */
    private void startPollingFallback() {
        if (!pollingActive) {
            Log.d(TAG, "🔄 Iniciando polling como fallback...");
            NotificationPoller.getInstance(context).startPolling();
            pollingActive = true;
        }
    }
    
    /**
     * Para o polling fallback
     */
    private void stopPollingFallback() {
        if (pollingActive) {
            Log.d(TAG, "⏹️ Parando polling (FCM ativo)");
            NotificationPoller.getInstance(context).stopPolling();
            pollingActive = false;
        }
    }
    
    /**
     * Envia o token FCM para o servidor
     */
    private void sendFcmTokenToServer(String fcmToken) {
        String authToken = TokenManager.getInstance(context).getToken();
        
        if (authToken == null || authToken.isEmpty()) {
            Log.w(TAG, "⚠️ Utilizador não autenticado, token será enviado após login");
            // Guardar para depois
            context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("pending_fcm_token", fcmToken)
                    .apply();
            return;
        }
        
        try {
            ApiService apiService = ApiClient.getInstance(context).getApiService();
            
            HashMap<String, String> body = new HashMap<>();
            body.put("fcmToken", fcmToken);
            
            apiService.updateFcmToken(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ FCM Token registado no servidor");
                    } else {
                        Log.e(TAG, "❌ Erro ao registar FCM Token: " + response.code());
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
     * Envia token FCM pendente após login
     */
    public void sendPendingFcmToken() {
        String pendingToken = context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
                .getString("pending_fcm_token", null);
        
        if (pendingToken != null) {
            Log.d(TAG, "📤 Enviando FCM token pendente...");
            sendFcmTokenToServer(pendingToken);
            
            // Limpar token pendente
            context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .remove("pending_fcm_token")
                    .apply();
        }
    }
    
    /**
     * Para todos os serviços de notificação
     */
    public void stop() {
        stopPollingFallback();
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
     * Força uma verificação imediata de notificações
     */
    public void checkNow() {
        if (pollingActive) {
            NotificationPoller.getInstance(context).checkNow();
        }
        // Se FCM está ativo, as notificações chegam automaticamente
    }
    
    /**
     * Obtém o status atual do sistema de notificações
     */
    public String getStatus() {
        if (fcmAvailable) {
            return "Firebase Cloud Messaging (tempo real)";
        } else if (pollingActive) {
            return "Polling (verificação periódica)";
        } else {
            return "Inativo";
        }
    }
}
