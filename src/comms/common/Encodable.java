package comms.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.List;

public abstract class Encodable {
    protected ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
    protected DataOutputStream dataOut = new DataOutputStream(bufferOut);

    protected ByteArrayInputStream bufferIn;
    protected DataInputStream dataIn;

    public static final byte BYTE_TYPE = 0x00;
    public static final byte INTEGER_TYPE = 0x01;
    public static final byte FLOAT_TYPE = 0x02;
    public static final byte DOUBLE_TYPE = 0x03;
    public static final byte CHAR_TYPE = 0x04;
    public static final byte STRING_TYPE = 0x05;
    public static final byte COORDINATE_TYPE = 0x06;
    public static final byte ARRAY_TYPE = 0x10;

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

    public static void writeIntBytes(List<Byte> bytesList, int toWrite){
        bytesList.add((byte)(toWrite >>> 24));
        bytesList.add((byte)(toWrite >>> 16));
        bytesList.add((byte)(toWrite >>> 8));
        bytesList.add((byte)(toWrite));
    }

    public static int writeIntArray(byte[] buffer, int offset, int[] toWrite){
        writeIntBytes(buffer, offset, toWrite.length);
        offset += 4;
        for (int i = 0; i < toWrite.length; i++, offset+=4){
            writeIntBytes(buffer, offset, toWrite[i]);
        }
        return offset;
    }

    public static void writeIntArray(List<Byte> bytesList, int[] toWrite){
        writeIntBytes(bytesList, toWrite.length);
        for (int j : toWrite) {
            writeIntBytes(bytesList, j);
        }
    }

    // retorna o novo offset
    public static int writeString(byte[] buffer, int offset, String toWrite){
        int stringSize = toWrite.length();
        buffer[offset++] = (byte)stringSize;
        for (int i = 0; i < stringSize; i++) buffer[offset++] = (byte)toWrite.charAt(i);
        return offset;
    }

    public static void writeString(List<Byte> bytesList, String toWrite){
        int stringSize = toWrite.length();
        bytesList.add((byte)toWrite.length());
        for (int i = 0; i < stringSize; i++){
            bytesList.add((byte)toWrite.charAt(i));
        }
    }


    public static String readString(ByteBuffer buffer){
        int stringSize = buffer.get();
        if (stringSize <= 0) return null;
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < stringSize; i++){
            sBuilder.append((char)buffer.get());
        }
        return sBuilder.toString();
    }

    public static int writeStringArray(byte[] buffer, int offset, String[] strings){
        int arraySize = strings.length;
        writeIntBytes(buffer, offset, arraySize);
        offset+=4;
        for (int i = 0; i < arraySize; i++) offset = writeString(buffer, offset, strings[i]);
        return offset;
    }

    public static String[] readStringArray(ByteBuffer buffer){
        int arraySize = buffer.getInt();
        String[] result = new String[arraySize];
        for (int i = 0; i < arraySize; i++){
            result[i] = readString(buffer);
        }
        return result;
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
