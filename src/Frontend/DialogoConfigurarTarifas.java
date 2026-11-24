package Frontend;

import Backend.GestorDespacho;
import Backend.TarifaEnvio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogoConfigurarTarifas extends JDialog {

    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);

    private final GestorDespacho gestorDespacho;
    private JTextField txtMinimoGratis;
    private JTextField txtRegion;
    private JTextField txtCosto;
    private JTextField txtDias;
    private DefaultListModel<String> modeloLista;
    private JList<String> listaVisual;

    public DialogoConfigurarTarifas(Frame parent, GestorDespacho gestorDespacho) {
        super(parent, "Configurar Tarifas de Despacho", true);
        this.gestorDespacho = gestorDespacho;

        setSize(500, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void mostrar() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        JPanel cabecera = new JPanel();
        cabecera.setBackground(NAVY);
        cabecera.setBorder(new EmptyBorder(15, 0, 15, 0));
        JLabel titulo = new JLabel("🚚 Configurar Tarifas de Envío");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);
        cabecera.add(titulo);
        add(cabecera, BorderLayout.NORTH);

        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(15, 25, 15, 25));

        // 1. Monto mínimo envío gratis
        PanelRedondeado panelGratis = new PanelRedondeado(15);
        panelGratis.setBackground(Color.WHITE);
        panelGratis.setLayout(new BorderLayout(10, 0));
        panelGratis.setBorder(new EmptyBorder(12, 15, 12, 15));
        panelGratis.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));

        JLabel lblGratis = new JLabel("Envío gratis sobre ($):");
        lblGratis.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblGratis.setForeground(NAVY);

        txtMinimoGratis = new JTextField(String.format("%.0f", gestorDespacho.getMontoMinimoEnvioGratis()));
        JButton btnGuardarMin = crearBoton("Guardar", AZUL);
        btnGuardarMin.addActionListener(e -> {
            try {
                double monto = Double.parseDouble(txtMinimoGratis.getText().trim());
                if (monto < 0) throw new NumberFormatException();
                gestorDespacho.setMontoMinimoEnvioGratis(monto);
                JOptionPane.showMessageDialog(this, "Monto mínimo actualizado.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese un monto válido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panelGratis.add(lblGratis, BorderLayout.WEST);
        panelGratis.add(txtMinimoGratis, BorderLayout.CENTER);
        panelGratis.add(btnGuardarMin, BorderLayout.EAST);

        // 2. Formulario región
        PanelRedondeado panelForm = new PanelRedondeado(15);
        panelForm.setBackground(Color.WHITE);
        panelForm.setLayout(new GridLayout(4, 2, 8, 10));
        panelForm.setBorder(new EmptyBorder(15, 15, 15, 15));

        txtRegion = new JTextField();
        txtCosto = new JTextField();
        txtDias = new JTextField();

        panelForm.add(new JLabel("Región / Zona:"));
        panelForm.add(txtRegion);
        panelForm.add(new JLabel("Costo Envío ($):"));
        panelForm.add(txtCosto);
        panelForm.add(new JLabel("Días hábiles:"));
        panelForm.add(txtDias);

        JButton btnEliminar = crearBoton("Eliminar Selección", new Color(215, 60, 70));
        JButton btnGuardar = crearBoton("Guardar Región", new Color(46, 139, 87));

        btnGuardar.addActionListener(e -> guardarRegion());
        btnEliminar.addActionListener(e -> eliminarRegion());

        panelForm.add(btnEliminar);
        panelForm.add(btnGuardar);

        // 3. Lista de tarifas vigentes
        modeloLista = new DefaultListModel<>();
        listaVisual = new JList<>(modeloLista);
        listaVisual.setFont(new Font("SansSerif", Font.PLAIN, 13));
        listaVisual.addListSelectionListener(e -> {
            int idx = listaVisual.getSelectedIndex();
            if (idx >= 0 && idx < gestorDespacho.getListaTarifas().size()) {
                TarifaEnvio t = gestorDespacho.getListaTarifas().get(idx);
                txtRegion.setText(t.getRegion());
                txtCosto.setText(String.format("%.0f", t.getCostoEnvio()));
                txtDias.setText(String.valueOf(t.getDiasEstimados()));
            }
        });

        recargarLista();

        JScrollPane scroll = new JScrollPane(listaVisual);
        scroll.setPreferredSize(new Dimension(0, 150));
        scroll.setBorder(BorderFactory.createTitledBorder("Tarifas vigentes"));

        centro.add(panelGratis);
        centro.add(Box.createRigidArea(new Dimension(0, 12)));
        centro.add(panelForm);
        centro.add(Box.createRigidArea(new Dimension(0, 12)));
        centro.add(scroll);
        add(centro, BorderLayout.CENTER);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 10));
        sur.setOpaque(false);
        JButton btnCerrar = crearBoton("Cerrar", new Color(149, 165, 166));
        btnCerrar.addActionListener(e -> dispose());
        sur.add(btnCerrar);
        add(sur, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void guardarRegion() {
        String region = txtRegion.getText().trim();
        if (region.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre de la región.");
            return;
        }
        try {
            double costo = Double.parseDouble(txtCosto.getText().trim());
            int dias = Integer.parseInt(txtDias.getText().trim());
            if (costo < 0 || dias <= 0) throw new NumberFormatException();

            gestorDespacho.guardarOActualizarTarifa(region, costo, dias);
            recargarLista();
            txtRegion.setText("");
            txtCosto.setText("");
            txtDias.setText("");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Costo y días deben ser números válidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarRegion() {
        int idx = listaVisual.getSelectedIndex();
        if (idx < 0) return;
        TarifaEnvio t = gestorDespacho.getListaTarifas().get(idx);
        gestorDespacho.eliminarTarifa(t.getRegion());
        recargarLista();
    }

    private void recargarLista() {
        modeloLista.clear();
        for (TarifaEnvio t : gestorDespacho.getListaTarifas()) {
            modeloLista.addElement(t.getRegion() + " — $" + String.format("%,.0f", t.getCostoEnvio()).replace(",", ".") + " (" + t.getDiasEstimados() + " días)");
        }
    }

    private JButton crearBoton(String texto, Color fondo) {
        JButton b = new JButton(texto);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(fondo);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}