package Backend;

import java.io.*;
import java.util.*;

public class GestorOfertas {

    private final List<Oferta> ofertas = new ArrayList<>();
    private final boolean guardarArchivo;
    private static final String ARCHIVO = "ofertas.csv";

    public GestorOfertas() {
        this(true);
    }

    public GestorOfertas(boolean guardarArchivo) {
        this.guardarArchivo = guardarArchivo;

        if (guardarArchivo) {
            cargarDesdeCSV();
        }
    }

    public synchronized boolean agregarOferta(
            String tipo, String objetivo,
            double descuento, long inicio, long fin) {

        if (tipo == null || objetivo == null
                || objetivo.trim().isEmpty()
                || descuento <= 0 || descuento >= 100
                || fin <= inicio) {
            return false;
        }

        tipo = tipo.toUpperCase().trim();

        if (!tipo.equals("PRODUCTO") && !tipo.equals("CATEGORIA")) {
            return false;
        }

        final String tipoFinal = tipo;
        final String objetivoFinal = objetivo.trim();

        ofertas.removeIf(o ->
                o.getTipo().equals(tipoFinal)
                        && o.getObjetivo().equalsIgnoreCase(objetivoFinal)
        );

        ofertas.add(new Oferta(
                tipoFinal, objetivoFinal, descuento, inicio, fin
        ));

        if (guardarArchivo) {
            guardarEnCSV();
        }

        return true;
    }

    public synchronized double obtenerPrecio(Producto producto) {

        if (producto == null) return 0;

        Oferta ofertaProducto = null;
        Oferta ofertaCategoria = null;

        for (Oferta oferta : ofertas) {

            if (!oferta.estaVigente()) continue;

            if (oferta.getTipo().equals("PRODUCTO")
                    && oferta.getObjetivo().equalsIgnoreCase(producto.getId())) {
                ofertaProducto = oferta;
            }

            if (oferta.getTipo().equals("CATEGORIA")
                    && oferta.getObjetivo().equalsIgnoreCase(producto.getCategoria())) {
                ofertaCategoria = oferta;
            }
        }

        // Una oferta específica del producto tiene prioridad.
        Oferta aplicada = ofertaProducto != null
                ? ofertaProducto : ofertaCategoria;

        if (aplicada == null) {
            return producto.getPrecio();
        }

        return producto.getPrecio()
                * (1 - aplicada.getPorcentajeDescuento() / 100);
    }

    public synchronized List<Oferta> obtenerOfertasVigentes() {

        List<Oferta> vigentes = new ArrayList<>();

        for (Oferta oferta : ofertas) {
            if (oferta.estaVigente()) {
                vigentes.add(oferta);
            }
        }

        return vigentes;
    }

    private void guardarEnCSV() {

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(ARCHIVO))) {

            writer.write("tipo;objetivo;descuento;inicio;fin\n");

            for (Oferta o : ofertas) {

                writer.write(
                        o.getTipo() + ";"
                                + o.getObjetivo().replace(";", " ") + ";"
                                + o.getPorcentajeDescuento() + ";"
                                + o.getFechaInicio() + ";"
                                + o.getFechaFin() + "\n"
                );
            }

        } catch (IOException e) {
            System.out.println("Error guardando ofertas: " + e.getMessage());
        }
    }

    private void cargarDesdeCSV() {

        File archivo = new File(ARCHIVO);

        if (!archivo.exists()) {
            guardarEnCSV();
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(archivo))) {

            reader.readLine();

            String linea;

            while ((linea = reader.readLine()) != null) {

                String[] datos = linea.split(";", 5);

                if (datos.length != 5) continue;

                ofertas.add(new Oferta(
                        datos[0],
                        datos[1],
                        Double.parseDouble(datos[2]),
                        Long.parseLong(datos[3]),
                        Long.parseLong(datos[4])
                ));
            }

        } catch (Exception e) {
            System.out.println("Error leyendo ofertas: " + e.getMessage());
        }
    }
}