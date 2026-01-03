package ao.co.isptec.aplm.locationads.network.models;

public class VerifyEmailRequest {
    private String email;
    private String code;

    public VerifyEmailRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }
}
