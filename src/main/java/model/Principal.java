package model;

import utils.MiEntradaSalida;

public class Principal {

    static void main(String[] args) {
        boolean usandoBaseDatos = true;

        while (usandoBaseDatos){
            mostrarMenu();
            int eleccion = MiEntradaSalida.leerEnteroPositivo("Elige una opcion: ", true);
            switch (eleccion){
                case 1:

                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    break;
                case 7:
                    break;
                case 8:
                    break;
                case 9:
                    break;
                case 10:
                    break;
                case 11:
                    break;
                case 12:
                    break;
                case 0:
                    System.out.println("Saliendo del sistema...");
                    usandoBaseDatos = false;
                    break;
                default:
                    System.out.print("La eleccion no es correcta");
                    break;
            }

        }
        System.out.println("Sistema cerrado");
    }

    private static void mostrarMenu() {
        System.out.println("\n================ GESTIÓN DE CONCESIONARIO ================");
        System.out.println(" 1. Conectar BBDD (MySQL / SQLite)");
        System.out.println(" 2. Inicializar / Crear Tablas");
        System.out.println(" 3. Registrar Propietario");
        System.out.println(" 4. Insertar Coche");
        System.out.println(" 5. Importar Coches (CSV)");
        System.out.println(" 6. Listar Coches (Concesionario - Sin propietario)");
        System.out.println(" 7. Listar Coches (Propietarios)");
        System.out.println(" 8. Modificar Coche");
        System.out.println(" 9. Borrar Coche");
        System.out.println("10. Realizar Traspaso");
        System.out.println("11. Ejecutar Procedimiento Almacenado (Solo MySQL)");
        System.out.println("12. Generar Informe Resumen");
        System.out.println(" 0. Salir");
        System.out.println("==========================================================");
    }
}
