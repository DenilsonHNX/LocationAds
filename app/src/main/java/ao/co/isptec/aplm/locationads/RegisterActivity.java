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


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {


    private EditText nameInput, numberInput, emailInput, emailOtpInput, firstPasswordInput, confirmPasswordInput;
    private Button btnSendCode, btnCreateAccount;
    private TextView toLoginBtn;


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

        // Enviar código OTP por email
        btnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailInput.getText().toString().trim();
                if (!email.isEmpty()) {
                    // Aqui adiciona lógica para enviar código OTP para o email
                    // Pode mostrar mensagem ou bloqueios temporários dependendo da sua lógica
                }
            }
        });

        // Criar conta ao clicar no botão
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // Aqui pode-se validar campos, confirmar OTP, validar senha, etc.
                String name = nameInput.getText().toString().trim();
                String phone = numberInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String otp = emailOtpInput.getText().toString().trim();
                String password = firstPasswordInput.getText().toString();
                String confirmPassword = confirmPasswordInput.getText().toString();

                // Validações básicas, depois processo de cadastro

                if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || otp.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    // Mostrar erro
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    // Mostrar erro de senha não confere
                    return;
                }

                String dataAtual = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());


                SharedPreferences sharedPref = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                String emailExistente = sharedPref.getString("email", null);

                if (emailExistente != null && emailExistente.equals(email)) {
                    Toast.makeText(RegisterActivity.this, "Este email já está registrado.", Toast.LENGTH_LONG).show();
                    // Não salve novamente
                } else {

                    SharedPreferences.Editor editor = sharedPref.edit();

                    // Salva os dados do usuário
                    editor.putString("nomeCompleto", name);
                    editor.putString("telefone", phone);
                    editor.putString("email", email);
                    editor.putString("senha", password);  // Armazene senha com cuidado, ideal usar hash para segurança
                    // OTP pode ser ignorado aqui ou tratado separadamente, já que é para verificação
                    editor.putString("dataCriacao", dataAtual);
                    editor.apply();
                }
                Toast.makeText(RegisterActivity.this, "Usuário registrado com sucesso!", Toast.LENGTH_SHORT).show();



                // Se tudo válido, realizar cadastro e prosseguir (exemplo: abrir LoginActivity)
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        toLogin_btn.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
        });
    }
}