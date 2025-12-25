package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;

public class RecoveryRequest {

    @SerializedName("email")
    private String email;

    public RecoveryRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "RecoveryRequest{" +
                "email='" + email + '\'' +
                '}';
    }
}
