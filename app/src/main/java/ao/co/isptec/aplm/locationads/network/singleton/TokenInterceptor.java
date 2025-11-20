package ao.co.isptec.aplm.locationads.network.singleton;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        String path = original.url().encodedPath();

        if(path.contains("/auth/login") ||
            path.contains("/auth/register") ||
            path.contains("/auth/verify-email") ||
                path.contains("/auth/resend-code") ||
                path.contains("/auth/forgot-password") ||
                path.contains("/auth/reset-password")) {
            return chain.proceed(original);
        }

        String token = TokenManager.getToken();

        if(token == null || token.isEmpty()) {
            if (!TokenManager.isTokenValid()) {
                TokenManager.clearAndLogout();
                return chain.proceed(original);
            }
            return chain.proceed(original);

        }

        Request.Builder builder = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .method(original.method(), original.body());

        Request request = builder.build();
        Response response = chain.proceed(request);
        // if (response.code() == 401) {
        //    TokenManager.clearAndLogout();
        // }
        return response;
    }
}
