package client.answers;

import java.nio.ByteBuffer;

import javax.management.Notification;

import comms.common.PacketType;

public class NotificationSeqAnswer extends Answer{
    private final boolean result;


    public NotificationSeqAnswer(PacketType type, byte[] data){
        super(type);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int value = buffer.getInt();
        this.result = value == 1 ? true : false;
    }

    public String toString(){
        if(result){
            return "Both products were sold successfully!";
        }
        else{
            return "The day ended and the products weren't sold!";
        }
    }

}
