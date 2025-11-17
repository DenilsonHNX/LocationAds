package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PerfilAccount extends AppCompatActivity {

    private ImageView toEditPerfil;
    private ImageView btnLogOut;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvNome = findViewById(R.id.perfilName); // Adicione esse id no XML, ou adapte
        TextView tvEmail = findViewById(R.id.info_email);
        TextView tvTelefone = findViewById(R.id.info_);
        TextView tvDataCriacao = findViewById(R.id.info_dataCriancaoDaConta);

        // Obtendo SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);

        String nome = sharedPref.getString("nomeCompleto", "Nome não definido");
        String email = sharedPref.getString("email", "Email não definido");
        String telefone = sharedPref.getString("telefone", "Telefone não definido");
        String dataCriacao = sharedPref.getString("dataCriacao", "Data não definida");

        // Setando os valores nos TextViews
        // Se não tem TextView para nome no XML, será necessário adicionar um com id para mostrar.
        if (tvNome != null) {
            tvNome.setText(nome);
        } else {
            // Se não tem, pode usar o TextView equivalente:
            // No seu XML, o TextView "NOME DO USUÁRIO" não tem id, então pode adicionar android:id="@+id/nome_usuario_textview"
        }

        tvEmail.setText(email);
        tvTelefone.setText(telefone);
        tvDataCriacao.setText(dataCriacao);

        toEditPerfil = findViewById(R.id.toEditPerfil);
        btnLogOut = findViewById(R.id.btnLogOut);
        // Não há id no ImageView com ic_back, precisaria adicionar para detectar clique, caso queira
        // Exemplo: android:id="@+id/btnBack"
        // Aqui supondo que você adicionou esse id no XML
        btnBack = findViewById(R.id.btnBack);

        // Intent para editar perfil
        toEditPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PerfilAccount.this, EditPerfilAccount.class);
                startActivity(intent);
            }
        });

        // Intent para logout
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Limpa dados de sessão, marcando isLoggedIn como false
                SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.clear();
                editor.apply();

                // Lógica de logout, limpar sessão, etc.
                // Depois voltar para tela de login, limpando o histórico para não voltar atrás
                Intent intent = new Intent(PerfilAccount.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });


        // Intent para voltar - precisa do id no XML para funcionar
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Fecha essa activity para voltar à anterior
                    finish();
                }
            });
        }


    }
}