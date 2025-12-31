package ao.co.isptec.aplm.locationads.network.interfaces;

import java.util.List;

import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.network.models.LoginRequest;
import ao.co.isptec.aplm.locationads.network.models.LoginResponse;
import ao.co.isptec.aplm.locationads.network.models.Ads;
import ao.co.isptec.aplm.locationads.network.models.PerfilKeyValue;
import ao.co.isptec.aplm.locationads.network.models.RecoveryRequest;
import ao.co.isptec.aplm.locationads.network.models.RecoveryResponse;
import ao.co.isptec.aplm.locationads.network.models.RegisterRequest;
import ao.co.isptec.aplm.locationads.network.models.UploadResponse;
import ao.co.isptec.aplm.locationads.network.models.UserProfile;
import ao.co.isptec.aplm.locationads.network.models.VerifyEmailRequest;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

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

    // ==================== MENSAGENS/ANÚNCIOS ====================

    /**
     * Criar novo anúncio/mensagem
     * Endpoint: POST /messages
     */
    @POST("messages")
    Call<Ads> addAd(@Body Ads ads);

    @POST("https://backend-aplm-segq.onrender.com/messages")
    Call<Ads> addAdAlternative(@Body Ads ads);

    /**
     * Buscar mensagens por localId (OBRIGATÓRIO)
     * Endpoint: GET /messages?localId={localId}
     *
     * ATENÇÃO: O backend EXIGE o parâmetro localId
     */
    @GET("messages")
    Call<List<Ads>> getMessagesByLocation(@Query("localId") int localId);

    /**
     * Buscar minhas mensagens (do usuário autenticado)
     * Endpoint: GET /messages/my-messages
     */
    @GET("messages/my-messages")
    Call<List<Ads>> getMyMessages();

    /**
     * Buscar mensagem específica por ID
     * Endpoint: GET /messages/{id}
     */
    @GET("messages/{id}")
    Call<Ads> getMessageById(@Path("id") int id);

    /**
     * Buscar notificações
     * Endpoint: GET /messages/notifications
     */
    @GET("messages/notifications")
    Call<List<Ads>> getNotifications();

    /**
     * Buscar mensagens salvas (favoritos)
     * Endpoint: GET /messages/saved
     */
    @GET("messages/saved")
    Call<List<Ads>> getSavedMessages();

    /**
     * Salvar mensagem nos favoritos
     * Endpoint: POST /messages/{id}/save
     */
    @POST("messages/{id}/save")
    Call<ResponseBody> saveMessage(@Path("id") int id);

    /**
     * Remover mensagem dos favoritos
     * Endpoint: DELETE /messages/{id}/save
     */
    @DELETE("messages/{id}/save")
    Call<ResponseBody> unsaveMessage(@Path("id") int id);

    @GET("/locais/user/{userId}")
    Call<List<Local>> getLocaisByUser(@Path("userId") int userId);

    @POST("/auth/forgot-password")
    Call<RecoveryResponse> sendRecoveryCode(@Body RecoveryRequest request);


    @POST("profile/add")
    Call<ResponseBody> addProfileProperty(
            @Header("Authorization") String token,
            @Body PerfilKeyValue property
    );

    /**
     * Remover propriedade do perfil do utilizador
     * DELETE /profile/remove/{key}
     */
    @DELETE("profile/remove/{key}")
    Call<ResponseBody> removeProfileProperty(
            @Header("Authorization") String token,
            @Path("key") String key
    );

    /**
     * Obter perfil completo do utilizador
     * GET /profile/get
     */
    @GET("profile/get")
    Call<UserProfile> getUserProfile(
            @Header("Authorization") String token
    );

    /**
     * Atualizar perfil completo do utilizador
     * PUT /profile/update
     */
    @PUT("profile/update")
    Call<ResponseBody> updateUserProfile(
            @Header("Authorization") String token,
            @Body UserProfile profile
    );

    /**
     * Obter lista de todas as chaves públicas
     * GET /profile/public-keys
     */
    @GET("profile/public-keys")
    Call<List<String>> getPublicKeys(
            @Header("Authorization") String token
    );

    // Buscar todos os anúncios


}