package ao.co.isptec.aplm.locationads.network.singleton;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import ao.co.isptec.aplm.locationads.LocationAdsApp;
import ao.co.isptec.aplm.locationads.LoginActivity;

public class TokenManager {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_EXPIRY = "expiry";

    private static SharedPreferences prefs() {
        Context ctx = LocationAdsApp.getContext();
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveToken(String token, long expiryMillis) {
        SharedPreferences.Editor editor = prefs().edit();
        editor.putString(KEY_TOKEN, token);
        editor.putLong(KEY_EXPIRY, expiryMillis);
        editor.apply();
    }

    public static String getToken() {
        return prefs().getString(KEY_TOKEN, null);
    }

    public static long getExpiry() {
        return prefs().getLong(KEY_EXPIRY, 0);
    }

    public static void clear() {
        SharedPreferences.Editor editor = prefs().edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_EXPIRY);
        editor.apply();
    }

    public static boolean isTokenValid() {
        String token = getToken();
        if (token == null || token.isEmpty()) return false;
        long expiry = getExpiry();
        long now = System.currentTimeMillis();
        return expiry > now;
    }

    public static boolean isLoggedIn() {
        return isTokenValid();
    }

    public static void clearAndLogout() {
        Context ctx = LocationAdsApp.getContext();
        clear();
        Intent intent = new Intent(ctx, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(intent);
    }
}
