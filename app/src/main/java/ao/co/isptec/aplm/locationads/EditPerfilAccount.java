package ao.co.isptec.aplm.locationads;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.locationads.adapter.PerfilAdapter;
import ao.co.isptec.aplm.locationads.network.models.PerfilKeyValue;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import ao.co.isptec.aplm.locationads.network.singleton.ProfileManager;
import ao.co.isptec.aplm.locationads.network.singleton.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPerfilAccount extends AppCompatActivity implements PerfilAdapter.OnPropertyActionListener {

    private static final String TAG = "EditPerfilAccount";

    // Views - Campos básicos
    private TextInputEditText editNome;
    private TextInputEditText editTelefone;

    // Views - Campos de propriedades chave-valor
    private TextInputEditText editKey;
    private TextInputEditText editValue;

    // Views - Botões
    private ImageButton btnVoltar;
    private MaterialButton btnAddProperty;
    private MaterialButton btnViewPublicKeys;
    private MaterialButton btnGuardar;

    // Views - RecyclerView e outros
    private RecyclerView recyclerViewPerfil;
    private TextView textPropertyCount;
    private View layoutEmptyState;

    // Dados
    private PerfilAdapter perfilAdapter;
    private List<PerfilKeyValue> perfilList;
    private ProfileManager profileManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_perfil_account);

        initViews();
        profileManager = ProfileManager.getInstance(this);

        // Inicializar lista vazia
        perfilList = new ArrayList<>();

        setupRecyclerView();
        setupListeners();

        // Carregar do backend
        loadUserProfileFromBackend();
    }

    private void initViews() {
        // Campos básicos
        editNome = findViewById(R.id.editNome);
        editTelefone = findViewById(R.id.editTelefone);

        // Campos de propriedades
        editKey = findViewById(R.id.editKey);
        editValue = findViewById(R.id.editValue);

        // Botões
        btnVoltar = findViewById(R.id.btnVoltar);
        btnAddProperty = findViewById(R.id.btnAddProperty);
        btnViewPublicKeys = findViewById(R.id.btnViewPublicKeys);
        btnGuardar = findViewById(R.id.btnGuardar);

        // RecyclerView e outros
        recyclerViewPerfil = findViewById(R.id.recyclerViewPerfil);
        textPropertyCount = findViewById(R.id.textPropertyCount);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
    }

    /**
     * ✅ CARREGAR PERFIL DO BACKEND
     */
    private void loadUserProfileFromBackend() {
        TokenManager tokenManager = TokenManager.getInstance(this);
        String token = tokenManager.getToken();
        int userId = tokenManager.getUserIdFromToken();

        if (token == null || userId == -1) {
            Toast.makeText(this, "Token inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "🔄 Carregando perfil do usuário ID: " + userId);

        ApiClient.getInstance(this)
                .getApiService()
                .getUserProfile(userId, "Bearer " + token)
                .enqueue(new Callback<List<PerfilKeyValue>>() {
                    @Override
                    public void onResponse(Call<List<PerfilKeyValue>> call,
                                           Response<List<PerfilKeyValue>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<PerfilKeyValue> properties = response.body();
                            Log.d(TAG, "✅ Propriedades recebidas: " + properties.size());

                            runOnUiThread(() -> {
                                perfilList.clear();
                                perfilList.addAll(properties);
                                perfilAdapter.notifyDataSetChanged();
                                updatePropertyCount();
                                updateEmptyState();

                                Toast.makeText(EditPerfilAccount.this,
                                        perfilList.size() + " propriedades carregadas",
                                        Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            Log.e(TAG, "❌ Erro ao carregar: " + response.code());
                            runOnUiThread(() -> {
                                Toast.makeText(EditPerfilAccount.this,
                                        "Erro ao carregar perfil: " + response.code(),
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PerfilKeyValue>> call, Throwable t) {
                        Log.e(TAG, "❌ Falha: " + t.getMessage(), t);
                        runOnUiThread(() -> {
                            Toast.makeText(EditPerfilAccount.this,
                                    "Erro de conexão: " + t.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void setupRecyclerView() {
        perfilAdapter = new PerfilAdapter(perfilList, this, this);
        recyclerViewPerfil.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPerfil.setAdapter(perfilAdapter);
    }

    private void setupListeners() {
        btnVoltar.setOnClickListener(v -> finish());
        btnAddProperty.setOnClickListener(v -> addProperty());
        btnViewPublicKeys.setOnClickListener(v -> showPublicKeysDialog());
        btnGuardar.setOnClickListener(v -> saveAllChanges());
    }

    /**
     * ✅ ADICIONAR PROPRIEDADE
     */
    private void addProperty() {
        String key = editKey.getText().toString().trim();
        String value = editValue.getText().toString().trim();

        // Validação
        if (TextUtils.isEmpty(key)) {
            editKey.setError("Digite a chave");
            editKey.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(value)) {
            editValue.setError("Digite o valor");
            editValue.requestFocus();
            return;
        }

        // Verificar se chave já existe
        for (PerfilKeyValue prop : perfilList) {
            if (prop.getKey().equalsIgnoreCase(key)) {
                showUpdateDialog(prop, value);
                return;
            }
        }

        // Adicionar nova propriedade
        profileManager.addProperty(key, value, new ProfileManager.ProfileCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    PerfilKeyValue newProp = new PerfilKeyValue(key, value);
                    perfilList.add(newProp);
                    perfilAdapter.notifyItemInserted(perfilList.size() - 1);

                    Toast.makeText(EditPerfilAccount.this,
                            "Propriedade adicionada", Toast.LENGTH_SHORT).show();

                    // Limpar campos
                    editKey.setText("");
                    editValue.setText("");
                    editKey.requestFocus();

                    updatePropertyCount();
                    updateEmptyState();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(EditPerfilAccount.this,
                            "Erro: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showUpdateDialog(PerfilKeyValue existingProp, String newValue) {
        new AlertDialog.Builder(this)
                .setTitle("Propriedade Existente")
                .setMessage("A chave '" + existingProp.getKey() +
                        "' já existe com o valor '" + existingProp.getValue() +
                        "'. Deseja atualizar para '" + newValue + "'?")
                .setPositiveButton("Atualizar", (dialog, which) -> {
                    updateProperty(existingProp, newValue);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * ✅ EDITAR PROPRIEDADE (usando novo método updateProperty)
     */
    private void updateProperty(PerfilKeyValue prop, String newValue) {
        profileManager.updateProperty(prop.getKey(), newValue,
                new ProfileManager.ProfileCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            prop.setValue(newValue);
                            perfilAdapter.notifyDataSetChanged();
                            Toast.makeText(EditPerfilAccount.this,
                                    "Propriedade atualizada", Toast.LENGTH_SHORT).show();
                            editKey.setText("");
                            editValue.setText("");
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(EditPerfilAccount.this,
                                    "Erro ao atualizar: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    /**
     * ✅ CALLBACK DO ADAPTER - EDITAR
     * Mostra dialog para editar valor
     */
    @Override
    public void onEditProperty(PerfilKeyValue property, int position) {
        // Criar dialog para editar
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_property, null);
        TextInputEditText inputNewValue = dialogView.findViewById(R.id.inputNewValue);
        inputNewValue.setText(property.getValue());

        new AlertDialog.Builder(this)
                .setTitle("Editar " + property.getKey())
                .setView(dialogView)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String newValue = inputNewValue.getText().toString().trim();
                    if (!TextUtils.isEmpty(newValue)) {
                        updatePropertyAtPosition(property, newValue, position);
                    } else {
                        Toast.makeText(this, "Valor não pode estar vazio", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();

        // Focar no campo e mostrar teclado
        inputNewValue.requestFocus();
    }

    private void updatePropertyAtPosition(PerfilKeyValue property, String newValue, int position) {
        profileManager.updateProperty(property.getKey(), newValue,
                new ProfileManager.ProfileCallback() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> {
                            property.setValue(newValue);
                            perfilAdapter.notifyItemChanged(position);
                            Toast.makeText(EditPerfilAccount.this,
                                    "Propriedade atualizada", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(EditPerfilAccount.this,
                                    "Erro ao atualizar: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
    }

    /**
     * ✅ CALLBACK DO ADAPTER - DELETAR
     */
    @Override
    public void onDeleteProperty(PerfilKeyValue property, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remover Propriedade")
                .setMessage("Deseja remover '" + property.getKey() +
                        " = " + property.getValue() + "'?")
                .setPositiveButton("Remover", (dialog, which) -> {
                    profileManager.removeProperty(property.getKey(),
                            new ProfileManager.ProfileCallback() {
                                @Override
                                public void onSuccess() {
                                    runOnUiThread(() -> {
                                        perfilList.remove(position);
                                        perfilAdapter.notifyItemRemoved(position);
                                        Toast.makeText(EditPerfilAccount.this,
                                                "Propriedade removida", Toast.LENGTH_SHORT).show();
                                        updatePropertyCount();
                                        updateEmptyState();
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(EditPerfilAccount.this,
                                                "Erro: " + error, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showPublicKeysDialog() {
        profileManager.getPublicKeys(new ProfileManager.PublicKeysCallback() {
            @Override
            public void onSuccess(List<String> keys) {
                runOnUiThread(() -> {
                    if (keys == null || keys.isEmpty()) {
                        Toast.makeText(EditPerfilAccount.this,
                                "Nenhuma chave pública disponível",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] keysArray = keys.toArray(new String[0]);
                    new AlertDialog.Builder(EditPerfilAccount.this)
                            .setTitle("Chaves Públicas Disponíveis")
                            .setItems(keysArray, (dialog, which) -> {
                                editKey.setText(keysArray[which]);
                                editValue.requestFocus();
                            })
                            .setNegativeButton("Fechar", null)
                            .show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(EditPerfilAccount.this,
                            "Erro ao carregar chaves: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void saveAllChanges() {
        String nome = editNome.getText().toString().trim();
        String telefone = editTelefone.getText().toString().trim();

        // TODO: Implementar salvamento de dados básicos no servidor

        profileManager.saveProfile();

        Toast.makeText(this, "Perfil atualizado com sucesso",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updatePropertyCount() {
        String countText = perfilList.size() + "";
        textPropertyCount.setText(countText);
    }

    private void updateEmptyState() {
        if (perfilList.isEmpty()) {
            recyclerViewPerfil.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewPerfil.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        editKey.setEnabled(true);
    }
}