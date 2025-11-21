package server;

import comms.Connection;
import comms.Packet;
import comms.common.PacketType;
import server.requests.LoginRequest;
import server.requests.RegisterRequest;
import server.requests.Request;

import java.io.DataOutputStream;
import java.io.IOException;

public class RequestHandlerThread extends Thread {
    private final int clientID;
    private final Connection conn;
    private final Packet receivedPacket;

    public RequestHandlerThread(int clientID, Connection conn, Packet p){
        this.clientID = clientID;
        this.conn = conn;
        this.receivedPacket = p;
    }

    public void run(){
        PacketType type = receivedPacket.getType();
        Request req = Request.fromPacket(clientID, receivedPacket);
        if (req == null){
            System.out.println("[REQUEST HANDLER] Invalid request type found!" +
                    " Check if that Request Exists!");
            return;
        }
        byte[] requestData = req.execute();
        if (requestData == null){
            System.out.println("Deu merda ao executar o pacote! Verifica isto aqui");
            return;
        }
        Packet answerPacket = new Packet(receivedPacket, requestData);
        try {
            conn.send(answerPacket);
            System.out.println("Answer to packet: " + answerPacket.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
