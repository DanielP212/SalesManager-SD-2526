package server.requests;

import comms.common.Encodable;
import core.SalesManager;
import core.base.Product;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;

public class AddSaleRequest extends Request{
    private final ByteBuffer buffer;

    public AddSaleRequest(byte[] data){ this.buffer = ByteBuffer.wrap(data); }


    @Override
    public byte[] execute() {
        if (requesterClient == -1) return null;
        int productId = getInt(buffer);
        int quantity = getInt(buffer);
        float price = getFloat(buffer);

        boolean success = SalesManager.registerSale(productId, quantity, price);
        if (success){
            Product addedProduct = SalesManager.getProduct(productId);
            byte[] result = new byte[addedProduct.getName().length() + 1];
            Encodable.writeString(result, 0, addedProduct.getName());
            return result;
        }
        else return new byte[]{0x00};
    }

    @Override
    public String getAnswer() {
        String productName = getString(buffer);
        if (productName == null) return "Non-existent Product!";
        return "Registered sale for product " + productName;
    }
}
