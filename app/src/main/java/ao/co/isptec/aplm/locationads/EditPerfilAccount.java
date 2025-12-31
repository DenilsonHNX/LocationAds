package ao.co.isptec.aplm.locationads;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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
        loadUserProfile();
        setupRecyclerView();
        setupListeners();
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

    private void loadUserProfile() {
        perfilList = new ArrayList<>();
        List<PerfilKeyValue> savedProperties = profileManager.getAllProperties();
        if (savedProperties != null) {
            perfilList.addAll(savedProperties);
        }
        updatePropertyCount();
        updateEmptyState();
    }

    private void setupRecyclerView() {
        perfilAdapter = new PerfilAdapter(perfilList, this, this);
        recyclerViewPerfil.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPerfil.setAdapter(perfilAdapter);
    }

    private void setupListeners() {
        // Botão voltar
        btnVoltar.setOnClickListener(v -> finish());

        // Botão adicionar propriedade
        btnAddProperty.setOnClickListener(v -> addProperty());

        // Botão ver chaves públicas
        btnViewPublicKeys.setOnClickListener(v -> showPublicKeysDialog());

        // Botão guardar
        btnGuardar.setOnClickListener(v -> saveAllChanges());
    }

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

    private void updateProperty(PerfilKeyValue prop, String newValue) {
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

            @Override
            public void onError(String error) {
                onSuccess(); // Continuar mesmo se falhar a remoção
            }
        });
    }

    @Override
    public void onEditProperty(PerfilKeyValue property, int position) {
        editKey.setText(property.getKey());
        editValue.setText(property.getValue());
        editKey.setEnabled(false);

        Toast.makeText(this, "Edite o valor e clique em Adicionar",
                Toast.LENGTH_SHORT).show();
    }

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
        // Salvar dados básicos (nome, telefone)
        String nome = editNome.getText().toString().trim();
        String telefone = editTelefone.getText().toString().trim();

        // TODO: Implementar salvamento de dados básicos no servidor

        // Salvar perfil
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
        // Reabilitar campo de chave ao retornar
        editKey.setEnabled(true);
    }
}