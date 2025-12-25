package client.answers;

import comms.common.PacketType;

import java.nio.ByteBuffer;

public class CreateProductAnswer extends Answer {
    private final int productID;

    public CreateProductAnswer(PacketType type, byte[] data) {
        super(type);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        productID = buffer.getInt();
    }

    @Override
    public String toString() {
        return "Created new product with ID: " + productID;
    }
}
