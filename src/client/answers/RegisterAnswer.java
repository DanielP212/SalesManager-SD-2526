package client.answers;

import comms.common.PacketType;

import java.nio.ByteBuffer;

public class RegisterAnswer extends Answer {
    private final byte success;

    public RegisterAnswer(PacketType type, byte[] data) {
        super(type);
        this.type = type;
        success = data[0];
    }

    @Override
    public String toString() {
        return (success == 0x01) ? "Registered Successfully" : "That username already exists!";
    }
}
