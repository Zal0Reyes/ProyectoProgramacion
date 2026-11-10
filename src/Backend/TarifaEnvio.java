package Backend;

public class TarifaEnvio {

    private String region;
    private double costoEnvio;
    private int diasEstimados;

    public TarifaEnvio(String region, double costoEnvio, int diasEstimados) {
        this.region = region;
        this.costoEnvio = costoEnvio;
        this.diasEstimados = diasEstimados;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public double getCostoEnvio() {
        return costoEnvio;
    }

    public void setCostoEnvio(double costoEnvio) {
        this.costoEnvio = costoEnvio;
    }

    public int getDiasEstimados() {
        return diasEstimados;
    }

    public void setDiasEstimados(int diasEstimados) {
        this.diasEstimados = diasEstimados;
    }
}