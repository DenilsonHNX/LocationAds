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
    private TextInputEditText otpInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;

    // Views - InputLayouts
    private TextInputLayout nameInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout otpInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;

    // Views - Buttons
    private MaterialButton btnSendCode;
    private MaterialButton btnCreateAccount;
    private ImageButton backButton;

    // Estado
    private boolean isLoading = false;
    private boolean otpSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initApiService();
        initViews();
        setupListeners();
        setInitialState();
    }

    private void initApiService() {
        apiService = ApiClient.getInstance().getApiService();
    }

    private void initViews() {
        // EditTexts
        nameInput = findViewById(R.id.name_input);
        phoneInput = findViewById(R.id.number_input);
        emailInput = findViewById(R.id.input_email);
        otpInput = findViewById(R.id.email_otp);
        passwordInput = findViewById(R.id.firstPassword_input);
        confirmPasswordInput = findViewById(R.id.confirmFirstPassword_input);

        // InputLayouts
        nameInputLayout = findViewById(R.id.nameInputLayout);
        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        otpInputLayout = findViewById(R.id.otpInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);

        // Buttons
        btnSendCode = findViewById(R.id.btnSendCode);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        backButton = findViewById(R.id.backButton);
    }

    private void setupListeners() {
        // Botão para enviar código OTP
        btnSendCode.setOnClickListener(v -> handleSendOtp());

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

    private void setInitialState() {
        otpInput.setEnabled(false);
        otpInputLayout.setHelperText("Clique em 'Receber Código' após preencher os dados");
    }

    /**
     * Envia o código OTP para o email do usuário
     */
    private void handleSendOtp() {
        if (isLoading) return;

        clearErrors();

        // Obter dados
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validar campos necessários para enviar OTP
        if (!validateFieldsForOtp(name, phone, email, password, confirmPassword)) {
            return;
        }

        // Enviar requisição para a API
        sendOtpRequest(name, email, password);
    }

    private boolean validateFieldsForOtp(String name, String phone, String email,
                                         String password, String confirmPassword) {
        boolean isValid = true;

        // Validar nome
        if (TextUtils.isEmpty(name)) {
            nameInputLayout.setError("Por favor, insira seu nome completo");
            if (isValid) nameInput.requestFocus();
            isValid = false;
        } else if (name.length() < 3) {
            nameInputLayout.setError("Nome deve ter pelo menos 3 caracteres");
            if (isValid) nameInput.requestFocus();
            isValid = false;
        }

        // Validar telefone
        if (TextUtils.isEmpty(phone)) {
            phoneInputLayout.setError("Por favor, insira seu telefone");
            if (isValid) phoneInput.requestFocus();
            isValid = false;
        } else if (!isValidPhone(phone)) {
            phoneInputLayout.setError("Número de telefone inválido (9 dígitos)");
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

    private void sendOtpRequest(String name, String email, String password) {
        setLoadingState(true, true);

        RegisterRequest request = new RegisterRequest(name, email, password);

        apiService.register(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setLoadingState(false, true);

                if (response.isSuccessful()) {
                    handleOtpSentSuccess();
                } else {
                    handleOtpSentError(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setLoadingState(false, true);
                handleNetworkError(t, "enviar o código OTP");
            }
        });
    }

    private void handleOtpSentSuccess() {
        otpSent = true;
        otpInput.setEnabled(true);
        btnSendCode.setText("Reenviar Código");
        otpInputLayout.setHelperText("Código enviado! Verifique seu e-mail");

        Toast.makeText(this, "OTP enviado ao seu e-mail!", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "OTP enviado com sucesso");

        // Foco no campo OTP
        otpInput.requestFocus();
    }

    private void handleOtpSentError(int statusCode, String message) {
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
                errorMessage = "Erro ao enviar o código: " + message;
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Erro ao enviar OTP. Status: " + statusCode + ", Message: " + message);
    }

    /**
     * Cria a conta do usuário após verificar o OTP
     */
    private void handleCreateAccount() {
        if (isLoading) return;

        clearErrors();

        // Obter dados
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String otp = otpInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validar todos os campos
        if (!validateAllFields(name, phone, email, otp, password, confirmPassword)) {
            return;
        }

        // Verificar OTP e criar conta
        verifyOtpAndCreateAccount(name, email, otp, password);
    }

    private boolean validateAllFields(String name, String phone, String email,
                                      String otp, String password, String confirmPassword) {
        boolean isValid = true;

        // Validar campos básicos (mesmas validações do envio de OTP)
        if (!validateFieldsForOtp(name, phone, email, password, confirmPassword)) {
            isValid = false;
        }

        // Validar OTP
        if (!otpSent) {
            Toast.makeText(this, "Por favor, solicite o código OTP primeiro",
                    Toast.LENGTH_SHORT).show();
            btnSendCode.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(otp)) {
            otpInputLayout.setError(getString(R.string.error_empty_otp));
            if (isValid) otpInput.requestFocus();
            isValid = false;
        } else if (otp.length() != 6) {
            otpInputLayout.setError(getString(R.string.error_invalid_otp));
            if (isValid) otpInput.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void verifyOtpAndCreateAccount(String name, String email, String otp, String password) {
        setLoadingState(true, false);

        // Primeiro, verificar o OTP
        VerifyEmailRequest verifyRequest = new VerifyEmailRequest(email, otp);

        apiService.verifyEmail(verifyRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // OTP válido, prosseguir com o registro
                    Log.d(TAG, "OTP verificado com sucesso");
                    createAccount(name, email, password);
                } else {
                    setLoadingState(false, false);
                    handleOtpVerificationError(response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setLoadingState(false, false);
                handleNetworkError(t, "verificar o OTP");
            }
        });
    }

    private void handleOtpVerificationError(int statusCode) {
        String errorMessage;

        if (statusCode == 400 || statusCode == 401) {
            errorMessage = "Código OTP inválido ou expirado";
            otpInputLayout.setError(errorMessage);
        } else {
            errorMessage = "Erro ao verificar código. Tente novamente.";
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Erro na verificação do OTP. Status: " + statusCode);
    }

    private void createAccount(String name, String email, String password) {
        RegisterRequest registerRequest = new RegisterRequest(name, email, password);

        apiService.register(registerRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                setLoadingState(false, false);

                if (response.isSuccessful()) {
                    handleRegistrationSuccess(email);
                } else {
                    handleRegistrationError(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                setLoadingState(false, false);
                handleNetworkError(t, "criar a conta");
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
                errorMessage = "Erro no registro: " + message;
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Erro no registro. Status: " + statusCode + ", Message: " + message);
    }

    private void handleNetworkError(Throwable t, String action) {
        String errorMessage;

        if (t instanceof java.net.UnknownHostException) {
            errorMessage = "Sem conexão com a internet";
        } else if (t instanceof java.net.SocketTimeoutException) {
            errorMessage = "Tempo de conexão esgotado";
        } else {
            errorMessage = "Erro de conexão ao " + action + ": " + t.getMessage();
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Erro de rede ao " + action, t);
    }

    private void clearErrors() {
        nameInputLayout.setError(null);
        phoneInputLayout.setError(null);
        emailInputLayout.setError(null);
        otpInputLayout.setError(null);
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

    private void setLoadingState(boolean loading, boolean isSendingOtp) {
        isLoading = loading;

        if (isSendingOtp) {
            // Estado de loading ao enviar OTP
            btnSendCode.setEnabled(!loading);
            nameInput.setEnabled(!loading);
            phoneInput.setEnabled(!loading);
            emailInput.setEnabled(!loading);
            passwordInput.setEnabled(!loading);
            confirmPasswordInput.setEnabled(!loading);

            if (loading) {
                btnSendCode.setText("Enviando...");
            } else {
                btnSendCode.setText(otpSent ? "Reenviar Código" : "Receber Código");
            }
        } else {
            // Estado de loading ao criar conta
            btnCreateAccount.setEnabled(!loading);
            nameInput.setEnabled(!loading);
            phoneInput.setEnabled(!loading);
            emailInput.setEnabled(!loading);
            otpInput.setEnabled(!loading);
            passwordInput.setEnabled(!loading);
            confirmPasswordInput.setEnabled(!loading);
            btnSendCode.setEnabled(!loading);

            if (loading) {
                btnCreateAccount.setText("Criando conta...");
            } else {
                btnCreateAccount.setText(getString(R.string.create_account));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpar referências
        apiService = null;
    }
}