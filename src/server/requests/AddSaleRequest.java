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
        productId = getInt(buffer);
        quantity = getInt(buffer);
        price = getFloat(buffer);
    }


    // TODO Mudar isto para receber por nome inves de ID
    @Override
    public byte[] execute() {
        if (requesterClient == -1) return null;

        boolean success = SalesManager.registerSale(productId, quantity, price);
        if (success){
            Product addedProduct = SalesManager.getProduct(productId);
            byte[] result = new byte[addedProduct.getName().length() + 1];
            Encodable.writeString(result, 0, addedProduct.getName());
            return result;
        }
        else return new byte[]{0x00};
    }
}
