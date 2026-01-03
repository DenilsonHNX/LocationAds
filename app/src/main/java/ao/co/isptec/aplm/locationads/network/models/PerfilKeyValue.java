package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PerfilKeyValue implements Serializable {

<<<<<<< HEAD
    @SerializedName("chave")
    private String key;

    @SerializedName("valor")
    private String value;

    public PerfilKeyValue(String key, String value) {
        this.key = key;
        this.value = value;
=======
    @SerializedName("key")
    private String key;

    @SerializedName("value")
    private String value;

    @SerializedName("timestamp")
    private long timestamp;

    public PerfilKeyValue() {
        this.timestamp = System.currentTimeMillis();
    }

    public PerfilKeyValue(String key, String value) {
        this.key = key;
        this.value = value;
        this.timestamp = System.currentTimeMillis();
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31
    }

    // Getters e Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

<<<<<<< HEAD

=======
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
>>>>>>> 20b503b5e93938c1d66742394c6a98ea2edecf31

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerfilKeyValue that = (PerfilKeyValue) o;
        return key.equals(that.key) && value.equals(that.value);
    }

    @Override
    public String toString() {
        return key + " = " + value;
    }
}