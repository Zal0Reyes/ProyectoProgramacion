package Backend;

import java.io.*;
import java.util.HashMap;

public class Sistema {

    private HashMap<String, Usuario> mapaUsuarios;
    private static final String ARCHIVO_USUARIOS = "usuarios.csv";
    // Variable para saber quién está usando la aplicación en este momento
    private Usuario usuarioActual;

    public Sistema() {
        this.mapaUsuarios = new HashMap<>();
        this.usuarioActual = null;

        cargarUsuariosPorDefecto();
        cargarUsuariosDesdeCSV();

        // Si el archivo no existía o estaba vacío, cargamos los por defecto y creamos el archivo
        if (mapaUsuarios.isEmpty()) {
            cargarUsuariosPorDefecto();
            guardarUsuariosEnCSV();
        }
    }

    private void cargarUsuariosPorDefecto() {
        Administrador admin = new Administrador("1", "Admin Principal", "1", "admin@mitiendita.cl", "ADM-01");
        Cliente cliente = new Cliente("2", "Joaquin", "2", "juaco@gmail.com", "+56912345678", "Avenida Siempre Viva 742");

        mapaUsuarios.put(admin.getRut(), admin);
        mapaUsuarios.put(cliente.getRut(), cliente);
    }
    // ==========================================
    // --- MÉTODOS DE PERSISTENCIA (CSV) ---
    // ==========================================

    public void guardarUsuariosEnCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_USUARIOS))) {
            writer.write("tipo,rut,nombre,contrasena,correo,telefono,direccion,codigoEmpleado\n");

            // Recorremos el HashMap de usuarios
            for (Usuario u : mapaUsuarios.values()) {
                if (u instanceof Cliente) {
                    Cliente c = (Cliente) u;
                    // Cliente no tiene código empleado, lo dejamos vacío al final
                    writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,\n",
                            c.getTipoUsuario(), c.getRut(), c.getNombre(),
                            c.getContrasena(), c.getCorreo(), c.getTelefono(), c.getDireccion()));
                }
                else if (u instanceof Administrador) {
                    Administrador a = (Administrador) u;
                    // Admin no tiene teléfono ni dirección, dejamos esos espacios vacíos
                    writer.write(String.format("%s,%s,%s,%s,%s,,,%s\n",
                            a.getTipoUsuario(), a.getRut(), a.getNombre(),
                            a.getContrasena(), a.getCorreo(), a.getCodigoEmpleado()));
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo guardar el archivo de usuarios: " + e.getMessage());
        }
    }

    private void cargarUsuariosDesdeCSV() {
        File archivo = new File(ARCHIVO_USUARIOS);

        if (!archivo.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            String linea = reader.readLine();

            while ((linea = reader.readLine()) != null) {

                String[] datos = linea.split(",", -1);

                if (datos.length < 8) continue; // Por si hay líneas corruptas

                String tipo = datos[0].trim();
                String rut = datos[1].trim();
                String nombre = datos[2].trim();
                String contrasena = datos[3].trim();
                String correo = datos[4].trim();

                if (tipo.equals("Cliente")) {
                    String telefono = datos[5].trim();
                    String direccion = datos[6].trim();
                    mapaUsuarios.put(rut, new Cliente(rut, nombre, contrasena, correo, telefono, direccion));
                }
                else if (tipo.equals("Administrador")) {
                    String codigoEmpleado = datos[7].trim();
                    mapaUsuarios.put(rut, new Administrador(rut, nombre, contrasena, correo, codigoEmpleado));
                }
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo de usuarios: " + e.getMessage());
        }
    }


    // ==========================================
    // --- GESTIÓN DE USUARIOS Y SESIÓN ---
    // ==========================================

    public boolean registrarCliente(String rut, String nombre, String contrasena, String correo, String telefono, String direccion) {
        if (mapaUsuarios.containsKey(rut)) {
            return false; // Falla el registro porque el rut ya está registrado
        }

        Cliente nuevoCliente = new Cliente(rut, nombre, contrasena, correo, telefono, direccion);
        mapaUsuarios.put(rut, nuevoCliente);

        guardarUsuariosEnCSV();

        return true;
    }

    // Dejo este método preparado para cuando hagas la vista de agregar admins
    public boolean registrarAdministrador(String rut, String nombre, String contrasena, String correo, String codigoEmpleado) {
        if (mapaUsuarios.containsKey(rut)) {
            return false;
        }

        Administrador nuevoAdmin = new Administrador(rut, nombre, contrasena, correo, codigoEmpleado);
        mapaUsuarios.put(rut, nuevoAdmin);
        guardarUsuariosEnCSV();

        return true;
    }

    public Usuario iniciarSesion(String rut, String contrasena) {
        Usuario u = mapaUsuarios.get(rut);

        if (u != null && u.getContrasena().equals(contrasena)) {
            usuarioActual = u;
            return u;
        }

        return null;
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