package Frontend;

import Backend.Inventario;
import Backend.Producto;
import Backend.Sistema;
import Backend.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class VistaCliente extends BaseFrame {

    // Colores principales de NexoMarket
    private static final Color FONDO = new Color(238, 243, 249);
    private static final Color NAVY = new Color(19, 40, 72);
    private static final Color AZUL = new Color(30, 105, 210);
    private static final Color AZUL_SUAVE = new Color(224, 235, 248);
    private static final Color GRIS = new Color(100, 110, 125);

    // Componentes que cambian durante la ejecución
    private JTextField txtBuscar;
    private JPanel gridProductos;
    private JLabel lblCantidad;
    private JPanel panelArticulosCarrito;
    private JLabel lblCarritoTitulo;
    private JLabel lblTotalCarrito;
    private JButton btnComprar;
    private final Map<Producto, Integer> carrito = new LinkedHashMap<>();

    // Filtros activos
    private String categoriaFiltro = "Todas";
    private double precioMinimo = -1;
    private double precioMaximo = -1;

    public VistaCliente(Inventario inventario, Sistema sistema) {
        super("NexoMarket - Cliente", inventario, sistema);
        setSize(1200, 720);
    }

    @Override
    public void mostrar() {

        setLayout(new BorderLayout());
        getContentPane().setBackground(FONDO);

        add(crearCabecera(), BorderLayout.NORTH);

        JPanel contenido = new JPanel(new BorderLayout(15, 0));
        contenido.setBackground(FONDO);
        contenido.setBorder(new EmptyBorder(15, 18, 18, 18));

        contenido.add(crearCatalogo(), BorderLayout.CENTER);
        contenido.add(crearCarrito(), BorderLayout.EAST);

        add(contenido, BorderLayout.CENTER);

        // Actualiza los productos mientras se escribe
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                actualizarProductos();
            }
        });

        actualizarProductos();
        setVisible(true);
    }

    // =========================================================
    // CABECERA: logo, buscador, filtro y login
    // =========================================================
    // =========================================================
// CABECERA: logo, buscador, filtro y login
// =========================================================
    private JPanel crearCabecera() {

        // Contenedor general de la cabecera
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Color.WHITE);


        // Cabecera principal:
        // izquierda = logo
        // centro = buscador
        // derecha = botones
        JPanel cabecera = new JPanel(new BorderLayout(15, 0));
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(new EmptyBorder(8, 20, 8, 20));


        // =========================
        // LOGO
        // =========================

        JLabel lblLogo = new JLabel();

        ImageIcon logo = cargarLogo();

        if (logo != null) {

            lblLogo.setIcon(logo);

        } else {

            // Texto de respaldo si no se encuentra la imagen
            lblLogo.setText("NEXOMARKET");

            lblLogo.setFont(
                    new Font(
                            "SansSerif",
                            Font.BOLD,
                            23
                    )
            );

            lblLogo.setForeground(NAVY);
        }


        // =========================
        // BUSCADOR
        // =========================

        txtBuscar = new JTextField();

        txtBuscar.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        15
                )
        );

        // Borde visible + espacio interior
        txtBuscar.setBorder(
                BorderFactory.createCompoundBorder(

                        BorderFactory.createLineBorder(
                                new Color(180, 190, 205)
                        ),

                        new EmptyBorder(
                                9,
                                12,
                                9,
                                12
                        )
                )
        );


        /*
         * Este panel permite que el buscador
         * ocupe TODO el espacio disponible entre
         * el logo y los botones.
         *
         * Ya NO usamos un ancho fijo de 620.
         */
        JPanel panelBusqueda =
                new JPanel(new BorderLayout());

        panelBusqueda.setOpaque(false);

        // Dejamos espacio arriba y abajo para
        // que el buscador no quede demasiado alto.
        panelBusqueda.setBorder(
                new EmptyBorder(
                        15,
                        0,
                        15,
                        0
                )
        );

        panelBusqueda.add(
                txtBuscar,
                BorderLayout.CENTER
        );


        // =========================
        // BOTONES DERECHA
        // =========================

        JPanel acciones =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                10,
                                10
                        )
                );

        acciones.setOpaque(false);


        JButton btnFiltrar =
                crearBoton(
                        "Filtrar",
                        AZUL
                );


        JButton btnLogin =
                crearBoton(
                        "Iniciar sesión",
                        NAVY
                );


        // Los botones sí mantienen tamaño fijo
        btnFiltrar.setPreferredSize(
                new Dimension(
                        120,
                        45
                )
        );

        btnLogin.setPreferredSize(
                new Dimension(
                        150,
                        45
                )
        );


        btnFiltrar.addActionListener(
                e -> mostrarFiltros()
        );



        btnLogin.addActionListener(e -> {

            VistaLogin dialogoLogin = new VistaLogin(this, sistema);
            dialogoLogin.mostrar();

            Usuario user = dialogoLogin.getUsuarioLogeado();

            if (user != null) {
                if (user.getTipoUsuario().equals("Administrador")) {
                    // Es Administrador: Cerramos la VistaCliente y abrimos Vista Tienda
                    this.dispose();
                    VistaPrincipalTienda adminVista = new VistaPrincipalTienda(inventario, sistema);
                    adminVista.mostrar();
                } else {
                    // Es Cliente: Se queda en esta misma vista
                    btnLogin.setText("👤 Hola, " + user.getNombre());
                    btnLogin.setEnabled(false); // Desactivar el botón porque ya inició sesión
                }
            }
        });


        acciones.add(btnFiltrar);
        acciones.add(btnLogin);


        // =========================
        // ARMAR CABECERA
        // =========================

        cabecera.add(
                lblLogo,
                BorderLayout.WEST
        );

        cabecera.add(
                panelBusqueda,
                BorderLayout.CENTER
        );

        cabecera.add(
                acciones,
                BorderLayout.EAST
        );


        // Franja inferior azul
        JPanel franja = new JPanel();

        franja.setBackground(NAVY);

        franja.setPreferredSize(
                new Dimension(
                        0,
                        5
                )
        );


        contenedor.add(
                cabecera,
                BorderLayout.CENTER
        );

        contenedor.add(
                franja,
                BorderLayout.SOUTH
        );


        return contenedor;
    }

    // =========================================================
    // CATÁLOGO DE PRODUCTOS
    // =========================================================
    private JPanel crearCatalogo() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        // Barra superior del catálogo
        PanelRedondeado encabezado = new PanelRedondeado(12);
        encabezado.setBackground(NAVY);
        encabezado.setLayout(new BorderLayout());
        encabezado.setBorder(new EmptyBorder(9, 12, 9, 12));

        JLabel titulo = new JLabel("Productos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);

        lblCantidad = new JLabel();
        lblCantidad.setForeground(new Color(205, 215, 230));

        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(lblCantidad, BorderLayout.EAST);

        // Grilla
        gridProductos = new JPanel(new GridLayout(0, 3, 12, 12));
        gridProductos.setOpaque(false);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.setBorder(new EmptyBorder(12, 0, 0, 0));
        contenedor.add(gridProductos, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(contenedor);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // =========================================================
    // PANEL DEL CARRITO
    // =========================================================
    private JPanel crearCarrito() {

        PanelRedondeado carrito = new PanelRedondeado(18);
        carrito.setBackground(AZUL_SUAVE);
        carrito.setPreferredSize(new Dimension(250, 0));
        carrito.setLayout(new BorderLayout());
        carrito.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Título
        PanelRedondeado cabecera = new PanelRedondeado(10);
        cabecera.setBackground(NAVY);
        cabecera.setLayout(new BorderLayout());
        cabecera.setBorder(new EmptyBorder(9, 10, 9, 10));

        lblCarritoTitulo = new JLabel("Mi Carrito (0)");
        lblCarritoTitulo.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblCarritoTitulo.setForeground(Color.WHITE);

        cabecera.add(lblCarritoTitulo);

        panelArticulosCarrito = new JPanel();
        panelArticulosCarrito.setOpaque(false);
        panelArticulosCarrito.setLayout(new BoxLayout(panelArticulosCarrito, BoxLayout.Y_AXIS));

        JScrollPane scrollCarrito = new JScrollPane(panelArticulosCarrito);
        scrollCarrito.setBorder(null);
        scrollCarrito.setOpaque(false);
        scrollCarrito.getViewport().setOpaque(false);
        scrollCarrito.getVerticalScrollBar().setUnitIncrement(12);

        // Parte inferior
        JPanel inferior = new JPanel();
        inferior.setOpaque(false);
        inferior.setLayout(new BoxLayout(inferior, BoxLayout.Y_AXIS));

        lblTotalCarrito = new JLabel("Total: $0");
        lblTotalCarrito.setFont(new Font("SansSerif", Font.BOLD, 17));
        lblTotalCarrito.setForeground(NAVY);

        btnComprar = crearBoton("Proceder a la compra", AZUL);
        btnComprar.setEnabled(false);
        btnComprar.addActionListener(e -> procederCompra());

        inferior.add(lblTotalCarrito);
        inferior.add(Box.createRigidArea(new Dimension(0, 10)));
        inferior.add(btnComprar);

        carrito.add(cabecera, BorderLayout.NORTH);
        carrito.add(scrollCarrito, BorderLayout.CENTER);
        carrito.add(inferior, BorderLayout.SOUTH);

        actualizarCarrito();

        return carrito;
    }

    // =========================================================
    // VENTANA DE FILTROS
    // =========================================================
    private void mostrarFiltros() {

        String[] existentes = inventario.getCategoriasDisponibles();
        String[] categorias = new String[existentes.length + 1];

        categorias[0] = "Todas";

        for (int i = 0; i < existentes.length; i++) {
            categorias[i + 1] = existentes[i];
        }

        JComboBox<String> cbCategoria = new JComboBox<>(categorias);
        cbCategoria.setSelectedItem(categoriaFiltro);

        JTextField txtMin = new JTextField();
        JTextField txtMax = new JTextField();

        if (precioMinimo >= 0)
            txtMin.setText(String.valueOf(precioMinimo));

        if (precioMaximo >= 0)
            txtMax.setText(String.valueOf(precioMaximo));

        Object[] contenido = {
                "Categoría:", cbCategoria,
                "Precio mínimo:", txtMin,
                "Precio máximo:", txtMax
        };

        int opcion = JOptionPane.showConfirmDialog(
                this, contenido, "Filtrar productos",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION)
            return;

        try {

            double min = leerPrecio(txtMin);
            double max = leerPrecio(txtMax);

            if ((min < 0 && min != -1) || (max < 0 && max != -1)) {
                mostrarError("Los precios no pueden ser negativos.");
                return;
            }

            if (min >= 0 && max >= 0 && min > max) {
                mostrarError("El precio mínimo no puede ser mayor al máximo.");
                return;
            }

            categoriaFiltro = cbCategoria.getSelectedItem().toString();
            precioMinimo = min;
            precioMaximo = max;

            actualizarProductos();

        } catch (NumberFormatException e) {
            mostrarError("Ingrese precios válidos.");
        }
    }

    // Devuelve -1 cuando el campo está vacío
    private double leerPrecio(JTextField campo) {
        if (campo.getText().trim().isEmpty())
            return -1;

        return Double.parseDouble(campo.getText().trim());
    }

    // =========================================================
    // BUSCAR Y FILTRAR PRODUCTOS
    // =========================================================
    private void actualizarProductos() {

        gridProductos.removeAll();

        String busqueda = txtBuscar.getText().trim().toLowerCase();
        int encontrados = 0;

        for (Producto p : inventario.getProductos()) {

            boolean coincideBusqueda =
                    p.getNombre().toLowerCase().contains(busqueda)
                            || p.getCategoria().toLowerCase().contains(busqueda);

            boolean coincideCategoria =
                    categoriaFiltro.equals("Todas")
                            || p.getCategoria().equalsIgnoreCase(categoriaFiltro);

            boolean coincidePrecio =
                    (precioMinimo < 0 || p.getPrecio() >= precioMinimo)
                            && (precioMaximo < 0 || p.getPrecio() <= precioMaximo);

            if (coincideBusqueda && coincideCategoria && coincidePrecio) {
                gridProductos.add(crearTarjeta(p));
                encontrados++;
            }
        }

        lblCantidad.setText(encontrados + " productos encontrados");

        gridProductos.revalidate();
        gridProductos.repaint();
    }

    // =========================================================
    // TARJETA DE UN PRODUCTO
    // =========================================================
    private JPanel crearTarjeta(Producto producto) {

        PanelRedondeado tarjeta = new PanelRedondeado(16);
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(10, 10, 10, 10));
        tarjeta.setPreferredSize(new Dimension(215, 255));

        // Imagen
        JLabel imagen = new JLabel();
        imagen.setOpaque(true);
        imagen.setBackground(new Color(231, 238, 247));
        imagen.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon icono = cargarImagen(producto.getRutaImagen());

        if (icono != null) {
            imagen.setIcon(icono);
        } else {
            imagen.setText(producto.getNombre().substring(0, 1).toUpperCase());
            imagen.setFont(new Font("SansSerif", Font.BOLD, 40));
            imagen.setForeground(NAVY);
        }

        // Datos
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBorder(new EmptyBorder(7, 0, 0, 0));

        JLabel nombre = new JLabel(acortar(producto.getNombre(), 34));
        nombre.setFont(new Font("SansSerif", Font.BOLD, 13));
        nombre.setForeground(NAVY);
        nombre.setToolTipText(producto.getNombre());

        JLabel categoria = new JLabel(producto.getCategoria());
        categoria.setForeground(GRIS);

        JLabel precio = new JLabel(formatearPesos(producto.getPrecio()));
        precio.setFont(new Font("SansSerif", Font.BOLD, 16));
        precio.setForeground(NAVY);

        JLabel stock = crearStock(producto);

        JButton agregar = crearBoton("Agregar al carrito", AZUL);

        if (producto.getStock() == 0) {
            agregar.setText("Sin stock");
            agregar.setEnabled(false);
        }

        agregar.addActionListener(e -> agregarAlCarrito(producto));

        info.add(nombre);
        info.add(categoria);
        info.add(Box.createRigidArea(new Dimension(0, 3)));
        info.add(precio);
        info.add(stock);
        info.add(Box.createRigidArea(new Dimension(0, 6)));
        info.add(agregar);

        tarjeta.add(imagen, BorderLayout.CENTER);
        tarjeta.add(info, BorderLayout.SOUTH);

        return tarjeta;
    }

    private void agregarAlCarrito(Producto producto) {
        if (!inventario.reservarUnidad(producto)) {
            actualizarProductos();
            mostrarError("El producto ya no tiene stock disponible.");
            return;
        }

        carrito.put(producto, carrito.getOrDefault(producto, 0) + 1);
        actualizarCarrito();
        actualizarProductos();
    }

    private void actualizarCarrito() {
        if (panelArticulosCarrito == null) {
            return;
        }

        panelArticulosCarrito.removeAll();
        int unidades = 0;
        double total = 0;

        for (Map.Entry<Producto, Integer> entrada : carrito.entrySet()) {
            Producto producto = entrada.getKey();
            int cantidad = entrada.getValue();
            unidades += cantidad;
            total += producto.getPrecio() * cantidad;

            JLabel articulo = new JLabel(cantidad + " x " + producto.getNombre()
                    + " - " + formatearPesos(producto.getPrecio() * cantidad));
            articulo.setFont(new Font("SansSerif", Font.PLAIN, 13));
            articulo.setForeground(NAVY);
            articulo.setBorder(new EmptyBorder(7, 3, 7, 3));
            articulo.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelArticulosCarrito.add(articulo);
        }

        if (carrito.isEmpty()) {
            JLabel vacio = new JLabel("<html><center><b>Tu carrito está vacío</b>"
                    + "<br><br>Agrega productos para comenzar</center></html>");
            vacio.setHorizontalAlignment(SwingConstants.CENTER);
            vacio.setForeground(GRIS);
            vacio.setAlignmentX(Component.CENTER_ALIGNMENT);
            panelArticulosCarrito.add(Box.createVerticalGlue());
            panelArticulosCarrito.add(vacio);
            panelArticulosCarrito.add(Box.createVerticalGlue());
        }

        lblCarritoTitulo.setText("Mi Carrito (" + unidades + ")");
        lblTotalCarrito.setText("Total: " + formatearPesos(total));
        btnComprar.setEnabled(!carrito.isEmpty());
        panelArticulosCarrito.revalidate();
        panelArticulosCarrito.repaint();
    }

    private void procederCompra() {
        Usuario usuarioActual = sistema.getUsuarioActual();

        if (!(usuarioActual instanceof Backend.Cliente)) {
            JOptionPane.showMessageDialog(this,
                    "Debes iniciar sesión como cliente para realizar la compra.",
                    "Inicio de sesión requerido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        inventario.confirmarCompra();
        carrito.clear();
        actualizarCarrito();
        actualizarProductos();
        JOptionPane.showMessageDialog(this, "Compra realizada", "Compra", JOptionPane.INFORMATION_MESSAGE);
    }

    // =========================================================
    // STOCK DEL PRODUCTO
    // =========================================================
    private JLabel crearStock(Producto producto) {

        JLabel stock = new JLabel();

        if (producto.getStock() == 0) {
            stock.setText("● Agotado");
            stock.setForeground(new Color(215, 60, 70));

        } else if (producto.getStock() <= 5) {
            stock.setText("● Pocas unidades (" + producto.getStock() + ")");
            stock.setForeground(new Color(225, 140, 25));

        } else {
            stock.setText("● Disponible (" + producto.getStock() + ")");
            stock.setForeground(new Color(35, 145, 85));
        }

        return stock;
    }

    // =========================================================
    // MÉTODOS AUXILIARES
    // =========================================================

    private JButton crearBoton(String texto, Color color) {

        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 13));
        boton.setForeground(Color.WHITE);
        boton.setBackground(color);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return boton;
    }

    // Busca el logo aunque cambie la extensión
    private ImageIcon cargarLogo() {

        String[] rutas = {
                "imagenes/Logo NEXOMARKET.jepg",
                "imagenes/Logo NEXOMARKET.jpeg",
                "imagenes/Logo NEXOMARKET.jpg",
                "imagenes/Logo NEXOMARKET.png"
        };

        for (String ruta : rutas) {

            File archivo = new File(ruta);

            if (archivo.isFile()) {

                ImageIcon original = new ImageIcon(ruta);

                Image escalada = original.getImage().getScaledInstance(
                        180, 75, Image.SCALE_SMOOTH
                );

                return new ImageIcon(escalada);
            }
        }

        return null;
    }

    private ImageIcon cargarImagen(String ruta) {

        File archivo = ImagenesUtil.resolverRutaImagen(ruta);
        if (archivo == null)
            return null;

        ImageIcon original = new ImageIcon(archivo.getPath());

        Image escalada = original.getImage().getScaledInstance(
                160, 95, Image.SCALE_SMOOTH
        );

        return new ImageIcon(escalada);
    }

    private String acortar(String texto, int maximo) {

        if (texto.length() <= maximo)
            return texto;

        return texto.substring(0, maximo) + "...";
    }

    private String formatearPesos(double valor) {
        return "$" + String.format("%,.0f", valor).replace(",", ".");
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
                this, mensaje, "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}