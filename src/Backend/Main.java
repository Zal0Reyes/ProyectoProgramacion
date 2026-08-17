package Backend;

import java.util.Scanner;

public class Main {
    // Metodo principal del programa, se ejecuta al iniciar la aplicacion
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Inventario inventario = new Inventario();

        boolean salir = false;

        while (!salir) {
            mostrarMenu();
            System.out.print("Seleccione una opcion: ");
            int opcion = pedirEntero(scanner);

            if (opcion == 1) {
                crearProducto(scanner, inventario);
            } else if (opcion == 2) {
                System.out.println("Opcion no disponible aun.");
            } else if (opcion == 3) {
                System.out.println("Opcion no disponible aun.");
            } else if (opcion == 4) {
                System.out.println("Opcion no disponible aun.");
            } else if (opcion == 5) {
                System.out.println("Saliendo...");
                salir = true;
            } else {
                System.out.println("Opcion invalida.");
            }
        }

        scanner.close();
    }

    // Muestra las opciones del menu principal de la tienda
    public static void mostrarMenu() {
        System.out.println();
        System.out.println("=== MENU INVENTARIO ===");
        System.out.println("1. Crear producto");
        System.out.println("2. Ver informacion");
        System.out.println("3. Modificar informacion");
        System.out.println("4. Eliminar producto");
        System.out.println("5. Salir");
    }

    // Solicita los datos de un producto y lo agrega al inventario
    public static void crearProducto(Scanner scanner, Inventario inventario) {
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();

        System.out.print("Precio: ");
        double precio = pedirDouble(scanner);

        System.out.print("Stock: ");
        int stock = pedirEntero(scanner);

        System.out.print("Categoria: ");
        String categoria = scanner.nextLine();

        inventario.registrarProducto(nombre, precio, stock, categoria);

    }

    // Lee un numero entero desde teclado y valida que sea correcto
    public static int pedirEntero(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.print("Ingrese un numero valido: ");
            scanner.nextLine();
        }

        int numero = scanner.nextInt();
        scanner.nextLine();
        return numero;
    }

    // Lee un numero decimal desde teclado y valida que sea correcto
    public static double pedirDouble(Scanner scanner) {
        while (!scanner.hasNextDouble()) {
            System.out.print("Ingrese un valor valido: ");
            scanner.nextLine();
        }

        double numero = scanner.nextDouble();
        scanner.nextLine();
        return numero;
    }
}
