package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ao.co.isptec.aplm.locationads.network.singleton.TokenManager;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewAds extends AppCompatActivity {

    private static final String TAG = "ViewAds";

    // Views
    private ImageButton btnVoltar;
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
    private Ads currentAd;
    private boolean isFavorite = false;
    private int adId = -1;
    private int autorId = -1;
    private int currentUserId = -1;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
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

            // Carregar dados iniciais do Intent
            loadInitialDataFromIntent();

            // Buscar dados atualizados da API se tivermos um ID
            if (adId > 0) {
                loadAdFromApi();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO no onCreate: ", e);
            Toast.makeText(this, "Erro ao abrir anúncio: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        btnVoltar = findViewById(R.id.btnVoltar);
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

    private void setupListeners() {
        if (btnVoltar != null) btnVoltar.setOnClickListener(v -> finish());
        if (btnShare != null) btnShare.setOnClickListener(v -> shareAd());
        if (btnContact != null) btnContact.setOnClickListener(v -> showContactInfo());
        if (btnDelete != null) btnDelete.setOnClickListener(v -> confirmDeleteAd());
        if (fabFavorite != null) fabFavorite.setOnClickListener(v -> toggleFavorite());
    }

    private void loadInitialDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            adId = intent.getIntExtra("ad_id", -1);
            autorId = intent.getIntExtra("autor_id", -1);
            isFavorite = intent.getBooleanExtra("is_saved", false);

            String title = intent.getStringExtra("title");
            String location = intent.getStringExtra("location");
            String description = intent.getStringExtra("description");
            String date = intent.getStringExtra("date");
            String author = intent.getStringExtra("author");

            if (title != null) {
                adTitle.setText(title);
                collapsingToolbar.setTitle(title);
            }
            if (location != null) adLocation.setText(location);
            if (description != null) adsDescription.setText(description);
            if (date != null) adDate.setText(date);
            if (author != null) adAuthor.setText(author);

            updateFavoriteIcon();
            checkIfCreator();
        }
    }

    private void loadAdFromApi() {
        apiService.getMessageById(adId).enqueue(new Callback<Ads>() {
            @Override
            public void onResponse(@NonNull Call<Ads> call, @NonNull Response<Ads> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentAd = response.body();
                    updateUIWithAd(currentAd);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Ads> call, @NonNull Throwable t) {
                Log.e(TAG, "Erro ao carregar do servidor: " + t.getMessage());
            }
        });
    }

    private void updateUIWithAd(Ads ad) {
        if (ad == null) return;

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
            autorId = ad.getAutorId();
        }

        isFavorite = ad.isSalvo();
        updateFavoriteIcon();
        checkIfCreator();
    }

    private void checkIfCreator() {
        if (btnDelete != null) {
            if (currentUserId > 0 && autorId > 0 && currentUserId == autorId) {
                btnDelete.setVisibility(View.VISIBLE);
            } else {
                btnDelete.setVisibility(View.GONE);
            }
        }
    }

    private void toggleFavorite() {
        if (adId <= 0) return;

        if (isFavorite) {
            removeFromFavorites();
        } else {
            addToFavorites();
        }
    }

    private void addToFavorites() {
        apiService.saveMessage(adId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(ViewAds.this, "Salvo nos favoritos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(ViewAds.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeFromFavorites() {
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
                Toast.makeText(ViewAds.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFavoriteIcon() {
        if (fabFavorite != null) {
            if (isFavorite) {
                fabFavorite.setImageResource(R.drawable.ic_favorite_filled);
            } else {
                fabFavorite.setImageResource(R.drawable.ic_favorite_border);
            }
        }
    }

    private void confirmDeleteAd() {
        new AlertDialog.Builder(this)
                .setTitle("Apagar Anúncio")
                .setMessage("Deseja realmente apagar este anúncio?")
                .setPositiveButton("Sim", (dialog, which) -> deleteAd())
                .setNegativeButton("Não", null)
                .show();
    }

    private void deleteAd() {
        TokenManager tokenManager = TokenManager.getInstance(this);
        String token = tokenManager.getToken();

        apiService.deleteMessage(adId, "Bearer " + token).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ViewAds.this, "Anúncio apagado", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else if (response.code() == 403) {
                    Toast.makeText(ViewAds.this, "Permissão negada", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ViewAds.this, "Erro ao apagar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(ViewAds.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareAd() {
        String info = adTitle.getText().toString() + "\n" +
                adLocation.getText().toString() + "\n\n" +
                adsDescription.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, info);
        startActivity(Intent.createChooser(intent, "Compartilhar via"));
    }

    private void showContactInfo() {
        Toast.makeText(this, "Informações de contato do autor indisponíveis.", Toast.LENGTH_SHORT).show();
    }
}
