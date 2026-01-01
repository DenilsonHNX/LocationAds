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
import retrofit2.Call;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity";

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
    private ApiService apiService;
    private List<Ads> anunciosFiltrados;
    private UserProfile perfilUsuario;

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

        // Carregar perfil do usuário
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
        anunciosFiltrados = new ArrayList<>();
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
     * Carrega o perfil do usuário do ProfileManager
     */
    private void loadUserProfile() {
        perfilUsuario = ProfileManager.getInstance(this).getCurrentProfile();

        if (perfilUsuario != null && perfilUsuario.getProperties() != null) {
            Log.d(TAG, "========== PERFIL DO USUÁRIO ==========");
            Log.d(TAG, "User ID: " + perfilUsuario.getUserId());
            Log.d(TAG, "Propriedades: " + perfilUsuario.getProperties().size());

            for (Map.Entry<String, String> entry : perfilUsuario.getProperties().entrySet()) {
                Log.d(TAG, "  " + entry.getKey() + " = " + entry.getValue());
            }

            Log.d(TAG, "=====================================");
        } else {
            Log.w(TAG, "⚠️ Usuário sem perfil definido");
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
     * Carrega e filtra os anúncios de todos os locais baseado no perfil do usuário
     */
    private void loadAds() {
        Log.d(TAG, "========== CARREGANDO ANÚNCIOS ==========");

        // Verificar se o perfil está carregado
        if (perfilUsuario == null) {
            Log.w(TAG, "⚠️ Tentando recarregar perfil do usuário...");
            loadUserProfile();
        }

        logPerfilUsuario();

        // Buscar todos os locais
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
                    final int[] totalAnunciosRecebidos = {0};
                    final int[] totalAnunciosPermitidos = {0};
                    final int[] totalAnunciosBloqueados = {0};

                    // Buscar mensagens de cada local
                    for (Local local : locais) {
                        apiService.getMessagesByLocation(local.getId())
                                .enqueue(new retrofit2.Callback<List<Ads>>() {
                                    @Override
                                    public void onResponse(Call<List<Ads>> call, Response<List<Ads>> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            List<Ads> mensagensDoLocal = response.body();
                                            totalAnunciosRecebidos[0] += mensagensDoLocal.size();

                                            Log.d(TAG, "");
                                            Log.d(TAG, "📍 Local: " + local.getNome() + " (ID: " + local.getId() + ")");
                                            Log.d(TAG, "📨 Total de anúncios: " + mensagensDoLocal.size());
                                            Log.d(TAG, "─────────────────────────────────────");

                                            // Aplicar filtro de WHITELIST/BLACKLIST
                                            for (Ads anuncio : mensagensDoLocal) {
                                                boolean podeVer = podeVerAnuncio(anuncio);

                                                if (podeVer) {
                                                    anunciosFiltrados.add(anuncio);
                                                    totalAnunciosPermitidos[0]++;
                                                } else {
                                                    totalAnunciosBloqueados[0]++;
                                                }
                                            }
                                        } else {
                                            Log.w(TAG, "⚠️ Erro ao buscar mensagens do local " +
                                                    local.getNome() + ": " + response.code());
                                        }

                                        locaisProcessados[0]++;

                                        // Se processou todos os locais, atualizar UI
                                        if (locaisProcessados[0] == totalLocais) {
                                            Log.d(TAG, "");
                                            Log.d(TAG, "========== RESUMO DA FILTRAGEM ==========");
                                            Log.d(TAG, "📊 Total de anúncios recebidos: " + totalAnunciosRecebidos[0]);
                                            Log.d(TAG, "✅ Anúncios PERMITIDOS: " + totalAnunciosPermitidos[0]);
                                            Log.d(TAG, "❌ Anúncios BLOQUEADOS: " + totalAnunciosBloqueados[0]);
                                            Log.d(TAG, "=========================================");

                                            runOnUiThread(() -> {
                                                updateAdsUI();

                                                String mensagem = totalAnunciosPermitidos[0] + " anúncios disponíveis para você";
                                                if (totalAnunciosBloqueados[0] > 0) {
                                                    mensagem += " (" + totalAnunciosBloqueados[0] + " bloqueados)";
                                                }

                                                Toast.makeText(MainActivity.this,
                                                        mensagem,
                                                        Toast.LENGTH_LONG).show();
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<List<Ads>> call, Throwable t) {
                                        Log.e(TAG, "❌ Erro ao buscar mensagens do local " +
                                                local.getNome() + ": " + t.getMessage());

                                        locaisProcessados[0]++;

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
     * Log detalhado do perfil do usuário
     */
    private void logPerfilUsuario() {
        if (perfilUsuario != null && perfilUsuario.getProperties() != null &&
                !perfilUsuario.getProperties().isEmpty()) {
            Log.d(TAG, "👤 Perfil do Usuário:");
            for (Map.Entry<String, String> entry : perfilUsuario.getProperties().entrySet()) {
                Log.d(TAG, "   " + entry.getKey() + " = \"" + entry.getValue() + "\"");
            }
        } else {
            Log.w(TAG, "⚠️ Usuário SEM perfil definido (verá apenas anúncios sem restrições)");
        }
        Log.d(TAG, "");
    }

    /**
     * Verifica se o usuário pode ver o anúncio baseado na política WHITELIST/BLACKLIST
     *
     * WHITELIST: Apenas quem corresponde às restrições pode ver
     * BLACKLIST: Todos podem ver, EXCETO quem corresponde às restrições
     */
    private boolean podeVerAnuncio(Ads anuncio) {
        String policy = anuncio.getPolicy();
        Map<String, Object> restricoes = anuncio.getRestricoes();

        Log.d(TAG, "");
        Log.d(TAG, "🔍 Anúncio: \"" + anuncio.getTitulo() + "\"");
        Log.d(TAG, "   Policy: " + policy);

        if (restricoes != null && !restricoes.isEmpty()) {
            Log.d(TAG, "   Restrições:");
            for (Map.Entry<String, Object> entry : restricoes.entrySet()) {
                Log.d(TAG, "     • " + entry.getKey() + " = " + entry.getValue());
            }
        } else {
            Log.d(TAG, "   Restrições: NENHUMA");
        }

        // Caso 1: Anúncio SEM restrições
        if (restricoes == null || restricoes.isEmpty()) {
            if ("WHITELIST".equalsIgnoreCase(policy)) {
                // WHITELIST vazia = ninguém pode ver
                Log.d(TAG, "   Resultado: ❌ BLOQUEADO (WHITELIST vazia - ninguém autorizado)");
                return false;
            } else {
                // BLACKLIST vazia = todos podem ver
                Log.d(TAG, "   Resultado: ✅ PERMITIDO (BLACKLIST vazia - todos autorizados)");
                return true;
            }
        }

        // Caso 2: Anúncio COM restrições
        boolean perfilCorresponde = verificaCorrespondenciaPerfil(restricoes);

        Log.d(TAG, "   Perfil corresponde às restrições: " + (perfilCorresponde ? "SIM" : "NÃO"));

        // Aplicar lógica da política
        boolean resultado;
        if ("WHITELIST".equalsIgnoreCase(policy)) {
            // WHITELIST: Só pode ver quem corresponde
            resultado = perfilCorresponde;
            Log.d(TAG, "   Resultado: " + (resultado ? "✅ PERMITIDO" : "❌ BLOQUEADO") +
                    " (WHITELIST - apenas quem corresponde)");
        } else {
            // BLACKLIST: Pode ver quem NÃO corresponde
            resultado = !perfilCorresponde;
            Log.d(TAG, "   Resultado: " + (resultado ? "✅ PERMITIDO" : "❌ BLOQUEADO") +
                    " (BLACKLIST - bloqueado quem corresponde)");
        }

        return resultado;
    }

    /**
     * Verifica se o perfil do usuário corresponde às restrições do anúncio
     * Retorna TRUE se o perfil corresponde a TODAS as restrições
     */
    private boolean verificaCorrespondenciaPerfil(Map<String, Object> restricoes) {
        // Se o usuário não tem perfil definido
        if (perfilUsuario == null || perfilUsuario.getProperties() == null ||
                perfilUsuario.getProperties().isEmpty()) {
            Log.d(TAG, "     ⚠️ Usuário sem perfil definido");
            return false;
        }

        Map<String, String> perfilMap = perfilUsuario.getProperties();

        // Verificar cada restrição
        for (Map.Entry<String, Object> restricao : restricoes.entrySet()) {
            String chave = restricao.getKey();
            String valorEsperado = String.valueOf(restricao.getValue());

            Log.d(TAG, "     Verificando: " + chave);

            // Verificar se o usuário tem essa propriedade no perfil
            if (!perfilMap.containsKey(chave)) {
                Log.d(TAG, "       ❌ Usuário NÃO tem a propriedade \"" + chave + "\"");
                return false; // Falta uma propriedade obrigatória
            }

            String valorUsuario = perfilMap.get(chave);

            // Tratamento especial para idade (idadeMinima)
            if (chave.equalsIgnoreCase("idadeMinima") || chave.equalsIgnoreCase("idade")) {
                boolean idadeOk = compararIdade(chave, valorUsuario, valorEsperado, perfilMap);
                if (!idadeOk) {
                    return false;
                }
            } else {
                // Comparação normal (case-insensitive)
                boolean corresponde = valorUsuario.trim().equalsIgnoreCase(valorEsperado.trim());

                Log.d(TAG, "       Usuário: \"" + valorUsuario + "\" vs Esperado: \"" + valorEsperado + "\"");
                Log.d(TAG, "       " + (corresponde ? "✅ CORRESPONDE" : "❌ NÃO CORRESPONDE"));

                if (!corresponde) {
                    return false; // Uma propriedade não corresponde
                }
            }
        }

        // Se chegou aqui, todas as restrições foram satisfeitas
        Log.d(TAG, "     ✅ TODAS as restrições foram satisfeitas");
        return true;
    }

    /**
     * Compara idade do usuário com restrição de idade mínima
     */
    private boolean compararIdade(String chave, String valorUsuario, String valorEsperado,
                                  Map<String, String> perfilMap) {
        try {
            int idadeMinima = Integer.parseInt(valorEsperado);

            // Se a restrição é "idadeMinima", precisamos buscar "idade" no perfil
            String idadeStr;
            if (chave.equalsIgnoreCase("idadeMinima")) {
                if (!perfilMap.containsKey("idade")) {
                    Log.d(TAG, "       ❌ Usuário NÃO tem \"idade\" no perfil");
                    return false;
                }
                idadeStr = perfilMap.get("idade");
            } else {
                idadeStr = valorUsuario;
            }

            int idadeUsuario = Integer.parseInt(idadeStr);

            boolean idadeOk = idadeUsuario >= idadeMinima;

            Log.d(TAG, "       Idade do usuário: " + idadeUsuario + " anos");
            Log.d(TAG, "       Idade mínima: " + idadeMinima + " anos");
            Log.d(TAG, "       " + (idadeOk ? "✅ IDADE SUFICIENTE" : "❌ IDADE INSUFICIENTE"));

            return idadeOk;

        } catch (NumberFormatException e) {
            Log.e(TAG, "       ❌ Erro ao comparar idades: " + e.getMessage());
            return false;
        }
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
    protected void onResume() {
        super.onResume();
        // Recarregar perfil e anúncios quando voltar para a activity
        loadUserProfile();
        loadAds();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.d(TAG, "===== MAPA PRONTO =====");

        // Verificar permissões de localização
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.w(TAG, "⚠️ Permissões não concedidas, solicitando...");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        Log.d(TAG, "✅ Permissões concedidas");

        // Habilitar localização no mapa
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Obter última localização conhecida
        Log.d(TAG, "Obtendo última localização conhecida...");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Log.d(TAG, "✅ Última localização obtida: Lat=" +
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
                        Log.w(TAG, "⚠️ Última localização é null, usando localização padrão...");
                        useDefaultLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Erro ao obter localização: " + e.getMessage());
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

    /**
     * Usa localização padrão (Luanda) se não conseguir obter GPS
     */
    private void useDefaultLocation() {
        Log.w(TAG, "⚠️ Usando localização padrão (Luanda)");

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
}