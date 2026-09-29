package Backend;

public abstract class Usuario {
    protected String rut;
    protected String nombre;
    protected String contrasena;
    protected String correo;

    public Usuario(String rut, String nombre, String contrasena, String correo) {
        this.rut = rut;
        this.nombre = nombre;
        this.contrasena = contrasena;
        this.correo = correo;
    }

    // Getters
    public String getRut() { return rut; }
    public String getNombre() { return nombre; }
    public String getContrasena() { return contrasena; }
    public String getCorreo() { return correo; }

    // Setters
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    public void setCorreo(String correo) { this.correo = correo; }

    // Método abstracto que obliga a las clases hijas a definir qué tipo de usuario son
    public abstract String getTipoUsuario();
}