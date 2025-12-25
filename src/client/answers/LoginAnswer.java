package client.answers;

import comms.common.PacketType;

import java.nio.ByteBuffer;

public class LoginAnswer extends Answer{
    private final int assignedID;

    public LoginAnswer(PacketType type, byte[] data){
        super(type);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        assignedID = buffer.getInt();
    }

    public int getAssignedID(){ return assignedID; }

    @Override
    public String toString() {
        return switch (assignedID){
            case -1 -> "Invalid User!";
            case -2 -> "Already Logged in as a User!";
            default -> String.valueOf(assignedID);
        };
    }
}
