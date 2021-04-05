package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PostGre {
    Connection connection;
    public PostGre(){
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/SBEG",
                    "postgres", "hazard<3");
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
    }
}
