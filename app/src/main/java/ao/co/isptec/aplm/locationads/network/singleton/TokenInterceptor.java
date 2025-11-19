package ao.co.isptec.aplm.locationads.network.singleton;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // Get token (may be null when user is not authenticated yet)
        String token = TokenManager.getToken();

        // If there is a token, check validity and act if expired
        if (token != null && !token.isEmpty()) {
            if (!TokenManager.isTokenValid()) {
                // token expired: clear and logout, but still proceed the request
                TokenManager.clearAndLogout();
                return chain.proceed(original);
            }
        }

        Request.Builder builder = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .method(original.method(), original.body());

        Request request = builder.build();
        Response response = chain.proceed(request);

        // If server returns 401, clear token and redirect to login
        if (response.code() == 401) {
            TokenManager.clearAndLogout();
        }

        return response;
    }
}
