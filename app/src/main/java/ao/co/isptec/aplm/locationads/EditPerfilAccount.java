package ao.co.isptec.aplm.locationads;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EditPerfilAccount extends AppCompatActivity {

    // Views
    private ImageButton btnVoltar;
    private MaterialButton btnTrocarFoto;
    private MaterialButton btnAddKeyValue;
    private MaterialButton btnGuardar;

    private TextInputEditText editNome;
    private TextInputEditText editEmail;
    private TextInputEditText editTelefone;

    private RecyclerView rvKeyValuePairs;
    private TextView txtTotalPropriedades;

    // Dados
    private Map<String, String> perfilMap = new LinkedHashMap<>();
    private KeyValueAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_perfil_account);

        // Inicializar views
        initViews();

        // Configurar RecyclerView
        setupRecyclerView();

        // Carregar dados do perfil
        carregarPerfil();

        // Configurar listeners
        setupListeners();
    }

    /**
     * Inicializa todas as views
     */
    private void initViews() {
        btnVoltar = findViewById(R.id.btnVoltar);
        btnTrocarFoto = findViewById(R.id.btnTrocarFoto);
        btnAddKeyValue = findViewById(R.id.btnAddKeyValue);
        btnGuardar = findViewById(R.id.btnGuardar);

        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editTelefone = findViewById(R.id.editTelefone);

        rvKeyValuePairs = findViewById(R.id.rvKeyValuePairs);
        txtTotalPropriedades = findViewById(R.id.txtTotalPropriedades);
    }

    /**
     * Configura o RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new KeyValueAdapter(perfilMap);
        rvKeyValuePairs.setLayoutManager(new LinearLayoutManager(this));
        rvKeyValuePairs.setAdapter(adapter);
    }

    /**
     * Configura os listeners
     */
    private void setupListeners() {
        // Botão voltar
        btnVoltar.setOnClickListener(v -> finish());

        // Botão trocar foto
        btnTrocarFoto.setOnClickListener(v -> {
            Toast.makeText(this,
                    "Funcionalidade de trocar foto será implementada em breve",
                    Toast.LENGTH_SHORT).show();
            // TODO: Implementar seleção de foto
        });

        // Botão adicionar propriedade
        btnAddKeyValue.setOnClickListener(v -> mostrarDialogNovoPar());

        // Botão guardar
        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                salvarPerfil();
                Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    /**
     * Carrega o perfil do SharedPreferences
     */
    private void carregarPerfil() {
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String json = prefs.getString("perfil_usuario", "{}");

        try {
            JSONObject jsonObject = new JSONObject(json);
            perfilMap.clear();

            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                perfilMap.put(key, jsonObject.getString(key));
            }

            // Preencher campos básicos
            String nome = prefs.getString("nomeCompleto", "");
            String email = prefs.getString("email", "");
            String telefone = prefs.getString("telefone", "");

            editNome.setText(nome);
            editEmail.setText(email);
            editTelefone.setText(telefone);

            // Atualizar UI
            atualizarContador();
            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar perfil", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Valida os campos obrigatórios
     */
    private boolean validarCampos() {
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String telefone = editTelefone.getText().toString().trim();

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

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("Email inválido");
            editEmail.requestFocus();
            return false;
        }

        if (telefone.isEmpty()) {
            editTelefone.setError("Telefone é obrigatório");
            editTelefone.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Salva o perfil no SharedPreferences
     */
    private void salvarPerfil() {
        // Atualizar dados básicos no mapa
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String telefone = editTelefone.getText().toString().trim();

        perfilMap.put("nome", nome);
        perfilMap.put("email", email);
        perfilMap.put("telefone", telefone);

        // Salvar no SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Salvar campos individuais
        editor.putString("nomeCompleto", nome);
        editor.putString("email", email);
        editor.putString("telefone", telefone);

        // Salvar perfil completo como JSON
        JSONObject jsonObject = new JSONObject(perfilMap);
        editor.putString("perfil_usuario", jsonObject.toString());

        editor.apply();
    }

    /**
     * Mostra dialog para adicionar nova propriedade
     */
    private void mostrarDialogNovoPar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflar layout customizado
        View dialogView = LayoutInflater.from(this).inflate(
                R.layout.dialog_add_property,
                null
        );

        AutoCompleteTextView inputChave = dialogView.findViewById(R.id.inputChave);
        EditText inputValor = dialogView.findViewById(R.id.inputValor);

        // Configurar AutoComplete com chaves existentes
        List<String> chavesExistentes = new ArrayList<>(perfilMap.keySet());
        ArrayAdapter<String> adapterChaves = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                chavesExistentes
        );
        inputChave.setAdapter(adapterChaves);

        builder.setTitle("Adicionar Propriedade")
                .setView(dialogView)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String chave = inputChave.getText().toString().trim();
                    String valor = inputValor.getText().toString().trim();

                    if (chave.isEmpty()) {
                        Toast.makeText(this,
                                "A chave não pode estar vazia",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Adicionar ou atualizar propriedade
                    perfilMap.put(chave, valor);
                    adapter.notifyDataSetChanged();
                    atualizarContador();

                    Toast.makeText(this,
                            "Propriedade adicionada",
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Atualiza o contador de propriedades
     */
    private void atualizarContador() {
        // Contar apenas propriedades adicionais (excluir nome, email, telefone)
        int total = perfilMap.size();
        if (perfilMap.containsKey("nome")) total--;
        if (perfilMap.containsKey("email")) total--;
        if (perfilMap.containsKey("telefone")) total--;

        txtTotalPropriedades.setText(String.valueOf(Math.max(0, total)));
    }

    @Override
    public void onBackPressed() {
        // Perguntar se quer descartar alterações
        new AlertDialog.Builder(this)
                .setTitle("Descartar alterações?")
                .setMessage("As alterações não salvas serão perdidas")
                .setPositiveButton("Descartar", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Continuar editando", null)
                .show();
    }
}