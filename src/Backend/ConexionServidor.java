package Backend;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;
import java.util.List;

public class ConexionServidor {

    private static final String HOST_PREDETERMINADO = "127.0.0.1";
    private static final int PUERTO = 65432;

    private String host = HOST_PREDETERMINADO;
    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    private boolean conectado = false;

    private Consumer<String> receptorMensajes;


    public boolean conectar() {
        return conectar(host);
    }

    public boolean conectar(String host) {
        if (host == null || host.trim().isEmpty()) {
            System.out.println("La IP del servidor no puede estar vacía.");
            return false;
        }

        cerrarConexion();
        this.host = host.trim();

        try {

            socket = new Socket(this.host, PUERTO);

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

    public String getHost() {
        return host;
    }


    private void iniciarRecepcion() {
        Socket socketConexion = socket;
        BufferedReader entradaConexion = entrada;

        Thread hiloRecepcion = new Thread(() -> {

            try {

                String mensaje;

                while ((mensaje = entradaConexion.readLine()) != null) {

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

                if (conectado && socket == socketConexion) {

                    System.out.println(
                            "Se perdió la conexión con el servidor."
                    );
                }

            } finally {

                if (socket == socketConexion) {
                    conectado = false;
                }
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

    public void enviarProductoAdmin(
            Producto producto
    ) {

        if (producto == null) {
            return;
        }


        enviarMensaje(
                "ADMIN_GUARDAR_PRODUCTO;"
                        + producto.getId() + ";"
                        + limpiarCampo(producto.getNombre()) + ";"
                        + producto.getPrecio() + ";"
                        + producto.getStock() + ";"
                        + limpiarCampo(producto.getCategoria()) + ";"
                        + limpiarCampo(producto.getRutaImagen())
        );
    }


    public void eliminarProductoAdmin(
            String idProducto
    ) {

        enviarMensaje(
                "ADMIN_ELIMINAR_PRODUCTO;"
                        + idProducto
        );
    }


    private String limpiarCampo(
            String texto
    ) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace(";", " ")
                .replace("|", " ");
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

    public void confirmarCompra(
            String rut,
            double subtotal,
            double iva,
            double total,
            List<String> articulos
    ) {

        String rutEnviar =
                rut == null ? "" : limpiarCampo(rut);


        StringBuilder productos =
                new StringBuilder();


        for (int i = 0;
             i < articulos.size();
             i++) {

            if (i > 0) {
                productos.append("|");
            }

            productos.append(
                    limpiarCampo(
                            articulos.get(i)
                    )
            );
        }


        enviarMensaje(
                "CONFIRMAR_COMPRA;"
                        + rutEnviar + ";"
                        + subtotal + ";"
                        + iva + ";"
                        + total + ";"
                        + productos
        );
    }

    public void solicitarStockActual() {

        enviarMensaje(
                "PEDIR_STOCK"
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