package client.answers;

import comms.common.Encodable;
import comms.common.PacketType;
import core.base.Event;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterAnswer extends Answer{
    private final Map<String, List<Event>> events = new HashMap<>();

    public FilterAnswer(PacketType type, byte[] data){
        super(type);
        ByteBuffer buffer = ByteBuffer.wrap(data);
        while(buffer.hasRemaining()){
            String productName = Encodable.readString(buffer);
            ArrayList<Event> eventList = new ArrayList<>();
            int eventNum = buffer.getInt();
            for (int i = 0; i < eventNum; i++){
                float price = buffer.getFloat();
                int quantity = buffer.getInt();
                Event e = new Event(price, quantity);
                eventList.add(e);
            }
            events.put(productName, eventList);
        }
    }

    @Override
    public String toString() {
        return events.toString();
    }
}
