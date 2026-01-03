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

import ao.co.isptec.aplm.locationads.network.singleton.TokenManager;

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
    private TextView infoDataCriacao;

    private TextView txtVisualizados;
    private TextView txtGuardados;
    private TextView txtPublicados;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_account);

        // Inicializar views
        initViews();

        // Carregar dados do perfil
        loadProfileData();

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

        // ✅ CORRIGIDO: Tentar ambos os IDs para compatibilidade
        infoTelefone = findViewById(R.id.info_telefone);
        if (infoTelefone == null) {
            infoTelefone = findViewById(R.id.info_telefone); // ID antigo do XML original
        }

        txtVisualizados = findViewById(R.id.txtVisualizados);
        txtGuardados = findViewById(R.id.txtGuardados);
        txtPublicados = findViewById(R.id.txtPublicados);

        // Log para debug
        Log.d(TAG, "Views inicializadas:");
        Log.d(TAG, "btnBack: " + (btnBack != null));
        Log.d(TAG, "btnLogOut: " + (btnLogOut != null));
        Log.d(TAG, "toEditPerfil: " + (toEditPerfil != null));
    }

    /**
     * Carrega os dados do perfil do SharedPreferences
     */
    private void loadProfileData() {
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);

        // Obter dados
        String nome = sharedPref.getString("nomeCompleto", "Nome não definido");
        String email = sharedPref.getString("email", "Email não definido");
        String telefone = sharedPref.getString("telefone", "Telefone não definido");
        String dataCriacao = sharedPref.getString("dataCriacao", "Data não definida");

        Log.d(TAG, "Dados carregados:");
        Log.d(TAG, "Nome: " + nome);
        Log.d(TAG, "Email: " + email);

        // Definir dados nas views (com verificação de null)
        if (perfilName != null) {
            perfilName.setText(nome);
        }

        if (perfilEmail != null) {
            perfilEmail.setText(email);
        }

        if (infoEmail != null) {
            infoEmail.setText(email);
        }

        if (infoTelefone != null) {
            infoTelefone.setText(telefone);
        }

        if (infoDataCriacao != null) {
            infoDataCriacao.setText(formatarData(dataCriacao));
        }
    }

    /**
     * Formata a data para um formato mais legível
     */
    private String formatarData(String data) {
        if (data == null || data.equals("Data não definida")) {
            return data;
        }

        try {
            String[] partes = data.split("/");
            if (partes.length == 3) {
                int dia = Integer.parseInt(partes[0]);
                int mes = Integer.parseInt(partes[1]);
                String ano = partes[2];

                String[] meses = {
                        "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                        "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
                };

                return dia + " de " + meses[mes - 1] + ", " + ano;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao formatar data: " + e.getMessage());
        }

        return data;
    }

    /**
     * Carrega as estatísticas do usuário
     */
    private void loadStatistics() {
        SharedPreferences sharedPref = getSharedPreferences("user_stats", MODE_PRIVATE);

        int visualizados = sharedPref.getInt("anuncios_visualizados", 0);
        int guardados = sharedPref.getInt("anuncios_guardados", 0);
        int publicados = sharedPref.getInt("anuncios_publicados", 0);

        if (txtVisualizados != null) {
            txtVisualizados.setText(String.valueOf(visualizados));
        }

        if (txtGuardados != null) {
            txtGuardados.setText(String.valueOf(guardados));
        }

        if (txtPublicados != null) {
            txtPublicados.setText(String.valueOf(publicados));
        }
    }

    /**
     * Configura os listeners dos botões
     */
    private void setupListeners() {
        // Botão Voltar
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "Botão voltar clicado");
                finish();
            });
        }

        // Botão Editar Perfil
        if (toEditPerfil != null) {
            toEditPerfil.setOnClickListener(v -> {
                Log.d(TAG, "Botão editar perfil clicado");
                Intent intent = new Intent(PerfilAccount.this, EditPerfilAccount.class);
                startActivity(intent);
            });
        }

        // Botão Logout
        if (btnLogOut != null) {
            btnLogOut.setOnClickListener(v -> {
                Log.d(TAG, "Botão logout clicado");
                showLogoutDialog();
            });
        } else {
            Log.e(TAG, "ERRO: btnLogOut é NULL!");
        }
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

            TokenManager tokenManager = TokenManager.getInstance(this);
            tokenManager.clearUserData(); // ← Isso aqui!

            // Limpar SharedPreferences antigo também
            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit().clear().commit();

            // Limpar SharedPreferences de user_prefs
            SharedPreferences userPrefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor userEditor = userPrefs.edit();
            userEditor.clear();
            boolean userCleared = userEditor.commit(); // Usar commit para garantir gravação imediata
            Log.d(TAG, "user_prefs limpo: " + userCleared);

            // Limpar SharedPreferences de user_stats
            SharedPreferences statsPrefs = getSharedPreferences("user_stats", MODE_PRIVATE);
            SharedPreferences.Editor statsEditor = statsPrefs.edit();
            statsEditor.clear();
            boolean statsCleared = statsEditor.commit();
            Log.d(TAG, "user_stats limpo: " + statsCleared);

            // Mostrar mensagem
            Toast.makeText(this, "Sessão encerrada com sucesso", Toast.LENGTH_SHORT).show();

            // Redirecionar para LoginActivity
            Log.d(TAG, "Redirecionando para LoginActivity");
            Intent intent = new Intent(PerfilAccount.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // Finalizar esta activity
            finish();

            Log.d(TAG, "Logout concluído");

        } catch (Exception e) {
            Log.e(TAG, "ERRO no logout: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Erro ao sair: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume chamado");
        loadProfileData();
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