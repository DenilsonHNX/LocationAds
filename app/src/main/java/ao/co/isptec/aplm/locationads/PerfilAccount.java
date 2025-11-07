package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

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
                // Lógica de logout, limpar sessão, etc.
                // Depois voltar para tela de login
                Intent intent = new Intent(PerfilAccount.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // limpa histórico
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