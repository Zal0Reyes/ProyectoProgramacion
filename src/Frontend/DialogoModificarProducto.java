package Frontend;

import Backend.Inventario;
import Backend.Producto;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogoModificarProducto extends JDialog {

    private Producto producto;
    private Inventario inventario;
    private boolean guardadoExitoso = false;
    private boolean productoBorrado = false;

    // Componentes del formulario
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtStock;
    private JComboBox<String> cbCategoria;
    private JTextField txtId;

    public DialogoModificarProducto(JFrame parent, Producto producto, Inventario inventario) {
        super(parent, "Detalles del Producto", true);
        this.producto = producto;
        this.inventario = inventario;

        // Ajustamos tamaño para acomodar las 5 filas y los 3 botones
        setSize(480, 440);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public void mostrar() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250)); // Mismo fondo que la vista principal

        // ==========================================
        // --- CABECERA DE LA VENTANA ---
        // ==========================================
        JPanel panelCabecera = new JPanel();
        panelCabecera.setBackground(new Color(255, 204, 51)); // Mismo amarillo
        panelCabecera.setBorder(new EmptyBorder(15, 0, 15, 0));

        JLabel lblTitulo = new JLabel("📝 Modificar Producto");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(40, 40, 40));
        panelCabecera.add(lblTitulo);

        add(panelCabecera, BorderLayout.NORTH);

        // ==========================================
        // --- PANEL DE FORMULARIO ---
        // ==========================================
        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 10, 20));
        panelFormulario.setOpaque(false); // Transparente para que se vea el fondo
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(25, 30, 15, 30));

        txtNombre = crearTextField();
        txtPrecio = crearTextField();
        txtStock = crearTextField();

        // --- COMPONENTE CATEGORÍA ---
        JPanel panelCategoria = new JPanel(new BorderLayout(5, 0));
        panelCategoria.setOpaque(false);

        cbCategoria = new JComboBox<>(inventario.getCategoriasDisponibles());
        cbCategoria.setFont(new Font("SansSerif", Font.PLAIN, 14));
        cbCategoria.setBackground(Color.WHITE);
        cbCategoria.setEditable(false);

        JPanel panelBotonesCat = new JPanel(new GridLayout(1, 2, 5, 0));
        panelBotonesCat.setOpaque(false);

        JButton btnNuevaCategoria = crearBotonAccionPequeño("➕", new Color(46, 204, 113));
        JButton btnBorrarCategoria = crearBotonAccionPequeño("➖", new Color(231, 76, 60));

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

        btnBorrarCategoria.addActionListener(e -> {
            if (cbCategoria.getSelectedItem() != null) {
                String categoriaSeleccionada = cbCategoria.getSelectedItem().toString();
                int confirmacion = JOptionPane.showConfirmDialog(this,
                        "¿Estás seguro de eliminar la categoría '" + categoriaSeleccionada + "'?",
                        "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirmacion == JOptionPane.YES_OPTION) {
                    boolean sePudoBorrar = inventario.eliminarCategoria(categoriaSeleccionada);
                    if (sePudoBorrar) {
                        actualizarCategorias(null);
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

        // ID (Bloqueado visualmente para que encaje con la estética)
        txtId = crearTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(235, 235, 235));
        txtId.setForeground(new Color(120, 120, 120));
        txtId.setFocusable(false); // Evita que parpadee el cursor ahí

        // Añadir etiquetas y campos
        agregarCampoFormulario(panelFormulario, "Nombre del Producto:", txtNombre);
        agregarCampoFormulario(panelFormulario, "Precio ($):", txtPrecio);
        agregarCampoFormulario(panelFormulario, "Stock Unidades:", txtStock);
        agregarCampoFormulario(panelFormulario, "Categoría:", panelCategoria);
        agregarCampoFormulario(panelFormulario, "ID:", txtId);

        add(panelFormulario, BorderLayout.CENTER);

        // ==========================================
        // --- PANEL DE BOTONES PRINCIPALES ---
        // ==========================================
        JPanel panelBotonesContenedor = new JPanel(new BorderLayout());
        panelBotonesContenedor.setOpaque(false);
        panelBotonesContenedor.setBorder(new EmptyBorder(10, 30, 20, 30));

        // Botón Borrar a la Izquierda
        JButton btnBorrar = crearBotonPrincipal("Borrar", new Color(231, 76, 60), 110);
        btnBorrar.addActionListener(e -> borrarProducto());
        panelBotonesContenedor.add(btnBorrar, BorderLayout.WEST);

        // Botones Cancelar y Guardar a la Derecha
        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelDerecha.setOpaque(false);

        JButton btnCancelar = crearBotonPrincipal("Cancelar", new Color(149, 165, 166), 110);
        JButton btnGuardar = crearBotonPrincipal("Guardar", new Color(41, 128, 185), 110);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarCambios());

        panelDerecha.add(btnCancelar);
        panelDerecha.add(btnGuardar);

        panelBotonesContenedor.add(panelDerecha, BorderLayout.EAST);

        add(panelBotonesContenedor, BorderLayout.SOUTH);

        // Cargamos los datos después de dibujar la ventana
        cargarDatosActuales();

        setVisible(true);
    }

    private void cargarDatosActuales() {
        txtNombre.setText(producto.getNombre());
        // Forzamos que el precio se muestre sin notación científica y en formato entendible
        txtPrecio.setText(String.format("%.2f", producto.getPrecio()).replace(",", "."));
        txtStock.setText(String.valueOf(producto.getStock()));
        txtId.setText(producto.getId());

        cbCategoria.setSelectedItem(producto.getCategoria());
    }

    private void borrarProducto() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que deseas eliminar este producto?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            productoBorrado = true;
            dispose();
        }
    }

    private void guardarCambios() {
        try {
            double nuevoPrecio = Double.parseDouble(txtPrecio.getText());
            int nuevoStock = Integer.parseInt(txtStock.getText());
            String categoriaSeleccionada = cbCategoria.getSelectedItem() != null ? cbCategoria.getSelectedItem().toString() : "";

            if (nuevoPrecio < 0 || nuevoStock < 0) {
                JOptionPane.showMessageDialog(this, "El precio y el stock no pueden ser negativos.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (txtNombre.getText().trim().isEmpty() || categoriaSeleccionada.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre y la categoría no pueden estar vacíos.", "Error de Validación", JOptionPane.WARNING_MESSAGE);
                return;
            }

            producto.setNombre(txtNombre.getText().trim());
            producto.setPrecio(nuevoPrecio);
            producto.setStock(nuevoStock);
            producto.setCategoria(categoriaSeleccionada);

            guardadoExitoso = true;
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese valores numéricos válidos para Precio y Stock.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isGuardadoExitoso() {
        return guardadoExitoso;
    }

    public boolean isProductoBorrado() {
        return productoBorrado;
    }

    // --- MÉTODOS AUXILIARES DE DISEÑO ---

    private void actualizarCategorias(String itemASeleccionar) {
        cbCategoria.setModel(new DefaultComboBoxModel<>(inventario.getCategoriasDisponibles()));
        if (itemASeleccionar != null) {
            cbCategoria.setSelectedItem(itemASeleccionar);
        }
    }

    private void agregarCampoFormulario(JPanel panel, String textoLabel, JComponent componente) {
        JLabel label = new JLabel(textoLabel);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(new Color(80, 80, 80));
        panel.add(label);
        panel.add(componente);
    }

    private JTextField crearTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return txt;
    }

    private JButton crearBotonPrincipal(String texto, Color colorFondo, int ancho) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(colorFondo);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(ancho, 40));
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
}