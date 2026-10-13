package Frontend;

import Backend.Compra;
import Backend.Inventario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DialogoHistorialCompras extends JDialog {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Inventario inventario;
    private final JTextField txtRut;
    private final JPanel panelCompras;

    public DialogoHistorialCompras(Frame propietario, Inventario inventario) {
        super(propietario, "Historial de compras", true);
        this.inventario = inventario;

        setSize(800, 800);
        setLocationRelativeTo(propietario);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        JPanel panelCabecera = new JPanel();
        panelCabecera.setBackground(new Color(110, 216, 255));
        panelCabecera.setBorder(new EmptyBorder(15, 0, 15, 0));

        JLabel lblTitulo = new JLabel("📋 Historial de compras");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(40, 40, 40));
        panelCabecera.add(lblTitulo);
        add(panelCabecera, BorderLayout.NORTH);

        JPanel panelPrincipal = new JPanel(new BorderLayout(0, 15));
        panelPrincipal.setOpaque(false);
        panelPrincipal.setBorder(new EmptyBorder(20, 30, 0, 30));

        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltro.setBackground(Color.WHITE);
        panelFiltro.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblRut = new JLabel("Filtrar por RUT:");
        lblRut.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRut.setForeground(new Color(80, 80, 80));
        txtRut = new JTextField(15);
        txtRut.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtRut.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        JButton btnFiltrar = new JButton("Filtrar");
        JButton btnRestablecer = new JButton("Restablecer");
        estilizarBotonSecundario(btnFiltrar);
        estilizarBotonSecundario(btnRestablecer);

        btnFiltrar.addActionListener(e -> actualizarLista());
        btnRestablecer.addActionListener(e -> {
            txtRut.setText("");
            actualizarLista();
        });
        txtRut.addActionListener(e -> actualizarLista());

        panelFiltro.add(lblRut);
        panelFiltro.add(txtRut);
        panelFiltro.add(btnFiltrar);
        panelFiltro.add(btnRestablecer);
        panelPrincipal.add(panelFiltro, BorderLayout.NORTH);

        panelCompras = new JPanel();
        panelCompras.setLayout(new BoxLayout(panelCompras, BoxLayout.Y_AXIS));
        panelCompras.setOpaque(false);
        panelCompras.setBorder(new EmptyBorder(5, 0, 15, 0));

        JScrollPane scroll = new JScrollPane(panelCompras);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        configurarBarraScroll(scroll);
        panelPrincipal.add(scroll, BorderLayout.CENTER);
        add(panelPrincipal, BorderLayout.CENTER);

        JButton cerrar = new JButton("Cerrar");
        cerrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        cerrar.setForeground(Color.WHITE);
        cerrar.setBackground(new Color(149, 165, 166));
        cerrar.setFocusPainted(false);
        cerrar.setBorderPainted(false);
        cerrar.setPreferredSize(new Dimension(110, 40));
        cerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cerrar.addActionListener(e -> dispose());
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBoton.setOpaque(false);
        panelBoton.setBorder(new EmptyBorder(10, 30, 20, 30));
        panelBoton.add(cerrar);
        add(panelBoton, BorderLayout.SOUTH);

        actualizarLista();
    }

    public void mostrar() {
        setVisible(true);
    }

    private void actualizarLista() {
        String rutBuscado = txtRut.getText().trim();
        List<Compra> compras = inventario.getHistorialCompras().stream()
                .filter(compra -> rutBuscado.isEmpty()
                        || compra.getRutUsuario().equalsIgnoreCase(rutBuscado))
                .sorted(Comparator.comparing(Compra::getFechaHora))
                .collect(Collectors.toList());

        panelCompras.removeAll();
        if (compras.isEmpty()) {
            panelCompras.add(crearMensajeVacio(rutBuscado.isEmpty()
                    ? "No hay compras registradas."
                    : "No hay compras asociadas al RUT ingresado."));
            panelCompras.revalidate();
            panelCompras.repaint();
            return;
        }

        for (int i = 0; i < compras.size(); i++) {
            panelCompras.add(crearTarjetaCompra(compras.get(i), i + 1));
            panelCompras.add(Box.createRigidArea(new Dimension(0, 12)));
        }
        panelCompras.revalidate();
        panelCompras.repaint();
    }

    private JPanel crearTarjetaCompra(Compra compra, int numero) {
        JPanel tarjeta = new JPanel(new GridLayout(4, 1, 0, 6));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                new EmptyBorder(15, 20, 15, 20)
        ));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblCompra = new JLabel("Compra #" + numero + "  |  "
                + compra.getFechaHora().format(FORMATO_FECHA));
        lblCompra.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblCompra.setForeground(new Color(40, 40, 40));

        JLabel lblRut = new JLabel("RUT: " + compra.getRutUsuario());
        lblRut.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblRut.setForeground(new Color(80, 80, 80));

        JLabel lblArticulos = new JLabel("Artículos: " + String.join(", ", compra.getArticulos()));
        lblArticulos.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblArticulos.setForeground(new Color(80, 80, 80));

        JLabel lblTotal = new JLabel("Total pagado: $" +
                String.format("%,.0f", compra.getTotalPagado()).replace(',', '.'));
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTotal.setForeground(new Color(46, 139, 87));

        tarjeta.add(lblCompra);
        tarjeta.add(lblRut);
        tarjeta.add(lblArticulos);
        tarjeta.add(lblTotal);
        return tarjeta;
    }

    private JLabel crearMensajeVacio(String mensaje) {
        JLabel etiqueta = new JLabel(mensaje);
        etiqueta.setFont(new Font("SansSerif", Font.BOLD, 14));
        etiqueta.setForeground(new Color(120, 120, 120));
        etiqueta.setAlignmentX(Component.CENTER_ALIGNMENT);
        return etiqueta;
    }

    private void estilizarBotonSecundario(JButton boton) {
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setForeground(new Color(60, 70, 80));
        boton.setBackground(Color.WHITE);
        boton.setBorder(BorderFactory.createLineBorder(new Color(205, 211, 217)));
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
            protected JButton createDecreaseButton(int orientation) {
                return crearBotonBarraScroll();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return crearBotonBarraScroll();
            }

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
}