package ao.co.isptec.aplm.locationads;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.Arrays;
import java.util.List;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddLocal extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 100;

    private EditText inputNome, inputTipo, inputRaio, inputWifiIds;
    private Button btnPublicar;

    private FusedLocationProviderClient fusedLocationClient;
    private ApiService apiService;

    private Double currentLatitude = null;
    private Double currentLongitude = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_local);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = ApiClient.getInstance().getApiService();

        inputNome = findViewById(R.id.inputNome);
        inputTipo = findViewById(R.id.inputTipo);
        inputRaio = findViewById(R.id.inputRaio);
        inputWifiIds = findViewById(R.id.inputWifiIds);
        btnPublicar = findViewById(R.id.btnAddLocal);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocationPermissionAndFetch();

        btnPublicar.setOnClickListener(v -> {
            if (currentLatitude == null || currentLongitude == null) {
                Toast.makeText(this, "Aguardando localização. Tente novamente em instantes.", Toast.LENGTH_SHORT).show();
                return;
            }

            String nome = inputNome.getText().toString().trim();
            String tipo = inputTipo.getText().toString().trim();
            String raioStr = inputRaio.getText().toString().trim();
            String wifiIdsStr = inputWifiIds.getText().toString().trim();

            if (nome.isEmpty() || tipo.isEmpty() || raioStr.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_LONG).show();
                return;
            }

            int raio;
            try {
                raio = Integer.parseInt(raioStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Raio inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> wifiIds = wifiIdsStr.isEmpty() ? null : Arrays.asList(wifiIdsStr.split(",\\s*"));

            Local novoLocal = new Local(nome, tipo, currentLatitude, currentLongitude, raio, wifiIds);

            apiService.addLocal(novoLocal).enqueue(new Callback<Local>() {
                @Override
                public void onResponse(Call<Local> call, Response<Local> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddLocal.this, "Local adicionado com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddLocal.this, "Falha ao adicionar local", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Local> call, Throwable t) {
                    Toast.makeText(AddLocal.this, "Erro na conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        });
    }

    private void getLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_LOCATION);
        }
    }

    private void fetchLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
            } else {
                Toast.makeText(this, "Não foi possível obter a localização actual!", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, "Active a permissão da Localização", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Erro ao pegar localização: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show();
            }
        }
    }
}