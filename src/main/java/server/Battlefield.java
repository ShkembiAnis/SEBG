package server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Battlefield {
    private String _username1, _username2, _type1, _type2;
    private int _count1, _count2, _duration1, _duration2;
    private final PostGre _db = new PostGre();
    private int player1, player2;
    private final StringBuilder _msg = new StringBuilder();

    public Battlefield(){
        player1 = 0;
        player2 = 0;
    }


    public int addPlayer(String username, String type, int count, int duration) {
        if(player1 == 0){
            player1 = 1;
            _username1 = username;
            _count1 = count;
            _duration1 = duration;
            _type1 = type;
            return 1;
        }else if(player2 == 0){
            if(username.equals(_username1)){
                return 3;
            }else {
                player2 = 1;
                _username2 = username;
                _count2 = count;
                _duration2 = duration;
                _type2 = type;
                return 2;
            }
        }
        return 0;
    }

    public String start() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        _msg.append("Time when started: ").append(dtf.format(now)).append("\n");
        if(sameType()){
            _db.addEntry(_type1, _duration1, _count1, _username1);
            _db.addEntry(_type2, _duration2, _count2, _username2);
            if(_count1 > _count2){
                _db.addWin(_username1);
                _db.addLoss(_username2);
                _msg.append("Winner is ").append(_username1).append("\n");
                return _msg.toString();
            }else if(_count2 > _count1){
                _db.addWin(_username2);
                _db.addLoss(_username1);
                _msg.append("Winner is ").append(_username2).append("\n");
                return _msg.toString();
            }else{
                _db.addDraw(_username1);
                _db.addDraw(_username2);
                _msg.append("Draw between ").append(_username1).append(" and ").append(_username2).append("\n");
                return _msg.toString();
            }
        }else{
            _msg.append("Exercises are of different types" + "\n");
            return _msg.toString();
        }
    }

    public boolean sameType(){
        return _type1.equals(_type2);
    }
}
