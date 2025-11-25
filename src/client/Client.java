package client;

import comms.Packet;
import comms.common.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class Client {
    private static final int NOT_LOGGED_ID = -1;
    private int id = NOT_LOGGED_ID;

    DataInputStream userInput;
    ClientConnectionThread connectionThread;


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
