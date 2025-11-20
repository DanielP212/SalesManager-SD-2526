import server.Server;

public class ServerMain {


    static void main() {
        Server server = new Server(12345);
        server.run();
    }
}
