package Frontend;

import Backend.Inventario;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogoAgregarProducto extends JDialog {
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtStock;
    private JComboBox<String> cbCategoria;

    private boolean guardadoExitoso = false;
    private Inventario inventario;

    public DialogoAgregarProducto(Frame parent, Inventario inventario) {
        super(parent, "Agregar Nuevo Producto", true);
        this.inventario = inventario;

        setSize(450, 380);
        setLocationRelativeTo(parent);
        setResizable(false); // Evita que se deforme la ventana
    }

    public void mostrar() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250)); // Mismo fondo que tu vista principal

        // ==========================================
        // --- CABECERA DE LA VENTANA ---
        // ==========================================
        JPanel panelCabecera = new JPanel();
        panelCabecera.setBackground(new Color(110, 216, 255)); // Mismo amarillo de tu menú lateral
        panelCabecera.setBorder(new EmptyBorder(15, 0, 15, 0));

        JLabel lblTitulo = new JLabel("📝 Agregar Nuevo Producto");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(40, 40, 40));
        panelCabecera.add(lblTitulo);

        add(panelCabecera, BorderLayout.NORTH);

        // ==========================================
        // --- PANEL DE FORMULARIO ---
        // ==========================================
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 10, 20));
        panelFormulario.setOpaque(false);
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Nombre
        panelFormulario.add(crearLabelFormulario("Nombre del Producto:"));
        txtNombre = crearTextField();
        panelFormulario.add(txtNombre);

        // Precio
        panelFormulario.add(crearLabelFormulario("Precio ($):"));
        txtPrecio = crearTextField();
        panelFormulario.add(txtPrecio);

        // Stock
        panelFormulario.add(crearLabelFormulario("Cantidad en Stock:"));
        txtStock = crearTextField();
        panelFormulario.add(txtStock);

        // Categoría y sus botones
        panelFormulario.add(crearLabelFormulario("Categoría:"));

        JPanel panelCategoria = new JPanel(new BorderLayout(5, 0));
        panelCategoria.setOpaque(false);

        cbCategoria = new JComboBox<>(inventario.getCategoriasDisponibles());
        cbCategoria.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cbCategoria.setBackground(Color.WHITE);
        cbCategoria.setEditable(false);

        // Botones de acción para categorías
        JPanel panelBotonesCat = new JPanel(new GridLayout(1, 2, 5, 0));
        panelBotonesCat.setOpaque(false);

        JButton btnNuevaCategoria = crearBotonAccionPequeño("➕", new Color(46, 204, 113));
        JButton btnBorrarCategoria = crearBotonAccionPequeño("➖", new Color(231, 76, 60));

        // ACCIÓN: AGREGAR CATEGORÍA
        btnNuevaCategoria.addActionListener(e -> {
            String nuevaCategoria = JOptionPane.showInputDialog(this,
                    "Ingrese el nombre de la nueva categoría:",
                    "Nueva Categoría", JOptionPane.PLAIN_MESSAGE);

            if (nuevaCategoria != null && !nuevaCategoria.trim().isEmpty()) {
                String categoriaLimpia = nuevaCategoria.trim();
                inventario.agregarCategoriaSiNoExiste(categoriaLimpia);
                actualizarCategorias(categoriaLimpia);
            }
        });

        // ACCIÓN: BORRAR CATEGORÍA
        btnBorrarCategoria.addActionListener(e -> {
            if (cbCategoria.getSelectedItem() != null) {
                String categoriaSeleccionada = cbCategoria.getSelectedItem().toString();

                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "¿Estás seguro de eliminar la categoría '" + categoriaSeleccionada + "'?",
                        "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    boolean sePudoBorrar = inventario.eliminarCategoria(categoriaSeleccionada);
                    if (sePudoBorrar) {
                        actualizarCategorias(null); // Refresca y selecciona el primero por defecto
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No se puede eliminar la categoría porque hay productos que la están utilizando.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        panelBotonesCat.add(btnNuevaCategoria);
        panelBotonesCat.add(btnBorrarCategoria);

        panelCategoria.add(cbCategoria, BorderLayout.CENTER);
        panelCategoria.add(panelBotonesCat, BorderLayout.EAST);
        panelFormulario.add(panelCategoria);

        add(panelFormulario, BorderLayout.CENTER);

        // ==========================================
        // --- PANEL DE BOTONES PRINCIPALES ---
        // ==========================================
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        panelBotones.setOpaque(false);

        JButton btnGuardar = crearBotonPrincipal("Guardar Producto", new Color(41, 128, 185));
        JButton btnCancelar = crearBotonPrincipal("Cancelar", new Color(149, 165, 166));

        btnGuardar.addActionListener(e -> validarYGuardar());
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        add(panelBotones, BorderLayout.SOUTH);

        setVisible(true);
    }

    // --- MÉTODOS DE VALIDACIÓN ---

    private void validarYGuardar() {
        String nombre = txtNombre.getText().trim();
        String precioStr = txtPrecio.getText().trim();
        String stockStr = txtStock.getText().trim();
        String categoria = cbCategoria.getSelectedItem() != null ? cbCategoria.getSelectedItem().toString().trim() : "";

        if (nombre.isEmpty() || precioStr.isEmpty() || stockStr.isEmpty() || categoria.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, complete todos los campos.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double precio = 0;
        try {
            precio = Double.parseDouble(precioStr);
            if (precio < 0) {
                JOptionPane.showMessageDialog(this, "El PRECIO no puede ser negativo.", "Error en Precio", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error en el campo PRECIO.\nAsegúrese de ingresar solo números.", "Error en Precio", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int stock = 0;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                JOptionPane.showMessageDialog(this, "El STOCK no puede ser negativo.", "Error en Stock", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error en el campo STOCK.\nAsegúrese de ingresar un número entero válido.", "Error en Stock", JOptionPane.ERROR_MESSAGE);
            return;
        }

        inventario.agregarCategoriaSiNoExiste(categoria);
        inventario.registrarProducto(nombre, precio, stock, categoria);

        guardadoExitoso = true;
        dispose();
    }

    // --- MÉTODOS AUXILIARES DE DISEÑO ---

    private void actualizarCategorias(String itemASeleccionar) {
        cbCategoria.setModel(new DefaultComboBoxModel<>(inventario.getCategoriasDisponibles()));
        if (itemASeleccionar != null) {
            cbCategoria.setSelectedItem(itemASeleccionar);
        }
    }

    private JLabel crearLabelFormulario(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 14));
        lbl.setForeground(new Color(80, 80, 80));
        return lbl;
    }

    private JTextField crearTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        // Un borde sutil para que se vea moderno
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return txt;
    }

    private JButton crearBotonPrincipal(String texto, Color colorFondo) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(colorFondo);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(160, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton crearBotonAccionPequeño(String icono, Color colorFondo) {
        JButton btn = new JButton(icono);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(colorFondo);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public boolean isGuardadoExitoso() {
        return guardadoExitoso;
    }
}