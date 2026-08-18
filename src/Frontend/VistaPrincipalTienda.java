package Frontend;

import Backend.Inventario;
import Backend.Producto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class VistaPrincipalTienda extends BaseFrame {

    private JPanel gridProductos;

    public VistaPrincipalTienda(Inventario sistema) {
        super("Vista Tienda", sistema);
        configurarUI();
        // Cargar los productos iniciales que tenga el sistema al abrir la ventana
        actualizarVistaProductos();

    }

    private void configurarUI() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 247, 250));

        // --- BARRA LATERAL ---
        JPanel barraLateral = new JPanel();
        barraLateral.setBackground(new Color(255, 204, 51));
        barraLateral.setPreferredSize(new Dimension(80, 0));
        barraLateral.setLayout(new BoxLayout(barraLateral, BoxLayout.Y_AXIS));
        barraLateral.setBorder(new EmptyBorder(30, 0, 30, 0));

        barraLateral.add(crearBotonMenu("🏠", null));
        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));

        // BOTÓN AGREGAR CON ACCIÓN
        JButton btnAgregar = crearBotonMenu("➕", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarDialogoAgregar();
            }
        });
        barraLateral.add(btnAgregar);

        barraLateral.add(Box.createRigidArea(new Dimension(0, 30)));
        barraLateral.add(crearBotonMenu("⚙", null));

        add(barraLateral, BorderLayout.EAST);

        // --- ÁREA CENTRAL ---
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.setOpaque(false);
        panelCentral.setBorder(new EmptyBorder(30, 40, 30, 40));

        // --- PANEL NORTE (Título + Buscador) ---
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.setOpaque(false);
        panelNorte.setBorder(new EmptyBorder(0, 0, 20, 0)); // Margen inferior

        JLabel lblTitulo = new JLabel("Inventario de Productos");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(40, 40, 40));
        panelNorte.add(lblTitulo, BorderLayout.WEST);

        // --- ZONA DE BÚSQUEDA ---
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelBusqueda.setOpaque(false);

        JLabel lblBuscar = new JLabel("🔍 Buscar:");
        lblBuscar.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblBuscar.setForeground(new Color(100, 100, 100));

        JTextField txtBuscar = new JTextField(15);
        txtBuscar.setFont(new Font("SansSerif", Font.PLAIN, 16));
        // Estilo de la barra de búsqueda
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Evento que se dispara cada vez que el usuario suelta una tecla
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                // Llama al metodo actualizar pasando el texto actual
                actualizarVistaProductos(txtBuscar.getText());
            }
        });

        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscar);

        panelNorte.add(panelBusqueda, BorderLayout.EAST);

        // Agregamos el panel ensamblado al centro
        panelCentral.add(panelNorte, BorderLayout.NORTH);

        gridProductos = new JPanel(new GridLayout(0, 3, 20, 20));
        gridProductos.setOpaque(false);

        JPanel wrapperAntiEstiramiento = new JPanel(new BorderLayout());
        wrapperAntiEstiramiento.setOpaque(false);
        wrapperAntiEstiramiento.add(gridProductos, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrapperAntiEstiramiento);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        panelCentral.add(scroll, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);
    }


    // Metodo de sobreCarga que llamara a actualizarVista pero con un string vacio
    private void actualizarVistaProductos() {
        actualizarVistaProductos("");
    }

    // metodo dinamico que filtra productos
    private void actualizarVistaProductos(String textoBusqueda) {
        gridProductos.removeAll(); // borramos lo visual

        // Convertimos el texto buscado a minúsculas para que la búsqueda no sea sensible a mayúsculas
        String busqueda = textoBusqueda.toLowerCase();

        // Recorremos el ArrayList de inventario
        for (Producto p : sistema.getProductos()) {

            // Verificamos si el nombre o el ID del producto contienen el texto que el usuario escribió
            boolean coincideNombre = p.getNombre().toLowerCase().contains(busqueda);
            boolean coincideId = p.getId().toLowerCase().contains(busqueda);

            // Si el texto está vacío, o coincide con el nombre o el ID, mostramos la tarjeta
            if (textoBusqueda.isEmpty() || coincideNombre || coincideId) {
                gridProductos.add(crearTarjetaProducto(p));
            }
        }

        //Le decimos a Java Swing que redibuje la pantalla con los nuevos elementos
        gridProductos.revalidate();
        gridProductos.repaint();
    }

    private void mostrarDialogoAgregar() {
        // Creamos campos de texto personalizados para el JOptionPane
        JTextField txtNombre = new JTextField();
        JTextField txtPrecio = new JTextField();
        JTextField txtStock = new JTextField();
        JTextField txtCategoria = new JTextField();

        // Agrupamos los elementos que irán dentro de la ventana
        Object[] mensaje = {
                "Nombre del Producto:", txtNombre,
                "Precio:", txtPrecio,
                "Stock:", txtStock,
                "Categoria:", txtCategoria,
        };

        // Mostramos el JOptionPane
        int opcion = JOptionPane.showConfirmDialog(this, mensaje, "Agregar Nuevo Producto", JOptionPane.OK_CANCEL_OPTION);

        // Si el usuario hace clic en "OK"
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                String nombre = txtNombre.getText();
                int stock = Integer.parseInt(txtStock.getText());
                double precio = Double.parseDouble(txtPrecio.getText());
                String categoria = txtCategoria.getText();
                //Creamos el objeto

                sistema.registrarProducto(nombre,precio, stock, categoria);

                //Refrescamos la vista
                actualizarVistaProductos();

            } catch (NumberFormatException ex) {
                // Validación por si el usuario escribe letras en el precio
                JOptionPane.showMessageDialog(this, "Por favor ingrese un precio válido (solo números).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private JPanel crearTarjetaProducto(Producto producto) {
        PanelRedondeado tarjeta = new PanelRedondeado(20);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(15, 15, 15, 15));
        tarjeta.setPreferredSize(new Dimension(200, 240));

        tarjeta.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setBackground(new Color(248, 248, 248));
                tarjeta.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                tarjeta.setBackground(Color.WHITE);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                DialogoModificarProducto dialogo = new DialogoModificarProducto(VistaPrincipalTienda.this, producto);
                dialogo.setVisible(true);

                // 1. Verificar si el usuario decidió borrar el producto
                if (dialogo.isProductoBorrado()) {
                    // Llamamos a tu método del backend para eliminarlo por ID
                    sistema.eliminarProductoPorId(producto.getId());

                    // Refrescamos la vista para que el producto desaparezca
                    actualizarVistaProductos();

                    JOptionPane.showMessageDialog(VistaPrincipalTienda.this,
                            "Producto eliminado con éxito.",
                            "Eliminado",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                // 2. Si no lo borró, verificar si guardó cambios exitosamente
                else if (dialogo.isGuardadoExitoso()) {
                    actualizarVistaProductos();

                    JOptionPane.showMessageDialog(VistaPrincipalTienda.this,
                            "Producto modificado con éxito.",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        PanelRedondeado imgPlaceholder = new PanelRedondeado(15);
        imgPlaceholder.setBackground(new Color(235, 235, 235));
        imgPlaceholder.setPreferredSize(new Dimension(100, 140));
        tarjeta.add(imgPlaceholder, BorderLayout.CENTER);

        JPanel panelTextos = new JPanel(new GridLayout(2, 1, 0, 5));
        panelTextos.setOpaque(false);
        panelTextos.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel lblNombre = new JLabel(producto.getNombre());
        lblNombre.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblNombre.setForeground(new Color(60, 60, 60));
        String precioFormateado = "$" + String.format("%.2f", producto.getPrecio());
        JLabel lblPrecio = new JLabel(precioFormateado);
        lblPrecio.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblPrecio.setForeground(new Color(150, 150, 150));

        panelTextos.add(lblNombre);
        panelTextos.add(lblPrecio);

        tarjeta.add(panelTextos, BorderLayout.SOUTH);

        return tarjeta;
    }


    private JButton crearBotonMenu(String texto, ActionListener accion) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 28));
        btn.setForeground(Color.DARK_GRAY);
        btn.setBackground(new Color(255, 204, 51));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(true);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (accion != null) {
            btn.addActionListener(accion);
        }

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(255, 220, 90));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(255, 204, 51));
            }
        });
        return btn;
    }

    @Override
    public void mostrar() {
        setVisible(true);
    }


}