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
    @GET("https://backend-aplm-segq.onrender.com/messages")
    Call<List<Ads>> getMessagesByLocation(@Query("localId") int localId);

    /**
     * Buscar minhas mensagens (do usuário autenticado)
     * Endpoint: GET /messages/my-messages
     */
    @GET("https://backend-aplm-segq.onrender.com/messages/my-messages")
    Call<List<Ads>> getMyMessages();

    /**
     * Buscar mensagem específica por ID
     * Endpoint: GET /messages/{id}
     */
    @GET("https://backend-aplm-segq.onrender.com/messages/{id}")
    Call<Ads> getMessageById(@Path("id") int id);

    /**
     * Buscar notificações
     * Endpoint: GET /messages/notifications
     */
    @GET("https://backend-aplm-segq.onrender.com/messages/notifications")
    Call<List<Ads>> getNotifications();

    /**
     * Buscar mensagens salvas (favoritos)
     * Endpoint: GET /messages/saved
     */
    @GET("https://backend-aplm-segq.onrender.com/messages/saved")
    Call<List<Ads>> getSavedMessages();

    /**
     * Salvar mensagem nos favoritos
     * Endpoint: POST /messages/{id}/save
     */
    @POST("https://backend-aplm-segq.onrender.com/messages/{id}/save")
    Call<ResponseBody> saveMessage(@Path("id") int id);

    /**
     * Remover mensagem dos favoritos
     * Endpoint: DELETE /messages/{id}/save
     */
    @DELETE("https://backend-aplm-segq.onrender.com/messages/{id}/save")
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

    // ✅ NOVO: Obter perfil por userId
    @GET("https://backend-aplm-segq.onrender.com/usuarios/{userId}/perfil")
    Call<List<PerfilKeyValue>> getPerfilByUserId(@Path("userId") int userId);

    // ✅ CORRIGIDO: userId na URL (não via Header)
    @POST("https://backend-aplm-segq.onrender.com/usuarios/{userId}/perfil")
    Call<PerfilKeyValue> addProfileProperty(
            @Path("userId") int userId,
            @Body PerfilKeyValue property
    );

    // ✅ NOVO: Atualizar perfil completo
    @PUT("https://backend-aplm-segq.onrender.com/usuarios/{userId}/perfil")
    Call<List<PerfilKeyValue>> updateCompleteProfile();

    // ✅ CORRIGIDO: Remover propriedade
    @DELETE("https://backend-aplm-segq.onrender.com/usuarios/{userId}/perfil/{chave}")
    Call<Void> removeProfileProperty(
            @Path("userId") int userId,
            @Path("chave") String chave
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
    @GET("https://backend-aplm-segq.onrender.com/auth/profile")
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
    @GET("https://backend-aplm-segq.onrender.com/perfil/chaves")
    Call<List<String>> getPublicKeys();

    // Buscar todos os anúncios


}