package Backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Inventario {
    // Lista donde se almacenan todos los productos
    private final ArrayList<Producto> listaProductos;
    private final ArrayList<Compra> historialCompras;
    private final List<String> categoriasDisponibles;
    private int contadorId;


    private static final String ARCHIVO_CSV = "inventario.csv";
    private static final String ARCHIVO_CATEGORIAS_CSV = "categorias.csv";
    private static final String ARCHIVO_COMPRAS_CSV = "compras.csv";

    public Inventario() {
        this.listaProductos = new ArrayList<>();
        this.historialCompras = new ArrayList<>();
        this.categoriasDisponibles = new ArrayList<>();
        contadorId = 1;
        // Las categorías deben cargarse antes que los productos para llenar los combos.
        cargarCategoriasDesdeCSV();
        cargarDesdeCSV();
        cargarComprasDesdeCSV();
    }


    // --- MÉTODOS DE CATEGORÍAS ---


    public String[] getCategoriasDisponibles() {
        return categoriasDisponibles.toArray(new String[0]);
    }

    public void agregarCategoriaSiNoExiste(String nuevaCategoria) {
        boolean existe = false;
        for (String cat : categoriasDisponibles) {
            if (cat.equalsIgnoreCase(nuevaCategoria)) {
                existe = true;
                break;
            }
        }
        if (!existe && !nuevaCategoria.trim().isEmpty()) {
            categoriasDisponibles.add(nuevaCategoria);
            guardarCategoriasEnCSV();
        }
    }

    public boolean eliminarCategoria(String categoriaAEliminar) {
        // No se permite eliminar una categoría que todavía usa algún producto.
        for (Producto p : listaProductos) {
            if (p.getCategoria().equalsIgnoreCase(categoriaAEliminar)) {
                return false;
            }
        }
        categoriasDisponibles.remove(categoriaAEliminar);
        guardarCategoriasEnCSV();
        return true;
    }


    //             ------------- METODOS PRODUCTOS ---------------
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



    public void registrarProducto(String nombre, double precio, int stock, String categoria, String rutaImagen) {

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
        Producto nuevoProducto = new Producto(nombre, precio, stock, categoria, nuevoId, rutaImagen);
        listaProductos.add(nuevoProducto);
        guardarEnCSV();

        System.out.println("Nuevo producto agregado: " + nombre + " con ID: " + nuevoId);
    }

    public ArrayList<Producto> filtrarPorCategoria(String categoria) {

        ArrayList<Producto> productosCategoria = new ArrayList<>();

        // Este método devuelve una nueva lista y no modifica el inventario original.
        for (Producto p : listaProductos) {

            if (p.getCategoria().equalsIgnoreCase(categoria)) {
                productosCategoria.add(p);
            }
        }

        return productosCategoria;
    }



    //         ----- CALCULOS BASICOS----


    public double calcularPrecioPromedioPorCategoria(String categoria) {
        // Devuelve -1 cuando no existen productos para evitar una división por cero.

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
        // Se usa en la ventana de "Cálculos básicos" para localizar el stock mínimo.

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
            writer.write("id,nombre,precio,stock,categoria,rutaImagen\n");

            for (Producto p : listaProductos) {
                writer.write(p.getId() + "," + p.getNombre() + "," + p.getPrecio() + "," + p.getStock() + "," + p.getCategoria() + "," + p.getRutaImagen() + "\n");
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
                String[] datos = linea.split(",", 6);

                if (datos.length < 5) {
                    continue;
                }

                String id = datos[0].trim();
                String nombre = datos[1].trim();
                double precio = Double.parseDouble(datos[2].trim());
                int stock = Integer.parseInt(datos[3].trim());
                String categoria = datos[4].trim();
                String rutaImagen = datos.length >= 6 ? datos[5].trim() : "";

                listaProductos.add(new Producto(nombre, precio, stock, categoria, id, rutaImagen));

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

    public Producto buscarProductoPorId(String idBuscado) {

        for (Producto producto : listaProductos) {

            if (producto.getId().equalsIgnoreCase(idBuscado)) {
                return producto;
            }
        }

        return null;
    }

    public boolean reservarUnidad(Producto producto) {
        return reservarUnidades(producto, 1);
    }

    // Reserva varias unidades de una sola vez
    public boolean reservarUnidades(Producto producto, int cantidad) {

        if (producto == null || cantidad <= 0) {
            return false;
        }

        if (!listaProductos.contains(producto)) {
            return false;
        }

        // No existe stock suficiente
        if (producto.getStock() < cantidad) {
            return false;
        }

        producto.setStock(
                producto.getStock() - cantidad
        );

        return true;
    }

    // Devuelve productos reservados al inventario
    public void devolverStock(
            Producto producto,
            int cantidad
    ) {

        if (producto == null || cantidad <= 0) {
            return;
        }

        if (listaProductos.contains(producto)) {

            producto.setStock(
                    producto.getStock() + cantidad
            );
        }
    }

    public void confirmarCompra() {
        guardarEnCSV();
    }

    public void registrarCompra(String rutUsuario, List<String> articulos, double totalPagado) {
        historialCompras.add(new Compra(rutUsuario, articulos, totalPagado));
        guardarComprasEnCSV();
        guardarEnCSV();
    }

    public List<Compra> getHistorialCompras() {
        return new ArrayList<>(historialCompras);
    }

    private void guardarComprasEnCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_COMPRAS_CSV))) {
            writer.write("rut,fechaHora,articulos,totalPagado\n");

            for (Compra compra : historialCompras) {
                String articulos = String.join(" | ", compra.getArticulos())
                        .replace("\n", " ")
                        .replace("\r", " ");
                writer.write(String.format("%s,%s,%s,%s\n",
                        compra.getRutUsuario(), compra.getFechaHora(), articulos, compra.getTotalPagado()));
            }
        } catch (IOException e) {
            System.out.println("No se pudo guardar el historial de compras: " + e.getMessage());
        }
    }

    private void cargarComprasDesdeCSV() {
        File archivo = new File(ARCHIVO_COMPRAS_CSV);
        if (!archivo.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            reader.readLine();
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",", 4);
                if (datos.length < 4) {
                    continue;
                }

                List<String> articulos = datos[2].trim().isEmpty()
                        ? new ArrayList<>()
                        : Arrays.asList(datos[2].split(" \\| "));
                historialCompras.add(new Compra(
                        datos[0].trim(),
                        LocalDateTime.parse(datos[1].trim()),
                        articulos,
                        Double.parseDouble(datos[3].trim())));
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("No se pudo leer el historial de compras: " + e.getMessage());
        }
    }

    // ==========================================
    // --- PERSISTENCIA DE CATEGORÍAS ---
    // ==========================================

    public void guardarCategoriasEnCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_CATEGORIAS_CSV))) {
            writer.write("categoria\n"); // Cabecera del archivo

            for (String cat : categoriasDisponibles) {
                writer.write(cat + "\n");
            }
        } catch (IOException e) {
            System.out.println("No se pudo guardar el archivo de categorías: " + e.getMessage());
        }
    }

    public double calcularValorTotalInventario() {
        // Valor total = suma de precio por unidades disponibles de cada producto.

        double valorTotal = 0;

        for (Producto p : listaProductos) {
            valorTotal = valorTotal + (p.getPrecio() * p.getStock());
        }

        return valorTotal;
    }

    public void cargarCategoriasDesdeCSV() {
        File archivo = new File(ARCHIVO_CATEGORIAS_CSV);

        // Si el archivo NO existe, creamos el archivo
        if (!archivo.exists()) {
            guardarCategoriasEnCSV(); // Las guardamos de inmediato para la próxima vez
            return;
        }

        // Si el archivo SÍ existe, lo leemos
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea = reader.readLine(); // Ignora la cabecera ("categoria")

            while ((linea = reader.readLine()) != null) {
                String categoriaLeida = linea.trim();

                if (!categoriaLeida.isEmpty()) {
                    this.categoriasDisponibles.add(categoriaLeida);
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo de categorías: " + e.getMessage());
        }
    }

    // ==========================================
    // --- MÉTODOS PARA VACIAR INVENTARIO ---
    // ==========================================

    public void vaciarInventarioCompleto() {
        // Limpia toda la lista de productos
        listaProductos.clear();
        guardarEnCSV();
    }

    public int eliminarProductosPorCategoria(String categoriaAEliminar) {
        int cantidadEliminada = 0;


        for (int i = listaProductos.size() - 1; i >= 0; i--) {
            Producto p = listaProductos.get(i);

            if (p.getCategoria().equalsIgnoreCase(categoriaAEliminar)) {
                listaProductos.remove(i);
                cantidadEliminada++;
            }
        }

        if (cantidadEliminada > 0) {
            guardarEnCSV();
        }
        return cantidadEliminada;
    }
}