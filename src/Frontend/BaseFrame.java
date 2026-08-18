package Frontend;

import Backend.Inventario;

import javax.swing.*;

public abstract class BaseFrame extends JFrame {
    protected Inventario sistema;

    public BaseFrame(String titulo, Inventario sistema){
        super(titulo);
        this.sistema = sistema;

        setSize(1000,700);
        setLocationRelativeTo(null); //Centra la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Cierra al salir
    }
    public abstract  void mostrar();
}
