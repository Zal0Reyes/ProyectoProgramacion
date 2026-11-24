package Frontend;

import Backend.GestorDespacho;
import Backend.TarifaEnvio;
import Backend.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogoDespacho extends JDialog {

    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color AZUL_HOVER = new Color(25, 90, 180);
    private static final Color GRIS_TEXTO = new Color(100, 110, 125);
    private static final Color GRIS_BORDE = new Color(210, 220, 230);
    private static final Color VERDE = new Color(35, 145, 85);

    private final GestorDespacho gestorDespacho;
    private final double subtotalNeto;
    private final Usuario usuarioActual;

    private JRadioButton rbRetiro;
    private JRadioButton rbDomicilio;
    private JTextField txtReceptor;
    private JTextField txtComuna;
    private JComboBox<String> cbRegion;
    private JLabel lblDiasEstimados;
    private JLabel lblCostoEnvio;
    private JLabel lblPromoGratis;

    private boolean confirmado = false;
    private double costoEnvioSeleccionado = 0;
    private String resumenEntrega = "Retiro en tienda";

    public DialogoDespacho(JFrame parent, GestorDespacho gestorDespacho, double subtotalNeto, Usuario usuarioActual) {
        super(parent, "Modalidad de Entrega", true);
        this.gestorDespacho = gestorDespacho;
        this.gestorDespacho.cargarDesdeCSV();
        this.subtotalNeto = subtotalNeto;
        this.usuarioActual = usuarioActual;

        setSize(470, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void mostrar() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        // ==========================================
        // CABECERA
        // ==========================================
        JPanel cabecera = new JPanel();
        cabecera.setBackground(NAVY);
        cabecera.setBorder(new EmptyBorder(18, 0, 18, 0));

        JLabel titulo = new JLabel("🚚 Modalidad y Datos de Entrega");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 19));
        titulo.setForeground(Color.WHITE);
        cabecera.add(titulo);
        add(cabecera, BorderLayout.NORTH);

        // ==========================================
        // CONTENIDO CENTRAL
        // ==========================================
        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(18, 25, 12, 25));

        PanelRedondeado tarjeta = new PanelRedondeado(20);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBorder(new EmptyBorder(18, 20, 18, 20));
        tarjeta.setAlignmentX(Component.CENTER_ALIGNMENT);

        rbRetiro = new JRadioButton("Retiro en tienda ($0)", true);
        rbDomicilio = new JRadioButton("Despacho a domicilio");

        estilarRadio(rbRetiro);
        estilarRadio(rbDomicilio);

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbRetiro);
        grupo.add(rbDomicilio);

        JPanel filaRadios = new JPanel(new GridLayout(1, 2, 12, 0));
        filaRadios.setOpaque(false);
        filaRadios.setAlignmentX(Component.LEFT_ALIGNMENT);
        filaRadios.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        filaRadios.add(rbRetiro);
        filaRadios.add(rbDomicilio);

        txtReceptor = crearCampoTexto(usuarioActual != null ? usuarioActual.getNombre() : "");
        txtComuna = crearCampoTexto("");

        cbRegion = new JComboBox<>();
        for (TarifaEnvio tarifa : gestorDespacho.getListaTarifas()) {
            cbRegion.addItem(tarifa.getRegion());
        }
        cbRegion.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cbRegion.setForeground(NAVY);
        cbRegion.setBackground(Color.WHITE);
        cbRegion.setAlignmentX(Component.LEFT_ALIGNMENT);
        cbRegion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        cbRegion.setPreferredSize(new Dimension(0, 38));

        // Caja destacada de estimación y costo
        PanelRedondeado cajaInfo = new PanelRedondeado(14);
        cajaInfo.setBackground(new Color(246, 249, 253));
        cajaInfo.setLayout(new BoxLayout(cajaInfo, BoxLayout.Y_AXIS));
        cajaInfo.setBorder(new EmptyBorder(12, 14, 12, 14));
        cajaInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDiasEstimados = new JLabel();
        lblDiasEstimados.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblDiasEstimados.setForeground(GRIS_TEXTO);
        lblDiasEstimados.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblCostoEnvio = new JLabel();
        lblCostoEnvio.setFont(new Font("SansSerif", Font.BOLD, 15));
        lblCostoEnvio.setForeground(NAVY);
        lblCostoEnvio.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblPromoGratis = new JLabel();
        lblPromoGratis.setFont(new Font("SansSerif", Font.ITALIC, 11));
        lblPromoGratis.setForeground(VERDE);
        lblPromoGratis.setAlignmentX(Component.LEFT_ALIGNMENT);

        cajaInfo.add(lblDiasEstimados);
        cajaInfo.add(Box.createRigidArea(new Dimension(0, 4)));
        cajaInfo.add(lblCostoEnvio);
        cajaInfo.add(Box.createRigidArea(new Dimension(0, 4)));
        cajaInfo.add(lblPromoGratis);

        ActionListener alCambiar = e -> actualizarEstadoCampos();
        rbRetiro.addActionListener(alCambiar);
        rbDomicilio.addActionListener(alCambiar);
        cbRegion.addActionListener(alCambiar);

        tarjeta.add(filaRadios);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 14)));
        tarjeta.add(crearEtiqueta("Nombre de quien recibe"));
        tarjeta.add(Box.createRigidArea(new Dimension(0, 5)));
        tarjeta.add(txtReceptor);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 12)));
        tarjeta.add(crearEtiqueta("Comuna"));
        tarjeta.add(Box.createRigidArea(new Dimension(0, 5)));
        tarjeta.add(txtComuna);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 12)));
        tarjeta.add(crearEtiqueta("Región"));
        tarjeta.add(Box.createRigidArea(new Dimension(0, 5)));
        tarjeta.add(cbRegion);
        tarjeta.add(Box.createRigidArea(new Dimension(0, 14)));
        tarjeta.add(cajaInfo);

        centro.add(tarjeta);
        add(centro, BorderLayout.CENTER);

        // ==========================================
        // BOTONES INFERIORES
        // ==========================================
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 15));
        sur.setOpaque(false);
        sur.setBorder(new EmptyBorder(0, 25, 8, 25));

        JButton btnCancelar = crearBotonSecundario("Cancelar");
        JButton btnContinuar = crearBotonPrincipal("Continuar");

        btnCancelar.addActionListener(e -> dispose());
        btnContinuar.addActionListener(e -> confirmarSeleccion());

        sur.add(btnCancelar);
        sur.add(btnContinuar);
        add(sur, BorderLayout.SOUTH);

        actualizarEstadoCampos();
        setVisible(true);
    }

    private void actualizarEstadoCampos() {
        boolean esDomicilio = rbDomicilio.isSelected();
        txtReceptor.setEnabled(esDomicilio);
        txtComuna.setEnabled(esDomicilio);
        cbRegion.setEnabled(esDomicilio);

        double minimoGratis = gestorDespacho.getMontoMinimoEnvioGratis();
        String minimoTxt = "$" + String.format("%,.0f", minimoGratis).replace(",", ".");
        lblPromoGratis.setText(minimoGratis > 0 ? "Envío gratis en compras desde " + minimoTxt : "");

        if (!esDomicilio) {
            lblDiasEstimados.setText("⏱️ Entrega: Retiro inmediato en tienda");
            lblCostoEnvio.setText("Costo de envío: $0");
            lblCostoEnvio.setForeground(VERDE);
            return;
        }

        String region = cbRegion.getSelectedItem() != null ? cbRegion.getSelectedItem().toString() : "";
        TarifaEnvio tarifa = gestorDespacho.buscarPorRegion(region);
        double costo = gestorDespacho.calcularCostoEnvio(true, region, subtotalNeto);

        String diasTexto = (tarifa != null) ? tarifa.getDiasEstimados() + " días hábiles" : "5 días hábiles";
        lblDiasEstimados.setText("⏱️ Llegada estimada: " + diasTexto);

        if (costo == 0 && minimoGratis > 0 && subtotalNeto >= minimoGratis) {
            lblCostoEnvio.setText("Costo de envío: GRATIS ($0)");
            lblCostoEnvio.setForeground(VERDE);
        } else {
            lblCostoEnvio.setText("Costo de envío: $" + String.format("%,.0f", costo).replace(",", "."));
            lblCostoEnvio.setForeground(NAVY);
        }
    }

    private void confirmarSeleccion() {
        if (!rbDomicilio.isSelected()) {
            costoEnvioSeleccionado = 0;
            resumenEntrega = "Retiro en tienda";
            confirmado = true;
            dispose();
            return;
        }

        String receptor = txtReceptor.getText().trim();
        String comuna = txtComuna.getText().trim();
        String region = cbRegion.getSelectedItem() != null ? cbRegion.getSelectedItem().toString().trim() : "";

        if (receptor.isEmpty() || comuna.isEmpty() || region.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Por favor, ingresa el nombre de quien recibe, la comuna y la región.",
                    "Datos incompletos",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        costoEnvioSeleccionado = gestorDespacho.calcularCostoEnvio(true, region, subtotalNeto);
        resumenEntrega = "Domicilio (" + region + ", " + comuna + " - Recibe: " + receptor + ")";
        confirmado = true;
        dispose();
    }

    private void estilarRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setFont(new Font("SansSerif", Font.BOLD, 13));
        rb.setForeground(NAVY);
        rb.setFocusPainted(false);
        rb.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JLabel crearEtiqueta(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(GRIS_TEXTO);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField crearCampoTexto(String valorInicial) {
        JTextField txt = new JTextField(valorInicial);
        txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txt.setForeground(NAVY);
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txt.setPreferredSize(new Dimension(0, 38));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRIS_BORDE, 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return txt;
    }

    private JButton crearBotonPrincipal(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(AZUL);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(130, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(AZUL_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(AZUL);
            }
        });
        return btn;
    }

    private JButton crearBotonSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(new Color(230, 235, 240));
        btn.setForeground(NAVY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(210, 220, 230));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(230, 235, 240));
            }
        });
        return btn;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public double getCostoEnvioSeleccionado() {
        return costoEnvioSeleccionado;
    }

    public String getResumenEntrega() {
        return resumenEntrega;
    }
}