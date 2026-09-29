
package Backend;

public class Administrador extends Usuario {
    private String codigoEmpleado;

    public Administrador(String rut, String nombre, String contrasena, String correo, String codigoEmpleado) {
        super(rut, nombre, contrasena, correo);
        this.codigoEmpleado = codigoEmpleado;
    }

    public String getCodigoEmpleado() { return codigoEmpleado; }
    public void setCodigoEmpleado(String codigoEmpleado) { this.codigoEmpleado = codigoEmpleado; }

    @Override
    public String getTipoUsuario() {
        return "Administrador";
    }
}