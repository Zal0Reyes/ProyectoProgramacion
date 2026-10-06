package Backend;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ServidorNexoMarket {

    private static final int PUERTO = 65432;

    // Aquí guardamos las conexiones de los clientes
    private static final List<PrintWriter> clientes = new ArrayList<>();


    public static void main(String[] args) {

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {

            System.out.println("================================");
            System.out.println("SERVIDOR INICIADO");
            System.out.println("Puerto: " + PUERTO);
            System.out.println("Esperando clientes...");
            System.out.println("================================");


            while (true) {

                // Espera a que alguien se conecte
                Socket socketCliente = servidor.accept();

                System.out.println(
                        "Nuevo cliente conectado: "
                                + socketCliente.getInetAddress()
                );


                // Cada cliente tendrá su propio hilo
                Thread hiloCliente = new Thread(() -> {

                    atenderCliente(socketCliente);

                });

                hiloCliente.start();
            }


        } catch (IOException e) {

            System.out.println(
                    "Error en el servidor: "
                            + e.getMessage()
            );
        }
    }


    private static void atenderCliente(Socket socketCliente) {

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


            // Mensaje inicial
            salida.println(
                    "{\"type\":\"message\","
                            + "\"content\":\"Conectado al servidor \"}"
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


                // Por ahora simplemente enviamos el mensaje a los demás clientes
                enviarATodos(
                        mensaje,
                        salida
                );
            }


        } catch (IOException e) {

            System.out.println(
                    "Un cliente se desconectó."
            );


        } finally {

            if (salida != null) {

                eliminarCliente(salida);
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


    private static synchronized void agregarCliente(
            PrintWriter cliente
    ) {

        clientes.add(cliente);

        System.out.println(
                "Clientes conectados: "
                        + clientes.size()
        );
    }


    private static synchronized void eliminarCliente(
            PrintWriter cliente
    ) {

        clientes.remove(cliente);

        System.out.println(
                "Clientes conectados: "
                        + clientes.size()
        );
    }


    private static synchronized void enviarATodos(
            String mensaje,
            PrintWriter emisor
    ) {

        for (PrintWriter cliente : clientes) {

            // No le devolvemos su propio mensaje
            if (cliente != emisor) {

                cliente.println(mensaje);
            }
        }
    }
}