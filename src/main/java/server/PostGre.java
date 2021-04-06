package server;

import client.Client;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

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
            PreparedStatement st = connection.prepareStatement("select username, bio, img, push_ups, elo from users as u join push_up_history as psh on u.user_id = psh.user_id where username = ?");
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            StringBuilder scoreB = new StringBuilder("Stats:\n");
            boolean done = false;
            int count = 0;
            while(rs.next())
            {
                if(!done){
                    scoreB.append("\n\tUsername: ").append(rs.getString("username"));
                    scoreB.append("\n\tBio: ").append(rs.getString("bio"));
                    scoreB.append("\n\tImg: ").append(rs.getString("img"));
                    scoreB.append("\n\tElo: ").append(rs.getString("elo"));

                    done = true;
                }
                int id = getIdFromUsername(rs.getString("username"));
                PreparedStatement st1 = connection.prepareStatement("select push_ups from push_up_history where user_id = ?");
                st1.setInt(1, id);
                ResultSet rs2 = st1.executeQuery();
                count = 0;
                while(rs2.next()) {
                    count += rs2.getInt("push_ups");
                }

            }
            scoreB.append("\n\tPush ups: ").append(count);
            return scoreB.toString();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }


        public String getScoreboard() {
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
                    int id = getIdFromUsername(rs.getString("username"));
                    PreparedStatement st1 = connection.prepareStatement("select psh.user_id, psh.push_ups from push_up_history as psh " +
                            "join users as u on psh.user_id = u.user_id where u.user_id = ?");
                    st1.setInt(1, id);
                    ResultSet rs2 = st1.executeQuery();
                    int count = 0;
                    while(rs2.next()) {
                        count += rs2.getInt("push_ups");
                    }
                    scoreB.append("\n\tPush ups: ").append(count);
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


    public void addEntry(String type1, int duration1, int count1, String username1) {
        try {
            PreparedStatement st = connection.prepareStatement("INSERT INTO push_up_history (push_ups, duration_exercise, user_id, type) VALUES (?, ?, ?, ?)");
            st.setInt(1, count1);
            st.setInt(2, duration1);
            st.setInt(3, getIdFromUsername(username1));
            st.setString(4, type1);
            st.executeUpdate();
            st.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public int addWin(String username1) {
        try {
            if(getIdFromUsername(username1) != 0){
                PreparedStatement st = connection.prepareStatement("update users set wins = wins+1, elo = elo + 2 where username = ?");
                st.setString(1, username1);
                st.executeUpdate();
                st.close();
                return 1;
            }else{
                return 0;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public int addLoss(String username1) {
        try {
            if(getIdFromUsername(username1) != 0){
                PreparedStatement st = connection.prepareStatement("update users set losses = losses+1, elo = elo -1 where username = ?");
                st.setString(1, username1);
                st.executeUpdate();
                st.close();
                return 1;
            }else{
                return 0;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public int addDraw(String username) {
        try {
            if(getIdFromUsername(username) != 0){
                PreparedStatement st = connection.prepareStatement("update users set draws = draws+1, elo = elo +1 where username = ?");
                st.setString(1, username);
                st.executeUpdate();
                st.close();
                return 1;
            }else{
                return 0;
            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return 0;
    }

    public String getUser(String username) {
        try {
            PreparedStatement st = connection.prepareStatement("select username, bio, img from users where username = ?");
            st.setString(1, username);
            ResultSet rs = st.executeQuery();
            Client user = new Client();
            if(!rs.next()){
                user.set_username(username);
                return user.getUser();
            }else{
                while(rs.next())
                {
                    user = new Client(rs.getString("username"), rs.getString("bio"),
                            rs.getString("img"), rs.getString("username"), 0);
                }
            }
            return user.getUser();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public void deleteAll(){
        try {
            PreparedStatement st = connection.prepareStatement("DELETE FROM push_up_history");
            st.executeUpdate();
            st = connection.prepareStatement("DELETE FROM tournament");
            st.executeUpdate();
            st = connection.prepareStatement("DELETE FROM users");
            st.executeUpdate();

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
