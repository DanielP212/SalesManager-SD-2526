package server.requests;

import server.Server;

import java.nio.ByteBuffer;

public class RegisterRequest extends Request {
    private final ByteBuffer buffer;
    private final String username;
    private final String password;

    public RegisterRequest(byte[] data){
        this.buffer = ByteBuffer.wrap(data);
        username = readString(buffer);
        password = readString(buffer);
    }


    @Override
    public byte[] execute() {
        if (requesterClient == -1) return null;

        boolean successfulRegister = Server.authHandler.registerUser(username, password, false);
        if (successfulRegister) return new byte[]{0x01};
        else return new byte[]{0x00};
    }
}
