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
import ao.co.isptec.aplm.locationads.network.models.LocationUpdate;
import ao.co.isptec.aplm.locationads.network.models.Notificacao;
import ao.co.isptec.aplm.locationads.network.models.MensagemTransito;
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

/**
 * Interface de API para comunicação com o backend AnunciosLoc
 * Backend: NestJS + Prisma + PostgreSQL
 * Base URL: https://backend-aplm-1.onrender.com
 */
public interface ApiService {

    // ==================== AUTENTICAÇÃO ====================

    /**
     * Registar novo utilizador
     * POST /auth/register
     */
    @POST("auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    /**
     * Verificar email com código
     * POST /auth/verify-email
     */
    @POST("auth/verify-email")
    Call<Void> verifyEmail(@Body VerifyEmailRequest request);

    /**
     * Login do utilizador
     * POST /auth/login
     * Retorna JWT token para autenticação
     */
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    /**
     * Recuperar password - enviar código
     * POST /auth/forgot-password
     */
    @POST("auth/forgot-password")
    Call<RecoveryResponse> sendRecoveryCode(@Body RecoveryRequest request);

    /**
     * Atualizar FCM token para notificações push
     * PUT /auth/fcm-token
     */
    @PUT("auth/fcm-token")
    Call<ResponseBody> updateFcmToken(@Body Map<String, String> fcmToken);

    // ==================== LOCAIS ====================

    /**
     * Criar novo local (GPS ou WiFi)
     * POST /locais
     */
    @POST("locais")
    Call<Local> addLocal(@Body Local request);

    /**
     * Listar todos os locais
     * GET /locais
     */
    @GET("locais")
    Call<List<Local>> getAllLocals();

    /**
     * Obter local por ID
     * GET /locais/{id}
     */
    @GET("locais/{id}")
    Call<Local> getLocalById(@Path("id") int id);

    /**
     * Remover local (apenas criador pode remover)
     * DELETE /locais/{id}
     */
    @DELETE("locais/{id}")
    Call<Void> removeLocalById(@Path("id") int id);

    /**
     * Listar locais criados pelo utilizador
     * GET /locais/user/{userId}
     */
    @GET("locais/user/{userId}")
    Call<List<Local>> getLocaisByUser(@Path("userId") int userId);

    // ==================== MENSAGENS/ANÚNCIOS ====================

    /**
     * Criar novo anúncio/mensagem
     * POST /messages
     * Body: { titulo, conteudo, localId, modoEntrega, policy, restricoes, horaInicio, horaFim }
     */
    @POST("messages")
    Call<Ads> createMessage(@Body Ads ads);

    /**
     * Listar anúncios ativos (com paginação)
     * GET /messages?localId={localId}&page={page}&limit={limit}
     */
    @GET("messages")
    Call<List<Ads>> getMessages(
            @Query("localId") Integer localId,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    /**
     * Buscar mensagens por localização
     * GET /messages?localId={localId}
     */
    @GET("messages")
    Call<List<Ads>> getMessagesByLocation(@Query("localId") int localId);

    /**
     * Obter anúncio por ID
     * GET /messages/{id}
     */
    @GET("messages/{id}")
    Call<Ads> getMessageById(@Path("id") int id);

    /**
     * Listar anúncios criados pelo utilizador autenticado
     * GET /messages/my-messages
     */
    @GET("messages/my-messages")
    Call<List<Ads>> getMyMessages();

    /**
     * Remover anúncio (soft-delete, apenas autor)
     * DELETE /messages/{id}
     */
    @DELETE("messages/{id}")
    Call<Void> deleteMessage(@Path("id") int id);

    /**
     * Marcar notificação como recebida
     * POST /messages/{id}/receive
     */
    @POST("messages/{id}/receive")
    Call<ResponseBody> receiveMessage(@Path("id") int id);

    // ==================== WHITELIST/BLACKLIST ====================

    /**
     * Listar anúncios onde o utilizador está na whitelist
     * GET /messages/whitelist?page={page}&limit={limit}
     */
    @GET("messages/whitelist")
    Call<List<Ads>> getAdsWhitelist(
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    /**
     * Listar anúncios onde o utilizador está na whitelist (sem paginação)
     * GET /messages/whitelist
     */
    @GET("messages/whitelist")
    Call<List<Ads>> getAdsWhitelist();

    /**
     * Listar anúncios com política blacklist
     * GET /messages/blacklist?page={page}&limit={limit}
     */
    @GET("messages/blacklist")
    Call<List<Ads>> getAdsBlacklist(
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    /**
     * Listar anúncios blacklist (sem paginação)
     * GET /messages/blacklist
     */
    @GET("messages/blacklist")
    Call<List<Ads>> getAdsBlacklist();

    // ==================== SALVOS/FAVORITOS ====================

    /**
     * Listar mensagens salvas (favoritos)
     * GET /messages/saved?page={page}&limit={limit}
     */
    @GET("messages/saved")
    Call<List<Ads>> getSavedMessages(
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    /**
     * Listar mensagens salvas (sem paginação)
     * GET /messages/saved
     */
    @GET("messages/saved")
    Call<List<Ads>> getSavedMessages();

    /**
     * Salvar mensagem nos favoritos
     * POST /messages/{id}/save
     */
    @POST("messages/{id}/save")
    Call<ResponseBody> saveMessage(@Path("id") int id);

    /**
     * Remover mensagem dos favoritos
     * DELETE /messages/{id}/save
     */
    @DELETE("messages/{id}/save")
    Call<ResponseBody> unsaveMessage(@Path("id") int id);

    // ==================== NOTIFICAÇÕES ====================

    /**
     * Listar notificações do utilizador
     * GET /messages/notifications
     */
    @GET("messages/notifications")
    Call<List<Notificacao>> getNotifications();

    /**
     * Atualizar localização do utilizador (para receber notificações)
     * PUT /messages/update-location
     * Body: { latitude, longitude, wifiIds }
     */
    @PUT("messages/update-location")
    Call<ResponseBody> updateLocation(@Body LocationUpdate locationUpdate);

    // ==================== PERFIL DO UTILIZADOR ====================

    /**
     * Obter perfil do utilizador
     * GET /usuarios/{userId}/perfil
     */
    @GET("usuarios/{userId}/perfil")
    Call<List<PerfilKeyValue>> getUserProfile(@Path("userId") int userId);

    /**
     * Adicionar propriedade ao perfil
     * POST /usuarios/{userId}/perfil
     * Body: { chave, valor }
     */
    @POST("usuarios/{userId}/perfil")
    Call<ResponseBody> addProfileProperty(
            @Path("userId") int userId,
            @Body PerfilKeyValue property
    );

    /**
     * Atualizar perfil completo
     * PUT /usuarios/{userId}/perfil
     * Body: [{ chave, valor }, ...]
     */
    @PUT("usuarios/{userId}/perfil")
    Call<ResponseBody> updateUserProfile(
            @Path("userId") int userId,
            @Body List<PerfilKeyValue> properties
    );

    /**
     * Remover propriedade do perfil
     * DELETE /usuarios/{userId}/perfil/{chave}
     */
    @DELETE("usuarios/{userId}/perfil/{chave}")
    Call<ResponseBody> removeProfileProperty(
            @Path("userId") int userId,
            @Path("chave") String chave
    );

    /**
     * Listar chaves públicas de perfis
     * GET /perfil/chaves
     */
    @GET("perfil/chaves")
    Call<List<String>> getPublicProfileKeys();

    // ==================== SISTEMA DE MULAS ====================

    /**
     * Atribuir anúncio a uma mula
     * POST /mensagens-transito/assign/{anuncioId}/{mulaId}
     */
    @POST("mensagens-transito/assign/{anuncioId}/{mulaId}")
    Call<MensagemTransito> assignMessageToMula(
            @Path("anuncioId") int anuncioId,
            @Path("mulaId") int mulaId
    );

    /**
     * Listar mensagens que a mula está transportando
     * GET /mensagens-transito/mula
     */
    @GET("mensagens-transito/mula")
    Call<List<MensagemTransito>> getMulaMessages();

    /**
     * Marcar mensagem como entregue pela mula
     * PUT /mensagens-transito/deliver/{transitoId}
     */
    @PUT("mensagens-transito/deliver/{transitoId}")
    Call<ResponseBody> deliverMessage(@Path("transitoId") int transitoId);

    /**
     * Listar mensagens disponíveis para entrega em um local
     * GET /mensagens-transito/local/{localId}
     */
    @GET("mensagens-transito/local/{localId}")
    Call<List<MensagemTransito>> getMessagesForLocation(@Path("localId") int localId);
}
