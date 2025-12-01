package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Local {

    @SerializedName("id")
    private Integer id; // Será usado apenas ao RECEBER da API

    @SerializedName("nome")
    private String nome;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("raio")
    private Integer raio;

    @SerializedName("wifiIds")
    private List<String> wifiIds;

    // Construtor para CRIAR local (sem ID) - não seta o ID
    public Local(String nome, String tipo, Double latitude, Double longitude,
                 Integer raio, List<String> wifiIds) {
        // NÃO setar o ID aqui
        this.nome = nome;
        this.tipo = tipo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.raio = raio;
        this.wifiIds = wifiIds;
    }

    // Construtor completo (para quando receber da API)
    public Local(Integer id, String nome, String tipo, Double latitude,
                 Double longitude, Integer raio, List<String> wifiIds) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.latitude = latitude;
        this.longitude = longitude;
        this.raio = raio;
        this.wifiIds = wifiIds;
    }

    // Getters e Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getRaio() {
        return raio;
    }

    public void setRaio(Integer raio) {
        this.raio = raio;
    }

    public List<String> getWifiIds() {
        return wifiIds;
    }

    public void setWifiIds(List<String> wifiIds) {
        this.wifiIds = wifiIds;
    }
}