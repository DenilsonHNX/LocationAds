package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class Ads {
    @SerializedName("titulo")
    private String titulo;

    @SerializedName("conteudo")
    private String conteudo;

    @SerializedName("autorId")
    private int autorId;

    @SerializedName("localId")  // ← MUDOU de localNome para localId
    private int localId;

    @SerializedName("policy")
    private String policy;

    @SerializedName("imagem")
    private String imagem;

    @SerializedName("restricoes")
    private Map<String, Object> restricoes;

    @SerializedName("horaInicio")
    private String horaInicio;

    @SerializedName("horaFim")
    private String horaFim;

    // Construtor corrigido com a ordem correta dos parâmetros
    public Ads(String titulo, String conteudo, int autorId, int localId,
               String policy, Map<String, Object> restricoes, String imagem,
               String horaInicio, String horaFim) {
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

    // Getters
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
}