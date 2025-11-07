package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditPerfilAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_perfil_account);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> {
            Intent i = new Intent(EditPerfilAccount.this, PerfilAccount.class);
            startActivity(i);
            finish();
        });

        // editar foto -> outra activity
        TextView btnTrocarFoto = findViewById(R.id.btnTrocarFoto);
        btnTrocarFoto.setOnClickListener(v -> {
            // Futuramente codigo aqui
        });

        // guardar alterações
        Button btnGuardar = findViewById(R.id.btnGuardar);
        btnGuardar.setOnClickListener(v -> {
            Intent i = new Intent(EditPerfilAccount.this, PerfilAccount.class);
            startActivity(i);
            finish();
        });
    }
}