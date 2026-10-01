package Frontend;

import Backend.Sistema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogoRegistroAdmin extends JDialog {
    private Sistema sistema;

    private JTextField txtRut, txtNombre, txtCorreo, txtCodigoEmpleado;
    private JPasswordField txtContrasena;

    // Colores corporativos
    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color AZUL_HOVER = new Color(25, 90, 180);
    private static final Color GRIS_BORDE = new Color(210, 220, 230);

    public DialogoRegistroAdmin(JFrame parent, Sistema sistema) {
        super(parent, "Registrar Nuevo Administrador", true);
        this.sistema = sistema;

        setSize(480, 560); // Un poco más bajo que el de cliente porque tiene menos campos
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
        panelCabecera.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel lblTitulo = new JLabel("🛡️ Registro de Administrador");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        panelCabecera.add(lblTitulo);

        add(panelCabecera, BorderLayout.NORTH);

        // ==========================================
        // FORMULARIO
        // ==========================================
        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setOpaque(false);
        panelContenedor.setBorder(new EmptyBorder(25, 25, 25, 25));

        PanelRedondeado tarjetaFormulario = new PanelRedondeado(20);
        tarjetaFormulario.setBackground(Color.WHITE);
        // Usamos GridLayout de 5 filas y 2 columnas
        tarjetaFormulario.setLayout(new GridLayout(5, 2, 10, 20));
        tarjetaFormulario.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Instanciamos los campos
        txtRut = crearTextField();
        txtNombre = crearTextField();
        txtCorreo = crearTextField();
        txtCodigoEmpleado = crearTextField();
        txtContrasena = new JPasswordField();
        estilarTextField(txtContrasena);

        // Agregamos pares de Etiqueta - Campo
        agregarCampo(tarjetaFormulario, "RUT:", txtRut);
        agregarCampo(tarjetaFormulario, "Nombre Completo:", txtNombre);
        agregarCampo(tarjetaFormulario, "Correo Electrónico:", txtCorreo);
        agregarCampo(tarjetaFormulario, "Contraseña:", txtContrasena);
        agregarCampo(tarjetaFormulario, "Código Empleado:", txtCodigoEmpleado);

        panelContenedor.add(tarjetaFormulario, BorderLayout.CENTER);
        add(panelContenedor, BorderLayout.CENTER);

        // ==========================================
        // BOTONES SUR
        // ==========================================
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(0, 0, 30, 0));

        JButton btnCancelar = crearBotonSecundario("Cancelar");
        JButton btnRegistrar = crearBotonPrincipal("Registrar Admin");

        btnCancelar.addActionListener(e -> dispose());
        btnRegistrar.addActionListener(e -> validarYRegistrar());

        panelBotones.add(btnCancelar);
        panelBotones.add(btnRegistrar);

        add(panelBotones, BorderLayout.SOUTH);

        setVisible(true);
    }

    // --- LÓGICA DE REGISTRO ---
    private void validarYRegistrar() {
        String rut = txtRut.getText().trim();
        String nombre = txtNombre.getText().trim();
        String correo = txtCorreo.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());
        String codigo = txtCodigoEmpleado.getText().trim();

        if (rut.isEmpty() || nombre.isEmpty() || correo.isEmpty() ||
                contrasena.isEmpty() || codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Llama al método que preparamos en el backend en el paso anterior
        boolean exito = sistema.registrarAdministrador(rut, nombre, contrasena, correo, codigo);

        if (exito) {
            JOptionPane.showMessageDialog(this, "¡Administrador registrado exitosamente en el sistema!", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "El RUT ingresado ya se encuentra registrado.", "Error de Registro", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- MÉTODOS DE DISEÑO ---
    private void agregarCampo(JPanel panel, String textoLabel, JComponent campo) {
        JLabel label = new JLabel(textoLabel);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(100, 110, 125));
        panel.add(label);
        panel.add(campo);
    }

    private JTextField crearTextField() {
        JTextField txt = new JTextField();
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
        btn.setPreferredSize(new Dimension(170, 40));
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
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(210, 220, 230)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(230, 235, 240)); }
        });
        return btn;
    }
}