package Frontend;

import Backend.Compra;
import Backend.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class DialogoComprobante extends JDialog {

    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);

    public DialogoComprobante(
            JFrame parent,
            Compra compra,
            Map<Producto, Integer> productosComprados,
            double subtotalOriginal,
            double descuento,
            double subtotalNeto,
            double iva,
            double total
    ) {
        this(parent, compra, productosComprados, subtotalOriginal, descuento, subtotalNeto, 0, "", iva, total);
    }

    public DialogoComprobante(
            JFrame parent,
            Compra compra,
            Map<Producto, Integer> productosComprados,
            double subtotalOriginal,
            double descuento,
            double subtotalNeto,
            double costoEnvio,
            String infoDespacho,
            double iva,
            double total
    ) {
        super(parent, "Comprobante de Venta - NexoMarket", true);
        setSize(450, 640);
        setLocationRelativeTo(parent);
        setResizable(false);

        initComponents(
                compra,
                productosComprados,
                subtotalOriginal,
                descuento,
                subtotalNeto,
                costoEnvio,
                infoDespacho,
                iva,
                total
        );
    }

    private void initComponents(
            Compra compra,
            Map<Producto, Integer> productosComprados,
            double subtotalOriginal,
            double descuento,
            double subtotalNeto,
            double costoEnvio,
            String infoDespacho,
            double iva,
            double total
    ) {
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        // CABECERA
        JPanel panelCabecera = new JPanel();
        panelCabecera.setBackground(NAVY);
        panelCabecera.setBorder(new EmptyBorder(15, 0, 15, 0));

        JLabel lblTitulo = new JLabel("🧾 Comprobante de Compra");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        panelCabecera.add(lblTitulo);
        add(panelCabecera, BorderLayout.NORTH);

        // TEXTO DEL COMPROBANTE
        JTextArea txtBoleta = new JTextArea();
        txtBoleta.setEditable(false);
        txtBoleta.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtBoleta.setBackground(Color.WHITE);
        txtBoleta.setBorder(new EmptyBorder(15, 15, 15, 15));

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        StringBuilder sb = new StringBuilder();

        sb.append("========================================\n");
        sb.append("           NEXOMARKET ONLINE            \n");
        sb.append("========================================\n");
        sb.append(" Fecha: ").append(compra.getFechaHora().format(formato)).append("\n");
        sb.append(" RUT Cliente: ").append(compra.getRutUsuario()).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("PRODUCTO                 CANT.   SUBTOTAL\n");
        sb.append("----------------------------------------\n");

        for (Map.Entry<Producto, Integer> entry : productosComprados.entrySet()) {
            Producto producto = entry.getKey();
            int cantidad = entry.getValue();
            double subtotalProducto = producto.getPrecio() * cantidad;

            String nombreProducto = producto.getNombre();
            if (nombreProducto.length() > 20) {
                nombreProducto = nombreProducto.substring(0, 17) + "...";
            }

            sb.append(String.format("%-22s %-5d $%9.0f\n", nombreProducto, cantidad, subtotalProducto));
        }

        sb.append("----------------------------------------\n");
        sb.append(" Subtotal:               $").append(formatearNumero(subtotalOriginal)).append("\n");

        if (compra.getCodigoCupon() != null && !compra.getCodigoCupon().isEmpty()) {
            sb.append(" Cupón utilizado:         ").append(compra.getCodigoCupon()).append("\n");
            sb.append(" Descuento:              -$").append(formatearNumero(descuento)).append("\n");
            sb.append(" Subtotal neto:           $").append(formatearNumero(subtotalNeto)).append("\n");
        }
        String textoEnvio = (costoEnvio == 0) ? "GRATIS ($0)" : "$" + formatearNumero(costoEnvio);
        sb.append(" Costo de envío:         ").append(textoEnvio).append("\n");

        sb.append(" IVA (19%):              $").append(formatearNumero(iva)).append("\n");
        sb.append("----------------------------------------\n");
        sb.append(" TOTAL A PAGAR:          $").append(formatearNumero(total)).append("\n");
        sb.append("========================================\n");
        sb.append("      ¡Gracias por su preferencia!      \n");
        sb.append("========================================\n");

        txtBoleta.setText(sb.toString());
        txtBoleta.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(txtBoleta);
        scroll.setBorder(new EmptyBorder(15, 20, 15, 20));
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        configurarBarraScroll(scroll);
        add(scroll, BorderLayout.CENTER);

        // BOTÓN CERRAR
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSur.setOpaque(false);
        panelSur.setBorder(new EmptyBorder(0, 0, 20, 0));

        JButton btnAceptar = new JButton("Aceptar y Cerrar");
        btnAceptar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnAceptar.setBackground(AZUL);
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorderPainted(false);
        btnAceptar.setPreferredSize(new Dimension(160, 40));
        btnAceptar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAceptar.addActionListener(e -> dispose());

        panelSur.add(btnAceptar);
        add(panelSur, BorderLayout.SOUTH);
    }

    private String formatearNumero(double valor) {
        return String.format("%,.0f", valor).replace(',', '.');
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

    public void mostrar() {
        setVisible(true);
    }
}