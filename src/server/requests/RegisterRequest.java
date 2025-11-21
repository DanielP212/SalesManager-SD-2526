package server.requests;

import server.Server;

import java.nio.ByteBuffer;

public class RegisterRequest extends Request {
    private final ByteBuffer buffer;

    public RegisterRequest(byte[] data){
        this.buffer = ByteBuffer.wrap(data);
    }


    @Override
    public byte[] execute() {
        if (requesterClient == -1) return null;
        String username = getString(buffer);
        String password = getString(buffer);

        boolean successfulRegister = Server.authHandler.registerUser(username, password);
        if (successfulRegister) return new byte[]{0x01};
        else return new byte[]{0x00};
    }

    @Override
    public String getAnswer() {
        byte successfulRegister = buffer.get();
        return (successfulRegister == 0x01) ? "Registered Successfully" : "That username already exists!";
    }
}
