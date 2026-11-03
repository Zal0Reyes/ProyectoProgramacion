package Backend;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Cupon {

    private String codigo;
    private String tipoDescuento;
    private double valorDescuento;
    private String fechaLimite;
    private int maximoUsos;
    private int usosRealizados;


    public Cupon(String codigo,
                 String tipoDescuento,
                 double valorDescuento,
                 String fechaLimite,
                 int maximoUsos) {

        this.codigo = codigo;
        this.tipoDescuento = tipoDescuento;
        this.valorDescuento = valorDescuento;
        this.fechaLimite = fechaLimite;
        this.maximoUsos = maximoUsos;
        this.usosRealizados = 0;
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

        DateTimeFormatter formato =
                DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate fechaVencimiento =
                LocalDate.parse(fechaLimite, formato);

        LocalDate fechaActual =
                LocalDate.now();

        return !fechaActual.isAfter(fechaVencimiento);
    }


    public void usarCupon() {

        if (quedanUsos()) {
            usosRealizados++;
        }
    }
}