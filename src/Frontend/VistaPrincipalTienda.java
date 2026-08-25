package Frontend;

import Backend.Inventario;
import Backend.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashSet;
import java.util.Set;

public class VistaPrincipalTienda extends BaseFrame {

    private JPanel gridProductos;

    public VistaPrincipalTienda(Inventario inventario) {
        super("Vista Tienda", inventario);
    }

    @Override
    public void mostrar() {

        // --- CONFIGURACIÓN BASE DEL FRAME ---
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        // ==========================================
        // --- BARRA LATERAL (MENÚ) ---
        // ==========================================
        JPanel barraLateral = new JPanel();
        barraLateral.setBackground(new Color(255, 204, 51));
        barraLateral.setPreferredSize(new Dimension(80, 0));
        barraLateral.setLayout(new BoxLayout(barraLateral, BoxLayout.Y_AXIS));
        barraLateral.setBorder(new EmptyBorder(30, 0, 30, 0));

        // BOTÓN INICIO
        barraLateral.add(crearBotonMenu("🏠", null));
        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        // BOTÓN AGREGAR CON ACCIÓN
        JButton btnAgregar = crearBotonMenu("➕", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarDialogoAgregar();
            }
        });
        barraLateral.add(btnAgregar);

        // BOTÓN ESTADÍSTICAS EN VENTANA SEPARADA
        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));
        JButton btnEstadisticas = crearBotonMenu("📊", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarDialogoEstadisticas();
            }
        });
        barraLateral.add(btnEstadisticas);

        // BOTÓN CONFIGURACIÓN
        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));
        barraLateral.add(crearBotonMenu("⚙", null));

        add(barraLateral, BorderLayout.EAST);

        // ==========================================
        // --- ÁREA CENTRAL PRINCIPAL ---
        // ==========================================
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setOpaque(false);
        panelCentral.setBorder(new EmptyBorder(30, 40, 30, 40));

        // ------------------------------------------
        // --- PANEL NORTE (CABECERA) ---
        // ------------------------------------------
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setOpaque(false);
        panelNorte.setBorder(new EmptyBorder(0, 0, 20, 0)); // Margen inferior

        // TÍTULO
        JLabel lblTitulo = new JLabel("Inventario de Productos");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(40, 40, 40));
        panelNorte.add(lblTitulo, BorderLayout.WEST);

        // ZONA DE BÚSQUEDA
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBusqueda.setOpaque(false);

        JLabel lblBuscar = new JLabel("🔍 Buscar:");
        lblBuscar.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblBuscar.setForeground(new Color(100, 100, 100));

        JTextField txtBuscar = new JTextField(15);
        txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 16));

        // ESTILO BARRA DE BÚSQUEDA
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // EVENTO DE BÚSQUEDA DINÁMICA
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                actualizarVistaProductos(txtBuscar.getText());
            }
        });

        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscar);

        panelNorte.add(panelBusqueda, BorderLayout.EAST);

        // ENSAMBLAJE PANEL NORTE
        panelCentral.add(panelNorte, BorderLayout.NORTH);

        // ------------------------------------------
        // --- PANEL CENTRAL (GRILLA DE PRODUCTOS) ---
        // ------------------------------------------
        gridProductos = new JPanel(new GridLayout(0, 3, 20, 20));
        gridProductos.setOpaque(false);

        // WRAPPER ANTI-ESTIRAMIENTO
        JPanel wrapperAntiEstiramiento = new JPanel(new BorderLayout());
        wrapperAntiEstiramiento.setOpaque(false);
        wrapperAntiEstiramiento.add(gridProductos, BorderLayout.NORTH);

        // SCROLL DE LA GRILLA
        JScrollPane scroll = new JScrollPane(wrapperAntiEstiramiento);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        // ENSAMBLAJE FINAL ÁREA CENTRAL
        panelCentral.add(scroll, BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);

        // --- CARGA DE DATOS INICIALES ---
        actualizarVistaProductos();

        // --- HACER VISIBLE LA VENTANA ---
        setVisible(true);
    }

    // --- MÉTODOS DE ACTUALIZACIÓN ---

    private void actualizarVistaProductos() {
        actualizarVistaProductos("");
    }

    private void actualizarVistaProductos(String textoBusqueda) {
        gridProductos.removeAll(); // Limpiar grilla visual

        String busqueda = textoBusqueda.toLowerCase();

        for (Producto p : inventario.getProductos()) {
            boolean coincideNombre = p.getNombre().toLowerCase().contains(busqueda);
            boolean coincideId = p.getId().toLowerCase().contains(busqueda);

            if (textoBusqueda.isEmpty() || coincideNombre || coincideId) {
                gridProductos.add(crearTarjetaProducto(p));
            }
        }

        // REDIBUJAR PANTALLA
        gridProductos.revalidate();
        gridProductos.repaint();
    }

    private void mostrarDialogoEstadisticas() {
        JDialog dialog = new JDialog(this, "Cálculos Básicos", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel panelContenido = crearPanelEstadisticas();
        JScrollPane scroll = new JScrollPane(panelContenido);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private JPanel crearPanelEstadisticas() {
        JPanel panelEstadisticas = new JPanel();
        panelEstadisticas.setLayout(new BoxLayout(panelEstadisticas, BoxLayout.Y_AXIS));
        panelEstadisticas.setOpaque(false);
        panelEstadisticas.setBorder(new EmptyBorder(10, 10, 10, 10));

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
            panelEstadisticas.add(lblSinDatos);
            return panelEstadisticas;
        }

        JLabel lblTitulo = new JLabel("Cálculos básicos por categoría");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(40, 40, 40));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelEstadisticas.add(lblTitulo);
        panelEstadisticas.add(Box.createRigidArea(new Dimension(0, 10)));

        for (String categoria : categorias) {
            double promedio = inventario.calcularPrecioPromedioPorCategoria(categoria);
            Producto productoMenorStock = inventario.buscarMenorStockPorCategoria(categoria);

            JPanel panelCategoria = new JPanel(new GridLayout(3, 1, 0, 4));
            panelCategoria.setOpaque(false);
            panelCategoria.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230)),
                    new EmptyBorder(10, 12, 10, 12)
            ));
            panelCategoria.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblCategoria = new JLabel("Categoría: " + categoria);
            lblCategoria.setFont(new Font("SansSerif", Font.BOLD, 14));
            lblCategoria.setForeground(new Color(60, 60, 60));

            JLabel lblPromedio = new JLabel("Precio promedio: $" + String.format("%.2f", promedio));
            lblPromedio.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblPromedio.setForeground(new Color(90, 90, 90));

            String menorStockTexto = (productoMenorStock == null)
                    ? "Menor stock: sin productos"
                    : "Menor stock: " + productoMenorStock.getNombre() + " (" + productoMenorStock.getStock() + ")";
            JLabel lblMenorStock = new JLabel(menorStockTexto);
            lblMenorStock.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblMenorStock.setForeground(new Color(90, 90, 90));

            panelCategoria.add(lblCategoria);
            panelCategoria.add(lblPromedio);
            panelCategoria.add(lblMenorStock);
            panelEstadisticas.add(panelCategoria);
            panelEstadisticas.add(Box.createRigidArea(new Dimension(0, 8)));
        }

        return panelEstadisticas;
    }

    // --- MÉTODOS DE DIÁLOGO Y TARJETAS ---

    private void mostrarDialogoAgregar() {
        JTextField txtNombre = new JTextField();
        JTextField txtPrecio = new JTextField();
        JTextField txtStock = new JTextField();
        JTextField txtCategoria = new JTextField();

        Object[] mensaje = {
                "Nombre del Producto:", txtNombre,
                "Precio:", txtPrecio,
                "Stock:", txtStock,
                "Categoria:", txtCategoria,
        };

        int opcion = JOptionPane.showConfirmDialog(this, mensaje, "Agregar Nuevo Producto", JOptionPane.OK_CANCEL_OPTION);

        if (opcion == JOptionPane.OK_OPTION) {
            try {
                String nombre = txtNombre.getText();
                int stock = Integer.parseInt(txtStock.getText());
                double precio = Double.parseDouble(txtPrecio.getText());
                String categoria = txtCategoria.getText();

                inventario.registrarProducto(nombre, precio, stock, categoria);
                actualizarVistaProductos();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor ingrese un precio válido (solo números).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel crearTarjetaProducto(Producto producto) {
        // --- CONFIGURACIÓN BASE TARJETA ---
        PanelRedondeado tarjeta = new PanelRedondeado(20);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(15, 15, 15, 15));
        tarjeta.setPreferredSize(new Dimension(200, 240));

        // EVENTOS DE RATÓN (HOVER Y CLICK)
        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setBackground(new Color(248, 248, 248));
                tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                tarjeta.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                DialogoModificarProducto dialogo = new DialogoModificarProducto(VistaPrincipalTienda.this, producto);
                dialogo.mostrar();

                if (dialogo.isProductoBorrado()) {
                    inventario.eliminarProductoPorId(producto.getId());
                    actualizarVistaProductos();
                    JOptionPane.showMessageDialog(VistaPrincipalTienda.this, "Producto eliminado con éxito.", "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                }
                else if (dialogo.isGuardadoExitoso()) {
                    inventario.guardarEnCSV();
                    actualizarVistaProductos();
                    JOptionPane.showMessageDialog(VistaPrincipalTienda.this, "Producto modificado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // --- IMAGEN PLACEHOLDER ---
        PanelRedondeado imgPlaceholder = new PanelRedondeado(15);
        imgPlaceholder.setBackground(new Color(235, 235, 235));
        imgPlaceholder.setPreferredSize(new Dimension(100, 140));
        tarjeta.add(imgPlaceholder, BorderLayout.CENTER);

        // --- TEXTOS DE LA TARJETA ---
        JPanel panelTextos = new JPanel(new GridLayout(2, 1, 0, 5));
        panelTextos.setOpaque(false);
        panelTextos.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblNombre = new JLabel(producto.getNombre());
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNombre.setForeground(new Color(60, 60, 60));

        String precioFormateado = "$" + String.format("%.2f", producto.getPrecio());
        JLabel lblPrecio = new JLabel(precioFormateado);
        lblPrecio.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblPrecio.setForeground(new Color(150, 150, 150));

        panelTextos.add(lblNombre);
        panelTextos.add(lblPrecio);
        tarjeta.add(panelTextos, BorderLayout.SOUTH);

        return tarjeta;
    }

    private JButton crearBotonMenu(String texto, ActionListener accion) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 28));
        btn.setForeground(Color.DARK_GRAY);
        btn.setBackground(new Color(255, 204, 51));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (accion != null) {
            btn.addActionListener(accion);
        }

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(255, 220, 90));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(255, 204, 51));
            }
        });
        return btn;
    }
}