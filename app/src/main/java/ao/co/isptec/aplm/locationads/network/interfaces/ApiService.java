package ao.co.isptec.aplm.locationads.network.interfaces;

import java.util.List;
import java.util.Map;

import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.models.LoginRequest;
import ao.co.isptec.aplm.locationads.network.models.LoginResponse;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.models.PerfilKeyValue;
import ao.co.isptec.aplm.locationads.network.models.RecoveryRequest;
import ao.co.isptec.aplm.locationads.network.models.RecoveryResponse;
import ao.co.isptec.aplm.locationads.network.models.RegisterRequest;
import ao.co.isptec.aplm.locationads.network.models.UserProfile;
import ao.co.isptec.aplm.locationads.network.models.VerifyEmailRequest;
import ao.co.isptec.aplm.locationads.network.models.SavedAd;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ==================== AUTENTICAÇÃO ====================

    @POST("/auth/register")
    Call<Void> register(@Body RegisterRequest request);

    @POST("/auth/verify-email")
    Call<Void> verifyEmail(@Body VerifyEmailRequest request);

    @POST("/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("/auth/forgot-password")
    Call<RecoveryResponse> sendRecoveryCode(@Body RecoveryRequest request);

    // ==================== LOCAIS ====================

    @POST("/locais")
    Call<Local> addLocal(@Body Local request);

    @GET("/locais")
    Call<List<Local>> getAllLocals();

    @GET("/locais/{id}")
    Call<Local> getLocalById(@Path("id") String id);

    @DELETE("/locais/{id}")
    Call<Void> removeLocalById(@Path("id") String id);

    @GET("/locais/user/{userId}")
    Call<List<Local>> getLocaisByUser(@Path("userId") int userId);

    // ==================== MENSAGENS/ANÚNCIOS ====================

    @POST("messages")
    Call<Ads> addAd(@Body Ads ads);

    @POST("https://backend-aplm-1.onrender.com/messages")
    Call<Ads> addAdAlternative(@Body Ads ads);

    @GET("https://backend-aplm-1.onrender.com/messages")
    Call<List<Ads>> getMessagesByLocation(@Query("localId") int localId);

    @GET("https://backend-aplm-1.onrender.com/messages/whitelist")
    Call<List<Ads>> getAdsWhitelist();

    @GET("https://backend-aplm-1.onrender.com/messages/BLACKlist")
    Call<List<Ads>> getAdsBlacklist();

    @GET("https://backend-aplm-1.onrender.com/messages/my-messages")
    Call<List<Ads>> getMyMessages();

    @GET("https://backend-aplm-1.onrender.com/messages/similar")
    Call<List<Ads>> getSimilarMessages();

    @GET("messages/{id}")
    Call<Ads> getMessageById(@Path("id") int id);

    @GET("messages/notifications")
    Call<List<Ads>> getNotifications();

    // ==================== SALVAR ANÚNCIOS ====================

    @GET("https://backend-aplm-1.onrender.com/messages/saved")
    Call<List<SavedAd>> getSavedMessages();

    @POST("https://backend-aplm-1.onrender.com/messages/{id}/save")
    Call<ResponseBody> saveMessage(@Path("id") int id);

    @DELETE("https://backend-aplm-1.onrender.com/messages/{id}/save")
    Call<ResponseBody> unsaveMessage(@Path("id") int id);

    // ==================== PERFIL (CORRIGIDO) ====================

    /**
     * Obter perfil do usuário
     * GET /usuarios/{userId}/perfil
     * Retorna: List<PerfilKeyValue> (array de {chave, valor})
     */
    @GET("https://backend-aplm-1.onrender.com/usuarios/{userId}/perfil")
    Call<List<PerfilKeyValue>> getUserPerfil(
            @Path("userId") int userId,
            @Header("Authorization") String token
    );

    /**
     * Adicionar nova propriedade ao perfil
     * POST /usuarios/{userId}/perfil
     * Body: {chave: "...", valor: "..."}
     */
    @POST("https://backend-aplm-1.onrender.com/usuarios/{userId}/perfil")
    Call<ResponseBody> addProfileProperty(
            @Path("userId") int userId,
            @Header("Authorization") String token,
            @Body PerfilKeyValue property
    );

    /**
     * Remover propriedade específica do perfil
     * DELETE /usuarios/{userId}/perfil/{chave}
     */
    @DELETE("https://backend-aplm-1.onrender.com/usuarios/{userId}/perfil/{chave}")
    Call<ResponseBody> removeProfileProperty(
            @Path("userId") int userId,
            @Path("chave") String chave,
            @Header("Authorization") String token
    );

    /**
     * Atualizar perfil completo
     * PUT /usuarios/{userId}/perfil
     * Body: [{chave: "...", valor: "..."}, ...]
     */
    @PUT("https://backend-aplm-1.onrender.com/usuarios/{userId}/perfil")
    Call<List<PerfilKeyValue>> updateCompleteProfile(
            @Path("userId") int userId,
            @Header("Authorization") String token,
            @Body List<PerfilKeyValue> properties
    );

    /**
     * Obter lista de chaves públicas disponíveis
     * GET /perfil/chaves
     */
    @GET("https://backend-aplm-1.onrender.com/perfil/chaves")
    Call<List<String>> getPublicKeys(
            @Header("Authorization") String token
    );

    // ==================== FCM TOKEN ====================

    @POST("https://backend-aplm-1.onrender.com/usuarios/fcm-token")
    Call<ResponseBody> saveFcmToken(
            @Header("Authorization") String token,
            @Body Map<String, String> body
    );
}