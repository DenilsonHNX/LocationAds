package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Modelo para atualização de localização do utilizador
 * Usado para: PUT /messages/update-location
 */
public class LocationUpdate {
    
    @SerializedName("latitude")
    private Double latitude;
    
    @SerializedName("longitude")
    private Double longitude;
    
    @SerializedName("wifiIds")
    private List<String> wifiIds;
    
    public LocationUpdate() {}
    
    public LocationUpdate(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    public LocationUpdate(Double latitude, Double longitude, List<String> wifiIds) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.wifiIds = wifiIds;
    }
    
    // Getters e Setters
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public List<String> getWifiIds() {
        return wifiIds;
    }
    
    public void setWifiIds(List<String> wifiIds) {
        this.wifiIds = wifiIds;
    }
}
