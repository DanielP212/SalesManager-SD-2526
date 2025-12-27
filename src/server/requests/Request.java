package server.requests;

import comms.Packet;
import comms.common.PacketType;

import java.nio.ByteBuffer;
import java.time.DateTimeException;
import java.time.LocalDate;

public abstract class Request {
    protected int requesterClient;
    protected PacketType type;


    /**
     * Executa o request do cliente no servidor
     * @return resultado ou null caso de erro
     */
    public abstract byte[] execute();

    public static Request fromPacket(Packet p){
        Request req = switch (p.getType()){
            case LOGIN -> new LoginRequest(p.getData());
            case REGISTER -> new RegisterRequest(p.getData());
            case QUERY_QTD, QUERY_TOTAL, QUERY_MAX, QUERY_AVG -> new QueryRequest(p.getData());
            case ADD_SALE -> new AddSaleRequest(p.getData());
            case CREATE_PRODUCT -> new CreateProductRequest(p.getData());
            case NOTIFY_SEQ -> new NotifySeqRequest(p.getData());
            case NOTIFY_CONC -> new NotifyConcRequest(p.getData());
            case null -> null;
        };
        if (req == null) return null;
        req.requesterClient = -1;
        req.type = p.getType();
        return req;
    }

    public static Request fromPacket(int clientID, Packet p){
         Request req = fromPacket(p);
         if (req == null) return null;
         req.requesterClient = clientID;
         return req;
    }


    // Funcao para receber uma string do buffer
    public String readString(ByteBuffer buffer){
        int stringSize = buffer.get();
        if (stringSize <= 0) return null;
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < stringSize; i++)
            sBuilder.append((char)buffer.get());
        return sBuilder.toString();
    }

    public String[] readStringArray(ByteBuffer buffer){
        int arraySize = buffer.getInt();
        String[] result = new String[arraySize];
        for (int i = 0; i < arraySize; i++){
            result[i] = readString(buffer);
        }
        return result;
    }

    public byte getByte(ByteBuffer buffer){
        return buffer.get();
    }

    public int getInt(ByteBuffer buffer){
        if (requesterClient == -1) return buffer.getInt();
        String maybeInt = readString(buffer);
        try{
            assert maybeInt != null;
            return Integer.parseInt(maybeInt);
        } catch (NumberFormatException e) {
            System.out.println("[REQUEST FORMATING] Error parsing int: " + maybeInt);
            throw new RuntimeException(e);
        }
    }

    public int[] getIntArray(ByteBuffer buffer){
        int arraySize = buffer.getInt();
        int[] result = new int[arraySize];
        for (int i = 0; i < arraySize; i++){
            result[i] = getInt(buffer);
        }
        return result;
    }

    public LocalDate getDate(ByteBuffer buffer){
        String maybeDate = readString(buffer);
        try {
            return LocalDate.parse(maybeDate);
        } catch (DateTimeException e){
            System.out.println("[REQUEST FORMATTING] Error parsing date");
            throw new RuntimeException(e);
        }
    }

    public float getFloat(ByteBuffer buffer){
        if (requesterClient == -1) return buffer.getFloat();
        String maybeFloat = readString(buffer);
        try{
            return Float.parseFloat(maybeFloat);
        } catch (NumberFormatException e){
            System.out.println("[REQUEST FORMATING] Error parsing float");
            throw new RuntimeException(e);

        }
    }

    // TODO a ver se nao me esqueço de fazer isto depois
    public static Packet errorPacket(){

        return null;
    }
}
