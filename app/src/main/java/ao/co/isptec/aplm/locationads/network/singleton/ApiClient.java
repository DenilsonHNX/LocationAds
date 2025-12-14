package ao.co.isptec.aplm.locationads.network.singleton;

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
    private static final String BASE_URL = "https://backend-aplm.onrender.com/api/";

    private static ApiClient instance;
    private Retrofit retrofit;

    private ApiClient() {

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

                // Log do corpo da resposta (cuidado: consome o body)
                if (response.body() != null) {
                    String bodyString = response.body().string();
                    Log.d(TAG, "Response Body: " + bodyString);

                    // Recriar o response body porque foi consumido
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
                .addInterceptor(new TokenInterceptor())  // Seu interceptor de token
                .addInterceptor(loggingInterceptor)      // Logging personalizado
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Configurar Gson para NÃO serializar campos null
        // Isso resolve o problema do "property id should not exist"
        Gson gson = new GsonBuilder()
                .serializeNulls()  // NÃO incluir campos null no JSON
                .setLenient()           // Permite JSON menos rígido
                .create();

        // Configurar Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))  // Usar Gson customizado
                .build();

        Log.d(TAG, "ApiClient inicializado com BASE_URL: " + BASE_URL);
    }

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public ApiService getApiService() {
        return retrofit.create(ApiService.class);
    }
}