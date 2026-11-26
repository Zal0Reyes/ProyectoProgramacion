package Backend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ServidorNexoMarket {

    private static final int PUERTO = 65432;


    // =====================================================
    // ESTRUCTURAS DE DATOS GLOBALES
    // =====================================================

    // Inventario central compartido
    private static final Inventario inventarioServidor = new Inventario();

    // Gestor central de cupones
    private static final GestorCupones gestorCuponesServidor =
            new GestorCupones();

    // Gestor central de ofertas
    private static final GestorOfertas gestorOfertasServidor =
            new GestorOfertas();

    // Clientes conectados
    private static final List<PrintWriter> clientes = new ArrayList<>();

    // Cliente -> (ID producto -> cantidad reservada)
    private static final Map<PrintWriter, Map<String, Integer>> reservasClientes = new HashMap<>();

    private static final GestorListaDeseos gestorDeseos = new GestorListaDeseos();
    private static final Map<PrintWriter, String> usuariosConectados = new HashMap<>();

    private static int tiempoReservaSegundos = 180;
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
                        "ADMIN_CREAR_OFERTA;")) {

                    procesarCrearOferta(
                            mensaje,
                            salida
                    );

                } else if (mensaje.equals(
                        "PEDIR_OFERTAS")) {

                    enviarOfertasVigentes(
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
                else if (mensaje.startsWith("IDENTIFICAR_USUARIO;")) {
                    String[] datos = mensaje.split(";", 2);
                    if (datos.length == 2 && !datos[1].trim().isEmpty()) {
                        String userId = datos[1].trim().toLowerCase();
                        usuariosConectados.put(salida, userId);

                        // Enviar notificaciones que ocurrieron mientras estaba desconectado
                        List<String> pendientes = gestorDeseos.consumirNotificacionesPendientes(userId);
                        for (String aviso : pendientes) {
                            salida.println(aviso);
                        }
                    }
                }
                // --- COMANDO: Cliente cierra sesión (deja de recibir alertas de esa cuenta) ---
                else if (mensaje.equals("CERRAR_SESION_USUARIO")) {
                    usuariosConectados.remove(salida);
                }
                // --- COMANDO: Cliente guarda/quita favorito ---
                else if (mensaje.startsWith("DESEO_TOGGLE;")) {
                    String[] datos = mensaje.split(";", 3);
                    if (datos.length == 3) {
                        String user = datos[1].trim();
                        String idProd = datos[2].trim();
                        if (gestorDeseos.estaEnListaDeseos(user, idProd)) {
                            gestorDeseos.eliminarDeseo(user, idProd);
                        } else {
                            gestorDeseos.agregarDeseo(user, idProd);
                        }
                    }
                }
                // --- COMANDO: Cliente se suscribe a reposición ---
                else if (mensaje.startsWith("SUSCRIBIR_STOCK;")) {
                    String[] datos = mensaje.split(";", 3);
                    if (datos.length == 3) {
                        String user = datos[1].trim();
                        String idProd = datos[2].trim();
                        gestorDeseos.suscribirReposicion(user, idProd);
                        int totalEsperando = gestorDeseos.contarClientesEsperando(idProd);
                        enviarATodos("ACTUALIZAR_ESPERANDO;" + idProd + ";" + totalEsperando, null);
                    }
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
                usuariosConectados.remove(salida);
                eliminarCliente(salida);
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
         * 6 = descuento
         * 7 = productos
         */

        String[] datos =
                mensaje.split(";", 8);


        if (datos.length != 8) {

            cliente.println(
                    "ERROR_COMPRA;Datos incorrectos"
            );

            return;
        }


        synchronized (inventarioServidor) {

            Map<String, Integer> reservas =
                    reservasClientes.get(cliente);


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

            reservas.clear();

            inventarioServidor.guardarEnCSV();


            cliente.println(
                    "COMPRA_OK"
            );


            // =====================================
            // INFORMAR VENTA AL ADMIN
            // =====================================

            enviarATodos(
                    "VENTA_REALIZADA;"
                            + datos[1] + ";"  // RUT
                            + datos[2] + ";"  // subtotal neto
                            + datos[3] + ";"  // IVA
                            + datos[4] + ";"  // total
                            + datos[5] + ";"  // cupón
                            + datos[6] + ";"  // descuento
                            + datos[7],       // productos
                    cliente
            );


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
            Producto existente = inventarioServidor.buscarProductoPorId(id);
            int stockAnterior = (existente != null) ? existente.getStock() : 0;
            double precioAnterior = (existente != null) ? existente.getPrecio() : precio;

            inventarioServidor.sincronizarProductoDesdeServidor(id, nombre, precio, stock, categoria, rutaImagen);
            inventarioServidor.guardarEnCSV();

            enviarATodos("PRODUCTO_ACTUALIZADO;" + id + ";" + nombre + ";" + precio + ";" + stock + ";" + categoria + ";" + rutaImagen, admin);

            // 1. Aviso de reposición (si estaba en 0 y ahora tiene stock > 0)
            if (stockAnterior == 0 && stock > 0) {
                Set<String> suscritos = gestorDeseos.consumirSuscriptoresReposicion(id);
                if (!suscritos.isEmpty()) {
                    notificarUsuariosEspecificos(suscritos, "AVISO_REPOSICION;" + id + ";" + nombre);
                    enviarATodos("ACTUALIZAR_ESPERANDO;" + id + ";0", null);
                }
            }

            // 2. Aviso de baja de precio (si bajó respecto al precio anterior)
            if (existente != null && precio < precioAnterior) {
                Set<String> interesados = gestorDeseos.obtenerUsuariosConProductoEnDeseos(id);
                if (!interesados.isEmpty()) {
                    notificarUsuariosEspecificos(interesados, "AVISO_BAJA_PRECIO;" + id + ";" + nombre + ";" + precioAnterior + ";" + precio);
                }
            }
        }
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

    // =====================================================
// ADMIN CREA OFERTA
// =====================================================

    private static void procesarCrearOferta(
            String mensaje,
            PrintWriter admin
    ) {

        String[] datos =
                mensaje.split(";", 6);

        /*
         * 0 = ADMIN_CREAR_OFERTA
         * 1 = tipo
         * 2 = objetivo
         * 3 = descuento
         * 4 = inicio
         * 5 = fin
         */

        if (datos.length != 6) {

            admin.println(
                    "ERROR_OFERTA;Datos incorrectos"
            );

            return;
        }


        try {

            String tipo =
                    datos[1];

            String objetivo =
                    datos[2];

            double descuento =
                    Double.parseDouble(
                            datos[3]
                    );

            long inicio =
                    Long.parseLong(
                            datos[4]
                    );

            long fin =
                    Long.parseLong(
                            datos[5]
                    );


            boolean guardada =
                    gestorOfertasServidor
                            .agregarOferta(
                                    tipo,
                                    objetivo,
                                    descuento,
                                    inicio,
                                    fin
                            );


            if (!guardada) {

                admin.println(
                        "ERROR_OFERTA;Los datos de la oferta son inválidos"
                );

                return;
            }


            String mensajeOferta =
                    "OFERTA_ACTUALIZADA;"
                            + tipo + ";"
                            + objetivo + ";"
                            + descuento + ";"
                            + inicio + ";"
                            + fin;


            // Avisar a todos los clientes conectados
            enviarATodos(
                    mensajeOferta,
                    null
            );


            admin.println(
                    "OFERTA_GUARDADA"
            );


            System.out.println(
                    "Oferta creada para "
                            + tipo
                            + ": "
                            + objetivo
            );


        } catch (NumberFormatException e) {

            admin.println(
                    "ERROR_OFERTA;Datos numéricos incorrectos"
            );
        }
    }


// =====================================================
// ENVIAR OFERTAS VIGENTES
// =====================================================

    private static void enviarOfertasVigentes(
            PrintWriter cliente
    ) {

        for (Oferta oferta :
                gestorOfertasServidor
                        .obtenerOfertasVigentes()) {

            cliente.println(
                    "OFERTA_ACTUALIZADA;"
                            + oferta.getTipo() + ";"
                            + oferta.getObjetivo() + ";"
                            + oferta.getPorcentajeDescuento() + ";"
                            + oferta.getFechaInicio() + ";"
                            + oferta.getFechaFin()
            );
        }


        System.out.println(
                "Ofertas vigentes enviadas a un cliente."
        );
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
    private static synchronized void notificarUsuariosEspecificos(Set<String> usuariosObjetivo, String mensaje) {
        Set<String> notificadosEnVivo = new HashSet<>();

        for (Map.Entry<PrintWriter, String> entry : usuariosConectados.entrySet()) {
            if (usuariosObjetivo.contains(entry.getValue())) {
                entry.getKey().println(mensaje);
                notificadosEnVivo.add(entry.getValue());
            }
        }

        // Quienes no estaban conectados reciben la notificación al iniciar sesión
        for (String usuarioId : usuariosObjetivo) {
            if (!notificadosEnVivo.contains(usuarioId)) {
                gestorDeseos.encolarNotificacion(usuarioId, mensaje);
            }
        }
    }
}