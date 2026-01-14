package ao.co.isptec.aplm.locationads.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;

import ao.co.isptec.aplm.locationads.MainActivity;
import ao.co.isptec.aplm.locationads.R;
import ao.co.isptec.aplm.locationads.network.interfaces.ApiService;
import ao.co.isptec.aplm.locationads.network.models.LocationUpdate;
import ao.co.isptec.aplm.locationads.network.singleton.ApiClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Serviço de rastreamento de localização em background
 * Envia atualizações de localização para o servidor para receber notificações de anúncios
 */
public class LocationTrackingService extends Service {

    private static final String TAG = "LocationTrackingService";
    private static final String CHANNEL_ID = "LocationTrackingChannel";
    private static final int NOTIFICATION_ID = 1;
    
    // Intervalo de atualização de localização (em ms)
    private static final long LOCATION_UPDATE_INTERVAL = 30000; // 30 segundos
    private static final long LOCATION_FASTEST_INTERVAL = 15000; // 15 segundos
    
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private ApiService apiService;
    private WifiManager wifiManager;
    
    private double lastLatitude = 0;
    private double lastLongitude = 0;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate");
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        apiService = ApiClient.getInstance(this).getApiService();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        
        createNotificationChannel();
        setupLocationCallback();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");
        
        // Iniciar como foreground service
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);
        
        // Iniciar atualizações de localização
        startLocationUpdates();
        
        return START_STICKY;
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service onDestroy");
        stopLocationUpdates();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Rastreamento de Localização",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Rastreia sua localização para enviar anúncios relevantes");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
        );
        
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("LocationAds")
                .setContentText("Rastreando localização para anúncios")
                .setSmallIcon(R.drawable.ic_location)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }
    
    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    handleLocationUpdate(location);
                }
            }
        };
    }
    
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "Permissão de localização não concedida");
            stopSelf();
            return;
        }
        
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
                .build();
        
        fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper());
        
        Log.d(TAG, "Location updates started");
    }
    
    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "Location updates stopped");
        }
    }
    
    private void handleLocationUpdate(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        
        // Verificar se mudou significativamente (mais de 10 metros)
        float[] distance = new float[1];
        Location.distanceBetween(lastLatitude, lastLongitude, latitude, longitude, distance);
        
        if (distance[0] > 10 || lastLatitude == 0) {
            lastLatitude = latitude;
            lastLongitude = longitude;
            
            Log.d(TAG, String.format("Nova localização: %.6f, %.6f", latitude, longitude));
            
            // Obter WiFi IDs próximos
            List<String> wifiIds = scanNearbyWifi();
            
            // Enviar para o servidor
            sendLocationToServer(latitude, longitude, wifiIds);
        }
    }
    
    private List<String> scanNearbyWifi() {
        List<String> wifiIds = new ArrayList<>();
        
        if (wifiManager != null && ActivityCompat.checkSelfPermission(this, 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                List<ScanResult> scanResults = wifiManager.getScanResults();
                for (ScanResult result : scanResults) {
                    // Usar BSSID como identificador único
                    wifiIds.add(result.BSSID);
                }
                Log.d(TAG, "WiFi networks encontradas: " + wifiIds.size());
            } catch (Exception e) {
                Log.e(TAG, "Erro ao escanear WiFi", e);
            }
        }
        
        return wifiIds;
    }
    
    private void sendLocationToServer(double latitude, double longitude, List<String> wifiIds) {
        LocationUpdate locationUpdate = new LocationUpdate(latitude, longitude, wifiIds);
        
        apiService.updateLocation(locationUpdate).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, 
                                   @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Localização enviada com sucesso");
                } else {
                    Log.e(TAG, "Erro ao enviar localização: " + response.code());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha ao enviar localização", t);
            }
        });
    }
    
    /**
     * Verificar se o serviço está em execução
     */
    public static boolean isRunning(Context context) {
        android.app.ActivityManager manager = 
                (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningServiceInfo service : 
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LocationTrackingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Iniciar o serviço
     */
    public static void start(Context context) {
        Intent intent = new Intent(context, LocationTrackingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }
    
    /**
     * Parar o serviço
     */
    public static void stop(Context context) {
        Intent intent = new Intent(context, LocationTrackingService.class);
        context.stopService(intent);
    }
}
