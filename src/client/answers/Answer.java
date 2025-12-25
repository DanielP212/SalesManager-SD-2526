package client.answers;

import comms.Packet;
import comms.common.PacketType;
import server.requests.*;


public abstract class Answer {
    protected PacketType type;

    public Answer(PacketType type){
        this.type = type;
    }


    public static Answer fromPacket(Packet p){
        return switch (p.getType()){
            case LOGIN -> new LoginAnswer(p.getType(), p.getData());
            case REGISTER -> new RegisterAnswer(p.getType(), p.getData());
            case QUERY_QTD, QUERY_TOTAL, QUERY_MAX, QUERY_AVG -> new QueryAnswer(p.getType(), p.getData());
            case ADD_SALE -> new AddSaleAnswer(p.getType(), p.getData());
            case CREATE_PRODUCT -> new CreateProductAnswer(p.getType(), p.getData());
        };
    }


    public abstract String toString();

}
