package Frontend;

import Backend.Inventario;
import Backend.Producto;
import Backend.ConexionServidor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogoCarritosActivos extends JDialog {

    private final Inventario inventario;
    private final ConexionServidor conexion;
    private final JPanel panelContenedor;

    // Colores corporativos
    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color VERDE = new Color(46, 139, 87);

    public DialogoCarritosActivos(JFrame parent, Inventario inventario, ConexionServidor conexion) {
        super(parent, "Monitor de Carritos Activos", false); // false para que el Admin pueda verla y seguir usando la tienda de fondo
        this.inventario = inventario;
        this.conexion = conexion;

        setSize(550, 650);
        setLocationRelativeTo(parent);
        setResizable(false);

        // Inicializamos el contenedor principal de las tarjetas
        panelContenedor = new JPanel();
        panelContenedor.setLayout(new BoxLayout(panelContenedor, BoxLayout.Y_AXIS));
        panelContenedor.setOpaque(false);
        panelContenedor.setBorder(new EmptyBorder(15, 20, 15, 20));
    }

    public void mostrar() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        // ==========================================
        // CABECERA
        // ==========================================
        JPanel panelCabecera = new JPanel(new BorderLayout());
        panelCabecera.setBackground(NAVY);
        panelCabecera.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblTitulo = new JLabel("🛒 Carritos en Tiempo Real");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        panelCabecera.add(lblTitulo, BorderLayout.WEST);

        // Botón Actualizar en la cabecera
        JButton btnActualizar = new JButton("↻ Actualizar");
        btnActualizar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnActualizar.setForeground(NAVY);
        btnActualizar.setBackground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setBorderPainted(false);
        btnActualizar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizar.addActionListener(e -> solicitarDatos());

        panelCabecera.add(btnActualizar, BorderLayout.EAST);
        add(panelCabecera, BorderLayout.NORTH);

        // ==========================================
        // ÁREA DE SCROLL PARA LAS TARJETAS
        // ==========================================
        JScrollPane scroll = new JScrollPane(panelContenedor);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        configurarBarraScroll(scroll);

        add(scroll, BorderLayout.CENTER);

        // ==========================================
        // BOTÓN CERRAR (SUR)
        // ==========================================
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSur.setOpaque(false);
        panelSur.setBorder(new EmptyBorder(10, 0, 15, 0));

        JButton btnCerrar = new JButton("Cerrar Monitor");
        btnCerrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCerrar.setBackground(new Color(149, 165, 166));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setPreferredSize(new Dimension(160, 40));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());
        panelSur.add(btnCerrar);

        add(panelSur, BorderLayout.SOUTH);

        // Solicitamos los datos apenas se abre la ventana
        solicitarDatos();
        setVisible(true);
    }

    private void solicitarDatos() {
        panelContenedor.removeAll();
        JLabel lblCargando = new JLabel("Cargando datos desde el servidor...");
        lblCargando.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblCargando.setForeground(new Color(100, 110, 125));
        lblCargando.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelContenedor.add(Box.createRigidArea(new Dimension(0, 50)));
        panelContenedor.add(lblCargando);
        panelContenedor.revalidate();
        panelContenedor.repaint();

        if (conexion.estaConectado()) {
            conexion.solicitarCarritosActivos();
        }
    }

    // Este método lo llamaremos desde VistaPrincipalTienda cuando llegue el mensaje del servidor
    public void dibujarCarritos(String datosServidor) {
        panelContenedor.removeAll();

        if (datosServidor.equals("VACIO")) {
            JLabel vacio = new JLabel("<html><center><b>No hay carritos activos en este momento</b><br><br>Los clientes no tienen productos reservados.</center></html>");
            vacio.setFont(new Font("SansSerif", Font.PLAIN, 15));
            vacio.setForeground(new Color(100, 110, 125));
            vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelContenedor.add(Box.createRigidArea(new Dimension(0, 50)));
            panelContenedor.add(vacio);
        } else {
            // Formato esperado: Cliente 1:P001,2-P002,1|Cliente 2:P003,5
            String[] clientes = datosServidor.split("\\|");

            for (String datosCliente : clientes) {
                String[] partes = datosCliente.split(":");
                if (partes.length == 2) {
                    String nombreCliente = partes[0];
                    String productosStr = partes[1];

                    panelContenedor.add(crearTarjetaCarrito(nombreCliente, productosStr));
                    panelContenedor.add(Box.createRigidArea(new Dimension(0, 15)));
                }
            }
        }

        panelContenedor.revalidate();
        panelContenedor.repaint();
    }

    private JPanel crearTarjetaCarrito(String nombreCliente, String productosStr) {
        PanelRedondeado tarjeta = new PanelRedondeado(15);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(15, 20, 15, 20));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // Título del cliente
        JLabel lblCliente = new JLabel("👤 " + nombreCliente);
        lblCliente.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblCliente.setForeground(NAVY);
        lblCliente.setBorder(new EmptyBorder(0, 0, 10, 0));
        tarjeta.add(lblCliente, BorderLayout.NORTH);

        // Lista de productos
        JPanel panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setOpaque(false);

        String[] items = productosStr.split("-");
        double totalCarrito = 0;

        for (String item : items) {
            String[] info = item.split(",");
            if (info.length == 2) {
                String idProducto = info[0];
                int cantidad = Integer.parseInt(info[1]);

                Producto p = inventario.buscarProductoPorId(idProducto);
                String nombreProd = (p != null) ? p.getNombre() : "Producto Desconocido";
                double subtotal = (p != null) ? p.getPrecio() * cantidad : 0;
                totalCarrito += subtotal;

                JLabel lblItem = new JLabel(" • " + cantidad + "x " + nombreProd);
                lblItem.setFont(new Font("SansSerif", Font.PLAIN, 14));
                lblItem.setForeground(new Color(60, 70, 80));
                panelLista.add(lblItem);
                panelLista.add(Box.createRigidArea(new Dimension(0, 4)));
            }
        }
        tarjeta.add(panelLista, BorderLayout.CENTER);

        // Total del carrito
        JLabel lblTotal = new JLabel("Total apartado: $" + String.format("%,.0f", totalCarrito).replace(',', '.'));
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotal.setForeground(VERDE);
        lblTotal.setBorder(new EmptyBorder(10, 0, 0, 0));
        tarjeta.add(lblTotal, BorderLayout.SOUTH);

        return tarjeta;
    }

    private void configurarBarraScroll(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor = new Color(190, 198, 205);
                trackColor = FONDO;
            }
            @Override
            protected JButton createDecreaseButton(int orientation) { return crearBotonNulo(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return crearBotonNulo(); }
            private JButton crearBotonNulo() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
    }
}