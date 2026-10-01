package Backend;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Compra {
    private final String rutUsuario;
    private final LocalDateTime fechaHora;
    private final List<String> articulos;
    private final double totalPagado;

    public Compra(String rutUsuario, List<String> articulos) {
        this(rutUsuario, LocalDateTime.now(), articulos, 0);
    }

    public Compra(String rutUsuario, List<String> articulos, double totalPagado) {
        this(rutUsuario, LocalDateTime.now(), articulos, totalPagado);
    }

    public Compra(String rutUsuario, LocalDateTime fechaHora, List<String> articulos, double totalPagado) {
        this.rutUsuario = rutUsuario == null || rutUsuario.trim().isEmpty()
                ? "No hay rut asociado"
                : rutUsuario.trim();
        this.fechaHora = fechaHora;
        this.articulos = new ArrayList<>(articulos);
        this.totalPagado = totalPagado;
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
}
