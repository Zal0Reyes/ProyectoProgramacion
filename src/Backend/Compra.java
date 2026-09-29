package Backend;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Compra {
    private final String rutUsuario;
    private final LocalDateTime fechaHora;
    private final List<String> articulos;

    public Compra(String rutUsuario, List<String> articulos) {
        this.rutUsuario = rutUsuario;
        this.fechaHora = LocalDateTime.now();
        this.articulos = new ArrayList<>(articulos);
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
}
