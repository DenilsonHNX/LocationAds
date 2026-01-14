package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo para notificação recebida pelo utilizador
 * Resposta de: GET /messages/notifications
 */
public class Notificacao {
    
    @SerializedName("id")
    private int id;
    
    @SerializedName("titulo")
    private String titulo;
    
    @SerializedName("conteudo")
    private String conteudo;
    
    @SerializedName("mensagemId")
    private int mensagemId;
    
    @SerializedName("usuarioId")
    private int usuarioId;
    
    @SerializedName("lido")
    private boolean lido;
    
    @SerializedName("recebidoEm")
    private String recebidoEm;
    
    @SerializedName("criadoEm")
    private String criadoEm;
    
    @SerializedName("mensagem")
    private Ads mensagem;
    
    public Notificacao() {}
    
    // Getters e Setters
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getTitulo() {
        return titulo;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public String getConteudo() {
        return conteudo;
    }
    
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
    
    public int getMensagemId() {
        return mensagemId;
    }
    
    public void setMensagemId(int mensagemId) {
        this.mensagemId = mensagemId;
    }
    
    public int getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public boolean isLido() {
        return lido;
    }
    
    public void setLido(boolean lido) {
        this.lido = lido;
    }
    
    public String getRecebidoEm() {
        return recebidoEm;
    }
    
    public void setRecebidoEm(String recebidoEm) {
        this.recebidoEm = recebidoEm;
    }
    
    public String getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(String criadoEm) {
        this.criadoEm = criadoEm;
    }
    
    public Ads getMensagem() {
        return mensagem;
    }
    
    public void setMensagem(Ads mensagem) {
        this.mensagem = mensagem;
    }
}
