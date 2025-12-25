package server.requests;


import comms.common.Encodable;
import server.Server;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class LoginRequest extends Request {
    private final ByteBuffer buffer;
    private final String username;
    private final String password;

    protected LoginRequest(byte[] data){
        this.buffer = ByteBuffer.wrap(data);
        username = readString(buffer);
        password = readString(buffer);
    }

    @Override
    public byte[] execute() {
        if (requesterClient == -1) return null;
        byte[] result = new byte[4]; // retorna ao cliente o seu ID
        if(Server.authHandler.isClientLoggedIn(requesterClient)){
            Encodable.writeIntBytes(result, 0, -2);
            return result;
        }

        boolean userLoggedIn = Server.authHandler.loginUser(requesterClient, username, password);
        if (userLoggedIn){
            Encodable.writeIntBytes(result, 0, requesterClient);
        } else {
            Encodable.writeIntBytes(result, 0, -1);
        }
        return result;
    }

}
