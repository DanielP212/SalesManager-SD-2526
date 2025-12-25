package client.answers;

import comms.common.PacketType;

import java.nio.ByteBuffer;

public class AddSaleAnswer extends Answer {
    private final int productID;

    public AddSaleAnswer(PacketType type, byte[] data) {
        super(type);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        productID = buffer.getInt();
    }

    @Override
    public String toString() {
        if (productID == -1){
            return "Product doesn't exist!";
        } else {
            return "Registered sale for product with ID: " + productID;
        }
    }
}
