package Backend;

public class Producto {

    private String nombre;
    private double precio;
    private int stock;
    private String categoria;
    private String id;
    private String rutaImagen;

    public Producto(String nombre, double precio, int stock, String categoria, String id) {
        this(nombre, precio, stock, categoria, id, "");
    }

    public Producto(String nombre, double precio, int stock, String categoria, String id, String rutaImagen) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.id= id;
        // Evita trabajar con null en la interfaz al mostrar o editar imágenes.
        this.rutaImagen = rutaImagen == null ? "" : rutaImagen;
    }
    public String getId(){ return id; }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getRutaImagen() {
        return rutaImagen;
    }

    public void setRutaImagen(String rutaImagen) {
        // El diálogo puede enviar una ruta vacía para quitar la imagen.
        this.rutaImagen = rutaImagen == null ? "" : rutaImagen;
    }

    @Override
    public String toString() {
        return "ID='" + id + '\'' +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                ", categoria='" + categoria + '\'';
    }
}