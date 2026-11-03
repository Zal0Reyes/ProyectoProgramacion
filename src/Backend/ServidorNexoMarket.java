package Backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServidorNexoMarket {

    private static final int PUERTO = 65432;

    // =====================================================
    // ESTRUCTURAS DE DATOS GLOBALES (MEMORIA DEL SERVIDOR)
    // =====================================================

    // --- INVENTARIO: Inventario central compartido por todos ---
    private static final Inventario inventarioServidor = new Inventario();

    // --- CLIENTES: Lista de conexiones activas (Sockets) ---
    private static final List<PrintWriter> clientes = new ArrayList<>();

    // --- RESERVAS: Guarda lo que tiene apartado cada cliente ---
    // Estructura: Cliente (PrintWriter) -> (ID producto -> cantidad)
    private static final Map<PrintWriter, Map<String, Integer>> reservasClientes = new HashMap<>();

    // =====================================================
    // MÉTODO PRINCIPAL: INICIO DEL SERVIDOR
    // =====================================================
    public static void main(String[] args) {
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("================================");
            System.out.println("SERVIDOR NEXOMARKET INICIADO");
            System.out.println("Puerto: " + PUERTO);
            System.out.println("Esperando clientes...");
            System.out.println("================================");

            // --- BUCLE INFINITO: Acepta nuevas conexiones ---
            while (true) {
                Socket socketCliente = servidor.accept();
                System.out.println("Nuevo cliente conectado: " + socketCliente.getInetAddress());

                // Se crea un hilo (Thread) por cada cliente para atenderlos en simultáneo
                Thread hiloCliente = new Thread(() -> atenderCliente(socketCliente));
                hiloCliente.start();
            }

        } catch (IOException e) {
            System.out.println("Error en el servidor: " + e.getMessage());
        }
    }

    // =====================================================
    // RECEPCIÓN DE MENSAJES (EL CEREBRO DEL SERVIDOR)
    // =====================================================
    private static void atenderCliente(Socket socketCliente) {
        PrintWriter salida = null;

        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            salida = new PrintWriter(socketCliente.getOutputStream(), true);

            // --- REGISTRO: Guardamos al nuevo cliente ---
            agregarCliente(salida);
            salida.println("{\"type\":\"message\",\"content\":\"Conectado al servidor NexoMarket\"}");

            String mensaje;

            // --- BUCLE DE ESCUCHA: Lee todo lo que envía este cliente ---
            while ((mensaje = entrada.readLine()) != null) {
                if (mensaje.trim().isEmpty()) continue;

                System.out.println("Mensaje recibido: " + mensaje);

                // =================================================
                // ENRUTADOR DE COMANDOS
                // =================================================

                // --- COMANDO: Cliente quiere apartar un producto ---
                if (mensaje.startsWith("RESERVAR;")) {
                    procesarReserva(mensaje, salida);
                }
                // --- COMANDO: Cliente devuelve un producto del carrito ---
                else if (mensaje.startsWith("DEVOLVER;")) {
                    procesarDevolucion(mensaje, salida);
                }
                // --- COMANDO: Admin crea o edita un producto ---
                else if (mensaje.startsWith("ADMIN_GUARDAR_PRODUCTO;")) {
                    procesarProductoAdmin(mensaje, salida);
                }
                // --- COMANDO: Admin elimina un producto del catálogo ---
                else if (mensaje.startsWith("ADMIN_ELIMINAR_PRODUCTO;")) {
                    procesarEliminarProductoAdmin(mensaje, salida);
                }
                // --- COMANDO: Cliente finaliza su compra ---
                else if (mensaje.startsWith("CONFIRMAR_COMPRA;")) {
                    procesarCompra(mensaje, salida);
                }
                // --- COMANDO: Cliente recién conectado pide el catálogo actual ---
                else if (mensaje.equals("PEDIR_STOCK")) {
                    enviarStockActual(salida);
                }
                // --- COMANDO: Admin pide ver los carritos en vivo ---
                else if (mensaje.equals("PEDIR_CARRITOS")) {
                    enviarCarritosActivos(salida);
                }
                // --- COMANDO: Mensaje genérico (Se reenvía a todos) ---
                else {
                    enviarATodos(mensaje, salida);
                }
            }

        } catch (IOException e) {
            System.out.println("Un cliente se desconectó de forma abrupta.");
        } finally {
            // --- LIMPIEZA: Si el cliente se va, devolvemos sus reservas ---
            if (salida != null) {
                eliminarCliente(salida);
            }
            try {
                socketCliente.close();
            } catch (IOException e) {
                System.out.println("Error cerrando socket del cliente.");
            }
        }
    }

    // =====================================================
    // MÉTODOS DE PROCESAMIENTO DE COMANDOS
    // =====================================================

    // --- ACCIÓN: CONFIRMAR COMPRA ---
    private static void procesarCompra(String mensaje, PrintWriter cliente) {
        String[] datos = mensaje.split(";", 6);

        if (datos.length != 6) {
            cliente.println("ERROR_COMPRA;Datos incorrectos");
            return;
        }

        // --- CONCURRENCIA: Bloqueamos el inventario para evitar choques ---
        synchronized (inventarioServidor) {
            Map<String, Integer> reservas = reservasClientes.get(cliente);

            if (reservas == null || reservas.isEmpty()) {
                cliente.println("ERROR_COMPRA;No hay productos reservados");
                return;
            }

            // La compra se confirma. NO se devuelve el stock (se vacía el carrito virtual)
            reservas.clear();
            inventarioServidor.guardarEnCSV();

            // --- RESPUESTA: Avisar al comprador que todo salió bien ---
            cliente.println("COMPRA_OK");

            // --- RESPUESTA: Avisar a todos (y al Admin) sobre la nueva venta ---
            enviarATodos(
                    "VENTA_REALIZADA;" + datos[1] + ";" + datos[2] + ";" + datos[3] + ";" + datos[4] + ";" + datos[5],
                    cliente
            );
            System.out.println("Compra confirmada y enviada al ADMIN.");
            notificarCarritosActivosATodos();
        }
    }

    // --- ACCIÓN: ADMIN GUARDA PRODUCTO ---
    private static void procesarProductoAdmin(String mensaje, PrintWriter admin) {
        String[] datos = mensaje.split(";", 7);
        if (datos.length != 7) return;

        String id = datos[1];
        String nombre = datos[2];
        double precio;
        int stock;

        try {
            precio = Double.parseDouble(datos[3]);
            stock = Integer.parseInt(datos[4]);
        } catch (NumberFormatException e) {
            return;
        }

        String categoria = datos[5];
        String rutaImagen = datos[6];

        // --- CONCURRENCIA: Modificamos el catálogo real ---
        synchronized (inventarioServidor) {
            inventarioServidor.sincronizarProductoDesdeServidor(id, nombre, precio, stock, categoria, rutaImagen);
            inventarioServidor.guardarEnCSV();

            // --- RESPUESTA: Avisamos a los clientes que un producto cambió ---
            enviarATodos("PRODUCTO_ACTUALIZADO;" + id + ";" + nombre + ";" + precio + ";" + stock + ";" + categoria + ";" + rutaImagen, admin);
        }
        System.out.println("Producto actualizado por ADMIN: " + id);
    }

    // --- ACCIÓN: ADMIN ELIMINA PRODUCTO ---
    private static void procesarEliminarProductoAdmin(String mensaje, PrintWriter admin) {
        String[] datos = mensaje.split(";");
        if (datos.length != 2) return;

        String idProducto = datos[1];

        synchronized (inventarioServidor) {
            boolean eliminado = inventarioServidor.eliminarProductoPorId(idProducto);
            if (eliminado) {
                enviarATodos("PRODUCTO_ELIMINADO;" + idProducto, admin);
            }
        }
        System.out.println("ADMIN eliminó producto: " + idProducto);
    }

    // --- ACCIÓN: ENVIAR STOCK ACTUAL INICIAL ---
    private static void enviarStockActual(PrintWriter cliente) {
        synchronized (inventarioServidor) {
            for (Producto producto : inventarioServidor.getProductos()) {
                cliente.println("STOCK;" + producto.getId() + ";" + producto.getStock());
            }
        }
        System.out.println("Stock actual enviado a un cliente.");
    }

    // --- ACCIÓN: RESERVAR PRODUCTO (AGREGAR AL CARRITO) ---
    private static void procesarReserva(String mensaje, PrintWriter cliente) {
        String[] datos = mensaje.split(";");
        if (datos.length != 3) {
            cliente.println("ERROR;Formato de reserva incorrecto");
            return;
        }

        String idProducto = datos[1];
        int cantidad;

        try {
            cantidad = Integer.parseInt(datos[2]);
        } catch (NumberFormatException e) {
            cliente.println("ERROR;Cantidad incorrecta");
            return;
        }

        if (cantidad <= 0) {
            cliente.println("ERROR;Cantidad incorrecta");
            return;
        }

        // --- CONCURRENCIA: Modificamos stock apartando unidades ---
        synchronized (inventarioServidor) {
            Producto producto = inventarioServidor.buscarProductoPorId(idProducto);

            if (producto == null) {
                cliente.println("ERROR;Producto no encontrado");
                return;
            }

            // Intentamos reservar
            boolean reservado = inventarioServidor.reservarUnidades(producto, cantidad);

            if (!reservado) {
                // No hay suficiente stock
                cliente.println("ERROR_STOCK;" + idProducto + ";" + producto.getStock());
                return;
            }

            // --- REGISTRO: Anotamos la reserva en la cuenta de este cliente ---
            Map<String, Integer> reservasCliente = reservasClientes.get(cliente);
            if (reservasCliente != null) {
                int cantidadActual = reservasCliente.getOrDefault(idProducto, 0);
                reservasCliente.put(idProducto, cantidadActual + cantidad);
            }

            inventarioServidor.guardarEnCSV();
            int nuevoStock = producto.getStock();

            System.out.println(producto.getNombre() + " - Reservado: " + cantidad + " - Stock nuevo: " + nuevoStock);

            // --- RESPUESTA: Confirmamos al que reservó ---
            cliente.println("RESERVA_OK;" + idProducto + ";" + cantidad + ";" + nuevoStock);

            // --- RESPUESTA: Avisamos a los demás del nuevo stock ---
            enviarATodos("STOCK;" + idProducto + ";" + nuevoStock, cliente);
            notificarCarritosActivosATodos();
        }
    }

    // --- ACCIÓN: DEVOLVER PRODUCTO (QUITAR DEL CARRITO) ---
    private static void procesarDevolucion(String mensaje, PrintWriter cliente) {
        String[] datos = mensaje.split(";");

        if (datos.length != 3) {
            cliente.println("ERROR;Formato de devolución incorrecto");
            return;
        }

        String idProducto = datos[1];
        int cantidad;

        try {
            cantidad = Integer.parseInt(datos[2]);
        } catch (NumberFormatException e) {
            cliente.println("ERROR;Cantidad incorrecta");
            return;
        }

        if (cantidad <= 0) {
            cliente.println("ERROR_DEVOLUCION;" + idProducto);
            return;
        }

        synchronized (inventarioServidor) {
            Producto producto = inventarioServidor.buscarProductoPorId(idProducto);

            if (producto == null) {
                cliente.println("ERROR;Producto no encontrado");
                return;
            }

            // Verificamos cuánto tiene realmente reservado este cliente
            Map<String, Integer> reservasCliente = reservasClientes.get(cliente);
            if (reservasCliente == null) {
                cliente.println("ERROR_DEVOLUCION;" + idProducto);
                return;
            }

            int cantidadReservada = reservasCliente.getOrDefault(idProducto, 0);

            // No puede devolver más de lo que reservó
            if (cantidad > cantidadReservada) {
                cliente.println("ERROR_DEVOLUCION;" + idProducto);
                return;
            }

            // Devolver stock real al inventario
            inventarioServidor.devolverStock(producto, cantidad);

            // Actualizar la lista de reservas
            int nuevaCantidadReservada = cantidadReservada - cantidad;
            if (nuevaCantidadReservada == 0) {
                reservasCliente.remove(idProducto);
            } else {
                reservasCliente.put(idProducto, nuevaCantidadReservada);
            }

            inventarioServidor.guardarEnCSV();
            int nuevoStock = producto.getStock();

            System.out.println(producto.getNombre() + " - Devuelto: " + cantidad + " - Stock nuevo: " + nuevoStock);

            // --- RESPUESTA: Confirmar devolución ---
            cliente.println("DEVOLUCION_OK;" + idProducto + ";" + cantidad + ";" + nuevoStock);

            // --- RESPUESTA: Avisar a los demás del stock recuperado ---
            enviarATodos("STOCK;" + idProducto + ";" + nuevoStock, cliente);
            notificarCarritosActivosATodos();
        }
    }

    // --- ACCIÓN (NUEVA): ENVIAR CARRITOS EN VIVO AL ADMIN ---
    private static void enviarCarritosActivos(PrintWriter admin) {
        StringBuilder datosCarritos = new StringBuilder();
        int contadorCliente = 1;
        boolean hayCarritos = false;

        // --- CONCURRENCIA: Leemos las reservas actuales ---
        synchronized (inventarioServidor) {
            for (Map.Entry<PrintWriter, Map<String, Integer>> entradaCliente : reservasClientes.entrySet()) {
                Map<String, Integer> reservas = entradaCliente.getValue();

                if (reservas != null && !reservas.isEmpty()) {
                    if (hayCarritos) datosCarritos.append("|");

                    datosCarritos.append("Cliente ").append(contadorCliente).append(":");
                    boolean primerProducto = true;

                    for (Map.Entry<String, Integer> producto : reservas.entrySet()) {
                        if (!primerProducto) datosCarritos.append("-");

                        datosCarritos.append(producto.getKey()).append(",").append(producto.getValue());
                        primerProducto = false;
                    }
                    hayCarritos = true;
                }
                contadorCliente++;
            }
        }

        // --- RESPUESTA: Mandamos la lista al Admin ---
        if (!hayCarritos) {
            admin.println("CARRITOS_ACTIVOS;VACIO");
        } else {
            admin.println("CARRITOS_ACTIVOS;" + datosCarritos.toString());
        }
        System.out.println("Lista de carritos activos enviada al ADMIN.");
    }

    // =====================================================
    // MÉTODOS DE GESTIÓN DE CONEXIONES
    // =====================================================

    // --- MÉTODO: Agregar nuevo cliente a la lista ---
    private static synchronized void agregarCliente(PrintWriter cliente) {
        clientes.add(cliente);
        reservasClientes.put(cliente, new HashMap<>());
        System.out.println("Clientes conectados: " + clientes.size());
    }

    // --- MÉTODO: Eliminar cliente y liberar sus reservas ---
    private static synchronized void eliminarCliente(PrintWriter cliente) {
        Map<String, Integer> reservas = reservasClientes.get(cliente);

        if (reservas != null) {
            // --- CONCURRENCIA: Devolvemos todo lo que tenía en el carrito ---
            synchronized (inventarioServidor) {
                for (Map.Entry<String, Integer> entrada : reservas.entrySet()) {
                    String idProducto = entrada.getKey();
                    int cantidad = entrada.getValue();

                    Producto producto = inventarioServidor.buscarProductoPorId(idProducto);
                    if (producto != null) {
                        inventarioServidor.devolverStock(producto, cantidad);
                        System.out.println("Cliente desconectado: se devolvieron " + cantidad + " unidades de " + producto.getNombre());
                        enviarATodos("STOCK;" + idProducto + ";" + producto.getStock(), cliente);
                    }
                }
                inventarioServidor.guardarEnCSV();
            }
        }
        notificarCarritosActivosATodos();
        reservasClientes.remove(cliente);
        clientes.remove(cliente);
        System.out.println("Clientes conectados: " + clientes.size());
    }

    // --- MÉTODO: Difundir mensaje a todos menos al emisor ---
    private static synchronized void enviarATodos(String mensaje, PrintWriter emisor) {
        for (PrintWriter cliente : clientes) {
            if (cliente != emisor) {
                cliente.println(mensaje);
            }
        }
    }
    private static void notificarCarritosActivosATodos() {
        StringBuilder datosCarritos = new StringBuilder();
        int contadorCliente = 1;
        boolean hayCarritos = false;

        synchronized (inventarioServidor) {
            for (Map.Entry<PrintWriter, Map<String, Integer>> entradaCliente : reservasClientes.entrySet()) {
                Map<String, Integer> reservas = entradaCliente.getValue();
                if (reservas != null && !reservas.isEmpty()) {
                    if (hayCarritos) datosCarritos.append("|");

                    datosCarritos.append("Cliente ").append(contadorCliente).append(":");
                    boolean primerProducto = true;

                    for (Map.Entry<String, Integer> producto : reservas.entrySet()) {
                        if (!primerProducto) datosCarritos.append("-");
                        datosCarritos.append(producto.getKey()).append(",").append(producto.getValue());
                        primerProducto = false;
                    }
                    hayCarritos = true;
                }
                contadorCliente++;
            }
        }

        String mensaje = !hayCarritos ? "CARRITOS_ACTIVOS;VACIO" : "CARRITOS_ACTIVOS;" + datosCarritos.toString();
        // Al enviar 'null' como emisor, el servidor le envía la actualización a todos los sockets conectados
        enviarATodos(mensaje, null);
    }
}