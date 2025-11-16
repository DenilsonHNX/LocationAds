package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.RegisterRequest;
import ao.co.isptec.aplm.locationads.network.models.VerifyEmailRequest;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    ApiService apiService = ApiClient.getInstance().getApiService();
    private EditText nameInput, numberInput, emailInput, emailOtpInput, firstPasswordInput, confirmPasswordInput;
    private Button btnSendCode, btnCreateAccount;
    private TextView toLoginBtn;

    boolean verifyToCreateAccount = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameInput = findViewById(R.id.name_input);
        numberInput = findViewById(R.id.number_input);
        emailInput = findViewById(R.id.input_email);
        emailOtpInput = findViewById(R.id.email_otp);
        firstPasswordInput = findViewById(R.id.firstPassword_input);
        confirmPasswordInput = findViewById(R.id.confirmFirstPassword_input);

        btnSendCode = findViewById(R.id.btnSendCode);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        toLoginBtn = findViewById(R.id.toLogin_btn);

        TextView toLogin_btn = findViewById(R.id.toLogin_btn);

        // Criar conta ao clicar no botão
        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameInput.getText().toString().trim();
                String phone = numberInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String password = firstPasswordInput.getText().toString();
                String confirmPassword = confirmPasswordInput.getText().toString();

                // Valide os campos como já faz

                if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, "Senhas não coincidem.", Toast.LENGTH_LONG).show();
                    return;
                }

                RegisterRequest request = new RegisterRequest(name, email, password);

                apiService.register(request).enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                        if (response.isSuccessful()) {
                            verifyToCreateAccount = true;
                            Toast.makeText(RegisterActivity.this, "OTP enviado ao seu email!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Erro ao enviar o cógiho OTP: " + response.message(), Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(RegisterActivity.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnCreateAccount.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String phone = numberInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String otp = emailOtpInput.getText().toString().trim();
            String password = firstPasswordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || otp.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_LONG).show();
                return;
            }
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Senhas não coincidem.", Toast.LENGTH_LONG).show();
                return;
            }


            apiService.verifyEmail(new VerifyEmailRequest(email, otp)).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                    if (response.isSuccessful()) {

                        RegisterRequest registerRequest = new RegisterRequest(name, email, password);
                        apiService.register(registerRequest).enqueue(new retrofit2.Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Usuário registrado com sucesso!", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Erro no registro: " + response.message(), Toast.LENGTH_LONG).show();
                                }
                            }
                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(RegisterActivity.this, "Erro de conexão no registro: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Código OTP inválido.", Toast.LENGTH_LONG).show();
                    }
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(RegisterActivity.this, "Erro na verificação do OTP: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });


        toLogin_btn.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        });
    }
}