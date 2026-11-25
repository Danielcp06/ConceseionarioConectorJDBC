package service;

import com.sun.jdi.connect.spi.Connection;
import exceptions.AppException;
import exceptions.ValorInvalidoException;
import utils.PropertiesReader;

import java.sql.DriverManager;
import java.sql.SQLException;

public class ConcesionarioService {


    public static void Conector(int opcion) throws ValorInvalidoException, AppException {

        String URL = PropertiesReader.getInstance().get("mysql.url");
        String USER = PropertiesReader.getInstance().get("mysql.user");
        String PASSWORD = PropertiesReader.getInstance().get("mysql.password");

        if(opcion == 1){
            try(Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else{

        }
    }
}
