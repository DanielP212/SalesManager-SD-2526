package server.requests;


import comms.Packet;
import comms.common.Encodable;
import server.Server;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class LoginRequest extends Request {
    private final ByteBuffer buffer;

    protected LoginRequest(byte[] data){
        this.buffer = ByteBuffer.wrap(data);
    }

    @Override
    public byte[] execute() {
        if (requesterClient == -1) return null;
        byte[] result = new byte[4]; // retorna ao cliente o seu ID
        if(Server.authHandler.isClientLoggedIn(requesterClient)){
            Encodable.writeIntBytes(result, 0, -2);
            return result;
        }

        String username = getString(buffer);
        String password = getString(buffer);
        boolean userLoggedIn = Server.authHandler.loginUser(requesterClient, username, password);
        if (userLoggedIn){
            Encodable.writeIntBytes(result, 0, requesterClient);
        } else {
            Encodable.writeIntBytes(result, 0, -1);
        }
        return result;
    }

    @Override
    public String getAnswer() {
        if (requesterClient != -1) return ""; // Se for -1 'e um packet de Answer
        System.out.println(Arrays.toString(buffer.array()));
        int assignedID = buffer.getInt();
        return switch (assignedID){
            case -1 -> "Invalid User!";
            case -2 -> "Already Logged in as a User!";
            default -> String.valueOf(assignedID);
        };
    }

}
