package Frontend;

import Backend.Compra;
import Backend.Inventario;
import Backend.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DialogoMisCompras extends JDialog {
    private final Inventario inventario;
    private final Usuario usuario;

    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color GRIS_TEXTO = new Color(100, 110, 125);
    private static final Color VERDE_PRECIO = new Color(46, 139, 87);

    public DialogoMisCompras(JDialog parent, Inventario inventario, Usuario usuario) {
        super(parent, "Mis Compras Realizadas", true);
        this.inventario = inventario;
        this.usuario = usuario;

        setSize(520, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void mostrar() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        // ==========================================
        // CABECERA
        // ==========================================
        JPanel panelCabecera = new JPanel();
        panelCabecera.setBackground(NAVY);
        panelCabecera.setBorder(new EmptyBorder(18, 0, 18, 0));

        JLabel lblTitulo = new JLabel("📦 Mis Compras (" + usuario.getNombre() + ")");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 19));
        lblTitulo.setForeground(Color.WHITE);
        panelCabecera.add(lblTitulo);

        add(panelCabecera, BorderLayout.NORTH);

        // ==========================================
        // LISTA DE COMPRAS DEL CLIENTE
        // ==========================================
        JPanel panelLista = new JPanel();
        panelLista.setLayout(new BoxLayout(panelLista, BoxLayout.Y_AXIS));
        panelLista.setOpaque(false);
        panelLista.setBorder(new EmptyBorder(20, 25, 20, 25));

        List<Compra> misCompras = filtrarComprasDeUsuario();

        if (misCompras.isEmpty()) {
            PanelRedondeado tarjetaVacia = new PanelRedondeado(20);
            tarjetaVacia.setBackground(Color.WHITE);
            tarjetaVacia.setLayout(new BorderLayout());
            tarjetaVacia.setBorder(new EmptyBorder(40, 20, 40, 20));

            JLabel lblVacio = new JLabel(
                    "<html><center><b>Aún no tienes compras asociadas a tu RUT (" + usuario.getRut() + ")</b>"
                            + "<br><br>Cuando realices una compra en la tienda ingresando tu RUT,"
                            + "<br>aparecerá registrada aquí.</center></html>",
                    SwingConstants.CENTER
            );
            lblVacio.setFont(new Font("SansSerif", Font.PLAIN, 14));
            lblVacio.setForeground(GRIS_TEXTO);

            tarjetaVacia.add(lblVacio, BorderLayout.CENTER);
            panelLista.add(tarjetaVacia);
        } else {
            DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

            // Recorremos de la más reciente a la más antigua
            for (int i = misCompras.size() - 1; i >= 0; i--) {
                Compra c = misCompras.get(i);
                panelLista.add(crearTarjetaCompra(c, i + 1, formatoFecha));
                panelLista.add(Box.createRigidArea(new Dimension(0, 15)));
            }
        }

        JScrollPane scroll = new JScrollPane(panelLista);
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
        panelSur.setBorder(new EmptyBorder(5, 0, 20, 0));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCerrar.setBackground(AZUL);
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setPreferredSize(new Dimension(140, 40));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());

        panelSur.add(btnCerrar);
        add(panelSur, BorderLayout.SOUTH);

        setVisible(true);
    }

    private List<Compra> filtrarComprasDeUsuario() {
        List<Compra> resultado = new ArrayList<>();
        if (inventario == null) return resultado;

        for (Compra c : inventario.getHistorialCompras()) {
            if (c.getRutUsuario().equalsIgnoreCase(usuario.getRut())) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    private JPanel crearTarjetaCompra(Compra compra, int numeroPedido, DateTimeFormatter formatoFecha) {
        PanelRedondeado tarjeta = new PanelRedondeado(18);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BorderLayout(0, 10));
        tarjeta.setBorder(new EmptyBorder(15, 20, 15, 20));
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        // Cabecera de la tarjeta: Pedido # y Fecha
        JPanel norte = new JPanel(new BorderLayout());
        norte.setOpaque(false);

        JLabel lblNum = new JLabel("🧾 Pedido #" + numeroPedido);
        lblNum.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblNum.setForeground(NAVY);

        JLabel lblFecha = new JLabel("📅 " + compra.getFechaHora().format(formatoFecha));
        lblFecha.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblFecha.setForeground(GRIS_TEXTO);

        norte.add(lblNum, BorderLayout.WEST);
        norte.add(lblFecha, BorderLayout.EAST);

        // Centro: Lista de artículos
        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setOpaque(false);

        for (String item : compra.getArticulos()) {
            JLabel lblItem = new JLabel("  • " + item);
            lblItem.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblItem.setForeground(new Color(60, 70, 80));
            centro.add(lblItem);
        }

        // Sur: Total pagado
        JPanel sur = new JPanel(new BorderLayout());
        sur.setOpaque(false);
        sur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)));

        JLabel lblTextoTotal = new JLabel("Total Pagado (IVA incl.):");
        lblTextoTotal.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTextoTotal.setForeground(GRIS_TEXTO);
        lblTextoTotal.setBorder(new EmptyBorder(8, 0, 0, 0));

        String totalFormateado = "$" + String.format("%,.0f", compra.getTotalPagado()).replace(",", ".");
        JLabel lblMontoTotal = new JLabel(totalFormateado);
        lblMontoTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblMontoTotal.setForeground(VERDE_PRECIO);
        lblMontoTotal.setBorder(new EmptyBorder(8, 0, 0, 0));

        sur.add(lblTextoTotal, BorderLayout.WEST);
        sur.add(lblMontoTotal, BorderLayout.EAST);

        tarjeta.add(norte, BorderLayout.NORTH);
        tarjeta.add(centro, BorderLayout.CENTER);
        tarjeta.add(sur, BorderLayout.SOUTH);

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
            protected JButton createDecreaseButton(int orientation) { return botonCero(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return botonCero(); }
            private JButton botonCero() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
    }
}