package client;

import comms.Connection;
import comms.Packet;
import server.requests.Request;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientConnectionThread extends Thread{
    private final Client parentClient;
    private final Connection conn;
    private final Map<Integer, PendingRequest> pendingRequests = new HashMap<>();

    public ClientConnectionThread(Client parentClient, Socket s){
        this.parentClient = parentClient;
        this.conn = new Connection(s);
    }

    public void run(){
        try{
            while(true){
                Packet received = conn.receive();
                if (received == null) {
                    System.out.println("[CLIENT CONNECTION] Null packet received!");
                    continue;
                }
                PendingRequest monitor = pendingRequests.get(received.getID());
                if (monitor == null){
                    System.out.println("[CLIENT CONNECTION] Received packet I was not waiting for!" +
                            " Are you sure this behaviour is intended?");
                    continue;
                }
                monitor.complete(received);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Mandar request. Retorna Packet de resposta
    public Packet sendRequest(Packet requestPacket){
        int packetID = requestPacket.getID();
        try {
            conn.send(requestPacket);
            PendingRequest monitor = new PendingRequest();
            pendingRequests.put(packetID, monitor);
            try{
                return monitor.waitForResponse();
            }finally {
                pendingRequests.remove(packetID);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void assignClientID(int assignedID){ parentClient.assignID(assignedID); }
    public boolean isTestConnection(){ return parentClient.isTestInstance(); }
    public PrintStream getTestOut(){ return parentClient.getTestOutput(); }
}
