package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewAds extends AppCompatActivity {

    private static final String TAG = "ViewAds";

    // Views
    private ImageButton btnVoltar;
    private ImageView adImage;
    private TextView adTitle;
    private TextView adLocation;
    private TextView adsDescription;
    private TextView adDate;
    private TextView adAuthor;
    private MaterialButton btnShare;
    private MaterialButton btnContact;
    private MaterialButton btnDelete;
    private FloatingActionButton fabFavorite;
    private CollapsingToolbarLayout collapsingToolbar;

    // Data
    private boolean isFavorite = false;
    private int adId = -1;
    private int autorId = -1;
    private int currentUserId = -1;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ads);

        // Inicializar API
        apiService = ApiClient.getInstance(this).getApiService();

        // Obter ID do utilizador atual
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);

        // Inicializar views
        initViews();

        // Configurar listeners
        setupListeners();

        // Carregar dados do anúncio
        loadAdData();
    }

    /**
     * Inicializa todas as views
     */
    private void initViews() {
        btnVoltar = findViewById(R.id.btnVoltar);
        adImage = findViewById(R.id.adImage);
        adTitle = findViewById(R.id.adTitle);
        adLocation = findViewById(R.id.adLocation);
        adsDescription = findViewById(R.id.adsDescription);
        adDate = findViewById(R.id.adDate);
        adAuthor = findViewById(R.id.adAuthor);
        btnShare = findViewById(R.id.btnShare);
        btnContact = findViewById(R.id.btnContact);
        btnDelete = findViewById(R.id.btnDelete);
        fabFavorite = findViewById(R.id.fabFavorite);
        collapsingToolbar = findViewById(R.id.collapsingToolbar);
    }

    /**
     * Configura todos os listeners
     */
    private void setupListeners() {
        // Botão Voltar
        btnVoltar.setOnClickListener(v -> finish());

        // Botão Compartilhar
        btnShare.setOnClickListener(v -> shareAd());

        // Botão Contato/Mais Informações
        btnContact.setOnClickListener(v -> showContactInfo());

        // Botão Apagar (só visível se for o criador)
        if (btnDelete != null) {
            btnDelete.setOnClickListener(v -> confirmDeleteAd());
        }

        // FAB Favorito
        fabFavorite.setOnClickListener(v -> toggleFavorite());
    }

    /**
     * Carrega os dados do anúncio
     */
    private void loadAdData() {
        Intent intent = getIntent();
        if (intent != null) {
            // Obter dados básicos
            adId = intent.getIntExtra("ad_id", -1);
            autorId = intent.getIntExtra("autor_id", -1);
            isFavorite = intent.getBooleanExtra("is_saved", false);
            
            String title = intent.getStringExtra("title");
            String location = intent.getStringExtra("location");
            String description = intent.getStringExtra("description");
            String date = intent.getStringExtra("date");
            String author = intent.getStringExtra("author");
            int imageResId = intent.getIntExtra("imageResId", R.drawable.bg_placeholder);

            // Definir dados nas views
            if (title != null) {
                adTitle.setText(title);
                collapsingToolbar.setTitle(title);
            }
            if (location != null) adLocation.setText(location);
            if (description != null) adsDescription.setText(description);
            if (date != null) adDate.setText(date);
            if (author != null) adAuthor.setText(author);
            adImage.setImageResource(imageResId);

            // Atualizar estado do favorito
            updateFavoriteIcon();

            // Mostrar botão de apagar se for o criador
            checkIfCreator();

            Log.d(TAG, "Ad ID: " + adId + ", Autor ID: " + autorId + ", Current User: " + currentUserId);
        }

        // Se não tiver dados, carregar da API
        if (adId > 0 && adTitle.getText().toString().isEmpty()) {
            loadAdFromApi();
        }
    }

    /**
     * Carrega dados do anúncio da API
     */
    private void loadAdFromApi() {
        apiService.getMessageById(adId).enqueue(new Callback<Ads>() {
            @Override
            public void onResponse(@NonNull Call<Ads> call, @NonNull Response<Ads> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Ads ad = response.body();
                    
                    adTitle.setText(ad.getTitulo());
                    collapsingToolbar.setTitle(ad.getTitulo());
                    adsDescription.setText(ad.getConteudo());
                    
                    if (ad.getLocal() != null) {
                        adLocation.setText(ad.getLocal().getNome());
                    }
                    
                    if (ad.getCriadoEm() != null) {
                        adDate.setText(ad.getCriadoEm().substring(0, 10));
                    }
                    
                    if (ad.getAutor() != null) {
                        adAuthor.setText(ad.getAutor().getUsername());
                    }
                    
                    autorId = ad.getAutorId();
                    isFavorite = ad.isSalvo();
                    
                    updateFavoriteIcon();
                    checkIfCreator();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Ads> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao carregar anúncio", t);
            }
        });
    }

    /**
     * Verifica se o utilizador atual é o criador do anúncio
     */
    private void checkIfCreator() {
        if (btnDelete != null) {
            if (currentUserId > 0 && autorId > 0 && currentUserId == autorId) {
                btnDelete.setVisibility(View.VISIBLE);
                Log.d(TAG, "✅ Utilizador é o criador - mostrando botão apagar");
            } else {
                btnDelete.setVisibility(View.GONE);
                Log.d(TAG, "❌ Utilizador não é o criador - escondendo botão apagar");
            }
        }
    }

    /**
     * Confirma antes de apagar o anúncio
     */
    private void confirmDeleteAd() {
        new AlertDialog.Builder(this)
                .setTitle("Apagar Anúncio")
                .setMessage("Tem certeza que deseja apagar este anúncio? Esta ação não pode ser desfeita.")
                .setPositiveButton("Apagar", (dialog, which) -> deleteAd())
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    /**
     * Apaga o anúncio
     */
    private void deleteAd() {
        if (adId <= 0) {
            Toast.makeText(this, "Erro: ID do anúncio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Apagando anúncio ID: " + adId);

        apiService.deleteMessage(adId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ViewAds.this, "Anúncio apagado com sucesso", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✅ Anúncio apagado");
                    
                    // Voltar para a tela anterior
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.e(TAG, "Erro ao apagar: " + response.code());
                    if (response.code() == 403) {
                        Toast.makeText(ViewAds.this, "Sem permissão para apagar este anúncio", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ViewAds.this, "Erro ao apagar anúncio", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha ao apagar anúncio", t);
                Toast.makeText(ViewAds.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Atualiza o ícone de favorito
     */
    private void updateFavoriteIcon() {
        if (isFavorite) {
            fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            fabFavorite.setImageResource(R.drawable.ic_favorite_border);
        }
    }

    /**
     * Alterna estado de favorito
     */
    private void toggleFavorite() {
        if (adId <= 0) {
            Toast.makeText(this, "Erro: ID do anúncio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFavorite) {
            // Remover dos favoritos
            apiService.unsaveMessage(adId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        isFavorite = false;
                        updateFavoriteIcon();
                        Toast.makeText(ViewAds.this, "Removido dos favoritos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(ViewAds.this, "Erro ao remover dos favoritos", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Adicionar aos favoritos
            apiService.saveMessage(adId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        isFavorite = true;
                        updateFavoriteIcon();
                        Toast.makeText(ViewAds.this, "Adicionado aos favoritos", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(ViewAds.this, "Erro ao adicionar aos favoritos", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Compartilha o anúncio
     */
    private void shareAd() {
        String shareText = adTitle.getText().toString() + "\n" +
                adLocation.getText().toString() + "\n\n" +
                adsDescription.getText().toString() + "\n\n" +
                "Enviado via LocationAds";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, adTitle.getText().toString());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        try {
            startActivity(Intent.createChooser(shareIntent, "Compartilhar via"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Nenhum app de compartilhamento encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Mostra informações de contato
     */
    private void showContactInfo() {
        Toast.makeText(this, "Entrando em contato com " + adAuthor.getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
