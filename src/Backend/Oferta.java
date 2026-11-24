package Backend;

public class Oferta {

    private String tipo;
    private String objetivo;
    private double porcentajeDescuento;
    private long fechaInicio;
    private long fechaFin;

    public Oferta(String tipo, String objetivo,
                  double porcentajeDescuento,
                  long fechaInicio, long fechaFin) {

        this.tipo = tipo;
        this.objetivo = objetivo;
        this.porcentajeDescuento = porcentajeDescuento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public String getTipo() {
        return tipo;
    }

    public String getObjetivo() {
        return objetivo;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public long getFechaInicio() {
        return fechaInicio;
    }

    public long getFechaFin() {
        return fechaFin;
    }

    public boolean estaVigente() {
        long ahora = System.currentTimeMillis();
        return ahora >= fechaInicio && ahora < fechaFin;
    }
}