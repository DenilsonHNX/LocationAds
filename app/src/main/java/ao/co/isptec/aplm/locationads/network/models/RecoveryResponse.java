package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;

public class RecoveryResponse {

    @SerializedName("message")
    private String message;

    @SerializedName("email")
    private String email;

    @SerializedName("success")
    private Boolean success;

    public RecoveryResponse() {
    }

    public RecoveryResponse(String message, String email, Boolean success) {
        this.message = message;
        this.email = email;
        this.success = success;
    }

    // Getters e Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "RecoveryResponse{" +
                "message='" + message + '\'' +
                ", email='" + email + '\'' +
                ", success=" + success +
                '}';
    }
}
