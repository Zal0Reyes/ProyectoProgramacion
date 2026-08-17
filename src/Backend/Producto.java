package Backend;

public class Producto {

    private String nombre;
    private double precio;
    private int stock;
    private String categoria;
    private String id;

    public Producto(String nombre, double precio, int stock, String categoria, String id) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
        this.id= id;
    }

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

    @Override
    public String toString() {
        return "Backend.Producto [" +
                "nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", stock=" + stock +
                ", categoria='" + categoria + '\'' +
                ']';
    }
}