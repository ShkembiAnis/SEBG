package server;

import client.Client;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

import static java.sql.DriverManager.getConnection;

public class PostGre {

    Connection connection;
    public PostGre(){
        String url,name,pass;
        url = "jdbc:postgresql://localhost:5432/SBEG";
        name = "postgres";
        pass = "hazard<3";
        try {
            //Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, name, pass);
            System.out.println("Connection created");
        } catch (SQLException e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }
    }

   /* Connection connection;
    public PostGre(){

        try {
            connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/SBEG",
                    "postgres", "hazard<3");
        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }
    }*/


    public int getIdFromUsername(String username) {
        PreparedStatement user_exst;
        try {
            user_exst = connection.prepareStatement( "SELECT user_id FROM users where username = ?" );
            user_exst.setString(1, username);
            ResultSet rs = user_exst.executeQuery();
            if(rs.next()){
                return rs.getInt("user_id");
            }
            return 0;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }


    public int registerUser(Client user){
        try {
            PreparedStatement user_exst = connection.prepareStatement( "SELECT * FROM users where username = ? " );
            user_exst.setString(1, user.getUsername());
            ResultSet rs = user_exst.executeQuery();
            if(rs.next()){
                return 0;
            }else{
                PreparedStatement st = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
                st.setString(1, user.getUsername());
                st.setString(2, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
                st.executeUpdate();
                //st = connection.prepareStatement("INSERT into score (user_id, wins, loses, draws, coins_spent) values (?, 0, 0, 0, 0)");

                //st.executeUpdate();
                st.close();
                return 1;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public int logInUser(Client user) {
        PreparedStatement stm;
        try {
            PreparedStatement user_exst = connection.prepareStatement( "SELECT password FROM users where username = ?" );
            user_exst.setString(1, user.getUsername());
            ResultSet rs = user_exst.executeQuery();
            if(rs.next()){
                if(BCrypt.checkpw(user.getPassword(), rs.getString("password"))){
                    stm = connection.prepareStatement( "UPDATE users set logged = ? WHERE username = ?" );
                    stm.setBoolean(1, true);
                    stm.setString(2, user.getUsername());
                    int count = stm.executeUpdate();
                    stm.close();
                    if(count > 0) {
                        return 1;
                    }else{
                        return 0;
                    }
                }
            }else{
                return 0;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public boolean isLogged(String username){
        PreparedStatement user_exst;
        try {
            user_exst = connection.prepareStatement( "SELECT logged FROM users where username = ?" );
            user_exst.setString(1, username);
            ResultSet rs = user_exst.executeQuery();
            if(rs.next()){
                return rs.getBoolean("logged");
            }else {
                return false;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}
