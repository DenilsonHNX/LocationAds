package ao.co.isptec.aplm.locationads;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddLocal extends AppCompatActivity {

    private static final String TAG = "AddLocal";
    private static final int PERMISSIONS_REQUEST_ACCESS_LOCATION = 100;
    private static final long LOCATION_UPDATE_INTERVAL = 5000; // 5 segundos
    private static final long LOCATION_FASTEST_INTERVAL = 2000; // 2 segundos

    // Views - EditTexts
    private TextInputEditText inputNome;
    private TextInputEditText inputTipo;
    private TextInputEditText inputRaio;
    private TextInputEditText inputWifiIds;

    // Views - InputLayouts
    private TextInputLayout nomeInputLayout;
    private TextInputLayout tipoInputLayout;
    private TextInputLayout raioInputLayout;
    private TextInputLayout wifiIdsInputLayout;

    // Views - Outros
    private MaterialButton btnAddLocal;
    private ImageButton btnBack;
    private TextView locationCoordinates;
    private ProgressBar locationProgressBar;
    private MaterialCardView locationInfoCard;

    // API e Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private ApiService apiService;

    // Estado
    private Double currentLatitude = null;
    private Double currentLongitude = null;
    private boolean isLoading = false;
    private boolean isLocationUpdating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_local);

        initApiService();
        initViews();
        setupListeners();
        initLocationServices();
        getLocationPermissionAndFetch();
    }

    private void initApiService() {
        apiService = ApiClient.getInstance().getApiService();
    }

    private void initViews() {
        // EditTexts
        inputNome = findViewById(R.id.inputNome);
        inputTipo = findViewById(R.id.inputTipo);
        inputRaio = findViewById(R.id.inputRaio);
        inputWifiIds = findViewById(R.id.inputWifiIds);

        // InputLayouts
        nomeInputLayout = findViewById(R.id.nomeInputLayout);
        tipoInputLayout = findViewById(R.id.tipoInputLayout);
        raioInputLayout = findViewById(R.id.raioInputLayout);
        wifiIdsInputLayout = findViewById(R.id.wifiIdsInputLayout);

        // Buttons e outros
        btnAddLocal = findViewById(R.id.btnAddLocal);
        btnBack = findViewById(R.id.btnBack);
        locationCoordinates = findViewById(R.id.locationCoordinates);
        locationProgressBar = findViewById(R.id.locationProgressBar);
        locationInfoCard = findViewById(R.id.locationInfoCard);
    }

    private void setupListeners() {
        btnAddLocal.setOnClickListener(v -> handleCreateLocal());
        btnBack.setOnClickListener(v -> finish());
    }

    private void initLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Criar LocationRequest para atualizações em tempo real
        locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL
        )
                .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
                .setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL)
                .build();

        // Callback para receber atualizações de localização
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult.getLastLocation() != null) {
                    currentLatitude = locationResult.getLastLocation().getLatitude();
                    currentLongitude = locationResult.getLastLocation().getLongitude();
                    updateLocationUI();

                    Log.d(TAG, "Localização atualizada: " + currentLatitude + ", " + currentLongitude);
                }
            }
        };
    }

    private void handleCreateLocal() {
        if (isLoading) return;

        clearErrors();

        // Verificar se tem localização
        if (currentLatitude == null || currentLongitude == null) {
            Toast.makeText(this, getString(R.string.error_waiting_location),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Obter dados
        String nome = inputNome.getText().toString().trim();
        String tipo = inputTipo.getText().toString().trim();
        String raioStr = inputRaio.getText().toString().trim();
        String wifiIdsStr = inputWifiIds.getText().toString().trim();

        // Validar
        if (!validateInputs(nome, tipo, raioStr)) {
            return;
        }

        // Converter raio
        int raio;
        try {
            raio = Integer.parseInt(raioStr);
        } catch (NumberFormatException e) {
            raioInputLayout.setError(getString(R.string.error_invalid_radius));
            inputRaio.requestFocus();
            return;
        }

        // Processar WiFi IDs
        List<String> wifiIds = wifiIdsStr.isEmpty() ?
                null : Arrays.asList(wifiIdsStr.split(",\\s*"));

        // Criar local
        createLocal(nome, tipo, raio, wifiIds);
    }

    private boolean validateInputs(String nome, String tipo, String raio) {
        boolean isValid = true;

        // Validar nome
        if (TextUtils.isEmpty(nome)) {
            nomeInputLayout.setError(getString(R.string.error_empty_local_name));
            if (isValid) inputNome.requestFocus();
            isValid = false;
        } else if (nome.length() < 3) {
            nomeInputLayout.setError("Nome deve ter pelo menos 3 caracteres");
            if (isValid) inputNome.requestFocus();
            isValid = false;
        }

        // Validar tipo
        if (TextUtils.isEmpty(tipo)) {
            tipoInputLayout.setError(getString(R.string.error_empty_type));
            if (isValid) inputTipo.requestFocus();
            isValid = false;
        }

        // Validar raio
        if (TextUtils.isEmpty(raio)) {
            raioInputLayout.setError(getString(R.string.error_empty_radius));
            if (isValid) inputRaio.requestFocus();
            isValid = false;
        } else {
            try {
                int raioInt = Integer.parseInt(raio);
                if (raioInt <= 0) {
                    raioInputLayout.setError("Raio deve ser maior que zero");
                    if (isValid) inputRaio.requestFocus();
                    isValid = false;
                } else if (raioInt > 10000) {
                    raioInputLayout.setError("Raio máximo: 10000 metros");
                    if (isValid) inputRaio.requestFocus();
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                raioInputLayout.setError(getString(R.string.error_invalid_radius));
                if (isValid) inputRaio.requestFocus();
                isValid = false;
            }
        }

        return isValid;
    }

    private void createLocal(String nome, String tipo, int raio, List<String> wifiIds) {
        setLoadingState(true);

        Local novoLocal = new Local(nome, tipo, currentLatitude, currentLongitude, raio, wifiIds);

        Log.d(TAG, "Criando local: " + new Gson().toJson(novoLocal));

        apiService.addLocal(novoLocal).enqueue(new Callback<Local>() {
            @Override
            public void onResponse(Call<Local> call, Response<Local> response) {
                setLoadingState(false);

                // LOG DA RESPOSTA
                Log.d(TAG, "Response Code: " + response.code());
                Log.d(TAG, "Response Message: " + response.message());

                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Sem corpo de erro";
                        Log.e(TAG, "Error Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody", e);
                    }
                }

                if (response.isSuccessful()) {
                    handleLocalCreatedSuccess();
                } else {
                    handleLocalCreatedError(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(Call<Local> call, Throwable t) {
                setLoadingState(false);
                handleNetworkError(t);
            }
        });
    }

    private void handleLocalCreatedSuccess() {
        Log.d(TAG, "Local criado com sucesso");
        Toast.makeText(this, getString(R.string.local_created_success),
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void handleLocalCreatedError(int statusCode, String message) {
        String errorMessage;

        switch (statusCode) {
            case 400:
                errorMessage = "Dados inválidos. Verifique os campos.";
                break;
            case 401:
                errorMessage = "Não autorizado. Faça login novamente.";
                break;
            case 409:
                errorMessage = "Este local já existe.";
                break;
            case 500:
                errorMessage = "Erro no servidor. Tente novamente mais tarde.";
                break;
            default:
                errorMessage = getString(R.string.error_creating_local) + " (Código: " + statusCode + ")";
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Erro ao criar local. Status: " + statusCode + ", Message: " + message);
    }

    private void handleNetworkError(Throwable t) {
        String errorMessage;

        if (t instanceof java.net.UnknownHostException) {
            errorMessage = "Sem conexão com a internet";
        } else if (t instanceof java.net.SocketTimeoutException) {
            errorMessage = "Tempo de conexão esgotado";
        } else {
            errorMessage = "Erro de conexão: " + t.getMessage();
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Erro de rede", t);
    }

    private void clearErrors() {
        nomeInputLayout.setError(null);
        tipoInputLayout.setError(null);
        raioInputLayout.setError(null);
        wifiIdsInputLayout.setError(null);
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;

        btnAddLocal.setEnabled(!loading);
        inputNome.setEnabled(!loading);
        inputTipo.setEnabled(!loading);
        inputRaio.setEnabled(!loading);
        inputWifiIds.setEnabled(!loading);

        if (loading) {
            btnAddLocal.setText("Criando...");
        } else {
            btnAddLocal.setText(getString(R.string.create_local));
        }
    }

    // ========== LOCALIZAÇÃO EM TEMPO REAL ==========

    private void getLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_LOCATION);
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationProgressBar.setVisibility(View.VISIBLE);
        isLocationUpdating = true;

        // Iniciar atualizações de localização em tempo real
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        ).addOnSuccessListener(aVoid -> {
            Log.d(TAG, "Atualizações de localização iniciadas com sucesso");
            locationProgressBar.setVisibility(View.GONE);
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Erro ao iniciar atualizações de localização", e);
            locationProgressBar.setVisibility(View.GONE);
            showLocationError();
        });

        // Também pegar a última localização conhecida para exibir algo imediatamente
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLatitude = location.getLatitude();
                currentLongitude = location.getLongitude();
                updateLocationUI();
                Log.d(TAG, "Localização inicial obtida: " + currentLatitude + ", " + currentLongitude);
            }
        });
    }

    private void stopLocationUpdates() {
        if (isLocationUpdating && fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isLocationUpdating = false;
            Log.d(TAG, "Atualizações de localização interrompidas");
        }
    }

    private void updateLocationUI() {
        if (currentLatitude != null && currentLongitude != null) {
            String coordinates = String.format(Locale.getDefault(),
                    "Lat: %.6f, Long: %.6f",
                    currentLatitude, currentLongitude);
            locationCoordinates.setText(coordinates);

            // Mudar a cor do card para verde quando tiver localização
            locationInfoCard.setCardBackgroundColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_light)
            );
        }
    }

    private void showLocationError() {
        locationCoordinates.setText(getString(R.string.error_getting_location));
        Toast.makeText(this, getString(R.string.enable_location),
                Toast.LENGTH_LONG).show();

        // Mudar a cor do card para vermelho quando houver erro
        locationInfoCard.setCardBackgroundColor(
                ContextCompat.getColor(this, android.R.color.holo_red_light)
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, getString(R.string.location_permission_denied),
                        Toast.LENGTH_LONG).show();
                locationProgressBar.setVisibility(View.GONE);
                showLocationError();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Retomar atualizações de localização quando a activity voltar ao primeiro plano
        if (!isLocationUpdating && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar atualizações de localização para economizar bateria
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Parar atualizações de localização e limpar referências
        stopLocationUpdates();
        apiService = null;
        fusedLocationClient = null;
        locationCallback = null;
    }
}