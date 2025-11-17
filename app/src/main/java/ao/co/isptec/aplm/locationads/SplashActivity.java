package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Base64;
import org.json.JSONObject;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Animação da logo, opcional
        ImageView logo = findViewById(R.id.logoSplash);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeIn);

        SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String token = sharedPref.getString("token", null);
        long expiry = sharedPref.getLong("expiry", 0);
        long now = System.currentTimeMillis();

        Intent intent;
        if (token != null && !token.isEmpty() && now < expiry) {
            // Sessão válida: vai para Main
            intent = new Intent(this, MainActivity.class);
        } else {
            // Sessão expirada ou sem token: vai para Login
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

}


