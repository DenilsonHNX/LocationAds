package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ao.co.isptec.aplm.locationads.network.models.RecoveryRequest;
import ao.co.isptec.aplm.locationads.network.models.RecoveryResponse;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecoveryActivity extends AppCompatActivity {

    private static final String TAG = "RecoveryActivity";

    // Views
    private TextInputLayout emailInputLayout;
    private TextInputEditText emailInput;
    private MaterialButton recoveryBtn;
    private ImageButton backButton;
    private TextView toLoginBtn;

    // Estado do loading
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);

        // Inicializar views
        initViews();

        // Configurar listeners
        setupListeners();
    }
    private void initViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout);
        emailInput = findViewById(R.id.email_input);
        recoveryBtn = findViewById(R.id.recovery_btn);
        backButton = findViewById(R.id.backButton);
        toLoginBtn = findViewById(R.id.toLogin_btn);
    }

    private void setupListeners() {
        // Botão de voltar
        backButton.setOnClickListener(v -> finish());

        // Link para voltar ao login
        toLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(RecoveryActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Botão de enviar código de recuperação
        recoveryBtn.setOnClickListener(v -> {
            if (!isLoading) {
                sendRecoveryCode();
            }
        });
    }

    private boolean validateEmail() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInputLayout.setError("Por favor, insira seu email");
            emailInput.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Email inválido");
            emailInput.requestFocus();
            return false;
        }

        emailInputLayout.setError(null);
        return true;
    }

    private void sendRecoveryCode() {
        // Validar email
        if (!validateEmail()) {
            return;
        }

        String email = emailInput.getText().toString().trim();

        // Mostrar loading
        setLoading(true);

        // Criar request
        RecoveryRequest request = new RecoveryRequest(email);

        Log.d(TAG, "=== ENVIANDO CÓDIGO DE RECUPERAÇÃO ===");
        Log.d(TAG, "Email: " + email);

        // Fazer a chamada à API
        Call<RecoveryResponse> call = ApiClient.getInstance()
                .getApiService()
                .sendRecoveryCode(request);

        call.enqueue(new Callback<RecoveryResponse>() {
            @Override
            public void onResponse(Call<RecoveryResponse> call, Response<RecoveryResponse> response) {
                setLoading(false);

                Log.d(TAG, "Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    RecoveryResponse recoveryResponse = response.body();

                    Log.d(TAG, "✅ Sucesso!");
                    Log.d(TAG, "Message: " + recoveryResponse.getMessage());

                    // Mostrar mensagem de sucesso
                    Toast.makeText(RecoveryActivity.this,
                            recoveryResponse.getMessage(),
                            Toast.LENGTH_LONG).show();

                    // Opcional: Redirecionar para tela de inserir código
                    // Intent intent = new Intent(RecoveryActivity.this, VerifyCodeActivity.class);
                    // intent.putExtra("email", email);
                    // startActivity(intent);
                    // finish();

                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<RecoveryResponse> call, Throwable t) {
                setLoading(false);

                Log.e(TAG, "Erro na requisição: " + t.getMessage(), t);

                Toast.makeText(RecoveryActivity.this,
                        "Erro de conexão. Verifique sua internet.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Trata erros da resposta da API
     */
    private void handleErrorResponse(Response<RecoveryResponse> response) {
        try {
            String errorBody = response.errorBody() != null
                    ? response.errorBody().string()
                    : "Erro desconhecido";

            Log.e(TAG, "Erro " + response.code() + ": " + errorBody);

            String errorMessage;

            switch (response.code()) {
                case 400:
                    errorMessage = "Email inválido ou não cadastrado";
                    emailInputLayout.setError("Email não encontrado");
                    break;
                case 404:
                    errorMessage = "Usuário não encontrado";
                    emailInputLayout.setError("Email não cadastrado");
                    break;
                case 429:
                    errorMessage = "Muitas tentativas. Tente novamente mais tarde";
                    break;
                case 500:
                    errorMessage = "Erro no servidor. Tente novamente";
                    break;
                default:
                    errorMessage = "Erro ao enviar código. Tente novamente";
            }

            Toast.makeText(RecoveryActivity.this, errorMessage, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar resposta de erro", e);
            Toast.makeText(RecoveryActivity.this,
                    "Erro ao processar resposta",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Controla o estado de loading da tela
     */
    private void setLoading(boolean loading) {
        isLoading = loading;

        // Desabilitar/habilitar inputs
        emailInput.setEnabled(!loading);
        recoveryBtn.setEnabled(!loading);
        backButton.setEnabled(!loading);
        toLoginBtn.setEnabled(!loading);

        // Atualizar texto do botão
        if (loading) {
            recoveryBtn.setText("Enviando...");
            recoveryBtn.setIcon(null);
        } else {
            recoveryBtn.setText(R.string.send_recovery_code);
            recoveryBtn.setIconResource(R.drawable.ic_send);
        }
    }

}