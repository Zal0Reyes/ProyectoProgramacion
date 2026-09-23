package Backend;

import java.util.HashMap;

public class Sistema {

    private HashMap<String, Usuario> mapaUsuarios;

    // Variable para saber quién está usando la aplicación en este momento
    private Usuario usuarioActual;

    public Sistema() {
        this.mapaUsuarios = new HashMap<>();
        this.usuarioActual = null;

        cargarUsuariosPorDefecto();
    }

    private void cargarUsuariosPorDefecto() {
        Administrador admin = new Administrador("11111111-1", "Admin Principal", "admin123", "admin@mitiendita.cl", "ADM-01");
        Cliente cliente = new Cliente("22222222-2", "Juan Pérez", "cliente123", "juan@gmail.com", "+56912345678", "Avenida Siempre Viva 742");

        mapaUsuarios.put(admin.getRut(), admin);
        mapaUsuarios.put(cliente.getRut(), cliente);
    }

    // Método para registrar un nuevo cliente
    public boolean registrarCliente(String rut, String nombre, String contrasena, String correo, String telefono, String direccion) {
        // Con HashMap, verificar si el RUT ya existe toma solo una línea y es súper rápido
        if (mapaUsuarios.containsKey(rut)) {
            return false; // Falla el registro porque el rut ya está registrado
        }

        Cliente nuevoCliente = new Cliente(rut, nombre, contrasena, correo, telefono, direccion);
        mapaUsuarios.put(rut, nuevoCliente);
        return true; // Registro exitoso
    }

    // Método para el Login
    public Usuario iniciarSesion(String rut, String contrasena) {
        // Obtenemos el usuario directamente usando el RUT
        Usuario u = mapaUsuarios.get(rut);

        // Si el usuario existe (no es null) y la contraseña coincide
        if (u != null && u.getContrasena().equals(contrasena)) {
            usuarioActual = u; // Guardamos en memoria quién inició sesión
            return u;          // Retornamos el usuario
        }

        return null; // Credenciales incorrectas o el RUT no existe
    }

    public void cerrarSesion() {
        usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public HashMap<String, Usuario> getMapaUsuarios() {
        return mapaUsuarios;
    }
}