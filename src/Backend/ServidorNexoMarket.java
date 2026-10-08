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

    // Inventario central del servidor
    private static final Inventario inventarioServidor =
            new Inventario();

    // Clientes conectados
    private static final List<PrintWriter> clientes =
            new ArrayList<>();

    // Guarda lo que tiene reservado cada cliente
    // Cliente -> (ID producto -> cantidad)
    private static final Map<PrintWriter, Map<String, Integer>>
            reservasClientes = new HashMap<>();


    public static void main(String[] args) {

        try (ServerSocket servidor =
                     new ServerSocket(PUERTO)) {

            System.out.println("================================");
            System.out.println("SERVIDOR NEXOMARKET INICIADO");
            System.out.println("Puerto: " + PUERTO);
            System.out.println("Esperando clientes...");
            System.out.println("================================");


            while (true) {

                Socket socketCliente =
                        servidor.accept();

                System.out.println(
                        "Nuevo cliente conectado: "
                                + socketCliente.getInetAddress()
                );


                Thread hiloCliente =
                        new Thread(() ->
                                atenderCliente(socketCliente)
                        );

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

    private static void atenderCliente(
            Socket socketCliente
    ) {

        PrintWriter salida = null;

        try {

            BufferedReader entrada =
                    new BufferedReader(
                            new InputStreamReader(
                                    socketCliente.getInputStream()
                            )
                    );

            salida =
                    new PrintWriter(
                            socketCliente.getOutputStream(),
                            true
                    );


            agregarCliente(salida);


            salida.println(
                    "{\"type\":\"message\","
                            + "\"content\":\"Conectado al servidor NexoMarket\"}"
            );


            String mensaje;

            while ((mensaje = entrada.readLine()) != null) {

                if (mensaje.trim().isEmpty()) {
                    continue;
                }


                System.out.println(
                        "Mensaje recibido: "
                                + mensaje
                );


                if (mensaje.startsWith("RESERVAR;")) {

                    procesarReserva(
                            mensaje,
                            salida
                    );

                } else if (mensaje.startsWith("DEVOLVER;")) {

                    procesarDevolucion(
                            mensaje,
                            salida
                    );

                } else {

                    enviarATodos(
                            mensaje,
                            salida
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Un cliente se desconectó."
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
                        "Error cerrando cliente."
                );
            }
        }
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
                    inventarioServidor.buscarProductoPorId(
                            idProducto
                    );


            if (producto == null) {

                cliente.println(
                        "ERROR;Producto no encontrado"
                );

                return;
            }


            boolean reservado =
                    inventarioServidor.reservarUnidades(
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


            // Registrar cuánto tiene reservado este cliente
            Map<String, Integer> reservasCliente =
                    reservasClientes.get(
                            cliente
                    );


            if (reservasCliente != null) {

                int cantidadActual =
                        reservasCliente.getOrDefault(
                                idProducto,
                                0
                        );


                reservasCliente.put(
                        idProducto,
                        cantidadActual + cantidad
                );
            }


            inventarioServidor.guardarEnCSV();


            int nuevoStock =
                    producto.getStock();


            System.out.println(
                    producto.getNombre()
                            + " - Reservado: "
                            + cantidad
                            + " - Stock nuevo: "
                            + nuevoStock
            );


            // Respuesta al cliente que reservó
            cliente.println(
                    "RESERVA_OK;"
                            + idProducto
                            + ";"
                            + cantidad
                            + ";"
                            + nuevoStock
            );


            // Avisar a los demás clientes
            enviarATodos(
                    "STOCK;"
                            + idProducto
                            + ";"
                            + nuevoStock,
                    cliente
            );
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
                    inventarioServidor.buscarProductoPorId(
                            idProducto
                    );


            if (producto == null) {

                cliente.println(
                        "ERROR;Producto no encontrado"
                );

                return;
            }


            // Ver cuánto tiene realmente reservado este cliente
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
                    reservasCliente.getOrDefault(
                            idProducto,
                            0
                    );


            // No puede devolver más de lo que reservó
            if (cantidad > cantidadReservada) {

                System.out.println(
                        "Devolución rechazada. "
                                + "Cliente quería devolver "
                                + cantidad
                                + " pero solo tenía "
                                + cantidadReservada
                );


                cliente.println(
                        "ERROR_DEVOLUCION;"
                                + idProducto
                );

                return;
            }


            // Devolver stock real
            inventarioServidor.devolverStock(
                    producto,
                    cantidad
            );


            // Actualizar las reservas de este cliente
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


            inventarioServidor.guardarEnCSV();


            int nuevoStock =
                    producto.getStock();


            System.out.println(
                    producto.getNombre()
                            + " - Devuelto: "
                            + cantidad
                            + " - Stock nuevo: "
                            + nuevoStock
            );


            // Confirmar al cliente que devolvió
            cliente.println(
                    "DEVOLUCION_OK;"
                            + idProducto
                            + ";"
                            + cantidad
                            + ";"
                            + nuevoStock
            );


            // Avisar a los demás clientes
            enviarATodos(
                    "STOCK;"
                            + idProducto
                            + ";"
                            + nuevoStock,
                    cliente
            );
        }
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
                reservasClientes.get(cliente);


        if (reservas != null) {

            synchronized (inventarioServidor) {

                for (Map.Entry<String, Integer> entrada
                        : reservas.entrySet()) {

                    String idProducto =
                            entrada.getKey();

                    int cantidad =
                            entrada.getValue();


                    Producto producto =
                            inventarioServidor.buscarProductoPorId(
                                    idProducto
                            );


                    if (producto != null) {

                        inventarioServidor.devolverStock(
                                producto,
                                cantidad
                        );


                        System.out.println(
                                "Cliente desconectado: se devolvieron "
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


                inventarioServidor.guardarEnCSV();
            }
        }


        reservasClientes.remove(cliente);

        clientes.remove(cliente);


        System.out.println(
                "Clientes conectados: "
                        + clientes.size()
        );
    }


    // =====================================================
    // ENVIAR MENSAJE A LOS DEMÁS CLIENTES
    // =====================================================

    private static synchronized void enviarATodos(
            String mensaje,
            PrintWriter emisor
    ) {

        for (PrintWriter cliente : clientes) {

            if (cliente != emisor) {

                cliente.println(
                        mensaje
                );
            }
        }
    }
}