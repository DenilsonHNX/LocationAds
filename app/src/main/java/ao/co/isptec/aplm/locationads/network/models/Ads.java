package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

/**
 * Modelo para anúncio/mensagem
 * Usado para criar, listar e exibir anúncios
 */
public class Ads {
    
    @SerializedName("id")
    private int id;
    
    @SerializedName("titulo")
    private String titulo;

    @SerializedName("conteudo")
    private String conteudo;

    @SerializedName("autorId")
    private int autorId;

    @SerializedName("localId")
    private int localId;

    @SerializedName("policy")
    private String policy; // "public", "whitelist", "blacklist"

    @SerializedName("modoEntrega")
    private String modoEntrega; // "push" ou "mula"

    @SerializedName("imagem")
    private String imagem;

    @SerializedName("restricoes")
    private Map<String, Object> restricoes;

    // ✅ NOVO: Tags (chave-valor do perfil)
    @SerializedName("tags")
    private Map<String, String> tags;

    @SerializedName("horaInicio")
    private String horaInicio;

    @SerializedName("horaFim")
    private String horaFim;
    
    @SerializedName("ativo")
    private boolean ativo;
    
    @SerializedName("criadoEm")
    private String criadoEm;
    
    @SerializedName("atualizadoEm")
    private String atualizadoEm;
    
    @SerializedName("local")
    private Local local;
    
    @SerializedName("autor")
    private UserProfile autor;
    
    @SerializedName("salvo")
    private boolean salvo;
    
    @SerializedName("whitelist")
    private List<Integer> whitelist;
    
    @SerializedName("blacklist")
    private List<Integer> blacklist;

    // Construtor padrão
    public Ads() {}

    // Construtor completo para criar novo anúncio
    public Ads(String titulo, String conteudo, int autorId, int localId,
               String policy, Map<String, Object> restricoes, String imagem,
               String horaInicio, String horaFim) {
        // NÃO seta this.id - fica null
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.autorId = autorId;
        this.localId = localId;
        this.policy = policy;
        this.restricoes = restricoes;
        this.imagem = imagem;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.modoEntrega = "push"; // padrão
    }
    
    // Construtor com modo de entrega
    public Ads(String titulo, String conteudo, int autorId, int localId,
               String policy, String modoEntrega, Map<String, Object> restricoes, 
               String imagem, String horaInicio, String horaFim) {
        this(titulo, conteudo, autorId, localId, policy, restricoes, imagem, horaInicio, horaFim);
        this.modoEntrega = modoEntrega;
    }

    // ✅ NOVO: Construtor completo com tags e modoEntrega
    public Ads(String titulo, String conteudo, int autorId, int localId,
               String policy, Map<String, Object> restricoes, String imagem,
               Map<String, String> tags, String modoEntrega,
               String horaInicio, String horaFim) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.autorId = autorId;
        this.localId = localId;
        this.policy = policy;
        this.restricoes = restricoes;
        this.imagem = imagem;
        this.tags = tags;
        this.modoEntrega = modoEntrega;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    // Getters
    public int getId() {
        return id;
    }
    
    public String getTitulo() {
        return titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public int getAutorId() {
        return autorId;
    }

    public int getLocalId() {
        return localId;
    }

    public String getPolicy() {
        return policy;
    }
    
    public String getModoEntrega() {
        return modoEntrega;
    }

    public String getImagem() {
        return imagem;
    }

    public Map<String, Object> getRestricoes() {
        return restricoes;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public String getHoraFim() {
        return horaFim;
    }
    
    public boolean isAtivo() {
        return ativo;
    }
    
    public String getCriadoEm() {
        return criadoEm;
    }
    
    public String getAtualizadoEm() {
        return atualizadoEm;
    }
    
    public Local getLocal() {
        return local;
    }
    
    public UserProfile getAutor() {
        return autor;
    }
    
    public boolean isSalvo() {
        return salvo;
    }
    
    public List<Integer> getWhitelist() {
        return whitelist;
    }
    
    public List<Integer> getBlacklist() {
        return blacklist;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
    
    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
    
    public void setAutorId(int autorId) {
        this.autorId = autorId;
    }
    
    public void setLocalId(int localId) {
        this.localId = localId;
    }
    
    public void setPolicy(String policy) {
        this.policy = policy;
    }
    
    public void setModoEntrega(String modoEntrega) {
        this.modoEntrega = modoEntrega;
    }
    
    public void setImagem(String imagem) {
        this.imagem = imagem;
    }
    
    public void setRestricoes(Map<String, Object> restricoes) {
        this.restricoes = restricoes;
    }
    
    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }
    
    public void setHoraFim(String horaFim) {
        this.horaFim = horaFim;
    }
    
    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
    
    public void setLocal(Local local) {
        this.local = local;
    }
    
    public void setAutor(UserProfile autor) {
        this.autor = autor;
    }
    
    public void setSalvo(boolean salvo) {
        this.salvo = salvo;
    }
    
    public void setWhitelist(List<Integer> whitelist) {
        this.whitelist = whitelist;
    }
    
    public void setBlacklist(List<Integer> blacklist) {
        this.blacklist = blacklist;
    }
    
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
    
    /**
     * Verifica se o anúncio é público
     */
    public boolean isPublic() {
        return "public".equalsIgnoreCase(policy);
    }
    
    /**
     * Verifica se o anúncio usa whitelist
     */
    public boolean isWhitelistPolicy() {
        return "whitelist".equalsIgnoreCase(policy);
    }
    
    /**
     * Verifica se o anúncio usa blacklist
     */
    public boolean isBlacklistPolicy() {
        return "blacklist".equalsIgnoreCase(policy);
    }
    
    /**
     * Verifica se o modo de entrega é push
     */
    public boolean isPushDelivery() {
        return "push".equalsIgnoreCase(modoEntrega);
    }
    
    /**
     * Verifica se o modo de entrega é mula
     */
    public boolean isMulaDelivery() {
        return "mula".equalsIgnoreCase(modoEntrega);
    }
}
