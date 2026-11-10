package Frontend;

import Backend.Cupon;
import Backend.GestorCupones;

import javax.swing.*;
import java.awt.*;

public class DialogoCrearCupon extends JDialog {

    private GestorCupones gestor;

    private JTextField txtCodigo;
    private JComboBox<String> cbTipo;
    private JTextField txtValor;
    private JTextField txtFecha;
    private JTextField txtUsos;


    public DialogoCrearCupon(
            Frame parent,
            GestorCupones gestor) {

        super(parent, "Crear cupón", true);

        this.gestor = gestor;

        setSize(430, 360);
        setLocationRelativeTo(parent);
        setResizable(false);
    }


    public void mostrar() {

        JPanel panel =
                new JPanel(
                        new GridLayout(6, 2, 10, 15)
                );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        25, 25, 25, 25
                )
        );


        txtCodigo = new JTextField();

        cbTipo =
                new JComboBox<>(
                        new String[]{
                                "Porcentaje",
                                "Monto fijo"
                        }
                );

        txtValor = new JTextField();
        txtFecha = new JTextField();
        txtUsos = new JTextField();


        panel.add(new JLabel("Código:"));
        panel.add(txtCodigo);

        panel.add(new JLabel("Tipo:"));
        panel.add(cbTipo);

        panel.add(new JLabel("Valor:"));
        panel.add(txtValor);

        panel.add(
                new JLabel("Vencimiento (dd-MM-yyyy):")
        );
        panel.add(txtFecha);

        panel.add(new JLabel("Máximo de usos:"));
        panel.add(txtUsos);


        JButton btnCrear =
                new JButton("Crear cupón");

        btnCrear.addActionListener(
                e -> crearCupon()
        );


        panel.add(new JLabel());
        panel.add(btnCrear);


        add(panel);

        setVisible(true);
    }


    private void crearCupon() {

        String codigo =
                txtCodigo.getText().trim();

        String tipo =
                cbTipo.getSelectedItem().toString();

        String fecha =
                txtFecha.getText().trim();


        if (codigo.isEmpty()
                || fecha.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Complete todos los campos."
            );

            return;
        }


        try {

            double valor =
                    Double.parseDouble(
                            txtValor.getText().trim()
                    );

            int usos =
                    Integer.parseInt(
                            txtUsos.getText().trim()
                    );


            if (valor <= 0 || usos <= 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "El valor y los usos deben ser mayores a 0."
                );

                return;
            }


            if (tipo.equals("Porcentaje")
                    && valor > 100) {

                JOptionPane.showMessageDialog(
                        this,
                        "El porcentaje no puede superar 100%."
                );

                return;
            }


            Cupon nuevo =
                    new Cupon(
                            codigo,
                            tipo,
                            valor,
                            fecha,
                            usos
                    );


            if (!nuevo.estaVigente()) {

                JOptionPane.showMessageDialog(
                        this,
                        "La fecha es inválida o el cupón ya está vencido."
                );

                return;
            }


            boolean guardado =
                    gestor.guardarCupon(nuevo);


            if (!guardado) {

                JOptionPane.showMessageDialog(
                        this,
                        "Ya existe un cupón con ese código."
                );

                return;
            }


            JOptionPane.showMessageDialog(
                    this,
                    "Cupón creado correctamente."
            );

            dispose();


        } catch (NumberFormatException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Valor o cantidad de usos inválidos."
            );
        }
    }
}