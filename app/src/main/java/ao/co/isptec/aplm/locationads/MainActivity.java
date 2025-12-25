package ao.co.isptec.aplm.locationads;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    // Views
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;
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
    private Map<String, String> perfilUsuario = new HashMap<>();
    private ApiService apiService;
    private List<AdMessage> anunciosFiltrados = new ArrayList<>();

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
        emptyStateCard = findViewById(R.id.emptyStateCard);
    }

    /**
     * Configura os RecyclerViews
     */
    private void setupRecyclerViews() {
        // RecyclerView de Locais
        listaLocais.setLayoutManager(new LinearLayoutManager(this));
        locaisAdapter = new LocaisAdapter(new ArrayList<>());
        listaLocais.setAdapter(locaisAdapter);

        // RecyclerView de Anúncios
        recyclerViewAnuncios.setLayoutManager(new LinearLayoutManager(this));
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
     * Carrega e filtra os anúncios
     */
    private void loadAds() {
        List<AdMessage> todasMensagens = AdMessage.carregarMensagens();
        anunciosFiltrados.clear();

        for (AdMessage msg : todasMensagens) {
            boolean permitido = podeVerMensagem(perfilUsuario, msg.getWhitelist(), false) &&
                    podeVerMensagem(perfilUsuario, msg.getBlacklist(), true);

            if (permitido) {
                anunciosFiltrados.add(msg);
            }
        }

        // Atualizar UI
        updateAdsUI();
    }

    /**
     * Atualiza a UI dos anúncios
     */
    private void updateAdsUI() {
        // Atualizar contador
        txtTotalAnuncios.setText(String.valueOf(anunciosFiltrados.size()));

        // Atualizar adapter
        anunciosAdapter.notifyDataSetChanged();

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

        // Verificar permissões de localização
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // Habilitar localização no mapa
        mMap.setMyLocationEnabled(true);

        // Obter última localização conhecida
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
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
                        LatLng defaultLocation = new LatLng(-8.838333, 13.234444);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12));
                        locActual.setText("Localização não disponível");
                    }
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
}