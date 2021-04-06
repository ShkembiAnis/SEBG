package server;

import client.Client;
import client.Push_Up_History;
import org.json.JSONObject;
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

    ////////////////////////////////// Register User ///////////////////////////////////////
    public int registerUser(Client user){
        try {
            PreparedStatement user_exst = connection.prepareStatement( "SELECT * FROM users where username = ? " );
            user_exst.setString(1, user.getUsername());
            ResultSet rs = user_exst.executeQuery();
            if(rs.next()){
                return 0;
            }else{
                PreparedStatement st = connection.prepareStatement("INSERT INTO users (username, password, name) VALUES (?, ?, ?)");
                st.setString(1, user.getUsername());
                st.setString(2, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
                st.setString(3, user.getUsername());
                st.executeUpdate();
                st.close();
                return 1;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }






    ////////////////////////////////// Login User ///////////////////////////////////////
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



    ////////////////////////////////// Is User Logged ///////////////////////////////////////
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




    ////////////////////////////////// Edit User ///////////////////////////////////////
    public boolean editUser(String json, String username) {
        JSONObject user = new JSONObject(json);
        try {
            PreparedStatement st = connection.prepareStatement("update users set name = ?, bio = ?, img = ?  where username = ?");
            st.setString(1, user.getString("Name"));
            st.setString(2, user.getString("Bio"));
            st.setString(3, user.getString("Image"));
            st.setString(4, username);
            int count = st.executeUpdate();
            if(count > 0){
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }




    ////////////////////////////////// Get User Stats ///////////////////////////////////////
    public String getStats(String username) {
        try {
            PreparedStatement st = connection.prepareStatement("select username, bio, img, name, push_ups from users as u join push_up_history" +
                    " as psh on u.user_id = psh.user_id where username = ?");
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            Client user = new Client();
            Push_Up_History push = new Push_Up_History();

            if(!rs.next()){
                user.set_username(username);
                return user.getStats();
            }else{
                while(rs.next())
                {
                    user = new Client(rs.getString("username"), rs.getString("bio"),
                            rs.getString("img"), rs.getString("name"),
                            rs.getInt("push_ups"));
                    user.setElo(rs.getInt("elo"));
                    push.setPush_ups(rs.getInt("push_ups"));
                }
            }
            return user.getStats();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }


        public String getScoreboard(String username) {
            try {
                PreparedStatement st = connection.prepareStatement("select username, elo, wins, losses, draws from users");
//                st.setString(1, username);
                ResultSet rs = st.executeQuery();
                StringBuilder scoreB = new StringBuilder("Scoreboard:\n");
                while(rs.next())
                {
                    scoreB.append("\n\tName: ").append(rs.getString("username"));
                    scoreB.append("\n\tElo: ").append(rs.getInt("elo"));
                    scoreB.append("\n\tWins: ").append(rs.getString("wins"));
                    scoreB.append("\n\tLoses: ").append(rs.getString("losses"));
                    scoreB.append("\n\tDraws: ").append(rs.getString("draws"));

                }
                int id = getIdFromUsername(username);

                PreparedStatement st1 = connection.prepareStatement("select (psh.user_id, push_ups) from push_up_history as psh " +
                        "join users as u on psh.user_id = u.user_id where u.user_id = ?");
                st1.setInt(1, id);
                ResultSet resset = st1.executeQuery();
                while(rs.next()) {
                    scoreB.append("\n\tPush ups: \n").append(resset.getInt("push_ups"));
                }
                //scoreB.append("\n\tPush ups: ").append(rs.getString("push_ups"));
                return scoreB.toString();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return null;
        }









    ////////////////////////////////// User History ///////////////////////////////////////
    public String showHistory(String username){
        try {
            PreparedStatement st = connection.prepareStatement( "SELECT username FROM users" );
            //user_exst.setInt(1, user.(getIdFromUsername()));
            ResultSet rs = st.executeQuery();
            StringBuilder scoreB = new StringBuilder("History:\n");
            while(rs.next())
            {
                scoreB.append("\n\tUsername: ").append(rs.getString("username"));

            }
            int id = getIdFromUsername(username);
            PreparedStatement st1 = connection.prepareStatement("select (entry_id, push_ups, duration_exercise, psh.user_id) " +
                    "from push_up_history as psh " +
                    "join users as u on psh.user_id = u.user_id where u.user_id = ?");
            st1.setInt(1, id);
            ResultSet resset = st1.executeQuery();
            while(rs.next()) {
                scoreB.append("\n\tEntry: \n").append(resset.getInt("entry_id"));
                scoreB.append("\n\tPush ups: \n").append(resset.getInt("push_ups"));
                scoreB.append("\n\tDuration per Entry: \n").append(resset.getInt("duration_exercise"));
            }
            return scoreB.toString();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }


    ////////////////////////////////// Tour Info ///////////////////////////////////////
    public String tourinfo(String username){
        try {
            PreparedStatement st = connection.prepareStatement( "SELECT username FROM users" );
            ResultSet rs = st.executeQuery();
            StringBuilder scoreB = new StringBuilder("Tournament Info:\n");
            while(rs.next())
            {
                scoreB.append("\n\tParticipant: ").append(rs.getString("username"));

            }
            int id = getIdFromUsername(username);
            PreparedStatement st1 = connection.prepareStatement("select (tour_id, total_push_ups, active, t.participant_id) " +
                    "from tournament as t " +
                    "join users as u on t.participant_id = u.user_id where t.participant_id = ?");
            st1.setInt(1, id);
            ResultSet resset = st1.executeQuery();
            while(rs.next()) {
                scoreB.append("\n\tTour ID \n").append(resset.getInt("tour_id"));
                scoreB.append("\n\tTotal Push ups: \n").append(resset.getInt("total_push_ups"));
                scoreB.append("\n\tActive Tournament: \n").append(resset.getInt("active"));
                scoreB.append("\n\tParticipant ID: \n").append(resset.getInt("participant_id"));
            }
            return scoreB.toString();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
















}
