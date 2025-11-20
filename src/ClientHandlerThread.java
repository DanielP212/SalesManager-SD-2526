import java.io.*;
import java.net.Socket;

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

    public void run(){

        while (!socket.isClosed()){
            byte[] buf = new byte[1024];
            String message = null;
            try{
                int bytesRead = in.read(buf);
                if (bytesRead > 0){
                    message = new String(buf);
                    System.out.println(message);
                }

                // TODO validar input
                if (message == null) continue;
                out.write(message.getBytes());
                out.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
