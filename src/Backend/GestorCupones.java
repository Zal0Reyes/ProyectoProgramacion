package Backend;

import java.io.*;
import java.util.ArrayList;

public class GestorCupones {

    private ArrayList<Cupon> listaCupones;

    private static final String ARCHIVO_CUPONES =
            "cupones.csv";


    public GestorCupones() {

        listaCupones = new ArrayList<>();

        cargarDesdeCSV();
    }


    public boolean guardarCupon(Cupon nuevo) {

        if (nuevo == null) {
            return false;
        }

        if (buscarPorCodigo(nuevo.getCodigo()) != null) {
            return false;
        }

        listaCupones.add(nuevo);

        guardarEnCSV();

        return true;
    }


    public Cupon buscarPorCodigo(String codigo) {

        if (codigo == null) {
            return null;
        }

        for (Cupon cupon : listaCupones) {

            if (cupon.getCodigo()
                    .equalsIgnoreCase(codigo.trim())) {

                return cupon;
            }
        }

        return null;
    }


    public boolean cuponDisponible(String codigo) {

        Cupon cupon =
                buscarPorCodigo(codigo);

        if (cupon == null) {
            return false;
        }

        if (!cupon.estaVigente()) {
            return false;
        }

        return cupon.quedanUsos();
    }


    public ArrayList<Cupon> getListaCupones() {
        return listaCupones;
    }


    public void guardarEnCSV() {

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(ARCHIVO_CUPONES))) {

            writer.write(
                    "codigo,tipo,valor,vencimiento,maxUsos,usosRealizados"
            );

            writer.newLine();


            for (Cupon cupon : listaCupones) {

                writer.write(
                        cupon.getCodigo() + ","
                                + cupon.getTipoDescuento() + ","
                                + cupon.getValorDescuento() + ","
                                + cupon.getFechaLimite() + ","
                                + cupon.getMaximoUsos() + ","
                                + cupon.getUsosRealizados()
                );

                writer.newLine();
            }

        } catch (IOException e) {

            System.out.println(
                    "No se pudieron guardar los cupones."
            );
        }
    }


    public void cargarDesdeCSV() {

        File archivo =
                new File(ARCHIVO_CUPONES);

        if (!archivo.exists()) {
            return;
        }


        listaCupones.clear();


        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(archivo))) {

            reader.readLine();

            String linea;


            while ((linea = reader.readLine()) != null) {

                String[] datos =
                        linea.split(",", 6);

                if (datos.length != 6) {
                    continue;
                }


                try {

                    String codigo = datos[0];
                    String tipo = datos[1];

                    double valor =
                            Double.parseDouble(datos[2]);

                    String fecha = datos[3];

                    int maximo =
                            Integer.parseInt(datos[4]);

                    int usados =
                            Integer.parseInt(datos[5]);


                    Cupon cupon =
                            new Cupon(
                                    codigo,
                                    tipo,
                                    valor,
                                    fecha,
                                    maximo,
                                    usados
                            );


                    listaCupones.add(cupon);

                } catch (NumberFormatException e) {

                    System.out.println(
                            "Cupón con datos incorrectos."
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "No se pudieron cargar los cupones."
            );
        }
    }


    // Sirve para que el cliente vea cambios nuevos del CSV
    public void recargarCupones() {
        cargarDesdeCSV();
    }
}