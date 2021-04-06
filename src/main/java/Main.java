import server.Battlefield;
import server.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static Server server = new Server();
    private static Battlefield battle = new Battlefield();
    static ServerSocket _sSocket = null;
    static final int _port = 10001;


    public static void main(String[] args) throws IOException {
        System.out.println("srv: Starting server...");
        Server.log("srv: Starting server...\r\n");

        _sSocket = new ServerSocket(_port);
        System.out.println("srv: Server is running in port " + _port);
        Server.log("srv: Server is running in port " + _port + "\r\n");

        while (true) {
            // connect to client
            Socket clientSocket = _sSocket.accept();
            System.out.println("srv: New client");
            server = new Server(clientSocket, battle);
            server.readRequest();
            //close the client socket
            clientSocket.close();

        }
    }
}
