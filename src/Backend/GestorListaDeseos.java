package Backend;

import java.io.*;
import java.util.*;

public class GestorListaDeseos {

    // usuarioId (RUT o nombre) -> Conjunto de IDs de productos favoritos
    private final Map<String, Set<String>> deseosPorUsuario;
    // idProducto -> Conjunto de usuarioIds suscritos a reposición
    private final Map<String, Set<String>> suscriptoresReposicion;
    private final Map<String, List<String>> notificacionesPendientes;

    private static final String ARCHIVO_PENDIENTES = "notificaciones_pendientes.csv";
    private static final String ARCHIVO_DESEOS = "lista_deseos.csv";
    private static final String ARCHIVO_SUSCRIPCIONES = "suscripciones_stock.csv";

    public GestorListaDeseos() {
        this.deseosPorUsuario = new HashMap<>();
        this.suscriptoresReposicion = new HashMap<>();
        this.notificacionesPendientes = new HashMap<>();

        cargarDeseosDesdeCSV();
        cargarSuscripcionesDesdeCSV();
        cargarPendientesDesdeCSV();
    }

    // =========================================================
    // 1. LISTA DE DESEOS (FAVORITOS)
    // =========================================================

    public synchronized boolean agregarDeseo(String usuarioId, String idProducto) {
        if (esInvalido(usuarioId) || esInvalido(idProducto)) return false;
        String keyUser = usuarioId.trim().toLowerCase();
        String keyProd = idProducto.trim().toUpperCase();

        deseosPorUsuario.computeIfAbsent(keyUser, k -> new LinkedHashSet<>());
        boolean agregado = deseosPorUsuario.get(keyUser).add(keyProd);
        if (agregado) {
            guardarDeseosEnCSV();
        }
        return agregado;
    }

    public synchronized boolean eliminarDeseo(String usuarioId, String idProducto) {
        if (esInvalido(usuarioId) || esInvalido(idProducto)) return false;
        Set<String> lista = deseosPorUsuario.get(usuarioId.trim().toLowerCase());
        if (lista != null && lista.remove(idProducto.trim().toUpperCase())) {
            guardarDeseosEnCSV();
            return true;
        }
        return false;
    }

    public synchronized boolean estaEnListaDeseos(String usuarioId, String idProducto) {
        if (esInvalido(usuarioId) || esInvalido(idProducto)) return false;
        Set<String> lista = deseosPorUsuario.get(usuarioId.trim().toLowerCase());
        return lista != null && lista.contains(idProducto.trim().toUpperCase());
    }

    public synchronized List<String> obtenerIdsDeseosUsuario(String usuarioId) {
        if (esInvalido(usuarioId)) return new ArrayList<>();
        Set<String> lista = deseosPorUsuario.get(usuarioId.trim().toLowerCase());
        return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
    }

    // Devuelve todos los usuarios que tienen un producto en su lista de deseos (para aviso de baja de precio)
    public synchronized Set<String> obtenerUsuariosConProductoEnDeseos(String idProducto) {
        Set<String> interesados = new HashSet<>();
        if (esInvalido(idProducto)) return interesados;
        String keyProd = idProducto.trim().toUpperCase();

        for (Map.Entry<String, Set<String>> entry : deseosPorUsuario.entrySet()) {
            if (entry.getValue().contains(keyProd)) {
                interesados.add(entry.getKey());
            }
        }
        return interesados;
    }

    // =========================================================
    // 2. SUSCRIPCIONES A REPOSICIÓN ("AVÍSAME CUANDO LLEGUE")
    // =========================================================

    public synchronized boolean suscribirReposicion(String usuarioId, String idProducto) {
        if (esInvalido(usuarioId) || esInvalido(idProducto)) return false;
        String keyUser = usuarioId.trim().toLowerCase();
        String keyProd = idProducto.trim().toUpperCase();

        suscriptoresReposicion.computeIfAbsent(keyProd, k -> new LinkedHashSet<>());
        boolean agregado = suscriptoresReposicion.get(keyProd).add(keyUser);
        if (agregado) {
            guardarSuscripcionesEnCSV();
        }
        return agregado;
    }

    public synchronized boolean estaSuscrito(String usuarioId, String idProducto) {
        if (esInvalido(usuarioId) || esInvalido(idProducto)) return false;
        Set<String> subs = suscriptoresReposicion.get(idProducto.trim().toUpperCase());
        return subs != null && subs.contains(usuarioId.trim().toLowerCase());
    }

    // Para mostrar en VistaPrincipalTienda: "X clientes esperando"
    public synchronized int contarClientesEsperando(String idProducto) {
        if (esInvalido(idProducto)) return 0;
        Set<String> subs = suscriptoresReposicion.get(idProducto.trim().toUpperCase());
        return subs == null ? 0 : subs.size();
    }

    // Extrae y limpia los suscriptores cuando el Admin repone stock > 0
    public synchronized Set<String> consumirSuscriptoresReposicion(String idProducto) {
        if (esInvalido(idProducto)) return new HashSet<>();
        Set<String> subs = suscriptoresReposicion.remove(idProducto.trim().toUpperCase());
        if (subs != null && !subs.isEmpty()) {
            guardarSuscripcionesEnCSV();
            return subs;
        }
        return new HashSet<>();
    }

    // =========================================================
    // 3. PERSISTENCIA EN CSV (SIN LIBRERÍAS EXTERNAS)
    // =========================================================

    public synchronized void recargarDesdeCSV() {
        deseosPorUsuario.clear();
        suscriptoresReposicion.clear();
        notificacionesPendientes.clear();

        cargarDeseosDesdeCSV();
        cargarSuscripcionesDesdeCSV();
        cargarPendientesDesdeCSV();
    }

    private void guardarDeseosEnCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_DESEOS))) {
            writer.write("usuarioId,productos\n");
            for (Map.Entry<String, Set<String>> entry : deseosPorUsuario.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    writer.write(entry.getKey() + "," + String.join("|", entry.getValue()) + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error guardando lista_deseos.csv: " + e.getMessage());
        }
    }

    private void cargarDeseosDesdeCSV() {
        File archivo = new File(ARCHIVO_DESEOS);
        if (!archivo.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            reader.readLine();
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",", 2);
                if (datos.length < 2 || datos[1].trim().isEmpty()) continue;
                Set<String> prods = new LinkedHashSet<>(Arrays.asList(datos[1].trim().split("\\|")));
                deseosPorUsuario.put(datos[0].trim().toLowerCase(), prods);
            }
        } catch (IOException e) {
            System.out.println("Error leyendo lista_deseos.csv: " + e.getMessage());
        }
    }

    private void guardarSuscripcionesEnCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_SUSCRIPCIONES))) {
            writer.write("idProducto,usuarios\n");
            for (Map.Entry<String, Set<String>> entry : suscriptoresReposicion.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    writer.write(entry.getKey() + "," + String.join("|", entry.getValue()) + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error guardando suscripciones_stock.csv: " + e.getMessage());
        }
    }

    private void cargarSuscripcionesDesdeCSV() {
        File archivo = new File(ARCHIVO_SUSCRIPCIONES);
        if (!archivo.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            reader.readLine();
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",", 2);
                if (datos.length < 2 || datos[1].trim().isEmpty()) continue;
                Set<String> users = new LinkedHashSet<>(Arrays.asList(datos[1].trim().split("\\|")));
                suscriptoresReposicion.put(datos[0].trim().toUpperCase(), users);
            }
        } catch (IOException e) {
            System.out.println("Error leyendo suscripciones_stock.csv: " + e.getMessage());
        }
    }
    // =========================================================
    // 4. NOTIFICACIONES PENDIENTES (PARA USUARIOS OFFLINE)
    // =========================================================

    public synchronized void encolarNotificacion(String usuarioId, String mensajeProtocolo) {
        if (esInvalido(usuarioId) || esInvalido(mensajeProtocolo)) return;
        String keyUser = usuarioId.trim().toLowerCase();
        notificacionesPendientes.computeIfAbsent(keyUser, k -> new ArrayList<>()).add(mensajeProtocolo);
        guardarPendientesEnCSV();
    }

    public synchronized List<String> consumirNotificacionesPendientes(String usuarioId) {
        if (esInvalido(usuarioId)) return new ArrayList<>();
        String keyUser = usuarioId.trim().toLowerCase();
        List<String> lista = notificacionesPendientes.remove(keyUser);
        if (lista != null && !lista.isEmpty()) {
            guardarPendientesEnCSV();
            return lista;
        }
        return new ArrayList<>();
    }

    private void guardarPendientesEnCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ARCHIVO_PENDIENTES))) {
            writer.write("usuarioId,mensajes\n");
            for (Map.Entry<String, List<String>> entry : notificacionesPendientes.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    writer.write(entry.getKey() + "," + String.join("||", entry.getValue()) + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("Error guardando notificaciones_pendientes.csv: " + e.getMessage());
        }
    }

    private void cargarPendientesDesdeCSV() {
        File archivo = new File(ARCHIVO_PENDIENTES);
        if (!archivo.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
            reader.readLine();
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",", 2);
                if (datos.length < 2 || datos[1].trim().isEmpty()) continue;
                List<String> msgs = new ArrayList<>(Arrays.asList(datos[1].trim().split("\\|\\|")));
                notificacionesPendientes.put(datos[0].trim().toLowerCase(), msgs);
            }
        } catch (IOException e) {
            System.out.println("Error leyendo notificaciones_pendientes.csv: " + e.getMessage());
        }
    }
    private boolean esInvalido(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}