package ao.co.isptec.aplm.locationads.network.singleton;

import android.content.SharedPreferences;

import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
import ao.co.isptec.aplm.locationads.network.singleton.TokenInterceptor;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:3001/api/";
    private static ApiClient instance;
    private Retrofit retrofit;

    private ApiClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new TokenInterceptor())
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
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