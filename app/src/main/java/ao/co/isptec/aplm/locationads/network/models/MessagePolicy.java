package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessagePolicy implements Serializable {

    public enum PolicyType {
        WHITELIST("whitelist"),
        BLACKLIST("blacklist");

        private String value;

        PolicyType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static PolicyType fromString(String value) {
            for (PolicyType type : PolicyType.values()) {
                if (type.value.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return WHITELIST;
        }
    }

    @SerializedName("type")
    private String type;

    @SerializedName("restrictions")
    private List<PerfilKeyValue> restrictions;

    @SerializedName("startTime")
    private long startTime;

    @SerializedName("endTime")
    private long endTime;

    public MessagePolicy() {
        this.type = PolicyType.WHITELIST.getValue();
        this.restrictions = new ArrayList<>();
        this.startTime = 0;
        this.endTime = Long.MAX_VALUE;
    }

    public MessagePolicy(PolicyType policyType) {
        this.type = policyType.getValue();
        this.restrictions = new ArrayList<>();
        this.startTime = 0;
        this.endTime = Long.MAX_VALUE;
    }

    // Adicionar restrição
    public void addRestriction(String key, String value) {
        restrictions.add(new PerfilKeyValue(key, value));
    }

    public void addRestriction(PerfilKeyValue restriction) {
        restrictions.add(restriction);
    }

    // Remover restrição
    public void removeRestriction(PerfilKeyValue restriction) {
        restrictions.remove(restriction);
    }

    public void clearRestrictions() {
        restrictions.clear();
    }

    // Verificar se perfil satisfaz a política
    public boolean matchesPolicy(UserProfile profile) {
        if (restrictions.isEmpty()) {
            return PolicyType.fromString(type) == PolicyType.BLACKLIST;
        }

        boolean hasMatch = profile.matchesAnyRestriction(restrictions);

        if (PolicyType.fromString(type) == PolicyType.WHITELIST) {
            return hasMatch;
        } else {
            return !hasMatch;
        }
    }

    // Verificar janela de tempo
    public boolean isWithinTimeWindow() {
        long currentTime = System.currentTimeMillis();
        return currentTime >= startTime && currentTime <= endTime;
    }

    public boolean isValidForProfile(UserProfile profile) {
        return isWithinTimeWindow() && matchesPolicy(profile);
    }

    // Getters e Setters
    public PolicyType getPolicyType() {
        return PolicyType.fromString(type);
    }

    public void setPolicyType(PolicyType policyType) {
        this.type = policyType.getValue();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<PerfilKeyValue> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(List<PerfilKeyValue> restrictions) {
        this.restrictions = restrictions;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setTimeWindow(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getRestrictionCount() {
        return restrictions.size();
    }
}