package server.requests;

import comms.common.Encodable;
import comms.common.PacketType;
import core.SalesManager;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

public class QueryRequest extends Request{
    private final ByteBuffer buffer;

    public QueryRequest(byte[] data){
        this.buffer = ByteBuffer.wrap(data);
    }

    // TODO request ainda nao testado
    @Override
    public byte[] execute(){
        if (requesterClient == -1) return null;
        int productID = getInt(buffer);
        int numDays = getInt(buffer);
        byte[] result = new byte[4];
        if (type == PacketType.QUERY_QTD){
            int soldQtd = 0;
            try {
                System.out.println("num days " + numDays + " id " + productID);
                soldQtd = SalesManager.getSoldQuantity2(numDays, productID);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            Encodable.writeIntBytes(result, 0, soldQtd);
        } else if (type == PacketType.QUERY_TOTAL){
            float totalMoney = SalesManager.getTotalMoney(numDays, productID);
            Encodable.writeIntBytes(result, 0, Float.floatToIntBits(totalMoney));
        } else if (type == PacketType.QUERY_MEDIAN){
            float median = SalesManager.getMedianPrice(numDays, productID);
            Encodable.writeIntBytes(result, 0, Float.floatToIntBits(median));
        } else { // Max
            float max = SalesManager.getMaxPrice(numDays, productID);
            Encodable.writeIntBytes(result, 0, Float.floatToIntBits(max));
        }
        return result;
    }

    @Override
    public String getAnswer() {
        if (type == PacketType.QUERY_QTD){
            int result = buffer.getInt();
            return String.valueOf(result);
        }
        float result = buffer.getFloat();
        return String.valueOf(result);
    }
}
