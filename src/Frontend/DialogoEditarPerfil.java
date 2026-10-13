package Frontend;

import Backend.Cliente;
import Backend.Sistema;
import Backend.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogoEditarPerfil extends JDialog {
    private final Sistema sistema;
    private final Usuario usuario;
    private boolean datosActualizados = false;

    private JTextField txtNombre;
    private JTextField txtCorreo;
    private JTextField txtTelefono;
    private JTextField txtDireccion;
    private JPasswordField txtNuevaContrasena;

    // Colores corporativos
    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color AZUL_HOVER = new Color(25, 90, 180);
    private static final Color GRIS_BORDE = new Color(210, 220, 230);

    public DialogoEditarPerfil(JDialog parent, Sistema sistema, Usuario usuario) {
        super(parent, "Editar Mis Datos", true);
        this.sistema = sistema;
        this.usuario = usuario;

        boolean esCliente = usuario instanceof Cliente;
        setSize(460, esCliente ? 560 : 440);
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

        JLabel lblTitulo = new JLabel("✏️ Editar Mis Datos");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        panelCabecera.add(lblTitulo);

        add(panelCabecera, BorderLayout.NORTH);

        // ==========================================
        // FORMULARIO
        // ==========================================
        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setOpaque(false);
        panelContenedor.setBorder(new EmptyBorder(20, 25, 20, 25));

        boolean esCliente = usuario instanceof Cliente;
        int filas = esCliente ? 5 : 3;

        PanelRedondeado tarjetaFormulario = new PanelRedondeado(20);
        tarjetaFormulario.setBackground(Color.WHITE);
        tarjetaFormulario.setLayout(new GridLayout(filas, 2, 10, 18));
        tarjetaFormulario.setBorder(new EmptyBorder(20, 20, 20, 20));

        txtNombre = crearTextField(usuario.getNombre());
        txtCorreo = crearTextField(usuario.getCorreo());
        txtNuevaContrasena = new JPasswordField();
        estilarTextField(txtNuevaContrasena);
        txtNuevaContrasena.setToolTipText("Déjelo en blanco si no desea cambiar su contraseña actual");

        agregarCampo(tarjetaFormulario, "Nombre Completo:", txtNombre);
        agregarCampo(tarjetaFormulario, "Correo Electrónico:", txtCorreo);

        if (esCliente) {
            Cliente cliente = (Cliente) usuario;
            txtTelefono = crearTextField(cliente.getTelefono());
            txtDireccion = crearTextField(cliente.getDireccion());

            agregarCampo(tarjetaFormulario, "Teléfono:", txtTelefono);
            agregarCampo(tarjetaFormulario, "Dirección:", txtDireccion);
        }

        agregarCampo(tarjetaFormulario, "Nueva Clave (opcional):", txtNuevaContrasena);

        panelContenedor.add(tarjetaFormulario, BorderLayout.CENTER);
        add(panelContenedor, BorderLayout.CENTER);

        // ==========================================
        // BOTONES SUR
        // ==========================================
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(0, 0, 25, 0));

        JButton btnCancelar = crearBotonSecundario("Cancelar");
        JButton btnGuardar = crearBotonPrincipal("Guardar Cambios");

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> validarYGuardar());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnGuardar);

        add(panelBotones, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void validarYGuardar() {
        String nuevoNombre = txtNombre.getText().trim();
        String nuevoCorreo = txtCorreo.getText().trim();
        String nuevaClave = new String(txtNuevaContrasena.getPassword()).trim();

        if (nuevoNombre.isEmpty() || nuevoCorreo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El nombre y el correo no pueden estar vacíos.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!nuevoCorreo.contains("@") || !nuevoCorreo.contains(".")) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un correo electrónico válido.", "Correo Inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Reemplazamos comas por punto y coma para proteger la estructura del CSV
        nuevoNombre = nuevoNombre.replace(",", ";");
        nuevoCorreo = nuevoCorreo.replace(",", ";");

        if (usuario instanceof Cliente) {
            String nuevoTel = txtTelefono.getText().trim().replace(",", ";");
            String nuevaDir = txtDireccion.getText().trim().replace(",", ";");

            if (nuevoTel.isEmpty() || nuevaDir.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El teléfono y la dirección son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Cliente cliente = (Cliente) usuario;
            cliente.setTelefono(nuevoTel);
            cliente.setDireccion(nuevaDir);
        }

        usuario.setNombre(nuevoNombre);
        usuario.setCorreo(nuevoCorreo);

        if (!nuevaClave.isEmpty()) {
            usuario.setContrasena(nuevaClave.replace(",", ";"));
        }

        // Guardamos los cambios de forma persistente en usuarios.csv
        sistema.guardarUsuariosEnCSV();
        datosActualizados = true;

        JOptionPane.showMessageDialog(this, "¡Tus datos han sido actualizados exitosamente!", "Perfil Actualizado", JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    public boolean isDatosActualizados() {
        return datosActualizados;
    }

    // --- MÉTODOS AUXILIARES DE DISEÑO ---
    private void agregarCampo(JPanel panel, String textoLabel, JComponent campo) {
        JLabel label = new JLabel(textoLabel);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(100, 110, 125));
        panel.add(label);
        panel.add(campo);
    }

    private JTextField crearTextField(String valorInicial) {
        JTextField txt = new JTextField(valorInicial);
        estilarTextField(txt);
        return txt;
    }

    private void estilarTextField(JTextField txt) {
        txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txt.setForeground(NAVY);
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRIS_BORDE, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private JButton crearBotonPrincipal(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(AZUL);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(165, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(AZUL_HOVER); }
            public void mouseExited(MouseEvent e) { btn.setBackground(AZUL); }
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
        btn.setPreferredSize(new Dimension(115, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(210, 220, 230)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(230, 235, 240)); }
        });
        return btn;
    }
}