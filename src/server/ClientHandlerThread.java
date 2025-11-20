package server;

import comms.Packet;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandlerThread extends Thread{
    Socket socket;
    DataOutputStream out;
    DataInputStream in;

    public ClientHandlerThread(Socket s){
        this.socket = s;
        try {
            out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
            in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO Prevenir que a exceção levantada ao fechar o cliente sem fechar a socket
    // Cause um aviso no servidor
    public void run(){

        while (!socket.isClosed()){
            try{
                int packetSize = in.readInt();
                System.out.println("Received packet with Size: " + packetSize);
                byte[] buf = new byte[packetSize];
                int bytesRead = in.read(buf);
                if (bytesRead != packetSize - 4 ){ // 4 = tamanho do int packetSize
                    System.out.println("[WARNING] Read different bytes(" + bytesRead + ") from packet size told on header. Is this intended?");
                    continue;
                }
                Packet received = new Packet(packetSize, buf);
                System.out.println("Received: " + received.toString());

                // Comecar thread para fazer o request
                new RequestHandlerThread(out, received).start();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
