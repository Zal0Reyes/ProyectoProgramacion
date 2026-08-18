package Backend;

import Frontend.VistaPrincipalTienda;

import java.util.Scanner;

public class Main {
    // Metodo principal del programa, se ejecuta al iniciar la aplicacion
    public static void main(String[] args) {
        Inventario inventario = new Inventario();

        VistaPrincipalTienda ventanaPrincipal = new VistaPrincipalTienda(inventario);

        ventanaPrincipal.mostrar();
    }
}
