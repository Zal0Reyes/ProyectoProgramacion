package Frontend;

import Backend.Producto;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogoModificarProducto extends JDialog {

    private Producto producto;
    private boolean guardadoExitoso = false; // Para saber si refrescar la vista principal
    private boolean productoBorrado = false; //Para saber si borrar un producto

    // Componentes del formulario
    private JTextField txtNombre;
    private JTextField txtPrecio;
    private JTextField txtStock;
    private JTextField txtCategoria;
    private JTextField txtId;

    public DialogoModificarProducto(JFrame parent, Producto producto) {
        super(parent, "Detalles del Producto", true); // 'true' la hace Modal (bloquea la ventana de atrás)
        this.producto = producto;

        configurarUI();
        cargarDatosActuales();

        pack(); // Ajusta el tamaño al contenido
        setLocationRelativeTo(parent); // Centra el diálogo sobre la ventana principal
    }

    private void configurarUI() {
        // Panel principal con márgenes blancos
        JPanel panelPrincipal = new JPanel(new BorderLayout(0, 20));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(new EmptyBorder(25, 30, 25, 30));

        // --- TÍTULO ---
        JLabel lblTitulo = new JLabel("Modificar Producto");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(40, 40, 40));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);

        // --- FORMULARIO 8 5 filas y 2 columnas) ---
        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 15, 15));
        panelFormulario.setBackground(Color.WHITE);

        txtNombre = crearTextField();
        txtPrecio = crearTextField();
        txtStock = crearTextField();
        txtCategoria = crearTextField();

        txtId = crearTextField();
        txtId.setEditable(false);
        txtId.setBackground(new Color(245, 245, 245)); // Color grisáceo para indicar que está bloqueado
        txtId.setForeground(Color.GRAY);

        // Añadir etiquetas y campos a la grilla
        agregarCampoFormulario(panelFormulario, "Nombre:", txtNombre);
        agregarCampoFormulario(panelFormulario, "Precio ($):", txtPrecio);
        agregarCampoFormulario(panelFormulario, "Stock Unidades:", txtStock);
        agregarCampoFormulario(panelFormulario, "Categoría:", txtCategoria);
        agregarCampoFormulario(panelFormulario, "ID:", txtId);

        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);

        // --- BOTONES ---
        // Usamos un BorderLayout para separar los botones
        JPanel panelBotonesContenedor = new JPanel(new BorderLayout());
        panelBotonesContenedor.setBackground(Color.WHITE);
        panelBotonesContenedor.setBorder(new EmptyBorder(10, 0, 0, 0));

        // Botón Borrar (Izquierda)
        JPanel panelIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelIzquierda.setBackground(Color.WHITE);
        JButton btnBorrar = crearBoton("Borrar Producto", new Color(231, 76, 60), Color.WHITE); // Color Rojo
        btnBorrar.addActionListener(e -> borrarProducto());
        panelIzquierda.add(btnBorrar);

        // Botones Cancelar y Guardar (Derecha)
        JPanel panelDerecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelDerecha.setBackground(Color.WHITE);
        JButton btnCancelar = crearBoton("Cancelar", new Color(220, 220, 220), Color.DARK_GRAY);
        JButton btnGuardar = crearBoton("Guardar Cambios", new Color(46, 204, 113), Color.WHITE);

        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarCambios());

        panelDerecha.add(btnCancelar);
        panelDerecha.add(btnGuardar);

        // Agregamos ambos paneles al contenedor principal de botones
        panelBotonesContenedor.add(panelIzquierda, BorderLayout.WEST);
        panelBotonesContenedor.add(panelDerecha, BorderLayout.EAST);

        panelPrincipal.add(panelBotonesContenedor, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void cargarDatosActuales() {
        txtNombre.setText(producto.getNombre());
        txtPrecio.setText(String.valueOf(producto.getPrecio()));
        txtStock.setText(String.valueOf(producto.getStock()));
        txtCategoria.setText(producto.getCategoria());
        txtId.setText(producto.getId());
    }
    private void borrarProducto() {
        // Mostramos un mensaje de advertencia antes de borrar
        int confirmacion = JOptionPane.showConfirmDialog(this,
                "¿Estás seguro de que deseas eliminar este producto?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        // Si el usuario elige "Sí"
        if (confirmacion == JOptionPane.YES_OPTION) {
            productoBorrado = true; // Activamos la bandera
            dispose(); // Cerramos la ventana
        }
    }
    private void guardarCambios() {
        try {
            // Validamos que precio y stock sean números válidos
            double nuevoPrecio = Double.parseDouble(txtPrecio.getText());
            int nuevoStock = Integer.parseInt(txtStock.getText());

            // Actualizamos el objeto Producto
            producto.setNombre(txtNombre.getText());
            producto.setPrecio(nuevoPrecio);
            producto.setStock(nuevoStock);
            producto.setCategoria(txtCategoria.getText());

            guardadoExitoso = true;
            dispose(); // Cerramos el diálogo con éxito

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese valores numéricos válidos para Precio y Stock.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isGuardadoExitoso() {
        return guardadoExitoso;
    }

    // --- MÉTODOS DE DISEÑO (ESTILO) ---

    private void agregarCampoFormulario(JPanel panel, String textoLabel, JTextField textField) {
        JLabel label = new JLabel(textoLabel);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(new Color(80, 80, 80));
        panel.add(label);
        panel.add(textField);
    }

    private JTextField crearTextField() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("SansSerif", Font.PLAIN, 14));
        // Crea un borde sutil y añade un padding (espaciado interno) para que el texto no pegue a los bordes
        txt.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200)),
                new EmptyBorder(5, 10, 5, 10)
        ));
        return txt;
    }

    private JButton crearBoton(String texto, Color fondo, Color textoColor) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBackground(fondo);
        btn.setForeground(textoColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Efecto Hover simple
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(fondo.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(fondo);
            }
        });
        return btn;
    }

    public boolean isProductoBorrado() {
        return productoBorrado;
    }
}