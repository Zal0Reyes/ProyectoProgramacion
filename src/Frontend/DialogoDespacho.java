package Frontend;

import Backend.Cliente;
import Backend.GestorDespacho;
import Backend.TarifaEnvio;
import Backend.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogoDespacho extends JDialog {

    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);

    private final GestorDespacho gestorDespacho;
    private final double subtotalCarrito;
    private final Usuario usuarioActual;

    private JRadioButton rbRetiro;
    private JRadioButton rbDomicilio;
    private JTextField txtReceptor;
    private JTextField txtDireccion;
    private JTextField txtComuna;
    private JComboBox<String> cbRegion;

    private JLabel lblCostoEnvio;
    private JLabel lblTiempoEstimado;

    private boolean confirmado = false;
    private double costoEnvioFinal = 0;
    private String modalidadElegida = "Retiro en tienda";
    private String detalleDireccion = "Sucursal NexoMarket";
    private String tiempoEstimado = "Retiro inmediato en tienda";

    public DialogoDespacho(JFrame parent, GestorDespacho gestorDespacho, double subtotalCarrito, Usuario usuarioActual) {
        super(parent, "Modalidad de Entrega", true);
        this.gestorDespacho = gestorDespacho;
        this.subtotalCarrito = subtotalCarrito;
        this.usuarioActual = usuarioActual;

        setSize(480, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void mostrar() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        // CABECERA
        JPanel cabecera = new JPanel();
        cabecera.setBackground(NAVY);
        cabecera.setBorder(new EmptyBorder(15, 0, 15, 0));
        JLabel titulo = new JLabel("🚚 Opciones de Despacho");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        cabecera.add(titulo);
        add(cabecera, BorderLayout.NORTH);

        // CENTRO
        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(15, 25, 15, 25));

        // 1. Selección de modalidad
        PanelRedondeado panelModalidad = new PanelRedondeado(15);
        panelModalidad.setBackground(Color.WHITE);
        panelModalidad.setLayout(new GridLayout(1, 2, 10, 0));
        panelModalidad.setBorder(new EmptyBorder(12, 15, 12, 15));

        rbRetiro = new JRadioButton("Retiro en tienda ($0)", true);
        rbDomicilio = new JRadioButton("Despacho a domicilio");
        rbRetiro.setOpaque(false);
        rbDomicilio.setOpaque(false);
        rbRetiro.setFont(new Font("SansSerif", Font.BOLD, 13));
        rbDomicilio.setFont(new Font("SansSerif", Font.BOLD, 13));

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(rbRetiro);
        grupo.add(rbDomicilio);

        panelModalidad.add(rbRetiro);
        panelModalidad.add(rbDomicilio);

        // 2. Formulario de datos de envío
        PanelRedondeado panelFormulario = new PanelRedondeado(15);
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setLayout(new GridLayout(4, 2, 10, 12));
        panelFormulario.setBorder(new EmptyBorder(15, 15, 15, 15));

        txtReceptor = new JTextField();
        txtDireccion = new JTextField();
        txtComuna = new JTextField();

        // Autocompletar si el cliente tiene sesión iniciada
        if (usuarioActual != null) {
            txtReceptor.setText(usuarioActual.getNombre());
            if (usuarioActual instanceof Cliente) {
                txtDireccion.setText(((Cliente) usuarioActual).getDireccion());
            }
        }

        cbRegion = new JComboBox<>();
        for (TarifaEnvio t : gestorDespacho.getListaTarifas()) {
            cbRegion.addItem(t.getRegion());
        }

        panelFormulario.add(new JLabel("Nombre quien recibe:"));
        panelFormulario.add(txtReceptor);
        panelFormulario.add(new JLabel("Dirección:"));
        panelFormulario.add(txtDireccion);
        panelFormulario.add(new JLabel("Comuna:"));
        panelFormulario.add(txtComuna);
        panelFormulario.add(new JLabel("Región:"));
        panelFormulario.add(cbRegion);

        // 3. Resumen de costo de envío y fecha estimada
        PanelRedondeado panelInfo = new PanelRedondeado(15);
        panelInfo.setBackground(Color.WHITE);
        panelInfo.setLayout(new GridLayout(2, 1, 0, 5));
        panelInfo.setBorder(new EmptyBorder(12, 15, 12, 15));

        lblCostoEnvio = new JLabel();
        lblCostoEnvio.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblCostoEnvio.setForeground(NAVY);

        lblTiempoEstimado = new JLabel();
        lblTiempoEstimado.setFont(new Font("SansSerif", Font.ITALIC, 13));
        lblTiempoEstimado.setForeground(AZUL);

        panelInfo.add(lblCostoEnvio);
        panelInfo.add(lblTiempoEstimado);

        centro.add(panelModalidad);
        centro.add(Box.createRigidArea(new Dimension(0, 12)));
        centro.add(panelFormulario);
        centro.add(Box.createRigidArea(new Dimension(0, 12)));
        centro.add(panelInfo);

        add(centro, BorderLayout.CENTER);

        // BOTONES SUR
        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
        sur.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(new Color(149, 165, 166));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBorderPainted(false);
        btnCancelar.setPreferredSize(new Dimension(120, 38));

        JButton btnContinuar = new JButton("Continuar");
        btnContinuar.setBackground(AZUL);
        btnContinuar.setForeground(Color.WHITE);
        btnContinuar.setFocusPainted(false);
        btnContinuar.setBorderPainted(false);
        btnContinuar.setPreferredSize(new Dimension(140, 38));

        btnCancelar.addActionListener(e -> dispose());
        btnContinuar.addActionListener(e -> validarYConfirmar());

        sur.add(btnCancelar);
        sur.add(btnContinuar);
        add(sur, BorderLayout.SOUTH);

        // Eventos para activar/desactivar campos y recalcular en vivo
        rbRetiro.addActionListener(e -> actualizarEstado());
        rbDomicilio.addActionListener(e -> actualizarEstado());
        cbRegion.addActionListener(e -> actualizarEstado());

        actualizarEstado();
        setVisible(true);
    }

    private void actualizarEstado() {
        boolean esDomicilio = rbDomicilio.isSelected();
        txtReceptor.setEnabled(esDomicilio);
        txtDireccion.setEnabled(esDomicilio);
        txtComuna.setEnabled(esDomicilio);
        cbRegion.setEnabled(esDomicilio);

        String region = cbRegion.getSelectedItem() != null ? cbRegion.getSelectedItem().toString() : "";
        costoEnvioFinal = gestorDespacho.calcularCostoEnvio(esDomicilio, region, subtotalCarrito);
        tiempoEstimado = gestorDespacho.obtenerTiempoEstimado(esDomicilio, region);

        if (esDomicilio && costoEnvioFinal == 0) {
            lblCostoEnvio.setText("Costo de envío: $0 (¡Envío GRATIS sobre $100.000!)");
        } else {
            lblCostoEnvio.setText("Costo de envío: $" + String.format("%,.0f", costoEnvioFinal).replace(",", "."));
        }
        lblTiempoEstimado.setText("Entrega estimada: " + tiempoEstimado);
    }

    private void validarYConfirmar() {
        if (rbDomicilio.isSelected()) {
            String receptor = txtReceptor.getText().trim();
            String dir = txtDireccion.getText().trim();
            String comuna = txtComuna.getText().trim();
            String region = cbRegion.getSelectedItem() != null ? cbRegion.getSelectedItem().toString() : "";

            if (receptor.isEmpty() || dir.isEmpty() || comuna.isEmpty() || region.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos de despacho.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            modalidadElegida = "Despacho a domicilio";
            detalleDireccion = dir + ", " + comuna + " (" + region + ") - Recibe: " + receptor;
        } else {
            modalidadElegida = "Retiro en tienda";
            detalleDireccion = "Sucursal NexoMarket";
        }

        confirmado = true;
        dispose();
    }

    public boolean isConfirmado() { return confirmado; }
    public double getCostoEnvioFinal() { return costoEnvioFinal; }
    public String getModalidadElegida() { return modalidadElegida; }
    public String getDetalleDireccion() { return detalleDireccion; }
    public String getTiempoEstimado() { return tiempoEstimado; }
}