package Frontend;

import Backend.Inventario;
import Backend.Sistema;
import javax.swing.*;

public abstract class BaseFrame extends JFrame {
    protected Inventario inventario;
    protected Sistema sistema; // Nueva variable

    public BaseFrame(String titulo, Inventario inventario, Sistema sistema){
        super(titulo);
        this.inventario = inventario;
        this.sistema = sistema; // Guardar la variable

        setSize(1000,700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    public abstract void mostrar();
}