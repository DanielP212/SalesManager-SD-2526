import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientMain {

    static void main() {
        try {
            Socket socket = new Socket("localhost", 12345);
            try (DataInputStream in = new DataInputStream(System.in)){
                byte[] buf = new byte[1024];
                int bytesRead = in.read(buf);
                if (bytesRead > 0){
                    try (DataOutputStream out = new DataOutputStream(socket.getOutputStream())){
                        out.write(buf);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
