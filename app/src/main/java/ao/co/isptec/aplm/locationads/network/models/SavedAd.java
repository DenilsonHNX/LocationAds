package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo que representa um anúncio salvo/guardado
 * A API retorna este wrapper que contém o anúncio completo dentro
 */
public class SavedAd {
    
    @SerializedName("id")
    private int id; // ID do registro de "salvo"
    
    @SerializedName("userId")
    private int userId;
    
    @SerializedName("anuncioId")
    private int anuncioId; // ID do anúncio que foi salvo
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("updatedAt")
    private String updatedAt;
    
    @SerializedName("anuncio")
    private Ads anuncio; // ✅ O ANÚNCIO COMPLETO ESTÁ AQUI!
    
    // Construtor vazio obrigatório para Gson
    public SavedAd() {
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public int getAnuncioId() {
        return anuncioId;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Retorna o anúncio completo com todos os dados
     */
    public Ads getAnuncio() {
        return anuncio;
    }
    
    // Setters (opcional mas útil)
    public void setId(int id) {
        this.id = id;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public void setAnuncioId(int anuncioId) {
        this.anuncioId = anuncioId;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setAnuncio(Ads anuncio) {
        this.anuncio = anuncio;
    }
    
    @Override
    public String toString() {
        return "SavedAd{" +
                "id=" + id +
                ", userId=" + userId +
                ", anuncioId=" + anuncioId +
                ", anuncio=" + (anuncio != null ? anuncio.getTitulo() : "null") +
                '}';
    }
}
