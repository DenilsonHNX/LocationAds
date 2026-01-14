package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo para mensagem em trânsito (sistema de mulas)
 * Usado para entrega descentralizada de anúncios
 */
public class MensagemTransito {
    
    @SerializedName("id")
    private int id;
    
    @SerializedName("mensagemId")
    private int mensagemId;
    
    @SerializedName("mulaId")
    private int mulaId;
    
    @SerializedName("status")
    private String status; // "em_transito", "entregue", "expirado"
    
    @SerializedName("atribuidoEm")
    private String atribuidoEm;
    
    @SerializedName("entregueEm")
    private String entregueEm;
    
    @SerializedName("expiraEm")
    private String expiraEm;
    
    @SerializedName("mensagem")
    private Ads mensagem;
    
    @SerializedName("mula")
    private UserProfile mula;
    
    public MensagemTransito() {}
    
    // Getters e Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getMensagemId() {
        return mensagemId;
    }
    
    public void setMensagemId(int mensagemId) {
        this.mensagemId = mensagemId;
    }
    
    public int getMulaId() {
        return mulaId;
    }
    
    public void setMulaId(int mulaId) {
        this.mulaId = mulaId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getAtribuidoEm() {
        return atribuidoEm;
    }
    
    public void setAtribuidoEm(String atribuidoEm) {
        this.atribuidoEm = atribuidoEm;
    }
    
    public String getEntregueEm() {
        return entregueEm;
    }
    
    public void setEntregueEm(String entregueEm) {
        this.entregueEm = entregueEm;
    }
    
    public String getExpiraEm() {
        return expiraEm;
    }
    
    public void setExpiraEm(String expiraEm) {
        this.expiraEm = expiraEm;
    }
    
    public Ads getMensagem() {
        return mensagem;
    }
    
    public void setMensagem(Ads mensagem) {
        this.mensagem = mensagem;
    }
    
    public UserProfile getMula() {
        return mula;
    }
    
    public void setMula(UserProfile mula) {
        this.mula = mula;
    }
    
    /**
     * Verifica se a mensagem está em trânsito
     */
    public boolean isEmTransito() {
        return "em_transito".equals(status);
    }
    
    /**
     * Verifica se a mensagem foi entregue
     */
    public boolean isEntregue() {
        return "entregue".equals(status);
    }
}
