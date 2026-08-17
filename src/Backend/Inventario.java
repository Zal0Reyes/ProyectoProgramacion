package Backend;

import java.util.ArrayList;
import java.util.List;

public class Inventario {
    // Lista donde se almacenan todos los productos
    private final List<Producto> listaProductos;
    private int contadorId;

    public Inventario() {
        this.listaProductos = new ArrayList<>();
        contadorId = 1;
    }


    public void registrarProducto(String nombre, double precio, int stock, String categoria) {

        // Confirmar si el producto ya existe
        for (Producto p : listaProductos) {
            // Comparamos los nombres (ignorando mayúsculas/minúsculas)
            if (p.getNombre().equalsIgnoreCase(nombre)) {
                System.out.println("El producto '" + nombre + "' ya existe. Actualizando stock...");
                // Le sumamos la cantidad nueva al stock actual
                p.setStock(p.getStock() + stock);
                return;
            }
        }

        // Crear ID
        // Usamos String.format para que el ID se vea como "P001", "P002", etc.
    String nuevoId = String.format("P%03d", contadorId);
        // Aumentamos el contador para el próximo producto
        contadorId++;

        // Crear y guardar el producto
        Producto nuevoProducto = new Producto(nombre ,precio ,stock ,categoria ,nuevoId);
        listaProductos.add(nuevoProducto);

        System.out.println("Nuevo producto agregado: " + nombre + " con ID: " + nuevoId);
    }

    public List<Producto> getProductos() {
        return listaProductos;
    }

}