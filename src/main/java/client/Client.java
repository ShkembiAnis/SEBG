package client;

import org.json.JSONObject;

public class Client {
    private String _username;
    private String _password;
    private int _elo;
    private boolean _logged;


    public Client() {
    }

    public Client(String json){
        this();
        JSONObject _jsonUser = new JSONObject(json);
        _username = _jsonUser.getString("Username");
        _password = _jsonUser.getString("Password");
    }


    public int getElo() {
        return _elo;
    }

    public void setElo(int elo) {
        this._elo = elo;
    }

    public boolean isLogged() {
        return _logged;
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public void showStats(){
        System.out.println("Users profile:");
        System.out.println("\r\tUsername: " + _username);
        System.out.println("\r\tElo: " + _elo);
    }



}
