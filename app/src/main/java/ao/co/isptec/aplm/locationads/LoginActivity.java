package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.LoginRequest;
import ao.co.isptec.aplm.locationads.network.models.LoginResponse;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import ao.co.isptec.aplm.locationads.network.singleton.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // Views
    private EditText userInput;
    private EditText passwordInput;
    private Button loginBtn;
    private TextView toRegister;

    // Services
    private ApiService apiService;
    private TokenManager tokenManager;

    // Estado
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Verificar se já está logado
        checkIfAlreadyLoggedIn();

        // Inicializar serviços
        initServices();

        // Inicializar views
        initViews();

        // Configurar listeners
        setupListeners();
    }

    /**
     * Verificar se o usuário já está logado
     */
    private void checkIfAlreadyLoggedIn() {
        tokenManager = TokenManager.getInstance(this);

        if (tokenManager.isLoggedIn()) {
            Log.d(TAG, "Usuário já está logado, redirecionando para MainActivity");
            navigateToMainActivity();
        }
    }

    /**
     * Inicializar serviços
     */
    private void initServices() {
        apiService = ApiClient.getInstance().getApiService();
        tokenManager = TokenManager.getInstance(this);
    }

    /**
     * Inicializar views
     */
    private void initViews() {
        userInput = findViewById(R.id.user_input);
        passwordInput = findViewById(R.id.password_input);
        loginBtn = findViewById(R.id.loginBtn);
        toRegister = findViewById(R.id.toRegister_btn);
    }

    /**
     * Configurar listeners
     */
    private void setupListeners() {
        // Botão de login
        loginBtn.setOnClickListener(v -> handleLogin());

        // Link para registro
        toRegister.setOnClickListener(v -> navigateToRegister());
    }

    /**
     * Navegar para tela de registro
     */
    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Navegar para MainActivity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Processar login
     */
    private void handleLogin() {
        if (isLoading) {
            return;
        }

        // Obter dados dos campos
        String email = userInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validar campos
        if (!validateInputs(email, password)) {
            return;
        }

        // Executar login
        performLogin(email, password);
    }

    /**
     * Validar inputs
     */
    private boolean validateInputs(String email, String password) {
        // Validar email
        if (email.isEmpty()) {
            userInput.setError("Digite seu email");
            userInput.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            userInput.setError("Email inválido");
            userInput.requestFocus();
            return false;
        }

        // Validar senha
        if (password.isEmpty()) {
            passwordInput.setError("Digite sua senha");
            passwordInput.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordInput.setError("Senha deve ter no mínimo 6 caracteres");
            passwordInput.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Executar login na API
     */
    private void performLogin(String email, String password) {
        setLoadingState(true);

        Log.d(TAG, "Iniciando login para: " + email);

        LoginRequest loginRequest = new LoginRequest(email, password);

        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body(), email);
                } else {
                    handleLoginError(response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoadingState(false);
                handleLoginFailure(t);
            }
        });
    }

    /**
     * Processar sucesso do login
     */
    private void handleLoginSuccess(LoginResponse loginResponse, String email) {
        try {
            // Extrair dados da resposta
            String token = loginResponse.getToken();
            int userId = loginResponse.getUser().getId();
            String username = loginResponse.getUser().getNome(); // Ajuste conforme seu modelo

            Log.d(TAG, "✅ Login bem-sucedido");
            Log.d(TAG, "Token recebido: " + token);
            Log.d(TAG, "User ID: " + userId);
            Log.d(TAG, "Username: " + username);

            // Calcular tempo de expiração do token
            long expiryTime = getTokenExpiry(token);

            if (expiryTime > 0) {
                Log.d(TAG, "Token expira em: " + expiryTime);
            } else {
                // Se não conseguir extrair expiração, usar padrão de 24 horas
                expiryTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000L);
                Log.d(TAG, "Usando expiração padrão (24h)");
            }

            // Salvar dados do usuário usando TokenManager
            tokenManager.saveUserData(
                    String.valueOf(userId),
                    username,
                    token,
                    expiryTime
            );

            // Também salvar no SharedPreferences antigo (para compatibilidade)
            saveToSharedPreferences(email, token, userId);

            // Mostrar mensagem de sucesso
            String message = loginResponse.getMessage();
            if (message != null && !message.isEmpty()) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
            }

            // Navegar para MainActivity
            navigateToMainActivity();

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar resposta do login", e);
            Toast.makeText(LoginActivity.this,
                    "Erro ao processar login: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Salvar dados no SharedPreferences (compatibilidade com código antigo)
     */
    private void saveToSharedPreferences(String email, String token, int userId) {
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("email", email);
        editor.putString("token", token);
        editor.putInt("userId", userId);
        editor.apply();

        Log.d(TAG, "Dados salvos no SharedPreferences");
    }

    /**
     * Processar erro do login
     */
    private void handleLoginError(int statusCode) {
        String errorMessage;

        switch (statusCode) {
            case 400:
                errorMessage = "Dados inválidos. Verifique os campos.";
                break;
            case 401:
                errorMessage = "Email ou senha incorretos";
                break;
            case 404:
                errorMessage = "Usuário não encontrado";
                break;
            case 500:
                errorMessage = "Erro no servidor. Tente novamente mais tarde.";
                break;
            default:
                errorMessage = "Erro ao fazer login. Código: " + statusCode;
        }

        Log.e(TAG, "❌ Erro no login: " + errorMessage);
        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Processar falha na requisição
     */
    private void handleLoginFailure(Throwable t) {
        String errorMessage;

        if (t instanceof java.net.UnknownHostException) {
            errorMessage = "Sem conexão com a internet";
        } else if (t instanceof java.net.SocketTimeoutException) {
            errorMessage = "Tempo de conexão esgotado";
        } else {
            errorMessage = "Erro de conexão: " + t.getMessage();
        }

        Log.e(TAG, "❌ Falha na requisição de login", t);
        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * Extrair tempo de expiração do token JWT
     */
    private long getTokenExpiry(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length == 3) {
                // Decodificar payload do JWT
                byte[] payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE);
                String payload = new String(payloadBytes, "UTF-8");

                // Parsear JSON
                JSONObject jsonObj = new JSONObject(payload);

                // Extrair campo 'exp' (em segundos)
                long exp = jsonObj.optLong("exp", 0);

                if (exp > 0) {
                    // Converter para milissegundos
                    return exp * 1000;
                } else {
                    Log.w(TAG, "Token JWT não contém campo 'exp'");
                }
            } else {
                Log.w(TAG, "Token JWT inválido (não tem 3 partes)");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao extrair expiração do token", e);
        }

        // Retornar 0 se falhar (indicando que deve usar expiração padrão)
        return 0;
    }

    /**
     * Definir estado de carregamento
     */
    private void setLoadingState(boolean loading) {
        isLoading = loading;

        // Desabilitar inputs durante carregamento
        userInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
        loginBtn.setEnabled(!loading);
        toRegister.setEnabled(!loading);

        // Alterar texto do botão
        if (loading) {
            loginBtn.setText("Entrando...");
        } else {
            loginBtn.setText("Entrar");
        }
    }
}