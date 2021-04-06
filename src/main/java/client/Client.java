package client;

import org.json.JSONObject;

public class Client {
    private String _username;
    private String _password;
    private String _name;
    private int _elo;
    private boolean _logged;
    private final String _bio;
    private final String _img;
    private int _push_ups;
    private Push_Up_History push;


    public Client() {
        _username = "Filan";
        _bio = "sup";
        _img = "football";
    }

    public Client(String json){
        this();
        JSONObject _jsonUser = new JSONObject(json);
        _username = _jsonUser.getString("Username");
        _name = _username;
        _password = _jsonUser.getString("Password");
    }

    public Client(String username, String bio, String img, String name, int push_ups){
        _username = username;
        _bio = bio;
        _img = img;
        _name = name;
        _push_ups = push_ups;
    }

    public void set_username(String _username) {
        this._username = _username;
    }

    public int getElo() {
        return _elo;
    }

    public void setElo(int elo) {
        this._elo = elo;
    }

    public boolean isLogged() { return _logged; }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    public String getBio() {
        return _bio;
    }

    public String getImg() {
        return _img;
    }

//    public int getPush_ups() {
//        return _push_ups;
//    }
//
//    public void setPush_ups(int push_ups) {this._push_ups = push_ups;}

    public void showStats(){
        System.out.println("Users profile:");
        System.out.println("\r\tUsername: " + _username);
        System.out.println("\r\tElo: " + _elo);
        System.out.println("\r\tBio: " + _bio);
        System.out.println("\r\tImg: " + _img);
        System.out.println("\r\tPush Ups: " + _push_ups);
    }

    public String getStats(){
        showStats();
        return "Users stats:\n" + "\n\tName: " + _username +
                "\n\tElo: " + _elo +
                "\n\tBio: " + _bio +
                "\n\tImg: " + _img +
                "\n\tPush ups: " + _push_ups + "\n";
    }

    public String getUserDate(){
        return "Users data:\n" + "\n\tName: " + _name +
                "\n\tBio: " + _bio +
                "\n\tImg: " + _img +
                "\n\tPush ups: " + _push_ups + "\n";
    }

    public String getName() {
        return _name;
    }


    public String getUser() {
        return "Users data:\n" + "\n\tName: " + _name +
                "\n\tBio: " + _bio +
                "\n\tImg: " + _img + "\n";
    }
}
