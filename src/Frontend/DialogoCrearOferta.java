package Frontend;

import Backend.Inventario;
import Backend.Producto;
import Backend.ConexionServidor;

import javax.swing.*;
import java.awt.*;

public class DialogoCrearOferta {

    private final Frame padre;
    private final Inventario inventario;
    private final ConexionServidor conexion;

    public DialogoCrearOferta(
            Frame padre,
            Inventario inventario,
            ConexionServidor conexion) {

        this.padre = padre;
        this.inventario = inventario;
        this.conexion = conexion;
    }

    public void mostrar() {

        if (!conexion.estaConectado()) {
            JOptionPane.showMessageDialog(
                    padre,
                    "No hay conexión con el servidor.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        JComboBox<String> cbTipo = new JComboBox<>(
                new String[]{"Producto", "Categoría"}
        );

        JComboBox<String> cbObjetivo = new JComboBox<>();

        Runnable cargarObjetivos = () -> {

            cbObjetivo.removeAllItems();

            if (cbTipo.getSelectedItem().equals("Producto")) {

                for (Producto p : inventario.getProductos()) {
                    cbObjetivo.addItem(
                            p.getId() + " - " + p.getNombre()
                    );
                }

            } else {

                for (String categoria : inventario.getCategoriasDisponibles()) {
                    cbObjetivo.addItem(categoria);
                }
            }
        };

        cargarObjetivos.run();
        cbTipo.addActionListener(e -> cargarObjetivos.run());

        JTextField txtDescuento = new JTextField("10");

        JSpinner spHoras = new JSpinner(
                new SpinnerNumberModel(24, 1, 720, 1)
        );

        JPanel formulario = new JPanel(new GridLayout(4, 2, 10, 10));

        formulario.add(new JLabel("Tipo de oferta:"));
        formulario.add(cbTipo);

        formulario.add(new JLabel("Producto / Categoría:"));
        formulario.add(cbObjetivo);

        formulario.add(new JLabel("Descuento (%):"));
        formulario.add(txtDescuento);

        formulario.add(new JLabel("Duración (horas):"));
        formulario.add(spHoras);

        int opcion = JOptionPane.showConfirmDialog(
                padre,
                formulario,
                "Crear oferta temporal",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) return;

        try {

            if (cbObjetivo.getSelectedItem() == null) {
                throw new IllegalArgumentException("Seleccione un producto o categoría.");
            }

            double descuento = Double.parseDouble(
                    txtDescuento.getText().trim().replace(",", ".")
            );

            if (descuento <= 0 || descuento >= 100) {
                throw new IllegalArgumentException(
                        "El descuento debe estar entre 0 y 100%."
                );
            }

            String tipo = cbTipo.getSelectedItem().equals("Producto")
                    ? "PRODUCTO" : "CATEGORIA";

            String objetivo = cbObjetivo.getSelectedItem().toString();

            if (tipo.equals("PRODUCTO")) {
                objetivo = objetivo.split(" - ", 2)[0];
            }

            long inicio = System.currentTimeMillis();

            long fin = inicio
                    + ((Integer) spHoras.getValue() * 3600000L);

            conexion.crearOfertaAdmin(
                    tipo,
                    objetivo,
                    descuento,
                    inicio,
                    fin
            );

        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    padre,
                    "Ingrese un porcentaje válido.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    padre,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}