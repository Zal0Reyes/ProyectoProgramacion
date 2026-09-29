package Backend;

public class Cliente extends Usuario {
    private String telefono;
    private String direccion;

    public Cliente(String rut, String nombre, String contrasena, String correo, String telefono, String direccion) {
        super(rut, nombre, contrasena, correo); // Llama al constructor de Usuario
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    @Override
    public String getTipoUsuario() {
        return "Cliente";
    }
}