package Frontend;

import Backend.SistemaTienda;

import javax.swing.*;

public abstract class BaseFrame extends JFrame {
    protected SistemaTienda sistema;

    public BaseFrame(String titulo, SistemaTienda sistema){
        super(titulo);
        this.sistema = sistema;

        setLocationRelativeTo(null); //Centra la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Cierra al salir
    }
    public abstract  void mostrar();
}
