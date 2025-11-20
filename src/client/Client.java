package client;

import comms.Packet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private int id = 0;

    DataInputStream in;
    DataOutputStream out;

    public void run(){
        try {
            Socket socket = new Socket("localhost", 12345);
            in = new DataInputStream(System.in);
            out = new DataOutputStream(socket.getOutputStream());
            while(true){
                byte[] buf = new byte[1024];
                int bytesRead = in.read(buf);
                if (bytesRead > 0){
                    Packet p = InputHandler.handle(id, new String(buf));
                    if (p == null) continue;

                    out.write(p.getBytes());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
