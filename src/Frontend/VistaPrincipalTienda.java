package Frontend;

import Backend.Inventario;
import Backend.Producto;
import Backend.Sistema;
import Backend.ConexionServidor;
import Backend.GestorCupones;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.Set;

public class VistaPrincipalTienda extends BaseFrame {

    // =========================================================
    // VARIABLES GLOBALES DE LA CLASE
    // =========================================================
    private final ConexionServidor conexionServidor;

    // --- GESTIÓN DE VISTAS (DASHBOARD) ---
    private CardLayout gestorVistas;
    private JPanel panelContenedorVistas;

    // --- BOTONES DE PESTAÑAS (NAVEGACIÓN SUPERIOR) ---
    private JButton btnNavProductos;
    private JButton btnNavCarritos;
    private JButton btnNavPedidos;

    // --- PANELES INTERNOS ---
    private JPanel gridProductos;
    private final GestorCupones gestorCupones;
    private JPanel panelCarritosGrid;
    private JPanel panelPedidosGrid;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public VistaPrincipalTienda(Inventario inventario, Sistema sistema) {
        super("NexoMarket - Panel de Administración", inventario, sistema);
        conexionServidor = new ConexionServidor();

        gestorCupones =
                new GestorCupones();
        // --- CONEXIÓN: Configurar recepción de mensajes ---
        conexionServidor.setReceptorMensajes(this::procesarMensajeServidor);
        conexionServidor.conectar();
    }

    // =========================================================
    // MÉTODO PRINCIPAL DE DIBUJO DE LA PANTALLA
    // =========================================================
    @Override
    public void mostrar() {
        setSize(1200, 720);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(238, 243, 249));

        // ==========================================
        // 1. CABECERA SUPERIOR (Pestañas Modernas)
        // ==========================================
        add(crearCabeceraSuperior(), BorderLayout.NORTH);

        // ==========================================
        // 2. BARRA LATERAL (Acciones secundarias)
        // ==========================================
        add(crearBarraLateral(), BorderLayout.WEST);

        // ==========================================
        // 3. CONTENEDOR CENTRAL (CardLayout)
        // ==========================================
        gestorVistas = new CardLayout();
        panelContenedorVistas = new JPanel(gestorVistas);
        panelContenedorVistas.setOpaque(false);

        // --- AGREGAMOS LAS VISTAS AL MAZO ---
        panelContenedorVistas.add(crearVistaProductos(), "VISTA_PRODUCTOS");
        panelContenedorVistas.add(crearVistaCarritos(), "VISTA_CARRITOS");
        panelContenedorVistas.add(crearVistaPedidos(), "VISTA_PEDIDOS");

        add(panelContenedorVistas, BorderLayout.CENTER);

        // --- Carga inicial ---
        actualizarVistaProductos("");
        if (conexionServidor.estaConectado()) {
            conexionServidor.solicitarStockActual();
        }

        setVisible(true);
    }

    // =========================================================
    // CONSTRUCCIÓN DE LA INTERFAZ: CABECERA Y LATERAL
    // =========================================================

    private JPanel crearCabeceraSuperior() {
        JPanel contenedorCabecera = new JPanel(new BorderLayout());
        contenedorCabecera.setBackground(Color.WHITE);

        JPanel panelCabecera = new JPanel(new BorderLayout(20, 0));
        panelCabecera.setBackground(Color.WHITE);
        panelCabecera.setBorder(new EmptyBorder(0, 20, 0, 20)); // Sin padding arriba y abajo para que las pestañas toquen el borde

        // --- LOGO (Mantiene la modificación de tu amigo: logo sin fondo) ---
        JLabel lblLogo = new JLabel();
        ImageIcon logo = cargarLogo();
        if (logo != null) {
            lblLogo.setIcon(logo);
        } else {
            lblLogo.setText("NEXOMARKET ADMIN");
            lblLogo.setFont(new Font("SansSerif", Font.BOLD, 23));
            lblLogo.setForeground(new Color(19, 40, 72));
        }
        lblLogo.setBorder(new EmptyBorder(10, 0, 10, 0));
        panelCabecera.add(lblLogo, BorderLayout.WEST);

        // --- NAVEGACIÓN PRINCIPAL (Diseño de Pestañas / Tabs) ---
        JPanel panelNavegacion = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        panelNavegacion.setOpaque(false);

        btnNavProductos = crearBotonPestana("📦 Catálogo de Productos", true);
        btnNavCarritos = crearBotonPestana("🛒 Carritos en Vivo", false);
        btnNavPedidos = crearBotonPestana("🚚 Gestión de Pedidos", false);

        btnNavProductos.addActionListener(e -> {
            gestorVistas.show(panelContenedorVistas, "VISTA_PRODUCTOS");
            activarPestana(btnNavProductos, btnNavCarritos,btnNavPedidos);
            actualizarVistaProductos("");
        });

        btnNavCarritos.addActionListener(e -> {
            gestorVistas.show(panelContenedorVistas, "VISTA_CARRITOS");
            activarPestana(btnNavCarritos, btnNavProductos,btnNavPedidos);
            if (conexionServidor.estaConectado()) conexionServidor.solicitarCarritosActivos();
        });

        btnNavPedidos.addActionListener(e -> {
            gestorVistas.show(panelContenedorVistas, "VISTA_PEDIDOS");
            activarPestana(btnNavPedidos, btnNavProductos, btnNavCarritos);
            dibujarPedidosEnPanel();
        });

        panelNavegacion.add(btnNavProductos);
        panelNavegacion.add(btnNavCarritos);
        panelNavegacion.add(btnNavPedidos);
        panelCabecera.add(panelNavegacion, BorderLayout.CENTER);

        // --- PERFIL ADMIN (Opcional a la derecha) ---
        JLabel lblAdminInfo = new JLabel("👤 Modo Administrador");
        lblAdminInfo.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblAdminInfo.setForeground(new Color(130, 140, 150));
        lblAdminInfo.setBorder(new EmptyBorder(10, 0, 10, 0));
        panelCabecera.add(lblAdminInfo, BorderLayout.EAST);

        // --- FRANJA INFERIOR ---
        JPanel franja = new JPanel();
        franja.setBackground(new Color(225, 230, 240));
        franja.setPreferredSize(new Dimension(0, 2));

        contenedorCabecera.add(panelCabecera, BorderLayout.CENTER);
        contenedorCabecera.add(franja, BorderLayout.SOUTH);

        return contenedorCabecera;
    }

    private JPanel crearBarraLateral() {
        JPanel barraLateral = new JPanel();
        barraLateral.setBackground(new Color(19, 40, 72));
        barraLateral.setPreferredSize(new Dimension(80, 0));
        barraLateral.setLayout(new BoxLayout(barraLateral, BoxLayout.Y_AXIS));
        barraLateral.setBorder(new EmptyBorder(30, 0, 30, 0));

        // --- BOTÓN AGREGAR PRODUCTO ---
        JButton btnAgregar = crearBotonMenu("➕", e -> {

            DialogoAgregarProducto dialogo =
                    new DialogoAgregarProducto(
                            VistaPrincipalTienda.this,
                            inventario
                    );

            dialogo.mostrar();

            if (dialogo.isGuardadoExitoso()) {

                Producto productoGuardado =
                        dialogo.getProductoGuardado();

                if (productoGuardado != null
                        && conexionServidor.estaConectado()) {

                    conexionServidor.enviarProductoAdmin(
                            productoGuardado
                    );
                }

                actualizarVistaProductos("");

                registrarAccionAdmin(
                        "Agregó producto: "
                                + productoGuardado.getNombre()
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Producto agregado exitosamente.",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        });

        btnAgregar.setToolTipText(
                "Agregar Nuevo Producto"
        );

        barraLateral.add(btnAgregar);


// ESPACIO
        barraLateral.add(
                Box.createRigidArea(
                        new Dimension(0, 30)
                )
        );


// ==========================================
// BOTÓN CREAR CUPÓN
// ==========================================

        JButton btnCupon =
                crearBotonMenu(
                        "🎟",
                        e -> {

                            DialogoCrearCupon dialogo =
                                    new DialogoCrearCupon(
                                            VistaPrincipalTienda.this,
                                            gestorCupones
                                    );

                            dialogo.mostrar();
                        }
                );

        btnCupon.setToolTipText(
                "Crear cupón"
        );

        barraLateral.add(btnCupon);


// ESPACIO
        barraLateral.add(
                Box.createRigidArea(
                        new Dimension(0, 30)
                )
        );


// --- BOTÓN AGREGAR ADMINISTRADOR ---
        JButton btnAgregarAdmin =
                crearBotonMenu(
                        "👨‍",
                        e -> {

                            DialogoRegistroAdmin dialogoAdmin =
                                    new DialogoRegistroAdmin(
                                            VistaPrincipalTienda.this,
                                            sistema
                                    );

                            dialogoAdmin.mostrar();
                        }
                );

        btnAgregarAdmin.setToolTipText(
                "Registrar Nuevo Administrador"
        );

        barraLateral.add(btnAgregarAdmin);
        barraLateral.add(
                Box.createRigidArea(
                        new Dimension(0, 30)
                )
        );

        JButton btnEstadisticas =
                crearBotonMenu(
                        "📊",
                        e -> mostrarDialogoEstadisticas()
                );

        btnEstadisticas.setToolTipText("Estadísticas");
        barraLateral.add(btnEstadisticas);

        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        JButton btnHistorial = crearBotonMenu("\uD83D\uDCCB", e -> new DialogoHistorialCompras(this, inventario).mostrar());
        btnHistorial.setToolTipText("Historial de compras");
        barraLateral.add(btnHistorial);
        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        JButton btnVaciar = crearBotonMenu("🗑", e -> {
            DialogoVaciarInventario dialogoVaciar = new DialogoVaciarInventario(VistaPrincipalTienda.this, inventario);
            dialogoVaciar.mostrar();
            if (dialogoVaciar.huboCambios()) {
                actualizarVistaProductos("");
                registrarAccionAdmin("Realizó un vaciado masivo del inventario");
            }
        });
        btnVaciar.setToolTipText("Gestión Masiva / Vaciar Inventario");
        barraLateral.add(btnVaciar);

        barraLateral.add(Box.createVerticalGlue());

        JButton btnCerrarSesion = crearBotonMenu("🚪", e -> {
            int confirmacion = JOptionPane.showConfirmDialog(this, "¿Cerrar sesión?", "Salir", JOptionPane.YES_NO_OPTION);
            if (confirmacion == JOptionPane.YES_OPTION) {
                sistema.cerrarSesion();
                conexionServidor.cerrarConexion();
                dispose();
                new VistaCliente(inventario, sistema).mostrar();
            }
        });
        btnCerrarSesion.setToolTipText("Cerrar Sesión");
        barraLateral.add(btnCerrarSesion);

        return barraLateral;
    }

    // =========================================================
    // CONSTRUCCIÓN DE LAS "CARTAS" (VISTAS DEL DASHBOARD)
    // =========================================================

    // --- CARTA 1: VISTA DE PRODUCTOS ---
    private JPanel crearVistaProductos() {
        JPanel panelCentral = new JPanel(new BorderLayout(15, 0));
        panelCentral.setOpaque(false);
        panelCentral.setBorder(new EmptyBorder(15, 18, 18, 18));

        // HERRAMIENTAS (Buscador y Filtro)
        PanelRedondeado herramientas = new PanelRedondeado(12);
        herramientas.setBackground(new Color(19, 40, 72));
        herramientas.setLayout(new BorderLayout(10, 0));
        herramientas.setBorder(new EmptyBorder(9, 12, 9, 12));

        JLabel titulo = new JLabel("Inventario Actual");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);
        herramientas.add(titulo, BorderLayout.WEST);

        JTextField txtBuscar = new JTextField();
        txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 15));
        txtBuscar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                actualizarVistaProductos(txtBuscar.getText());
            }
        });
        herramientas.add(txtBuscar, BorderLayout.CENTER);

        JButton btnFiltro = new JButton("Filtrar");
        btnFiltro.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnFiltro.setForeground(new Color(19, 40, 72));
        btnFiltro.setBackground(Color.WHITE);
        btnFiltro.setFocusPainted(false);
        btnFiltro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFiltro.addActionListener(e -> mostrarDialogoFiltros());
        herramientas.add(btnFiltro, BorderLayout.EAST);

        panelCentral.add(herramientas, BorderLayout.NORTH);

        // GRILLA DE PRODUCTOS
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
        configurarBarraScroll(scroll);

        panelCentral.add(scroll, BorderLayout.CENTER);
        return panelCentral;
    }

    // --- CARTA 2: VISTA DE CARRITOS ACTIVOS ---
    private JPanel crearVistaCarritos() {
        JPanel panelCentral = new JPanel(new BorderLayout(15, 0));
        panelCentral.setOpaque(false);
        panelCentral.setBorder(new EmptyBorder(15, 18, 18, 18));

        // ENCABEZADO 
        PanelRedondeado encabezado = new PanelRedondeado(12);
        encabezado.setBackground(new Color(19, 40, 72));
        encabezado.setLayout(new BorderLayout());
        encabezado.setBorder(new EmptyBorder(9, 12, 9, 12));

        JLabel titulo = new JLabel("Monitoreo de Reservas en Vivo");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);
        encabezado.add(titulo, BorderLayout.WEST);


        panelCentral.add(encabezado, BorderLayout.NORTH);

        // LISTA DE CARRITOS
        panelCarritosGrid = new JPanel();
        panelCarritosGrid.setLayout(new BoxLayout(panelCarritosGrid, BoxLayout.Y_AXIS));
        panelCarritosGrid.setOpaque(false);
        panelCarritosGrid.setBorder(new EmptyBorder(15, 0, 15, 0));

        JScrollPane scroll = new JScrollPane(panelCarritosGrid);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        configurarBarraScroll(scroll);

        panelCentral.add(scroll, BorderLayout.CENTER);
        return panelCentral;
    }

    // =========================================================
    // COMUNICACIÓN CON EL SERVIDOR (RED)
    // =========================================================
    private void procesarMensajeServidor(String mensaje) {

        // --- ACCIÓN: Recepción de carritos activos en tiempo real ---
        if (mensaje.startsWith("CARRITOS_ACTIVOS;")) {
            String datos = mensaje.substring(17);
            SwingUtilities.invokeLater(() -> dibujarCarritosEnPanel(datos));
            return;
        }

        // --- ACCIÓN: Producto Modificado ---
        if (mensaje.startsWith("PRODUCTO_ACTUALIZADO;")) {
            String[] datos = mensaje.split(";", 7);
            if (datos.length != 7) return;
            try {
                inventario.sincronizarProductoDesdeServidor(datos[1], datos[2], Double.parseDouble(datos[3]), Integer.parseInt(datos[4]), datos[5], datos[6]);
                SwingUtilities.invokeLater(() -> actualizarVistaProductos(""));
            } catch (NumberFormatException e) { }
            return;
        }

        // --- ACCIÓN: Producto Eliminado ---
        if (mensaje.startsWith("PRODUCTO_ELIMINADO;")) {
            String[] datos = mensaje.split(";");
            if (datos.length != 2) return;
            SwingUtilities.invokeLater(() -> {
                inventario.eliminarProductoDesdeServidor(datos[1]);
                actualizarVistaProductos("");
            });
            return;
        }

        // --- ACCIÓN: Stock Actualizado ---
        if (mensaje.startsWith("STOCK;")) {
            String[] datos = mensaje.split(";");
            if (datos.length != 3) return;
            try {
                int nuevoStock = Integer.parseInt(datos[2]);
                SwingUtilities.invokeLater(() -> {
                    Producto producto = inventario.buscarProductoPorId(datos[1]);
                    if (producto != null) producto.setStock(nuevoStock);
                    actualizarVistaProductos("");
                });
            } catch (NumberFormatException e) { }
            return;
        }

        // --- ACCIÓN: Nueva Venta Confirmada ---
        if (mensaje.startsWith("VENTA_REALIZADA;")) {
            String[] datos = mensaje.split(";", 6);
            if (datos.length != 6) return;

            String rut = datos[1].trim().isEmpty() ? "Sin RUT" : datos[1].trim();
            try {
                double subtotal = Double.parseDouble(datos[2]);
                double iva = Double.parseDouble(datos[3]);
                double total = Double.parseDouble(datos[4]);

                java.util.List<String> articulos = new java.util.ArrayList<>();
                if (!datos[5].trim().isEmpty()) {
                    for (String articulo : datos[5].split("\\|")) articulos.add(articulo);
                }

                SwingUtilities.invokeLater(() -> {
                    Backend.Compra compra = new Backend.Compra(rut, articulos, total);
                    inventario.agregarCompraEnMemoria(compra);
                });
            } catch (NumberFormatException e) { }
        }
    }

    // =========================================================
    // LÓGICA DE DIBUJO DE VISTAS (PRODUCTOS Y CARRITOS)
    // =========================================================

    private void actualizarVistaProductos(String textoBusqueda) {
        if (gridProductos == null) return;
        gridProductos.removeAll();

        String busqueda = textoBusqueda.toLowerCase();
        for (Producto p : inventario.getProductos()) {
            if (textoBusqueda.isEmpty() || p.getNombre().toLowerCase().contains(busqueda) || p.getId().toLowerCase().contains(busqueda)) {
                gridProductos.add(crearTarjetaProducto(p));
            }
        }
        gridProductos.revalidate();
        gridProductos.repaint();
    }

    private void actualizarVistaConFiltros(java.util.List<String> categoriasFiltro, double minimo, double maximo) {
        if (gridProductos == null) return;
        gridProductos.removeAll();

        for (Producto p : inventario.getProductos()) {
            boolean pasaCategoria = categoriasFiltro.isEmpty();
            for (String cat : categoriasFiltro) {
                if (p.getCategoria().equalsIgnoreCase(cat)) {
                    pasaCategoria = true;
                    break;
                }
            }

            boolean pasaPrecio = true;
            if (minimo >= 0 && maximo >= 0) pasaPrecio = (p.getPrecio() >= minimo && p.getPrecio() <= maximo);
            else if (minimo >= 0) pasaPrecio = (p.getPrecio() >= minimo);
            else if (maximo >= 0) pasaPrecio = (p.getPrecio() <= maximo);

            if (pasaCategoria && pasaPrecio) gridProductos.add(crearTarjetaProducto(p));
        }
        gridProductos.revalidate();
        gridProductos.repaint();
    }

    private void dibujarCarritosEnPanel(String datosServidor) {
        if (panelCarritosGrid == null) return;
        panelCarritosGrid.removeAll();

        if (datosServidor.equals("VACIO")) {
            JLabel vacio = new JLabel("<html><center><b>No hay carritos activos en este momento</b><br><br>Los clientes no tienen productos reservados.</center></html>");
            vacio.setFont(new Font("SansSerif", Font.PLAIN, 15));
            vacio.setForeground(new Color(100, 110, 125));
            vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelCarritosGrid.add(Box.createRigidArea(new Dimension(0, 50)));
            panelCarritosGrid.add(vacio);
        } else {
            String[] clientes = datosServidor.split("\\|");
            for (String datosCliente : clientes) {
                String[] partes = datosCliente.split(":");
                if (partes.length == 2) {
                    panelCarritosGrid.add(crearTarjetaCarrito(partes[0], partes[1]));
                    panelCarritosGrid.add(Box.createRigidArea(new Dimension(0, 15)));
                }
            }
        }
        panelCarritosGrid.revalidate();
        panelCarritosGrid.repaint();
    }

    // =========================================================
    // CREACIÓN DE COMPONENTES VISUALES (TARJETAS Y PESTAÑAS)
    // =========================================================

    // --- ESTILIZADOR DE PESTAÑAS MATERIAL DESIGN ---
    private JButton crearBotonPestana(String texto, boolean activo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (activo) {
            btn.setForeground(new Color(30, 105, 210)); // Azul Primario
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 4, 0, new Color(30, 105, 210)), // Línea inferior activa
                    new EmptyBorder(15, 25, 15, 25)
            ));
            btn.setBackground(new Color(245, 248, 255));
        } else {
            btn.setForeground(new Color(130, 140, 150)); // Gris
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 4, 0, Color.WHITE), // Invisible
                    new EmptyBorder(15, 25, 15, 25)
            ));
            btn.setBackground(Color.WHITE);
        }

        // Efecto Hover suave para las pestañas inactivas
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn.getForeground().equals(new Color(130, 140, 150))) {
                    btn.setForeground(new Color(80, 90, 100));
                    btn.setBackground(new Color(250, 250, 250));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (btn.getForeground().equals(new Color(80, 90, 100))) {
                    btn.setForeground(new Color(130, 140, 150));
                    btn.setBackground(Color.WHITE);
                }
            }
        });

        return btn;
    }

    private void activarPestana(JButton activa, JButton inactiva1, JButton inactiva2 ) {
        activa.setForeground(new Color(30, 105, 210));
        activa.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, new Color(30, 105, 210)), new EmptyBorder(15, 25, 15, 25)));
        activa.setBackground(new Color(245, 248, 255));

        inactiva1.setForeground(new Color(130, 140, 150));
        inactiva1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, Color.WHITE), new EmptyBorder(15, 25, 15, 25)));
        inactiva1.setBackground(Color.WHITE);

        inactiva2.setForeground(new Color(130, 140, 150));
        inactiva2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, Color.WHITE), new EmptyBorder(15, 25, 15, 25)));
        inactiva2.setBackground(Color.WHITE);
    }

    private JPanel crearTarjetaProducto(Producto producto) {
        PanelRedondeado tarjeta = new PanelRedondeado(20);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(15, 15, 15, 15));
        tarjeta.setPreferredSize(new Dimension(200, 240));

        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setBackground(new Color(248, 248, 255));
                tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) { tarjeta.setBackground(Color.WHITE); }
            @Override
            public void mouseClicked(MouseEvent e) {
                DialogoModificarProducto dialogo = new DialogoModificarProducto(VistaPrincipalTienda.this, producto, inventario);
                dialogo.mostrar();

                if (dialogo.isProductoBorrado()) {
                    String idProducto = producto.getId();
                    inventario.eliminarProductoPorId(idProducto);
                    if (conexionServidor.estaConectado()) conexionServidor.eliminarProductoAdmin(idProducto);
                    actualizarVistaProductos("");
                    registrarAccionAdmin("Eliminó el producto: " + idProducto);
                    JOptionPane.showMessageDialog(VistaPrincipalTienda.this, "Producto eliminado con éxito.", "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                }
                else if (dialogo.isGuardadoExitoso()) {
                    inventario.guardarEnCSV();
                    if (conexionServidor.estaConectado()) conexionServidor.enviarProductoAdmin(producto);
                    actualizarVistaProductos("");
                    registrarAccionAdmin("Modificó el producto: " + producto.getNombre());
                }
            }
        });

        // --- IMAGEN Y TEXTOS ---
        JLabel lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setOpaque(true);
        lblImagen.setBackground(new Color(240, 242, 245));
        ImageIcon imagen = crearIconoImagen(producto.getRutaImagen());
        if (imagen != null) lblImagen.setIcon(imagen);
        else {
            lblImagen.setText(producto.getNombre().substring(0, 1).toUpperCase());
            lblImagen.setFont(new Font("SansSerif", Font.BOLD, 48));
            lblImagen.setForeground(new Color(130, 130, 150));
        }
        tarjeta.add(lblImagen, BorderLayout.CENTER);

        JPanel panelTextos = new JPanel();
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
        panelTextos.setOpaque(false);
        panelTextos.setBorder(new EmptyBorder(12, 0, 0, 0));

        String categoria = (producto.getCategoria() != null && !producto.getCategoria().isEmpty()) ? producto.getCategoria().toUpperCase() : "SIN CATEGORÍA";
        JLabel lblCategoriaId = new JLabel(categoria + "  •  #" + producto.getId());
        lblCategoriaId.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblCategoriaId.setForeground(new Color(150, 150, 150));

        JLabel lblNombre = new JLabel(producto.getNombre());
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNombre.setForeground(new Color(40, 40, 40));

        JPanel panelPrecioStock = new JPanel(new BorderLayout());
        panelPrecioStock.setOpaque(false);
        panelPrecioStock.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lblPrecio = new JLabel(formatearPesos(producto.getPrecio()));
        lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblPrecio.setForeground(new Color(46, 139, 87));

        JLabel lblStock = new JLabel();
        lblStock.setFont(new Font("SansSerif", Font.BOLD, 12));
        int stock = producto.getStock();
        if (stock == 0) {
            lblStock.setText("¡Agotado!"); lblStock.setForeground(new Color(220, 53, 69));
        } else if (stock <= 5) {
            lblStock.setText("Quedan: " + stock); lblStock.setForeground(new Color(255, 140, 0));
        } else {
            lblStock.setText("Stock: " + stock); lblStock.setForeground(new Color(120, 120, 120));
        }

        panelPrecioStock.add(lblPrecio, BorderLayout.WEST);
        panelPrecioStock.add(lblStock, BorderLayout.EAST);

        panelTextos.add(lblCategoriaId);
        panelTextos.add(Box.createRigidArea(new Dimension(0, 4)));
        panelTextos.add(lblNombre);
        panelTextos.add(Box.createRigidArea(new Dimension(0, 10)));
        panelTextos.add(panelPrecioStock);

        tarjeta.add(panelTextos, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearTarjetaCarrito(String nombreCliente, String productosStr) {
        PanelRedondeado tarjeta = new PanelRedondeado(15);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(15, 20, 15, 20));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        JLabel lblCliente = new JLabel("👤 " + nombreCliente);
        lblCliente.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblCliente.setForeground(new Color(19, 40, 72));
        lblCliente.setBorder(new EmptyBorder(0, 0, 10, 0));
        tarjeta.add(lblCliente, BorderLayout.NORTH);

        JPanel panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setOpaque(false);

        String[] items = productosStr.split("-");
        double totalCarrito = 0;

        for (String item : items) {
            String[] info = item.split(",");
            if (info.length == 2) {
                Producto p = inventario.buscarProductoPorId(info[0]);
                int cantidad = Integer.parseInt(info[1]);
                String nombreProd = (p != null) ? p.getNombre() : "Producto Eliminado";
                totalCarrito += (p != null) ? p.getPrecio() * cantidad : 0;

                JLabel lblItem = new JLabel(" • " + cantidad + "x " + nombreProd);
                lblItem.setFont(new Font("SansSerif", Font.PLAIN, 14));
                lblItem.setForeground(new Color(60, 70, 80));
                panelLista.add(lblItem);
            }
        }
        tarjeta.add(panelLista, BorderLayout.CENTER);

        // --- BOTÓN: Anular Carrito (Lógica conectada al servidor) ---
        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.setOpaque(false);
        panelSur.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblTotal = new JLabel("Total apartado: " + formatearPesos(totalCarrito));
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotal.setForeground(new Color(46, 139, 87));
        panelSur.add(lblTotal, BorderLayout.WEST);

        JButton btnAnular = new JButton("❌ Anular");
        btnAnular.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnAnular.setBackground(new Color(231, 76, 60));
        btnAnular.setForeground(Color.WHITE);
        btnAnular.setFocusPainted(false);
        btnAnular.setBorderPainted(false);
        btnAnular.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnAnular.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "¿Forzar el vaciado del carrito de " + nombreCliente + "?", "Anular Carrito", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                if (conexionServidor.estaConectado()) conexionServidor.enviarMensaje("ADMIN_VACIAR_CARRITO;" + nombreCliente);
                registrarAccionAdmin("Vació el carrito de " + nombreCliente);
            }
        });
        panelSur.add(btnAnular, BorderLayout.EAST);
        tarjeta.add(panelSur, BorderLayout.SOUTH);

        return tarjeta;
    }

    // =========================================================
    // UTILIDADES GRÁFICAS, BITÁCORA Y DIÁLOGOS
    // =========================================================

    private void registrarAccionAdmin(String accion) {
        try {
            java.io.FileWriter escritor = new java.io.FileWriter("historial_admin.txt", true);
            String fecha = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            String nombreAdmin = sistema.getUsuarioActual() != null ? sistema.getUsuarioActual().getNombre() : "Admin";
            escritor.write("[" + fecha + "] " + nombreAdmin + " -> " + accion + "\n");
            escritor.close();
        } catch (Exception e) { }
    }

    private void mostrarDialogoEstadisticas() {
        JDialog dialog = new JDialog(this, "Estadísticas del Inventario", true);
        dialog.setSize(800, 800);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(new Color(245, 247, 250));

        JPanel panelCabecera = new JPanel();
        panelCabecera.setBackground(new Color(110, 216, 255));
        panelCabecera.setBorder(new EmptyBorder(15, 0, 15, 0));
        JLabel lblTitulo = new JLabel("📊 Estadísticas del Inventario");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        panelCabecera.add(lblTitulo);
        dialog.add(panelCabecera, BorderLayout.NORTH);

        JPanel panelEstadisticas = new JPanel();
        panelEstadisticas.setLayout(new BoxLayout(panelEstadisticas, BoxLayout.Y_AXIS));
        panelEstadisticas.setOpaque(false);
        panelEstadisticas.setBorder(BorderFactory.createEmptyBorder(25, 30, 15, 30));

        double valorTotal = inventario.calcularValorTotalInventario();
        JPanel panelTotal = new JPanel(new BorderLayout());
        panelTotal.setBackground(Color.WHITE);
        panelTotal.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), new EmptyBorder(15, 20, 15, 20)));
        panelTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lblTotalTexto = new JLabel("Valor total del inventario:");
        lblTotalTexto.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel lblTotalValor = new JLabel(formatearPesos(valorTotal));
        lblTotalValor.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotalValor.setForeground(new Color(110, 216, 255));
        panelTotal.add(lblTotalTexto, BorderLayout.WEST);
        panelTotal.add(lblTotalValor, BorderLayout.EAST);
        panelEstadisticas.add(panelTotal);
        panelEstadisticas.add(Box.createRigidArea(new Dimension(0, 25)));

        Set<String> categorias = new LinkedHashSet<>();
        for (Producto producto : inventario.getProductos()) {
            if (producto.getCategoria() != null && !producto.getCategoria().trim().isEmpty()) categorias.add(producto.getCategoria());
        }

        for (String categoria : categorias) {
            double promedio = inventario.calcularPrecioPromedioPorCategoria(categoria);
            Producto productoMenorStock = inventario.buscarMenorStockPorCategoria(categoria);

            JPanel panelCategoria = new JPanel(new GridLayout(3, 1, 0, 8));
            panelCategoria.setBackground(Color.WHITE);
            panelCategoria.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), new EmptyBorder(15, 20, 15, 20)));
            panelCategoria.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

            JLabel lblCategoria = new JLabel("📁 " + categoria);
            lblCategoria.setFont(new Font("SansSerif", Font.BOLD, 15));
            JLabel lblPromedio = new JLabel("Precio promedio: " + formatearPesos(promedio));
            String menorStockTexto = (productoMenorStock == null) ? "Menor stock: sin productos" : "Menor stock: " + productoMenorStock.getNombre() + " (" + productoMenorStock.getStock() + " unids)";
            JLabel lblMenorStock = new JLabel(menorStockTexto);
            lblMenorStock.setForeground(new Color(231, 76, 60));

            panelCategoria.add(lblCategoria);
            panelCategoria.add(lblPromedio);
            panelCategoria.add(lblMenorStock);
            panelEstadisticas.add(panelCategoria);
            panelEstadisticas.add(Box.createRigidArea(new Dimension(0, 12)));
        }

        JScrollPane scroll = new JScrollPane(panelEstadisticas);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        configurarBarraScroll(scroll);
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCerrar.setBackground(new Color(149, 165, 166));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.addActionListener(e -> dialog.dispose());
        panelBotones.add(btnCerrar);
        dialog.add(panelBotones, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void mostrarDialogoFiltros() {
        JTextField txtMinimo = new JTextField();
        JTextField txtMaximo = new JTextField();

        String[] categoriasGuardadas = inventario.getCategoriasDisponibles();
        java.util.Arrays.sort(categoriasGuardadas);
        JPanel panelCategorias = new JPanel();
        panelCategorias.setLayout(new BoxLayout(panelCategorias, BoxLayout.Y_AXIS));
        panelCategorias.setBackground(Color.WHITE);

        java.util.List<JCheckBox> listaCheckboxes = new java.util.ArrayList<>();
        for (String cat : categoriasGuardadas) {
            JCheckBox checkBox = new JCheckBox(cat);
            checkBox.setBackground(Color.WHITE);
            listaCheckboxes.add(checkBox);
            panelCategorias.add(checkBox);
        }

        JScrollPane scrollCategorias = new JScrollPane(panelCategorias);
        scrollCategorias.setPreferredSize(new Dimension(250, 120));
        configurarBarraScroll(scrollCategorias);

        Object[] mensaje = { "Selecciona las categorías:", scrollCategorias, " ", "Precio mínimo:", txtMinimo, "Precio máximo:", txtMaximo };

        JOptionPane panelDialogo = new JOptionPane(mensaje, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialogo = panelDialogo.createDialog(this, "Opciones de Filtro");
        dialogo.setVisible(true);

        Object valorOpcion = panelDialogo.getValue();
        if (valorOpcion instanceof Integer && (Integer) valorOpcion == JOptionPane.OK_OPTION) {
            java.util.List<String> categoriasSeleccionadas = new java.util.ArrayList<>();
            for (JCheckBox cb : listaCheckboxes) if (cb.isSelected()) categoriasSeleccionadas.add(cb.getText());

            double minimo = -1, maximo = -1;
            try {
                if (!txtMinimo.getText().trim().isEmpty()) minimo = Double.parseDouble(txtMinimo.getText().trim());
                if (!txtMaximo.getText().trim().isEmpty()) maximo = Double.parseDouble(txtMaximo.getText().trim());
            } catch (NumberFormatException ex) { return; }

            actualizarVistaConFiltros(categoriasSeleccionadas, minimo, maximo);
        }
    }

    private JButton crearBotonMenu(String texto, ActionListener accion) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 28));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(19, 40, 72));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (accion != null) btn.addActionListener(accion);
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(45, 77, 124)); btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(19, 40, 72)); }
        });
        return btn;
    }

    private String formatearPesos(double valor) {
        return "$" + String.format("%,.0f", valor).replace(",", ".");
    }

    private void configurarBarraScroll(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() { thumbColor = new Color(190, 198, 205); trackColor = new Color(245, 247, 250); }
            @Override protected JButton createDecreaseButton(int o) { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
            @Override protected JButton createIncreaseButton(int o) { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        });
    }

    private ImageIcon crearIconoImagen(String rutaImagen) {
        java.io.File archivo = ImagenesUtil.resolverRutaImagen(rutaImagen);
        if (archivo == null) return null;
        ImageIcon img = new ImageIcon(archivo.getAbsolutePath());
        return (img.getIconWidth() <= 0) ? null : new ImageIcon(img.getImage().getScaledInstance(170, 130, Image.SCALE_SMOOTH));
    }

    private ImageIcon cargarLogo() {
        String[] rutas = {"imagenes/logo sin fondo.png", "imagenes/Logo NEXOMARKET.jpeg", "imagenes/Logo NEXOMARKET.jpg", "imagenes/Logo NEXOMARKET.png"};
        for (String r : rutas) if (new java.io.File(r).isFile()) return new ImageIcon(new ImageIcon(r).getImage().getScaledInstance(180, 75, Image.SCALE_SMOOTH));
        return null;
    }
    // --- VISTA DE PEDIDOS ---
    private JPanel crearVistaPedidos() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 18, 18, 18));

        PanelRedondeado encabezado = new PanelRedondeado(12);
        encabezado.setBackground(new Color(19, 40, 72));
        encabezado.setLayout(new BorderLayout());
        encabezado.setBorder(new EmptyBorder(9, 12, 9, 12));

        JLabel titulo = new JLabel("Seguimiento y Estados de Pedidos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);
        encabezado.add(titulo, BorderLayout.WEST);
        panel.add(encabezado, BorderLayout.NORTH);

        panelPedidosGrid = new JPanel();
        panelPedidosGrid.setLayout(new BoxLayout(panelPedidosGrid, BoxLayout.Y_AXIS));
        panelPedidosGrid.setOpaque(false);
        panelPedidosGrid.setBorder(new EmptyBorder(15, 0, 15, 0));

        JScrollPane scroll = new JScrollPane(panelPedidosGrid);
        scroll.setBorder(null); scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        configurarBarraScroll(scroll);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void dibujarPedidosEnPanel() {
        if (panelPedidosGrid == null) return;
        panelPedidosGrid.removeAll();

        java.util.List<Backend.Compra> historial = inventario.getHistorialCompras();

        if (historial.isEmpty()) {
            JLabel vacio = new JLabel("<html><center><b>Aún no hay pedidos registrados.</b></center></html>");
            vacio.setFont(new Font("SansSerif", Font.PLAIN, 15)); vacio.setForeground(new Color(100, 110, 125)); vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelPedidosGrid.add(Box.createRigidArea(new Dimension(0, 50))); panelPedidosGrid.add(vacio);
        } else {
            for (int i = historial.size() - 1; i >= 0; i--) {
                panelPedidosGrid.add(crearTarjetaPedido(historial.get(i), i + 1));
                panelPedidosGrid.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }
        panelPedidosGrid.revalidate(); panelPedidosGrid.repaint();
    }

    private JPanel crearTarjetaPedido(Backend.Compra compra, int numeroPedido) {
        PanelRedondeado tarjeta = new PanelRedondeado(15);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(15, 20, 15, 20));

        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 155));
        tarjeta.setPreferredSize(new Dimension(0, 155));

        JPanel panelInfo = new JPanel(new GridLayout(4, 1, 0, 3));
        panelInfo.setOpaque(false);
        panelInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTitulo = new JLabel("📦 Pedido #" + numeroPedido + "  |  " + compra.getFechaHora().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 15)); lblTitulo.setForeground(new Color(19, 40, 72));

        JLabel lblRut = new JLabel("Cliente RUT: " + compra.getRutUsuario());
        lblRut.setFont(new Font("SansSerif", Font.PLAIN, 13)); lblRut.setForeground(new Color(80, 80, 80));

        JLabel lblArts = new JLabel("Artículos: " + String.join(", ", compra.getArticulos()));
        lblArts.setFont(new Font("SansSerif", Font.PLAIN, 13)); lblArts.setForeground(new Color(100, 100, 100));

        JLabel lblTotal = new JLabel("Total Pagado: " + formatearPesos(compra.getTotalPagado()));
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14)); lblTotal.setForeground(new Color(46, 139, 87));

        panelInfo.add(lblTitulo);
        panelInfo.add(lblRut);
        panelInfo.add(lblArts);
        panelInfo.add(lblTotal);

        tarjeta.add(panelInfo);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel panelEstado = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelEstado.setOpaque(false);
        panelEstado.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelEstado.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        JLabel lblEstadoText = new JLabel("Estado del Despacho:");
        lblEstadoText.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblEstadoText.setForeground(new Color(130, 140, 150));

        String[] estados = {"Pendiente", "En preparación", "Despachado", "Entregado", "Cancelado"};
        JComboBox<String> cbEstado = new JComboBox<>(estados);
        cbEstado.setFont(new Font("SansSerif", Font.BOLD, 13));
        cbEstado.setBackground(Color.WHITE);
        cbEstado.setSelectedItem(compra.getEstado());
        cbEstado.setPreferredSize(new Dimension(160, 30));

        cbEstado.addActionListener(e -> {
            String nuevoEstado = (String) cbEstado.getSelectedItem();
            if (!nuevoEstado.equals(compra.getEstado())) {
                if (nuevoEstado.equals("Cancelado")) {
                    int resp = JOptionPane.showConfirmDialog(this, "¿Estás seguro de cancelar este pedido?\nEl stock será devuelto al inventario.", "Cancelar Pedido", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (resp == JOptionPane.YES_OPTION) {
                        inventario.anularCompra(compra);
                        registrarAccionAdmin("Canceló el pedido #" + numeroPedido);
                        dibujarPedidosEnPanel();
                    } else {
                        cbEstado.setSelectedItem(compra.getEstado());
                    }
                } else {
                    compra.setEstado(nuevoEstado);
                    registrarAccionAdmin("Actualizó pedido #" + numeroPedido + " a: " + nuevoEstado);
                    JOptionPane.showMessageDialog(this, "El pedido #" + numeroPedido + " ahora está: " + nuevoEstado);
                }
            }
        });

        panelEstado.add(lblEstadoText);
        panelEstado.add(cbEstado);

        tarjeta.add(panelEstado);

        return tarjeta;
    }
}