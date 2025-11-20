package server;

import comms.Packet;
import comms.common.RequestType;

import java.io.DataOutputStream;

// TODO Ler o comentario
// Temos a opcao de fazer esta classe de uma classe parente e fazer uma classe filha especifica para cada tipo de request.
// Ou fazer tudo nesta classe
// Como preferirem
public class RequestHandlerThread extends Thread{
    DataOutputStream out;
    Packet receivedPacket;

    public RequestHandlerThread(DataOutputStream out, Packet p){
        this.out = out;
        this.receivedPacket = p;
    }

    public void run(){
        // Do shit
        RequestType type = receivedPacket.getType();
        if (type == RequestType.AUTH){
        }
    }

}
