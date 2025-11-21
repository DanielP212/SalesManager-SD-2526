package server.requests;


import comms.Packet;
import server.Server;

import java.nio.ByteBuffer;

public class LoginRequest extends Request {
    private final ByteBuffer buffer;

    protected LoginRequest(byte[] data){
        this.buffer = ByteBuffer.wrap(data);
    }

    @Override
    public byte[] execute() {
        if (requesterClient == -1) return null;
        if(Server.authHandler.isClientLoggedIn(requesterClient)){
            return new byte[]{0x02};
        }

        String username = getString(buffer);
        String password = getString(buffer);
        boolean userLoggedIn = Server.authHandler.loginUser(requesterClient, username, password);

        if (userLoggedIn) return new byte[]{0x01};
        else return new byte[]{0x00};
    }

    @Override
    public String getAnswer() {
        if (requesterClient != -1) return ""; // Se for -1 'e um packet de Answer
        byte successfulLogin = buffer.get();
        return switch (successfulLogin){
            case 0x01 -> "Welcome~";
            case 0x02 -> "Already Logged in as a User!";
            default -> "Invalid User!";
        };
    }

}
