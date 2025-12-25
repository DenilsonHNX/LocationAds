package ao.co.isptec.aplm.locationads;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.locationads.adapter.AnunciosAdapter;

public class ListMenu extends AppCompatActivity {

    // Views
    private ImageButton btnVoltar;
    private TabLayout tabLayout;

    private LinearLayout containerGuardados;
    private LinearLayout containerCriados;

    private RecyclerView listGuardados;
    private RecyclerView listCriados;

    private MaterialCardView emptyStateGuardados;
    private MaterialCardView emptyStateCriados;

    private TextView txtTotalGuardados;
    private TextView txtTotalCriados;

    // Adapters
    private AnunciosAdapter adapterGuardados;
    private AnunciosAdapter adapterCriados;

    // Dados
    private List<AdMessage> anunciosGuardados = new ArrayList<>();
    private List<AdMessage> anunciosCriados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_menu);

        // Inicializar views
        initViews();

        // Configurar listeners
        setupListeners();

        // Configurar RecyclerViews
        setupRecyclerViews();

        // Carregar dados
        loadData();
    }

    /**
     * Inicializa todas as views
     */
    private void initViews() {
        btnVoltar = findViewById(R.id.btnVoltar);
        tabLayout = findViewById(R.id.tabLayout);

        containerGuardados = findViewById(R.id.containerGuardados);
        containerCriados = findViewById(R.id.containerCriados);

        listGuardados = findViewById(R.id.listGuardados);
        listCriados = findViewById(R.id.listCriados);

        emptyStateGuardados = findViewById(R.id.emptyStateGuardados);
        emptyStateCriados = findViewById(R.id.emptyStateCriados);

        txtTotalGuardados = findViewById(R.id.txtTotalGuardados);
        txtTotalCriados = findViewById(R.id.txtTotalCriados);
    }

    /**
     * Configura os listeners
     */
    private void setupListeners() {
        // Botão voltar
        btnVoltar.setOnClickListener(v -> finish());

        // TabLayout
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Guardados
                    showGuardados();
                } else {
                    // Criados
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
     * Configura os RecyclerViews
     */
    private void setupRecyclerViews() {
        // RecyclerView Guardados
        listGuardados.setLayoutManager(new LinearLayoutManager(this));
        adapterGuardados = new AnunciosAdapter(this, anunciosGuardados);
        listGuardados.setAdapter(adapterGuardados);

        // RecyclerView Criados
        listCriados.setLayoutManager(new LinearLayoutManager(this));
        adapterCriados = new AnunciosAdapter(this, anunciosCriados);
        listCriados.setAdapter(adapterCriados);
    }

    /**
     * Carrega os dados dos anúncios
     */
    private void loadData() {
        // Carregar anúncios guardados (do SharedPreferences ou banco de dados)
        loadAnunciosGuardados();

        // Carregar anúncios criados (do SharedPreferences ou banco de dados)
        loadAnunciosCriados();

        // Atualizar UI
        updateUI();
    }

    /**
     * Carrega anúncios guardados
     * TODO: Implementar carregamento do SharedPreferences ou banco de dados
     */
    private void loadAnunciosGuardados() {
        // Exemplo de dados mockados
        // Em produção, carregue do SharedPreferences ou banco de dados

        // anunciosGuardados.add(new AdMessage(...));
        // anunciosGuardados.add(new AdMessage(...));

        // Por enquanto, lista vazia para mostrar empty state
        anunciosGuardados.clear();
    }

    /**
     * Carrega anúncios criados pelo usuário
     * TODO: Implementar carregamento do SharedPreferences ou banco de dados
     */
    private void loadAnunciosCriados() {
        // Exemplo de dados mockados
        // Em produção, carregue do SharedPreferences ou banco de dados

        // anunciosCriados.add(new AdMessage(...));
        // anunciosCriados.add(new AdMessage(...));

        // Por enquanto, lista vazia para mostrar empty state
        anunciosCriados.clear();
    }

    /**
     * Atualiza a UI baseado nos dados
     */
    private void updateUI() {
        // Atualizar contadores
        txtTotalGuardados.setText(String.valueOf(anunciosGuardados.size()));
        txtTotalCriados.setText(String.valueOf(anunciosCriados.size()));

        // Atualizar adapters
        adapterGuardados.notifyDataSetChanged();
        adapterCriados.notifyDataSetChanged();

        // Mostrar/ocultar empty states
        updateEmptyStates();
    }

    /**
     * Atualiza os empty states
     */
    private void updateEmptyStates() {
        // Empty state guardados
        if (anunciosGuardados.isEmpty()) {
            listGuardados.setVisibility(View.GONE);
            emptyStateGuardados.setVisibility(View.VISIBLE);
        } else {
            listGuardados.setVisibility(View.VISIBLE);
            emptyStateGuardados.setVisibility(View.GONE);
        }

        // Empty state criados
        if (anunciosCriados.isEmpty()) {
            listCriados.setVisibility(View.GONE);
            emptyStateCriados.setVisibility(View.VISIBLE);
        } else {
            listCriados.setVisibility(View.VISIBLE);
            emptyStateCriados.setVisibility(View.GONE);
        }
    }

    /**
     * Mostra lista de guardados
     */
    private void showGuardados() {
        containerGuardados.setVisibility(View.VISIBLE);
        containerCriados.setVisibility(View.GONE);
    }

    /**
     * Mostra lista de criados
     */
    private void showCriados() {
        containerGuardados.setVisibility(View.GONE);
        containerCriados.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar dados quando voltar para a tela
        loadData();
    }

    /**
     * Método público para adicionar anúncio aos guardados
     */
    public static void adicionarGuardado(AdMessage anuncio) {
        // TODO: Salvar no SharedPreferences ou banco de dados
    }

    /**
     * Método público para remover anúncio dos guardados
     */
    public static void removerGuardado(AdMessage anuncio) {
        // TODO: Remover do SharedPreferences ou banco de dados
    }

    /**
     * Método público para adicionar anúncio aos criados
     */
    public static void adicionarCriado(AdMessage anuncio) {
        // TODO: Salvar no SharedPreferences ou banco de dados
    }
}