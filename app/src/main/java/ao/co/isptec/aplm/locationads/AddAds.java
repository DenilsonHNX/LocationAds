package ao.co.isptec.aplm.locationads;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.loader.content.CursorLoader;

import java.io.File;
import java.util.HashMap;
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
    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri imagemUri;
    private ApiService apiService;
    private Spinner spinnerLocais, spinnerPolicy;

    private EditText inputTitulo, inputConteudo, inputHoraInicio, inputHoraFim, inputIdadeMinima;
    private TextView btnEscolherImagem;
    private String imagemUrl = ""; // valor a ser atribuído quando o usuário selecionar imagem

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_ads);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        apiService = ApiClient.getInstance().getApiService();

        spinnerLocais = findViewById(R.id.spinnerLocais);
        spinnerPolicy = findViewById(R.id.spinnerPolicy);

        inputTitulo = findViewById(R.id.inputTitulo);
        inputConteudo = findViewById(R.id.inputConteudo);
        inputHoraInicio = findViewById(R.id.inputHoraInicio);
        inputHoraFim = findViewById(R.id.inputHoraFim);
        inputIdadeMinima = findViewById(R.id.inputIdadeMinima);

        btnEscolherImagem = findViewById(R.id.btnEscolherImagem);
        btnEscolherImagem.setOnClickListener(v -> {
            abrirGaleria();
        });

        Button btnPublicar = findViewById(R.id.btnPublicar);
        btnPublicar.setOnClickListener(v -> {

            // Recupera autorId do SharedPreferences
            SharedPreferences sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE);
            int autorId = sharedPref.getInt("userId", -1);
            if (autorId == -1) {
                Toast.makeText(this, "Usuário não está logado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verifica seleção do local
            Object localSelecionadoObj = spinnerLocais.getSelectedItem();
            if (localSelecionadoObj == null) {
                Toast.makeText(this, "Por favor, selecione um local", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pega valores dos inputs
            String titulo = inputTitulo.getText().toString().trim();
            String conteudo = inputConteudo.getText().toString().trim();
            String horaInicio = inputHoraInicio.getText().toString().trim();
            String horaFim = inputHoraFim.getText().toString().trim();

            if (titulo.isEmpty() || conteudo.isEmpty() || horaInicio.isEmpty() || horaFim.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
                return;
            }

            String policy = spinnerPolicy.getSelectedItem().toString();

            Map<String, Object> restricoes = new HashMap<>();
            String idadeMinimaStr = inputIdadeMinima.getText().toString().trim();
            if (!idadeMinimaStr.isEmpty()) {
                try {
                    int idadeMinima = Integer.parseInt(idadeMinimaStr);
                    restricoes.put("idadeMinima", idadeMinima);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Idade mínima inválida", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String localNome = "nome";

            // Se precisar, validar imagemUrl aqui, ou colocar URL default
            if (imagemUrl.isEmpty()) {
                imagemUrl = "https://cdn-icons-png.flaticon.com/512/9584/9584876.png"; // placeholder
            }

            Ads novoAnuncio = new Ads(
                    titulo,
                    conteudo,
                    autorId,
                    localNome,
                    policy,
                    restricoes,
                    imagemUrl,
                    horaInicio,
                    horaFim
            );

            apiService.addAd(novoAnuncio).enqueue(new retrofit2.Callback<Ads>() {
                @Override
                public void onResponse(Call<Ads> call, Response<Ads> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddAds.this, "Anúncio criado com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddAds.this, "Erro ao criar anúncio", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Ads> call, Throwable t) {
                    Toast.makeText(AddAds.this, "Erro: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        });

        ImageView btnVoltar = findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            finish();
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            // Exiba imagem na preview
            ImageView imgPreview = findViewById(R.id.imgPreview);
            imgPreview.setImageURI(imageUri);

            // Salve Uri ou caminho para uso posterior
            imagemUri = imageUri;
        }
    }

    private void uploadImagem(Uri imageUri, Callback<String> callback) {
        // Obtém o caminho absoluto do arquivo a partir do Uri
        File file = new File(getRealPathFromURI(imageUri));

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("imagem", file.getName(), requestFile);

        ApiService apiService = ApiClient.getInstance().getApiService();
        Call<UploadResponse> call = apiService.uploadImage(body);
        call.enqueue(new retrofit2.Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = response.body().getUrl();
                    callback.onSuccess(imageUrl);
                } else {
                    callback.onError(new Exception("Falha no upload"));
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                callback.onError(t);
            }
        });
    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Throwable error);
    }

}