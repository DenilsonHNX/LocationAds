package ao.co.isptec.aplm.locationads.network.models;

import java.util.Map;

public class Ads {
    private String titulo;
    private String conteudo;
    private int autorId;
    private String localNome;
    private String policy;
    private String imagem;
    private Map<String, Object> restricoes;  // para permitir propriedades dinâmicas variadas
    private String horaInicio;  // ISO 8601 UTC
    private String horaFim;

    public Ads(String titulo, String conteudo, int autorId, String localNome, String policy,
               Map<String, Object> restricoes, String horaInicio, String horaFim, String imagem) {
        this.titulo = titulo;
        this.conteudo = conteudo;
        this.autorId = autorId;
        this.localNome = localNome;
        this.policy = policy;
        this.restricoes = restricoes;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
        this.imagem = imagem;
    }


}
