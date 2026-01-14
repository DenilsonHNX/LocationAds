package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ao.co.isptec.aplm.locationads.adapter.AnunciosAdapter;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.models.SavedAd;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListMenu extends AppCompatActivity {

    private static final String TAG = "ListMenu";

    private TabLayout tabLayout;
    private RecyclerView recyclerViewAnuncios;
    private MaterialCardView emptyStateCard;
    private TextView txtTotalAnuncios;
    private TextView emptyStateText;
    private ImageButton btnVoltar;
    private FloatingActionButton fabHome;

    private AnunciosAdapter adapterGuardados;
    private AnunciosAdapter adapterCriados;

    private List<Ads> anunciosGuardados;
    private List<Ads> anunciosCriados;
    private ApiService apiService;
    private Set<Integer> savedAdsIds;

    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_menu);


        // Inicializar API
        apiService = ApiClient.getInstance(this).getApiService();
        initViews();
        initData();
        setupListeners();
        loadData();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        recyclerViewAnuncios = findViewById(R.id.recyclerViewAnuncios);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        txtTotalAnuncios = findViewById(R.id.txtTotalAnuncios);
        emptyStateText = findViewById(R.id.emptyStateText);
        btnVoltar = findViewById(R.id.btnVoltar);

        recyclerViewAnuncios.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData() {
        anunciosGuardados = new ArrayList<>();
        anunciosCriados = new ArrayList<>();
        savedAdsIds = new HashSet<>();

        adapterGuardados = new AnunciosAdapter(this, anunciosGuardados);
        adapterCriados = new AnunciosAdapter(this, anunciosCriados);

        // ✅ ATUALIZADO: Listener para Guardados - Abre ViewAds
        adapterGuardados.setOnItemClickListener(new AnunciosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Ads ads) {
                // ✅ Abrir ViewAds em vez de Toast
                openViewAds(ads);
            }

            @Override
            public void onSaveClick(Ads ads, boolean isSaved) {
                Log.d(TAG, "🔔 Callback guardados - isSaved: " + isSaved);
                if (!isSaved) {
                    // Recarregar lista ao remover dos guardados
                    loadAnunciosGuardados();
                }
            }
        });

        // ✅ ATUALIZADO: Listener para Criados - Abre ViewAds
        adapterCriados.setOnItemClickListener(new AnunciosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Ads ads) {
                // ✅ Abrir ViewAds em vez de Toast
                openViewAds(ads);
            }

            @Override
            public void onSaveClick(Ads ads, boolean isSaved) {
                if (isSaved) {
                    savedAdsIds.add(ads.getId());
                    Toast.makeText(ListMenu.this,
                            "Anúncio salvo!",
                            Toast.LENGTH_SHORT).show();
                } else {
                    savedAdsIds.remove(ads.getId());
                    Toast.makeText(ListMenu.this,
                            "Anúncio removido dos salvos",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        recyclerViewAnuncios.setAdapter(adapterGuardados);
    }

    private void setupListeners() {
        // Botão voltar
        btnVoltar.setOnClickListener(v -> {
            Intent intent = new Intent(ListMenu.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                if (currentTab == 0) {
                    showGuardados();
                } else {
                    showCriados();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // ✅ NOVO MÉTODO: Abre ViewAds com dados do anúncio
    /**
     * Abre ViewAds com dados completos do anúncio
     */
    private void openViewAds(Ads ads) {
        if (ads == null) {
            Toast.makeText(this, "Erro ao abrir anúncio", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "🔗 Abrindo ViewAds para: " + ads.getTitulo());

        Intent intent = new Intent(ListMenu.this, ViewAds.class);

        // ✅ Passar objeto serializado como JSON
        Gson gson = new Gson();
        String adsJson = gson.toJson(ads);
        intent.putExtra("ads_json", adsJson);

        // ✅ Também passar se está salvo
        boolean isSaved = savedAdsIds.contains(ads.getId());
        intent.putExtra("is_saved", isSaved);

        startActivity(intent);
    }

    private void loadData() {
        Log.d(TAG, "========== CARREGANDO DADOS ==========");
        loadAnunciosGuardados();
        loadAnunciosCriados();
    }

    private void loadAnunciosGuardados() {
        Log.d(TAG, "📥 Carregando anúncios guardados...");

        apiService.getSavedMessages().enqueue(new Callback<List<SavedAd>>() {
            @Override
            public void onResponse(Call<List<Ads>> call, Response<List<Ads>> response) {
                Log.d(TAG, "📨 Resposta getSavedMessages: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    anunciosGuardados.clear();
                    savedAdsIds.clear();

                    Log.d(TAG, "✅ Anúncios guardados carregados: " + anunciosGuardados.size());

                            Log.d(TAG, "  ✅ " + anuncio.getTitulo() + " (ID: " + anuncio.getId() + ")");
                        }
                    }

                    runOnUiThread(() -> {
                        adapterGuardados.setSavedAdsIds(savedAdsIds);
                        adapterCriados.setSavedAdsIds(savedAdsIds);

                        if (currentTab == 0) {
                            updateUI();
                            Toast.makeText(ListMenu.this,
                                    anunciosGuardados.size() + " anúncios guardados",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "❌ Erro ao carregar guardados: " + response.code());

                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Sem corpo de erro";
                        Log.e(TAG, "Error Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody", e);
                    }

                    runOnUiThread(() -> {
                        if (currentTab == 0) {
                            updateUI();
                            Toast.makeText(ListMenu.this,
                                    "Erro ao carregar anúncios guardados",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Ads>> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao carregar guardados: " + t.getMessage());
                t.printStackTrace();

                runOnUiThread(() -> {
                    Toast.makeText(ListMenu.this,
                            "Erro de conexão ao carregar guardados",
                            Toast.LENGTH_SHORT).show();
                    if (currentTab == 0) {
                        updateUI();
                    }
                });
            }
        });
    }

    private void loadAnunciosCriados() {
        Log.d(TAG, "📥 Carregando anúncios criados...");

        apiService.getMyMessages().enqueue(new Callback<List<Ads>>() {
            @Override
            public void onResponse(Call<List<Ads>> call, Response<List<Ads>> response) {
                Log.d(TAG, "📨 Resposta getMyMessages: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    anunciosCriados.clear();
                    anunciosCriados.addAll(response.body());

                    Log.d(TAG, "✅ Anúncios criados carregados: " + anunciosCriados.size());

                    // Log de cada anúncio
                    for (int i = 0; i < anunciosCriados.size(); i++) {
                        Ads ads = anunciosCriados.get(i);
                        Log.d(TAG, "  " + (i + 1) + ". " + ads.getTitulo() +
                               ", Local: " + ads.getLocalId() + ")");
                    }

                    runOnUiThread(() -> {
                        if (currentTab == 1) {
                            updateUI();
                            Toast.makeText(ListMenu.this,
                                    anunciosCriados.size() + " anúncios criados",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "❌ Erro ao carregar criados: " + response.code());

                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Sem corpo de erro";
                        Log.e(TAG, "Error Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody", e);
                    }

                    runOnUiThread(() -> {
                        if (currentTab == 1) {
                            updateUI();
                            Toast.makeText(ListMenu.this,
                                    "Erro ao carregar seus anúncios",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Ads>> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao carregar criados: " + t.getMessage());
                t.printStackTrace();

                runOnUiThread(() -> {
                    Toast.makeText(ListMenu.this,
                            "Erro de conexão ao carregar seus anúncios",
                            Toast.LENGTH_SHORT).show();
                    if (currentTab == 1) {
                        updateUI();
                    }
                });
            }
        });
    }

    private void showGuardados() {
        Log.d(TAG, "👁️ Mostrando anúncios guardados");
        recyclerViewAnuncios.setAdapter(adapterGuardados);
        updateUI();
    }

    private void showCriados() {
        Log.d(TAG, "👁️ Mostrando anúncios criados");
        recyclerViewAnuncios.setAdapter(adapterCriados);
        updateUI();
    }

    private void updateUI() {
        List<Ads> currentList = currentTab == 0 ? anunciosGuardados : anunciosCriados;
        AnunciosAdapter currentAdapter = currentTab == 0 ? adapterGuardados : adapterCriados;
        String emptyMessage = currentTab == 0 ?
                "Você ainda não guardou nenhum anúncio" :
                "Você ainda não criou nenhum anúncio";

        txtTotalAnuncios.setText(currentList.size() + " anúncios");
        emptyStateText.setText(emptyMessage);
        currentAdapter.updateData(currentList);

        // Mostrar/ocultar empty state
        if (currentList.isEmpty()) {
            recyclerViewAnuncios.setVisibility(View.GONE);
            emptyStateCard.setVisibility(View.VISIBLE);
            Log.d(TAG, "📭 Empty state visível");
        } else {
            recyclerViewAnuncios.setVisibility(View.VISIBLE);
            emptyStateCard.setVisibility(View.GONE);
            Log.d(TAG, "📋 Lista visível com " + currentList.size() + " itens");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 onResume - Recarregando dados...");
        // Recarregar dados quando voltar para a activity
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        apiService = null;
    }
}