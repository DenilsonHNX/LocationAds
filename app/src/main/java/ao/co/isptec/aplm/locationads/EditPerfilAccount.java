package ao.co.isptec.aplm.locationads;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
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
import ao.co.isptec.aplm.locationads.network.singleton.ProfileManager;

public class EditPerfilAccount extends AppCompatActivity implements PerfilAdapter.OnPropertyActionListener {

    private static final String TAG = "EditPerfilAccount";

    // Views - Campos básicos
    private TextInputEditText editNome;
    private TextInputEditText editEmail;
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
        loadUserData();
        loadUserProfile();
        setupRecyclerView();
        setupListeners();
    }

    /**
     * Inicializar views
     */
    private void initViews() {
        // Campos básicos
        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
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
     * Carregar dados básicos do usuário (nome, email, telefone)
     */
    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);

        String nome = prefs.getString("nomeCompleto", "");
        String email = prefs.getString("email", "");
        String telefone = prefs.getString("telefone", "");

        editNome.setText(nome);
        editEmail.setText(email);
        editTelefone.setText(telefone);
    }

    /**
     * Carregar perfil (propriedades) do usuário
     */
    private void loadUserProfile() {
        perfilList = new ArrayList<>();
        List<PerfilKeyValue> savedProperties = profileManager.getAllProperties();
        if (savedProperties != null) {
            perfilList.addAll(savedProperties);
            Log.d(TAG, "✅ Propriedades carregadas: " + perfilList.size());
        }
        updatePropertyCount();
        updateEmptyState();
    }

    /**
     * Configurar RecyclerView
     */
    private void setupRecyclerView() {
        perfilAdapter = new PerfilAdapter(perfilList, this, this);
        recyclerViewPerfil.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPerfil.setAdapter(perfilAdapter);
    }

    /**
     * Configurar listeners
     */
    private void setupListeners() {
        // Botão voltar
        btnVoltar.setOnClickListener(v -> onBackPressed());

        // Botão adicionar propriedade
        btnAddProperty.setOnClickListener(v -> addProperty());

        // Botão ver chaves públicas
        btnViewPublicKeys.setOnClickListener(v -> showPublicKeysDialog());

        // Botão guardar
        btnGuardar.setOnClickListener(v -> saveAllChanges());
    }

    /**
     * Adicionar propriedade
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
        Log.d(TAG, "Adicionando propriedade: " + key + " = " + value);

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

                    Log.d(TAG, "✅ Propriedade adicionada com sucesso");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(EditPerfilAccount.this,
                            "Erro: " + error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "❌ Erro ao adicionar propriedade: " + error);
                });
            }
        });
    }

    /**
     * Mostrar dialog de atualização
     */
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
     * Atualizar propriedade existente
     */
    private void updateProperty(PerfilKeyValue prop, String newValue) {
        Log.d(TAG, "Atualizando propriedade: " + prop.getKey() + " = " + newValue);

        profileManager.removeProperty(prop.getKey(), new ProfileManager.ProfileCallback() {
            @Override
            public void onSuccess() {
                profileManager.addProperty(prop.getKey(), newValue,
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

                                    Log.d(TAG, "✅ Propriedade atualizada com sucesso");
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> {
                                    Toast.makeText(EditPerfilAccount.this,
                                            "Erro ao atualizar: " + error, Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "❌ Erro ao atualizar propriedade: " + error);
                                });
                            }
                        });
            }

            @Override
            public void onError(String error) {
                onSuccess(); // Continuar mesmo se falhar a remoção
            }
        });
    }

    /**
     * Callback: Editar propriedade
     */
    @Override
    public void onEditProperty(PerfilKeyValue property, int position) {
        editKey.setText(property.getKey());
        editValue.setText(property.getValue());
        editKey.setEnabled(false);

        Toast.makeText(this, "Edite o valor e clique em Adicionar",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Callback: Deletar propriedade
     */
    @Override
    public void onDeleteProperty(PerfilKeyValue property, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remover Propriedade")
                .setMessage("Deseja remover '" + property.getKey() +
                        " = " + property.getValue() + "'?")
                .setPositiveButton("Remover", (dialog, which) -> {
                    Log.d(TAG, "Removendo propriedade: " + property.getKey());

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

                                        Log.d(TAG, "✅ Propriedade removida com sucesso");
                                    });
                                }

                                @Override
                                public void onError(String error) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(EditPerfilAccount.this,
                                                "Erro: " + error, Toast.LENGTH_SHORT).show();
                                        Log.e(TAG, "❌ Erro ao remover propriedade: " + error);
                                    });
                                }
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Mostrar dialog com chaves públicas
     */
    private void showPublicKeysDialog() {
        Log.d(TAG, "Carregando chaves públicas...");

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

                    Log.d(TAG, "✅ Chaves públicas carregadas: " + keys.size());

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
                    Log.e(TAG, "❌ Erro ao carregar chaves: " + error);
                });
            }
        });
    }

    /**
     * Salvar todas as alterações
     */
    private void saveAllChanges() {
        // Validar campos básicos
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String telefone = editTelefone.getText().toString().trim();

        if (!validateBasicFields(nome, email, telefone)) {
            return;
        }

        // Salvar dados básicos
        saveBasicData(nome, email, telefone);

        // Salvar perfil (propriedades)
        profileManager.saveProfile();

        Log.d(TAG, "✅ Perfil salvo com sucesso");
        Toast.makeText(this, "Perfil atualizado com sucesso",
                Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Validar campos básicos
     */
    private boolean validateBasicFields(String nome, String email, String telefone) {
        if (nome.isEmpty()) {
            editNome.setError("Nome é obrigatório");
            editNome.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            editEmail.setError("Email é obrigatório");
            editEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Email inválido");
            editEmail.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Salvar dados básicos no SharedPreferences
     */
    private void saveBasicData(String nome, String email, String telefone) {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit()
                .putString("nomeCompleto", nome)
                .putString("email", email)
                .putString("telefone", telefone)
                .apply();

        Log.d(TAG, "Dados básicos salvos no SharedPreferences");
    }

    /**
     * Atualizar contador de propriedades
     */
    private void updatePropertyCount() {
        String countText = perfilList.size() + "";
        textPropertyCount.setText(countText);
    }

    /**
     * Atualizar estado vazio
     */
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
    public void onBackPressed() {
        if (hasUnsavedChanges()) {
            new AlertDialog.Builder(this)
                    .setTitle("Alterações não salvas")
                    .setMessage("Tem alterações não guardadas. Deseja sair sem guardar?")
                    .setPositiveButton("Sair", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Verificar se há alterações não salvas
     */
    private boolean hasUnsavedChanges() {
        // TODO: Implementar lógica real de detecção de mudanças
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reabilitar campo de chave ao retornar
        editKey.setEnabled(true);
    }
}