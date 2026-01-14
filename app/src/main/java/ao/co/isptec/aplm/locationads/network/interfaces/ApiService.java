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
import ao.co.isptec.aplm.locationads.network.models.Notificacao;
import ao.co.isptec.aplm.locationads.network.models.MensagemTransito;
import ao.co.isptec.aplm.locationads.network.models.LocationUpdate;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Interface de API para comunicação com o backend LocationAds
 * Backend: NestJS + Prisma + PostgreSQL
 * Base URL: https://backend-aplm-1.onrender.com
 * 
 * Organizado por módulos:
 * - AUTH: Autenticação e gestão de sessão
 * - HEALTH: Verificação de saúde do servidor
 * - LOCATION: Gestão de locais (GPS/WiFi)
 * - MESSAGES: Gestão de anúncios/mensagens
 * - MENSAGENS-TRANSITO: Sistema de mulas (entrega descentralizada)
 * - PERFIL: Gestão de perfil do utilizador
 * - PERFIL PUBLIC: Chaves públicas de perfil
 */
public interface ApiService {

    // ============================================================================
    // AUTH - Endpoints de autenticação
    // ============================================================================

    /**
     * POST /auth/register
     * Registrar novo usuário
     * @param request { nome, email, password }
     * @return LoginResponse com token JWT e dados do utilizador
     */
    @POST("auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    /**
     * POST /auth/login
     * Fazer login e receber token JWT
     * @param request { email, password }
     * @return LoginResponse com token JWT e dados do utilizador
     */
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    /**
     * POST /auth/forgot-password
     * Alterar senha do usuário
     * @param request { email, newPassword } ou { email, code, newPassword }
     * @return RecoveryResponse
     */
    @POST("auth/forgot-password")
    Call<RecoveryResponse> forgotPassword(@Body RecoveryRequest request);

    // Alias para compatibilidade
    @POST("auth/forgot-password")
    Call<RecoveryResponse> sendRecoveryCode(@Body RecoveryRequest request);

    /**
     * GET /auth/profile
     * Obter perfil do usuário autenticado
     * @return UserProfile com dados do utilizador logado
     */
    @GET("auth/profile")
    Call<UserProfile> getAuthProfile();

    /**
     * POST /auth/update-fcm-token
     * Atualizar token FCM do usuário (para notificações push)
     * @param body { "fcmToken": "token_value" }
     * @return ResponseBody
     */
    @POST("auth/update-fcm-token")
    Call<ResponseBody> updateFcmToken(@Body Map<String, String> body);

    /**
     * POST /auth/usuarios/{userId}/fcm-token
     * Salvar token FCM do usuário (compatível com Android)
     * @param userId ID do utilizador
     * @param body { "fcmToken": "token_value" }
     * @return ResponseBody
     */
    @POST("auth/usuarios/{userId}/fcm-token")
    Call<ResponseBody> saveFcmToken(
            @Path("userId") int userId,
            @Body Map<String, String> body
    );

    // ============================================================================
    // HEALTH - Verificação de saúde do servidor
    // ============================================================================

    /**
     * GET /health
     * Verificar se o servidor está funcionando
     * @return ResponseBody com status do servidor
     */
    @GET("health")
    Call<ResponseBody> healthCheck();

    // ============================================================================
    // LOCATION - Gestão de locais
    // ============================================================================

    /**
     * POST /locais
     * Criar novo local (GPS ou WiFi)
     * @param local { nome, tipo, latitude?, longitude?, raio?, wifiIds? }
     * @return Local criado com ID
     */
    @POST("locais")
    Call<Local> createLocal(@Body Local local);

    /**
     * GET /locais
     * Listar todos os locais
     * @return Lista de todos os locais
     */
    @GET("locais")
    Call<List<Local>> getAllLocals();

    /**
     * GET /locais/{id}
     * Obter local por ID
     * @param id ID do local
     * @return Local com os dados
     */
    @GET("locais/{id}")
    Call<Local> getLocalById(@Path("id") int id);

    /**
     * DELETE /locais/{id}
     * Apagar local (apenas o criador pode apagar)
     * @param id ID do local
     * @return Void (204 No Content em sucesso)
     */
    @DELETE("locais/{id}")
    Call<Void> deleteLocal(@Path("id") int id);

    // Alias para manter compatibilidade
    @POST("locais")
    Call<Local> addLocal(@Body Local local);

    @DELETE("locais/{id}")
    Call<Void> removeLocalById(@Path("id") int id);

    // ============================================================================
    // MESSAGES - Gestão de anúncios/mensagens
    // ============================================================================

    /**
     * POST /messages
     * Criar novo anúncio
     * @param ads { titulo, conteudo, localId, modoEntrega?, policy?, restricoes?, horaInicio?, horaFim? }
     * @return Ads criado com ID
     */
    @POST("messages")
    Call<Ads> createMessage(@Body Ads ads);

    /**
     * GET /messages
     * Listar anúncios (com filtros opcionais)
     * @param localId Filtrar por local (opcional)
     * @param page Página (opcional)
     * @param limit Limite por página (opcional)
     * @return Lista de anúncios
     */
    @GET("messages")
    Call<List<Ads>> getMessages(
            @Query("localId") Integer localId,
            @Query("page") Integer page,
            @Query("limit") Integer limit
    );

    /**
     * GET /messages?localId={localId}
     * Buscar mensagens por localização (alias)
     * @param localId ID do local
     * @return Lista de anúncios do local
     */
    @GET("messages")
    Call<List<Ads>> getMessagesByLocation(@Query("localId") int localId);

    /**
     * GET /messages/my-messages
     * Listar anúncios criados pelo utilizador autenticado
     * @return Lista de anúncios do utilizador
     */
    @GET("messages/my-messages")
    Call<List<Ads>> getMyMessages();

    /**
     * GET /messages/all-public
     * Listar TODOS os anúncios (com autenticação, sem filtros)
     * @return Lista de todos os anúncios públicos
     */
    @GET("messages/all-public")
    Call<List<Ads>> getAllPublicMessages();

    /**
     * GET /messages/debug
     * Debug: Ver informações sobre anúncios no banco
     * @return ResponseBody com informações de debug
     */
    @GET("messages/debug")
    Call<ResponseBody> getMessagesDebug();

    /**
     * GET /messages/notifications
     * Listar notificações do utilizador
     * @return Lista de notificações
     */
    @GET("messages/notifications")
    Call<List<Notificacao>> getNotifications();

    /**
     * PUT /messages/update-location
     * Atualizar localização do utilizador (para receber notificações baseadas em localização)
     * @param locationUpdate { latitude, longitude, wifiIds }
     * @return ResponseBody
     */
    @PUT("messages/update-location")
    Call<ResponseBody> updateLocation(@Body LocationUpdate locationUpdate);

    /**
     * GET /messages/saved
     * Listar anúncios salvos nos favoritos
     * @return Lista de anúncios favoritos
     */
    @GET("messages/saved")
    Call<List<Ads>> getSavedMessages();

    /**
     * GET /messages/whitelist
     * Listar anúncios onde o usuário está na whitelist
     * @return Lista de anúncios whitelist
     */
    @GET("messages/whitelist")
    Call<List<Ads>> getWhitelistMessages();

    /**
     * GET /messages/blacklist
     * Listar anúncios onde o usuário está na blacklist
     * @return Lista de anúncios blacklist
     */
    @GET("messages/blacklist")
    Call<List<Ads>> getBlacklistMessages();

    /**
     * GET /messages/similar
     * Listar anúncios similares baseado no perfil do usuário
     * @return Lista de anúncios similares/recomendados
     */
    @GET("messages/similar")
    Call<List<Ads>> getSimilarMessages();

    /**
     * GET /messages/{id}
     * Obter anúncio por ID
     * @param id ID do anúncio
     * @return Anúncio com os dados
     */
    @GET("messages/{id}")
    Call<Ads> getMessageById(@Path("id") int id);

    /**
     * POST /messages/{id}/save
     * Salvar anúncio nos favoritos
     * @param id ID do anúncio
     * @return ResponseBody
     */
    @POST("messages/{id}/save")
    Call<ResponseBody> saveMessage(@Path("id") int id);

    /**
     * DELETE /messages/{id}/save
     * Remover anúncio dos favoritos
     * @param id ID do anúncio
     * @return ResponseBody
     */
    @DELETE("messages/{id}/save")
    Call<ResponseBody> unsaveMessage(@Path("id") int id);

    /**
     * DELETE /messages/{id}
     * Apagar anúncio (apenas o autor pode apagar)
     * @param id ID do anúncio
     * @return Void
     */
    @DELETE("messages/{id}")
    Call<Void> deleteMessage(@Path("id") int id);

    // Aliases para manter compatibilidade
    @GET("messages/whitelist")
    Call<List<Ads>> getAdsWhitelist();

    @GET("messages/blacklist")
    Call<List<Ads>> getAdsBlacklist();

    // ============================================================================
    // MENSAGENS-TRANSITO - Sistema de mulas (entrega descentralizada)
    // ============================================================================

    /**
     * POST /mensagens-transito/assign/{anuncioId}/{mulaId}
     * Atribuir anúncio a uma mula para transporte
     * @param anuncioId ID do anúncio
     * @param mulaId ID do utilizador mula
     * @return MensagemTransito criada
     */
    @POST("mensagens-transito/assign/{anuncioId}/{mulaId}")
    Call<MensagemTransito> assignToMula(
            @Path("anuncioId") int anuncioId,
            @Path("mulaId") int mulaId
    );

    /**
     * GET /mensagens-transito/mula
     * Listar mensagens que a mula autenticada está transportando
     * @return Lista de mensagens em trânsito
     */
    @GET("mensagens-transito/mula")
    Call<List<MensagemTransito>> getMulaMessages();

    /**
     * PUT /mensagens-transito/deliver/{transitoId}
     * Marcar mensagem como entregue
     * @param transitoId ID do trânsito
     * @return ResponseBody
     */
    @PUT("mensagens-transito/deliver/{transitoId}")
    Call<ResponseBody> deliverMessage(@Path("transitoId") int transitoId);

    /**
     * GET /mensagens-transito/local/{localId}
     * Listar mensagens disponíveis para entrega em um local
     * @param localId ID do local
     * @return Lista de mensagens disponíveis
     */
    @GET("mensagens-transito/local/{localId}")
    Call<List<MensagemTransito>> getMessagesForDelivery(@Path("localId") int localId);

    // ============================================================================
    // PERFIL - Gestão de perfil do utilizador
    // ============================================================================

    /**
     * GET /usuarios/{userId}/perfil
     * Obter perfil do usuário
     * @param userId ID do utilizador
     * @return Lista de pares chave-valor do perfil
     */
    @GET("usuarios/{userId}/perfil")
    Call<List<PerfilKeyValue>> getUserProfile(@Path("userId") int userId);

    /**
     * POST /usuarios/{userId}/perfil
     * Adicionar par chave-valor ao perfil do usuário
     * @param userId ID do utilizador
     * @param property { chave, valor }
     * @return ResponseBody
     */
    @POST("usuarios/{userId}/perfil")
    Call<ResponseBody> addProfileProperty(
            @Path("userId") int userId,
            @Body PerfilKeyValue property
    );

    /**
     * PUT /usuarios/{userId}/perfil
     * Atualizar perfil completo do usuário
     * @param userId ID do utilizador
     * @param properties Lista de { chave, valor }
     * @return ResponseBody
     */
    @PUT("usuarios/{userId}/perfil")
    Call<ResponseBody> updateUserProfile(
            @Path("userId") int userId,
            @Body List<PerfilKeyValue> properties
    );

    /**
     * DELETE /usuarios/{userId}/perfil/{chave}
     * Remover par chave-valor do perfil
     * @param userId ID do utilizador
     * @param chave Nome da chave a remover
     * @return ResponseBody
     */
    @DELETE("usuarios/{userId}/perfil/{chave}")
    Call<ResponseBody> removeProfileProperty(
            @Path("userId") int userId,
            @Path("chave") String chave
    );

    // ============================================================================
    // PERFIL PUBLIC - Chaves públicas de perfil
    // ============================================================================

    /**
     * GET /perfil/chaves
     * Listar todas as chaves públicas do sistema
     * @return Lista de nomes de chaves disponíveis
     */
    @GET("perfil/chaves")
    Call<List<String>> getPublicProfileKeys();
}
