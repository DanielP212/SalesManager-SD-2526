package client.answers;

import comms.common.PacketType;

import java.nio.ByteBuffer;

public class AdvanceDayAnswer extends Answer {
    private final int success;

    public AdvanceDayAnswer(PacketType type, byte[] data) {
        super(type);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        success = buffer.getInt();
    }

    @Override
    public String toString() {

        if(success == 1)  return "Day advanced";
        else return "Could not advance day";
    }
}
