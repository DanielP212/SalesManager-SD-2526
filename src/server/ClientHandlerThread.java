package server;

import comms.Connection;
import comms.Packet;

import java.io.*;
import java.net.Socket;

public class ClientHandlerThread extends Thread{
    private final int clientID;
    private final Connection conn;

    public ClientHandlerThread(int clientID, Socket s){
        this.clientID = clientID;
        this.conn = new Connection(s);
        System.out.println("Created thread to handle client with ID " + clientID);
    }

    public void run(){
        while (!conn.isClosed()){
            try{
                Packet received = conn.receive();
                if (received == null){
                    System.out.println("[CLIENT THREAD] Null packet received?");
                    continue;
                }
                System.out.println("Received: " + received.toString());

                // Começar thread para fazer o request
                new RequestHandlerThread(clientID, conn, received).start();
            } catch (IOException e) {
                System.out.println("[CLIENT HANDLER] Thread Closing!");
                conn.close();
                Server.stopTracking(this);
                return;
            }
        }
    }
}
