package ao.co.isptec.aplm.locationads.network.singleton;

import android.content.Context;
import android.content.SharedPreferences;

import ao.co.isptec.aplm.locationads.LocationAdsApp;

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

    public static boolean isLoggedIn() {
        String t = getToken();
        return t != null && !t.isEmpty();
    }
}
