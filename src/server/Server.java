package server;

import comms.Packet;
import core.AuthenticationHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static int CLIENT_ID_COUNTER = 0;

    private final ServerSocket socket;
    private final List<ClientHandlerThread> clients = new ArrayList<>();

    private static final AuthenticationHandler authHandler = new AuthenticationHandler();

    public Server(int port){
        try {
            socket = new ServerSocket(12345);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void run(){
        while(true){
            try {
                // Iniciar uma nova thread por cada conexão do cliente
                Socket clientSocket = socket.accept();
                System.out.println("Connected with client");
                ClientHandlerThread clientHandler = new ClientHandlerThread(clientSocket);
                clientHandler.start();
                clients.add(clientHandler);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void handleAuthentication(Packet p){ authHandler.handle(p); }

}
