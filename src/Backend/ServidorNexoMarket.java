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
    // ESTRUCTURAS DE DATOS GLOBALES
    // =====================================================

    // Inventario central compartido
    private static final Inventario inventarioServidor = new Inventario();

    // Gestor central de cupones
    private static final GestorCupones gestorCuponesServidor = new GestorCupones();

    // Clientes conectados
    private static final List<PrintWriter> clientes = new ArrayList<>();

    // Cliente -> (ID producto -> cantidad reservada)
    private static final Map<PrintWriter, Map<String, Integer>> reservasClientes = new HashMap<>();

    private static int tiempoReservaSegundos = 60;
    // =====================================================
    // INICIO DEL SERVIDOR
    // =====================================================

    public static void main(String[] args) {

        try (ServerSocket servidor =
                     new ServerSocket(PUERTO)) {

            System.out.println(
                    "================================"
            );

            System.out.println(
                    "SERVIDOR NEXOMARKET INICIADO"
            );

            System.out.println(
                    "Puerto: " + PUERTO
            );

            System.out.println(
                    "Esperando clientes..."
            );

            System.out.println(
                    "================================"
            );


            while (true) {

                Socket socketCliente =
                        servidor.accept();


                System.out.println(
                        "Nuevo cliente conectado: "
                                + socketCliente.getInetAddress()
                );

                Thread hiloCliente = new Thread(() -> atenderCliente(socketCliente));
                hiloCliente.start();
            }

        } catch (IOException e) {

            System.out.println(
                    "Error en el servidor: "
                            + e.getMessage()
            );
        }
    }


    // =====================================================
    // ATENDER CLIENTE
    // =====================================================

    private static void atenderCliente(Socket socketCliente) {

        PrintWriter salida = null;

        try {

            BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            salida = new PrintWriter(socketCliente.getOutputStream(), true);

            agregarCliente(salida);


            salida.println("{\"type\":\"message\"," + "\"content\":\"Conectado al servidor NexoMarket\"}");

            String mensaje;

            while ((mensaje = entrada.readLine()) != null) {

                if (mensaje.trim().isEmpty()) {
                    continue;
                }


                System.out.println(
                        "Mensaje recibido: "
                                + mensaje
                );


                // =========================================
                // ENRUTADOR DE COMANDOS
                // =========================================

                if (mensaje.startsWith(
                        "RESERVAR;")) {

                    procesarReserva(
                            mensaje,
                            salida
                    );

                } else if (mensaje.startsWith(
                        "DEVOLVER;")) {

                    procesarDevolucion(
                            mensaje,
                            salida
                    );

                } else if (mensaje.startsWith(
                        "ADMIN_GUARDAR_PRODUCTO;")) {

                    procesarProductoAdmin(
                            mensaje,
                            salida
                    );

                } else if (mensaje.startsWith(
                        "ADMIN_ELIMINAR_PRODUCTO;")) {

                    procesarEliminarProductoAdmin(
                            mensaje,
                            salida
                    );

                } else if (mensaje.startsWith(
                        "CONFIRMAR_COMPRA;")) {

                    procesarCompra(
                            mensaje,
                            salida
                    );

                } else if (mensaje.equals(
                        "PEDIR_STOCK")) {

                    enviarStockActual(
                            salida
                    );

                } else if (mensaje.equals(
                        "PEDIR_CARRITOS")) {

                    enviarCarritosActivos(
                            salida
                    );

                } else if (mensaje.startsWith(
                        "ADMIN_VACIAR_CARRITO;")) {

                    procesarVaciarCarritoAdmin(
                            mensaje,
                            salida
                    );

                }
                else if (mensaje.startsWith("ADMIN_SET_TIEMPO_CARRITO;")) {
                    try {
                        int nuevosSegundos = Integer.parseInt(mensaje.split(";")[1]);
                        if (nuevosSegundos > 0) {
                            tiempoReservaSegundos = nuevosSegundos;
                            enviarATodos("TIEMPO_RESERVA;" + tiempoReservaSegundos, null);
                            System.out.println("Tiempo de reserva actualizado a: " + tiempoReservaSegundos + " seg.");
                        }
                    } catch (Exception ignored) {}
                }

                else {

                    enviarATodos(
                            mensaje,
                            salida
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Un cliente se desconectó de forma abrupta."
            );

        } finally {

            if (salida != null) {

                eliminarCliente(
                        salida
                );
            }


            try {

                socketCliente.close();

            } catch (IOException e) {

                System.out.println(
                        "Error cerrando socket del cliente."
                );
            }
        }
    }


    // =====================================================
    // CONFIRMAR COMPRA
    // =====================================================

    private static void procesarCompra(
            String mensaje,
            PrintWriter cliente
    ) {

        /*
         * Formato recibido:
         *
         * 0 = CONFIRMAR_COMPRA
         * 1 = RUT
         * 2 = subtotal neto
         * 3 = IVA
         * 4 = total
         * 5 = código cupón
         * 6 = productos
         */

        String[] datos =
                mensaje.split(";", 7);


        if (datos.length != 7) {

            cliente.println(
                    "ERROR_COMPRA;Datos incorrectos"
            );

            return;
        }


        synchronized (inventarioServidor) {

            Map<String, Integer> reservas =
                    reservasClientes.get(
                            cliente
                    );


            // =====================================
            // VALIDAR QUE EXISTAN PRODUCTOS
            // =====================================

            if (reservas == null ||
                    reservas.isEmpty()) {

                cliente.println(
                        "ERROR_COMPRA;No hay productos reservados"
                );

                return;
            }


            // =====================================
            // VALIDAR Y CONSUMIR CUPÓN
            // =====================================

            String codigoCupon =
                    datos[5].trim();


            if (!codigoCupon.isEmpty()) {

                String errorCupon =
                        gestorCuponesServidor
                                .consumirCupon(
                                        codigoCupon
                                );


                if (!errorCupon.isEmpty()) {

                    cliente.println(
                            "ERROR_COMPRA;"
                                    + errorCupon
                    );

                    return;
                }
            }


            // =====================================
            // CONFIRMAR COMPRA
            // =====================================

            /*
             * El stock ya fue descontado cuando
             * el cliente agregó los productos
             * al carrito.
             *
             * Por eso NO devolvemos stock.
             *
             * Solamente eliminamos las reservas
             * porque ahora los productos fueron
             * comprados definitivamente.
             */

            reservas.clear();


            inventarioServidor.guardarEnCSV();


            // =====================================
            // AVISAR AL CLIENTE
            // =====================================

            cliente.println(
                    "COMPRA_OK"
            );


            // =====================================
            // INFORMAR VENTA AL ADMIN
            // =====================================

            /*
             * Por ahora seguimos enviando:
             *
             * RUT
             * subtotal neto
             * IVA
             * total
             * productos
             *
             * En el próximo avance agregaremos
             * cupón y descuento al registro ADMIN.
             */

            enviarATodos(
                    "VENTA_REALIZADA;"
                            + datos[1] + ";"
                            + datos[2] + ";"
                            + datos[3] + ";"
                            + datos[4] + ";"
                            + datos[6],
                    cliente
            );


            // El carrito comprado debe desaparecer
            // inmediatamente de la vista del ADMIN.
            notificarCarritosActivosATodos();


            System.out.println(
                    "Compra confirmada y enviada al ADMIN."
            );
        }
    }


    // =====================================================
    // ADMIN GUARDA O MODIFICA PRODUCTO
    // =====================================================

    private static void procesarProductoAdmin(
            String mensaje,
            PrintWriter admin
    ) {

        String[] datos =
                mensaje.split(";", 7);


        if (datos.length != 7) {
            return;
        }


        String id =
                datos[1];

        String nombre =
                datos[2];


        double precio;

        int stock;


        try {

            precio =
                    Double.parseDouble(
                            datos[3]
                    );

            stock =
                    Integer.parseInt(
                            datos[4]
                    );

        } catch (NumberFormatException e) {

            return;
        }


        String categoria =
                datos[5];

        String rutaImagen =
                datos[6];


        synchronized (inventarioServidor) {

            inventarioServidor
                    .sincronizarProductoDesdeServidor(
                            id,
                            nombre,
                            precio,
                            stock,
                            categoria,
                            rutaImagen
                    );


            inventarioServidor.guardarEnCSV();


            enviarATodos(
                    "PRODUCTO_ACTUALIZADO;"
                            + id + ";"
                            + nombre + ";"
                            + precio + ";"
                            + stock + ";"
                            + categoria + ";"
                            + rutaImagen,
                    admin
            );
        }


        System.out.println(
                "Producto actualizado por ADMIN: "
                        + id
        );
    }


    // =====================================================
    // ADMIN ELIMINA PRODUCTO
    // =====================================================

    private static void procesarEliminarProductoAdmin(
            String mensaje,
            PrintWriter admin
    ) {

        String[] datos =
                mensaje.split(";");


        if (datos.length != 2) {
            return;
        }


        String idProducto =
                datos[1];


        synchronized (inventarioServidor) {

            boolean eliminado =
                    inventarioServidor
                            .eliminarProductoPorId(
                                    idProducto
                            );


            if (eliminado) {

                enviarATodos(
                        "PRODUCTO_ELIMINADO;"
                                + idProducto,
                        admin
                );
            }
        }


        System.out.println(
                "ADMIN eliminó producto: "
                        + idProducto
        );
    }


    // =====================================================
    // ENVIAR STOCK ACTUAL
    // =====================================================

    private static void enviarStockActual(
            PrintWriter cliente
    ) {

        synchronized (inventarioServidor) {

            for (Producto producto :
                    inventarioServidor.getProductos()) {

                cliente.println(
                        "STOCK;"
                                + producto.getId()
                                + ";"
                                + producto.getStock()
                );
            }
        }

        cliente.println("TIEMPO_RESERVA;" + tiempoReservaSegundos);
        System.out.println("Stock actual enviado a un cliente.");
    }


    // =====================================================
    // ADMIN VACÍA CARRITO
    // =====================================================

    private static void procesarVaciarCarritoAdmin(
            String mensaje,
            PrintWriter admin
    ) {

        String[] datos =
                mensaje.split(";");


        if (datos.length != 2) {
            return;
        }


        String nombreObjetivo =
                datos[1];


        synchronized (inventarioServidor) {

            int contadorCliente = 1;


            for (Map.Entry<PrintWriter,
                    Map<String, Integer>> entrada :
                    reservasClientes.entrySet()) {


                Map<String, Integer> reservas =
                        entrada.getValue();


                if (reservas != null &&
                        !reservas.isEmpty()) {


                    String nombreActual =
                            "Cliente "
                                    + contadorCliente;


                    if (nombreActual.equals(
                            nombreObjetivo)) {


                        for (Map.Entry<String, Integer> prod :
                                reservas.entrySet()) {


                            Producto producto =
                                    inventarioServidor
                                            .buscarProductoPorId(
                                                    prod.getKey()
                                            );


                            if (producto != null) {

                                inventarioServidor
                                        .devolverStock(
                                                producto,
                                                prod.getValue()
                                        );


                                enviarATodos(
                                        "STOCK;"
                                                + producto.getId()
                                                + ";"
                                                + producto.getStock(),
                                        null
                                );
                            }
                        }


                        reservas.clear();


                        inventarioServidor
                                .guardarEnCSV();


                        entrada.getKey().println(
                                "CARRITO_VACIADO_POR_ADMIN"
                        );


                        break;
                    }
                }


                contadorCliente++;
            }
        }


        notificarCarritosActivosATodos();
    }


    // =====================================================
    // RESERVAR PRODUCTO
    // =====================================================

    private static void procesarReserva(
            String mensaje,
            PrintWriter cliente
    ) {

        String[] datos =
                mensaje.split(";");


        if (datos.length != 3) {

            cliente.println(
                    "ERROR;Formato de reserva incorrecto"
            );

            return;
        }


        String idProducto =
                datos[1];


        int cantidad;


        try {

            cantidad =
                    Integer.parseInt(
                            datos[2]
                    );

        } catch (NumberFormatException e) {

            cliente.println(
                    "ERROR;Cantidad incorrecta"
            );

            return;
        }


        if (cantidad <= 0) {

            cliente.println(
                    "ERROR;Cantidad incorrecta"
            );

            return;
        }


        synchronized (inventarioServidor) {

            Producto producto =
                    inventarioServidor
                            .buscarProductoPorId(
                                    idProducto
                            );


            if (producto == null) {

                cliente.println(
                        "ERROR;Producto no encontrado"
                );

                return;
            }


            boolean reservado =
                    inventarioServidor
                            .reservarUnidades(
                                    producto,
                                    cantidad
                            );


            if (!reservado) {

                cliente.println(
                        "ERROR_STOCK;"
                                + idProducto
                                + ";"
                                + producto.getStock()
                );

                return;
            }


            Map<String, Integer> reservasCliente =
                    reservasClientes.get(
                            cliente
                    );


            if (reservasCliente != null) {

                int cantidadActual =
                        reservasCliente
                                .getOrDefault(
                                        idProducto,
                                        0
                                );


                reservasCliente.put(
                        idProducto,
                        cantidadActual
                                + cantidad
                );
            }


            inventarioServidor
                    .guardarEnCSV();


            int nuevoStock =
                    producto.getStock();


            System.out.println(
                    producto.getNombre()
                            + " - Reservado: "
                            + cantidad
                            + " - Stock nuevo: "
                            + nuevoStock
            );


            cliente.println(
                    "RESERVA_OK;"
                            + idProducto
                            + ";"
                            + cantidad
                            + ";"
                            + nuevoStock
            );


            enviarATodos(
                    "STOCK;"
                            + idProducto
                            + ";"
                            + nuevoStock,
                    cliente
            );


            notificarCarritosActivosATodos();
        }
    }


    // =====================================================
    // DEVOLVER PRODUCTO
    // =====================================================

    private static void procesarDevolucion(
            String mensaje,
            PrintWriter cliente
    ) {

        String[] datos =
                mensaje.split(";");


        if (datos.length != 3) {

            cliente.println(
                    "ERROR;Formato de devolución incorrecto"
            );

            return;
        }


        String idProducto =
                datos[1];


        int cantidad;


        try {

            cantidad =
                    Integer.parseInt(
                            datos[2]
                    );

        } catch (NumberFormatException e) {

            cliente.println(
                    "ERROR;Cantidad incorrecta"
            );

            return;
        }


        if (cantidad <= 0) {

            cliente.println(
                    "ERROR_DEVOLUCION;"
                            + idProducto
            );

            return;
        }


        synchronized (inventarioServidor) {

            Producto producto =
                    inventarioServidor
                            .buscarProductoPorId(
                                    idProducto
                            );


            if (producto == null) {

                cliente.println(
                        "ERROR;Producto no encontrado"
                );

                return;
            }


            Map<String, Integer> reservasCliente =
                    reservasClientes.get(
                            cliente
                    );


            if (reservasCliente == null) {

                cliente.println(
                        "ERROR_DEVOLUCION;"
                                + idProducto
                );

                return;
            }


            int cantidadReservada =
                    reservasCliente
                            .getOrDefault(
                                    idProducto,
                                    0
                            );


            if (cantidad >
                    cantidadReservada) {

                cliente.println(
                        "ERROR_DEVOLUCION;"
                                + idProducto
                );

                return;
            }


            inventarioServidor
                    .devolverStock(
                            producto,
                            cantidad
                    );


            int nuevaCantidadReservada =
                    cantidadReservada
                            - cantidad;


            if (nuevaCantidadReservada == 0) {

                reservasCliente.remove(
                        idProducto
                );

            } else {

                reservasCliente.put(
                        idProducto,
                        nuevaCantidadReservada
                );
            }


            inventarioServidor
                    .guardarEnCSV();


            int nuevoStock =
                    producto.getStock();


            System.out.println(
                    producto.getNombre()
                            + " - Devuelto: "
                            + cantidad
                            + " - Stock nuevo: "
                            + nuevoStock
            );


            cliente.println(
                    "DEVOLUCION_OK;"
                            + idProducto
                            + ";"
                            + cantidad
                            + ";"
                            + nuevoStock
            );


            enviarATodos(
                    "STOCK;"
                            + idProducto
                            + ";"
                            + nuevoStock,
                    cliente
            );


            notificarCarritosActivosATodos();
        }
    }


    // =====================================================
    // ENVIAR CARRITOS ACTIVOS AL ADMIN
    // =====================================================

    private static void enviarCarritosActivos(
            PrintWriter admin
    ) {

        StringBuilder datosCarritos =
                new StringBuilder();

        int contadorCliente = 1;

        boolean hayCarritos = false;


        synchronized (inventarioServidor) {

            for (Map.Entry<PrintWriter,
                    Map<String, Integer>> entradaCliente :
                    reservasClientes.entrySet()) {


                Map<String, Integer> reservas =
                        entradaCliente.getValue();


                if (reservas != null &&
                        !reservas.isEmpty()) {


                    if (hayCarritos) {
                        datosCarritos.append("|");
                    }


                    datosCarritos
                            .append("Cliente ")
                            .append(contadorCliente)
                            .append(":");


                    boolean primerProducto = true;


                    for (Map.Entry<String, Integer> producto :
                            reservas.entrySet()) {


                        if (!primerProducto) {
                            datosCarritos.append("-");
                        }


                        datosCarritos
                                .append(producto.getKey())
                                .append(",")
                                .append(producto.getValue());


                        primerProducto = false;
                    }


                    hayCarritos = true;
                }


                contadorCliente++;
            }
        }


        if (!hayCarritos) {

            admin.println(
                    "CARRITOS_ACTIVOS;VACIO"
            );

        } else {

            admin.println(
                    "CARRITOS_ACTIVOS;"
                            + datosCarritos
            );
        }


        System.out.println(
                "Lista de carritos activos enviada al ADMIN."
        );
    }


    // =====================================================
    // AGREGAR CLIENTE
    // =====================================================

    private static synchronized void agregarCliente(
            PrintWriter cliente
    ) {

        clientes.add(
                cliente
        );


        reservasClientes.put(
                cliente,
                new HashMap<>()
        );


        System.out.println(
                "Clientes conectados: "
                        + clientes.size()
        );
    }


    // =====================================================
    // ELIMINAR CLIENTE
    // =====================================================

    private static synchronized void eliminarCliente(
            PrintWriter cliente
    ) {

        Map<String, Integer> reservas =
                reservasClientes.get(
                        cliente
                );


        if (reservas != null) {

            synchronized (inventarioServidor) {

                for (Map.Entry<String, Integer> entrada :
                        reservas.entrySet()) {


                    String idProducto =
                            entrada.getKey();


                    int cantidad =
                            entrada.getValue();


                    Producto producto =
                            inventarioServidor
                                    .buscarProductoPorId(
                                            idProducto
                                    );


                    if (producto != null) {

                        inventarioServidor
                                .devolverStock(
                                        producto,
                                        cantidad
                                );


                        System.out.println(
                                "Cliente desconectado: "
                                        + "se devolvieron "
                                        + cantidad
                                        + " unidades de "
                                        + producto.getNombre()
                        );


                        enviarATodos(
                                "STOCK;"
                                        + idProducto
                                        + ";"
                                        + producto.getStock(),
                                cliente
                        );
                    }
                }


                inventarioServidor
                        .guardarEnCSV();
            }
        }


        reservasClientes.remove(
                cliente
        );

        clientes.remove(
                cliente
        );


        notificarCarritosActivosATodos();


        System.out.println(
                "Clientes conectados: "
                        + clientes.size()
        );
    }


    // =====================================================
    // ENVIAR MENSAJE A TODOS
    // =====================================================

    private static synchronized void enviarATodos(
            String mensaje,
            PrintWriter emisor
    ) {

        for (PrintWriter cliente :
                clientes) {

            if (cliente != emisor) {

                cliente.println(
                        mensaje
                );
            }
        }
    }


    // =====================================================
    // ACTUALIZAR CARRITOS EN TODOS LOS CLIENTES/ADMIN
    // =====================================================

    private static void notificarCarritosActivosATodos() {

        StringBuilder datosCarritos =
                new StringBuilder();

        int contadorCliente = 1;

        boolean hayCarritos = false;


        synchronized (inventarioServidor) {

            for (Map.Entry<PrintWriter,
                    Map<String, Integer>> entradaCliente :
                    reservasClientes.entrySet()) {


                Map<String, Integer> reservas =
                        entradaCliente.getValue();


                if (reservas != null &&
                        !reservas.isEmpty()) {


                    if (hayCarritos) {
                        datosCarritos.append("|");
                    }


                    datosCarritos
                            .append("Cliente ")
                            .append(contadorCliente)
                            .append(":");


                    boolean primerProducto = true;


                    for (Map.Entry<String, Integer> producto :
                            reservas.entrySet()) {


                        if (!primerProducto) {
                            datosCarritos.append("-");
                        }


                        datosCarritos
                                .append(producto.getKey())
                                .append(",")
                                .append(producto.getValue());


                        primerProducto = false;
                    }


                    hayCarritos = true;
                }


                contadorCliente++;
            }
        }


        String mensaje;


        if (hayCarritos) {

            mensaje =
                    "CARRITOS_ACTIVOS;"
                            + datosCarritos;

        } else {

            mensaje =
                    "CARRITOS_ACTIVOS;VACIO";
        }


        enviarATodos(
                mensaje,
                null
        );
    }
}