package Frontend;

import Backend.Inventario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogoVaciarInventario extends JDialog {
    private Inventario inventario;
    private boolean seRealizaronCambios = false;

    // Colores
    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color ROJO = new Color(231, 76, 60);
    private static final Color ROJO_HOVER = new Color(192, 57, 43);
    private static final Color GRIS_TEXTO = new Color(100, 110, 125);

    public DialogoVaciarInventario(JFrame parent, Inventario inventario) {
        super(parent, "Gestión Masiva de Inventario", true);
        this.inventario = inventario;

        setSize(460, 520);
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

        JLabel lblTitulo = new JLabel("🗑 Vaciar Inventario");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        panelCabecera.add(lblTitulo);

        add(panelCabecera, BorderLayout.NORTH);

        // ==========================================
        // CONTENEDOR CENTRAL (Tarjetas)
        // ==========================================
        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setOpaque(false);
        panelCentral.setBorder(new EmptyBorder(20, 25, 20, 25));

        // ------------------------------------------
        // TARJETA 1: ELIMINAR POR CATEGORÍA
        // ------------------------------------------
        PanelRedondeado tarjetaParcial = new PanelRedondeado(20);
        tarjetaParcial.setBackground(Color.WHITE);
        tarjetaParcial.setLayout(new BoxLayout(tarjetaParcial, BoxLayout.Y_AXIS));
        tarjetaParcial.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTituloParcial = new JLabel("Eliminar por Categoría");
        lblTituloParcial.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTituloParcial.setForeground(NAVY);
        lblTituloParcial.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubParcial = new JLabel("Borra todos los productos de una categoría.");
        lblSubParcial.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubParcial.setForeground(GRIS_TEXTO);
        lblSubParcial.setAlignmentX(Component.CENTER_ALIGNMENT);

        JComboBox<String> cbCategorias = new JComboBox<>(inventario.getCategoriasDisponibles());
        cbCategorias.setMaximumSize(new Dimension(250, 35));
        cbCategorias.setBackground(Color.WHITE);
        cbCategorias.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnEliminarCat = crearBotonAccion("Eliminar Categoría", AZUL, new Color(25, 90, 180));

        btnEliminarCat.addActionListener(e -> {
            if (cbCategorias.getSelectedItem() != null) {
                String categoriaSeleccionada = cbCategorias.getSelectedItem().toString();
                eliminarPorCategoriaSeguro(categoriaSeleccionada);
            }
        });

        tarjetaParcial.add(lblTituloParcial);
        tarjetaParcial.add(Box.createRigidArea(new Dimension(0, 5)));
        tarjetaParcial.add(lblSubParcial);
        tarjetaParcial.add(Box.createRigidArea(new Dimension(0, 15)));
        tarjetaParcial.add(cbCategorias);
        tarjetaParcial.add(Box.createRigidArea(new Dimension(0, 15)));
        tarjetaParcial.add(btnEliminarCat);

        // ------------------------------------------
        // TARJETA 2: VACIAR TODO
        // ------------------------------------------
        PanelRedondeado tarjetaTotal = new PanelRedondeado(20);
        tarjetaTotal.setBackground(new Color(253, 242, 240)); // Fondo rojizo muy suave
        // Borde rojo para indicar peligro
        tarjetaTotal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(245, 183, 177), 2),
                new EmptyBorder(20, 20, 20, 20)
        ));
        tarjetaTotal.setLayout(new BoxLayout(tarjetaTotal, BoxLayout.Y_AXIS));

        JLabel lblTituloTotal = new JLabel("⚠️ Zona de Peligro");
        lblTituloTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTituloTotal.setForeground(ROJO);
        lblTituloTotal.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubTotal = new JLabel("Borra TODO el inventario. No se puede deshacer.");
        lblSubTotal.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubTotal.setForeground(ROJO);
        lblSubTotal.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnVaciarTodo = crearBotonAccion("Vaciar Inventario Completo", ROJO, ROJO_HOVER);

        btnVaciarTodo.addActionListener(e -> vaciarInventarioSeguro());

        tarjetaTotal.add(lblTituloTotal);
        tarjetaTotal.add(Box.createRigidArea(new Dimension(0, 5)));
        tarjetaTotal.add(lblSubTotal);
        tarjetaTotal.add(Box.createRigidArea(new Dimension(0, 15)));
        tarjetaTotal.add(btnVaciarTodo);

        // Ensamblaje central
        panelCentral.add(tarjetaParcial);
        panelCentral.add(Box.createRigidArea(new Dimension(0, 20))); // Espacio entre tarjetas
        panelCentral.add(tarjetaTotal);

        add(panelCentral, BorderLayout.CENTER);

        // ==========================================
        // BOTÓN CERRAR (SUR)
        // ==========================================
        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 10));
        panelSur.setOpaque(false);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCerrar.setForeground(NAVY);
        btnCerrar.setBackground(new Color(230, 235, 240));
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setPreferredSize(new Dimension(100, 35));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());

        panelSur.add(btnCerrar);
        add(panelSur, BorderLayout.SOUTH);

        setVisible(true);
    }

    // ==========================================
    // LÓGICA CON CONFIRMACIONES DE SEGURIDAD
    // ==========================================

    private void eliminarPorCategoriaSeguro(String categoria) {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que deseas eliminar TODOS los productos de la categoría '" + categoria + "'?\nEsta acción no se puede recuperar.",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            int borrados = inventario.eliminarProductosPorCategoria(categoria);

            if (borrados > 0) {
                seRealizaronCambios = true;
                JOptionPane.showMessageDialog(this, "Se han eliminado " + borrados + " productos exitosamente.", "Operación Completada", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // Cerramos la ventana para refrescar la vista principal
            } else {
                JOptionPane.showMessageDialog(this, "No se encontraron productos en esta categoría.", "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void vaciarInventarioSeguro() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "ESTÁS A PUNTO DE VACIAR TODO EL INVENTARIO.\n¿Estás absolutamente seguro de continuar?",
                "⚠️ ADVERTENCIA CRÍTICA", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            inventario.vaciarInventarioCompleto();
            seRealizaronCambios = true;
            JOptionPane.showMessageDialog(this, "El inventario ha sido vaciado por completo.", "Operación Completada", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }

    // Retorna true si el usuario borró algo, para saber si hay que refrescar la tabla en la Tienda
    public boolean huboCambios() {
        return seRealizaronCambios;
    }

    // ==========================================
    // DISEÑO DE BOTONES
    // ==========================================
    private JButton crearBotonAccion(String texto, Color base, Color hover) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(base);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(250, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e) { btn.setBackground(base); }
        });

        return btn;
    }
}