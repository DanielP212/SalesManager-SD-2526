package comms.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class Encodable {
    protected ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
    protected DataOutputStream dataOut = new DataOutputStream(bufferOut);

    protected ByteArrayInputStream bufferIn;
    protected DataInputStream dataIn;

    public static byte BYTE_TYPE = 0x00;
    public static byte INTEGER_TYPE = 0x01;
    public static byte FLOAT_TYPE = 0x02;
    public static byte DOUBLE_TYPE = 0x03;
    public static byte CHAR_TYPE = 0x04;
    public static byte STRING_TYPE = 0x05;
    public static byte COORDINATE_TYPE = 0x06;
    public static byte ARRAY_TYPE = 0x10;

    public Encodable(){;}

    public Encodable(byte[] data){
        this.bufferIn = new ByteArrayInputStream(data);
        this.dataIn = new DataInputStream(bufferIn);
    }

    public static void writeIntBytes(byte[] buffer, int offset, int toWrite){
        buffer[offset] = (byte)(toWrite >>> 24);
        buffer[offset + 1] = (byte)(toWrite >>> 16);
        buffer[offset + 2] = (byte)(toWrite >>> 8);
        buffer[offset + 3] = (byte)(toWrite);
    }

    private void writePacketSize(byte[] buffer,int size){
        writeIntBytes(buffer, 0, size);
    }

    public abstract void writeToOut();

    public byte[] getBytes(){
        // Escrever para o dataOut
        this.writeToOut();
        byte[] data = bufferOut.toByteArray();
        writePacketSize(data, data.length);
        return data;
    }
}
