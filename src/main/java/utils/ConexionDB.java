package utils;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConexionDB {
    private static Connection conexion = null;

    // Privado para evitar instanciación externa (Patrón Singleton)
    private ConexionDB() {}

    /**
     * Devuelve una conexión activa a la base de datos.
     */
    public static Connection getConnection() throws Exception {
        PropertiesReader p = PropertiesReader.getInstance();

        // Si la conexión no existe o está cerrada, creamos una nueva
        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(p.get("url"), p.get("usuario"), p.get("password"));
            if ()
        }
        return conexion;

    }

    public static void configurarConexion(int opcion){

        if ()

    }
}