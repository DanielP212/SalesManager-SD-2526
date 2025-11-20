package comms;

import comms.common.Encodable;
import comms.common.PacketHeader;
import comms.common.RequestType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Packet extends Encodable {
    private PacketHeader header;
    private static int ID_COUNTER = 0;
    private final int dataSize;
    private byte[] data;

    public Packet(int clientID, RequestType type, byte[] data){
        super();
        int packetID = (int)(clientID*(Math.pow(10, (int)(clientID/10) + 1))) + ID_COUNTER++;

        this.dataSize = data.length;
        this.data = data;
        this.header = new PacketHeader(dataSize, packetID, clientID, type);
        header.setBufferOut(bufferOut);
        header.setDataOut(dataOut);
        /*
        ByteBuffer buffer = ByteBuffer.wrap(data);
        int size = buffer.getInt();
        for (int i = 0; i < size; i++){
            System.out.println(buffer.getChar());
        }
         */
    }

    // Construtor para reconstruir um pacote a partir de um byteBuffer
    public Packet(int packetSize, byte[] buffer){
        super(buffer); // mete os dados do array no bufferIn e cria dataIn
        this.header = new PacketHeader(packetSize, dataIn);
        dataSize = buffer.length - this.header.headerSize();
    }

    @Override
    public void writeToOut() {
        header.writeToOut();
        try {
            dataOut.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String toString() {
        return "Packet{" +
                "header=" + header +
                '}';
    }

    public void setHeader(PacketHeader newHeader){
        this.header = newHeader;
    }

    public byte[] getData(){
        byte[] data = new byte[dataSize];
        bufferIn.read(data, header.headerSize(), header.headerSize() + dataSize);
        return data;
    }

    public RequestType getType(){ return header.getType(); }
    public int getID(){ return header.getID(); }
    public int getClientID(){ return header.getClientID(); }

}
