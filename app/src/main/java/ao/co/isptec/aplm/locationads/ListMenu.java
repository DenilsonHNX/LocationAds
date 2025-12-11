package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

public class ListMenu extends AppCompatActivity {

    private MaterialCardView btnAnunciosGuardados, btnAnunciosCriados;
    private TextView txtGuardados, txtCriados;
    private LinearLayout containerGuardados, containerCriados;
    private RecyclerView listGuardados, listCriados;

    private boolean showingGuardados = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar views
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        btnAnunciosGuardados = findViewById(R.id.btnAnunciosGuardados);
        btnAnunciosCriados = findViewById(R.id.btnAnunciosCriados);
        txtGuardados = findViewById(R.id.txtGuardados);
        txtCriados = findViewById(R.id.txtCriados);
        containerGuardados = findViewById(R.id.containerGuardados);
        containerCriados = findViewById(R.id.containerCriados);
        listGuardados = findViewById(R.id.listGuardados);
        listCriados = findViewById(R.id.listCriados);

        // Botão voltar
        btnVoltar.setOnClickListener(v -> {
            finish(); // Volta para a tela anterior em vez de criar nova MainActivity
        });

        // Tab Anúncios Guardados
        btnAnunciosGuardados.setOnClickListener(v -> {
            if (!showingGuardados) {
                switchToGuardados();
            }
        });

        // Tab Anúncios Criados
        btnAnunciosCriados.setOnClickListener(v -> {
            if (showingGuardados) {
                switchToCriados();
            }
        });

        // Configurar RecyclerViews
        // listGuardados.setLayoutManager(new LinearLayoutManager(this));
        // listCriados.setLayoutManager(new LinearLayoutManager(this));

        // TODO: Configurar adapters quando tiver os dados
        // listGuardados.setAdapter(guardadosAdapter);
        // listCriados.setAdapter(criadosAdapter);
    }

    private void switchToGuardados() {
        showingGuardados = true;

        // Atualizar cores dos botões
        btnAnunciosGuardados.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.primary)
        );
        btnAnunciosCriados.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.white)
        );

        // Atualizar cores do texto
        txtGuardados.setTextColor(
                ContextCompat.getColor(this, R.color.white)
        );
        txtCriados.setTextColor(
                ContextCompat.getColor(this, android.R.color.darker_gray)
        );

        // Mostrar/ocultar containers
        containerGuardados.setVisibility(View.VISIBLE);
        containerCriados.setVisibility(View.GONE);
    }

    private void switchToCriados() {
        showingGuardados = false;

        // Atualizar cores dos botões
        btnAnunciosCriados.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.primary)
        );
        btnAnunciosGuardados.setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.white)
        );

        // Atualizar cores do texto
        txtCriados.setTextColor(
                ContextCompat.getColor(this, R.color.white)
        );
        txtGuardados.setTextColor(
                ContextCompat.getColor(this, android.R.color.darker_gray)
        );

        // Mostrar/ocultar containers
        containerCriados.setVisibility(View.VISIBLE);
        containerGuardados.setVisibility(View.GONE);
    }
}