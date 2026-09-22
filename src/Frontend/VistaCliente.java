package Frontend;

import Backend.Inventario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VistaCliente extends BaseFrame {

    public VistaCliente(Inventario inventario) {
        super("Mi Tiendita - Cliente", inventario);
    }

    @Override
    public void mostrar() {

        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(231, 244, 255));

        // Cabecera
        JPanel panelNorte = new JPanel();
        panelNorte.setOpaque(false);
        panelNorte.setBorder(new EmptyBorder(25, 0, 20, 0));

        JLabel lblTitulo = new JLabel("Mi Tiendita");
        lblTitulo.setFont(new Font("SansSerif", Font.BOLD, 28));

        panelNorte.add(lblTitulo);

        add(panelNorte, BorderLayout.NORTH);

        // Área donde después se mostrarán los productos
        JPanel panelProductos = new JPanel();
        panelProductos.setOpaque(false);

        add(panelProductos, BorderLayout.CENTER);

        setVisible(true);
    }
}