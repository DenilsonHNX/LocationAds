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
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Inicializar API
        apiService = ApiClient.getInstance(this).getApiService();

        // Obter ID do utilizador atual
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);

        // Inicializar views
        initViews();

            setContentView(R.layout.activity_view_ads);
            Log.d(TAG, "✅ Layout inflado");

            // Inicializar API
            apiService = ApiClient.getInstance(this).getApiService();
            Log.d(TAG, "✅ ApiService inicializado");

            // Inicializar views
            initViews();
            Log.d(TAG, "✅ Views inicializadas");

            // Configurar listeners
            setupListeners();
            Log.d(TAG, "✅ Listeners configurados");

            // Carregar dados do anúncio
            loadAdData();
            Log.d(TAG, "✅ Dados carregados");

        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO no onCreate: ", e);
            Toast.makeText(this, "Erro ao abrir anúncio: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

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

    private void setupListeners() {
        // Botão Voltar
        btnVoltar.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> shareAd());
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
     * ✅ NOVO: Busca o NOME do autor da API
     * Nota: Precisa de um endpoint que retorne dados do usuário
     * Por enquanto, vou usar um placeholder
     */
    private void loadAutorName() {
        // TODO: Implementar endpoint para buscar usuário por ID
        // Por exemplo: apiService.getUserById(autorId)

        // TEMPORÁRIO: Mostrar "Usuário [ID]"
        int autorId = currentAd.getAutorId();
        adAuthor.setText("Usuário " + autorId);

        Log.d(TAG, "⚠️ Nome do autor não disponível, mostrando ID: " + autorId);

        /*
        // ✅ QUANDO TIVER O ENDPOINT, usar assim:
        apiService.getUserById(autorId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String nome = response.body().getNome();
                    adAuthor.setText(nome);
                    autorNome = nome;
                } else {
                    adAuthor.setText("Usuário " + autorId);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                adAuthor.setText("Usuário " + autorId);
            }
        });
        */
    }

    private String formatTimeWindow() {
        if (currentAd.getHoraInicio() == null || currentAd.getHoraFim() == null) {
            return "Sempre disponível";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat(
                    "dd/MM/yyyy HH:mm", Locale.getDefault());

            Date inicio = inputFormat.parse(currentAd.getHoraInicio());
            Date fim = inputFormat.parse(currentAd.getHoraFim());

            if (inicio != null && fim != null) {
                return "Válido de " + outputFormat.format(inicio) +
                        " até " + outputFormat.format(fim);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Erro ao parsear datas", e);
        }

        return "Período: " + currentAd.getHoraInicio() + " - " + currentAd.getHoraFim();
    }

    private String formatRestrictions() {
        Map<String, Object> restricoes = currentAd.getRestricoes();

        if (restricoes == null || restricoes.isEmpty()) {
            return "Sem restrições";
        }

        StringBuilder sb = new StringBuilder("Restrições: ");
        int count = 0;
        for (Map.Entry<String, Object> entry : restricoes.entrySet()) {
            if (count > 0) sb.append(", ");
            sb.append(entry.getKey()).append(" = ").append(entry.getValue());
            count++;
        }

        return sb.toString();
    }

    private void updateSaveButton() {
        if (isSaved) {
            fabFavorite.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            fabFavorite.setImageResource(R.drawable.ic_bookmark_border);
        }
    }

    private void toggleSave() {
        if (currentAd == null || currentAd.getId() == null) {
            Toast.makeText(this, "Erro: anúncio inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSaved) {
            unsaveAd();
        } else {
            saveAd();
        }
    }

    private void saveAd() {
        Log.d(TAG, "💾 Salvando anúncio ID: " + currentAd.getId());

        apiService.saveMessage(currentAd.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    isSaved = true;
                    updateSaveButton();
                    Toast.makeText(ViewAds.this, "Anúncio salvo!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✅ Salvo com sucesso");
                } else {
                    Log.e(TAG, "❌ Erro ao salvar: " + response.code());
                    Toast.makeText(ViewAds.this, "Erro ao salvar", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao salvar", t);
                Toast.makeText(ViewAds.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void unsaveAd() {
        Log.d(TAG, "🗑️ Removendo anúncio ID: " + currentAd.getId());

        apiService.unsaveMessage(currentAd.getId()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    isSaved = false;
                    updateSaveButton();
                    Toast.makeText(ViewAds.this, "Removido dos salvos", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✅ Removido com sucesso");
                } else {
                    Log.e(TAG, "❌ Erro ao remover: " + response.code());
                    Toast.makeText(ViewAds.this, "Erro ao remover", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao remover", t);
                Toast.makeText(ViewAds.this, "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareAd() {
        String shareText = adTitle.getText().toString() + "\n" +
                adLocation.getText().toString() + "\n\n" +
                adsDescription.getText().toString() + "\n\n" +
                "Enviado via LocationAds";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentAd.getTitulo());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        try {
            startActivity(Intent.createChooser(shareIntent, "Compartilhar via"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Nenhum app de compartilhamento encontrado", Toast.LENGTH_SHORT).show();
        }
    }

    private void showContactInfo() {
        Toast.makeText(this, "Entrando em contato com " + adAuthor.getText().toString(), Toast.LENGTH_SHORT).show();
    }
}
