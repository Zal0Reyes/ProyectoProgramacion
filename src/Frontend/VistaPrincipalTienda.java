package Frontend;

import Backend.Inventario;
import Backend.Producto;
import Backend.Sistema;
import Backend.ConexionServidor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.Set;

public class VistaPrincipalTienda extends BaseFrame {

    // =========================================================
    // VARIABLES GLOBALES DE LA CLASE
    // =========================================================
    private JPanel gridProductos;
    private final ConexionServidor conexionServidor;

    // --- NUEVO: Instancia del Monitor de Carritos ---
    private DialogoCarritosActivos monitorCarritos;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================
    public VistaPrincipalTienda(Inventario inventario, Sistema sistema) {
        super("Vista Admin", inventario, sistema);
        conexionServidor = new ConexionServidor();

        // --- CONEXIÓN: Configurar qué hacer al recibir un mensaje ---
        conexionServidor.setReceptorMensajes(mensaje -> procesarMensajeServidor(mensaje));
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
        // 1. ZONA DE LA CABECERA SUPERIOR
        // ==========================================
        JPanel contenedorCabecera = new JPanel(new BorderLayout());
        contenedorCabecera.setBackground(Color.WHITE);

        JPanel panelCabecera = new JPanel(new BorderLayout(15, 0));
        panelCabecera.setBackground(Color.WHITE);
        panelCabecera.setBorder(new EmptyBorder(8, 20, 8, 20));

        // --- SECCIÓN: LOGO ---
        JLabel lblLogo = new JLabel();
        ImageIcon logo = cargarLogo();
        if (logo != null) {
            lblLogo.setIcon(logo);
        } else {
            lblLogo.setText("NEXOMARKET ADMIN");
            lblLogo.setFont(new Font("SansSerif", Font.BOLD, 23));
            lblLogo.setForeground(new Color(19, 40, 72));
        }
        panelCabecera.add(lblLogo, BorderLayout.WEST);

        // --- SECCIÓN: BARRA DE BÚSQUEDA ---
        JTextField txtBuscar = new JTextField();
        txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 15));
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 205)),
                new EmptyBorder(9, 12, 9, 12)
        ));

        JPanel panelBusqueda = new JPanel(new BorderLayout());
        panelBusqueda.setOpaque(false);
        panelBusqueda.setBorder(new EmptyBorder(15, 0, 15, 0));
        panelBusqueda.add(txtBuscar, BorderLayout.CENTER);
        panelCabecera.add(panelBusqueda, BorderLayout.CENTER);

        // --- SECCIÓN: BOTONES DERECHA (CABECERA) ---
        // BOTÓN: Monitor de Carritos Activos
        JButton btnCarritos = new JButton("🛒 Carritos");
        btnCarritos.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnCarritos.setForeground(Color.WHITE);
        btnCarritos.setBackground(new Color(46, 139, 87)); // VERDE
        btnCarritos.setPreferredSize(new Dimension(120, 45));
        btnCarritos.setFocusPainted(false);
        btnCarritos.setBorderPainted(false);
        btnCarritos.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCarritos.addActionListener(e -> abrirMonitorCarritos());

        // BOTÓN: Filtrar
        JButton btnFiltro = new JButton("Filtrar");
        btnFiltro.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnFiltro.setForeground(Color.WHITE);
        btnFiltro.setBackground(new Color(30, 105, 210)); // AZUL
        btnFiltro.setPreferredSize(new Dimension(120, 45));
        btnFiltro.setFocusPainted(false);
        btnFiltro.setBorderPainted(false);
        btnFiltro.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFiltro.addActionListener(e -> mostrarDialogoFiltros());

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        acciones.setOpaque(false);
        acciones.add(btnCarritos);
        acciones.add(btnFiltro);
        panelCabecera.add(acciones, BorderLayout.EAST);

        // FRANJA INFERIOR CABECERA
        JPanel franja = new JPanel();
        franja.setBackground(new Color(19, 40, 72));
        franja.setPreferredSize(new Dimension(0, 5));

        contenedorCabecera.add(panelCabecera, BorderLayout.CENTER);
        contenedorCabecera.add(franja, BorderLayout.SOUTH);
        add(contenedorCabecera, BorderLayout.NORTH);

        // ==========================================
        // 2. ZONA DE LA BARRA LATERAL (MENÚ)
        // ==========================================
        JPanel barraLateral = new JPanel();
        barraLateral.setBackground(new Color(19, 40, 72));
        barraLateral.setPreferredSize(new Dimension(80, 0));
        barraLateral.setLayout(new BoxLayout(barraLateral, BoxLayout.Y_AXIS));
        barraLateral.setBorder(new EmptyBorder(30, 0, 30, 0));

        // --- BOTÓN: Inicio ---
        JButton btnInicio = crearBotonMenu("🏠", e -> actualizarVistaProductos());
        barraLateral.add(btnInicio);
        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- BOTÓN: Agregar Producto ---
        JButton btnAgregar = crearBotonMenu("➕", e -> {
            DialogoAgregarProducto dialogo = new DialogoAgregarProducto(VistaPrincipalTienda.this, inventario);
            dialogo.mostrar();
            if (dialogo.isGuardadoExitoso()) {
                Producto productoGuardado = dialogo.getProductoGuardado();
                if (productoGuardado != null && conexionServidor.estaConectado()) {
                    conexionServidor.enviarProductoAdmin(productoGuardado);
                }
                actualizarVistaProductos();
                JOptionPane.showMessageDialog(VistaPrincipalTienda.this, "Producto agregado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        barraLateral.add(btnAgregar);
        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- BOTÓN: Registrar Nuevo Admin ---
        JButton btnAgregarAdmin = crearBotonMenu("👨‍", e -> {
            DialogoRegistroAdmin dialogoAdmin = new DialogoRegistroAdmin(VistaPrincipalTienda.this, sistema);
            dialogoAdmin.mostrar();
        });
        btnAgregarAdmin.setToolTipText("Registrar Nuevo Administrador");
        barraLateral.add(btnAgregarAdmin);
        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- BOTÓN: Estadísticas ---
        JButton btnEstadisticas = crearBotonMenu("📊", e -> mostrarDialogoEstadisticas());
        barraLateral.add(btnEstadisticas);
        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- BOTÓN: Historial de Compras ---
        JButton btnHistorial = crearBotonMenu("\uD83D\uDCCB", e -> mostrarHistorialCompras());
        btnHistorial.setToolTipText("Historial de compras");
        barraLateral.add(btnHistorial);
        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- BOTÓN: Vaciar Inventario (Destrucción) ---
        JButton btnVaciar = crearBotonMenu("🗑", e -> {
            DialogoVaciarInventario dialogoVaciar = new DialogoVaciarInventario(VistaPrincipalTienda.this, inventario);
            dialogoVaciar.mostrar();
            if (dialogoVaciar.huboCambios()) {
                actualizarVistaProductos();
            }
        });
        btnVaciar.setToolTipText("Gestión Masiva / Vaciar Inventario");
        barraLateral.add(btnVaciar);
        barraLateral.add(Box.createVerticalGlue());

        // --- BOTÓN: Cerrar Sesión ---
        JButton btnCerrarSesion = crearBotonMenu("🚪", e -> {
            int confirmacion = JOptionPane.showConfirmDialog(VistaPrincipalTienda.this, "¿Estás seguro de que deseas cerrar sesión?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirmacion == JOptionPane.YES_OPTION) {
                sistema.cerrarSesion();
                conexionServidor.cerrarConexion();
                dispose();
                VistaCliente vistaCliente = new VistaCliente(inventario, sistema);
                vistaCliente.mostrar();
            }
        });
        barraLateral.add(btnCerrarSesion);

        add(barraLateral, BorderLayout.WEST);

        // ==========================================
        // 3. ZONA CENTRAL (CATÁLOGO ADMIN)
        // ==========================================
        JPanel panelCentral = new JPanel(new BorderLayout(15, 0));
        panelCentral.setOpaque(false);
        panelCentral.setBorder(new EmptyBorder(15, 18, 18, 18));

        // --- ENCABEZADO CATÁLOGO ---
        PanelRedondeado encabezado = new PanelRedondeado(12);
        encabezado.setBackground(new Color(19, 40, 72));
        encabezado.setLayout(new BorderLayout());
        encabezado.setBorder(new EmptyBorder(9, 12, 9, 12));

        JLabel titulo = new JLabel("Gestión de Inventario");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);
        encabezado.add(titulo, BorderLayout.WEST);
        panelCentral.add(encabezado, BorderLayout.NORTH);

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
        configurarBarraScroll(scroll);

        panelCentral.add(scroll, BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        // --- EVENTO: Búsqueda dinámica ---
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                actualizarVistaProductos(txtBuscar.getText());
            }
        });

        // Carga inicial
        actualizarVistaProductos();
        if (conexionServidor.estaConectado()) {
            conexionServidor.solicitarStockActual();
        }

        setVisible(true);
    }

    // =========================================================
    // COMUNICACIÓN CON EL SERVIDOR (RED)
    // =========================================================
    private void procesarMensajeServidor(String mensaje) {

        // --- ACCIÓN: Recepción de carritos activos en tiempo real ---
        if (mensaje.startsWith("CARRITOS_ACTIVOS;")) {
            String datos = mensaje.substring(17);
            SwingUtilities.invokeLater(() -> {
                if (monitorCarritos != null && monitorCarritos.isVisible()) {
                    monitorCarritos.dibujarCarritos(datos);
                }
            });
            return;
        }

        // --- ACCIÓN: Producto Modificado ---
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
                    actualizarVistaProductos();
                });
            } catch (NumberFormatException e) {
                return;
            }
            return;
        }

        // --- ACCIÓN: Producto Eliminado ---
        if (mensaje.startsWith("PRODUCTO_ELIMINADO;")) {
            String[] datos = mensaje.split(";");
            if (datos.length != 2) return;

            SwingUtilities.invokeLater(() -> {
                inventario.eliminarProductoDesdeServidor(datos[1]);
                actualizarVistaProductos();
            });
            return;
        }

        // --- ACCIÓN: Stock Actualizado por reservas ---
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
                Producto producto = inventario.buscarProductoPorId(idProducto);
                if (producto == null) return;
                producto.setStock(nuevoStock);
                actualizarVistaProductos();
            });
            return;
        }

        // --- ACCIÓN: Notificación de Nueva Venta Confirmada ---
        if (mensaje.startsWith("VENTA_REALIZADA;")) {
            String[] datos = mensaje.split(";", 6);
            if (datos.length != 6) return;

            String rut = datos[1].trim();
            if (rut.isEmpty()) rut = "Sin RUT";

            double subtotal, iva, total;
            try {
                subtotal = Double.parseDouble(datos[2]);
                iva = Double.parseDouble(datos[3]);
                total = Double.parseDouble(datos[4]);
            } catch (NumberFormatException e) {
                return;
            }

            java.util.List<String> articulos = new java.util.ArrayList<>();
            if (!datos[5].trim().isEmpty()) {
                String[] lista = datos[5].split("\\|");
                for (String articulo : lista) articulos.add(articulo);
            }

            String rutFinal = rut;
            double subtotalFinal = subtotal, ivaFinal = iva, totalFinal = total;

            SwingUtilities.invokeLater(() -> {
                Backend.Compra compra = new Backend.Compra(rutFinal, articulos, totalFinal);
                inventario.agregarCompraEnMemoria(compra);

                JOptionPane.showMessageDialog(this,
                        "Nueva venta realizada\n\n"
                                + "RUT: " + rutFinal
                                + "\n\nProductos:\n" + String.join("\n", articulos)
                                + "\n\nSubtotal: " + formatearPesos(subtotalFinal)
                                + "\nIVA (19%): " + formatearPesos(ivaFinal)
                                + "\nTotal: " + formatearPesos(totalFinal),
                        "Venta realizada", JOptionPane.INFORMATION_MESSAGE);
            });
        }
    }

    // =========================================================
    // LÓGICA DE DIBUJO DE PRODUCTOS Y VISTAS
    // =========================================================

    // --- MÉTODO: Abre la ventana del monitor de carritos en vivo ---
    private void abrirMonitorCarritos() {
        if (monitorCarritos == null) {
            monitorCarritos = new DialogoCarritosActivos(this, inventario, conexionServidor);
        }
        monitorCarritos.mostrar();
    }

    private void actualizarVistaProductos() {
        actualizarVistaProductos("");
    }

    private void actualizarVistaProductos(String textoBusqueda) {
        if (gridProductos == null) return; // Validación anticrash
        gridProductos.removeAll();

        String busqueda = textoBusqueda.toLowerCase();
        for (Producto p : inventario.getProductos()) {
            boolean coincideNombre = p.getNombre().toLowerCase().contains(busqueda);
            boolean coincideId = p.getId().toLowerCase().contains(busqueda);

            if (textoBusqueda.isEmpty() || coincideNombre || coincideId) {
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

            if (pasaCategoria && pasaPrecio) {
                gridProductos.add(crearTarjetaProducto(p));
            }
        }
        gridProductos.revalidate();
        gridProductos.repaint();
    }

    // --- MÉTODO: Diseña la tarjeta individual de producto ---
    private JPanel crearTarjetaProducto(Producto producto) {
        PanelRedondeado tarjeta = new PanelRedondeado(20);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(15, 15, 15, 15));
        tarjeta.setPreferredSize(new Dimension(200, 240));

        // --- EVENTO: Al hacer clic en el producto (Modificar) ---
        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setBackground(new Color(248, 248, 255));
                tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                tarjeta.setBackground(Color.WHITE);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                DialogoModificarProducto dialogo = new DialogoModificarProducto(VistaPrincipalTienda.this, producto, inventario);
                dialogo.mostrar();

                if (dialogo.isProductoBorrado()) {
                    String idProducto = producto.getId();
                    inventario.eliminarProductoPorId(idProducto);

                    if (conexionServidor.estaConectado()) conexionServidor.eliminarProductoAdmin(idProducto);
                    actualizarVistaProductos();
                    JOptionPane.showMessageDialog(VistaPrincipalTienda.this, "Producto eliminado con éxito.", "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                }
                else if (dialogo.isGuardadoExitoso()) {
                    inventario.guardarEnCSV();
                    if (conexionServidor.estaConectado()) conexionServidor.enviarProductoAdmin(producto);
                    actualizarVistaProductos();
                    JOptionPane.showMessageDialog(VistaPrincipalTienda.this, "Producto modificado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // --- IMAGEN ---
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

        // --- TEXTOS ---
        JPanel panelTextos = new JPanel();
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
        panelTextos.setOpaque(false);
        panelTextos.setBorder(new EmptyBorder(12, 0, 0, 0));

        String categoria = (producto.getCategoria() != null && !producto.getCategoria().isEmpty()) ? producto.getCategoria().toUpperCase() : "SIN CATEGORÍA";
        JLabel lblCategoriaId = new JLabel(categoria + "  •  #" + producto.getId());
        lblCategoriaId.setFont(new Font("SansSerif", Font.BOLD, 10));
        lblCategoriaId.setForeground(new Color(150, 150, 150));
        lblCategoriaId.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNombre = new JLabel(producto.getNombre());
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNombre.setForeground(new Color(40, 40, 40));
        lblNombre.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel panelPrecioStock = new JPanel(new BorderLayout());
        panelPrecioStock.setOpaque(false);
        panelPrecioStock.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelPrecioStock.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lblPrecio = new JLabel(formatearPesos(producto.getPrecio()));
        lblPrecio.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblPrecio.setForeground(new Color(46, 139, 87));

        JLabel lblStock = new JLabel();
        lblStock.setFont(new Font("SansSerif", Font.BOLD, 12));
        int stock = producto.getStock();
        if (stock == 0) {
            lblStock.setText("¡Agotado!");
            lblStock.setForeground(new Color(220, 53, 69));
        } else if (stock <= 5) {
            lblStock.setText("Quedan: " + stock);
            lblStock.setForeground(new Color(255, 140, 0));
        } else {
            lblStock.setText("Stock: " + stock);
            lblStock.setForeground(new Color(120, 120, 120));
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

    // =========================================================
    // DIÁLOGOS (Estadísticas y Filtros)
    // =========================================================
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
        lblTitulo.setForeground(new Color(40, 40, 40));
        panelCabecera.add(lblTitulo);
        dialog.add(panelCabecera, BorderLayout.NORTH);

        JPanel panelContenido = crearPanelEstadisticas();
        JScrollPane scroll = new JScrollPane(panelContenido);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        configurarBarraScroll(scroll);
        dialog.add(scroll, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(10, 30, 20, 30));
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setBackground(new Color(149, 165, 166));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setPreferredSize(new Dimension(110, 40));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dialog.dispose());
        panelBotones.add(btnCerrar);
        dialog.add(panelBotones, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private JPanel crearPanelEstadisticas() {
        JPanel panelEstadisticas = new JPanel();
        panelEstadisticas.setLayout(new BoxLayout(panelEstadisticas, BoxLayout.Y_AXIS));
        panelEstadisticas.setOpaque(false);
        panelEstadisticas.setBorder(BorderFactory.createEmptyBorder(25, 30, 15, 30));

        double valorTotal = inventario.calcularValorTotalInventario();
        JPanel panelTotal = new JPanel(new BorderLayout());
        panelTotal.setBackground(Color.WHITE);
        panelTotal.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), new EmptyBorder(15, 20, 15, 20)));
        panelTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        panelTotal.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTotalTexto = new JLabel("Valor total del inventario:");
        lblTotalTexto.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotalTexto.setForeground(new Color(80, 80, 80));

        JLabel lblTotalValor = new JLabel(formatearPesos(valorTotal));
        lblTotalValor.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotalValor.setForeground(new Color(110, 216, 255));

        panelTotal.add(lblTotalTexto, BorderLayout.WEST);
        panelTotal.add(lblTotalValor, BorderLayout.EAST);
        panelEstadisticas.add(panelTotal);
        panelEstadisticas.add(Box.createRigidArea(new Dimension(0, 25)));

        Set<String> categorias = new LinkedHashSet<>();
        for (Producto producto : inventario.getProductos()) {
            if (producto.getCategoria() != null && !producto.getCategoria().trim().isEmpty()) {
                categorias.add(producto.getCategoria());
            }
        }

        if (categorias.isEmpty()) {
            JLabel lblSinDatos = new JLabel("No hay categorías registradas aún.");
            lblSinDatos.setFont(new Font("SansSerif", Font.BOLD, 14));
            lblSinDatos.setForeground(new Color(120, 120, 120));
            lblSinDatos.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelEstadisticas.add(lblSinDatos);
            return panelEstadisticas;
        }

        JLabel lblTituloCat = new JLabel("Cálculos por Categoría");
        lblTituloCat.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTituloCat.setForeground(new Color(40, 40, 40));
        lblTituloCat.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelEstadisticas.add(lblTituloCat);
        panelEstadisticas.add(Box.createRigidArea(new Dimension(0, 15)));

        for (String categoria : categorias) {
            double promedio = inventario.calcularPrecioPromedioPorCategoria(categoria);
            Producto productoMenorStock = inventario.buscarMenorStockPorCategoria(categoria);

            JPanel panelCategoria = new JPanel(new GridLayout(3, 1, 0, 8));
            panelCategoria.setBackground(Color.WHITE);
            panelCategoria.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)), new EmptyBorder(15, 20, 15, 20)));
            panelCategoria.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
            panelCategoria.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblCategoria = new JLabel("📁 " + categoria);
            lblCategoria.setFont(new Font("SansSerif", Font.BOLD, 15));
            lblCategoria.setForeground(new Color(40, 40, 40));

            JLabel lblPromedio = new JLabel("Precio promedio: " + formatearPesos(promedio));
            lblPromedio.setFont(new Font("SansSerif", Font.PLAIN, 14));
            lblPromedio.setForeground(new Color(80, 80, 80));

            String menorStockTexto = (productoMenorStock == null) ? "Menor stock: sin productos" : "Menor stock: " + productoMenorStock.getNombre() + " (" + productoMenorStock.getStock() + " unids)";
            JLabel lblMenorStock = new JLabel(menorStockTexto);
            lblMenorStock.setFont(new Font("SansSerif", Font.PLAIN, 14));
            lblMenorStock.setForeground(new Color(231, 76, 60));

            panelCategoria.add(lblCategoria);
            panelCategoria.add(lblPromedio);
            panelCategoria.add(lblMenorStock);
            panelEstadisticas.add(panelCategoria);
            panelEstadisticas.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        return panelEstadisticas;
    }

    private void mostrarHistorialCompras() {
        new DialogoHistorialCompras(this, inventario).mostrar();
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
            checkBox.setFocusPainted(false);
            checkBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
            listaCheckboxes.add(checkBox);
            panelCategorias.add(checkBox);
        }

        JScrollPane scrollCategorias = new JScrollPane(panelCategorias);
        scrollCategorias.setPreferredSize(new Dimension(250, 120));
        scrollCategorias.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        configurarBarraScroll(scrollCategorias);

        Object[] mensaje = {
                "Selecciona las categorías:", scrollCategorias,
                " ",
                "Precio mínimo (opcional):", txtMinimo,
                "Precio máximo (opcional):", txtMaximo
        };

        JOptionPane panelDialogo = new JOptionPane(mensaje, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialogo = panelDialogo.createDialog(this, "Opciones de Filtro");
        aplanarBotones(panelDialogo);
        dialogo.setVisible(true);

        Object valorOpcion = panelDialogo.getValue();
        int opcion = valorOpcion instanceof Integer ? (Integer) valorOpcion : JOptionPane.CLOSED_OPTION;

        if (opcion == JOptionPane.OK_OPTION) {
            java.util.List<String> categoriasSeleccionadas = new java.util.ArrayList<>();
            for (JCheckBox cb : listaCheckboxes) {
                if (cb.isSelected()) categoriasSeleccionadas.add(cb.getText());
            }

            String minTxt = txtMinimo.getText().trim();
            String maxTxt = txtMaximo.getText().trim();
            double minimo = -1, maximo = -1;

            try {
                if (!minTxt.isEmpty()) minimo = Double.parseDouble(minTxt);
                if (!maxTxt.isEmpty()) maximo = Double.parseDouble(maxTxt);

                if ((minimo >= 0 && maximo >= 0) && (minimo > maximo)) {
                    JOptionPane.showMessageDialog(this, "El precio mínimo no puede ser mayor que el máximo.", "Rango inválido", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese valores numéricos válidos en los precios.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            actualizarVistaConFiltros(categoriasSeleccionadas, minimo, maximo);
        }
    }

    // =========================================================
    // UTILIDADES GRÁFICAS Y TEXTO
    // =========================================================
    private String formatearPesos(double valor) {
        return "$" + String.format("%,.0f", valor).replace(",", ".");
    }

    private void configurarBarraScroll(JScrollPane scroll) {
        configurarBarraScroll(scroll.getVerticalScrollBar(), new Dimension(8, 0));
        configurarBarraScroll(scroll.getHorizontalScrollBar(), new Dimension(0, 8));
    }

    private void configurarBarraScroll(JScrollBar barra, Dimension dimension) {
        barra.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(190, 198, 205);
                trackColor = new Color(245, 247, 250);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return crearBotonBarraScroll(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return crearBotonBarraScroll(); }
            private JButton crearBotonBarraScroll() {
                JButton boton = new JButton();
                boton.setPreferredSize(new Dimension(0, 0));
                boton.setMinimumSize(new Dimension(0, 0));
                boton.setMaximumSize(new Dimension(0, 0));
                return boton;
            }
        });
        barra.setPreferredSize(dimension);
        barra.setOpaque(false);
    }

    private void aplanarBotones(Container contenedor) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JButton) {
                JButton boton = (JButton) componente;
                boton.setFont(new Font("SansSerif", Font.PLAIN, 13));
                boton.setForeground(new Color(60, 70, 80));
                boton.setBackground(Color.WHITE);
                boton.setBorder(BorderFactory.createLineBorder(new Color(205, 211, 217)));
                boton.setFocusPainted(false);
                boton.setContentAreaFilled(true);
                boton.setOpaque(true);
                boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                boton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { boton.setBackground(new Color(238, 248, 252)); }
                    @Override
                    public void mouseExited(MouseEvent e) { boton.setBackground(Color.WHITE); }
                });
            } else if (componente instanceof Container) {
                aplanarBotones((Container) componente);
            }
        }
    }

    private ImageIcon crearIconoImagen(String rutaImagen) {
        java.io.File archivo = ImagenesUtil.resolverRutaImagen(rutaImagen);
        if (archivo == null) return null;

        ImageIcon imagenOriginal = new ImageIcon(archivo.getAbsolutePath());
        if (imagenOriginal.getIconWidth() <= 0 || imagenOriginal.getIconHeight() <= 0) return null;

        Image imagenRedimensionada = imagenOriginal.getImage().getScaledInstance(170, 130, Image.SCALE_SMOOTH);
        return new ImageIcon(imagenRedimensionada);
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
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(45, 77, 124));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(19, 40, 72));
            }
        });
        return btn;
    }

    private ImageIcon cargarLogo() {
        String[] rutas = {"imagenes/logo sin fondo.png", "imagenes/Logo NEXOMARKET.jpeg", "imagenes/Logo NEXOMARKET.jpg", "imagenes/Logo NEXOMARKET.png"};
        for (String ruta : rutas) {
            java.io.File archivo = new java.io.File(ruta);
            if (archivo.isFile()) {
                ImageIcon original = new ImageIcon(ruta);
                Image escalada = original.getImage().getScaledInstance(180, 75, Image.SCALE_SMOOTH);
                return new ImageIcon(escalada);
            }
        }
        return null;
    }
}