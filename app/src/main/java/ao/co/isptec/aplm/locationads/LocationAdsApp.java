package ao.co.isptec.aplm.locationads;

import android.app.Application;
import android.content.Context;

public class LocationAdsApp extends Application {
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        // If token is expired at startup, clear it and redirect to login
        try {
            if (!ao.co.isptec.aplm.locationads.network.singleton.TokenManager.isTokenValid()) {
                ao.co.isptec.aplm.locationads.network.singleton.TokenManager.clearAndLogout();
            }
        } catch (Exception ignored) {
            // ignore any issues here to avoid crashing app startup
        }
    }

    public static Context getContext() {
        return appContext;
    }
}
