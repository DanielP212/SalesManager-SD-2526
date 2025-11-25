package client;

import comms.Packet;
import comms.common.PacketType;
import server.requests.Request;

public class PendingRequestThread extends Thread {
    private final Packet requestPacket;
    private final ClientConnectionThread connectionThread;


    public PendingRequestThread(Packet request, ClientConnectionThread connectionThread){
        this.requestPacket = request;
        this.connectionThread = connectionThread;
    }

    public void run(){
        System.out.println("[Request " + requestPacket.getID() + "] " +
                "Sending Request");
        Packet response = connectionThread.sendRequest(requestPacket);
        Request reqResponse = Request.fromPacket(response);
        if (reqResponse == null){
            System.out.println("Null packet. How did we get here?");
            return;
        };
        if (requestPacket.getType() == PacketType.LOGIN){
           try{
                int maybeID = Integer.parseInt(reqResponse.getAnswer());
                connectionThread.assignClientID(maybeID);
               System.out.println("[Response " + response.getID() + "] Assigned ID " +
                       maybeID);
               return;
           } catch (NumberFormatException e) {
           }
        }
        System.out.println("[Response " + response.getID() + "] " +
                reqResponse.getAnswer());
    }
}
