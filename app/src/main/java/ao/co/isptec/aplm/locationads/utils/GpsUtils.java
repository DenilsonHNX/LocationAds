package ao.co.isptec.aplm.locationads.utils;

import android.location.Location;

/**
 * Utilitários para cálculo de distância GPS
 *
 * Implementa:
 * - Fórmula de Haversine para distância entre coordenadas
 * - Verificação se usuário está dentro de um raio
 *
 * ONDE COLOCAR:
 * app/src/main/java/ao/co/isptec/aplm/locationads/utils/GpsUtils.java
 */
public class GpsUtils {

    private static final String TAG = "GpsUtils";
    private static final int EARTH_RADIUS_METERS = 6371000; // Raio da Terra em metros

    /**
     * Calcula distância entre duas coordenadas GPS usando Fórmula de Haversine
     *
     * @param lat1 Latitude do ponto 1
     * @param lon1 Longitude do ponto 1
     * @param lat2 Latitude do ponto 2
     * @param lon2 Longitude do ponto 2
     * @return Distância em metros
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Verifica se uma coordenada está dentro do raio de um local
     *
     * @param userLat Latitude do usuário
     * @param userLon Longitude do usuário
     * @param localLat Latitude do local
     * @param localLon Longitude do local
     * @param raio Raio em metros
     * @return true se está dentro do raio
     */
    public static boolean isWithinRadius(double userLat, double userLon,
                                         double localLat, double localLon,
                                         int raio) {
        double distance = calculateDistance(userLat, userLon, localLat, localLon);
        return distance <= raio;
    }

    /**
     * Calcula distância entre duas coordenadas usando Location do Android
     *
     * @param userLocation Localização do usuário
     * @param targetLat Latitude do alvo
     * @param targetLon Longitude do alvo
     * @return Distância em metros
     */
    public static float calculateDistance(Location userLocation, double targetLat, double targetLon) {
        if (userLocation == null) {
            return Float.MAX_VALUE;
        }

        Location targetLocation = new Location("");
        targetLocation.setLatitude(targetLat);
        targetLocation.setLongitude(targetLon);

        return userLocation.distanceTo(targetLocation);
    }

    /**
     * Verifica se Location está dentro do raio
     */
    public static boolean isWithinRadius(Location userLocation,
                                         double targetLat, double targetLon,
                                         int raio) {
        if (userLocation == null) {
            return false;
        }

        float distance = calculateDistance(userLocation, targetLat, targetLon);
        return distance <= raio;
    }

    /**
     * Formata distância para exibição
     */
    public static String formatDistance(double distanceMeters) {
        if (distanceMeters < 1000) {
            return String.format("%.0f m", distanceMeters);
        } else {
            return String.format("%.1f km", distanceMeters / 1000);
        }
    }
}