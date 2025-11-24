import client.Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientMain {

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
