package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class PerfilAccount extends AppCompatActivity {

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
        infoTelefone = findViewById(R.id.info_telefone);
        infoDataCriacao = findViewById(R.id.info_dataCriancaoDaConta);

        txtVisualizados = findViewById(R.id.txtVisualizados);
        txtGuardados = findViewById(R.id.txtGuardados);
        txtPublicados = findViewById(R.id.txtPublicados);
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

        // Definir dados nas views
        perfilName.setText(nome);
        perfilEmail.setText(email);
        infoEmail.setText(email);
        infoTelefone.setText(telefone);
        infoDataCriacao.setText(formatarData(dataCriacao));
    }

    /**
     * Formata a data para um formato mais legível
     */
    private String formatarData(String data) {
        // Se a data estiver no formato dd/MM/yyyy, converte para formato mais legível
        // Exemplo: 02/11/2025 → 2 de Novembro, 2025

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
            e.printStackTrace();
        }

        return data; // Retorna original se falhar
    }

    /**
     * Carrega as estatísticas do usuário
     */
    private void loadStatistics() {
        SharedPreferences sharedPref = getSharedPreferences("user_stats", MODE_PRIVATE);

        // Obter estatísticas (valores padrão = 0)
        int visualizados = sharedPref.getInt("anuncios_visualizados", 0);
        int guardados = sharedPref.getInt("anuncios_guardados", 0);
        int publicados = sharedPref.getInt("anuncios_publicados", 0);

        // Definir nas views
        txtVisualizados.setText(String.valueOf(visualizados));
        txtGuardados.setText(String.valueOf(guardados));
        txtPublicados.setText(String.valueOf(publicados));
    }

    /**
     * Configura os listeners dos botões
     */
    private void setupListeners() {
        // Botão Voltar
        btnBack.setOnClickListener(v -> finish());

        // Botão Editar Perfil
        toEditPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilAccount.this, EditPerfilAccount.class);
            startActivity(intent);
        });

        // Botão Logout
        btnLogOut.setOnClickListener(v -> showLogoutDialog());
    }

    /**
     * Mostra dialog de confirmação de logout
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Terminar Sessão")
                .setMessage("Tem certeza que deseja sair da sua conta?")
                .setPositiveButton("Sim", (dialog, which) -> performLogout())
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_logout)
                .show();
    }

    /**
     * Executa o logout
     */
    private void performLogout() {
        // Limpar SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        // Também limpar estatísticas se necessário
        SharedPreferences statsPrefs = getSharedPreferences("user_stats", MODE_PRIVATE);
        statsPrefs.edit().clear().apply();

        // Mostrar mensagem
        Toast.makeText(this, "Sessão encerrada com sucesso", Toast.LENGTH_SHORT).show();

        // Redirecionar para tela de login
        Intent intent = new Intent(PerfilAccount.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar dados quando voltar para a tela (caso tenha editado)
        loadProfileData();
        loadStatistics();
    }

    /**
     * Método público para atualizar estatísticas
     * Pode ser chamado de outras activities
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