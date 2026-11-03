package Frontend;

import Backend.Inventario;
import Backend.Producto;
import Backend.Sistema;
import Backend.Usuario;
import Backend.Carrito;
import Backend.ConexionServidor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class VistaCliente extends BaseFrame {

    // =========================================================
    // VARIABLES Y COLORES GLOBALES
    // =========================================================
    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color AZUL_SUAVE = new Color(224, 235, 248);
    private static final Color GRIS = new Color(100, 110, 125);

    // --- COMPONENTES VISUALES ---
    private JTextField txtBuscar;
    private JPanel gridProductos;
    private JLabel lblCantidad;
    private JPanel panelArticulosCarrito;

    private JLabel lblCarritoTitulo;
    private JLabel lblSubtotalCarrito;
    private JLabel lblIvaCarrito;
    private JLabel lblTotalCarrito;

    private JButton btnComprar;
    private JButton btnVaciarCarrito;

    // --- LÓGICA DE NEGOCIO Y RED ---
    private final Carrito carrito;
    private final ConexionServidor conexionServidor;

    // --- FILTROS Y DATOS TEMPORALES ---
    private String rutCompra = "";
    private String categoriaFiltro = "Todas";
    private double precioMinimo = -1;
    private double precioMaximo = -1;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public VistaCliente(Inventario inventario, Sistema sistema) {
        super("NexoMarket - Cliente", inventario, sistema);

        this.carrito = new Carrito(inventario);
        this.conexionServidor = new ConexionServidor();

        // --- CONEXIÓN: Configurar qué hacer al recibir un mensaje ---
        conexionServidor.setReceptorMensajes(mensaje -> procesarMensajeServidor(mensaje));
        conexionServidor.conectar();

        setSize(1200, 720);

        if (conexionServidor.estaConectado()) {
            conexionServidor.solicitarStockActual();
        }
    }

    // =========================================================
    // MÉTODO PRINCIPAL DE DIBUJO DE LA PANTALLA
    // =========================================================
    @Override
    public void mostrar() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        // --- PANELES PRINCIPALES ---
        add(crearCabecera(), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(15, 0));
        contenido.setBackground(FONDO);
        contenido.setBorder(new EmptyBorder(15, 18, 18, 18));

        contenido.add(crearCatalogo(), BorderLayout.CENTER);
        contenido.add(crearCarrito(), BorderLayout.EAST);

        add(contenido, BorderLayout.CENTER);

        // --- EVENTO: Actualización en vivo del buscador ---
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                actualizarProductos();
            }
        });

        actualizarProductos();
        setVisible(true);
    }

    // =========================================================
    // 1. ZONA DE CABECERA (Logo, Buscador, Botones)
    // =========================================================
    private JPanel crearCabecera() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Color.WHITE);

        JPanel cabecera = new JPanel(new BorderLayout(15, 0));
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(new EmptyBorder(8, 20, 8, 20));

        // --- SECCIÓN: LOGO ---
        JLabel lblLogo = new JLabel();
        ImageIcon logo = cargarLogo();
        if (logo != null) {
            lblLogo.setIcon(logo);
        } else {
            lblLogo.setText("NEXOMARKET");
            lblLogo.setFont(new Font("SansSerif", Font.BOLD, 23));
            lblLogo.setForeground(NAVY);
        }

        // --- SECCIÓN: BUSCADOR ---
        txtBuscar = new JTextField();
        txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 15));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 205)),
                new EmptyBorder(9, 12, 9, 12)
        ));

        JPanel panelBusqueda = new JPanel(new BorderLayout());
        panelBusqueda.setOpaque(false);
        panelBusqueda.setBorder(new EmptyBorder(15, 0, 15, 0));
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);

        // --- SECCIÓN: BOTONES DERECHA ---
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        acciones.setOpaque(false);

        // --- BOTÓN: Filtrar ---
        JButton btnFiltrar = crearBoton("Filtrar", AZUL);
        btnFiltrar.setPreferredSize(new Dimension(110, 45));
        btnFiltrar.addActionListener(e -> mostrarFiltros());

        // --- BOTÓN: Iniciar Sesión / Perfil ---
        JButton btnLogin = crearBoton("Iniciar sesión", NAVY);
        btnLogin.setPreferredSize(new Dimension(140, 45));

        btnLogin.addActionListener(e -> {
            Usuario usuarioActual = sistema.getUsuarioActual();
            if (usuarioActual == null) {
                // --- ABRIR: Login ---
                VistaLogin dialogoLogin = new VistaLogin(this, sistema);
                dialogoLogin.mostrar();

                Usuario user = dialogoLogin.getUsuarioLogeado();
                if (user != null) {
                    if (user.getTipoUsuario().equals("Administrador")) {
                        // --- REDIRECCIÓN: Vista Admin ---
                        conexionServidor.cerrarConexion();
                        this.dispose();
                        VistaPrincipalTienda adminVista = new VistaPrincipalTienda(inventario, sistema);
                        adminVista.mostrar();
                    } else {
                        btnLogin.setText("👤 Hola, " + user.getNombre());
                    }
                }
            } else {
                // --- ABRIR: Mi Perfil ---
                DialogoPerfil dialogoPerfil = new DialogoPerfil(this, sistema, inventario, usuarioActual);
                dialogoPerfil.mostrar();
                if (dialogoPerfil.isCerroSesion()) {
                    btnLogin.setText("Iniciar sesión");
                } else {
                    btnLogin.setText("👤 Hola, " + usuarioActual.getNombre());
                }
            }
        });

        // --- BOTÓN: Configurar IP ---
        JButton btnConfiguracion = crearBoton("*", new Color(90, 105, 125));
        btnConfiguracion.setFont(new Font("SansSerif", Font.PLAIN, 23));
        btnConfiguracion.setToolTipText("Configurar IP del host");
        btnConfiguracion.setPreferredSize(new Dimension(45, 45));
        btnConfiguracion.addActionListener(e -> mostrarConfiguracionConexion());

        // --- ENSAMBLAJE CABECERA ---
        acciones.add(btnFiltrar);
        acciones.add(btnLogin);
        acciones.add(btnConfiguracion);

        cabecera.add(lblLogo, BorderLayout.WEST);
        cabecera.add(panelBusqueda, BorderLayout.CENTER);
        cabecera.add(acciones, BorderLayout.EAST);

        JPanel franja = new JPanel();
        franja.setBackground(NAVY);
        franja.setPreferredSize(new Dimension(0, 5));

        contenedor.add(cabecera, BorderLayout.CENTER);
        contenedor.add(franja, BorderLayout.SOUTH);

        return contenedor;
    }

    // =========================================================
    // 2. ZONA CENTRAL (Catálogo de Productos)
    // =========================================================
    private JPanel crearCatalogo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // --- ENCABEZADO CATÁLOGO ---
        PanelRedondeado encabezado = new PanelRedondeado(12);
        encabezado.setBackground(NAVY);
        encabezado.setLayout(new BorderLayout());
        encabezado.setBorder(new EmptyBorder(9, 12, 9, 12));

        JLabel titulo = new JLabel("Productos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);

        lblCantidad = new JLabel();
        lblCantidad.setForeground(new Color(205, 215, 230));

        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(lblCantidad, BorderLayout.EAST);

        // --- GRILLA DE PRODUCTOS ---
        gridProductos = new JPanel(new GridLayout(0, 3, 12, 12));
        gridProductos.setOpaque(false);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setBorder(new EmptyBorder(12, 0, 0, 0));
        contenedor.add(gridProductos, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(contenedor);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // 3. ZONA DERECHA (Panel del Carrito)
    // =========================================================
    private JPanel crearCarrito() {
        PanelRedondeado carritoPanel = new PanelRedondeado(18);
        carritoPanel.setBackground(AZUL_SUAVE);
        carritoPanel.setPreferredSize(new Dimension(250, 0));
        carritoPanel.setLayout(new BorderLayout());
        carritoPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // --- TÍTULO CARRITO ---
        PanelRedondeado cabecera = new PanelRedondeado(10);
        cabecera.setBackground(NAVY);
        cabecera.setLayout(new BorderLayout());
        cabecera.setBorder(new EmptyBorder(9, 10, 9, 10));

        lblCarritoTitulo = new JLabel("Mi Carrito (0)");
        lblCarritoTitulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblCarritoTitulo.setForeground(Color.WHITE);
        cabecera.add(lblCarritoTitulo);

        // --- LISTA DE ARTÍCULOS ---
        panelArticulosCarrito = new JPanel();
        panelArticulosCarrito.setOpaque(false);
        panelArticulosCarrito.setLayout(new BoxLayout(panelArticulosCarrito, BoxLayout.Y_AXIS));

        JScrollPane scrollCarrito = new JScrollPane(panelArticulosCarrito);
        scrollCarrito.setBorder(null);
        scrollCarrito.setOpaque(false);
        scrollCarrito.getViewport().setOpaque(false);
        scrollCarrito.getVerticalScrollBar().setUnitIncrement(12);

        // --- TOTALES Y BOTONES DE COMPRA ---
        JPanel inferior = new JPanel();
        inferior.setOpaque(false);
        inferior.setLayout(new BoxLayout(inferior, BoxLayout.Y_AXIS));

        lblSubtotalCarrito = new JLabel("Subtotal: $0");
        lblSubtotalCarrito.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSubtotalCarrito.setForeground(NAVY);

        lblIvaCarrito = new JLabel("IVA (19%): $0");
        lblIvaCarrito.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblIvaCarrito.setForeground(NAVY);

        lblTotalCarrito = new JLabel("Total: $0");
        lblTotalCarrito.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblTotalCarrito.setForeground(NAVY);

        // --- BOTÓN: Vaciar Carrito ---
        btnVaciarCarrito = crearBoton("Vaciar carrito", new Color(215, 60, 70));
        btnVaciarCarrito.setEnabled(false);
        btnVaciarCarrito.addActionListener(e -> vaciarCarrito());

        // --- BOTÓN: Proceder a Compra ---
        btnComprar = crearBoton("Proceder a la compra", AZUL);
        btnComprar.setEnabled(false);
        btnComprar.addActionListener(e -> procederCompra());

        // --- ENSAMBLAJE CARRITO ---
        inferior.add(lblSubtotalCarrito);
        inferior.add(Box.createRigidArea(new Dimension(0, 4)));
        inferior.add(lblIvaCarrito);
        inferior.add(Box.createRigidArea(new Dimension(0, 5)));
        inferior.add(lblTotalCarrito);
        inferior.add(Box.createRigidArea(new Dimension(0, 12)));
        inferior.add(btnVaciarCarrito);
        inferior.add(Box.createRigidArea(new Dimension(0, 7)));
        inferior.add(btnComprar);

        carritoPanel.add(cabecera, BorderLayout.NORTH);
        carritoPanel.add(scrollCarrito, BorderLayout.CENTER);
        carritoPanel.add(inferior, BorderLayout.SOUTH);

        actualizarCarrito();

        return carritoPanel;
    }

    // =========================================================
    // LÓGICA DE DIBUJO Y FILTRADO DE PRODUCTOS
    // =========================================================
    private void actualizarProductos() {
        // --- VALIDACIÓN ANTICRASH (NullPointerException) ---
        if (gridProductos == null) {
            return;
        }

        gridProductos.removeAll();

        String busqueda = txtBuscar.getText().trim().toLowerCase();
        int encontrados = 0;

        for (Producto p : inventario.getProductos()) {
            boolean coincideBusqueda = p.getNombre().toLowerCase().contains(busqueda) || p.getCategoria().toLowerCase().contains(busqueda);
            boolean coincideCategoria = categoriaFiltro.equals("Todas") || p.getCategoria().equalsIgnoreCase(categoriaFiltro);
            boolean coincidePrecio = (precioMinimo < 0 || p.getPrecio() >= precioMinimo) && (precioMaximo < 0 || p.getPrecio() <= precioMaximo);

            if (coincideBusqueda && coincideCategoria && coincidePrecio) {
                gridProductos.add(crearTarjeta(p));
                encontrados++;
            }
        }

        lblCantidad.setText(encontrados + " productos encontrados");
        gridProductos.revalidate();
        gridProductos.repaint();
    }

    // --- MÉTODO: Crea una tarjeta individual por producto ---
    private JPanel crearTarjeta(Producto producto) {
        PanelRedondeado tarjeta = new PanelRedondeado(16);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(10, 10, 10, 10));
        tarjeta.setPreferredSize(new Dimension(215, 255));

        JLabel imagen = new JLabel();
        imagen.setOpaque(true);
        imagen.setBackground(new Color(231, 238, 247));
        imagen.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon icono = cargarImagen(producto.getRutaImagen());
        if (icono != null) {
            imagen.setIcon(icono);
        } else {
            imagen.setText(producto.getNombre().substring(0, 1).toUpperCase());
            imagen.setFont(new Font("SansSerif", Font.BOLD, 40));
            imagen.setForeground(NAVY);
        }

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(new EmptyBorder(7, 0, 0, 0));

        JLabel nombre = new JLabel(acortar(producto.getNombre(), 34));
        nombre.setFont(new Font("SansSerif", Font.BOLD, 13));
        nombre.setForeground(NAVY);
        nombre.setToolTipText(producto.getNombre());

        JLabel categoria = new JLabel(producto.getCategoria());
        categoria.setForeground(GRIS);

        JLabel precio = new JLabel(formatearPesos(producto.getPrecio()));
        precio.setFont(new Font("SansSerif", Font.BOLD, 16));
        precio.setForeground(NAVY);

        JLabel stock = crearStock(producto);

        // --- BOTÓN: Agregar al Carrito ---
        JButton agregar = crearBoton("Agregar al carrito", AZUL);
        if (producto.getStock() == 0) {
            agregar.setText("Sin stock");
            agregar.setEnabled(false);
        }
        agregar.addActionListener(e -> agregarAlCarrito(producto));

        info.add(nombre);
        info.add(categoria);
        info.add(Box.createRigidArea(new Dimension(0, 3)));
        info.add(precio);
        info.add(stock);
        info.add(Box.createRigidArea(new Dimension(0, 6)));
        info.add(agregar);

        tarjeta.add(imagen, BorderLayout.CENTER);
        tarjeta.add(info, BorderLayout.SOUTH);

        return tarjeta;
    }

    // =========================================================
    // COMUNICACIÓN CON EL SERVIDOR (RED)
    // =========================================================
    private void procesarMensajeServidor(String mensaje) {

        // --- ACCIÓN: Compra procesada ---
        if (mensaje.equals("COMPRA_OK")) {
            System.out.println("El servidor confirmó la compra.");
            return;
        }

        // --- ACCIÓN: Error en compra ---
        if (mensaje.startsWith("ERROR_COMPRA;")) {
            SwingUtilities.invokeLater(() -> mostrarError("El servidor no pudo confirmar la compra."));
            return;
        }

        // --- ACCIÓN: Devolución Exitosa ---
        if (mensaje.startsWith("DEVOLUCION_OK;")) {
            String[] datos = mensaje.split(";");
            if (datos.length != 4) return;

            String idProducto = datos[1];
            int cantidadDevuelta;
            int nuevoStock;

            try {
                cantidadDevuelta = Integer.parseInt(datos[2]);
                nuevoStock = Integer.parseInt(datos[3]);
            } catch (NumberFormatException e) {
                return;
            }

            // HILO PRINCIPAL UI
            SwingUtilities.invokeLater(() -> {
                Producto producto = buscarProductoLocal(idProducto);
                if (producto == null) return;

                producto.setStock(nuevoStock);
                carrito.devolverProductoConfirmado(producto, cantidadDevuelta);
                actualizarCarrito();
                actualizarProductos();
            });
            return;
        }

        // --- ACCIÓN: Reserva Exitosa ---
        if (mensaje.startsWith("RESERVA_OK;")) {
            String[] datos = mensaje.split(";");
            if (datos.length != 4) return;

            String idProducto = datos[1];
            int cantidadReservada;
            int nuevoStock;

            try {
                cantidadReservada = Integer.parseInt(datos[2]);
                nuevoStock = Integer.parseInt(datos[3]);
            } catch (NumberFormatException e) {
                return;
            }

            // HILO PRINCIPAL UI
            SwingUtilities.invokeLater(() -> {
                Producto producto = buscarProductoLocal(idProducto);
                if (producto == null) return;

                producto.setStock(nuevoStock);
                carrito.agregarProductoConfirmado(producto, cantidadReservada);
                actualizarCarrito();
                actualizarProductos();
            });
            return;
        }

        // --- ACCIÓN: Producto Modificado por Admin ---
        if (mensaje.startsWith("PRODUCTO_ACTUALIZADO;")) {
            String[] datos = mensaje.split(";", 7);
            if (datos.length != 7) return;

            try {
                String id = datos[1];
                String nombre = datos[2];
                double precio = Double.parseDouble(datos[3]);
                int stock = Integer.parseInt(datos[4]);
                String categoria = datos[5];
                String rutaImagen = datos[6];

                SwingUtilities.invokeLater(() -> {
                    inventario.sincronizarProductoDesdeServidor(id, nombre, precio, stock, categoria, rutaImagen);
                    actualizarProductos();
                });
            } catch (NumberFormatException e) {
                return;
            }
            return;
        }

        // --- ACCIÓN: Producto Eliminado por Admin ---
        if (mensaje.startsWith("PRODUCTO_ELIMINADO;")) {
            String[] datos = mensaje.split(";");
            if (datos.length != 2) return;

            SwingUtilities.invokeLater(() -> {
                inventario.eliminarProductoDesdeServidor(datos[1]);
                actualizarProductos();
            });
            return;
        }

        // --- ACCIÓN: Stock Modificado en Vivo ---
        if (mensaje.startsWith("STOCK;")) {
            String[] datos = mensaje.split(";");
            if (datos.length != 3) return;

            String idProducto = datos[1];
            int nuevoStock;

            try {
                nuevoStock = Integer.parseInt(datos[2]);
            } catch (NumberFormatException e) {
                return;
            }

            SwingUtilities.invokeLater(() -> {
                Producto producto = buscarProductoLocal(idProducto);
                if (producto == null) return;

                producto.setStock(nuevoStock);
                actualizarProductos();
            });
            return;
        }

        // --- ACCIÓN: Sin Stock ---
        if (mensaje.startsWith("ERROR_STOCK;")) {
            String[] datos = mensaje.split(";");

            SwingUtilities.invokeLater(() -> {
                if (datos.length == 3) {
                    Producto producto = buscarProductoLocal(datos[1]);
                    try {
                        int stockReal = Integer.parseInt(datos[2]);
                        if (producto != null) producto.setStock(stockReal);
                    } catch (NumberFormatException ignored) {}
                }
                actualizarProductos();
                mostrarError("El producto ya no tiene stock disponible.");
            });
        }
    }

    // =========================================================
    // GESTIÓN DEL CARRITO (LÓGICA)
    // =========================================================
    private void actualizarCarrito() {
        if (panelArticulosCarrito == null) return;

        panelArticulosCarrito.removeAll();
        Map<Producto, Integer> productosCarrito = carrito.getProductos();

        for (Map.Entry<Producto, Integer> entrada : productosCarrito.entrySet()) {
            Producto producto = entrada.getKey();
            int cantidad = entrada.getValue();

            JPanel filaProducto = new JPanel(new BorderLayout(5, 0));
            filaProducto.setOpaque(false);
            filaProducto.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

            JPanel datos = new JPanel();
            datos.setLayout(new BoxLayout(datos, BoxLayout.Y_AXIS));
            datos.setOpaque(false);

            JLabel nombre = new JLabel(producto.getNombre());
            nombre.setFont(new Font("SansSerif", Font.BOLD, 12));
            nombre.setForeground(NAVY);

            JLabel precio = new JLabel(formatearPesos(producto.getPrecio() * cantidad));
            precio.setFont(new Font("SansSerif", Font.PLAIN, 12));
            precio.setForeground(GRIS);

            datos.add(nombre);
            datos.add(precio);

            JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));
            controles.setOpaque(false);

            // --- BOTONES: Controles del Carrito (+, -, x) ---
            JButton btnMenos = crearBotonCarrito("-", NAVY);
            JLabel lblCantidadProducto = new JLabel(String.valueOf(cantidad));
            lblCantidadProducto.setFont(new Font("SansSerif", Font.BOLD, 13));
            JButton btnMas = crearBotonCarrito("+", AZUL);
            JButton btnEliminar = crearBotonCarrito("X", new Color(215, 60, 70));

            btnMenos.addActionListener(e -> cambiarCantidad(producto, cantidad - 1));
            btnMas.addActionListener(e -> cambiarCantidad(producto, cantidad + 1));
            btnEliminar.addActionListener(e -> eliminarDelCarrito(producto));

            if (producto.getStock() == 0) {
                btnMas.setEnabled(false);
            }

            controles.add(btnMenos);
            controles.add(lblCantidadProducto);
            controles.add(btnMas);
            controles.add(btnEliminar);

            filaProducto.add(datos, BorderLayout.CENTER);
            filaProducto.add(controles, BorderLayout.EAST);
            panelArticulosCarrito.add(filaProducto);
            panelArticulosCarrito.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        // --- VISTA: Carrito Vacío ---
        if (carrito.estaVacio()) {
            JLabel vacio = new JLabel("<html><center><b>Tu carrito está vacío</b><br><br>Agrega productos para comenzar</center></html>");
            vacio.setHorizontalAlignment(SwingConstants.CENTER);
            vacio.setForeground(GRIS);
            vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelArticulosCarrito.add(Box.createVerticalGlue());
            panelArticulosCarrito.add(vacio);
            panelArticulosCarrito.add(Box.createVerticalGlue());
        }

        // --- ACTUALIZAR TOTALES ---
        double subtotal = carrito.calcularSubtotal();
        double iva = carrito.calcularIva();
        double total = carrito.calcularTotal();

        lblCarritoTitulo.setText("Mi Carrito (" + carrito.getCantidadTotal() + ")");
        lblSubtotalCarrito.setText("Subtotal: " + formatearPesos(subtotal));
        lblIvaCarrito.setText("IVA (19%): " + formatearPesos(iva));
        lblTotalCarrito.setText("Total: " + formatearPesos(total));

        boolean tieneProductos = !carrito.estaVacio();
        btnComprar.setEnabled(tieneProductos);
        btnVaciarCarrito.setEnabled(tieneProductos);

        panelArticulosCarrito.revalidate();
        panelArticulosCarrito.repaint();
    }

    private void agregarAlCarrito(Producto producto) {
        if (!conexionServidor.estaConectado()) {
            mostrarError("No hay conexión con el servidor.");
            return;
        }
        conexionServidor.solicitarReserva(producto.getId(), 1);
    }

    private void cambiarCantidad(Producto producto, int nuevaCantidad) {
        Integer cantidadActual = carrito.getProductos().get(producto);
        if (cantidadActual == null) return;

        if (nuevaCantidad > cantidadActual) {
            int cantidadExtra = nuevaCantidad - cantidadActual;
            conexionServidor.solicitarReserva(producto.getId(), cantidadExtra);
            return;
        }

        if (nuevaCantidad < cantidadActual) {
            int cantidadADevolver = cantidadActual - nuevaCantidad;
            conexionServidor.solicitarDevolucion(producto.getId(), cantidadADevolver);
        }
    }

    private void eliminarDelCarrito(Producto producto) {
        Integer cantidad = carrito.getProductos().get(producto);
        if (cantidad == null) return;
        conexionServidor.solicitarDevolucion(producto.getId(), cantidad);
    }

    private void vaciarCarrito() {
        if (carrito.estaVacio()) return;

        int opcion = JOptionPane.showConfirmDialog(this, "¿Desea vaciar todo el carrito?", "Vaciar carrito", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) return;

        Map<Producto, Integer> productosCarrito = carrito.getProductos();
        for (Map.Entry<Producto, Integer> entrada : productosCarrito.entrySet()) {
            conexionServidor.solicitarDevolucion(entrada.getKey().getId(), entrada.getValue());
        }
    }

    // =========================================================
    // LÓGICA FINAL DE COMPRA Y BOLETA
    // =========================================================
    private void procederCompra() {
        // --- 1. VALIDACIÓN DEL RUT DEL CLIENTE ---
        if (sistema.getUsuarioActual() != null) {
            rutCompra = sistema.getUsuarioActual().getRut();
        } else {
            int opcionRut = JOptionPane.showConfirmDialog(this, "¿Desea ingresar su RUT para asociarlo a la compra?", "RUT opcional", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (opcionRut == JOptionPane.YES_OPTION) {
                String rutIngresado = JOptionPane.showInputDialog(this, "Ingrese su RUT:", "Ingresar RUT", JOptionPane.PLAIN_MESSAGE);
                if (rutIngresado == null) return;
                rutIngresado = rutIngresado.trim();

                if (rutIngresado.isEmpty()) {
                    mostrarError("Debe ingresar un RUT o elegir comprar sin RUT.");
                    return;
                }
                rutCompra = rutIngresado;
            } else {
                rutCompra = "";
            }
        }

        // --- 2. CONFIRMACIÓN MONETARIA ---
        int confirmar = JOptionPane.showConfirmDialog(
                this,
                "Subtotal: " + formatearPesos(carrito.calcularSubtotal())
                        + "\nIVA (19%): " + formatearPesos(carrito.calcularIva())
                        + "\nTotal: " + formatearPesos(carrito.calcularTotal())
                        + "\n\n¿Desea confirmar la compra?",
                "Confirmar compra",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirmar != JOptionPane.YES_OPTION) return;

        // --- 3. CREACIÓN DE LISTA DE ARTÍCULOS ---
        java.util.List<String> articulosComprados = new ArrayList<>();
        for (Map.Entry<Producto, Integer> entrada : carrito.getProductos().entrySet()) {
            articulosComprados.add(entrada.getValue() + " x " + entrada.getKey().getNombre());
        }

        double subtotalCompra = carrito.calcularSubtotal();
        double ivaCompra = carrito.calcularIva();
        double totalCompra = carrito.calcularTotal();
        Backend.Compra nuevaCompra = new Backend.Compra(rutCompra, articulosComprados, totalCompra);

        if (!conexionServidor.estaConectado()) {
            mostrarError("No se puede confirmar la compra porque no hay conexión con el servidor.");
            return;
        }

        // --- 4. ENVÍO AL SERVIDOR Y GUARDADO PERSISTENTE ---
        conexionServidor.confirmarCompra(rutCompra, subtotalCompra, ivaCompra, totalCompra, articulosComprados);
        inventario.registrarCompra(rutCompra, articulosComprados, totalCompra);
        inventario.confirmarCompra();

        // --- 5. MOSTRAR BOLETA VISUAL ---
        DialogoComprobante dialogoComprobante = new DialogoComprobante(this, nuevaCompra, carrito.getProductos(), subtotalCompra, ivaCompra, totalCompra);
        dialogoComprobante.mostrar();

        carrito.limpiarDespuesDeCompra();
        actualizarCarrito();
        actualizarProductos();
        rutCompra = "";
    }

    // =========================================================
    // DIÁLOGOS MENORES (Configuración y Filtros)
    // =========================================================
    private void mostrarConfiguracionConexion() {
        JTextField txtHost = new JTextField(conexionServidor.getHost(), 18);
        txtHost.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtHost.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 205)),
                new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel ayuda = new JLabel("Usa 127.0.0.1 si el host está en este mismo PC.");
        ayuda.setFont(new Font("SansSerif", Font.PLAIN, 12));
        ayuda.setForeground(GRIS);

        JDialog dialogo = new JDialog(this, "Conectar al host", true);
        dialogo.setSize(430, 250);
        dialogo.setLocationRelativeTo(this);
        dialogo.setLayout(new BorderLayout());
        dialogo.getContentPane().setBackground(FONDO);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setBackground(NAVY);
        cabecera.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel titulo = new JLabel("Conectar al host");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        cabecera.add(titulo, BorderLayout.WEST);
        dialogo.add(cabecera, BorderLayout.NORTH);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(new EmptyBorder(18, 25, 10, 25));
        JLabel etiqueta = new JLabel("IP del host:");
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 14));
        etiqueta.setForeground(NAVY);
        contenido.add(etiqueta);
        contenido.add(Box.createRigidArea(new Dimension(0, 6)));
        contenido.add(txtHost);
        contenido.add(Box.createRigidArea(new Dimension(0, 8)));
        contenido.add(ayuda);
        dialogo.add(contenido, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        botones.setOpaque(false);
        JButton cancelar = crearBoton("Cancelar", new Color(149, 165, 166));
        JButton conectar = crearBoton("Conectar", AZUL);
        cancelar.setPreferredSize(new Dimension(105, 38));
        conectar.setPreferredSize(new Dimension(105, 38));
        final boolean[] confirmado = {false};
        cancelar.addActionListener(e -> dialogo.dispose());
        conectar.addActionListener(e -> {
            if (txtHost.getText().trim().isEmpty()) {
                mostrarError("Ingresa una IP o nombre de host.");
                return;
            }
            confirmado[0] = true;
            dialogo.dispose();
        });
        botones.add(cancelar);
        botones.add(conectar);
        dialogo.add(botones, BorderLayout.SOUTH);
        dialogo.getRootPane().setDefaultButton(conectar);
        dialogo.setVisible(true);

        if (!confirmado[0]) {
            return;
        }

        String host = txtHost.getText().trim();

        if (conexionServidor.conectar(host)) {
            conexionServidor.solicitarStockActual();
            JOptionPane.showMessageDialog(this, "Conectado al host " + host + ".", "Conexión exitosa", JOptionPane.INFORMATION_MESSAGE);
        } else {
            mostrarError("No se pudo conectar al host " + host + " en el puerto 65432.");
        }
    }

    private void mostrarFiltros() {
        String[] existentes = inventario.getCategoriasDisponibles();
        String[] categorias = new String[existentes.length + 1];
        categorias[0] = "Todas";
        System.arraycopy(existentes, 0, categorias, 1, existentes.length);

        JComboBox<String> cbCategoria = new JComboBox<>(categorias);
        cbCategoria.setSelectedItem(categoriaFiltro);

        JTextField txtMin = new JTextField();
        JTextField txtMax = new JTextField();
        if (precioMinimo >= 0) txtMin.setText(String.valueOf(precioMinimo));
        if (precioMaximo >= 0) txtMax.setText(String.valueOf(precioMaximo));

        Object[] contenido = {"Categoría:", cbCategoria, "Precio mínimo:", txtMin, "Precio máximo:", txtMax};

        if (JOptionPane.showConfirmDialog(this, contenido, "Filtrar productos", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) return;

        try {
            double min = leerPrecio(txtMin);
            double max = leerPrecio(txtMax);

            if ((min < 0 && min != -1) || (max < 0 && max != -1)) {
                mostrarError("Los precios no pueden ser negativos.");
                return;
            }
            if (min >= 0 && max >= 0 && min > max) {
                mostrarError("El precio mínimo no puede ser mayor al máximo.");
                return;
            }

            categoriaFiltro = cbCategoria.getSelectedItem().toString();
            precioMinimo = min;
            precioMaximo = max;
            actualizarProductos();

        } catch (NumberFormatException e) {
            mostrarError("Ingrese precios válidos.");
        }
    }

    // =========================================================
    // UTILIDADES GRÁFICAS Y TEXTO
    // =========================================================
    private Producto buscarProductoLocal(String idProducto) {
        for (Producto producto : inventario.getProductos()) {
            if (producto.getId().equalsIgnoreCase(idProducto)) return producto;
        }
        return null;
    }

    private double leerPrecio(JTextField campo) {
        return campo.getText().trim().isEmpty() ? -1 : Double.parseDouble(campo.getText().trim());
    }

    private JLabel crearStock(Producto producto) {
        JLabel stock = new JLabel();
        if (producto.getStock() == 0) {
            stock.setText("● Agotado");
            stock.setForeground(new Color(215, 60, 70));
        } else if (producto.getStock() <= 5) {
            stock.setText("● Pocas unidades (" + producto.getStock() + ")");
            stock.setForeground(new Color(225, 140, 25));
        } else {
            stock.setText("● Disponible (" + producto.getStock() + ")");
            stock.setForeground(new Color(35, 145, 85));
        }
        return stock;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setForeground(Color.WHITE);
        boton.setBackground(color);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private JButton crearBotonCarrito(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setForeground(Color.WHITE);
        boton.setBackground(color);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setPreferredSize(new Dimension(35, 28));
        boton.setMargin(new Insets(0, 0, 0, 0));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private ImageIcon cargarLogo() {
        String[] rutas = {"imagenes/logo sin fondo.png", "imagenes/Logo NEXOMARKET.jpeg", "imagenes/Logo NEXOMARKET.jpg", "imagenes/Logo NEXOMARKET.png"};
        for (String ruta : rutas) {
            File archivo = new File(ruta);
            if (archivo.isFile()) {
                return new ImageIcon(new ImageIcon(ruta).getImage().getScaledInstance(180, 75, Image.SCALE_SMOOTH));
            }
        }
        return null;
    }

    private ImageIcon cargarImagen(String ruta) {
        File archivo = ImagenesUtil.resolverRutaImagen(ruta);
        if (archivo == null) return null;
        return new ImageIcon(new ImageIcon(archivo.getPath()).getImage().getScaledInstance(160, 95, Image.SCALE_SMOOTH));
    }

    private String acortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo) + "...";
    }

    private String formatearPesos(double valor) {
        return "$" + String.format("%,.0f", valor).replace(",", ".");
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}