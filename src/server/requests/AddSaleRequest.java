package server.requests;

import comms.common.Encodable;
import core.SalesManager;
import core.base.Product;

import java.nio.ByteBuffer;

public class AddSaleRequest extends Request{
    private final ByteBuffer buffer;
    private final int productId;
    private final int quantity;
    private final float price;

    public AddSaleRequest(byte[] data){
        this.buffer = ByteBuffer.wrap(data);
        String productName = Encodable.readString(buffer);
        Product p = SalesManager.getProductByName(productName);
        productId = (p == null) ? -1 : p.getId();
        quantity = buffer.getInt();
        price = buffer.getFloat();
    }


    @Override
    public byte[] execute() {
        if (requesterClient == -1) return null;
        if (productId == -1) return null;

        boolean success = SalesManager.registerSale(productId, quantity, price);
        byte[] result = new byte[4];
        if (success){
            Encodable.writeIntBytes(result, 0, productId);

        } else {
            Encodable.writeIntBytes(result, 0, -1);
        }
        return result;
    }
}
