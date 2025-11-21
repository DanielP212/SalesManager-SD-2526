package server;

import core.AuthenticationHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static int CLIENT_ID_COUNTER = 1;

    private final ServerSocket socket;
    private static final List<ClientHandlerThread> aliveClients = new ArrayList<>();

    public static final AuthenticationHandler authHandler = new AuthenticationHandler();

    public Server(int port){
        try {
            socket = new ServerSocket(12345);
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Implementar a classe Connection
    public void run(){
        while(true){
            try {
                // Iniciar uma nova thread por cada conexão do cliente
                Socket clientSocket = socket.accept();
                System.out.println("Connected with client");
                ClientHandlerThread clientHandler = new ClientHandlerThread(CLIENT_ID_COUNTER++, clientSocket);
                clientHandler.start();
                aliveClients.add(clientHandler);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Inicializacao das coisas para testes
    public void init(){
        authHandler.registerUser("mamaco", "preto");
    }

    public static void stopTracking(ClientHandlerThread c){
        aliveClients.remove(c);
    }
}
