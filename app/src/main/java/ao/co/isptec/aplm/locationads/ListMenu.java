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
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.locationads.adapter.AnunciosAdapter;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListMenu extends AppCompatActivity {

    private static final String TAG = "ListMenu";

    // Views
    private TabLayout tabLayout;
    private RecyclerView recyclerViewAnuncios;
    private MaterialCardView emptyStateCard;
    private TextView txtTotalAnuncios;
    private ImageButton btnVoltar;

    // Adapters
    private AnunciosAdapter adapterGuardados;
    private AnunciosAdapter adapterCriados;

    // Data
    private List<Ads> anunciosGuardados;
    private List<Ads> anunciosCriados;
    private ApiService apiService;

    // Estado atual
    private int currentTab = 0; // 0 = Guardados, 1 = Criados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_menu);

        // Inicializar API
        apiService = ApiClient.getInstance(this).getApiService();

        // Inicializar views
        initViews();

        // Inicializar dados
        initData();

        // Configurar listeners
        setupListeners();

        // Carregar dados
        loadData();
    }

    /**
     * Inicializa todas as views
     */
    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        recyclerViewAnuncios = findViewById(R.id.recyclerViewAnuncios);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        txtTotalAnuncios = findViewById(R.id.txtTotalAnuncios);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Configurar RecyclerView
        recyclerViewAnuncios.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Inicializa listas e adapters
     */
    private void initData() {
        // Inicializar listas
        anunciosGuardados = new ArrayList<>();
        anunciosCriados = new ArrayList<>();

        // Criar adapters
        adapterGuardados = new AnunciosAdapter(this, anunciosGuardados);
        adapterCriados = new AnunciosAdapter(this, anunciosCriados);

        // Definir adapter inicial (Guardados)
        recyclerViewAnuncios.setAdapter(adapterGuardados);
    }

    /**
     * Configura os listeners
     */
    private void setupListeners() {
        // Botão voltar
        btnVoltar.setOnClickListener(v -> finish());

        // TabLayout - Alternar entre Guardados e Criados
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
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * Carrega os dados da API
     */
    private void loadData() {
        loadAnunciosGuardados();
        loadAnunciosCriados();
    }

    /**
     * Carrega anúncios guardados/salvos
     */
    private void loadAnunciosGuardados() {
        Log.d(TAG, "Carregando anúncios guardados...");

        apiService.getSavedMessages().enqueue(new Callback<List<Ads>>() {
            @Override
            public void onResponse(Call<List<Ads>> call, Response<List<Ads>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    anunciosGuardados.clear();
                    anunciosGuardados.addAll(response.body());

                    Log.d(TAG, "✅ Anúncios guardados: " + anunciosGuardados.size());

                    runOnUiThread(() -> {
                        if (currentTab == 0) {
                            updateUI();
                        }
                    });
                } else {
                    Log.e(TAG, "❌ Erro ao carregar guardados: " + response.code());
                    runOnUiThread(() -> {
                        if (currentTab == 0) {
                            updateUI();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Ads>> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao carregar guardados: " + t.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(ListMenu.this,
                            "Erro ao carregar anúncios guardados",
                            Toast.LENGTH_SHORT).show();
                    if (currentTab == 0) {
                        updateUI();
                    }
                });
            }
        });
    }

    /**
     * Carrega anúncios criados pelo usuário
     */
    private void loadAnunciosCriados() {
        Log.d(TAG, "Carregando anúncios criados...");

        apiService.getMyMessages().enqueue(new Callback<List<Ads>>() {
            @Override
            public void onResponse(Call<List<Ads>> call, Response<List<Ads>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    anunciosCriados.clear();
                    anunciosCriados.addAll(response.body());

                    Log.d(TAG, "✅ Anúncios criados: " + anunciosCriados.size());

                    runOnUiThread(() -> {
                        if (currentTab == 1) {
                            updateUI();
                        }
                    });
                } else {
                    Log.e(TAG, "❌ Erro ao carregar criados: " + response.code());
                    runOnUiThread(() -> {
                        if (currentTab == 1) {
                            updateUI();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Ads>> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao carregar criados: " + t.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(ListMenu.this,
                            "Erro ao carregar anúncios criados",
                            Toast.LENGTH_SHORT).show();
                    if (currentTab == 1) {
                        updateUI();
                    }
                });
            }
        });
    }

    /**
     * Mostra a aba de anúncios guardados
     */
    private void showGuardados() {
        Log.d(TAG, "Mostrando anúncios guardados");
        recyclerViewAnuncios.setAdapter(adapterGuardados);
        updateUI();
    }

    /**
     * Mostra a aba de anúncios criados
     */
    private void showCriados() {
        Log.d(TAG, "Mostrando anúncios criados");
        recyclerViewAnuncios.setAdapter(adapterCriados);
        updateUI();
    }

    /**
     * Atualiza a UI baseado na aba atual
     */
    private void updateUI() {
        List<Ads> currentList = currentTab == 0 ? anunciosGuardados : anunciosCriados;
        AnunciosAdapter currentAdapter = currentTab == 0 ? adapterGuardados : adapterCriados;

        // Atualizar contador
        txtTotalAnuncios.setText(String.valueOf(currentList.size()));

        // Notificar adapter
        currentAdapter.notifyDataSetChanged();

        // Mostrar/ocultar empty state
        if (currentList.isEmpty()) {
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
        // Recarregar dados quando voltar para a activity
        loadData();
    }
}