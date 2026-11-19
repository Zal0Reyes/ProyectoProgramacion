package Backend;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Compra {

    private final String rutUsuario;
    private final LocalDateTime fechaHora;
    private final List<String> articulos;
    private final double totalPagado;

    // NUEVO: datos del cupón
    private final String codigoCupon;
    private final double descuento;

    private String estado = "Pendiente";


    // Constructor antiguo
    public Compra(
            String rutUsuario,
            List<String> articulos
    ) {

        this(
                rutUsuario,
                LocalDateTime.now(),
                articulos,
                0,
                "",
                0
        );
    }


    // Constructor antiguo
    public Compra(
            String rutUsuario,
            List<String> articulos,
            double totalPagado
    ) {

        this(
                rutUsuario,
                LocalDateTime.now(),
                articulos,
                totalPagado,
                "",
                0
        );
    }


    // NUEVO: compra con cupón y descuento
    public Compra(
            String rutUsuario,
            List<String> articulos,
            double totalPagado,
            String codigoCupon,
            double descuento
    ) {

        this(
                rutUsuario,
                LocalDateTime.now(),
                articulos,
                totalPagado,
                codigoCupon,
                descuento
        );
    }


    // Constructor antiguo para cargar CSV viejo
    public Compra(
            String rutUsuario,
            LocalDateTime fechaHora,
            List<String> articulos,
            double totalPagado
    ) {

        this(
                rutUsuario,
                fechaHora,
                articulos,
                totalPagado,
                "",
                0
        );
    }


    // Constructor completo
    public Compra(
            String rutUsuario,
            LocalDateTime fechaHora,
            List<String> articulos,
            double totalPagado,
            String codigoCupon,
            double descuento
    ) {

        this.rutUsuario =
                rutUsuario == null ||
                        rutUsuario.trim().isEmpty()
                        ? "No hay rut asociado"
                        : rutUsuario.trim();

        this.fechaHora = fechaHora;

        this.articulos =
                new ArrayList<>(articulos);

        this.totalPagado =
                totalPagado;

        this.codigoCupon =
                codigoCupon == null
                        ? ""
                        : codigoCupon.trim();

        this.descuento =
                descuento;
    }


    public String getRutUsuario() {
        return rutUsuario;
    }


    public LocalDateTime getFechaHora() {
        return fechaHora;
    }


    public List<String> getArticulos() {
        return new ArrayList<>(articulos);
    }


    public double getTotalPagado() {
        return totalPagado;
    }


    // NUEVO
    public String getCodigoCupon() {
        return codigoCupon;
    }


    // NUEVO
    public double getDescuento() {
        return descuento;
    }


    public String getEstado() {
        return estado;
    }


    public void setEstado(String estado) {
        this.estado = estado;
    }
}