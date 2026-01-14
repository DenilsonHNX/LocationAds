package ao.co.isptec.aplm.locationads;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import ao.co.isptec.aplm.locationads.network.models.UserProfile;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import ao.co.isptec.aplm.locationads.network.singleton.ProfileManager;
import ao.co.isptec.aplm.locationads.network.singleton.TokenManager;
import ao.co.isptec.aplm.locationads.services.LocationTracker;
import ao.co.isptec.aplm.locationads.service.NotificationManager;
import ao.co.isptec.aplm.locationads.utils.MessageDeliveryManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.gson.Gson;

import android.location.Location;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";

    // Views
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap mMap;

    private List<Ads> anunciosWhitelist = new ArrayList<>();
    private List<Ads> anunciosCriados = new ArrayList<>();

    private AnunciosAdapter adapterWhitelist;
    private AnunciosAdapter adapterCriados;

    private TextView emptyStateText;
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
    private List<Ads> anunciosFiltrados = new ArrayList<>();
    private Map<String, String> perfilUsuario = new HashMap<>();

    private MessageDeliveryManager deliveryManager;
    private ProfileManager profileManager;
    private LocationTracker locationTracker;
    private Integer currentDetectedLocalId = null;
    private int currentLocalId = -1;
    private boolean isLoadingAds = false;
    private int meuUserId = -1;
    private UserProfile currentUserProfile;

    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        Log.d(TAG, "🚀 ========== MAINACTIVITY INICIADO ==========");

        // Inicializar API e Location
        apiService = ApiClient.getInstance(this).getApiService();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // ✅ Inicializar sistema de políticas
        deliveryManager = MessageDeliveryManager.getInstance(this);

        // ✅ Inicializar ProfileManager e LocationTracker
        profileManager = ProfileManager.getInstance(this);
        locationTracker = new LocationTracker(this);

        // ✅ Configurar listener do LocationTracker
        setupLocationTracker();

        // Inicializar views
        initViews();

        // Configurar listeners
        setupListeners();

        // Configurar mapa
        setupMap();

        // Configurar RecyclerViews
        setupRecyclerViews();

        // Carregar anúncios similares
        loadSimilarAds();

        // ✅ Carregar locais e iniciar rastreamento
        loadLocalsAndStartTracking();

    }

    /**
     * Configura o LocationTracker com callbacks
     */
    private void setupLocationTracker() {
        locationTracker.setLocationChangeListener(new LocationTracker.LocationChangeListener() {
            @Override
            public void onEnteredLocal(Local local, Location location) {
                Log.d(TAG, "========================================");
                Log.d(TAG, "🎯 ENTROU NO LOCAL: " + local.getNome());
                Log.d(TAG, "   ID: " + local.getId());
                Log.d(TAG, "   Lat: " + location.getLatitude());
                Log.d(TAG, "   Lng: " + location.getLongitude());
                Log.d(TAG, "========================================");

                currentDetectedLocalId = local.getId();
                currentLocalId = local.getId(); // Atualizar também o antigo

                // Atualizar UI
                runOnUiThread(() -> {
                    locActual.setText(local.getNome());
                    Toast.makeText(MainActivity.this,
                            "📍 Local detectado: " + local.getNome(),
                            Toast.LENGTH_SHORT).show();
                });

                // Carregar anúncios do local
                loadAdsForCurrentLocal();
            }

            @Override
            public void onExitedLocal(int localId) {
                Log.d(TAG, "🚪 Saiu do local ID: " + localId);

                currentDetectedLocalId = null;
                currentLocalId = -1;

                runOnUiThread(() -> {
                    locActual.setText("Fora de locais cadastrados");

                    // Limpar anúncios quando sair do local
                    anunciosFiltrados.clear();
                    updateAdsUI();

                    Toast.makeText(MainActivity.this,
                            "Você saiu do local",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onLocationUpdated(Location location) {
                // Atualização silenciosa de localização
                runOnUiThread(() -> {
                    locActual.setText("Lat: " + String.format("%.4f", location.getLatitude()) +
                            ", Lng: " + String.format("%.4f", location.getLongitude()));
                });
            }

            @Override
            public void onLocationError(String error) {
                Log.e(TAG, "❌ Erro de localização: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this,
                            "Erro ao obter localização",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Carrega todos os locais e inicia rastreamento
     */
    private void loadLocalsAndStartTracking() {
        Log.d(TAG, "🔄 Carregando locais...");

        apiService.getAllLocals().enqueue(new retrofit2.Callback<List<Local>>() {
            @Override
            public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Local> locais = response.body();
                    Log.d(TAG, "✅ " + locais.size() + " locais carregados");

                    // Passar locais para o LocationTracker
                    locationTracker.setAllLocals(locais);

                    // Iniciar rastreamento
                    locationTracker.startTracking();

                    Log.d(TAG, "🚀 Rastreamento de localização iniciado (polling: 15s)");

                } else {
                    Log.e(TAG, "❌ Erro ao carregar locais: " + response.code());
                    Toast.makeText(MainActivity.this,
                            "Erro ao carregar locais",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Local>> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao carregar locais", t);
                Toast.makeText(MainActivity.this,
                        "Erro de conexão: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Cria perfil de teste para filtro de políticas
     */
    private void createTestProfile() {
        currentUserProfile = new UserProfile();
        Map<String, Object> testProfile = new HashMap<>();
        testProfile.put("idadeMinima", 25); // Maior que as restrições das mensagens
        testProfile.put("Profissao", "Estudante");
        currentUserProfile.setProfile(testProfile);
        Log.d(TAG, "✅ Perfil de teste criado: " + testProfile);
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
        emptyStateText = findViewById(R.id.emptyStateText);
        emptyStateCard = findViewById(R.id.emptyStateCard);
    }

    /**
     * Configura os RecyclerViews
     */
    private void setupRecyclerViews() {

        adapterWhitelist = new AnunciosAdapter(this, anunciosWhitelist);
        adapterCriados = new AnunciosAdapter(this, anunciosCriados);

// Usa o adapter da whitelist por default
        recyclerViewAnuncios.setAdapter(adapterWhitelist);

        // RecyclerView de Locais - com listener para click e delete
        listaLocais.setLayoutManager(new LinearLayoutManager(this));
        locaisAdapter = new LocaisAdapter(this, new ArrayList<>(), new LocaisAdapter.OnLocalClickListener() {
            @Override
            public void onLocalClick(Local local) {
                // Ir para o local no mapa
                if (local.getLatitude() != null && local.getLongitude() != null) {
                    LatLng posicao = new LatLng(local.getLatitude(), local.getLongitude());
                    if (mMap != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicao, 16));
                        showMap();
                    }
                }
            }
            
            @Override
            public void onLocalDeleted(Local local) {
                // Recarregar a lista de locais
                buscarTodosLocais();
            }
        });
        listaLocais.setAdapter(locaisAdapter);

        // RecyclerView de Anúncios
        recyclerViewAnuncios.setLayoutManager(new LinearLayoutManager(this));
        anunciosFiltrados = new ArrayList<>();
        anunciosAdapter = new AnunciosAdapter(this, anunciosFiltrados);

        // ✅ ADICIONAR: Listener para abrir ViewAds ao clicar
        anunciosAdapter.setOnItemClickListener(new AnunciosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Ads ads) {
                // Abrir ViewAds com os dados do anúncio
                openViewAds(ads);
            }

            @Override
            public void onSaveClick(Ads ads, boolean isSaved) {
                // Callback de salvar (já implementado)
            }
        });

        recyclerViewAnuncios.setAdapter(anunciosAdapter);
    }

    private void openViewAds(Ads ads) {
        Intent intent = new Intent(MainActivity.this, ViewAds.class);

        // ✅ OPÇÃO 1: Passar objeto serializado como JSON (RECOMENDADO)
        Gson gson = new Gson();
        String adsJson = gson.toJson(ads);
        intent.putExtra("ads_json", adsJson);

        // ✅ OPÇÃO 2: Passar apenas o ID (caso queira buscar da API)
        // intent.putExtra("ad_id", ads.getId());

        startActivity(intent);

        Log.d(TAG, "🔗 Abrindo ViewAds para: " + ads.getTitulo());
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
     * Carrega anúncios do local atual detectado
     */
    private void loadAdsForCurrentLocal() {
        if (currentDetectedLocalId == null) {
            Log.w(TAG, "⚠️ Nenhum local detectado, não há anúncios para carregar");
            anunciosFiltrados.clear();
            runOnUiThread(() -> updateAdsUI());
            return;
        }

        if (isLoadingAds) {
            Log.d(TAG, "⏳ Já está carregando anúncios...");
            return;
        }

        isLoadingAds = true;

        Log.d(TAG, "========================================");
        Log.d(TAG, "🔄 CARREGANDO ANÚNCIOS DO LOCAL");
        Log.d(TAG, "   Local ID: " + currentDetectedLocalId);
        Log.d(TAG, "========================================");

        // Buscar mensagens do local
        apiService.getMessagesByLocation(currentDetectedLocalId)
                .enqueue(new retrofit2.Callback<List<Ads>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Ads>> call, @NonNull Response<List<Ads>> response) {
                        isLoadingAds = false;

                        if (response.isSuccessful() && response.body() != null) {
                            List<Ads> todasMensagens = response.body();

                            Log.d(TAG, "📨 Mensagens recebidas: " + todasMensagens.size());

                            // Filtrar mensagens de outros usuários
                            List<Ads> mensagensDeOutros = new ArrayList<>();
                            for (Ads ads : todasMensagens) {
                                if (ads.getAutorId() != meuUserId) {
                                    mensagensDeOutros.add(ads);
                                    Log.d(TAG, "  ✅ " + ads.getTitulo() + " (Autor: " + ads.getAutorId() + ")");
                                } else {
                                    Log.d(TAG, "  ⏭️ Ignorada (minha): " + ads.getTitulo());
                                }
                            }

                            Log.d(TAG, "🔍 Mensagens de outros: " + mensagensDeOutros.size());

                            // Filtrar por política
                            runOnUiThread(() -> {
                                filterAndDisplayMessages(mensagensDeOutros);
                            });

                        } else {
                            Log.e(TAG, "❌ Erro ao buscar mensagens: " + response.code());
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this,
                                        "Erro ao buscar anúncios",
                                        Toast.LENGTH_SHORT).show();
                                anunciosFiltrados.clear();
                                updateAdsUI();
                            });
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Ads>> call, @NonNull Throwable t) {
                        isLoadingAds = false;
                        Log.e(TAG, "❌ Falha ao buscar mensagens", t);

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "Erro de conexão: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            anunciosFiltrados.clear();
                            updateAdsUI();
                        });
                    }
                });
    }

    /**
     * Carregar anúncios similares do backend
     */
    private void loadSimilarAds() {
        if (isLoadingAds) {
            Log.d(TAG, "⏳ Já está carregando anúncios similares...");
            return;
        }

        isLoadingAds = true;

        Log.d(TAG, "========================================");
        Log.d(TAG, "🔄 CARREGANDO ANÚNCIOS SIMILARES");
        Log.d(TAG, "========================================");

        // Obter ID do usuário atual
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        final int meuUserId = prefs.getInt("userId", -1);
        Log.d(TAG, "👤 Meu User ID nas preferences: " + meuUserId);

        // Buscar anúncios similares
        apiService.getSimilarMessages()
                .enqueue(new retrofit2.Callback<List<Ads>>() {
                    @Override
                    public void onResponse(Call<List<Ads>> call, Response<List<Ads>> response) {
                        isLoadingAds = false;

                        Log.d(TAG, "📡 RESPOSTA RECEBIDA - Código: " + response.code());
                        Log.d(TAG, "📡 Sucesso? " + response.isSuccessful());
                        Log.d(TAG, "📡 Body é null? " + (response.body() == null));

                        if (response.isSuccessful() && response.body() != null) {
                            List<Ads> todasMensagens = response.body();
                            Log.d(TAG, "✅ DADOS RECEBIDOS: " + todasMensagens.size() + " anúncios");

                            // MOSTRAR TODOS OS ANÚNCIOS RECEBIDOS
                            for (int i = 0; i < todasMensagens.size(); i++) {
                                Ads ads = todasMensagens.get(i);
                                Log.d(TAG, "   📌 [" + i + "] " +
                                        "Título: " + ads.getTitulo() +
                                        " | ID: " + ads.getId() +
                                        " | Autor: " + ads.getAutorId() +
                                        " | É meu? " + (ads.getAutorId() == meuUserId) +
                                        " | Policy: " + ads.getPolicy() +
                                        " | LocalId: " + ads.getLocalId());
                            }

                            // Filtrar anúncios de outros usuários
                            List<Ads> mensagensDeOutros = new ArrayList<>();
                            for (Ads ads : todasMensagens) {
                                if (ads.getAutorId() != meuUserId) {
                                    mensagensDeOutros.add(ads);
                                    Log.d(TAG, "  ✅ ADICIONADO: " + ads.getTitulo() + " (Autor: " + ads.getAutorId() + ")");
                                } else {
                                    Log.d(TAG, "  ⏭️ IGNORADO (meu próprio anúncio): " + ads.getTitulo());
                                }
                            }

                            Log.d(TAG, "🔍 Anúncios similares de outros: " + mensagensDeOutros.size());

                            // Se ainda houver anúncios após filtrar os próprios
                            if (mensagensDeOutros.isEmpty()) {
                                Log.d(TAG, "⚠️ Nenhum anúncio de outros usuários encontrado!");
                                Log.d(TAG, "💡 Possível problema: Todos os anúncios são seus ou userId está errado");
                                Log.d(TAG, "💡 Meu userId: " + meuUserId);

                                // TESTE: Mostrar todos os anúncios sem filtrar
                                runOnUiThread(() -> {
                                    anunciosFiltrados.clear();
                                    anunciosFiltrados.addAll(todasMensagens);
                                    updateAdsUI();
                                    Toast.makeText(MainActivity.this,
                                            "TESTE: Mostrando " + todasMensagens.size() + " anúncios (incluindo meus)",
                                            Toast.LENGTH_LONG).show();
                                });
                            } else {
                                // Filtrar por política
                                runOnUiThread(() -> {
                                    filterAndDisplayMessages(mensagensDeOutros, true);
                                });
                            }

                        } else {
                            Log.e(TAG, "❌ Erro ao buscar anúncios similares: " + response.code());

                            // Mostrar erro da resposta se houver
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "❌ Error Body: " + errorBody);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "❌ Erro ao ler errorBody", e);
                            }

                            runOnUiThread(() -> {
                                anunciosFiltrados.clear();
                                updateAdsUI();

                                Toast.makeText(MainActivity.this,
                                        "Erro " + response.code() + " ao buscar anúncios similares",
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Ads>> call, Throwable t) {
                        isLoadingAds = false;
                        Log.e(TAG, "❌ FALHA NA REQUISIÇÃO: " + t.getMessage(), t);

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this,
                                    "Falha de conexão: " + t.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            anunciosFiltrados.clear();
                            updateAdsUI();
                        });
                    }
                });
    }

    /**
     * Filtra mensagens usando o sistema de políticas
     */
    private void filterAndDisplayMessages(List<Ads> allMessages) {
        filterAndDisplayMessages(allMessages, false);
    }

    /**
     * Filtra mensagens usando o sistema de políticas
     */
    private void filterAndDisplayMessages(List<Ads> allMessages, boolean skipLocationCheck) {

        // Adicione no início do filterAndDisplayMessages:
        UserProfile userProfile = profileManager.getCurrentProfile();
        Log.d(TAG, "📝 Perfil carregado: " + (userProfile != null));
        if (userProfile != null) {
            Log.d(TAG, "📝 Propriedades: " + userProfile.getProperties());
            Log.d(TAG, "📝 Tamanho: " + userProfile.getProperties().size());
        }
        Log.d(TAG, "🎯 ========== FILTRANDO MENSAGENS ==========");
        Log.d(TAG, "📦 Total recebido: " + allMessages.size());

        if (allMessages.isEmpty()) {
            Log.d(TAG, "⚠️ LISTA VAZIA - nada para filtrar");
            anunciosFiltrados.clear();
            updateAdsUI();
            return;
        }

        // Obter meu userId
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        int meuUserId = prefs.getInt("userId", -1);
        Log.d(TAG, "👤 Meu User ID: " + meuUserId);

        // 1. Filtrar mensagens de outros usuários
        List<Ads> mensagensDeOutros = new ArrayList<>();
        for (Ads ads : allMessages) {
            boolean eMeu = (ads.getAutorId() == meuUserId);
            Log.d(TAG, "   👥 " + ads.getTitulo() +
                    " | Autor: " + ads.getAutorId() +
                    " | É meu? " + eMeu);

            if (!eMeu) {
                mensagensDeOutros.add(ads);
            }
        }

        Log.d(TAG, "📋 Após filtrar meus: " + mensagensDeOutros.size() + " anúncios");

        // Mostrar quais mensagens passaram
        for (Ads msg : mensagensDeOutros) {
            Log.d(TAG, "  ✅ Visível: " + msg.getTitulo() +
                    " (Policy: " + msg.getPolicy() + ")");
        }

        // Atualizar lista
        anunciosFiltrados.clear();
        anunciosFiltrados.addAll(mensagensDeOutros);

        Log.d(TAG, "=========================================");

        // Atualizar UI
        updateAdsUI();

        Toast.makeText(this,
                mensagensDeOutros.size() + " anúncios disponíveis",
                Toast.LENGTH_SHORT).show();
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("MainActivity", "===== MAPA PRONTO =====");

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

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

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
                        Log.w("MainActivity", "⚠️ Última localização é null");
                        requestCurrentLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "❌ Erro ao obter localização: " + e.getMessage());
                    useDefaultLocation();
                });
    }

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
        // Carregar anúncios quando voltar para a activity
        loadAds();
        
        // Iniciar serviço de rastreamento de localização se tiver permissão
        startLocationTrackingIfPermitted();
        
        // Iniciar sistema de notificações (FCM + fallback polling)
        initializeNotifications();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // O NotificationManager gerencia FCM e polling automaticamente
        // FCM continua em background, polling para quando app não está visível
        
        // Se quiser economizar bateria, pode parar o tracking aqui se não estiver no local
        // Mas a lógica do professor pede rastreamento constante
    }
    
    /**
     * Alias para carregar anúncios (carrega ambos: do local e similares)
     */
    private void loadAds() {
        loadAdsForCurrentLocal();
        loadSimilarAds();
    }
    
    /**
     * Inicializa o sistema de notificações (FCM com fallback para polling)
     */


    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Log.d("MainActivity", "Solicitando localização em tempo real...");

        com.google.android.gms.location.LocationRequest locationRequest =
                new com.google.android.gms.location.LocationRequest.Builder(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        5000
                ).build();

        com.google.android.gms.location.LocationCallback locationCallback =
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull com.google.android.gms.location.LocationResult locationResult) {
                        super.onLocationResult(locationResult);

                        if (locationResult.getLastLocation() != null) {
                            android.location.Location location = locationResult.getLastLocation();

                            Log.d("MainActivity", "✅ Localização em tempo real obtida");

                            LatLng currentLocation = new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            );

                            if (mMap != null) {
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions()
                                        .position(currentLocation)
                                        .title("Minha localização"));

                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                                locActual.setText("Lat: " + String.format("%.4f", location.getLatitude()) +
                                        ", Lng: " + String.format("%.4f", location.getLongitude()));
                            }

                            fusedLocationClient.removeLocationUpdates(this);
                        }
                    }
                };

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                android.os.Looper.getMainLooper()
        );
    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Parar rastreamento ao destruir activity
        if (locationTracker != null) {
            locationTracker.stopTracking();
            Log.d(TAG, "🛑 Rastreamento parado (Activity destruída)");
        }
    }

    /**
     * Inicia o rastreamento de localização se as permissões estiverem concedidas
     */
    private void startLocationTrackingIfPermitted() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationTracker != null) {
                locationTracker.startTracking();
            }
        }
    }

    /**
     * Inicializa o sistema de notificações (FCM + Fallback)
     */
    private void initializeNotifications() {
        NotificationManager.getInstance(this).initialize();
    }

}