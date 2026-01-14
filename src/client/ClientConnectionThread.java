package client;

import comms.Connection;
import comms.Packet;
import server.requests.Request;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientConnectionThread extends Thread{
    private final Client parentClient;
    private final Connection conn;
    private final Map<Integer, PendingRequest> pendingRequests = new HashMap<>();
    private final Lock mapLock = new ReentrantLock();

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

                PendingRequest monitor = null;
                mapLock.lock();
                try{
                    monitor = pendingRequests.get(received.getID());
                } finally {
                    mapLock.unlock();
                }

                if (monitor == null){
                    System.out.println("[CLIENT CONNECTION] Received packet I was not waiting for!" +
                            " Are you sure this behaviour is intended?");
                    continue;
                }
                monitor.complete(received);
            }
        } catch (IOException e) {
        }
    }

    // Mandar request. Retorna Packet de resposta
    public Packet sendRequest(Packet requestPacket){
        int packetID = requestPacket.getID();

        try {
            PendingRequest monitor = new PendingRequest();
            mapLock.lock();
            try{
                pendingRequests.put(packetID, monitor);
            }finally {
                mapLock.unlock();
            }

            conn.send(requestPacket);
            try{
                return monitor.waitForResponse();
            }finally {
                mapLock.lock();
                try{
                    pendingRequests.remove(packetID);
                } finally {
                    mapLock.unlock();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void assignClientID(int assignedID){ parentClient.assignID(assignedID); }
    public boolean isTestConnection(){ return parentClient.isTestInstance(); }
    public PrintStream getTestOut(){ return parentClient.getTestOutput(); }

    public void quit(){
        conn.close();
    }
}
