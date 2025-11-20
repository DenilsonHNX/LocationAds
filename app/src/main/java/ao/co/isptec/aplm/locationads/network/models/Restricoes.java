package ao.co.isptec.aplm.locationads.network.models;

public class Restricoes {
    private int idadeMinima;

    public Restricoes(int idadeMinima) {
        this.idadeMinima = idadeMinima;
    }

    public int getIdadeMinima() {
        return idadeMinima;
    }

    public void setIdadeMinima(int idadeMinima) {
        this.idadeMinima = idadeMinima;
    }
}