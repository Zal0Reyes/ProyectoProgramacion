package Backend;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class ConexionServidor {

    private static final String HOST = "127.0.0.1";
    private static final int PUERTO = 65432;

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    private boolean conectado = false;

    private Consumer<String> receptorMensajes;


    public boolean conectar() {

        try {

            socket = new Socket(HOST, PUERTO);

            entrada = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()
                    )
            );

            salida = new PrintWriter(
                    socket.getOutputStream(),
                    true
            );

            conectado = true;

            System.out.println(
                    "Conectado al servidor NexoMarket."
            );

            iniciarRecepcion();

            return true;

        } catch (IOException e) {

            System.out.println(
                    "No se pudo conectar al servidor: "
                            + e.getMessage()
            );

            conectado = false;

            return false;
        }
    }


    private void iniciarRecepcion() {

        Thread hiloRecepcion = new Thread(() -> {

            try {

                String mensaje;

                while ((mensaje = entrada.readLine()) != null) {

                    System.out.println(
                            "[Servidor] "
                                    + mensaje
                    );

                    if (receptorMensajes != null) {

                        receptorMensajes.accept(
                                mensaje
                        );
                    }
                }

            } catch (IOException e) {

                if (conectado) {

                    System.out.println(
                            "Se perdió la conexión con el servidor."
                    );
                }

            } finally {

                conectado = false;
            }
        });

        hiloRecepcion.setDaemon(true);
        hiloRecepcion.start();
    }


    public void setReceptorMensajes(
            Consumer<String> receptorMensajes
    ) {

        this.receptorMensajes =
                receptorMensajes;
    }


    public void enviarMensaje(
            String mensaje
    ) {

        if (conectado && salida != null) {

            salida.println(
                    mensaje
            );
        }
    }


    public void solicitarReserva(
            String idProducto,
            int cantidad
    ) {

        enviarMensaje(
                "RESERVAR;"
                        + idProducto
                        + ";"
                        + cantidad
        );
    }


    public void solicitarDevolucion(
            String idProducto,
            int cantidad
    ) {

        enviarMensaje(
                "DEVOLVER;"
                        + idProducto
                        + ";"
                        + cantidad
        );
    }


    public boolean estaConectado() {

        return conectado;
    }


    public void cerrarConexion() {

        conectado = false;

        try {

            if (socket != null) {

                socket.close();
            }

        } catch (IOException e) {

            System.out.println(
                    "Error al cerrar la conexión."
            );
        }
    }
}