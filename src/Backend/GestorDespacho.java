package Backend;

import java.io.*;
import java.util.ArrayList;

public class GestorDespacho {

    private static final String ARCHIVO_TARIFAS = "tarifas_envio.csv";
    private ArrayList<TarifaEnvio> listaTarifas;
    private double montoMinimoEnvioGratis;

    public GestorDespacho() {
        this.listaTarifas = new ArrayList<>();
        this.montoMinimoEnvioGratis = 100000;
        cargarDesdeCSV();

        if (listaTarifas.isEmpty()) {
            cargarTarifasIniciales();
            guardarEnCSV();
        }
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

    public void guardarOActualizarTarifa(String region, double costo, int dias) {
        TarifaEnvio existente = buscarPorRegion(region);
        if (existente != null) {
            existente.setCostoEnvio(costo);
            existente.setDiasEstimados(dias);
        } else {
            listaTarifas.add(new TarifaEnvio(region.trim(), costo, dias));
        }
        guardarEnCSV();
    }

    public boolean eliminarTarifa(String region) {
        TarifaEnvio tarifa = buscarPorRegion(region);
        if (tarifa != null) {
            listaTarifas.remove(tarifa);
            guardarEnCSV();
            return true;
        }
        return false;
    }

    public double calcularCostoEnvio(boolean esDespachoDomicilio, String region, double subtotal) {
        if (!esDespachoDomicilio) {
            return 0;
        }

        if (montoMinimoEnvioGratis > 0 && subtotal >= montoMinimoEnvioGratis) {
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
        this.montoMinimoEnvioGratis = Math.max(0, montoMinimoEnvioGratis);
        guardarEnCSV();
    }

    public void guardarEnCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_TARIFAS))) {
            writer.write("MINIMO_GRATIS," + montoMinimoEnvioGratis + ",0");
            writer.newLine();

            for (TarifaEnvio t : listaTarifas) {
                writer.write(t.getRegion().replace(",", " ") + "," + t.getCostoEnvio() + "," + t.getDiasEstimados());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("No se pudieron guardar las tarifas de envío.");
        }
    }

    public void cargarDesdeCSV() {
        File archivo = new File(ARCHIVO_TARIFAS);
        if (!archivo.exists()) {
            return;
        }

        listaTarifas.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",", 3);
                if (datos.length != 3) continue;

                if (datos[0].equals("MINIMO_GRATIS")) {
                    montoMinimoEnvioGratis = Double.parseDouble(datos[1].trim());
                } else {
                    String region = datos[0].trim();
                    double costo = Double.parseDouble(datos[1].trim());
                    int dias = Integer.parseInt(datos[2].trim());
                    listaTarifas.add(new TarifaEnvio(region, costo, dias));
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error al cargar tarifas de envío.");
        }
    }
}