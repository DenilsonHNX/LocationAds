package ao.co.isptec.aplm.locationads;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.loader.content.CursorLoader;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.models.UploadResponse;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAds extends AppCompatActivity {

    private static final String TAG = "AddAds";
    private static final int PICK_IMAGE_REQUEST = 1001;

    // Views - Inputs
    private TextInputEditText inputTitulo;
    private TextInputEditText inputConteudo;
    private TextInputEditText inputHoraInicio;
    private TextInputEditText inputHoraFim;
    private TextInputEditText inputIdadeMinima;
    private AutoCompleteTextView spinnerPolicy;
    private AutoCompleteTextView spinnerLocais;

    // Views - InputLayouts
    private TextInputLayout tituloInputLayout;
    private TextInputLayout conteudoInputLayout;
    private TextInputLayout horaInicioInputLayout;
    private TextInputLayout horaFimInputLayout;
    private TextInputLayout idadeMinimaInputLayout;
    private TextInputLayout policyInputLayout;
    private TextInputLayout localidadeInputLayout;

    // Views - Outros
    private ImageButton btnVoltar;
    private MaterialButton btnPublicar;
    private MaterialButton btnAtualizarLocais;
    private TextView btnEscolherImagem;
    private ImageView imgPreview;
    private LinearLayout placeholderLayout;
    private MaterialCardView imagePreviewCard;

    // API e dados
    private ApiService apiService;
    private Uri imagemUri;
    private String imagemUrl = "";
    private List<Local> locaisList = new ArrayList<>();

    // Estado
    private boolean isLoading = false;
    private Calendar startDateTime = Calendar.getInstance();
    private Calendar endDateTime = Calendar.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ads);

        // Inicializar FORA do WindowInsetsListener
        initApiService();
        initViews();
        setupListeners();
        setupSpinners();
        carregarLocais();
    }
        private void initApiService() {
            apiService = ApiClient.getInstance().getApiService();
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

            // InputLayouts
            tituloInputLayout = findViewById(R.id.tituloInputLayout);
            conteudoInputLayout = findViewById(R.id.conteudoInputLayout);
            horaInicioInputLayout = findViewById(R.id.horaInicioInputLayout);
            horaFimInputLayout = findViewById(R.id.horaFimInputLayout);
            idadeMinimaInputLayout = findViewById(R.id.idadeMinimaInputLayout);
            policyInputLayout = findViewById(R.id.policyInputLayout);
            localidadeInputLayout = findViewById(R.id.localidadeInputLayout);

            // Buttons e outros
            btnVoltar = findViewById(R.id.btnVoltar);
            btnPublicar = findViewById(R.id.btnPublicar);
            btnAtualizarLocais = findViewById(R.id.btnAtualizarLocais);
            btnEscolherImagem = findViewById(R.id.btnEscolherImagem);
            imgPreview = findViewById(R.id.imgPreview);
            placeholderLayout = findViewById(R.id.placeholderLayout);
            imagePreviewCard = findViewById(R.id.imagePreviewCard);
        }

        private void setupListeners() {
            btnVoltar.setOnClickListener(v -> finish());
            btnPublicar.setOnClickListener(v -> handlePublicarAnuncio());
            btnAtualizarLocais.setOnClickListener(v -> carregarLocais());
            btnEscolherImagem.setOnClickListener(v -> abrirGaleria());
            imagePreviewCard.setOnClickListener(v -> abrirGaleria());

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

        private void carregarLocais() {
            SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
            int userId = sharedPref.getInt("userId", -1);

            if (userId == -1) {
                Toast.makeText(this, "Usuário não está logado", Toast.LENGTH_SHORT).show();
                return;
            }

            btnAtualizarLocais.setEnabled(false);
            btnAtualizarLocais.setText("Carregando...");

            apiService.getLocaisByUser(userId).enqueue(new Callback<List<Local>>() {
                @Override
                public void onResponse(Call<List<Local>> call, Response<List<Local>> response) {
                    btnAtualizarLocais.setEnabled(true);
                    btnAtualizarLocais.setText(getString(R.string.update_locations));

                    if (response.isSuccessful() && response.body() != null) {
                        locaisList = response.body();

                        if (locaisList.isEmpty()) {
                            Toast.makeText(AddAds.this,
                                    "Você precisa criar um local primeiro",
                                    Toast.LENGTH_LONG).show();
                            localidadeInputLayout.setHelperText("Nenhum local disponível. Crie um local primeiro.");
                            return;
                        }

                        // Criar lista de nomes para o Spinner
                        List<String> locaisNomes = new ArrayList<>();
                        for (Local local : locaisList) {
                            locaisNomes.add(local.getNome());
                        }

                        // Configurar adapter do Spinner
                        ArrayAdapter<String> locaisAdapter = new ArrayAdapter<>(
                                AddAds.this,
                                android.R.layout.simple_dropdown_item_1line,
                                locaisNomes
                        );
                        spinnerLocais.setAdapter(locaisAdapter);

                        Toast.makeText(AddAds.this,
                                getString(R.string.locations_updated),
                                Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "Locais carregados: " + locaisList.size());

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

        private void handleLocaisError(int statusCode) {
            String errorMessage;

            switch (statusCode) {
                case 404:
                    errorMessage = getString(R.string.no_locations_found);
                    break;
                case 401:
                    errorMessage = "Não autorizado. Faça login novamente.";
                    break;
                default:
                    errorMessage = "Erro ao carregar locais";
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Erro ao carregar locais. Status: " + statusCode);
        }

        private void showDateTimePicker(boolean isStartTime) {
            Calendar calendar = isStartTime ? startDateTime : endDateTime;

            // Date Picker
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Time Picker
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                this,
                                (timeView, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);
                                    calendar.set(Calendar.SECOND, 0);

                                    // Formatar e exibir
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

            // Recupera autorId
            SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
            int autorId = sharedPref.getInt("userId", -1);

            if (autorId == -1) {
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

            // Converter datas para ISO format
            String horaInicioISO = convertToISOFormat(startDateTime);
            String horaFimISO = convertToISOFormat(endDateTime);

            // Upload da imagem ou criar anúncio
            if (imagemUri != null) {
                uploadImagem(imagemUri, new ImageCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        imagemUrl = imageUrl;
                        criarAnuncio(titulo, conteudo, autorId, localId, policy,
                                restricoes, imagemUrl, horaInicioISO, horaFimISO);
                    }

                    @Override
                    public void onError(Throwable error) {
                        setLoadingState(false);
                        Toast.makeText(AddAds.this,
                                "Erro ao fazer upload da imagem: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                // Verificar se deve ter imagem
                if (imagemUrl.isEmpty()) {
                    Toast.makeText(this, getString(R.string.error_no_image),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                criarAnuncio(titulo, conteudo, autorId, localId, policy,
                        restricoes, imagemUrl, horaInicioISO, horaFimISO);
            }
        }

        private boolean validateInputs(String titulo, String conteudo, String horaInicio,
                String horaFim, String policy, String local) {
            boolean isValid = true;

            // Validar título
            if (TextUtils.isEmpty(titulo)) {
                tituloInputLayout.setError(getString(R.string.error_empty_title));
                if (isValid) inputTitulo.requestFocus();
                isValid = false;
            } else if (titulo.length() < 3) {
                tituloInputLayout.setError("Título deve ter pelo menos 3 caracteres");
                if (isValid) inputTitulo.requestFocus();
                isValid = false;
            }

            // Validar conteúdo
            if (TextUtils.isEmpty(conteudo)) {
                conteudoInputLayout.setError(getString(R.string.error_empty_description));
                if (isValid) inputConteudo.requestFocus();
                isValid = false;
            } else if (conteudo.length() < 10) {
                conteudoInputLayout.setError("Descrição deve ter pelo menos 10 caracteres");
                if (isValid) inputConteudo.requestFocus();
                isValid = false;
            }

            // Validar política
            if (TextUtils.isEmpty(policy)) {
                policyInputLayout.setError(getString(R.string.error_empty_policy));
                if (isValid) spinnerPolicy.requestFocus();
                isValid = false;
            }

            // Validar hora início
            if (TextUtils.isEmpty(horaInicio)) {
                horaInicioInputLayout.setError(getString(R.string.error_empty_start_time));
                if (isValid) inputHoraInicio.requestFocus();
                isValid = false;
            }

            // Validar hora fim
            if (TextUtils.isEmpty(horaFim)) {
                horaFimInputLayout.setError(getString(R.string.error_empty_end_time));
                if (isValid) inputHoraFim.requestFocus();
                isValid = false;
            }

            // Validar que hora fim é depois de hora início
            if (!TextUtils.isEmpty(horaInicio) && !TextUtils.isEmpty(horaFim)) {
                if (endDateTime.before(startDateTime)) {
                    horaFimInputLayout.setError(getString(R.string.error_end_before_start));
                    if (isValid) inputHoraFim.requestFocus();
                    isValid = false;
                }
            }

            // Validar local
            if (TextUtils.isEmpty(local)) {
                localidadeInputLayout.setError(getString(R.string.error_empty_location));
                if (isValid) spinnerLocais.requestFocus();
                isValid = false;
            }

            // Validar se há locais disponíveis
            if (locaisList.isEmpty()) {
                Toast.makeText(this, "Nenhum local disponível. Crie um local primeiro.",
                        Toast.LENGTH_LONG).show();
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
                    Locale.getDefault()
            );
            return sdf.format(calendar.getTime());
        }

        private void criarAnuncio(String titulo, String conteudo, int autorId, int localId,
        String policy, Map<String, Object> restricoes,
                String imagemUrl, String horaInicio, String horaFim) {
            setLoadingState(true);

            Ads novoAnuncio = new Ads(
                    titulo,
                    conteudo,
                    autorId,
                    localId,
                    policy,
                    restricoes,
                    imagemUrl,
                    horaInicio,
                    horaFim
            );

            Log.d(TAG, "Criando anúncio: " + titulo);

            apiService.addAd(novoAnuncio).enqueue(new Callback<Ads>() {
                @Override
                public void onResponse(Call<Ads> call, Response<Ads> response) {
                    setLoadingState(false);

                    if (response.isSuccessful()) {
                        handleAdCreatedSuccess();
                    } else {
                        handleAdCreatedError(response.code());
                    }
                }

                @Override
                public void onFailure(Call<Ads> call, Throwable t) {
                    setLoadingState(false);
                    handleNetworkError(t, "criar anúncio");
                }
            });
        }

        private void handleAdCreatedSuccess() {
            Log.d(TAG, "Anúncio criado com sucesso");
            Toast.makeText(this, getString(R.string.ad_created_success),
                    Toast.LENGTH_SHORT).show();
            finish();
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
                case 500:
                    errorMessage = "Erro no servidor. Tente novamente mais tarde.";
                    break;
                default:
                    errorMessage = getString(R.string.error_creating_ad);
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Erro ao criar anúncio. Status: " + statusCode);
        }

        private void handleNetworkError(Throwable t, String action) {
            String errorMessage;

            if (t instanceof java.net.UnknownHostException) {
                errorMessage = "Sem conexão com a internet";
            } else if (t instanceof java.net.SocketTimeoutException) {
                errorMessage = "Tempo de conexão esgotado";
            } else {
                errorMessage = "Erro de conexão ao " + action + ": " + t.getMessage();
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            Log.e(TAG, "Erro de rede ao " + action, t);
        }

        private void abrirGaleria() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                    && data != null && data.getData() != null) {
                imagemUri = data.getData();

                // Exibir imagem na preview
                imgPreview.setImageURI(imagemUri);
                placeholderLayout.setVisibility(View.GONE);
                imgPreview.setVisibility(View.VISIBLE);

                Toast.makeText(this, "Imagem selecionada", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Imagem selecionada: " + imagemUri.toString());
            }
        }

        private void uploadImagem(Uri imageUri, ImageCallback callback) {
            setLoadingState(true);

            try {
                File file = new File(getRealPathFromURI(imageUri));

                RequestBody requestFile = RequestBody.create(
                        MediaType.parse("image/*"),
                        file
                );
                MultipartBody.Part body = MultipartBody.Part.createFormData(
                        "imagem",
                        file.getName(),
                        requestFile
                );

                apiService.uploadImage(body).enqueue(new Callback<UploadResponse>() {
                    @Override
                    public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            String imageUrl = response.body().getUrl();
                            Log.d(TAG, "Upload bem-sucedido: " + imageUrl);
                            callback.onSuccess(imageUrl);
                        } else {
                            setLoadingState(false);
                            callback.onError(new Exception("Falha no upload: " + response.code()));
                        }
                    }

                    @Override
                    public void onFailure(Call<UploadResponse> call, Throwable t) {
                        setLoadingState(false);
                        callback.onError(t);
                    }
                });
            } catch (Exception e) {
                setLoadingState(false);
                callback.onError(e);
                Log.e(TAG, "Erro ao preparar upload", e);
            }
        }

        private String getRealPathFromURI(Uri contentUri) {
            String[] proj = { MediaStore.Images.Media.DATA };
            CursorLoader loader = new CursorLoader(
                    this,
                    contentUri,
                    proj,
                    null,
                    null,
                    null
            );
            Cursor cursor = loader.loadInBackground();

            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                String result = cursor.getString(column_index);
                cursor.close();
                return result;
            }

            return null;
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
            inputTitulo.setEnabled(!loading);
            inputConteudo.setEnabled(!loading);
            inputHoraInicio.setEnabled(!loading);
            inputHoraFim.setEnabled(!loading);
            inputIdadeMinima.setEnabled(!loading);
            spinnerPolicy.setEnabled(!loading);
            spinnerLocais.setEnabled(!loading);
            btnAtualizarLocais.setEnabled(!loading);
            btnEscolherImagem.setEnabled(!loading);

            if (loading) {
                btnPublicar.setText("Publicando...");
            } else {
                btnPublicar.setText(getString(R.string.publish_ad));
            }
        }

        public interface ImageCallback {
            void onSuccess(String result);
            void onError(Throwable error);
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            // Limpar referências
            apiService = null;
            imagemUri = null;
        }
    }