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

    // Colores corporativos
    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color AZUL_HOVER = new Color(25, 90, 180);
    private static final Color GRIS_TEXTO = new Color(100, 110, 125);
    private static final Color GRIS_BORDE = new Color(210, 220, 230);

    public VistaLogin(JFrame parent, Sistema sistema) {
        super(parent, "Iniciar Sesión", true);
        this.sistema = sistema;

        setSize(420, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void mostrar() {
        // Fondo general de la ventana
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setOpaque(false);
        panelPrincipal.setBorder(new EmptyBorder(30, 35, 30, 35));

        // ==========================================
        // 1. ZONA DEL ICONO Y BIENVENIDA
        // ==========================================

        // Círculo del icono
        PanelRedondeado iconoCirculo = new PanelRedondeado(65);
        iconoCirculo.setBackground(NAVY);
        iconoCirculo.setPreferredSize(new Dimension(65, 65));
        iconoCirculo.setMaximumSize(new Dimension(65, 65));
        iconoCirculo.setLayout(new BorderLayout());
        iconoCirculo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblIcono = new JLabel("👋");
        lblIcono.setForeground(Color.WHITE);
        lblIcono.setFont(new Font("SansSerif", Font.PLAIN, 30));
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        iconoCirculo.add(lblIcono, BorderLayout.CENTER);

        // Títulos
        JLabel lblTitulo = new JLabel("¡Hola de nuevo!");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTitulo.setForeground(NAVY);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("Inicia sesión en NexoMarket");
        lblSubtitulo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSubtitulo.setForeground(GRIS_TEXTO);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelPrincipal.add(iconoCirculo);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        panelPrincipal.add(lblTitulo);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 5)));
        panelPrincipal.add(lblSubtitulo);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 25)));

        // ==========================================
        // 2. TARJETA DE FORMULARIO
        // ==========================================

        PanelRedondeado tarjetaFormulario = new PanelRedondeado(20);
        tarjetaFormulario.setBackground(Color.WHITE);
        tarjetaFormulario.setLayout(new BoxLayout(tarjetaFormulario, BoxLayout.Y_AXIS));
        tarjetaFormulario.setBorder(new EmptyBorder(25, 25, 25, 25));
        tarjetaFormulario.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Campo RUT ---
        JLabel lblRut = new JLabel("RUT");
        lblRut.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblRut.setForeground(GRIS_TEXTO);
        lblRut.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtRut = new JTextField();
        estilarTextField(txtRut);

        // --- Campo Contraseña ---
        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblPass.setForeground(GRIS_TEXTO);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtContrasena = new JPasswordField();
        estilarTextField(txtContrasena);

        // --- Botón Ingresar ---
        JButton btnIngresar = new JButton("Ingresar");
        btnIngresar.setFont(new Font("SansSerif", Font.BOLD, 15));
        btnIngresar.setBackground(AZUL);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnIngresar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        btnIngresar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnIngresar.setBackground(AZUL_HOVER); }
            public void mouseExited(MouseEvent e) { btnIngresar.setBackground(AZUL); }
        });

        btnIngresar.addActionListener(e -> validarLogin());
        txtContrasena.addActionListener(e -> validarLogin());

        // --- NUEVO: Sección de Registro ---
        JPanel panelRegistro = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panelRegistro.setOpaque(false);
        panelRegistro.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPregunta = new JLabel("¿No tienes cuenta?");
        lblPregunta.setFont(new Font("SansSerif", Font.PLAIN, 13));
        lblPregunta.setForeground(GRIS_TEXTO);

        JButton btnRegistrar = new JButton("Regístrate aquí");
        btnRegistrar.setFont(new Font("SansSerif", Font.BOLD, 13));
        btnRegistrar.setForeground(AZUL);
        btnRegistrar.setContentAreaFilled(false); // Fondo transparente
        btnRegistrar.setBorderPainted(false);
        btnRegistrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegistrar.setMargin(new Insets(0, 0, 0, 0)); // Quitar márgenes extra

        btnRegistrar.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnRegistrar.setForeground(NAVY); }
            public void mouseExited(MouseEvent e) { btnRegistrar.setForeground(AZUL); }
        });

        // Acción para abrir el nuevo diálogo de registro
        btnRegistrar.addActionListener(e -> {
            DialogoRegistroCliente dialogoRegistro = new DialogoRegistroCliente(this, sistema);
            dialogoRegistro.mostrar();
        });

        panelRegistro.add(lblPregunta);
        panelRegistro.add(btnRegistrar);

        // Ensamblaje de la tarjeta (NUEVO ORDEN)
        tarjetaFormulario.add(lblRut);
        tarjetaFormulario.add(Box.createRigidArea(new Dimension(0, 8)));
        tarjetaFormulario.add(txtRut);
        tarjetaFormulario.add(Box.createRigidArea(new Dimension(0, 20)));

        tarjetaFormulario.add(lblPass);
        tarjetaFormulario.add(Box.createRigidArea(new Dimension(0, 8)));
        tarjetaFormulario.add(txtContrasena);
        tarjetaFormulario.add(Box.createRigidArea(new Dimension(0, 25)));

        tarjetaFormulario.add(btnIngresar);
        tarjetaFormulario.add(Box.createRigidArea(new Dimension(0, 15))); // Espacio antes del registro

        // Línea separadora sutil
        JSeparator separador = new JSeparator();
        separador.setForeground(GRIS_BORDE);
        separador.setBackground(GRIS_BORDE);
        separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        tarjetaFormulario.add(separador);
        tarjetaFormulario.add(Box.createRigidArea(new Dimension(0, 10)));

        tarjetaFormulario.add(panelRegistro); // Agregamos la sección de registro abajo

        panelPrincipal.add(tarjetaFormulario);

        add(panelPrincipal, BorderLayout.CENTER);

        // Ajustamos levemente el tamaño de la ventana para que quepa todo perfecto
        setSize(420, 600);
        setVisible(true);
    }

    // ==========================================
    // MÉTODOS AUXILIARES Y LOGICA
    // ==========================================

    private void estilarTextField(JTextField txt) {
        txt.setFont(new Font("SansSerif", Font.PLAIN, 15));
        txt.setForeground(NAVY);
        txt.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); // Más alto para que se vea moderno
        txt.setPreferredSize(new Dimension(300, 45));
        txt.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Borde redondeado suave para el textfield
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(GRIS_BORDE, 1, true),
                new EmptyBorder(5, 12, 5, 12)
        ));
    }

    private void validarLogin() {
        String rut = txtRut.getText().trim();
        String pass = new String(txtContrasena.getPassword());

        if (rut.isEmpty() || pass.isEmpty()) {
            mostrarError("Por favor, complete ambos campos.");
            return;
        }

        Usuario user = sistema.iniciarSesion(rut, pass); // Lógica intacta

        if (user != null) {
            this.usuarioLogeado = user;
            dispose();
        } else {
            mostrarError("RUT o contraseña incorrectos");
            txtContrasena.setText("");
            txtContrasena.requestFocus();
        }
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de Inicio de Sesión", JOptionPane.WARNING_MESSAGE);
    }

    public Usuario getUsuarioLogeado() {
        return usuarioLogeado; // Lógica intacta
    }
}