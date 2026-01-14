import server.Server;

public class ServerMain {


    public static void main(String[] args) {
        Server server = new Server(12345, 50, 90);
        server.run();
    }
}
