package service;

import exceptions.AppException;
import model.Coche;
import model.Propietario;
import utils.ConexionDB;
import utils.PropertiesReader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConcesionarioService {


    public static void iniciarTablas() throws AppException, SQLException {
        //Creamos una conexion
        Connection connection = ConexionDB.getConnection();

        //Obtenemos el motor que vamos a usar(1. MySql, 2. MySQLite)
        int motor = ConexionDB.getOpcionActual();

        //Dependiendo del motor seleccionado añadimos la sentencia con la sintaxis de cada uno
        String pkAuto = "";
        if (motor == 1) {
            pkAuto = "INT AUTO_INCREMENT PRIMARY KEY";
        } else if (motor == 2) {
            pkAuto = "INTEGER PRIMARY KEY AUTOINCREMENT";
        }

        //Creacion de la tabla Propietarios
        String tablaPropietarios = "CREATE TABLE IF NOT EXISTS propietarios (" +
                "id_propietario " + pkAuto + "," +
                "dni VARCHAR(10) NOT NULL UNIQUE,\n" +
                "nombre VARCHAR(100) NOT NULL,\n" +
                "apellidos VARCHAR(150) NOT NULL,\n" +
                "telefono VARCHAR(15)\n" +
                ");";

        //Creacion de la tabla Coches
        String tablaCoches = "CREATE TABLE IF NOT EXISTS coches (\n" +
                "matricula VARCHAR(10) PRIMARY KEY,\n" +
                "marca VARCHAR(50) NOT NULL,\n" +
                "modelo VARCHAR(50) NOT NULL,\n" +
                "extras VARCHAR(255),\n" +
                "precio DECIMAL(10,2) NOT NULL,\n" +
                "id_propietario INT,\n" +
                "FOREIGN KEY (id_propietario) REFERENCES propietarios(id_propietario)\n" +
                ");";

        //Creacion de la tabla Traspasos
        String tablaTraspasos = "CREATE TABLE IF NOT EXISTS traspasos (\n" +
                "id_traspaso " + pkAuto + "," +
                "matricula_coche VARCHAR(10) NOT NULL,\n" +
                "id_vendedor INT,\n" +
                "id_comprador INT NOT NULL,\n" +
                "monto_economico DECIMAL(10,2) NOT NULL,\n" +
                "FOREIGN KEY (matricula_coche) REFERENCES coches(matricula),\n" +
                "FOREIGN KEY (id_vendedor) REFERENCES propietarios(id_propietario),\n" +
                "FOREIGN KEY (id_comprador) REFERENCES propietarios(id_propietario)\n" +
                ");";

        //Creacion del procedimiento almacenado
        String cochesPorMarca = "CREATE PROCEDURE sp_coches_por_marca(IN p_marca VARCHAR(50)) " +
                "BEGIN " +
                "SELECT * FROM coches WHERE marca = p_marca;" +
                "END";


        //Usamos Statement y try with resources para cerrar el recurso automaticamente
        try (Statement st = connection.createStatement()) {
            //Creamos las tablas
            st.executeUpdate(tablaPropietarios);
            st.executeUpdate(tablaCoches);
            st.executeUpdate(tablaTraspasos);

            //Y si es MySql
            if (motor == 1) {
                //Borramos el procedimiento si ya existia
                st.execute("DROP PROCEDURE IF EXISTS sp_coches_por_marca");
                //Creamos el procedimiento
                st.execute(cochesPorMarca);
            }
        }
    }

    public static void añadirPropietario(Propietario p) throws AppException, SQLException {
        Connection connection = ConexionDB.getConnection();

        String añadirPropietario = "INSERT INTO propietarios(dni, nombre, apellidos, telefono) VALUES (?, ?, ?, ?)";

        try(PreparedStatement preparedStatement = connection.prepareStatement(añadirPropietario)) {
            //Añadimos al nuevo propietario
            preparedStatement.setString(1, p.getDni());
            preparedStatement.setString(2, p.getNombre());
            preparedStatement.setString(3, p.getApellidos());
            preparedStatement.setString(4, p.getTelefono());

            preparedStatement.executeUpdate();
        }
    }

    public static void insertarCoche(Coche c) throws AppException, SQLException {
        Connection conn = ConexionDB.getConnection();

        //Sentencia para insertar un nuevo coche
        String insertarCoche = "INSERT INTO coches(matricula, marca, modelo, extras, precio, id_propietario) VALUES (?, ?, ?, ?, ?, NULL)";

        try (PreparedStatement preparedStatement = conn.prepareStatement(insertarCoche)) {

            //Añadimos el nuevo coche sustituyendo las ? por el valor
            preparedStatement.setString(1, c.getMatricula());
            preparedStatement.setString(2, c.getMarca());
            preparedStatement.setString(3, c.getModelo());
            preparedStatement.setString(4, c.getExtras());
            preparedStatement.setDouble(5, c.getPrecio());


            preparedStatement.executeUpdate();
        }
    }
    public static List<Coche> listarCochesConcesionario() throws AppException, SQLException {
        Connection connection = ConexionDB.getConnection();
        //Consulta que obtiene todos los coches del concesionario
        String consultaListaCoches = "SELECT * FROM coches WHERE id_propietario IS NULL";

        List<Coche> coches = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(consultaListaCoches);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            //Con resultSet.next() comprobamos si esa columna tiene contenido, si tiene devuelve true, si no devuelve false
            while (resultSet.next()) {
                //Creamos coche con los datos de la base de datos
                Coche coche = new Coche(
                        resultSet.getString("matricula"),
                        resultSet.getString("marca"),
                        resultSet.getString("modelo"),
                        resultSet.getString("extras"),
                        resultSet.getDouble("precio"),
                        null
                );
                //Lo añadimos a la lista de coches
                coches.add(coche);
            }

        }
        return coches;
    }
    public static List<String> listarCochesConPropietario() throws AppException, SQLException {
        Connection conn = ConexionDB.getConnection();

        //Creamos una sentencia donde usamos el JOIN uniendo la tabla de propietario con la de coches
        String consultaListaCochesConPropietarios = "SELECT c.*, p.dni FROM coches c INNER JOIN propietarios p ON c.id_propietario = p.id_propietario";

        List<String> listaADevolver = new ArrayList<>();


        try (PreparedStatement ps = conn.prepareStatement(consultaListaCochesConPropietarios);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                //Creamos un String con los datos de la base de datos del coche
                String resultado = "Matricula: " + rs.getString("matricula") +
                        "| Marca: " + rs.getString("marca") +
                        "| Modelo: " + rs.getString("modelo") +
                        "| Precio: " + rs.getDouble("precio") +
                        "| DNI Propietario: " + rs.getString("dni");
                //Añadimos el String a la lista
                listaADevolver.add(resultado);
            }
        }
        return listaADevolver;
    }

    public static boolean modificarCoche(Coche c) throws AppException, SQLException {
        Connection connection = ConexionDB.getConnection();

        //Sentencia encargada de la modificacion del coche, en la que usamos UPDATE para actualizar la tabla,
        //y buscamos por la matricula, que tiene que existir en la base de datos
        String updateCoche = "UPDATE coches SET marca = ?, modelo = ?, extras = ?, precio = ? WHERE matricula = ?";


        try (PreparedStatement preparedStatement = connection.prepareStatement(updateCoche)) {
            preparedStatement.setString(1, c.getMarca());
            preparedStatement.setString(2, c.getModelo());
            preparedStatement.setString(3, c.getExtras());
            preparedStatement.setDouble(4, c.getPrecio());
            preparedStatement.setString(5, c.getMatricula());


            //Ya que executeUpdate devuelve un int, si ese int (que es el numero de filas que se han modificado) es mayor que 0(es decir que se ha modificado correctamente) devuelva true, si no, false
            return preparedStatement.executeUpdate() > 0;
        }
    }

    /**
     * Metodo encargado del borrado de un coche en la base de datos
     *
     * @param matricula matricula del coche a borrar
     * @return devuelve true si se ha podido borrar el coche correctamente, false si no.
     * @throws AppException
     * @throws SQLException
     */
    public static boolean borrarCoche(String matricula) throws AppException, SQLException {
        Connection conn = ConexionDB.getConnection();

        //Sentencia encargada del borrado de un coche donde el coche tenga la matricula pasada por parametro
        String deleteCoche = "DELETE FROM coches WHERE matricula  = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(deleteCoche)) {
            preparedStatement.setString(1, matricula);

            return preparedStatement.executeUpdate() > 0;
        }
    }


    /**
     * Metodo encargado de la importacion desde el fichero CSV
     *
     * @throws AppException
     * @throws SQLException
     */
    public static void importarCsv() throws AppException, SQLException {
        //Usamos el PropertiesReader para obtener desde el config.properties
        String rutaCsv = PropertiesReader.getInstance().get("ruta.csv");
        Connection conn = ConexionDB.getConnection();

        //Desactivo el autocommit, que viene por defecto en true
        conn.setAutoCommit(false);

        //Sentencia encargada de la insertacion de los coches desde el CSV
        String contenidoCSV = "INSERT INTO coches (matricula, marca, modelo, extras, precio, id_propietario) VALUES (?, ?, ?, ?, ?, NULL)";

        //Creamos un BufferedReader para leer el fichero
        try (BufferedReader br = Files.newBufferedReader(Paths.get(rutaCsv), StandardCharsets.UTF_8);
             PreparedStatement preparedStatement = conn.prepareStatement(contenidoCSV)) {

            //Guardamos la cabecera
            String cabecera = br.readLine();

            String linea;

            //Guardamos la linea en un String, y comprobamos que no sea nulo
            while ((linea = br.readLine()) != null) {
                //Si la linea esta vacia (eliminando todos los espacios con .trim())que continue con la siguiente linea
                if (linea.trim().isEmpty()) continue;

                //Separamos los datos de la linea con .split() con el separador ;, guardandolo en un array de String
                String[] datos = linea.split(";");

                //Guardamos lo que hay en cada posicion como corresponde
                preparedStatement.setString(1, datos[0].trim());
                preparedStatement.setString(2, datos[1].trim());
                preparedStatement.setString(3, datos[2].trim());
                preparedStatement.setString(4, datos[3].trim());
                preparedStatement.setDouble(5, Double.parseDouble(datos[4].trim()));

                preparedStatement.executeUpdate();

            }

            //Hacemos commit de todos los cambios, para que o se pasen todos o no se pase ninguno
            conn.commit();


        } catch (IOException | SQLException | NumberFormatException e) {
            //Si falla hacemos un rollback
            conn.rollback();
            throw new AppException("Error al importar el CSV " + e.getMessage());
        } finally {
            //Y usamos el bloque Finally para volver a activar los autocommit
            conn.setAutoCommit(true);
        }

    }

    /**
     * Metodo encargado de realizar un traspaso, es decir cambiar de propietario un coche
     *
     * @param dniComprador   dni del nuevo propietario del vehiculo
     * @param matricula      matricula del coche a traspasar
     * @param montoEconomico cantidad economica del traspaso
     * @throws AppException
     * @throws SQLException
     */
    public static void realizarTraspaso(String dniComprador, String matricula, double montoEconomico) throws AppException, SQLException {
        Connection conn = ConexionDB.getConnection();

        //Sentencias de todas las transacciones necesarias para el traspaso
        String buscarPropietarioPorDNI = "SELECT id_propietario FROM propietarios WHERE dni = ?";
        String buscarCocheYSuPropietarioPorMatricula = "SELECT id_propietario FROM coches WHERE matricula = ?";
        String insertarTraspaso = "INSERT INTO traspasos (matricula_coche, id_vendedor, id_comprador, monto_economico) VALUES (?, ?, ?, ?)";
        String actualizarCoche = "UPDATE coches SET id_propietario = ? WHERE matricula = ?";

        int idComprador;
        Integer idVendedor;

        //Buscamos al comprador
        try (PreparedStatement preparedStatement = conn.prepareStatement(buscarPropietarioPorDNI)) {
            preparedStatement.setString(1, dniComprador);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    throw new AppException("No existe ningun propietario con ese DNI");
                }
                idComprador = rs.getInt("id_propietario");
            }
        }

        //Buscamos el coche y el vendedor
        try (PreparedStatement preparedStatement = conn.prepareStatement(buscarCocheYSuPropietarioPorMatricula)) {
            preparedStatement.setString(1, matricula);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    throw new AppException("No existe un coche con esa matricula");
                }
                idVendedor = (Integer) rs.getObject("id_propietario");

                if (idVendedor != null && idVendedor == idComprador) {
                    throw new AppException("El comprador ya es propietario de este coche");
                }
            }
        }

        //Ponemos el autocommit a false
        conn.setAutoCommit(false);
        //Creamos dos preparedStatement uno encargado de insertar y otro encargado de actualizar el propetario de un coche
        try (PreparedStatement preparedStatementInsert = conn.prepareStatement(insertarTraspaso);
             PreparedStatement preparedStatementUpdate = conn.prepareStatement(actualizarCoche)) {

            //Preparamos e insertamos el registro de la operacion en la tabla traspasos
            preparedStatementInsert.setString(1, matricula);
            if (idVendedor == null) {
                //Si el coche pertenecia al concesionario, no hay vendedor previo y guardamos NULL
                preparedStatementInsert.setNull(2, Types.INTEGER);
            } else {
                preparedStatementInsert.setInt(2, idVendedor);
            }
            preparedStatementInsert.setInt(3, idComprador);
            preparedStatementInsert.setDouble(4, montoEconomico);
            preparedStatementInsert.executeUpdate();

            //Actualizamos la tabla coches, asignando el vehiculo al nuevo propietario
            preparedStatementUpdate.setInt(1, idComprador);
            preparedStatementUpdate.setString(2, matricula);
            preparedStatementUpdate.executeUpdate();

            //Si las operaciones se ejecutan sin errores hacemos commit
            conn.commit();
        } catch (SQLException e) {
            //Si falla cualquier consulta revertimos todos los cambios
            conn.rollback();
            throw new AppException("Error en el traslado " + e.getMessage());
        } finally {
            //Volvemos a activar el autocommit
            conn.setAutoCommit(true);
        }

    }

    /**
     * Metodo encargado de la ejecucion del procedimiento
     *
     * @param marca marca a consultar
     * @return Lista de coches que sean de esa marca
     * @throws AppException
     * @throws SQLException
     */
    public static List<Coche> ejecutarProcedimientoMarca(String marca) throws AppException, SQLException {
        Connection conn = ConexionDB.getConnection();

        List<Coche> coches = new ArrayList<>();

        //Comprobamos que la conexion es de tipo MySql
        if (ConexionDB.getOpcionActual() != 1) {
            throw new AppException("Los procedimientos almacenados solo estan disponibles en MySql");
        }

        //Usamos CallableStatement (en lugar de PreparedStatement) porque vamos a llamar a un procedimiento almacenado de MySQL
        try (CallableStatement cs = conn.prepareCall("{call sp_coches_por_marca(?)}")) {
            //Asignamos el parametro marca
            cs.setString(1, marca);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    //Recorriendo con el ResultSet creamos un objeto de tipo coche con los datos de ese coche en concreto
                    Coche coche = new Coche(
                            rs.getString("matricula"),
                            rs.getString("marca"),
                            rs.getString("modelo"),
                            rs.getString("extras"),
                            rs.getDouble("precio"),
                            (Integer) rs.getObject("id_propietario")
                    );
                    //Y lo añadimos a una lista de coches donde coincidan con esa marca
                    coches.add(coche);
                }
            }

        }
        return coches;
    }

    /**
     * Metodo encargado de generar un informe resumen
     *
     * @throws AppException
     * @throws SQLException
     */
    public static void generarInforme() throws AppException, SQLException {
        Connection conn = ConexionDB.getConnection();
        //Obtenemos la ruta donde se va a guardar el informe desde config.properties
        String rutaInforme = PropertiesReader.getInstance().get("ruta.informe");

        String numCoches = "SELECT COUNT(*) FROM coches";
        String cochesPorMarca = "SELECT marca, COUNT(*) AS cantidad FROM coches GROUP BY marca";
        String extraMasRepetido = "SELECT extras FROM coches WHERE extras IS NOT NULL AND extras != ''";


        int total = 0;

        //Creamos un BufferedWriter con try-with-resources para escribir el fichero en UTF-8
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(rutaInforme), StandardCharsets.UTF_8)) {

            //Consulta usando COUNT para sacar el numero total de coches
            try (PreparedStatement preparedStatement = conn.prepareStatement(numCoches)) {
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    if (!rs.next()) {
                        throw new AppException("No hay coches");
                    }
                    total = rs.getInt(1);
                    bw.write("===Numero Total de Coches===");
                    bw.newLine();
                    bw.write("Total de coches: " + total);
                    bw.newLine();
                }
            }

            //Consulta usando GROUP BY para contar cuantos coches hay de cada marca
            try (PreparedStatement preparedStatement = conn.prepareStatement(cochesPorMarca)) {
                bw.write("===Coches Agrupados Por Marca===");
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    bw.newLine();
                    while (rs.next()) {
                        bw.write(rs.getString("marca") + ": " + rs.getInt("cantidad") + " coche(s)");
                        bw.newLine();
                    }
                }
            }

            //Usamos un HashMap donde la clave es el nombre del extra y el valor las veces que se repite
            Map<String, Integer> conteoExtras = new HashMap<>();
            try (PreparedStatement preparedStatement = conn.prepareStatement(extraMasRepetido)) {
                bw.write("===Extra mas repetido===");
                try (ResultSet rs = preparedStatement.executeQuery()) {
                    while (rs.next()) {
                        String extrasFila = rs.getString("extras");
                        //Separamos cada extra por el caracter | (escapado con \\)
                        for (String extra : extrasFila.split("\\|")) {
                            //Quitamos espacios y lo pasamos a minusculas para que no cuente el mismo extra por separado
                            String limpio = extra.trim().toLowerCase();
                            //Sumamos 1 al contador de ese extra (o empezamos en 0 si es la primera vez que sale)
                            conteoExtras.put(limpio, conteoExtras.getOrDefault(limpio, 0) + 1);
                        }
                    }
                }
            }

            //Recorremos el mapa para buscar cual es el extra que mas veces se ha repetido
            String topExtra = "Ninguno";
            int maxVeces = 0;
            for (Map.Entry<String, Integer> entry : conteoExtras.entrySet()) {
                if (entry.getValue() > maxVeces) {
                    maxVeces = entry.getValue();
                    topExtra = entry.getKey();
                }
            }
            bw.newLine();
            bw.write(topExtra + " (" + maxVeces + " veces)");
            bw.newLine();
        } catch (IOException e) {
            throw new AppException("No se pudo generar el informe " + e.getMessage());
        }

    }

}