package ao.co.isptec.aplm.locationads.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.List;

import ao.co.isptec.aplm.locationads.network.models.Local;
import ao.co.isptec.aplm.locationads.utils.GpsUtils;

/**
 * Rastreador de localização com polling a cada 15 segundos
 *
 * Funcionalidades:
 * - Detecta localização GPS do usuário periodicamente
 * - Calcula qual Local está dentro
 * - Notifica quando entrar/sair de um local
 * - Polling configurável (padrão: 15 segundos)
 */
public class LocationTracker {

    private static final String TAG = "LocationTracker";
    private static final long POLLING_INTERVAL = 15000; // 15 segundos

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private Handler handler;
    private Runnable pollingRunnable;
    private LocationCallback locationCallback;

    private Location lastKnownLocation;
    private Integer currentLocalId = null;
    private List<Local> allLocals;
    private LocationChangeListener listener;

    private boolean isTracking = false;

    /**
     * Interface para receber callbacks de mudança de localização
     */
    public interface LocationChangeListener {
        /**
         * Chamado quando usuário entra em um novo local
         * @param local Local em que entrou
         * @param location Coordenadas GPS atuais
         */
        void onEnteredLocal(Local local, Location location);

        /**
         * Chamado quando usuário sai de um local
         * @param localId ID do local que saiu
         */
        void onExitedLocal(int localId);

        /**
         * Chamado quando localização é atualizada mas não mudou de local
         * @param location Nova localização
         */
        void onLocationUpdated(Location location);

        /**
         * Chamado quando há erro ao obter localização
         * @param error Mensagem de erro
         */
        void onLocationError(String error);
    }

    public LocationTracker(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Define o listener para receber callbacks
     */
    public void setLocationChangeListener(LocationChangeListener listener) {
        this.listener = listener;
    }

    /**
     * Define a lista de locais para verificar
     */
    public void setAllLocals(List<Local> locals) {
        this.allLocals = locals;
        Log.d(TAG, "📍 " + locals.size() + " locais carregados para rastreamento");
    }

    /**
     * Inicia o rastreamento de localização
     */
    public void startTracking() {
        if (isTracking) {
            Log.w(TAG, "⚠️ Rastreamento já está ativo");
            return;
        }

        if (!checkLocationPermission()) {
            Log.e(TAG, "❌ Permissão de localização negada");
            if (listener != null) {
                listener.onLocationError("Permissão de localização negada");
            }
            return;
        }

        isTracking = true;
        Log.d(TAG, "🚀 Iniciando rastreamento (polling: " + POLLING_INTERVAL + "ms)");

        // Configurar polling
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTracking) {
                    requestLocationUpdate();
                    handler.postDelayed(this, POLLING_INTERVAL);
                }
            }
        };

        // Primeira atualização imediata
        requestLocationUpdate();

        // Iniciar polling
        handler.postDelayed(pollingRunnable, POLLING_INTERVAL);
    }

    /**
     * Para o rastreamento de localização
     */
    public void stopTracking() {
        if (!isTracking) {
            return;
        }

        isTracking = false;

        if (pollingRunnable != null) {
            handler.removeCallbacks(pollingRunnable);
        }

        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        Log.d(TAG, "⏹️ Rastreamento parado");
    }

    /**
     * Solicita atualização de localização
     */
    private void requestLocationUpdate() {
        if (!checkLocationPermission()) {
            return;
        }

        try {
            // Tentar obter última localização conhecida primeiro
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            processNewLocation(location);
                        } else {
                            // Se não tem última localização, solicitar nova
                            requestCurrentLocation();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Erro ao obter localização", e);
                        if (listener != null) {
                            listener.onLocationError("Erro ao obter localização: " + e.getMessage());
                        }
                    });

        } catch (SecurityException e) {
            Log.e(TAG, "❌ Erro de permissão", e);
        }
    }

    /**
     * Solicita localização atual em tempo real
     */
    private void requestCurrentLocation() {
        if (!checkLocationPermission()) {
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000 // Intervalo de 5 segundos
        )
                .setMinUpdateIntervalMillis(2000)
                .setMaxUpdateDelayMillis(10000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null && locationResult.getLastLocation() != null) {
                    Location location = locationResult.getLastLocation();
                    processNewLocation(location);

                    // Remover callback após receber localização
                    fusedLocationClient.removeLocationUpdates(this);
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
        } catch (SecurityException e) {
            Log.e(TAG, "❌ Erro de permissão ao solicitar localização", e);
        }
    }

    /**
     * Processa nova localização recebida
     */
    private void processNewLocation(Location location) {
        Log.d(TAG, "📍 Nova localização: Lat=" +
                String.format("%.6f", location.getLatitude()) +
                ", Lng=" + String.format("%.6f", location.getLongitude()) +
                ", Accuracy=" + String.format("%.0f", location.getAccuracy()) + "m");

        lastKnownLocation = location;

        // Verificar em qual local está
        Local nearestLocal = findNearestLocal(location);

        if (nearestLocal != null) {
            Integer newLocalId = nearestLocal.getId();

            // Verificou se mudou de local
            if (currentLocalId == null || !currentLocalId.equals(newLocalId)) {
                // Entrou em novo local
                Integer previousLocalId = currentLocalId;
                currentLocalId = newLocalId;

                Log.d(TAG, "✅ ENTROU NO LOCAL: " + nearestLocal.getNome() + " (ID: " + newLocalId + ")");

                if (listener != null) {
                    listener.onEnteredLocal(nearestLocal, location);
                }

                // Se saiu de um local anterior
                if (previousLocalId != null) {
                    Log.d(TAG, "🚪 Saiu do local anterior (ID: " + previousLocalId + ")");
                    if (listener != null) {
                        listener.onExitedLocal(previousLocalId);
                    }
                }
            } else {
                // Continua no mesmo local
                Log.d(TAG, "📍 Continua no local: " + nearestLocal.getNome());
                if (listener != null) {
                    listener.onLocationUpdated(location);
                }
            }

        } else {
            // Não está em nenhum local cadastrado
            if (currentLocalId != null) {
                Log.d(TAG, "🚪 Saiu do local (ID: " + currentLocalId + ")");

                Integer previousLocalId = currentLocalId;
                currentLocalId = null;

                if (listener != null) {
                    listener.onExitedLocal(previousLocalId);
                }
            } else {
                Log.d(TAG, "📍 Fora de qualquer local cadastrado");
                if (listener != null) {
                    listener.onLocationUpdated(location);
                }
            }
        }
    }

    /**
     * Encontra o local mais próximo dentro do raio
     */
    private Local findNearestLocal(Location userLocation) {
        if (allLocals == null || allLocals.isEmpty()) {
            return null;
        }

        Local nearestLocal = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Local local : allLocals) {
            // Verificar se local tem coordenadas GPS
            if (local.getLatitude() == null || local.getLongitude() == null) {
                continue;
            }

            int raio = local.getRaio() != null ? local.getRaio() : 100; // Raio padrão 100m

            // Calcular distância
            float distance = GpsUtils.calculateDistance(
                    userLocation,
                    local.getLatitude(),
                    local.getLongitude()
            );

            Log.d(TAG, "   Local: " + local.getNome() +
                    " | Distância: " + GpsUtils.formatDistance(distance) +
                    " | Raio: " + raio + "m");

            // Verificar se está dentro do raio
            if (distance <= raio && distance < nearestDistance) {
                nearestLocal = local;
                nearestDistance = distance;
            }
        }

        if (nearestLocal != null) {
            Log.d(TAG, "🎯 Local mais próximo: " + nearestLocal.getNome() +
                    " (" + GpsUtils.formatDistance(nearestDistance) + ")");
        }

        return nearestLocal;
    }

    /**
     * Verifica permissão de localização
     */
    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Obtém ID do local atual
     */
    public Integer getCurrentLocalId() {
        return currentLocalId;
    }

    /**
     * Obtém última localização conhecida
     */
    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    /**
     * Verifica se está rastreando
     */
    public boolean isTracking() {
        return isTracking;
    }

    /**
     * Força atualização imediata
     */
    public void forceUpdate() {
        if (isTracking) {
            Log.d(TAG, "🔄 Forçando atualização de localização...");
            requestLocationUpdate();
        }
    }
}