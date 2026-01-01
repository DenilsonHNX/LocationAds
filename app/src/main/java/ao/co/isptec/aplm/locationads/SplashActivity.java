package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import ao.co.isptec.aplm.locationads.network.singleton.TokenManager;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Log.d(TAG, "========== SPLASH INICIADO ==========");

        // Inicializar ApiClient
        ApiClient.getInstance(this);

        // Animação da logo
        ImageView logo = findViewById(R.id.logoSplash);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeIn);

        // Aguardar animação e verificar sessão
        new Handler().postDelayed(() -> {
            checkSessionAndNavigate();
        }, SPLASH_DELAY);
    }

    private void checkSessionAndNavigate() {
        Log.d(TAG, "Verificando sessão...");

        // ✅ DEBUG: Verificar TODOS os SharedPreferences
        debugAllPreferences();

        // Usar TokenManager
        TokenManager tokenManager = TokenManager.getInstance(this);
        boolean isLoggedIn = tokenManager.isLoggedIn();

        Log.d(TAG, "TokenManager.isLoggedIn() = " + isLoggedIn);

        // Se TokenManager diz que está logado, verificar os dados
        if (isLoggedIn) {
            Log.d(TAG, "Token: " + tokenManager.getToken());
            Log.d(TAG, "User ID: " + tokenManager.getUserId());
            Log.d(TAG, "Username: " + tokenManager.getUsername());
            Log.d(TAG, "Now: " + System.currentTimeMillis());
        }

        Intent intent;

        if (isLoggedIn) {
            Log.d(TAG, "✅ Sessão válida → MainActivity");
            intent = new Intent(this, MainActivity.class);
        } else {
            Log.d(TAG, "❌ Sem sessão → LoginActivity");
            intent = new Intent(this, LoginActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Log.d(TAG, "========== SPLASH FINALIZADO ==========");
    }

    /**
     * Debug: Verificar TODOS os SharedPreferences
     */
    private void debugAllPreferences() {
        Log.d(TAG, "===== DEBUG SharedPreferences =====");

        // Verificar user_prefs
        SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        Log.d(TAG, "user_prefs:");
        Log.d(TAG, "  - token: " + userPrefs.getString("token", "NULL"));
        Log.d(TAG, "  - expiry: " + userPrefs.getLong("expiry", 0));
        Log.d(TAG, "  - isLoggedIn: " + userPrefs.getBoolean("isLoggedIn", false));
        Log.d(TAG, "  - email: " + userPrefs.getString("email", "NULL"));

        // Verificar token_prefs (usado pelo TokenManager)
        SharedPreferences tokenPrefs = getSharedPreferences("token_prefs", MODE_PRIVATE);
        Log.d(TAG, "token_prefs:");
        Log.d(TAG, "  - auth_token: " + tokenPrefs.getString("auth_token", "NULL"));
        Log.d(TAG, "  - token_expiry: " + tokenPrefs.getLong("token_expiry", 0));
        Log.d(TAG, "  - is_logged_in: " + tokenPrefs.getBoolean("is_logged_in", false));
        Log.d(TAG, "  - user_id: " + tokenPrefs.getString("user_id", "NULL"));
        Log.d(TAG, "  - username: " + tokenPrefs.getString("username", "NULL"));

        Log.d(TAG, "===================================");
    }
}