package ao.co.isptec.aplm.locationads.network.interfaces;

import java.util.List;

import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.models.LoginRequest;
import ao.co.isptec.aplm.locationads.network.models.LoginResponse;
import ao.co.isptec.aplm.locationads.network.models.RegisterRequest;
import ao.co.isptec.aplm.locationads.network.models.VerifyEmailRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/auth/register")
    Call<Void> register(@Body RegisterRequest request);

    @POST("/auth/verify-email")
    Call<Void> verifyEmail(@Body VerifyEmailRequest request);

    @POST("/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/locais")
    Call<Local> addLocal(@Body Local request);

    @GET("/locais")
    Call<List<Local>> getAllLocals();

    @GET("/locais/{id}")
    Call<Local> getLocalById(@Path("id") String id);

    @DELETE("/locais/{id}")
    Call<Void> removeLocalById(@Path("id") String id);


}