package ao.co.isptec.aplm.locationads.network.models;

public class LoginResponse {
    private String accessToken;
    private String message;
    private User user;

    public String getToken() {
        return accessToken;
    }

    public User getUser() {
        return user;
    }

    public String getMessage() {
        return message;
    }

    public class User {
        private int id;
        private String name;
        private String email;

        public int getId() { return id; }
        public String getNome() { return name; }
        public String getEmail() { return email; }
    }
}


