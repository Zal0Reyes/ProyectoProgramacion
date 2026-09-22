package Frontend;

import Backend.Inventario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VistaLogin extends JFrame{

    public VistaLogin() {
        super("Iniciar Sesión");

        setSize(300,300);
        setLocationRelativeTo(null); //Centra la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void mostrar() {

        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(231, 244, 255));

        // Cabecera
        JPanel panelNorte = new JPanel();
        panelNorte.setOpaque(false);
        panelNorte.setBorder(new EmptyBorder(25, 0, 20, 0));

        JLabel lblTitulo = new JLabel("Iniciar Sesión");
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