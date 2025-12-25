package server.requests;

import comms.common.Encodable;
import comms.common.PacketType;
import core.SalesManager;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

public class QueryRequest extends Request{
    private final ByteBuffer buffer;
    private final int productID;
    private final int numDays;

    public QueryRequest(byte[] data){
        this.buffer = ByteBuffer.wrap(data);
        productID = getInt(buffer);
        numDays = getInt(buffer);
    }

    @Override
    public byte[] execute(){
        if (requesterClient == -1) return null;

        byte[] result = new byte[4];
        if (type == PacketType.QUERY_QTD){
            int soldQtd = 0;
            try {
                System.out.println("num days " + numDays + " id " + productID);
                soldQtd = SalesManager.getSoldQuantity(numDays, productID);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            Encodable.writeIntBytes(result, 0, soldQtd);
        } else if (type == PacketType.QUERY_TOTAL){
            float totalMoney = SalesManager.getTotalMoney(numDays, productID);
            Encodable.writeIntBytes(result, 0, Float.floatToIntBits(totalMoney));
        } else if (type == PacketType.QUERY_AVG){
            float median = SalesManager.getAveragePrice(numDays, productID);
            Encodable.writeIntBytes(result, 0, Float.floatToIntBits(median));
        } else { // Max
            float max = SalesManager.getMaxPrice(numDays, productID);
            Encodable.writeIntBytes(result, 0, Float.floatToIntBits(max));
        }
        return result;
    }
}
