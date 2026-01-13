package ao.co.isptec.aplm.locationads;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.models.PerfilKeyValue;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAds extends AppCompatActivity {

    private static final String TAG = "AddAds";

    // Views - Inputs
    private TextInputEditText inputTitulo;
    private TextInputEditText inputConteudo;
    private TextInputEditText inputHoraInicio;
    private TextInputEditText inputHoraFim;
    private TextInputEditText inputIdadeMinima;
    private AutoCompleteTextView spinnerPolicy;
    private AutoCompleteTextView spinnerLocais;

    // ✅ NOVOS: Tags e Modo Descentralizado
    private LinearLayout tagsContainer;
    private MaterialButton btnAdicionarTag;
    private MaterialCheckBox checkboxDescentralizado;

    // Views - InputLayouts
    private TextInputLayout tituloInputLayout;
    private TextInputLayout conteudoInputLayout;
    private TextInputLayout horaInicioInputLayout;
    private TextInputLayout horaFimInputLayout;
    private TextInputLayout idadeMinimaInputLayout;
    private TextInputLayout policyInputLayout;
    private TextInputLayout localidadeInputLayout;

    // Views - Botões
    private ImageButton btnVoltar;
    private MaterialButton btnPublicar;
    private MaterialButton btnAtualizarLocais;

    // API e dados
    private ApiService apiService;
    private List<Local> locaisList = new ArrayList<>();

    // ✅ NOVOS: Dados do perfil e tags selecionadas
    private List<PerfilKeyValue> perfilChaves = new ArrayList<>();
    private Map<String, String> tagsSelecionadas = new HashMap<>();

    // Estado
    private boolean isLoading = false;
    private Calendar startDateTime = Calendar.getInstance();
    private Calendar endDateTime = Calendar.getInstance();
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ads);

        // ✅ Obter userId
        SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
        userId = sharedPref.getInt("userId", -1);

        initApiService();
        initViews();
        setupListeners();
        setupSpinners();
        carregarLocais();
        carregarChavesPerfil(); // ✅ NOVO
    }

    private void initApiService() {
        apiService = ApiClient.getInstance(this).getApiService();
    }

    private void initViews() {
        // EditTexts
        inputTitulo = findViewById(R.id.inputTitulo);
        inputConteudo = findViewById(R.id.inputConteudo);
        inputHoraInicio = findViewById(R.id.inputHoraInicio);
        inputHoraFim = findViewById(R.id.inputHoraFim);
        inputIdadeMinima = findViewById(R.id.inputIdadeMinima);
        spinnerPolicy = findViewById(R.id.spinnerPolicy);
        spinnerLocais = findViewById(R.id.spinnerLocais);

        // ✅ NOVOS: Tags e Checkbox
        tagsContainer = findViewById(R.id.tagsContainer);
        btnAdicionarTag = findViewById(R.id.btnAdicionarTag);
        checkboxDescentralizado = findViewById(R.id.checkboxDescentralizado);

        // InputLayouts
        tituloInputLayout = findViewById(R.id.tituloInputLayout);
        conteudoInputLayout = findViewById(R.id.conteudoInputLayout);
        horaInicioInputLayout = findViewById(R.id.horaInicioInputLayout);
        horaFimInputLayout = findViewById(R.id.horaFimInputLayout);
        idadeMinimaInputLayout = findViewById(R.id.idadeMinimaInputLayout);
        policyInputLayout = findViewById(R.id.policyInputLayout);
        localidadeInputLayout = findViewById(R.id.localidadeInputLayout);

        // Buttons
        btnVoltar = findViewById(R.id.btnVoltar);
        btnPublicar = findViewById(R.id.btnPublicar);
        btnAtualizarLocais = findViewById(R.id.btnAtualizarLocais);
    }

    private void setupListeners() {
        btnVoltar.setOnClickListener(v -> finish());
        btnPublicar.setOnClickListener(v -> handlePublicarAnuncio());
        btnAtualizarLocais.setOnClickListener(v -> carregarLocais());

        // ✅ NOVO: Adicionar Tag
        btnAdicionarTag.setOnClickListener(v -> mostrarDialogoSelecionarTag());

        // Date/Time pickers
        inputHoraInicio.setOnClickListener(v -> showDateTimePicker(true));
        inputHoraFim.setOnClickListener(v -> showDateTimePicker(false));
    }

    private void setupSpinners() {
        // Spinner de Políticas
        String[] policies = {"WHITELIST", "BLACKLIST"};
        ArrayAdapter<String> policyAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                policies
        );
        spinnerPolicy.setAdapter(policyAdapter);
    }

    /**
     * ✅ CORRIGIDO: Carregar chaves E valores do perfil do backend
     */
    private void carregarChavesPerfil() {
        if (userId == -1) {
            Log.e(TAG, "❌ userId inválido");
            return;
        }

        Log.d(TAG, "🔑 Carregando perfil do usuário ID: " + userId);

        // Obter token
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        if (token.isEmpty()) {
            Log.e(TAG, "❌ Token não encontrado");
            btnAdicionarTag.setEnabled(false);
            return;
        }

        // ✅ CORRIGIDO: userId primeiro, token depois
        apiService.getUserPerfil(userId, "Bearer " + token)
                .enqueue(new Callback<List<PerfilKeyValue>>() {
                    @Override
                    public void onResponse(Call<List<PerfilKeyValue>> call,
                                           Response<List<PerfilKeyValue>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<PerfilKeyValue> perfil = response.body();
                            Log.d(TAG, "✅ Perfil recebido: " + perfil.size() + " propriedades");

                            perfilChaves.clear();
                            perfilChaves.addAll(perfil);

                            // Log de cada chave-valor
                            for (PerfilKeyValue kv : perfil) {
                                Log.d(TAG, "  ✅ " + kv.getKey() + " = " + kv.getValue());
                            }

                            if (perfilChaves.isEmpty()) {
                                Toast.makeText(AddAds.this,
                                        "Você não tem propriedades no perfil. Configure seu perfil primeiro.",
                                        Toast.LENGTH_LONG).show();
                                btnAdicionarTag.setEnabled(false);
                            } else {
                                btnAdicionarTag.setEnabled(true);
                            }
                        } else {
                            Log.e(TAG, "❌ Erro ao carregar perfil: " + response.code());
                            btnAdicionarTag.setEnabled(false);
                            Toast.makeText(AddAds.this,
                                    "Erro ao carregar perfil",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<PerfilKeyValue>> call, Throwable t) {
                        Log.e(TAG, "❌ Falha ao carregar perfil", t);
                        btnAdicionarTag.setEnabled(false);
                        Toast.makeText(AddAds.this,
                                "Erro de conexão ao carregar perfil",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }



    /**
     * ✅ NOVO: Mostrar diálogo para selecionar tag
     */
    private void mostrarDialogoSelecionarTag() {
        if (perfilChaves.isEmpty()) {
            Toast.makeText(this,
                    "Nenhuma chave disponível no perfil",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Criar lista de strings para exibir
        List<String> opcoes = new ArrayList<>();
        for (PerfilKeyValue kv : perfilChaves) {
            // Verificar se já foi selecionada
            if (!tagsSelecionadas.containsKey(kv.getKey())) {
                opcoes.add(kv.getKey() + " = " + kv.getValue());
            }
        }

        if (opcoes.isEmpty()) {
            Toast.makeText(this,
                    "Todas as tags já foram adicionadas",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecionar Tag");
        builder.setItems(opcoes.toArray(new String[0]), (dialog, which) -> {
            PerfilKeyValue selecionada = null;
            int contador = 0;
            for (PerfilKeyValue kv : perfilChaves) {
                if (!tagsSelecionadas.containsKey(kv.getKey())) {
                    if (contador == which) {
                        selecionada = kv;
                        break;
                    }
                    contador++;
                }
            }

            if (selecionada != null) {
                adicionarTagView(selecionada.getKey(), selecionada.getValue());
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    /**
     * ✅ NOVO: Adicionar tag visual no container
     */
    private void adicionarTagView(String chave, String valor) {
        // Adicionar ao mapa
        tagsSelecionadas.put(chave, valor);

        // Inflar layout do chip/tag
        View tagView = LayoutInflater.from(this)
                .inflate(R.layout.item_tag, tagsContainer, false);

        TextView textTag = tagView.findViewById(R.id.textTag);
        ImageButton btnRemoverTag = tagView.findViewById(R.id.btnRemoverTag);

        textTag.setText(chave + ": " + valor);

        // Listener para remover
        btnRemoverTag.setOnClickListener(v -> {
            tagsSelecionadas.remove(chave);
            tagsContainer.removeView(tagView);
            Toast.makeText(this, "Tag removida", Toast.LENGTH_SHORT).show();
        });

        tagsContainer.addView(tagView);

        Log.d(TAG, "✅ Tag adicionada: " + chave + " = " + valor);
    }

    private void carregarLocais() {
        btnAtualizarLocais.setEnabled(false);
        btnAtualizarLocais.setText("Carregando...");

        Log.d(TAG, "========== CARREGANDO LOCAIS ==========");

        apiService.getAllLocals().enqueue(new Callback<List<Local>>() {
            @Override
            public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                btnAtualizarLocais.setEnabled(true);
                btnAtualizarLocais.setText(getString(R.string.update_locations));

                if (response.isSuccessful() && response.body() != null) {
                    locaisList = response.body();
                    Log.d(TAG, "✅ Locais recebidos: " + locaisList.size());

                    if (locaisList.isEmpty()) {
                        Toast.makeText(AddAds.this,
                                "Você precisa criar um local primeiro",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    List<String> locaisNomes = new ArrayList<>();
                    for (Local local : locaisList) {
                        locaisNomes.add(local.getNome());
                    }

                    ArrayAdapter<String> locaisAdapter = new ArrayAdapter<>(
                            AddAds.this,
                            android.R.layout.simple_dropdown_item_1line,
                            locaisNomes
                    );
                    spinnerLocais.setAdapter(locaisAdapter);

                    Toast.makeText(AddAds.this,
                            locaisList.size() + " locais carregados",
                            Toast.LENGTH_SHORT).show();
                } else {
                    handleLocaisError(response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Local>> call, Throwable t) {
                btnAtualizarLocais.setEnabled(true);
                btnAtualizarLocais.setText(getString(R.string.update_locations));
                handleNetworkError(t, "carregar locais");
            }
        });
    }

    private void showDateTimePicker(boolean isStartTime) {
        Calendar calendar = isStartTime ? startDateTime : endDateTime;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);
                                calendar.set(Calendar.SECOND, 0);

                                SimpleDateFormat sdf = new SimpleDateFormat(
                                        "dd/MM/yyyy HH:mm",
                                        Locale.getDefault()
                                );
                                String dateTimeString = sdf.format(calendar.getTime());

                                if (isStartTime) {
                                    inputHoraInicio.setText(dateTimeString);
                                } else {
                                    inputHoraFim.setText(dateTimeString);
                                }
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                    );
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void handlePublicarAnuncio() {
        if (isLoading) return;

        clearErrors();

        Log.d(TAG, "========== PUBLICANDO ANÚNCIO ==========");

        if (userId == -1) {
            Toast.makeText(this, "Usuário não está logado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Obter dados
        String titulo = inputTitulo.getText().toString().trim();
        String conteudo = inputConteudo.getText().toString().trim();
        String horaInicio = inputHoraInicio.getText().toString().trim();
        String horaFim = inputHoraFim.getText().toString().trim();
        String policy = spinnerPolicy.getText().toString().trim();
        String localSelecionado = spinnerLocais.getText().toString().trim();
        String idadeMinimaStr = inputIdadeMinima.getText().toString().trim();

        // ✅ NOVO: Modo descentralizado
        boolean modoDescentralizado = checkboxDescentralizado.isChecked();
        String modoEntrega = modoDescentralizado ? "descentralizado" : "centralizado";

        Log.d(TAG, "Título: " + titulo);
        Log.d(TAG, "Policy: " + policy);
        Log.d(TAG, "Tags: " + tagsSelecionadas.size());
        Log.d(TAG, "Modo Entrega: " + modoEntrega);

        // Validar
        if (!validateInputs(titulo, conteudo, horaInicio, horaFim, policy, localSelecionado)) {
            return;
        }

        // Obter localId
        int localId = getLocalIdByName(localSelecionado);
        if (localId == -1) {
            localidadeInputLayout.setError("Local inválido");
            return;
        }

        // Montar restrições
        Map<String, Object> restricoes = new HashMap<>();
        if (!idadeMinimaStr.isEmpty()) {
            try {
                int idadeMinima = Integer.parseInt(idadeMinimaStr);
                if (idadeMinima < 0 || idadeMinima > 120) {
                    idadeMinimaInputLayout.setError("Idade deve estar entre 0 e 120");
                    return;
                }
                restricoes.put("idadeMinima", idadeMinima);
            } catch (NumberFormatException e) {
                idadeMinimaInputLayout.setError(getString(R.string.error_invalid_age));
                return;
            }
        }

        // Converter datas para ISO
        String horaInicioISO = convertToISOFormat(startDateTime);
        String horaFimISO = convertToISOFormat(endDateTime);

        // ✅ Criar anúncio com TAGS
        criarAnuncioComTags(titulo, conteudo, userId, localId, policy,
                restricoes.isEmpty() ? null : restricoes,
                tagsSelecionadas,
                modoEntrega,
                horaInicioISO, horaFimISO);
    }

    private void criarAnuncioComTags(String titulo, String conteudo, int autorId, int localId,
                                     String policy, Map<String, Object> restricoes,
                                     Map<String, String> tags, String modoEntrega,
                                     String horaInicio, String horaFim) {
        setLoadingState(true);

        // ✅ Criar objeto Ads corretamente
        Ads novoAnuncio = new Ads();
        novoAnuncio.setTitulo(titulo);
        novoAnuncio.setConteudo(conteudo);
        novoAnuncio.setAutorId(autorId);
        novoAnuncio.setLocalId(localId);
        novoAnuncio.setPolicy(policy);
        novoAnuncio.setRestricoes(restricoes);
        novoAnuncio.setTags(tags);              // ✅ NOVO
        novoAnuncio.setModoEntrega(modoEntrega); // ✅ NOVO
        novoAnuncio.setHoraInicio(horaInicio);
        novoAnuncio.setHoraFim(horaFim);
        novoAnuncio.setImagem(null);

        Log.d(TAG, "========== CRIANDO ANÚNCIO ==========");
        Log.d(TAG, "Título: " + titulo);
        Log.d(TAG, "Tags: " + tags);
        Log.d(TAG, "Modo Entrega: " + modoEntrega);

        // ✅ ADICIONAR: Log detalhado das tags
        for (Map.Entry<String, String> entry : tags.entrySet()) {
            Log.d(TAG, "  Tag: " + entry.getKey() + " = " + entry.getValue());
        }

        // ✅ Passar objeto Ads
        apiService.addAdAlternative(novoAnuncio).enqueue(new Callback<Ads>() {
            @Override
            public void onResponse(Call<Ads> call, Response<Ads> response) {
                setLoadingState(false);

                Log.d(TAG, "Status Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ Anúncio criado: ID=" + response.body().getId());

                    String mensagem = tags.isEmpty() ?
                            "Anúncio publicado!" :
                            "Anúncio publicado com " + tags.size() + " tags!";

                    Toast.makeText(AddAds.this, mensagem, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "❌ Erro ao criar anúncio");
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Sem corpo de erro";
                        Log.e(TAG, "Error Body: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody", e);
                    }
                    handleAdCreatedError(response.code());
                }
            }

            @Override
            public void onFailure(Call<Ads> call, Throwable t) {
                setLoadingState(false);
                Log.e(TAG, "❌ Falha", t);
                handleNetworkError(t, "criar anúncio");
            }
        });
    }

    private boolean validateInputs(String titulo, String conteudo, String horaInicio,
                                   String horaFim, String policy, String local) {
        boolean isValid = true;

        if (TextUtils.isEmpty(titulo)) {
            tituloInputLayout.setError(getString(R.string.error_empty_title));
            if (isValid) inputTitulo.requestFocus();
            isValid = false;
        } else if (titulo.length() < 3) {
            tituloInputLayout.setError("Título deve ter pelo menos 3 caracteres");
            if (isValid) inputTitulo.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(conteudo)) {
            conteudoInputLayout.setError(getString(R.string.error_empty_description));
            if (isValid) inputConteudo.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(policy)) {
            policyInputLayout.setError(getString(R.string.error_empty_policy));
            if (isValid) spinnerPolicy.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(horaInicio)) {
            horaInicioInputLayout.setError(getString(R.string.error_empty_start_time));
            if (isValid) inputHoraInicio.requestFocus();
            isValid = false;
        }

        if (TextUtils.isEmpty(horaFim)) {
            horaFimInputLayout.setError(getString(R.string.error_empty_end_time));
            if (isValid) inputHoraFim.requestFocus();
            isValid = false;
        }

        if (!TextUtils.isEmpty(horaInicio) && !TextUtils.isEmpty(horaFim)) {
            if (endDateTime.before(startDateTime)) {
                horaFimInputLayout.setError(getString(R.string.error_end_before_start));
                if (isValid) inputHoraFim.requestFocus();
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(local)) {
            localidadeInputLayout.setError(getString(R.string.error_empty_location));
            if (isValid) spinnerLocais.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private int getLocalIdByName(String nome) {
        for (Local local : locaisList) {
            if (local.getNome().equals(nome)) {
                return local.getId();
            }
        }
        return -1;
    }

    private String convertToISOFormat(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.US
        );
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(calendar.getTime());
    }

    private void handleAdCreatedError(int statusCode) {
        String errorMessage;
        switch (statusCode) {
            case 400:
                errorMessage = "Dados inválidos. Verifique os campos.";
                break;
            case 401:
                errorMessage = "Não autorizado. Faça login novamente.";
                break;
            default:
                errorMessage = "Erro ao criar anúncio";
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void handleLocaisError(int statusCode) {
        Toast.makeText(this, "Erro ao carregar locais", Toast.LENGTH_SHORT).show();
    }

    private void handleNetworkError(Throwable t, String action) {
        String errorMessage = "Erro de conexão ao " + action;
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void clearErrors() {
        tituloInputLayout.setError(null);
        conteudoInputLayout.setError(null);
        policyInputLayout.setError(null);
        horaInicioInputLayout.setError(null);
        horaFimInputLayout.setError(null);
        localidadeInputLayout.setError(null);
        idadeMinimaInputLayout.setError(null);
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        btnPublicar.setEnabled(!loading);
        btnPublicar.setText(loading ? "Publicando..." : getString(R.string.publish_ad));
    }
}