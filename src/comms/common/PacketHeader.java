package comms.common;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketHeader extends Encodable{
    private int packetSize; // apenas set quando construido apartir de buffer
    private int packetID;
    private int clientID;
    private PacketType type;


    public PacketHeader(int dataSize, int packetID, int clientID, PacketType type){
        this.packetSize = this.headerSize() + dataSize; // not set yet
        this.packetID = packetID;
        this.clientID = clientID;
        this.type = type;
    }

    public PacketHeader(int packetSize, DataInputStream dataIn){
        try {
            this.packetSize = packetSize;
            this.packetID = dataIn.readInt();
            this.clientID = dataIn.readInt();
            this.type = PacketType.fromByte(dataIn.readByte());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void writeToOut() {
        try {
            dataOut.writeInt(-1); // placeHolder para o tamanho
            dataOut.writeInt(getID());
            dataOut.writeInt(getClientID());
            dataOut.writeByte(getType().toByte());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public int getID(){ return packetID; }
    public int getClientID(){ return clientID; }
    public PacketType getType(){ return type; }
    public int headerSize(){ return 4 + 4 + 4 + 1; } // packetSize, packetID, clientID, type
    public int packetSize(){ return packetSize; }
    public void setBufferOut(ByteArrayOutputStream out){ this.bufferOut = out; }
    public void setDataOut(DataOutputStream out){ this.dataOut = out; }

    @Override
    public String toString() {
        return "PacketHeader{" +
                "packetSize=" + packetSize +
                ", packetID=" + packetID +
                ", clientID=" + clientID +
                ", type=" + type +
                '}';
    }
}
