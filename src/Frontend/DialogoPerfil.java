package Frontend;

import Backend.Cliente;
import Backend.Inventario;
import Backend.Sistema;
import Backend.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogoPerfil extends JDialog {
    private Sistema sistema;
    private Inventario inventario;
    private Usuario usuario;
    private boolean cerroSesion = false;

    // Colores corporativos basados en tu aplicación
    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color AZUL_HOVER = new Color(25, 90, 180);
    private static final Color GRIS_CLARO = new Color(230, 235, 240);
    private static final Color ROJO_BOTON = new Color(231, 76, 60);
    private static final Color ROJO_HOVER = new Color(192, 57, 43);

    // Constructor original (se mantiene por compatibilidad)
    public DialogoPerfil(JFrame parent, Sistema sistema, Usuario usuario) {
        this(parent, sistema, null, usuario);
    }

    // Nuevo constructor con Inventario para consultar "Mis Compras"
    public DialogoPerfil(JFrame parent, Sistema sistema, Inventario inventario, Usuario usuario) {
        super(parent, "Mi Perfil", true);
        this.sistema = sistema;
        this.inventario = inventario;
        this.usuario = usuario;

        setSize(430, 650);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void mostrar() {
        construirInterfaz();
        setVisible(true);
    }

    private void construirInterfaz() {
        getContentPane().removeAll();
        getContentPane().setBackground(FONDO);
        setLayout(new BorderLayout());

        // Contenedor principal con padding
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setOpaque(false);
        panelPrincipal.setBorder(new EmptyBorder(25, 30, 25, 30));

        // ==========================================
        // 1. ZONA DEL AVATAR Y SALUDO
        // ==========================================
        PanelRedondeado avatarCirculo = new PanelRedondeado(70);
        avatarCirculo.setBackground(NAVY);
        avatarCirculo.setPreferredSize(new Dimension(70, 70));
        avatarCirculo.setMaximumSize(new Dimension(70, 70));
        avatarCirculo.setLayout(new BorderLayout());
        avatarCirculo.setAlignmentX(Component.CENTER_ALIGNMENT);

        String inicial = usuario.getNombre().isEmpty() ? "U" : usuario.getNombre().substring(0, 1).toUpperCase();
        JLabel lblInicial = new JLabel(inicial);
        lblInicial.setFont(new Font("SansSerif", Font.BOLD, 32));
        lblInicial.setForeground(Color.WHITE);
        lblInicial.setHorizontalAlignment(SwingConstants.CENTER);
        avatarCirculo.add(lblInicial, BorderLayout.CENTER);

        // Nombre y Rol
        JLabel lblNombre = new JLabel(usuario.getNombre());
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblNombre.setForeground(NAVY);
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblRol = new JLabel(usuario.getTipoUsuario().toUpperCase());
        lblRol.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblRol.setForeground(new Color(120, 130, 140));
        lblRol.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelPrincipal.add(avatarCirculo);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 12)));
        panelPrincipal.add(lblNombre);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 4)));
        panelPrincipal.add(lblRol);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 20)));

        // ==========================================
        // 2. TARJETA DE INFORMACIÓN (Panel Redondeado)
        // ==========================================
        PanelRedondeado tarjetaDatos = new PanelRedondeado(20);
        tarjetaDatos.setBackground(Color.WHITE);
        tarjetaDatos.setLayout(new BoxLayout(tarjetaDatos, BoxLayout.Y_AXIS));
        tarjetaDatos.setBorder(new EmptyBorder(12, 20, 12, 20));
        tarjetaDatos.setAlignmentX(Component.CENTER_ALIGNMENT);

        agregarFilaATarjeta(tarjetaDatos, "✉", "Correo", usuario.getCorreo(), true);
        agregarFilaATarjeta(tarjetaDatos, "👤", "RUT", usuario.getRut(), usuario instanceof Cliente);

        if (usuario instanceof Cliente) {
            Cliente cliente = (Cliente) usuario;
            agregarFilaATarjeta(tarjetaDatos, "📞", "Teléfono", cliente.getTelefono(), true);
            agregarFilaATarjeta(tarjetaDatos, "📍", "Dirección", cliente.getDireccion(), false);
        }

        panelPrincipal.add(tarjetaDatos);
        panelPrincipal.add(Box.createVerticalGlue());

        // ==========================================
        // 3. BOTONES DE AUTOGESTIÓN Y CIERRE DE SESIÓN
        // ==========================================

        // Botón: Editar Mis Datos
        JButton btnEditarDatos = crearBotonAccion("✏️ Editar Mis Datos", AZUL, AZUL_HOVER);
        btnEditarDatos.addActionListener(e -> {
            DialogoEditarPerfil dialogoEditar = new DialogoEditarPerfil(this, sistema, usuario);
            dialogoEditar.mostrar();
            if (dialogoEditar.isDatosActualizados()) {
                // Redibujamos la ventana para reflejar el nuevo nombre, correo, etc. al instante
                construirInterfaz();
            }
        });
        panelPrincipal.add(btnEditarDatos);
        panelPrincipal.add(Box.createRigidArea(new Dimension(0, 10)));

        // Botón: Mis Compras (Solo si es Cliente y se pasó el inventario)
        if (usuario instanceof Cliente && inventario != null) {
            JButton btnMisCompras = crearBotonAccion("📦 Ver Mis Compras", NAVY, new Color(35, 65, 110));
            btnMisCompras.addActionListener(e -> {
                DialogoMisCompras dialogoCompras = new DialogoMisCompras(this, inventario, usuario);
                dialogoCompras.mostrar();
            });
            panelPrincipal.add(btnMisCompras);
            panelPrincipal.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Botón: Cerrar Sesión
        JButton btnCerrarSesion = crearBotonAccion("Cerrar Sesión", ROJO_BOTON, ROJO_HOVER);
        btnCerrarSesion.addActionListener(e -> {
            sistema.cerrarSesion();
            cerroSesion = true;
            dispose();
        });

        panelPrincipal.add(btnCerrarSesion);
        add(panelPrincipal, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // ==========================================
    // MÉTODOS AUXILIARES DE DISEÑO
    // ==========================================

    private JButton crearBotonAccion(String texto, Color colorBase, Color colorHover) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(colorBase);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(colorHover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(colorBase);
            }
        });
        return btn;
    }

    private void agregarFilaATarjeta(JPanel tarjeta, String icono, String etiqueta, String valor, boolean incluirSeparador) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.setBorder(new EmptyBorder(10, 0, 10, 0));

        JLabel lblIzquierda = new JLabel(icono + "  " + etiqueta);
        lblIzquierda.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblIzquierda.setForeground(new Color(100, 110, 125));

        String valorMostrar = valor.length() > 22 ? valor.substring(0, 20) + "..." : valor;
        JLabel lblDerecha = new JLabel(valorMostrar);
        lblDerecha.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblDerecha.setForeground(NAVY);
        lblDerecha.setHorizontalAlignment(SwingConstants.RIGHT);
        lblDerecha.setToolTipText(valor);

        fila.add(lblIzquierda, BorderLayout.WEST);
        fila.add(lblDerecha, BorderLayout.CENTER);

        tarjeta.add(fila);

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