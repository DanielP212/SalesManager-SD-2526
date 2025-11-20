package client;

import comms.Packet;
import server.RequestHandlerThread;
import server.requests.Request;

import java.io.DataInputStream;
import java.io.IOException;

public class RequestAnswerHandler extends Thread{
    DataInputStream in;

    public RequestAnswerHandler(DataInputStream in){
        this.in = in;
    }

    public void run(){
        try{
            while(true){
               int packetSize = in.readInt();
                byte[] buf = new byte[packetSize];
                int bytesRead = in.read(buf);
                if (bytesRead != packetSize - 4){ // Quem nao perceber o -4 que se mate
                    System.out.println("[WARNING] Read different bytes(" + bytesRead + ") from packet size told on header. Is this intended?");
                    continue;
                }
                Packet received = new Packet(packetSize, buf);
                System.out.println("Received: " + received.toString());
                Request reqAnswer = Request.fromPacket(received);
                if (reqAnswer == null) continue;
                System.out.println("[ANSWER " + received.getID() + "] " + reqAnswer.getAnswer());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
