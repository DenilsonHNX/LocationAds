package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.locationads.adapter.PropertyReadOnlyAdapter;
import ao.co.isptec.aplm.locationads.network.models.PerfilKeyValue;
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

    // Profile properties
    private RecyclerView recyclerViewProperties;
    private TextView txtPropertiesCount;
    private View layoutEmptyProperties;
    private PropertyReadOnlyAdapter propertiesAdapter;
    private List<PerfilKeyValue> propertiesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_account);

        initViews();
        loadProfileData();
        setupListeners();
        setupPropertiesRecyclerView();
        loadProfileProperties();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        toEditPerfil = findViewById(R.id.toEditPerfil);
        btnLogOut = findViewById(R.id.btnLogOut);

        perfilName = findViewById(R.id.perfilName);
        perfilEmail = findViewById(R.id.perfilEmail);
        infoEmail = findViewById(R.id.info_email);
        infoTelefone = findViewById(R.id.info_telefone);

        recyclerViewProperties = findViewById(R.id.recyclerViewProperties);
        txtPropertiesCount = findViewById(R.id.txtPropertiesCount);
        layoutEmptyProperties = findViewById(R.id.layoutEmptyProperties);

        propertiesList = new ArrayList<>();

        Log.d(TAG, "Views inicializadas");
    }

    private void loadProfileData() {
        TokenManager tokenManager = TokenManager.getInstance(this);
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);

        String nome = sharedPref.getString("nomeCompleto", "Nome não definido");
        String email = sharedPref.getString("email", "Email não definido");
        String telefone = sharedPref.getString("telefone", "Não informado");

        Log.d(TAG, "Dados carregados - Nome: " + nome + ", Email: " + email);

        if (perfilName != null) perfilName.setText(nome);
        if (perfilEmail != null) perfilEmail.setText(email);
        if (infoEmail != null) infoEmail.setText(email);
        if (infoTelefone != null) infoTelefone.setText(telefone);
    }

    private void setupPropertiesRecyclerView() {
        propertiesAdapter = new PropertyReadOnlyAdapter(propertiesList);
        recyclerViewProperties.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProperties.setAdapter(propertiesAdapter);
    }

    private void loadProfileProperties() {
        TokenManager tokenManager = TokenManager.getInstance(this);
        String token = tokenManager.getToken();
        int userId = tokenManager.getUserIdFromToken();

        if (token == null || userId == -1) {
            Log.e(TAG, "Token ou userId inválido");
            updatePropertiesUI();
            return;
        }

        Log.d(TAG, "🔄 Carregando propriedades do usuário ID: " + userId);

        ApiClient.getInstance(this)
                .getApiService()
                .getUserPerfil(userId,"Bearer " + token)
                .enqueue(new Callback<List<PerfilKeyValue>>() {
                    @Override
                    public void onResponse(Call<List<PerfilKeyValue>> call,
                                           Response<List<PerfilKeyValue>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<PerfilKeyValue> properties = response.body();
                            Log.d(TAG, "✅ Propriedades recebidas: " + properties.size());

                            runOnUiThread(() -> {
                                propertiesList.clear();
                                propertiesList.addAll(properties);
                                propertiesAdapter.notifyDataSetChanged();
                                updatePropertiesUI();
                            });
                        } else {
                            Log.e(TAG, "❌ Erro ao carregar propriedades: " + response.code());
                            runOnUiThread(() -> updatePropertiesUI());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PerfilKeyValue>> call, Throwable t) {
                        Log.e(TAG, "❌ Falha ao carregar propriedades: " + t.getMessage(), t);
                        runOnUiThread(() -> updatePropertiesUI());
                    }
                });
    }

    private void updatePropertiesUI() {
        int count = propertiesList.size();
        txtPropertiesCount.setText(String.valueOf(count));

        if (count == 0) {
            recyclerViewProperties.setVisibility(View.GONE);
            layoutEmptyProperties.setVisibility(View.VISIBLE);
        } else {
            recyclerViewProperties.setVisibility(View.VISIBLE);
            layoutEmptyProperties.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                Log.d(TAG, "Botão voltar clicado");
                finish();
            });
        }

        if (toEditPerfil != null) {
            toEditPerfil.setOnClickListener(v -> {
                Log.d(TAG, "Botão editar perfil clicado");
                Intent intent = new Intent(PerfilAccount.this, EditPerfilAccount.class);
                startActivity(intent);
            });
        }

        if (btnLogOut != null) {
            btnLogOut.setOnClickListener(v -> {
                Log.d(TAG, "Botão logout clicado");
                showLogoutDialog();
            });
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Terminar Sessão")
                .setMessage("Tem certeza que deseja sair da sua conta?")
                .setPositiveButton("Sim", (dialog, which) -> performLogout())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void performLogout() {
        Log.d(TAG, "Executando logout...");

        try {
            TokenManager tokenManager = TokenManager.getInstance(this);
            tokenManager.clearUserData();

            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
            prefs.edit().clear().commit();

            Toast.makeText(this, "Sessão encerrada com sucesso", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(PerfilAccount.this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

            Log.d(TAG, "Logout concluído");
        } catch (Exception e) {
            Log.e(TAG, "ERRO no logout: " + e.getMessage(), e);
            Toast.makeText(this, "Erro ao sair: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume chamado");
        loadProfileData();
        loadProfileProperties();
    }
}