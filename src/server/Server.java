package server;

import core.AuthenticationManager;
import core.SalesManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable{
    private static int CLIENT_ID_COUNTER = 1;

    private final ServerSocket socket;
    private static final List<ClientHandlerThread> aliveClients = new ArrayList<>();

    public static final AuthenticationManager authHandler = new AuthenticationManager();
    private SalesManager salesManager;

    public Server(int port, int s, int d){
        try {
            socket = new ServerSocket(12345);
            this.salesManager = new SalesManager(s, d);
            init();
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
        authHandler.registerUser("mamaco", "preto", true);
        SalesManager.createProduct("A", 15.0f);
        SalesManager.createProduct("B", 15.0f);
        SalesManager.createProduct("C", 15.0f);
        SalesManager.createProduct("D", 15.0f);
    }

    public static void stopTracking(ClientHandlerThread c){
        aliveClients.remove(c);
    }
}
