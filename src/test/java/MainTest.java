import client.Client;
import org.junit.jupiter.api.Test;
import server.Battlefield;
import server.PostGre;
import server.Server;
import server.Verb;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @Test
    void cleanAll(){
        PostGre db = new PostGre();
        db.deleteAll();
    }

    @Test
    void VerbTest(){
        Server sr = new Server();
        sr.setMyVerb("GET");
        assertEquals(Verb.GET, sr.getMyVerb());
        sr.setMyVerb("POST");
        assertNotEquals(Verb.GET, sr.getMyVerb());
    }
    @Test
    void addUserTest(){
        PostGre db = new PostGre();
        Client user = new Client();
        db.registerUser(user);
        assertNotEquals(0, db.getIdFromUsername(user.getUsername()));
    }

    @Test
    void userLoggedTest(){
        PostGre db = new PostGre();
        Client user = new Client();
        user.set_username("Anis");
        db.registerUser(user);
        db.logInUser(user);
        assertTrue(db.isLogged(user.getUsername()));
    }

    @Test
    void addPlayerTest(){
        Battlefield battle = new Battlefield();
        assertEquals(1, battle.addPlayer("test1", "push ups", 30, 30));
        assertEquals(3, battle.addPlayer("test1", "push ups", 30, 30));
        assertEquals(2, battle.addPlayer("test2", "push ups", 30, 30));
    }

    @Test
    void addWinTest(){
        PostGre db = new PostGre();
        Client user = new Client();
        user.set_username("Win");
        db.registerUser(user);
        db.logInUser(user);
        assertEquals(1, db.addWin(user.getUsername()));
        assertEquals(0, db.addWin("No username"));
    }

    @Test
    void addLossTest(){
        PostGre db = new PostGre();
        Client user = new Client();
        user.set_username("Loss");
        db.registerUser(user);
        db.logInUser(user);
        assertEquals(1, db.addLoss(user.getUsername()));
        assertEquals(0, db.addLoss("No username"));
    }

    @Test
    void addDrawTest(){
        PostGre db = new PostGre();
        Client user = new Client();
        user.set_username("Draw");
        db.registerUser(user);
        db.logInUser(user);
        assertEquals(1, db.addDraw(user.getUsername()));
        assertEquals(0, db.addDraw("No username"));
    }


}
