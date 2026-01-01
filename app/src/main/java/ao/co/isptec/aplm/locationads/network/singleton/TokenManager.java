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
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
        return instance;
    }

    private TokenManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ✅ CORRIGIDO - Salvar dados do usuário COM EMAIL
    public void saveUserData(String userId, String username, String email, String token) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)  // ← ADICIONADO!
                .putString(KEY_TOKEN, token)
                .apply();

        // Definir tempo de expiração padrão (24 horas)
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
        prefs.edit().putLong(KEY_TOKEN_EXPIRY, expiryTime).apply();

        Log.d(TAG, "✅ Dados do usuário salvos:");
        Log.d(TAG, "  - User ID: " + userId);
        Log.d(TAG, "  - Username: " + username);
        Log.d(TAG, "  - Email: " + email);  // ← ADICIONADO!
    }

    // ✅ CORRIGIDO - Salvar dados com expiração personalizada
    public void saveUserData(String userId, String username, String email, String token, long expiryTimeMillis) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)  // ← ADICIONADO!
                .putString(KEY_TOKEN, token)
                .putLong(KEY_TOKEN_EXPIRY, expiryTimeMillis)
                .apply();

        Log.d(TAG, "✅ Dados do usuário salvos com expiração personalizada");
        Log.d(TAG, "  - Email: " + email);  // ← ADICIONADO!
    }

    // ⚠️ DEPRECATED - Manter para compatibilidade (sem email)
    @Deprecated
    public void saveUserData(String userId, String username, String token) {
        saveUserData(userId, username, "", token);
        Log.w(TAG, "⚠️ Usando saveUserData sem email - use a versão com email!");
    }

    @Deprecated
    public void saveUserData(String userId, String username, String token, long expiryTimeMillis) {
        saveUserData(userId, username, "", token, expiryTimeMillis);
        Log.w(TAG, "⚠️ Usando saveUserData sem email - use a versão com email!");
    }

    // Salvar token
    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
        long expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
        prefs.edit().putLong(KEY_TOKEN_EXPIRY, expiryTime).apply();
        Log.d(TAG, "Token salvo com sucesso");
    }

    public void saveToken(String token, long expiryTimeMillis) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
        prefs.edit().putLong(KEY_TOKEN_EXPIRY, expiryTimeMillis).apply();
        Log.d(TAG, "Token salvo com expiração: " + expiryTimeMillis);
    }

    // Obter token
    public String getToken() {
        return prefs.getString(KEY_TOKEN, "");
    }

    // Obter ID do usuário
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    // Obter username
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    // ✅ Obter email - AGORA FUNCIONA!
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    // Verificar se tem token
    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    // Verificar se o token é válido (não expirou)
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

    public static boolean isTokenValid(Context context) {
        return getInstance(context).isTokenValid();
    }

    // Verificar se está logado
    public boolean isLoggedIn() {
        return hasToken() && isTokenValid();
    }

    // Obter tempo restante do token (em milissegundos)
    public long getTokenRemainingTime() {
        if (!hasToken()) {
            return 0;
        }

        long expiryTime = prefs.getLong(KEY_TOKEN_EXPIRY, 0);
        long currentTime = System.currentTimeMillis();
        long remaining = expiryTime - currentTime;

        return Math.max(0, remaining);
    }

    // Renovar token (atualizar tempo de expiração)
    public void renewToken() {
        if (hasToken()) {
            long newExpiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
            prefs.edit().putLong(KEY_TOKEN_EXPIRY, newExpiryTime).apply();
            Log.d(TAG, "Token renovado");
        }
    }

    // Salvar refresh token
    public void saveRefreshToken(String refreshToken) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply();
    }

    // Obter refresh token
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, "");
    }

    // Limpar token (logout)
    public void clearToken() {
        prefs.edit().clear().apply();
        Log.d(TAG, "Token limpo");
    }

    // Limpar todos os dados do usuário
    public void clearUserData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();

        Log.d(TAG, "🗑️ Todos os dados do usuário foram limpos");
    }

    // Limpar e fazer logout
    public void clearAndLogout() {
        clearUserData();
        Log.d(TAG, "Logout realizado - token e dados limpos");
    }

    public static void clearAndLogout(Context context) {
        getInstance(context).clearAndLogout();
    }
}