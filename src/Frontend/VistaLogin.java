package Frontend;

import Backend.Sistema;
import Backend.Usuario;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VistaLogin extends JDialog {
    private Sistema sistema;
    private Usuario usuarioLogeado = null;

    private JTextField txtRut;
    private JPasswordField txtContrasena;

    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color GRIS_TEXTO = new Color(80, 90, 100);
    private static final Color GRIS_BORDE = new Color(200, 210, 220);

    public VistaLogin(JFrame parent, Sistema sistema) {
        super(parent, "Iniciar Sesión", true);
        this.sistema = sistema;

        setSize(400, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void mostrar() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // ==========================================
        // CABECERA
        // ==========================================
        JPanel panelNorte = new JPanel();
        panelNorte.setBackground(NAVY);
        panelNorte.setBorder(new EmptyBorder(20, 0, 20, 0));

        JLabel lblTitulo = new JLabel("Bienvenido a NexoMarket");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        panelNorte.add(lblTitulo);

        add(panelNorte, BorderLayout.NORTH);

        // ==========================================
        // FORMULARIO CENTRAL
        // ==========================================

        JPanel panelFormulario = new JPanel();
        panelFormulario.setLayout(new BoxLayout(panelFormulario, BoxLayout.Y_AXIS));
        panelFormulario.setBorder(new EmptyBorder(30, 40, 30, 40));
        panelFormulario.setBackground(Color.WHITE);

        // --- Campo RUT ---
        JLabel lblRut = new JLabel("RUT");
        lblRut.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRut.setForeground(GRIS_TEXTO);
        lblRut.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtRut = crearTextFieldOculto();

        // --- Campo Contraseña ---
        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPass.setForeground(GRIS_TEXTO);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtContrasena = new JPasswordField();
        estilarTextField(txtContrasena);

        // Ensamblaje vertical con espacios (RigidArea)
        panelFormulario.add(lblRut);
        panelFormulario.add(Box.createRigidArea(new Dimension(0, 8)));
        panelFormulario.add(txtRut);

        panelFormulario.add(Box.createRigidArea(new Dimension(0, 20))); // Espacio entre campos

        panelFormulario.add(lblPass);
        panelFormulario.add(Box.createRigidArea(new Dimension(0, 8)));
        panelFormulario.add(txtContrasena);

        add(panelFormulario, BorderLayout.CENTER);

        // ==========================================
        // ZONA DEL BOTÓN
        // ==========================================
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSur.setBackground(Color.WHITE);
        panelSur.setBorder(new EmptyBorder(0, 40, 35, 40)); // Margen inferior

        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.setFont(new Font("SansSerif", Font.BOLD, 15));
        btnIngresar.setBackground(AZUL);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnIngresar.setPreferredSize(new Dimension(300, 45));


        btnIngresar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btnIngresar.setBackground(new Color(25, 90, 180)); // Azul más oscuro
            }
            public void mouseExited(MouseEvent e) {
                btnIngresar.setBackground(AZUL);
            }
        });

        btnIngresar.addActionListener(e -> validarLogin());

        // Permitir iniciar sesión presionando Enter en la contraseña
        txtContrasena.addActionListener(e -> validarLogin());

        panelSur.add(btnIngresar);
        add(panelSur, BorderLayout.SOUTH);

        setVisible(true);
    }

    // ==========================================
    // MÉTODOS AUXILIARES Y LOGICA
    // ==========================================

    private JTextField crearTextFieldOculto() {
        JTextField txt = new JTextField();
        estilarTextField(txt);
        return txt;
    }

    private void estilarTextField(JTextField txt) {
        txt.setFont(new Font("SansSerif", Font.PLAIN, 15));
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txt.setPreferredSize(new Dimension(300, 40));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Borde redondeado suave para el textfield
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRIS_BORDE, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void validarLogin() {
        String rut = txtRut.getText().trim();
        String pass = new String(txtContrasena.getPassword());

        if (rut.isEmpty() || pass.isEmpty()) {
            mostrarError("Por favor, complete ambos campos.");
            return;
        }

        Usuario user = sistema.iniciarSesion(rut, pass);

        if (user != null) {
            this.usuarioLogeado = user;
            dispose();
        } else {
            mostrarError("RUT o contraseña incorrectos.\n(Admin: 11111111-1 / admin123)\n(Cliente: 22222222-2 / cliente123)");
            txtContrasena.setText("");
            txtContrasena.requestFocus();
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Inicio de Sesión", JOptionPane.WARNING_MESSAGE);
    }

    public Usuario getUsuarioLogeado() {
        return usuarioLogeado;
    }
}