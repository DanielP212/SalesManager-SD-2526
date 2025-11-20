package server.requests;


import comms.Packet;
import server.Server;

import java.nio.ByteBuffer;

public class LoginRequest extends Request {
    private final ByteBuffer buffer;

    public LoginRequest(byte[] data){
        this.buffer = ByteBuffer.wrap(data);
    }

    @Override
    public byte[] execute() {
        int usernameSize = buffer.get();
        StringBuilder uBuilder = new StringBuilder();
        for (int i = 0; i < usernameSize; i++) {
            uBuilder.append((char)buffer.get());
        }
        String username = uBuilder.toString();

        int passwordSize = buffer.get();
        StringBuilder pBuilder = new StringBuilder();
        for (int i = 0; i < passwordSize; i++) {
            pBuilder.append((char)buffer.get());
        }
        String password = pBuilder.toString();

        boolean userLoggedIn = Server.authHandler.loginUser(username, password);

        if (userLoggedIn) return new byte[]{0x01};
        else return new byte[]{0x00};
    }

    @Override
    public String getAnswer() {
        byte successfulLogin = buffer.get();
        return (successfulLogin == 0x01) ? "Welcome~" : "Invalid user";
    }

}
