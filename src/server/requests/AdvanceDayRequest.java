package server.requests;

import java.io.IOException;
import java.nio.ByteBuffer;

import comms.common.Encodable;
import core.AuthenticationManager;
import core.SalesManager;
import core.User;
import core.base.Product;

public class AdvanceDayRequest extends Request{

    public AdvanceDayRequest(byte[] data){
    }

    @Override
    public byte[] execute(){
        if (requesterClient == -1) return null;
        boolean isAdmin = AuthenticationManager.isUserAdmin(requesterClient);

        byte[] result = new byte[4];
        if (!isAdmin){
            Encodable.writeIntBytes(result, 0, 0);

        } else {
            try {
                SalesManager.advanceDay();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Encodable.writeIntBytes(result, 0, 1);
        }
        return result;
    }
}
