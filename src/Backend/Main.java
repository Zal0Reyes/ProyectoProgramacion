package Backend;

import Frontend.VistaCliente;


public class Main {
    // Metodo principal del programa, se ejecuta al iniciar la aplicacion
    public static void main(String[] args) {
        Inventario inventario = new Inventario();
        Sistema sistema = new Sistema();

        VistaCliente vistaCliente = new VistaCliente(inventario, sistema);
        vistaCliente.mostrar();
    }
}
