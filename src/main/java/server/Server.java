package server;

import client.Client;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Server implements Runnable{

    private final PostGre _db = new PostGre();
    private BufferedReader _in;
    private BufferedWriter _out;
    private StringBuilder _messageSeparator = new StringBuilder();
    private Verb _myVerb = Verb.OTHER;
    private String _message;
    private String _payload;
    private String[] _command;
    private final Map<String, String> __header = new HashMap<>();
    private boolean _http_first_line = true;
    private Battlefield _battle;

    private final String[] _allowedReq = {"users", "sessions", "stats",
            "score", "history", "tournament"};

    public Server() {

    }

    public Server(Socket clientSocket, Battlefield battle) throws IOException {
        this._in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this._out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        this._battle = battle;
        //Thread t = new Thread(this, battle.toString());
        //t.start();
    }

    public void setMyVerb(String myVerb) {
        switch (myVerb) {
            case "GET" -> _myVerb = Verb.GET;
            case "POST" -> _myVerb = Verb.POST;
            case "PUT" -> _myVerb = Verb.PUT;
            case "DELETE" -> _myVerb = Verb.DELETE;
            default -> _myVerb = Verb.OTHER;
        }
    }

    public Verb getMyVerb() {
        return _myVerb;
    }

    private void separateMessage() {
        //separate message
        String[] request = _messageSeparator.toString().split(System.getProperty("line.separator"));
        _messageSeparator = new StringBuilder();
        for (String line : request) {
            if (!line.isEmpty()) {
                if (_http_first_line) {
                    //saving folder and version
                    String[] first_line = line.split(" ");
                    setMyVerb(first_line[0]);
                    _message = first_line[1];
                    _http_first_line = false;
                } else {

                    //saving the header
                    if (line.contains(": ") && !line.contains("{")) {
                        String[] other_lines = line.split(": ");
                        __header.put(other_lines[0], other_lines[1]);
                    }
                    //saving the payload
                    else {
                        _messageSeparator.append(line);
                        _messageSeparator.append("\r\n");
                    }
                }
            }
        }
        _payload = _messageSeparator.toString();
    }


    public void readRequest() throws IOException {
        //read and save request
        //save request
        while (_in.ready()) {
            _messageSeparator.append((char) _in.read());
        }
        separateMessage();
        int status = checkRequest();
        performRequest(status);
    }


    private int checkRequest(){
        if (_myVerb == Verb.OTHER) {
            System.out.println("srv: Request method not supported");
        } else {
            _command = _message.split("/");
            if(Arrays.asList(_allowedReq).contains(_command[1])) {
                if (_command[1].equals("users") && _myVerb == Verb.POST) {
                    return 1;
                }else if (_command[1].equals("sessions") && _myVerb == Verb.POST) {
                    return 2;
                }else if (_command[1].contains("users") && _myVerb == Verb.GET) {
                    return 3;
                }else if(_command[1].contains("users") && _myVerb == Verb.PUT){
                    return 5;
                }else if(_command[1].equals("stats") && _myVerb == Verb.GET){
                    return 6;
                }else if(_command[1].equals("score") && _myVerb == Verb.GET){
                    return 7;
                }else if(_command[1].equals("history") && _myVerb == Verb.GET){
                    return 8;
                }else if(_command[1].equals("tournament") && _myVerb == Verb.GET){
                    return 9;
                }else if(_command[1].equals("history") && _myVerb == Verb.POST){
                    return 10;
                }
//                if (_command.length == 3) {
//                    if (_command[1].equals("transactions") && _command[2].equals("packages")) {
//                        if (_myVerb == Verb.POST) {
//                            return 4;
//                        }
//                    } else {
//                        return 0;
//                    }
//                }
            }
        }
        return 0;
    }


        private void performRequest(int status) throws IOException{
            if (status == 0) {
                _out.write("HTTP/1.1 400\r\n");
                _out.write("Content-Type: text/html\r\n");
                _out.write("\r\n");
                _out.write("Bad request!\r\n");
            } else {
                _out.write("HTTP/1.1 200 OK\r\n");
                _out.write("Content-Type: text/html\r\n");
                _out.write("\r\n");
            }
            switch (status) {
                case 1 -> createUser(_payload);
                case 2 -> logInUser(_payload);
                case 3 -> getUser();
                case 5 -> editUser();
                case 6 -> getStats();
                case 7 -> getScoreboard();
                case 8 -> showHistory();
                case 9 -> tourinfo();
                case 10 -> addEntry(_payload);
            }
            _out.flush();
        }

    private void getUser() throws IOException {
        if(getUserInfoHeader() != null){
            String[] uname = getUserInfoHeader();
            if(isUserValid(uname[0], uname[1])){
                String user = _db.getUser(uname[0]);
                _out.write(user);
                Server.log(user + "\r\n");
            }else{
                _out.write("User is not valid");
                Server.log("User is not valid\r\n");
            }
        }else{
            _out.write("No user entered.");
            Server.log("No user entered.\r\n");
        }
    }

    private void addEntry(String json) throws IOException {
        if(getUserInfoHeader() != null){
            String[] uname = getUserInfoHeader();
            if(isUserValid(uname[0], uname[1])){

                JSONObject _jsonUser = new JSONObject(json);
                String type = _jsonUser.getString("Name");
                int count = _jsonUser.getInt("Count");
                int duration = _jsonUser.getInt("DurationInSeconds");
                Client user = new Client();
                user.set_username(uname[0]);
                if(_battle.addPlayer(uname[0], type, count, duration) == 1){
                    _out.write("User added to the tournament. Waiting for opponent\n");
                    Server.log("User added to the tournament. Waiting for opponent\r\n");
                }else if(_battle.addPlayer(uname[0], type, count, duration) == 2){
                    _out.write("User added to the tournament. Battle starts\n");
                    Server.log("User added to the tournament. Battle starts\r\n");
                }else if(_battle.addPlayer(uname[0], type, count, duration) == 3){
                    _out.write("User cannot enter twice\n");
                    Server.log("User cannot enter twice\r\n");
                } else{
                    _out.write("Tournament is taking place. Please wait\n");
                    Server.log("Tournament is taking place. Please wait\r\n");
                    String outcome = _battle.start();
                    _out.write(outcome);
                    Server.log(outcome + "\r\n");
                }

            } else {
                _out.write("Scoreboard cannot be shown");
                Server.log("Scoreboard cannot be shown\r\n");
            }
        }
    }


    private void createUser(String json) throws IOException {
        Client user = new Client(json);
        if (_db.registerUser(user) == 1) {
            _out.write("New user is created\n");
            Server.log("New user is created\r\n");
        } else {
            _out.write("Username already exists\n");
            Server.log("Username already exists\r\n");
        }
    }

    private void logInUser(String json) throws IOException {
        Client user = new Client(json);
        _db.logInUser(user);
        if (_db.logInUser(user) == 0) {
            _out.write("Can't log user in\n");
            Server.log("Can't log user in\r\n");
        } else {
            _out.write("User is logged.\n");
            Server.log("User is logged.\r\n");
        }
    }



    private void editUser() throws IOException {
        if(getUserInfoHeader() != null){
            String[] uname = getUserInfoHeader();
            if(isUserValid(uname[0], uname[1]) && _command[2].equals(uname[0]) ){

                if(_db.editUser(_payload, uname[0])){
                    _out.write("User updated");
                    Server.log("User updated\r\n");
                }else{
                    _out.write("Something went wrong");
                    Server.log("Something went wrong\r\n");
                }
            }else{
                _out.write("User is not valid");
                Server.log("User is not valid\r\n");
            }
        }else{
            _out.write("No user entered.");
            Server.log("No user entered.\r\n");
        }
    }


    private void getStats() throws IOException {
        if(getUserInfoHeader() != null){
            String[] uname = getUserInfoHeader();
            if(isUserValid(uname[0], uname[1])){
                String stats = _db.getStats(uname[0]);
                _out.write(stats);
                Server.log(stats);
            }else{
                _out.write("User is not valid");
                Server.log("User is not valid\r\n");
            }
        }else{
            _out.write("No user entered.");
            Server.log("No user entered.\r\n");
        }
    }

    private void getScoreboard() throws IOException{
        if(getUserInfoHeader() != null){
            String[] uname = getUserInfoHeader();
            if(isUserValid(uname[0], uname[1])){
                String score = _db.getScoreboard();
                _out.write(score);
            } else {
                _out.write("Scoreboard cannot be shown");
                Server.log("Scoreboard cannot be shown\r\n");
            }
        }
    }

    private void showHistory() throws IOException{
        if(getUserInfoHeader() != null){
            String[] uname = getUserInfoHeader();
            if(isUserValid(uname[0], uname[1])){
                String history = _db.showHistory(uname[0]);
                _out.write(history);
                Server.log(history);
            } else {
                _out.write("History cannot be shown");
                Server.log("History cannot be shown\r\n");
            }
        }
    }

    private void tourinfo() throws IOException{
        if(getUserInfoHeader() != null){
            String[] uname = getUserInfoHeader();
            if(isUserValid(uname[0], uname[1])){
                String tourinfo = _db.tourinfo(uname[0]);
                _out.write(tourinfo);
                Server.log(tourinfo);
            } else {
                _out.write("History cannot be shown");
                Server.log("History cannot be shown\r\n");
            }
        }
    }





    public static void log(String msg) {
        File file = new File("log.txt");

        // creates the file
        try {
            file.createNewFile();
            // creates a FileWriter Object
            FileWriter writer = new FileWriter(file, true);

            // Writes the content to the file
            writer.write("logged: " + msg + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private String[] getUserInfoHeader() {
        if(__header.get("Authorization") != null){
            String[] token = __header.get("Authorization").split(" ");
            return token[1].split("-");
        }
        return null;

    }

    private boolean isUserValid(String username, String token) {
        if (_db.isLogged(username)) {
            return token.contains("sebToken");

        } else {
            return false;
        }
    }


















    public void run() {

    }
}
