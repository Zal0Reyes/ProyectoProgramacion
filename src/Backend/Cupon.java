package Backend;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Cupon {

    private String codigo;
    private String tipoDescuento;
    private double valorDescuento;
    private String fechaLimite;
    private int maximoUsos;
    private int usosRealizados;


    public Cupon(String codigo, String tipoDescuento,
                 double valorDescuento, String fechaLimite,
                 int maximoUsos) {

        this.codigo = codigo.trim().toUpperCase();
        this.tipoDescuento = tipoDescuento;
        this.valorDescuento = valorDescuento;
        this.fechaLimite = fechaLimite;
        this.maximoUsos = maximoUsos;
        this.usosRealizados = 0;
    }


    // Se usa al cargar cupones desde el CSV
    public Cupon(String codigo, String tipoDescuento,
                 double valorDescuento, String fechaLimite,
                 int maximoUsos, int usosRealizados) {

        this.codigo = codigo.trim().toUpperCase();
        this.tipoDescuento = tipoDescuento;
        this.valorDescuento = valorDescuento;
        this.fechaLimite = fechaLimite;
        this.maximoUsos = maximoUsos;
        this.usosRealizados = usosRealizados;
    }


    public String getCodigo() {
        return codigo;
    }

    public String getTipoDescuento() {
        return tipoDescuento;
    }

    public double getValorDescuento() {
        return valorDescuento;
    }

    public String getFechaLimite() {
        return fechaLimite;
    }

    public int getMaximoUsos() {
        return maximoUsos;
    }

    public int getUsosRealizados() {
        return usosRealizados;
    }


    public boolean quedanUsos() {
        return usosRealizados < maximoUsos;
    }


    public boolean estaVigente() {

        try {
            DateTimeFormatter formato =
                    DateTimeFormatter.ofPattern("dd-MM-yyyy");

            LocalDate vencimiento =
                    LocalDate.parse(fechaLimite, formato);

            return !LocalDate.now().isAfter(vencimiento);

        } catch (DateTimeParseException e) {
            return false;
        }
    }


    public double calcularDescuento(double subtotal) {

        if (subtotal <= 0) {
            return 0;
        }

        double descuento = 0;

        if (tipoDescuento.equalsIgnoreCase("Porcentaje")) {

            descuento =
                    subtotal * valorDescuento / 100;

        } else if (tipoDescuento.equalsIgnoreCase("Monto fijo")) {

            descuento = valorDescuento;
        }


        // Nunca descontar más que el subtotal
        if (descuento > subtotal) {
            descuento = subtotal;
        }

        return descuento;
    }


    public void usarCupon() {

        if (quedanUsos()) {
            usosRealizados++;
        }
    }
}