package ao.co.isptec.aplm.locationads;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ao.co.isptec.aplm.locationads.adapter.AnunciosAdapter;
import ao.co.isptec.aplm.locationads.adapter.LocaisAdapter;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
<<<<<<< HEAD
import retrofit2.Callback;
=======
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private static final String TAG = "MainActivity";

    // Views
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
<<<<<<< HEAD

    private List<Ads> anunciosWhitelist = new ArrayList<>();
    private List<Ads> anunciosCriados = new ArrayList<>();

    private AnunciosAdapter adapterWhitelist;
    private AnunciosAdapter adapterCriados;

    private TextView emptyStateText;
=======
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31
    private RecyclerView listaLocais;
    private RecyclerView recyclerViewAnuncios;
    private LocaisAdapter locaisAdapter;
    private AnunciosAdapter anunciosAdapter;
    private TextView locActual;
    private TextView txtTotalAnuncios;
    private TabLayout tabLayout;
    private MaterialCardView emptyStateCard;
    private View mapView;

    // Data
    private ApiService apiService;
    private List<Ads> anunciosFiltrados; // ✅ Mudar de AdMessage para Ads

    // Data
    private Map<String, String> perfilUsuario = new HashMap<>();

<<<<<<< HEAD
    private int currentTab = 0;

=======
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        // Inicializar API e Location
        apiService = ApiClient.getInstance().getApiService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializar views
        initViews();

        // Configurar listeners
        setupListeners();

        // Carregar dados do usuário
        loadUserProfile();

        // Configurar mapa
        setupMap();

        // Configurar RecyclerViews
        setupRecyclerViews();

        // Carregar anúncios
        loadAds();
    }

    /**
     * Inicializa todas as views
     */
    private void initViews() {
        listaLocais = findViewById(R.id.listaLocais);
        recyclerViewAnuncios = findViewById(R.id.recyclerViewAnuncios);
        locActual = findViewById(R.id.locActual);
        txtTotalAnuncios = findViewById(R.id.txtTotalAnuncios);
        tabLayout = findViewById(R.id.tabLayout);
<<<<<<< HEAD
        emptyStateText = findViewById(R.id.emptyStateText);
=======
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31
        emptyStateCard = findViewById(R.id.emptyStateCard);
    }

    /**
     * Configura os RecyclerViews
     */
    private void setupRecyclerViews() {
<<<<<<< HEAD

        adapterWhitelist = new AnunciosAdapter(this, anunciosWhitelist);
        adapterCriados = new AnunciosAdapter(this, anunciosCriados);

// Usa o adapter da whitelist por default
        recyclerViewAnuncios.setAdapter(adapterWhitelist);

=======
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31
        // RecyclerView de Locais
        listaLocais.setLayoutManager(new LinearLayoutManager(this));
        locaisAdapter = new LocaisAdapter(new ArrayList<>());
        listaLocais.setAdapter(locaisAdapter);

        // RecyclerView de Anúncios
        recyclerViewAnuncios.setLayoutManager(new LinearLayoutManager(this));
        anunciosFiltrados = new ArrayList<>(); // ✅ Inicializar lista
        anunciosAdapter = new AnunciosAdapter(this, anunciosFiltrados);
        recyclerViewAnuncios.setAdapter(anunciosAdapter);
    }

    /**
     * Configura todos os listeners
     */
    private void setupListeners() {
        // TabLayout - Alternar entre Mapa e Lista
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showMap();
                } else {
                    showLocaisList();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // Bottom Navigation
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Toast.makeText(this, "Você já está na Home", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnToList).setOnClickListener(v -> {
            Intent intent = new Intent(this, ListMenu.class);
            startActivity(intent);
        });

        findViewById(R.id.btnToIdea).setOnClickListener(v -> {
            Intent intent = new Intent(this, AboutApp.class);
            startActivity(intent);
        });

        findViewById(R.id.btnToPerfil).setOnClickListener(v -> {
            Intent intent = new Intent(this, PerfilAccount.class);
            startActivity(intent);
        });

        // FAB - Adicionar Local ou Anúncio
        FloatingActionButton fabAdd = findViewById(R.id.btnToAddAds);
        fabAdd.setOnClickListener(v -> showAddOptionsDialog());
    }

    /**
     * Mostra dialog para escolher entre adicionar Local ou Anúncio
     */
    private void showAddOptionsDialog() {
        AddOptionsDialog dialog = new AddOptionsDialog();
        dialog.setListener(new AddOptionsDialog.AddOptionsListener() {
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
    }

    /**
     * Carrega o perfil do usuário
     */
    private void loadUserProfile() {
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
    }

    /**
     * Configura o fragmento do mapa
     */
    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            mapView = mapFragment.getView();
        }
    }

    /**
     * Mostra a view do mapa
     */
    private void showMap() {
        if (mapView != null) {
            mapView.setVisibility(View.VISIBLE);
        }
        listaLocais.setVisibility(View.GONE);
    }

    /**
     * Mostra a lista de locais
     */
    private void showLocaisList() {
        if (mapView != null) {
            mapView.setVisibility(View.GONE);
        }
        listaLocais.setVisibility(View.VISIBLE);
        buscarTodosLocais();
    }

    /**
     * Busca todos os locais da API
     */
    private void buscarTodosLocais() {
        apiService.getAllLocals()
                .enqueue(new retrofit2.Callback<List<Local>>() {
                    @Override
                    public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Local> locais = response.body();
                            locaisAdapter.updateData(locais);

                            if (locais.isEmpty()) {
                                Toast.makeText(MainActivity.this,
                                        "Nenhum local encontrado",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Erro ao carregar locais",
                                    Toast.LENGTH_SHORT).show();
                            locaisAdapter.updateData(new ArrayList<>());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Local>> call, Throwable t) {
                        Toast.makeText(MainActivity.this,
                                "Erro de conexão: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        locaisAdapter.updateData(new ArrayList<>());
                    }
                });
    }


    /**
     * Carrega e filtra os anúncios de todos os locais
     */
    private void loadAds() {
        Log.d(TAG, "========== CARREGANDO ANÚNCIOS ==========");

        // Primeiro, buscar todos os locais
        apiService.getAllLocals().enqueue(new retrofit2.Callback<List<Local>>() {
            @Override
            public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Local> locais = response.body();
                    Log.d(TAG, "✅ Locais encontrados: " + locais.size());

                    // Limpar lista
                    anunciosFiltrados.clear();

                    if (locais.isEmpty()) {
                        Log.d(TAG, "⚠️ Nenhum local encontrado");
                        runOnUiThread(() -> {
                            updateAdsUI();
                            Toast.makeText(MainActivity.this,
                                    "Nenhum local encontrado",
                                    Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }

                    // Contador para saber quando terminou todas as requisições
                    final int totalLocais = locais.size();
                    final int[] locaisProcessados = {0};

<<<<<<< HEAD
                    // Teste ----------

                    apiService.getAdsWhitelist().enqueue(new Callback<List<Ads>>() {
                        @Override
                        public void onResponse(Call<List<Ads>> call, Response<List<Ads>> response) {
                            Log.d(TAG, "📨 Resposta getAdsWhitelist: " + response.code());

                            if (response.isSuccessful() && response.body() != null) {

                                anunciosWhitelist.clear(); // ou anunciosWhitelist, se quiser separar
                                anunciosWhitelist.addAll(response.body());

                                Log.d(TAG, "✅ Anúncios da whitelist carregados: " + anunciosWhitelist.size());

                                // Log de cada anúncio
                                for (int i = 0; i < anunciosWhitelist.size(); i++) {
                                    Ads ads = anunciosWhitelist.get(i);
                                    Log.d(TAG, "  " + (i + 1) + ". " + ads.getTitulo());
                                }

                                runOnUiThread(() -> {
                                    if (currentTab == 0) {
                                        updateUI();
                                        Toast.makeText(
                                                MainActivity.this,
                                                anunciosWhitelist.size() + " anúncios da whitelist",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                });

                            } else {
                                Log.e(TAG, "❌ Erro ao carregar anúncios da whitelist: " + response.code());

                                try {
                                    String errorBody = response.errorBody() != null
                                            ? response.errorBody().string()
                                            : "Sem corpo de erro";
                                    Log.e(TAG, "Error Body: " + errorBody);
                                } catch (Exception e) {
                                    Log.e(TAG, "Erro ao ler errorBody", e);
                                }

                                runOnUiThread(() -> {
                                    if (currentTab == 0) {
                                        updateUI();
                                        Toast.makeText(
                                                MainActivity.this,
                                                "Erro ao carregar anúncios da whitelist",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Ads>> call, Throwable t) {
                            Log.e(TAG, "❌ Falha na requisição getAdsWhitelist", t);

                            runOnUiThread(() -> {
                                Toast.makeText(
                                        MainActivity.this,
                                        "Falha de conexão ao carregar whitelist",
                                        Toast.LENGTH_SHORT
                                ).show();
                            });
                        }
                    });



=======
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31
                    // Buscar mensagens de cada local
                    for (Local local : locais) {
                        apiService.getMessagesByLocation(local.getId())
                                .enqueue(new retrofit2.Callback<List<Ads>>() {
                                    @Override
                                    public void onResponse(Call<List<Ads>> call, Response<List<Ads>> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            List<Ads> mensagensDoLocal = response.body();
                                            anunciosFiltrados.addAll(mensagensDoLocal);
                                            Log.d(TAG, "✅ Mensagens do local " + local.getNome() +
                                                    " (" + local.getId() + "): " + mensagensDoLocal.size());
                                        } else {
                                            Log.w(TAG, "⚠️ Erro ao buscar mensagens do local " +
                                                    local.getNome() + ": " + response.code());
                                        }

                                        locaisProcessados[0]++;

                                        // Se processou todos os locais, atualizar UI
                                        if (locaisProcessados[0] == totalLocais) {
                                            Log.d(TAG, "✅ Total de anúncios carregados: " +
                                                    anunciosFiltrados.size());
                                            Log.d(TAG, "=========================================");

                                            runOnUiThread(() -> {
                                                updateAdsUI();
                                                Toast.makeText(MainActivity.this,
                                                        anunciosFiltrados.size() + " anúncios carregados",
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<List<Ads>> call, Throwable t) {
                                        Log.e(TAG, "❌ Erro ao buscar mensagens do local " +
                                                local.getNome() + ": " + t.getMessage());

                                        locaisProcessados[0]++;

                                        // Se processou todos os locais (mesmo com erros), atualizar UI
                                        if (locaisProcessados[0] == totalLocais) {
                                            Log.d(TAG, "Total de anúncios carregados (com erros): " +
                                                    anunciosFiltrados.size());
                                            Log.d(TAG, "=========================================");

                                            runOnUiThread(() -> updateAdsUI());
                                        }
                                    }
                                });
                    }
                } else {
                    Log.e(TAG, "❌ Erro ao buscar locais: " + response.code());
                    Log.d(TAG, "=========================================");

                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                "Erro ao buscar locais",
                                Toast.LENGTH_SHORT).show();
                        updateAdsUI();
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Local>> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao buscar locais: " + t.getMessage());
                Log.d(TAG, "=========================================");

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Erro de conexão: " + t.getMessage(),
                            Toast.LENGTH_LONG).show();
                    updateAdsUI();
                });
            }
        });
    }


    /**
     * Atualiza a UI dos anúncios
     */
    private void updateAdsUI() {
        // Atualizar contador
        txtTotalAnuncios.setText(String.valueOf(anunciosFiltrados.size()));

        // Atualizar adapter
        if (anunciosAdapter != null) {
            anunciosAdapter.updateData(anunciosFiltrados);
        }

        // Mostrar/ocultar empty state
        if (anunciosFiltrados.isEmpty()) {
            recyclerViewAnuncios.setVisibility(View.GONE);
            emptyStateCard.setVisibility(View.VISIBLE);
        } else {
            recyclerViewAnuncios.setVisibility(View.VISIBLE);
            emptyStateCard.setVisibility(View.GONE);
        }
    }

    /**
     * Verifica se o usuário pode ver a mensagem baseado nas regras
     */
    private boolean podeVerMensagem(Map<String, String> perfilUsuario,
                                    Map<String, String> restricao,
                                    boolean isBlacklist) {
        if (restricao == null || restricao.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, String> regra : restricao.entrySet()) {
            String chave = regra.getKey();
            String valor = regra.getValue();

            if (isBlacklist) {
                if (perfilUsuario.containsKey(chave) &&
                        perfilUsuario.get(chave).equalsIgnoreCase(valor)) {
                    return false;
                }
            } else {
                if (!perfilUsuario.containsKey(chave) ||
                        !perfilUsuario.get(chave).equalsIgnoreCase(valor)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Callback quando o mapa está pronto
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.d("MainActivity", "===== MAPA PRONTO =====");

        // Verificar permissões de localização
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.w("MainActivity", "⚠️ Permissões não concedidas, solicitando...");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        Log.d("MainActivity", "✅ Permissões concedidas");

        // Habilitar localização no mapa
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Obter última localização conhecida
        Log.d("MainActivity", "Obtendo última localização conhecida...");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Log.d("MainActivity", "✅ Última localização obtida: Lat=" +
                                location.getLatitude() + ", Lng=" + location.getLongitude());

                        LatLng currentLocation = new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        );

                        mMap.addMarker(new MarkerOptions()
                                .position(currentLocation)
                                .title("Minha localização"));

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                        locActual.setText("Lat: " + String.format("%.4f", location.getLatitude()) +
                                ", Lng: " + String.format("%.4f", location.getLongitude()));

                    } else {
                        Log.w("MainActivity", "⚠️ Última localização é null, tentando localização em tempo real...");
                        // Se getLastLocation retornar null, solicitar atualizações
                        requestCurrentLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "❌ Erro ao obter localização: " + e.getMessage());
                    useDefaultLocation();
                });
    }

    /**
     * Callback de resultado de permissões
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    onMapReady(mMap);
                }
            } else {
                Toast.makeText(this,
                        "Permissão de localização negada",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar anúncios quando voltar para a activity
        loadAds();
    }

    /**
     * Solicita a localização atual em tempo real (como no AddLocal)
     */
    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Log.d("MainActivity", "Solicitando localização em tempo real...");

        // Criar LocationRequest
        com.google.android.gms.location.LocationRequest locationRequest =
                new com.google.android.gms.location.LocationRequest.Builder(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        5000 // 5 segundos
                ).build();

        // LocationCallback
        com.google.android.gms.location.LocationCallback locationCallback =
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull com.google.android.gms.location.LocationResult locationResult) {
                        super.onLocationResult(locationResult);

                        if (locationResult.getLastLocation() != null) {
                            android.location.Location location = locationResult.getLastLocation();

                            Log.d("MainActivity", "✅ Localização em tempo real obtida: Lat=" +
                                    location.getLatitude() + ", Lng=" + location.getLongitude());

                            LatLng currentLocation = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );

                            // Limpar marcadores antigos e adicionar novo
                            if (mMap != null) {
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions()
                                        .position(currentLocation)
                                        .title("Minha localização"));

                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                                locActual.setText("Lat: " + String.format("%.4f", location.getLatitude()) +
                                        ", Lng: " + String.format("%.4f", location.getLongitude()));
                            }

                            // Parar atualizações após obter a primeira localização
                            fusedLocationClient.removeLocationUpdates(this);
                        }
                    }
                };

        // Solicitar atualizações
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                android.os.Looper.getMainLooper()
        );
    }

    /**
     * Usa localização padrão (Luanda) se não conseguir obter GPS
     */
    private void useDefaultLocation() {
        Log.w("MainActivity", "⚠️ Usando localização padrão (Luanda)");

        LatLng defaultLocation = new LatLng(-8.838333, 13.234444);

        if (mMap != null) {
            mMap.addMarker(new MarkerOptions()
                    .position(defaultLocation)
                    .title("Luanda (localização padrão)"));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
        }

        locActual.setText("Localização padrão: Luanda");

        Toast.makeText(this,
                "Não foi possível obter sua localização. Usando Luanda como padrão.",
                Toast.LENGTH_LONG).show();
    }
<<<<<<< HEAD

    private void updateUI() {

        // Segurança: evitar crash se algo não estiver pronto
        if (recyclerViewAnuncios == null ||
                txtTotalAnuncios == null ||
                emptyStateCard == null ||
                anunciosAdapter == null) {

            Log.e(TAG, "❌ updateUI(): views ou adapter não inicializados");
            return;
        }

        // Atualizar adapter com a lista atual
        anunciosAdapter.updateData(anunciosFiltrados);

        // Atualizar contador
        txtTotalAnuncios.setText(anunciosFiltrados.size() + " anúncios");

        // Mostrar / ocultar empty state
        if (anunciosFiltrados.isEmpty()) {
            recyclerViewAnuncios.setVisibility(View.GONE);
            emptyStateCard.setVisibility(View.VISIBLE);
        } else {
            recyclerViewAnuncios.setVisibility(View.VISIBLE);
            emptyStateCard.setVisibility(View.GONE);
        }

        Log.d(TAG, "✅ UI atualizada com " + anunciosFiltrados.size() + " anúncios");
    }

=======
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31
}