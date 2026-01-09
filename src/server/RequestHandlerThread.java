package server;

import comms.Connection;
import comms.Packet;
import comms.common.PacketType;
import server.requests.LoginRequest;
import server.requests.RegisterRequest;
import server.requests.Request;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RequestHandlerThread extends Thread {
    private final int clientID;
    private final Connection conn;
    private final Lock requestLock = new ReentrantLock();
    private final Condition hasRequest = requestLock.newCondition();
    private final ClientHandlerThread parent;
    private Packet currentPacket = null;
    private volatile boolean running;

    public RequestHandlerThread(int clientID, Connection conn, ClientHandlerThread parent){
        this.clientID = clientID;
        this.conn = conn;
        this.parent = parent;
    }

    public void assign(Packet p){
        requestLock.lock();
        try{
            currentPacket = p;
            hasRequest.signal();
        } finally {
            requestLock.unlock();
        }
    }

    public void stopThread(){
        running = false;
        this.interrupt();
    }

    public void run(){
        running = true;
        while(running){
            Packet toProcess = null;

            requestLock.lock();
            while (currentPacket == null && running){
                try {
                    hasRequest.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!running) return;
            toProcess = currentPacket;
            currentPacket = null;
            requestLock.unlock();

            if (toProcess == null) continue;
            Request req = Request.fromPacket(clientID, toProcess);
            if (req == null){
                System.out.println("[REQUEST HANDLER] Invalid request type found!" +
                        " Check if that Request Exists!");
                parent.freeRequestHThread(this);
                continue;
            }
            byte[] requestData = req.execute();
            if (requestData == null){
                System.out.println("Deu asneira ao executar o pacote! Verifica isto aqui(RequestHandlerThread.java) " +
                        "Melhor fazer um pacote caso dẽ erro nao esqueça!");
                parent.freeRequestHThread(this);
                continue;
            }
            Packet answerPacket = new Packet(toProcess, requestData);
            try {
                conn.send(answerPacket);
                System.out.println("Answer to packet: " + answerPacket.toString());
                parent.freeRequestHThread(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
