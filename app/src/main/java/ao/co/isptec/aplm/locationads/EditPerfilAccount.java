package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EditPerfilAccount extends AppCompatActivity {

    private RecyclerView rvKeyValuePairs;
    private Button btnAddKeyValue, btnGuardar;
    private EditText editNome, editEmail, editTelefone;
    private ImageView btnVoltar;
    private Map<String, String> perfilMap = new LinkedHashMap<>();
    private KeyValueAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_perfil_account);

        rvKeyValuePairs = findViewById(R.id.rvKeyValuePairs);
        btnAddKeyValue = findViewById(R.id.btnAddKeyValue);
        btnGuardar = findViewById(R.id.btnGuardar);
        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editTelefone = findViewById(R.id.editTelefone);
        btnVoltar = findViewById(R.id.btnVoltar);

        // Configura RecyclerView
        adapter = new KeyValueAdapter(perfilMap);
        rvKeyValuePairs.setLayoutManager(new LinearLayoutManager(this));
        rvKeyValuePairs.setAdapter(adapter);

        carregarPerfil();

        btnAddKeyValue.setOnClickListener(v -> mostrarDialogNovoPar());

        btnGuardar.setOnClickListener(v -> {
            salvarPerfil();
            Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnVoltar.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        });
    }

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

            // Preencher os campos fixos se existirem
            if(perfilMap.containsKey("nome")) editNome.setText(perfilMap.get("nome"));
            if(perfilMap.containsKey("email")) editEmail.setText(perfilMap.get("email"));
            if(perfilMap.containsKey("telefone")) editTelefone.setText(perfilMap.get("telefone"));

            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void salvarPerfil() {
        perfilMap.put("nome", editNome.getText().toString().trim());
        perfilMap.put("email", editEmail.getText().toString().trim());
        perfilMap.put("telefone", editTelefone.getText().toString().trim());

        JSONObject jsonObject = new JSONObject(perfilMap);
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        prefs.edit().putString("perfil_usuario", jsonObject.toString()).apply();
    }

    private void mostrarDialogNovoPar() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar nova propriedade");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        AutoCompleteTextView inputChave = new AutoCompleteTextView(this);
        inputChave.setHint("Chave");
        layout.addView(inputChave);

        EditText inputValor = new EditText(this);
        inputValor.setHint("Valor");
        layout.addView(inputValor);

        List<String> chavesExistentes = new ArrayList<>(perfilMap.keySet());
        ArrayAdapter<String> adapterChaves = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, chavesExistentes);
        inputChave.setAdapter(adapterChaves);

        builder.setView(layout);

        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            String chave = inputChave.getText().toString().trim();
            String valor = inputValor.getText().toString().trim();
            if (!chave.isEmpty()) {
                perfilMap.put(chave, valor);
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "A chave não pode estar vazia.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
