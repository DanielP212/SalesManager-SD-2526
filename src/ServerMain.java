import server.Server;

public class ServerMain {


    public static void main(String[] args) {
        Server server = new Server(12345, 5, 6);
        server.run();
    }
}
