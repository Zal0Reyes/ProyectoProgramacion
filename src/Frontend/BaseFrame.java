package Frontend;

import Backend.Inventario;

import javax.swing.*;

public abstract class BaseFrame extends JFrame {
    protected Inventario inventario;

    public BaseFrame(String titulo, Inventario inventario){
        super(titulo);
        this.inventario = inventario;

        setSize(1000,700);
        setLocationRelativeTo(null); //Centra la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Cierra al salir
    }
    public abstract  void mostrar();
}
