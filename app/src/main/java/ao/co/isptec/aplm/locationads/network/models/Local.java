package ao.co.isptec.aplm.locationads.network.models;

import com.google.gson.annotations.Expose;

import java.util.List;

public class Local {
    private String nome;
    private String tipo;
    private double latitude;
    private double longitude;
    private int raio;
    private List<String> wifiIds;

    public Local(String nome, String tipo, double latitude, double longitude, int raio){
        this.nome = nome;
        this.tipo = tipo;
        this.longitude = longitude;
        this.latitude = latitude;
        this.raio = raio;
    }

    public Local(String nome, String tipo, double latitude, double longitude, int raio, List<String> wifiIds ){
        this.nome = nome;
        this.tipo = tipo;
        this.longitude = longitude;
        this.latitude = latitude;
        this.raio = raio;
        this.wifiIds = wifiIds;
    }

    public String getNome(){
        return this.nome;
    }

}
