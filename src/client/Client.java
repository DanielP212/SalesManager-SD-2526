package client;

import comms.Packet;
import comms.common.PacketType;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class Client implements Runnable {
    private static final int NOT_LOGGED_ID = -1;
    private int id = NOT_LOGGED_ID; // MUDAR para qualquer cena para nao ter de dar login

    InputStream userInput = null;
    ClientConnectionThread connectionThread;

    public Client(){;}

    // Para UnitTests
    public Client(InputStream systemIn){
        System.setIn(systemIn);
        id = 1;
    }

    public void run(){
        try {
            Socket socket = new Socket("localhost", 12345);
            userInput = new DataInputStream(System.in);
            connectionThread = new ClientConnectionThread(this, socket);
            connectionThread.start();
            while(true){
                byte[] buf = new byte[1024];
                int bytesRead = userInput.read(buf);
                if (bytesRead > 0){
                    if (new String(buf).trim().equals("quit")) return;

                    Packet p = InputHandler.handle(id, new String(Arrays.copyOf(buf, bytesRead)));
                    if (p == null){
                        System.out.println("Null packet. deu muita merda maltinha! Inputs erradas?");
                        continue;
                    }
                    // Nao fazer nada enquanto nao estiver loggado
                    if (!isLoggedIn() &&
                            (p.getType() != PacketType.LOGIN && p.getType() != PacketType.REGISTER)){
                        System.out.println("It seems you are not logged in! Please login or register first!");
                        continue;
                    }
                    new PendingRequestThread(p, connectionThread).start();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void assignID(int assignedID){
        if (isLoggedIn()) return;
        this.id = assignedID;
    }

    public boolean isLoggedIn(){ return id != NOT_LOGGED_ID; }
}
