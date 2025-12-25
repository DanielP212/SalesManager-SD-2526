package client.answers;

import comms.common.PacketType;

import java.nio.ByteBuffer;

public class QueryAnswer extends Answer {
    private final Integer resultInt;
    private final Float resultFloat;


    public QueryAnswer(PacketType type, byte[] data) {
        super(type);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        if (type == PacketType.QUERY_QTD){
            resultInt = buffer.getInt();
            resultFloat = null;
        } else {
            resultFloat = buffer.getFloat();
            resultInt = null;
        }
    }

    @Override
    public String toString() {
        if (type == PacketType.QUERY_QTD){
            return String.valueOf(resultInt);
        }
        return String.valueOf(resultFloat);
    }
}
