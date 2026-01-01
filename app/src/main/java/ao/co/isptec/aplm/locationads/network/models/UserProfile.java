package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.*;

public class UserProfile implements Serializable {

    @SerializedName("userId")
    private int userId;

    @SerializedName("username")
    private String username;

    @SerializedName("properties")
    private Map<String, String> properties;

    @SerializedName("lastUpdated")
    private long lastUpdated;

    public UserProfile() {
        this.userId = -1;
        this.properties = new HashMap<>();
        this.lastUpdated = System.currentTimeMillis();
    }

    public UserProfile(String username) {
        this.username = username;
        this.properties = new HashMap<>();
        this.lastUpdated = System.currentTimeMillis();
    }

    public UserProfile(int userId, String username) {
        this.userId = userId;
        this.username = username;
        this.properties = new HashMap<>();
        this.lastUpdated = System.currentTimeMillis();
    }

    // Adicionar propriedade
    public boolean addProperty(String key, String value) {
        if (key == null || key.trim().isEmpty() || value == null || value.trim().isEmpty()) {
            return false;
        }
        properties.put(key.trim(), value.trim());
        this.lastUpdated = System.currentTimeMillis();
        return true;
    }

    // Remover propriedade
    public boolean removeProperty(String key) {
        if (properties.containsKey(key)) {
            properties.remove(key);
            this.lastUpdated = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    // Verificar se corresponde a uma restrição
    public boolean matchesRestriction(String key, String value) {
        String profileValue = properties.get(key);
        return profileValue != null && profileValue.equalsIgnoreCase(value);
    }

    // Verificar se corresponde a qualquer restrição da lista
    public boolean matchesAnyRestriction(List<PerfilKeyValue> restrictions) {
        for (PerfilKeyValue restriction : restrictions) {
            if (matchesRestriction(restriction.getKey(), restriction.getValue())) {
                return true;
            }
        }
        return false;
    }

    // Obter todas as chaves
    public Set<String> getAllKeys() {
        return new HashSet<>(properties.keySet());
    }

    // Obter todas as propriedades como lista
    public List<PerfilKeyValue> getAllPropertiesAsList() {
        List<PerfilKeyValue> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            list.add(new PerfilKeyValue(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    public String getPropertyValue(String key) {
        return properties.get(key);
    }

    public int getPropertyCount() {
        return properties.size();
    }

    public void clearAllProperties() {
        properties.clear();
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters e Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        this.lastUpdated = System.currentTimeMillis();
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public boolean isValid() {
        return userId != -1;
    }

    @Override
    public String toString() { 
        return "UserProfile{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", properties=" + properties.size() + " items" +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}