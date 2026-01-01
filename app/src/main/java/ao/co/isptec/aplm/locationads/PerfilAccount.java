package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.UserProfile;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import ao.co.isptec.aplm.locationads.network.singleton.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilAccount extends AppCompatActivity {

    private static final String TAG = "PerfilAccount";

    // Views
    private ImageButton btnBack;
    private ImageButton toEditPerfil;
    private MaterialButton btnLogOut;

    private TextView perfilName;
    private TextView perfilEmail;
    private TextView infoEmail;
    private TextView infoTelefone;

    private TextView txtVisualizados;
    private TextView txtGuardados;
    private TextView txtPublicados;

    // API
    private ApiService apiService;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_account);

        // Inicializar API
        apiService = ApiClient.getInstance().getApiService();
        tokenManager = TokenManager.getInstance(this);

        // Inicializar views
        initViews();

        // Carregar dados do perfil da API
        loadProfileFromApi();

        // Configurar listeners
        setupListeners();

        // Carregar estatísticas
        loadStatistics();
    }

    /**
     * Inicializa todas as views
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        toEditPerfil = findViewById(R.id.toEditPerfil);
        btnLogOut = findViewById(R.id.btnLogOut);

        perfilName = findViewById(R.id.perfilName);
        perfilEmail = findViewById(R.id.perfilEmail);
        infoEmail = findViewById(R.id.info_email);
        infoTelefone = findViewById(R.id.info_telefone);

        txtVisualizados = findViewById(R.id.txtVisualizados);
        txtGuardados = findViewById(R.id.txtGuardados);
        txtPublicados = findViewById(R.id.txtPublicados);

        Log.d(TAG, "Views inicializadas");
    }

    /**
     * Carrega os dados do perfil da API
     */
    private void loadProfileFromApi() {
        Log.d(TAG, "Carregando perfil da API...");

        String token = "Bearer " + tokenManager.getToken();

        apiService.getUserProfile(token).enqueue(new Callback<UserProfile>() {
            @Override
            public void onResponse(Call<UserProfile> call, Response<UserProfile> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfile profile = response.body();

                    Log.d(TAG, "✅ Perfil carregado da API");
                    Log.d(TAG, "User: " + profile);

                    // Extrair dados do response
                    // O response é: {"message": "...", "user": {"sub": "...", "email": "...", "name": "..."}}
                    // Mas precisamos acessar via UserProfile

                    // Atualizar UI
                    runOnUiThread(() -> {
                        updateUI(profile);
                        saveProfileDataLocally(profile);
                    });

                } else {
                    Log.e(TAG, "Erro ao carregar perfil: " + response.code());
                    // Fallback: tentar carregar do SharedPreferences
                    loadProfileFromSharedPreferences();
                }
            }

            @Override
            public void onFailure(Call<UserProfile> call, Throwable t) {
                Log.e(TAG, "Falha ao carregar perfil da API", t);
                // Fallback: tentar carregar do SharedPreferences
                loadProfileFromSharedPreferences();
            }
        });
    }

    /**
     * Atualiza a UI com os dados do perfil
     */
    private void updateUI(UserProfile profile) {
        // Nome do usuário
        String username = tokenManager.getUsername();
        if (username != null && !username.isEmpty()) {
            perfilName.setText(username);
        }

        // Email do usuário
        String email = tokenManager.getEmail();
        if (email != null && !email.isEmpty()) {
            perfilEmail.setText(email);
            infoEmail.setText(email);
        }

        // Telefone (se existir no SharedPreferences)
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String telefone = prefs.getString("telefone", "Não definido");
        infoTelefone.setText(telefone);

        Log.d(TAG, "UI atualizada com dados do perfil");
    }

    /**
     * Salva os dados do perfil localmente para fallback
     */
    private void saveProfileDataLocally(UserProfile profile) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String username = tokenManager.getUsername();
        String email = tokenManager.getEmail();

        if (username != null) {
            editor.putString("nomeCompleto", username);
        }

        if (email != null) {
            editor.putString("email", email);
        }

        editor.apply();

        Log.d(TAG, "Dados salvos localmente");
    }

    /**
     * Carrega dados do SharedPreferences (fallback)
     */
    private void loadProfileFromSharedPreferences() {
        Log.d(TAG, "Carregando perfil do SharedPreferences (fallback)");

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        String nome = prefs.getString("nomeCompleto", "Nome não definido");
        String email = prefs.getString("email", "Email não definido");
        String telefone = prefs.getString("telefone", "Não definido");

        // Verificar também o TokenManager
        String tokenName = tokenManager.getUsername();
        String tokenEmail = tokenManager.getEmail();

        if (tokenName != null && !tokenName.isEmpty()) {
            nome = tokenName;
        }

        if (tokenEmail != null && !tokenEmail.isEmpty()) {
            email = tokenEmail;
        }

        Log.d(TAG, "Nome: " + nome);
        Log.d(TAG, "Email: " + email);

        // Atualizar UI
        perfilName.setText(nome);
        perfilEmail.setText(email);
        infoEmail.setText(email);
        infoTelefone.setText(telefone);
    }

    /**
     * Carrega as estatísticas do usuário
     */
    private void loadStatistics() {
        SharedPreferences prefs = getSharedPreferences("user_stats", MODE_PRIVATE);

        int visualizados = prefs.getInt("anuncios_visualizados", 0);
        int guardados = prefs.getInt("anuncios_guardados", 0);
        int publicados = prefs.getInt("anuncios_publicados", 0);

        txtVisualizados.setText(String.valueOf(visualizados));
        txtGuardados.setText(String.valueOf(guardados));
        txtPublicados.setText(String.valueOf(publicados));
    }

    /**
     * Configura os listeners dos botões
     */
    private void setupListeners() {
        // Botão Voltar
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Botão voltar clicado");
            finish();
        });

        // Botão Editar Perfil
        toEditPerfil.setOnClickListener(v -> {
            Log.d(TAG, "Botão editar perfil clicado");
            Intent intent = new Intent(PerfilAccount.this, EditPerfilAccount.class);
            startActivity(intent);
        });

        // Botão Logout
        btnLogOut.setOnClickListener(v -> {
            Log.d(TAG, "Botão logout clicado");
            showLogoutDialog();
        });
    }

    /**
     * Mostra dialog de confirmação de logout
     */
    private void showLogoutDialog() {
        Log.d(TAG, "Mostrando dialog de logout");

        new AlertDialog.Builder(this)
                .setTitle("Terminar Sessão")
                .setMessage("Tem certeza que deseja sair da sua conta?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    Log.d(TAG, "Usuário confirmou logout");
                    performLogout();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    Log.d(TAG, "Usuário cancelou logout");
                })
                .show();
    }

    /**
     * Executa o logout
     */
    private void performLogout() {
        Log.d(TAG, "Executando logout...");

        try {
            // Limpar TokenManager
            tokenManager.clearUserData();
            Log.d(TAG, "✅ TokenManager limpo");

            // Limpar SharedPreferences
            SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            userPrefs.edit().clear().commit();
            Log.d(TAG, "✅ user_prefs limpo");

            SharedPreferences statsPrefs = getSharedPreferences("user_stats", MODE_PRIVATE);
            statsPrefs.edit().clear().commit();
            Log.d(TAG, "✅ user_stats limpo");

            // Mostrar mensagem
            Toast.makeText(this, "Sessão encerrada com sucesso", Toast.LENGTH_SHORT).show();

            // Redirecionar para SplashActivity
            Intent intent = new Intent(PerfilAccount.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            finishAffinity();

            Log.d(TAG, "✅ Logout concluído");

        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO no logout: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Erro ao sair: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume chamado");
        loadProfileFromApi();
        loadStatistics();
    }

    /**
     * Métodos públicos para atualizar estatísticas
     */
    public static void incrementarVisualizados(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_stats", MODE_PRIVATE);
        int atual = prefs.getInt("anuncios_visualizados", 0);
        prefs.edit().putInt("anuncios_visualizados", atual + 1).apply();
    }

    public static void incrementarGuardados(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_stats", MODE_PRIVATE);
        int atual = prefs.getInt("anuncios_guardados", 0);
        prefs.edit().putInt("anuncios_guardados", atual + 1).apply();
    }

    public static void decrementarGuardados(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_stats", MODE_PRIVATE);
        int atual = prefs.getInt("anuncios_guardados", 0);
        if (atual > 0) {
            prefs.edit().putInt("anuncios_guardados", atual - 1).apply();
        }
    }

    public static void incrementarPublicados(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_stats", MODE_PRIVATE);
        int atual = prefs.getInt("anuncios_publicados", 0);
        prefs.edit().putInt("anuncios_publicados", atual + 1).apply();
    }
}