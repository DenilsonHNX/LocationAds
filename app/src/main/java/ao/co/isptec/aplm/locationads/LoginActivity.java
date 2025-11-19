package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Base64;
import org.json.JSONObject;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.LoginRequest;
import ao.co.isptec.aplm.locationads.network.models.LoginResponse;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    ApiService apiService = ApiClient.getInstance().getApiService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        EditText userInput = findViewById(R.id.user_input);
        EditText passwordInput = findViewById(R.id.password_input);
        Button loginBtn = findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(v -> {
            String emailDigitado = userInput.getText().toString().trim();
            String senhaDigitada = passwordInput.getText().toString().trim();

            if (emailDigitado.isEmpty() || senhaDigitada.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest loginRequest = new LoginRequest(emailDigitado, senhaDigitada);

            apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                    if (response.isSuccessful() && response.body() != null) {

                        String token = response.body().getToken();
                        long expiry = getTokenExpiry(token); // Função acima
                        String mensagem = response.body().getMessage();
                        Log.d("LOGIN", "Token recebido: " + token);        // Mostra o token que veio da API
                        Log.d("LOGIN", "Mensagem recebida: " + mensagem);  // Mostra qualquer mensagem recebida

                        // Salve o token usando TokenManager (centralizado)
                        ao.co.isptec.aplm.locationads.network.singleton.TokenManager.saveToken(token, expiry);
                        // também pode salvar email se quiser manter
                        SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("email", emailDigitado);
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();






                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<LoginResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Erro na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    public static long getTokenExpiry(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length == 3) {
                byte[] payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE);
                String payload = new String(payloadBytes, "UTF-8");
                JSONObject jsonObj = new JSONObject(payload);
                long exp = jsonObj.optLong("exp", 0);   // exp é em segundos
                return exp * 1000;                      // converte para milissegundos
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}