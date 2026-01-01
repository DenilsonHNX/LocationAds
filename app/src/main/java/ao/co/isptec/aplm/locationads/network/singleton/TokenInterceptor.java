package ao.co.isptec.aplm.locationads.network.singleton;

import android.content.Context;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    private static final String TAG = "TokenInterceptor";
    private Context context;

    /**
     * Construtor que recebe o contexto
     */
    public TokenInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String path = original.url().encodedPath();

        // Rotas que não precisam de autenticação
        if (path.contains("/auth/login") ||
                path.contains("/auth/register") ||
                path.contains("/auth/verify-email") ||
                path.contains("/auth/resend-code") ||
                path.contains("/auth/forgot-password") ||
                path.contains("/auth/reset-password")) {

            Log.d(TAG, "Rota pública, sem token: " + path);
            return chain.proceed(original);
        }

        // Obter token do TokenManager
        TokenManager tokenManager = TokenManager.getInstance(context);
        String token = tokenManager.getToken();

        // Verificar se tem token
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Token não encontrado para rota: " + path);
            return chain.proceed(original);
        }

        // Verificar se o token é válido
        if (!tokenManager.isTokenValid()) {
            Log.w(TAG, "Token expirado, limpando dados");
            tokenManager.clearAndLogout();
            return chain.proceed(original);
        }

        // Adicionar token ao header
        Request.Builder builder = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .method(original.method(), original.body());

        Request request = builder.build();
        Log.d(TAG, "Token adicionado à requisição: " + path);

        // Executar requisição
        Response response = chain.proceed(request);

        // Se retornar 401 (não autorizado), limpar token
        if (response.code() == 401) {
            Log.w(TAG, "Resposta 401 - Token inválido, fazendo logout");
            tokenManager.clearAndLogout();
        }

        return response;
    }
}