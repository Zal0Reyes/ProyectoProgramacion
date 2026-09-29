package Frontend;

import Backend.Cliente;
import Backend.Sistema;
import Backend.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogoPerfil extends JDialog {
    private Sistema sistema;
    private Usuario usuario;
    private boolean cerroSesion = false;

    // Colores corporativos basados en tu aplicación
    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color GRIS_CLARO = new Color(230, 235, 240);
    private static final Color ROJO_BOTON = new Color(231, 76, 60);
    private static final Color ROJO_HOVER = new Color(192, 57, 43);

    public DialogoPerfil(JFrame parent, Sistema sistema, Usuario usuario) {
        super(parent, "Mi Perfil", true);
        this.sistema = sistema;
        this.usuario = usuario;

        setSize(420, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void mostrar() {
        // Fondo general de la ventana
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        // Contenedor principal con padding
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setOpaque(false);
        panelPrincipal.setBorder(new EmptyBorder(30, 30, 30, 30));

        // ==========================================
        // 1. ZONA DEL AVATAR Y SALUDO
        // ==========================================

        // Círculo del avatar (usando PanelRedondeado con radio alto)
        PanelRedondeado avatarCiculo = new PanelRedondeado(70);
        avatarCiculo.setBackground(NAVY);
        avatarCiculo.setPreferredSize(new Dimension(70, 70));
        avatarCiculo.setMaximumSize(new Dimension(70, 70));
        avatarCiculo.setLayout(new BorderLayout());
        avatarCiculo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblInicial = new JLabel(usuario.getNombre().substring(0, 1).toUpperCase());
        lblInicial.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblInicial.setForeground(Color.WHITE);
        lblInicial.setHorizontalAlignment(SwingConstants.CENTER);
        avatarCiculo.add(lblInicial, BorderLayout.CENTER);

        // Nombre y Rol
        JLabel lblNombre = new JLabel(usuario.getNombre());
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblNombre.setForeground(NAVY);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRol = new JLabel(usuario.getTipoUsuario().toUpperCase());
        lblRol.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblRol.setForeground(new Color(120, 130, 140));
        lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelPrincipal.add(avatarCiculo);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 15)));
        panelPrincipal.add(lblNombre);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 5)));
        panelPrincipal.add(lblRol);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 25)));

        // ==========================================
        // 2. TARJETA DE INFORMACIÓN (Panel Redondeado)
        // ==========================================

        PanelRedondeado tarjetaDatos = new PanelRedondeado(20);
        tarjetaDatos.setBackground(Color.WHITE);
        tarjetaDatos.setLayout(new BoxLayout(tarjetaDatos, BoxLayout.Y_AXIS));
        tarjetaDatos.setBorder(new EmptyBorder(15, 20, 15, 20));
        tarjetaDatos.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Agregamos los datos con formato de lista y separadores
        agregarFilaATarjeta(tarjetaDatos, "✉", "Correo", usuario.getCorreo(), true);
        agregarFilaATarjeta(tarjetaDatos, "️👤", "RUT", usuario.getRut(), usuario instanceof Cliente);

        // Si es cliente, agregamos los extras
        if (usuario instanceof Cliente) {
            Cliente cliente = (Cliente) usuario;
            agregarFilaATarjeta(tarjetaDatos, "📞", "Teléfono", cliente.getTelefono(), true);
            agregarFilaATarjeta(tarjetaDatos, "📍", "Dirección", cliente.getDireccion(), false);
        }

        panelPrincipal.add(tarjetaDatos);
        panelPrincipal.add(Box.createVerticalGlue()); // Empuja el botón hacia abajo

        // ==========================================
        // 3. BOTÓN CERRAR SESIÓN
        // ==========================================

        JButton btnCerrarSesion = new JButton("Cerrar Sesión");
        btnCerrarSesion.setFont(new Font("SansSerif", Font.BOLD, 15));
        btnCerrarSesion.setBackground(ROJO_BOTON);
        btnCerrarSesion.setForeground(Color.WHITE);
        btnCerrarSesion.setFocusPainted(false);
        btnCerrarSesion.setBorderPainted(false);
        btnCerrarSesion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrarSesion.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCerrarSesion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        // Efecto visual al pasar el cursor (Hover)
        btnCerrarSesion.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnCerrarSesion.setBackground(ROJO_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btnCerrarSesion.setBackground(ROJO_BOTON);
            }
        });

        btnCerrarSesion.addActionListener(e -> {
            sistema.cerrarSesion();
            cerroSesion = true;
            dispose();
        });

        panelPrincipal.add(btnCerrarSesion);
        add(panelPrincipal, BorderLayout.CENTER);

        setVisible(true);
    }

    // ==========================================
    // MÉTODOS AUXILIARES DE DISEÑO
    // ==========================================

    private void agregarFilaATarjeta(JPanel tarjeta, String icono, String etiqueta, String valor, boolean incluirSeparador) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Lado izquierdo (Icono + Etiqueta)
        JLabel lblIzquierda = new JLabel(icono + "  " + etiqueta);
        lblIzquierda.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblIzquierda.setForeground(new Color(100, 110, 125));

        // Lado derecho (Valor)
        // Acortamos el texto si es muy largo para que no rompa el diseño
        String valorMostrar = valor.length() > 22 ? valor.substring(0, 20) + "..." : valor;
        JLabel lblDerecha = new JLabel(valorMostrar);
        lblDerecha.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDerecha.setForeground(NAVY);
        lblDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDerecha.setToolTipText(valor); // Muestra el texto completo al pasar el mouse

        fila.add(lblIzquierda, BorderLayout.WEST);
        fila.add(lblDerecha, BorderLayout.CENTER); // Center para que tome el espacio sobrante hacia la derecha

        tarjeta.add(fila);

        // Agrega una línea sutil entre los datos
        if (incluirSeparador) {
            JSeparator separador = new JSeparator();
            separador.setForeground(GRIS_CLARO);
            separador.setBackground(GRIS_CLARO);
            separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            tarjeta.add(separador);
        }
    }

    public boolean isCerroSesion() {
        return cerroSesion;
    }
}