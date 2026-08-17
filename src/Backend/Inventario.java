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

    public Producto buscarProductoPorId(String idBuscado) {
        for (Producto p : listaProductos) {
            if (p.getId().equalsIgnoreCase(idBuscado)) {
                return p; // Retorna el producto si lo encuentra
            }
        }
        return null; // Retorna null si no encontró nada con ese ID
    }

    public Producto buscarProductoPorNombre(String nombreBuscado) {
        for (Producto p : listaProductos) {
            if (p.getNombre().equalsIgnoreCase(nombreBuscado)) {
                return p; // Retorna el producto si lo encuentra
            }
        }
        return null; // Retorna null si no existe
    }


    public void imprimirTodosConsola() {
        if (listaProductos.isEmpty()) {
            System.out.println("El inventario está vacío.");
        } else {
            System.out.println("--- LISTA DE PRODUCTOS ---");
            for (Producto p : listaProductos) {
                // Al llamar a "p", Java usa automáticamente el metodo ToString
                System.out.println(p);
            }
        }
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