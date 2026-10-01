package Frontend;

import Backend.Sistema;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogoRegistroCliente extends JDialog {
    private Sistema sistema;

    private JTextField txtRut, txtNombre, txtCorreo, txtTelefono, txtDireccion;
    private JPasswordField txtContrasena;

    // Colores corporativos
    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color AZUL_HOVER = new Color(25, 90, 180);
    private static final Color GRIS_BORDE = new Color(210, 220, 230);

    public DialogoRegistroCliente(JDialog parent, Sistema sistema) {
        super(parent, "Registro de Nuevo Cliente", true);
        this.sistema = sistema;

        setSize(480, 620);
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

        JLabel lblTitulo = new JLabel("📝 Crear Cuenta de Cliente");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        panelCabecera.add(lblTitulo);

        add(panelCabecera, BorderLayout.NORTH);

        // ==========================================
        // FORMULARIO (En 2 columnas)
        // ==========================================
        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setOpaque(false);
        panelContenedor.setBorder(new EmptyBorder(25, 25, 25, 25));

        PanelRedondeado tarjetaFormulario = new PanelRedondeado(20);
        tarjetaFormulario.setBackground(Color.WHITE);
        // Usamos GridLayout de 6 filas y 2 columnas
        tarjetaFormulario.setLayout(new GridLayout(6, 2, 10, 20));
        tarjetaFormulario.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Instanciamos los campos
        txtRut = crearTextField();
        txtNombre = crearTextField();
        txtCorreo = crearTextField();
        txtTelefono = crearTextField();
        txtDireccion = crearTextField();
        txtContrasena = new JPasswordField();
        estilarTextField(txtContrasena);

        // Agregamos pares de Etiqueta - Campo
        agregarCampo(tarjetaFormulario, "RUT:", txtRut);
        agregarCampo(tarjetaFormulario, "Nombre Completo:", txtNombre);
        agregarCampo(tarjetaFormulario, "Correo Electrónico:", txtCorreo);
        agregarCampo(tarjetaFormulario, "Contraseña:", txtContrasena);
        agregarCampo(tarjetaFormulario, "Teléfono:", txtTelefono);
        agregarCampo(tarjetaFormulario, "Dirección:", txtDireccion);

        panelContenedor.add(tarjetaFormulario, BorderLayout.CENTER);
        add(panelContenedor, BorderLayout.CENTER);

        // ==========================================
        // BOTONES SUR
        // ==========================================
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panelBotones.setOpaque(false);
        panelBotones.setBorder(new EmptyBorder(0, 0, 30, 0));

        JButton btnCancelar = crearBotonSecundario("Cancelar");
        JButton btnRegistrar = crearBotonPrincipal("Registrar Cuenta");

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
        String telefono = txtTelefono.getText().trim();
        String direccion = txtDireccion.getText().trim();

        if (rut.isEmpty() || nombre.isEmpty() || correo.isEmpty() ||
                contrasena.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Llama al backend para registrar al cliente
        boolean exito = sistema.registrarCliente(rut, nombre, contrasena, correo, telefono, direccion);

        if (exito) {
            JOptionPane.showMessageDialog(this, "¡Cuenta creada exitosamente!\nYa puedes iniciar sesión.", "Registro Exitoso", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "El RUT ingresado ya se encuentra registrado en el sistema.", "Error de Registro", JOptionPane.ERROR_MESSAGE);
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