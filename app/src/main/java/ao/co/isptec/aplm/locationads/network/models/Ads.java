package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Ads {
    @SerializedName("id")
    private Integer id; // Integer permite null, então não será enviado ao criar

    @SerializedName("titulo")
    private String titulo;

    @SerializedName("conteudo")
    private String conteudo;

    @SerializedName("autorId")
    private int autorId;

    @SerializedName("localId")
    private int localId;

    @SerializedName("policy")
    private String policy;

    @SerializedName("imagem")
    private String imagem;

    @SerializedName("restricoes")
    private Map<String, Object> restricoes;

    // ✅ NOVO: Tags (chave-valor do perfil)
    @SerializedName("tags")
    private Map<String, String> tags;

    // ✅ NOVO: Modo de entrega (centralizado/descentralizado)
    @SerializedName("modoEntrega")
    private String modoEntrega;

    @SerializedName("horaInicio")
    private String horaInicio;

    @SerializedName("horaFim")
    private String horaFim;

    @SerializedName("local")
    private Local local; // Objeto Local retornado pelo backend

    // ✅ Construtor vazio (necessário para Gson)
    public Ads() {
    }

    // ✅ Construtor para CRIAR novos anúncios (SEM id)
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
    public Integer getId() { // ✅ Retorna Integer
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

    public Local getLocal() {
        return local;
    }

    // ✅ NOVO
    public Map<String, String> getTags() {
        return tags;
    }

    // ✅ NOVO
    public String getModoEntrega() {
        return modoEntrega;
    }

    // Setters
    public void setId(Integer id) { // ✅ Recebe Integer
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setLocal(Local local) {
        this.local = local;
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


    // ✅ NOVO
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    // ✅ NOVO
    public void setModoEntrega(String modoEntrega) {
        this.modoEntrega = modoEntrega;
    }

    @Override
    public String toString() {
        return "Ads{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", conteudo='" + conteudo + '\'' +
                ", autorId=" + autorId +
                ", localId=" + localId +
                ", policy='" + policy + '\'' +
                ", horaInicio='" + horaInicio + '\'' +
                ", horaFim='" + horaFim + '\'' +
                '}';
    }
}