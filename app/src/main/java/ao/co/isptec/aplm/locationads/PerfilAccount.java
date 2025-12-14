package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PerfilAccount extends AppCompatActivity {

    private FloatingActionButton toEditPerfil;
    private ImageButton btnLogOut;
    private ImageButton btnBack;

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

        // Obter referências das views
        TextView tvNome = findViewById(R.id.perfilName);
        TextView tvEmail = findViewById(R.id.info_email);
        TextView tvTelefone = findViewById(R.id.info_phone);  // ✅ CORRIGIDO!
        TextView tvDataCriacao = findViewById(R.id.info_dataCriancaoDaConta);

        // Obtendo SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);

        String nome = sharedPref.getString("nomeCompleto", "Nome não definido");
        String email = sharedPref.getString("email", "Email não definido");
        String telefone = sharedPref.getString("telefone", "Telefone não definido");
        String dataCriacao = sharedPref.getString("dataCriacao", "Data não definida");

        // Setando os valores nos TextViews
        if (tvNome != null) {
            tvNome.setText(nome);
        }

        if (tvEmail != null) {
            tvEmail.setText(email);
        }

        if (tvTelefone != null) {
            tvTelefone.setText(telefone);
        }

        if (tvDataCriacao != null) {
            tvDataCriacao.setText(dataCriacao);
        }

        // Botões
        toEditPerfil = findViewById(R.id.toEditPerfil);
        btnLogOut = findViewById(R.id.btnLogOut);
        btnBack = findViewById(R.id.btnBack);

        // Intent para editar perfil
        if (toEditPerfil != null) {
            toEditPerfil.setOnClickListener(v -> {
                Intent intent = new Intent(PerfilAccount.this, EditPerfilAccount.class);
                startActivity(intent);
            });
        }

        // Intent para logout
        if (btnLogOut != null) {
            btnLogOut.setOnClickListener(v -> {
                // Limpa dados de sessão
                SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                // Voltar para tela de login
                Intent intent = new Intent(PerfilAccount.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Intent para voltar
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                // Fecha essa activity para voltar à anterior
                finish();
            });
        }
    }
}