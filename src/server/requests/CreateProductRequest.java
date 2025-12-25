package server.requests;

import comms.common.Encodable;
import core.SalesManager;

import java.nio.ByteBuffer;

public class CreateProductRequest extends Request{
    private final ByteBuffer buffer;
    private final String productName;
    private final float basePrice;

    public CreateProductRequest(byte[] data){
        this.buffer = ByteBuffer.wrap(data);
        productName = readString(buffer);
        basePrice = getFloat(buffer);
    }


    @Override
    public byte[] execute() {
        if (requesterClient == -1) return null;
        int newProductID = SalesManager.createProduct(productName, basePrice);
        byte[] result = new byte[4];
        Encodable.writeIntBytes(result, 0, newProductID);
        return result;
    }
}
