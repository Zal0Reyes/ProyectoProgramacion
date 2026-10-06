package Frontend;

import Backend.Compra;
import Backend.Inventario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DialogoHistorialCompras extends JDialog {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Inventario inventario;
    private final JTextField txtRut;
    private final JTextArea texto;

    public DialogoHistorialCompras(Frame propietario, Inventario inventario) {
        super(propietario, "Historial de compras", true);
        this.inventario = inventario;

        setSize(760, 560);
        setLocationRelativeTo(propietario);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 247, 250));

        JPanel panelFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelFiltro.setOpaque(false);
        panelFiltro.setBorder(new EmptyBorder(5, 10, 0, 10));

        JLabel lblRut = new JLabel("Filtrar por RUT:");
        txtRut = new JTextField(15);
        JButton btnFiltrar = new JButton("Filtrar");
        JButton btnRestablecer = new JButton("Restablecer");

        btnFiltrar.addActionListener(e -> actualizarLista());
        btnRestablecer.addActionListener(e -> {
            txtRut.setText("");
            actualizarLista();
        });
        txtRut.addActionListener(e -> actualizarLista());

        panelFiltro.add(lblRut);
        panelFiltro.add(txtRut);
        panelFiltro.add(btnFiltrar);
        panelFiltro.add(btnRestablecer);
        add(panelFiltro, BorderLayout.NORTH);

        texto = new JTextArea();
        texto.setEditable(false);
        texto.setFont(new Font("SansSerif", Font.PLAIN, 14));
        texto.setBackground(Color.WHITE);
        texto.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(new JScrollPane(texto), BorderLayout.CENTER);

        JButton cerrar = new JButton("Cerrar");
        cerrar.addActionListener(e -> dispose());
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBoton.setOpaque(false);
        panelBoton.add(cerrar);
        add(panelBoton, BorderLayout.SOUTH);

        actualizarLista();
    }

    public void mostrar() {
        setVisible(true);
    }

    private void actualizarLista() {
        String rutBuscado = txtRut.getText().trim();
        List<Compra> compras = inventario.getHistorialCompras().stream()
                .filter(compra -> rutBuscado.isEmpty()
                        || compra.getRutUsuario().equalsIgnoreCase(rutBuscado))
                .sorted(Comparator.comparing(Compra::getFechaHora).reversed())
                .collect(Collectors.toList());

        if (compras.isEmpty()) {
            texto.setText(rutBuscado.isEmpty()
                    ? "No hay compras registradas."
                    : "No hay compras asociadas al RUT ingresado.");
            return;
        }

        StringBuilder contenido = new StringBuilder();
        for (int i = 0; i < compras.size(); i++) {
            Compra compra = compras.get(i);
            contenido.append("Compra #").append(i + 1)
                    .append("  |  ").append(compra.getFechaHora().format(FORMATO_FECHA)).append("\n")
                    .append("RUT: ").append(compra.getRutUsuario()).append("\n")
                    .append("Artículos: ").append(String.join(", ", compra.getArticulos())).append("\n")
                    .append("Total pagado: $")
                    .append(String.format("%,.0f", compra.getTotalPagado()).replace(',', '.'))
                    .append("\n\n");
        }
        texto.setText(contenido.toString());
        texto.setCaretPosition(0);
    }
}