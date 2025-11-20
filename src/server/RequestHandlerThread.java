package server;

import comms.Packet;
import comms.common.PacketType;
import server.requests.LoginRequest;
import server.requests.Request;

import java.io.DataOutputStream;
import java.io.IOException;

public class RequestHandlerThread extends Thread {
    DataOutputStream out;
    Packet receivedPacket;

    public RequestHandlerThread(DataOutputStream out, Packet p){
        this.out = out;
        this.receivedPacket = p;
    }

    public void run(){
        PacketType type = receivedPacket.getType();
        Request req;
        req = switch(type){
            case LOGIN -> new LoginRequest(receivedPacket.getData());
            case null, default -> null;
        };
        if (req == null){
            System.out.println("[REQUEST HANDLER] Invalid request type found!" +
                    " How did we get here?");
            return;
        }
        byte[] requestData = req.execute();
        Packet answerPacket = new Packet(receivedPacket, requestData);
        try {
            // TODO NECESSITA DE LOCKS SEUS MACACOS
            out.write(answerPacket.getBytes());
            out.flush();
            System.out.println("Answer to packet: " + answerPacket.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
