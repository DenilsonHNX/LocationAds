package ao.co.isptec.aplm.locationads;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import ao.co.isptec.aplm.locationads.network.singleton.TokenManager;

public class LocationAdsApp extends Application {
    private static final String TAG = "LocationAdsApp";
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Aplicação iniciada");

        ApiClient.getInstance(this);

        Log.d(TAG, "ApiClient inicializado");
    }
}