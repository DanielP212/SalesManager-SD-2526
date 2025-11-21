package client;

import comms.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class Client {
    private int id = 1;

    DataInputStream in;
    DataOutputStream out;
    AnswerHandlerThread reqAnswerThread;


    public void run(){
        try {
            Socket socket = new Socket("localhost", 12345);
            in = new DataInputStream(System.in);
            out = new DataOutputStream(socket.getOutputStream());
            reqAnswerThread = new AnswerHandlerThread(new DataInputStream(socket.getInputStream()));
            reqAnswerThread.start();
            // TODO Precisa de um pacote de ACK com o servidor para receber ID
            while(true){
                byte[] buf = new byte[1024];
                int bytesRead = in.read(buf);
                if (bytesRead > 0){
                    Packet p = InputHandler.handle(id, new String(Arrays.copyOf(buf, bytesRead)));
                    if (p == null){
                        System.out.println("Null packet. deu muita merda maltinha! Inputs erradas?");
                        continue;
                    }

                    System.out.println("[REQUEST " + p.getID() + "] Sent request");
                    out.write(p.getBytes());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
