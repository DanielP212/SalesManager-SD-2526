package client;

import client.answers.Answer;
import client.answers.LoginAnswer;
import comms.Packet;
import comms.common.PacketType;
import server.requests.Request;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PendingRequestThread extends Thread {
    private Packet requestPacket = null;
    private final ClientConnectionThread connectionThread;
    private final Client parentClient;

    private final Lock requestLock = new ReentrantLock();
    private final Condition hasRequest = requestLock.newCondition();




    public PendingRequestThread(Client client, ClientConnectionThread connectionThread){
        this.connectionThread = connectionThread;
        this.parentClient = client;
    }

    public void giveRequest(Packet requestPacket){
        requestLock.lock();
        try {
            if (this.requestPacket != null) return;
            this.requestPacket = requestPacket;
            hasRequest.signal();
        } finally {
            requestLock.unlock();
        }
    }

    public void run(){
        while(true){
            requestLock.lock();
            while (requestPacket == null){
                try {
                    hasRequest.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            requestLock.unlock();
            parentClient.signalRequestStart(this);

            System.out.println("[Request " + requestPacket.getID() + "] " +
                    "Sending Request");
            Packet response = connectionThread.sendRequest(requestPacket);
            Answer answer = Answer.fromPacket(response);
            if (answer == null){
                System.out.println("Null packet. How did we get here?");
                finishRequest();
                continue;
            };
            if (requestPacket.getType() == PacketType.LOGIN){
                try{
                    int maybeID = ((LoginAnswer) answer).getAssignedID();
                    connectionThread.assignClientID(maybeID);
                    System.out.println("[Response " + response.getID() + "] Assigned ID " +
                            maybeID);
                } catch (NumberFormatException e) {
                }
                finishRequest();
                return;
            }

            if (connectionThread.isTestConnection()){
                connectionThread.getTestOut().println(answer);
            } else {
                System.out.println("[Response " + response.getID() + "] " +
                        answer);
            }
            finishRequest();
        }
    }

    public void finishRequest(){
        this.requestPacket = null;
        parentClient.signalRequestDone(this);
    }
}
