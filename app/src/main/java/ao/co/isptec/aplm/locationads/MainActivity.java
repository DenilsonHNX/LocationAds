package ao.co.isptec.aplm.locationads;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
<<<<<<< Updated upstream
        setContentView(R.layout.login_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
=======
        setContentView(R.layout.home_activity);

        // inicialize fusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        containerAnuncios = findViewById(R.id.containerAnuncios);

        // Recuperar perfil do usuário
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String perfilJson = prefs.getString("perfil_usuario", "{}");
        try {
            JSONObject jsonObject = new JSONObject(perfilJson);
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                perfilUsuario.put(key, jsonObject.getString(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mostrarAnunciosFiltrados();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Opção 1: Usar View (genérico, funciona para qualquer tipo)
        View btnToPerfil = findViewById(R.id.btnToPerfil);
        btnToPerfil.setOnClickListener(v -> {
            Intent i = new Intent(this, PerfilAccount.class);
            startActivity(i);
        });

        View btnToList = findViewById(R.id.btnToList);
        btnToList.setOnClickListener(v -> {
            Intent i = new Intent(this, ListMenu.class);
            startActivity(i);
        });

        View btnToIdea = findViewById(R.id.btnToIdea);
        btnToIdea.setOnClickListener(v -> {
            Intent i = new Intent(this, AboutApp.class);
            startActivity(i);
        });

// btnToAddAds continua como estava (é FAB, está correto)

        ImageView btnToAddAds = findViewById(R.id.btnToAddAds);
        btnToAddAds.setOnClickListener(v -> {
            AddOpctions dialog = new AddOpctions();
            dialog.setListener(new AddOpctions.AddOptionsListener() {
                @Override
                public void onAddLocalSelected() {
                    Intent intent = new Intent(MainActivity.this, AddLocal.class);
                    startActivity(intent);
                }
                @Override
                public void onAddAdsSelected() {
                    Intent intent = new Intent(MainActivity.this, AddAds.class);
                    startActivity(intent);
                }
            });
            dialog.show(getSupportFragmentManager(), "AddOptionsDialog");
>>>>>>> Stashed changes
        });
    }
}