package server.requests;

import comms.Packet;
import comms.common.PacketType;

import java.nio.ByteBuffer;

public abstract class Request {
    protected int requesterClient;
    protected PacketType type;


    /**
     * Executa o request do cliente no servidor
     * @return resultado ou null caso de erro
     */
    public abstract byte[] execute();

    // Para ser chamado no cliente!
    public abstract String getAnswer();

    public static Request fromPacket(Packet p){
        Request req = switch (p.getType()){
            case LOGIN -> new LoginRequest(p.getData());
            case REGISTER -> new RegisterRequest(p.getData());
            case null, default -> null;
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
    public static String getString(ByteBuffer buffer){
        int stringSize = buffer.get();
        StringBuilder sBuilder = new StringBuilder();
        for (int i = 0; i < stringSize; i++)
            sBuilder.append((char)buffer.get());
        return sBuilder.toString();
    }

    public static byte getByte(ByteBuffer buffer){
        return buffer.get();
    }

    public static int getInt(ByteBuffer buffer){
        String maybeInt = getString(buffer);
        try{
            return Integer.parseInt(maybeInt);
        } catch (NumberFormatException e) {
            System.out.println("[REQUEST FORMATING] Error parsing int");
            throw new RuntimeException(e);
        }
    }

    // TODO a ver se nao me esqueço de fazer isto depois
    public static Packet errorPacket(){

        return null;
    }
}
