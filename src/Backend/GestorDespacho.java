package Backend;

import java.util.ArrayList;

public class GestorDespacho {

    private ArrayList<TarifaEnvio> listaTarifas;
    private double montoMinimoEnvioGratis;

    public GestorDespacho() {
        this.listaTarifas = new ArrayList<>();
        this.montoMinimoEnvioGratis = 100000; // Envío gratis sobre $100.000
        cargarTarifasIniciales();
    }

    private void cargarTarifasIniciales() {
        listaTarifas.add(new TarifaEnvio("Región del Maule", 4990, 2));
        listaTarifas.add(new TarifaEnvio("Región Metropolitana", 5990, 3));
        listaTarifas.add(new TarifaEnvio("Resto del país", 7990, 5));
    }

    public TarifaEnvio buscarPorRegion(String region) {
        if (region == null) {
            return null;
        }
        for (TarifaEnvio tarifa : listaTarifas) {
            if (tarifa.getRegion().equalsIgnoreCase(region.trim())) {
                return tarifa;
            }
        }
        return null;
    }


    public double calcularCostoEnvio(boolean esDespachoDomicilio, String region, double subtotal) {
        if (!esDespachoDomicilio) {
            return 0;
        }

        if (subtotal >= montoMinimoEnvioGratis) {
            return 0;
        }

        TarifaEnvio tarifa = buscarPorRegion(region);
        if (tarifa != null) {
            return tarifa.getCostoEnvio();
        }

        return 0;
    }

    public ArrayList<TarifaEnvio> getListaTarifas() {
        return listaTarifas;
    }

    public double getMontoMinimoEnvioGratis() {
        return montoMinimoEnvioGratis;
    }

    public void setMontoMinimoEnvioGratis(double montoMinimoEnvioGratis) {
        this.montoMinimoEnvioGratis = montoMinimoEnvioGratis;
    }
}