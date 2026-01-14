package comms;

import comms.common.Encodable;
import comms.common.PacketHeader;
import comms.common.PacketType;

import java.io.IOException;
import java.util.Arrays;

public class Packet extends Encodable {
    private PacketHeader header;
    private static int ID_COUNTER = 1;
    private final int dataSize;
    private final byte[] data;

    public Packet(int clientID, PacketType type, byte[] data){
        super();
        int packetID = (int)(clientID*(Math.pow(10, (int)(clientID/10) + 1))) + ID_COUNTER++;

        this.dataSize = data.length;
        this.data = data;
        this.header = new PacketHeader(dataSize, packetID, clientID, type);
        header.setBufferOut(bufferOut);
        header.setDataOut(dataOut);
    }

    // Constructor para criar um pacote de resposta a um pacote de request
    public Packet(Packet rp, byte[] data){
        super();
        this.dataSize = data.length;
        this.data = data;
        this.header = new PacketHeader(dataSize, rp.getID(), rp.getClientID(), rp.getType());
        header.setBufferOut(bufferOut);
        header.setDataOut(dataOut);
    }


    // Construtor para reconstruir um pacote a partir de um byteBuffer
    public Packet(int packetSize, byte[] buffer){
        super(buffer); // mete os dados do array no bufferIn e cria dataIn
        this.header = new PacketHeader(packetSize, dataIn);
        dataSize = buffer.length - (this.header.headerSize() - 4);
        try {
            this.data = new byte[dataSize];
            int bytesRead = dataIn.read(this.data, 0, dataSize);
            if (bytesRead != dataSize){
                System.out.println("[PACKET DECODING] Read different number of bytes than expected!" +
                        " Are you sure this is intended?");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
                ", dataSize=" + dataSize +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public void setHeader(PacketHeader newHeader){
        this.header = newHeader;
    }

    public byte[] getData(){
        return data;
    }

    public PacketType getType(){ return header.getType(); }
    public int getID(){ return header.getID(); }
    public int getClientID(){ return header.getClientID(); }

}
