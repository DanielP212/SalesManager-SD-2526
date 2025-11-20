package server.requests;

import comms.Packet;

public abstract class Request {

    // Retorna os dados para mandar aqui
    public abstract byte[] execute();

    // Para ser chamado no cliente!
    public abstract String getAnswer();

    public static Request fromPacket(Packet p){
        return switch (p.getType()){
            case LOGIN -> new LoginRequest(p.getData());
            case null, default -> null;
        };
    }
}
