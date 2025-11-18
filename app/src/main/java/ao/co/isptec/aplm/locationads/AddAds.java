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

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;

public class AddAds extends AppCompatActivity {

    private String inputNome, inputTipo, inputLatitude, inputLongitude, inputRaio, inputWifiIds;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_ads);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = ApiClient.getInstance().getApiService();

        Button btnPublicar = findViewById(R.id.btnPublicar);
        btnPublicar.setOnClickListener(v -> {
            // aqui futuramente salva o anúncio no BD
        });

        // botão escolher imagem
        TextView btnImg = findViewById(R.id.btnEscolherImagem);
        btnImg.setOnClickListener(v -> {
            // aqui futuramente abre a galeria
        });

        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        });
    }
}