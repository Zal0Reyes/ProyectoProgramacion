package Backend;

import java.util.ArrayList;

public class GestorCupones {

    private ArrayList<Cupon> listaCupones;


    public GestorCupones() {
        listaCupones = new ArrayList<>();
    }


    public boolean guardarCupon(Cupon nuevoCupon) {

        if (nuevoCupon == null) {
            return false;
        }


        Cupon encontrado =
                buscarPorCodigo(
                        nuevoCupon.getCodigo()
                );


        // No dejamos guardar dos cupones con el mismo código
        if (encontrado != null) {
            return false;
        }


        listaCupones.add(nuevoCupon);

        return true;
    }


    public Cupon buscarPorCodigo(String codigoBuscado) {

        if (codigoBuscado == null) {
            return null;
        }


        for (Cupon cupon : listaCupones) {

            if (cupon.getCodigo()
                    .equalsIgnoreCase(codigoBuscado.trim())) {

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


        if (!cupon.quedanUsos()) {
            return false;
        }


        return true;
    }


    public ArrayList<Cupon> getListaCupones() {
        return listaCupones;
    }
}