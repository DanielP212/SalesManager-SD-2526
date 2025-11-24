package server.requests;

import comms.common.Encodable;
import core.SalesManager;

import java.nio.ByteBuffer;

public class CreateProductRequest extends Request{
    private final ByteBuffer buffer;

    public CreateProductRequest(byte[] data){ this.buffer = ByteBuffer.wrap(data); }


    @Override
    public byte[] execute() {
        if (requesterClient == -1) return null;
        String productName = getString(buffer);
        float basePrice = getFloat(buffer);
        int newProductID = SalesManager.createProduct(productName, basePrice);
        byte[] result = new byte[4];
        Encodable.writeIntBytes(result, 0, newProductID);
        return result;
    }

    @Override
    public String getAnswer() {
        int newProductID = buffer.getInt();
        return "Created new product with ID: " + newProductID;
    }
}
