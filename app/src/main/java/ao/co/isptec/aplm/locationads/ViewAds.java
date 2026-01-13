package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class ViewAds extends AppCompatActivity {

    private static final String TAG = "ViewAds";

    // Views obrigatórias
    private ImageButton btnVoltar;
    private TextView adTitle;
    private TextView adLocation;
    private TextView adsDescription;
    private TextView adDate;
    private TextView adAuthor;
    private MaterialButton btnShare;
    private MaterialButton btnContact;
    private FloatingActionButton fabFavorite;
    private CollapsingToolbarLayout collapsingToolbar;

    // Data
    private Ads currentAd;
    private boolean isSaved = false;
    private ApiService apiService;
    private String autorNome = null; // ✅ Nome do autor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Log.d(TAG, "🚀 ViewAds onCreate() iniciado");

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
        try {
            btnVoltar = findViewById(R.id.btnVoltar);
            adTitle = findViewById(R.id.adTitle);
            adLocation = findViewById(R.id.adLocation);
            adsDescription = findViewById(R.id.adsDescription);
            adDate = findViewById(R.id.adDate);
            adAuthor = findViewById(R.id.adAuthor);
            btnShare = findViewById(R.id.btnShare);
            btnContact = findViewById(R.id.btnContact);
            fabFavorite = findViewById(R.id.fabFavorite);
            collapsingToolbar = findViewById(R.id.collapsingToolbar);

            Log.d(TAG, "✅ Views obrigatórias encontradas");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao inicializar views: ", e);
            throw e;
        }
    }

    private void setupListeners() {
        btnVoltar.setOnClickListener(v -> {
            Log.d(TAG, "🔙 Voltando...");
            finish();
        });

        btnShare.setOnClickListener(v -> shareAd());
        btnContact.setOnClickListener(v -> showContactInfo());
        fabFavorite.setOnClickListener(v -> toggleSave());
    }

    private void loadAdData() {
        try {
            Intent intent = getIntent();
            if (intent == null) {
                Log.e(TAG, "❌ Intent é null");
                Toast.makeText(this, "Erro: Intent null", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Receber objeto Ads serializado
            String adsJson = intent.getStringExtra("ads_json");

            if (adsJson != null && !adsJson.isEmpty()) {
                Log.d(TAG, "📦 Recebendo ads_json");

                Gson gson = new Gson();
                currentAd = gson.fromJson(adsJson, Ads.class);

                Log.d(TAG, "✅ Anúncio deserializado: " + currentAd.getTitulo());

                // Verificar se está salvo
                isSaved = intent.getBooleanExtra("is_saved", false);
                Log.d(TAG, "💾 Está salvo? " + isSaved);

                displayAdData();
                return;
            }

            // Fallback: Receber ID
            int adId = intent.getIntExtra("ad_id", -1);
            if (adId != -1) {
                Log.d(TAG, "🔍 Recebendo ad_id: " + adId);
                loadAdFromApi(adId);
                return;
            }

            Log.e(TAG, "❌ Nenhum dado recebido");
            Toast.makeText(this, "Erro: Nenhum dado do anúncio", Toast.LENGTH_SHORT).show();
            finish();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao carregar dados: ", e);
            Toast.makeText(this, "Erro ao carregar: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadAdFromApi(int adId) {
        Log.d(TAG, "🔍 Buscando anúncio ID: " + adId);

        apiService.getMessageById(adId).enqueue(new Callback<Ads>() {
            @Override
            public void onResponse(Call<Ads> call, Response<Ads> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentAd = response.body();
                    Log.d(TAG, "✅ Anúncio carregado: " + currentAd.getTitulo());
                    displayAdData();
                } else {
                    Log.e(TAG, "❌ Erro ao carregar: " + response.code());
                    Toast.makeText(ViewAds.this, "Erro ao carregar anúncio",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Ads> call, Throwable t) {
                Log.e(TAG, "❌ Falha ao carregar", t);
                Toast.makeText(ViewAds.this, "Erro de conexão",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayAdData() {
        try {
            if (currentAd == null) {
                Log.e(TAG, "❌ currentAd é null!");
                return;
            }

            Log.d(TAG, "📄 Exibindo anúncio: " + currentAd.getTitulo());

            // Título
            if (currentAd.getTitulo() != null) {
                adTitle.setText(currentAd.getTitulo());
                collapsingToolbar.setTitle(currentAd.getTitulo());
            } else {
                adTitle.setText("Sem título");
                collapsingToolbar.setTitle("Anúncio");
            }

            // Conteúdo
            if (currentAd.getConteudo() != null) {
                adsDescription.setText(currentAd.getConteudo());
            } else {
                adsDescription.setText("Sem descrição");
            }

            // ✅ LOCALIZAÇÃO - Extrair NOME do objeto Local aninhado
            String locationText = getLocationName();
            adLocation.setText(locationText);
            Log.d(TAG, "📍 Localização: " + locationText);

            // Data
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            adDate.setText(sdf.format(new Date()));

            // ✅ AUTOR - Buscar NOME do autor (temporariamente mostra ID enquanto carrega)
            adAuthor.setText("Carregando autor...");
            loadAutorName();

            // Botão salvar
            updateSaveButton();

            Log.d(TAG, "✅ Dados exibidos com sucesso!");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao exibir dados: ", e);
            Toast.makeText(this, "Erro ao exibir dados", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ NOVO: Obtém o NOME do local do objeto aninhado
     */
    private String getLocationName() {
        if (currentAd.getLocal() != null) {
            Local local = currentAd.getLocal();
            String nome = local.getNome();
            String tipo = local.getTipo();

            if (nome != null && !nome.isEmpty()) {
                if (tipo != null && !tipo.isEmpty()) {
                    return nome + " (" + tipo + ")";
                }
                return nome;
            }
        }

        // Fallback: usar ID
        return "Local ID: " + currentAd.getLocalId();
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
        if (currentAd == null) return;

        String shareText = currentAd.getTitulo() + "\n\n" +
                currentAd.getConteudo() + "\n\n" +
                "Local: " + getLocationName();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentAd.getTitulo());
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        try {
            startActivity(Intent.createChooser(shareIntent, "Compartilhar via"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Nenhum app de compartilhamento encontrado",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showContactInfo() {
        String autorInfo = autorNome != null ? autorNome : ("Usuário " + currentAd.getAutorId());
        Toast.makeText(this, "Entrando em contato com " + autorInfo,
                Toast.LENGTH_SHORT).show();
    }
}