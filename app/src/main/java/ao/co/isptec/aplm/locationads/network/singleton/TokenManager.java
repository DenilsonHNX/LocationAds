package ao.co.isptec.aplm.locationads.network.singleton;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class TokenManager {

    private static final String TAG = "TokenManager";
    private static TokenManager instance;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry"; // ✅ NOVO

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
        return instance;
    }

    private TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Salvar token
    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
        // Definir tempo de expiração (exemplo: 24 horas a partir de agora)
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 horas
        prefs.edit().putLong(KEY_TOKEN_EXPIRY, expiryTime).apply();
        Log.d(TAG, "Token salvo com sucesso");
    }

    // Salvar token com tempo de expiração personalizado
    public void saveToken(String token, long expiryTimeMillis) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
        prefs.edit().putLong(KEY_TOKEN_EXPIRY, expiryTimeMillis).apply();
        Log.d(TAG, "Token salvo com expiração: " + expiryTimeMillis);
    }

    // Obter token
    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    // Verificar se tem token
    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    // ✅ MÉTODO NOVO - Verificar se o token é válido (não expirou)
    public boolean isTokenValid() {
        if (!hasToken()) {
            Log.d(TAG, "Token não existe");
            return false;
        }

        long expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0);
        long currentTime = System.currentTimeMillis();

        boolean isValid = currentTime < expiryTime;

        if (!isValid) {
            Log.d(TAG, "Token expirado");
        }

        return isValid;
    }

    // ✅ MÉTODO NOVO (STATIC) - Verificar se o token é válido estaticamente
    public static boolean isTokenValid(Context context) {
        return getInstance(context).isTokenValid();
    }

    // Limpar token (logout)
    public void clearToken() {
        prefs.edit().clear().apply();
        Log.d(TAG, "Token limpo");
    }

    // ✅ MÉTODO NOVO - Limpar token e fazer logout
    public void clearAndLogout() {
        clearToken();
        Log.d(TAG, "Logout realizado - token e dados limpos");
    }

    // ✅ MÉTODO NOVO (STATIC) - Limpar e fazer logout estaticamente
    public static void clearAndLogout(Context context) {
        getInstance(context).clearAndLogout();
    }

    // Salvar dados do utilizador
    public void saveUserData(String userId, String username, String token) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_TOKEN, token)
                .apply();

        // Definir tempo de expiração padrão
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 horas
        prefs.edit().putLong(KEY_TOKEN_EXPIRY, expiryTime).apply();

        Log.d(TAG, "Dados do usuário salvos");
    }

    // Salvar dados do utilizador com tempo de expiração personalizado
    public void saveUserData(String userId, String username, String token, long expiryTimeMillis) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_TOKEN, token)
                .putLong(KEY_TOKEN_EXPIRY, expiryTimeMillis)
                .apply();

        Log.d(TAG, "Dados do usuário salvos com expiração personalizada");
    }

    // Obter ID do utilizador
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    // Obter username
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    // Salvar refresh token
    public void saveRefreshToken(String refreshToken) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply();
    }

    // Obter refresh token
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, "");
    }

    // ✅ MÉTODO NOVO - Verificar se está logado
    public boolean isLoggedIn() {
        return hasToken() && isTokenValid();
    }

    // ✅ MÉTODO NOVO - Obter tempo restante do token (em milissegundos)
    public long getTokenRemainingTime() {
        if (!hasToken()) {
            return 0;
        }

        long expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0);
        long currentTime = System.currentTimeMillis();
        long remaining = expiryTime - currentTime;

        return Math.max(0, remaining); // Nunca retornar negativo
    }



    // ✅ MÉTODO NOVO - Renovar token (atualizar tempo de expiração)
    public void renewToken() {
        if (hasToken()) {
            long newExpiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // +24 horas
            prefs.edit().putLong(KEY_TOKEN_EXPIRY, newExpiryTime).apply();
            Log.d(TAG, "Token renovado");
        }
    }

    public void clearUserData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear(); // Limpa TUDO
        editor.commit(); // Grava imediatamente

        Log.d(TAG, "🗑️ Todos os dados do usuário foram limpos");
    }


}