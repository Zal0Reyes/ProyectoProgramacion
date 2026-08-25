package Backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Inventario {
    // Lista donde se almacenan todos los productos
    private final ArrayList<Producto> listaProductos;
    private int contadorId;
    private static final String ARCHIVO_CSV = "inventario.csv";

    public Inventario() {
        this.listaProductos = new ArrayList<>();
        contadorId = 1;
        cargarDesdeCSV();
    }


    public boolean eliminarProductoPorId(String idBuscado) {
        // Usamos un ciclo 'for' tradicional con índice (i)
        for (int i = 0; i < listaProductos.size(); i++) {
            // Obtenemos el producto en la posición 'i'

            Producto p = listaProductos.get(i);

            // Comparamos el ID (ignorando mayúsculas/minúsculas)
            if (p.getId().equalsIgnoreCase(idBuscado)) {

                // Si coincide, lo eliminamos de la lista usando su índice
                listaProductos.remove(i);
                guardarEnCSV();
                return true; // Retornamos true porque se eliminó con éxito

            }
        }

        return false; // Si termina el ciclo y no encontró nada, retorna false
    }

    public void registrarProducto(String nombre, double precio, int stock, String categoria) {

        // Confirmar si el producto ya existe
        for (Producto p : listaProductos) {
            // Comparamos los nombres (ignorando mayúsculas/minúsculas)
            if (p.getNombre().equalsIgnoreCase(nombre)) {
                System.out.println("El producto '" + nombre + "' ya existe. Actualizando stock...");
                // Le sumamos la cantidad nueva al stock actual
                p.setStock(p.getStock() + stock);
                guardarEnCSV();
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
        guardarEnCSV();

        System.out.println("Nuevo producto agregado: " + nombre + " con ID: " + nuevoId);
    }

    public ArrayList<Producto> filtrarPorCategoria(String categoria) {

        ArrayList<Producto> productosCategoria = new ArrayList<>();

        for (Producto p : listaProductos) {

            if (p.getCategoria().equalsIgnoreCase(categoria)) {
                productosCategoria.add(p);
            }
        }

        return productosCategoria;
    }
    public ArrayList<Producto> filtrarPorRangoPrecio(double precioMinimo, double precioMaximo) {

        ArrayList<Producto> productosFiltrados = new ArrayList<>();

        for (Producto p : listaProductos) {

            if (p.getPrecio() >= precioMinimo && p.getPrecio() <= precioMaximo) {
                productosFiltrados.add(p);
            }
        }

        return productosFiltrados;
    }



    //         ----- CALCULOS BASICOS----


    public double calcularPrecioPromedioPorCategoria(String categoria) {

        ArrayList<Producto> productosCategoria = filtrarPorCategoria(categoria);

        if (productosCategoria.isEmpty()) {
            return -1;
        }

        double sumaPrecios = 0;

        for (Producto p : productosCategoria) {
            sumaPrecios = sumaPrecios + p.getPrecio();
        }

        return sumaPrecios / productosCategoria.size();
    }

    public Producto buscarMenorStockPorCategoria(String categoria) {

        List<Producto> productosCategoria = filtrarPorCategoria(categoria);

        if (productosCategoria.isEmpty()) {
            return null;
        }

        Producto productoMenorStock = productosCategoria.get(0);

        for (Producto p : productosCategoria) {

            if (p.getStock() < productoMenorStock.getStock()) {
                productoMenorStock = p;
            }
        }

        return productoMenorStock;
    }



    public void guardarEnCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_CSV))) {
            writer.write("id,nombre,precio,stock,categoria\n");

            for (Producto p : listaProductos) {
                writer.write(p.getId() + "," + p.getNombre() + "," + p.getPrecio() + "," + p.getStock() + "," + p.getCategoria() + "\n");
            }

        } catch (IOException e) {
            System.out.println("No se pudo guardar el archivo CSV: " + e.getMessage());
        }
    }

    public void cargarDesdeCSV() {
        File archivo = new File(ARCHIVO_CSV);

        if (!archivo.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea = reader.readLine(); // Ignora la cabecera

            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",", 5);

                if (datos.length < 5) {
                    continue;
                }

                String id = datos[0].trim();
                String nombre = datos[1].trim();
                double precio = Double.parseDouble(datos[2].trim());
                int stock = Integer.parseInt(datos[3].trim());
                String categoria = datos[4].trim();

                listaProductos.add(new Producto(nombre, precio, stock, categoria, id));

                int numeroId = Integer.parseInt(id.replace("P", ""));
                if (numeroId >= contadorId) {
                    contadorId = numeroId + 1;
                }
            }

        } catch (IOException | NumberFormatException e) {
            System.out.println("No se pudo leer el archivo CSV: " + e.getMessage());
        }
    }

    public ArrayList<Producto> getProductos() {
        return listaProductos;
    }

}