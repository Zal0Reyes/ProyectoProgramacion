import java.util.ArrayList;
import java.util.List;

public class Inventario {
    // Lista donde se almacenan todos los productos
    private final List<Producto> productos;

    public Inventario() {
        this.productos = new ArrayList<>();
    }

    // Agrega un producto nuevo al inventario
    public void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    public List<Producto> getProductos() {
        return productos;
    }
}