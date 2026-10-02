package Backend;

import java.util.LinkedHashMap;
import java.util.Map;

public class Carrito {

    private final Map<Producto, Integer> productos;
    private final Inventario inventario;

    public Carrito(Inventario inventario) {
        this.inventario = inventario;
        this.productos = new LinkedHashMap<>();
    }

    // Agrega una cantidad de un producto al carrito
    public boolean agregarProducto(Producto producto, int cantidad) {

        if (producto == null || cantidad <= 0) {
            return false;
        }

        // El inventario revisa si existe stock suficiente
        if (!inventario.reservarUnidades(producto, cantidad)) {
            return false;
        }

        int cantidadActual = productos.getOrDefault(producto, 0);

        productos.put(
                producto,
                cantidadActual + cantidad
        );

        return true;
    }

    // Elimina completamente un producto del carrito
    public boolean eliminarProducto(Producto producto) {

        Integer cantidad = productos.get(producto);

        if (cantidad == null) {
            return false;
        }

        // Devuelve al inventario todo lo que estaba reservado
        inventario.devolverStock(producto, cantidad);

        productos.remove(producto);

        return true;
    }

    // Cambia la cantidad de un producto
    public boolean actualizarCantidad(Producto producto, int nuevaCantidad) {

        Integer cantidadActual = productos.get(producto);

        if (cantidadActual == null || nuevaCantidad < 0) {
            return false;
        }

        // Si queda en 0, se elimina
        if (nuevaCantidad == 0) {
            return eliminarProducto(producto);
        }

        // Si quiere aumentar
        if (nuevaCantidad > cantidadActual) {

            int cantidadExtra =
                    nuevaCantidad - cantidadActual;

            if (!inventario.reservarUnidades(
                    producto,
                    cantidadExtra
            )) {
                return false;
            }
        }

        // Si quiere disminuir
        if (nuevaCantidad < cantidadActual) {

            int cantidadADevolver =
                    cantidadActual - nuevaCantidad;

            inventario.devolverStock(
                    producto,
                    cantidadADevolver
            );
        }

        productos.put(
                producto,
                nuevaCantidad
        );

        return true;
    }

    // Vacía el carrito y devuelve todo el stock
    public void vaciarCarrito() {

        for (Map.Entry<Producto, Integer> entrada
                : productos.entrySet()) {

            inventario.devolverStock(
                    entrada.getKey(),
                    entrada.getValue()
            );
        }

        productos.clear();
    }

    // Se usa después de una compra realizada.
    // No devuelve stock porque los productos sí fueron comprados.
    public void limpiarDespuesDeCompra() {
        productos.clear();
    }

    public Map<Producto, Integer> getProductos() {
        return new LinkedHashMap<>(productos);
    }

    public boolean estaVacio() {
        return productos.isEmpty();
    }

    public int getCantidadTotal() {

        int cantidadTotal = 0;

        for (int cantidad : productos.values()) {
            cantidadTotal += cantidad;
        }

        return cantidadTotal;
    }

    public double calcularSubtotal() {

        double subtotal = 0;

        for (Map.Entry<Producto, Integer> entrada
                : productos.entrySet()) {

            Producto producto = entrada.getKey();
            int cantidad = entrada.getValue();

            subtotal +=
                    producto.getPrecio() * cantidad;
        }

        return subtotal;
    }

    public double calcularIva() {
        return calcularSubtotal() * 0.19;
    }

    public double calcularTotal() {
        return calcularSubtotal() + calcularIva();
    }
}