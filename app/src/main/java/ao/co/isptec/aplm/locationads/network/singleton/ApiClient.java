package ao.co.isptec.aplm.locationads.network.singleton;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String TAG = "ApiClient";
<<<<<<< HEAD
    private static final String BASE_URL = "https://backend-aplm-1.onrender.com/api/docs/";
=======
    private static final String BASE_URL = "https://backend-aplm-segq.onrender.com/api/docs/";
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31

    private static ApiClient instance;
    private Retrofit retrofit;
    private ApiService apiService;
    private Context context; // ✅ ADICIONAR

    /**
     * Construtor privado com Context
     */
    private ApiClient(Context context) { // ✅ ADICIONAR PARÂMETRO
        this.context = context.getApplicationContext(); // ✅ ADICIONAR
        initializeRetrofit(); // ✅ MOVER LÓGICA PARA MÉTODO SEPARADO
    }

    /**
     * Obter instância do ApiClient (COM Context) - PREFERIDO
     */
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Obter instância do ApiClient (SEM Context)
     * Use apenas se já tiver inicializado antes com getInstance(Context)
     */
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            throw new IllegalStateException(
                    "ApiClient não foi inicializado. " +
                            "Chame ApiClient.getInstance(context) primeiro."
            );
        }
        return instance;
    }

    /**
     * Inicializar Retrofit
     */
    private void initializeRetrofit() {
        // Interceptor de logging
        Interceptor loggingInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                // Log da requisição
                Log.d(TAG, "=== REQUEST ===");
                Log.d(TAG, "URL: " + request.url());
                Log.d(TAG, "Method: " + request.method());
                Log.d(TAG, "Headers: " + request.headers());

                if (request.body() != null) {
                    Log.d(TAG, "Content-Type: " + request.body().contentType());
                }

                // Executar a requisição
                long startTime = System.currentTimeMillis();
                Response response = chain.proceed(request);
                long endTime = System.currentTimeMillis();

                // Log da resposta
                Log.d(TAG, "=== RESPONSE ===");
                Log.d(TAG, "URL: " + response.request().url());
                Log.d(TAG, "Status Code: " + response.code());
                Log.d(TAG, "Time: " + (endTime - startTime) + "ms");
                Log.d(TAG, "Headers: " + response.headers());

                // Log do corpo da resposta
                if (response.body() != null) {
                    String bodyString = response.body().string();
                    Log.d(TAG, "Response Body: " + bodyString);

                    // Recriar o response body
                    ResponseBody newBody = ResponseBody.create(
                            response.body().contentType(),
                            bodyString
                    );
                    response = response.newBuilder().body(newBody).build();
                }

                Log.d(TAG, "===============");

                return response;
            }
        };

        // Configurar OkHttpClient
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor(context))  // ✅ PASSAR CONTEXT
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Configurar Gson
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        // Configurar Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        // Criar ApiService
        apiService = retrofit.create(ApiService.class);

        Log.d(TAG, "ApiClient inicializado com BASE_URL: " + BASE_URL);
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public ApiService getApiService() {
        return apiService;
    }
}