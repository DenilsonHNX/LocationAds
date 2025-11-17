package ao.co.isptec.aplm.locationads.network.interfaces;

import ao.co.isptec.aplm.locationads.network.models.LoginRequest;
import ao.co.isptec.aplm.locationads.network.models.LoginResponse;
import ao.co.isptec.aplm.locationads.network.models.RegisterRequest;
import ao.co.isptec.aplm.locationads.network.models.VerifyEmailRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
public interface ApiService {
    @POST("/auth/register")
    Call<Void> register(@Body RegisterRequest request);

    @POST("/auth/verify-email")
    Call<Void> verifyEmail(@Body VerifyEmailRequest request);

    @POST("/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}