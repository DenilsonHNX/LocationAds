package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.RegisterRequest;
import ao.co.isptec.aplm.locationads.network.models.VerifyEmailRequest;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    // API Service
    private ApiService apiService;

    // Views - EditTexts
    private TextInputEditText nameInput;
    private TextInputEditText phoneInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;

    // Views - InputLayouts
    private TextInputLayout nameInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    // Views - Buttons
    private MaterialButton btnCreateAccount;
    private ImageButton backButton;

    // Estado
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initApiService();
        initViews();
        setupListeners();
    }

    private void initApiService() {
        apiService = ApiClient.getInstance().getApiService();
    }

    private void initViews() {
        // EditTexts
        nameInput = findViewById(R.id.name_input);
        phoneInput = findViewById(R.id.number_input);
        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.firstPassword_input);
        confirmPasswordInput = findViewById(R.id.confirmFirstPassword_input);

        // InputLayouts
        nameInputLayout = findViewById(R.id.nameInputLayout);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);

        // Buttons
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        // Botão para criar conta
        btnCreateAccount.setOnClickListener(v -> handleCreateAccount());

        // Botão voltar
        backButton.setOnClickListener(v -> finish());

        // Link para ir ao login
        findViewById(R.id.toLogin_btn).setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void handleCreateAccount() {
        if (isLoading) return;

        clearErrors();

        // Obter dados
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validar todos os campos
        if (!validateAllFields(name, phone, email, password, confirmPassword)) {
            return;
        }

        // Criar conta
        createAccount(name, email, password);
    }

    private boolean validateAllFields(String name, String phone, String email,
                                      String password, String confirmPassword) {
        boolean isValid = true;

        // Validar nome
        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError(getString(R.string.error_empty_name));
            if (isValid) nameInput.requestFocus();
            isValid = false;
        } else if (name.length() < 3) {
            nameInputLayout.setError("Nome deve ter pelo menos 3 caracteres");
            if (isValid) nameInput.requestFocus();
            isValid = false;
        }

        // Validar telefone
        if (TextUtils.isEmpty(phone)) {
            phoneInputLayout.setError(getString(R.string.error_empty_phone));
            if (isValid) phoneInput.requestFocus();
            isValid = false;
        } else if (!isValidPhone(phone)) {
            phoneInputLayout.setError(getString(R.string.error_invalid_phone));
            if (isValid) phoneInput.requestFocus();
            isValid = false;
        }

        // Validar email
        if (TextUtils.isEmpty(email)) {
            emailInputLayout.setError(getString(R.string.error_empty_email));
            if (isValid) emailInput.requestFocus();
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailInputLayout.setError(getString(R.string.error_invalid_email));
            if (isValid) emailInput.requestFocus();
            isValid = false;
        }

        // Validar senha
        if (TextUtils.isEmpty(password)) {
            passwordInputLayout.setError(getString(R.string.error_empty_password));
            if (isValid) passwordInput.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            passwordInputLayout.setError(getString(R.string.error_password_too_short));
            if (isValid) passwordInput.requestFocus();
            isValid = false;
        }

        // Validar confirmação de senha
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordInputLayout.setError("Por favor, confirme sua senha");
            if (isValid) confirmPasswordInput.requestFocus();
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError(getString(R.string.error_passwords_dont_match));
            if (isValid) confirmPasswordInput.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void createAccount(String name, String email, String password) {
        setLoadingState(true);

        RegisterRequest registerRequest = new RegisterRequest(name, email, password);

        Log.d(TAG, "Criando conta para: " + email);

        apiService.register(registerRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setLoadingState(false);

                if (response.isSuccessful()) {
                    handleRegistrationSuccess(email);
                } else {
                    handleRegistrationError(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setLoadingState(false);
                handleNetworkError(t);
            }
        });
    }

    private void handleRegistrationSuccess(String email) {
        Log.d(TAG, "Usuário registrado com sucesso");
        Toast.makeText(this, getString(R.string.register_success),
                Toast.LENGTH_SHORT).show();

        // Navegar para o login com o email preenchido
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("registered_email", email);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void handleRegistrationError(int statusCode, String message) {
        String errorMessage;

        switch (statusCode) {
            case 400:
                errorMessage = "Dados inválidos. Verifique os campos.";
                break;
            case 409:
                errorMessage = getString(R.string.error_email_already_exists);
                break;
            case 500:
                errorMessage = "Erro no servidor. Tente novamente mais tarde.";
                break;
            default:
                errorMessage = getString(R.string.error_register_failed);
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Erro no registro. Status: " + statusCode + ", Message: " + message);
    }

    private void handleNetworkError(Throwable t) {
        String errorMessage;

        if (t instanceof java.net.UnknownHostException) {
            errorMessage = "Sem conexão com a internet";
        } else if (t instanceof java.net.SocketTimeoutException) {
            errorMessage = "Tempo de conexão esgotado";
        } else {
            errorMessage = "Erro de conexão: " + t.getMessage();
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Erro de rede", t);
    }

    private void clearErrors() {
        nameInputLayout.setError(null);
        phoneInputLayout.setError(null);
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        // Validação para números angolanos (9 dígitos começando com 9)
        return phone.matches("^9[0-9]{8}$");
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;

        btnCreateAccount.setEnabled(!loading);
        nameInput.setEnabled(!loading);
        phoneInput.setEnabled(!loading);
        emailInput.setEnabled(!loading);
        passwordInput.setEnabled(!loading);
        confirmPasswordInput.setEnabled(!loading);

        if (loading) {
            btnCreateAccount.setText("Criando conta...");
        } else {
            btnCreateAccount.setText(getString(R.string.create_account));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpar referências
        apiService = null;
    }
}