package client.answers;

import java.nio.ByteBuffer;

import comms.common.Encodable;
import comms.common.PacketType;

public class NotificationConcAnswer extends Answer{
    private final String product;


    public NotificationConcAnswer(PacketType type, byte[] data){
        super(type);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        if(buffer.get(0) == 0x00) product = null;
        else product = Encodable.readString(buffer);
        
    }

    public String toString(){
        if(product != null){
            return "Product: " + product + " was consecutively sold the requested amount of times!";
        }
        else{
            return "The day ended and the product wasn't sold consecutively enough times!";
        }
    }

}
