package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ao.co.isptec.aplm.locationads.adapter.AnunciosAdapter;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Ads;
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

        adapterGuardados.setOnItemClickListener(new AnunciosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Ads ads) {
                openViewAds(ads);
            }

            @Override
            public void onSaveClick(Ads ads, boolean isSaved) {
                if (!isSaved) {
                    loadAnunciosGuardados();
                }
            }
        });

        adapterCriados.setOnItemClickListener(new AnunciosAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Ads ads) {
                openViewAds(ads);
            }

            @Override
            public void onSaveClick(Ads ads, boolean isSaved) {
                if (isSaved) {
                    savedAdsIds.add(ads.getId());
                } else {
                    savedAdsIds.remove(ads.getId());
                }
                adapterGuardados.setSavedAdsIds(savedAdsIds);
                adapterCriados.setSavedAdsIds(savedAdsIds);
            }
        });

        recyclerViewAnuncios.setAdapter(adapterGuardados);
    }

    private void setupListeners() {
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                updateTabSelection();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateTabSelection() {
        if (currentTab == 0) {
            recyclerViewAnuncios.setAdapter(adapterGuardados);
        } else {
            recyclerViewAnuncios.setAdapter(adapterCriados);
        }
        updateUI();
    }

    private void openViewAds(Ads ads) {
        if (ads == null) return;
        Intent intent = new Intent(this, ViewAds.class);
        intent.putExtra("ad_id", ads.getId());
        intent.putExtra("autor_id", ads.getAutorId());
        intent.putExtra("is_saved", savedAdsIds.contains(ads.getId()));
        intent.putExtra("title", ads.getTitulo());
        intent.putExtra("description", ads.getConteudo());
        if (ads.getLocal() != null) {
            intent.putExtra("location", ads.getLocal().getNome());
        }
        if (ads.getAutor() != null) {
            intent.putExtra("author", ads.getAutor().getUsername());
        }
        if (ads.getCriadoEm() != null) {
            intent.putExtra("date", ads.getCriadoEm().substring(0, 10));
        }
        startActivity(intent);
    }

    private void loadData() {
        loadAnunciosGuardados();
        loadAnunciosCriados();
    }

    private void loadAnunciosGuardados() {
        apiService.getSavedMessages().enqueue(new Callback<List<Ads>>() {
            @Override
            public void onResponse(@NonNull Call<List<Ads>> call, @NonNull Response<List<Ads>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    anunciosGuardados.clear();
                    savedAdsIds.clear();
                    for (Ads ad : response.body()) {
                        anunciosGuardados.add(ad);
                        savedAdsIds.add(ad.getId());
                    }
                    runOnUiThread(() -> {
                        adapterGuardados.setSavedAdsIds(savedAdsIds);
                        adapterCriados.setSavedAdsIds(savedAdsIds);
                        if (currentTab == 0) updateUI();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Ads>> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha ao carregar guardados: " + t.getMessage());
            }
        });
    }

    private void loadAnunciosCriados() {
        apiService.getMyMessages().enqueue(new Callback<List<Ads>>() {
            @Override
            public void onResponse(@NonNull Call<List<Ads>> call, @NonNull Response<List<Ads>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    anunciosCriados.clear();
                    anunciosCriados.addAll(response.body());
                    runOnUiThread(() -> {
                        if (currentTab == 1) updateUI();
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Ads>> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha ao carregar criados: " + t.getMessage());
            }
        });
    }

    private void updateUI() {
        List<Ads> currentList = currentTab == 0 ? anunciosGuardados : anunciosCriados;
        txtTotalAnuncios.setText(currentList.size() + " anúncios");
        
        if (currentList.isEmpty()) {
            emptyStateCard.setVisibility(View.VISIBLE);
            recyclerViewAnuncios.setVisibility(View.GONE);
            emptyStateText.setText(currentTab == 0 ? 
                "Você ainda não guardou nenhum anúncio" : 
                "Você ainda não criou nenhum anúncio");
        } else {
            emptyStateCard.setVisibility(View.GONE);
            recyclerViewAnuncios.setVisibility(View.VISIBLE);
        }
        
        if (currentTab == 0) adapterGuardados.notifyDataSetChanged();
        else adapterCriados.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}