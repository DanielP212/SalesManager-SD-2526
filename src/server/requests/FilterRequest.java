package server.requests;

import comms.common.Encodable;
import core.SalesManager;
import core.base.Event;
import core.base.WorkDay;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterRequest extends Request{
    private final ByteBuffer buffer;

    protected FilterRequest(byte[] data){ this.buffer = ByteBuffer.wrap(data); }


    // TODO Funcao nao testada
    @Override
    public byte[] execute() {
        if (requesterClient == -1) return null;
        LocalDate date = getDate(buffer);
        int[] productsQueried = getIntArray(buffer);
        Map<Integer, List<Event>> productsEvents = SalesManager.getAllEventsAt(date, productsQueried);
        if (productsEvents == null) return null;

        int offset = 0;
        List<Byte> resultList = new ArrayList<>();
        for (Map.Entry<Integer, List<Event>> productEvent : productsEvents.entrySet()) {
            Encodable.writeIntBytes(resultList, productEvent.getKey());
            Map<Float, Integer> collapsedEvents = new HashMap<>();
            for (Event event : productEvent.getValue()) {
                if (!collapsedEvents.containsKey(event.sellPrice)){
                    collapsedEvents.put(event.sellPrice, event.quantity);
                }
                else collapsedEvents.computeIfPresent(event.sellPrice, (p, q) -> q + event.quantity);
            }

            Encodable.writeIntBytes(resultList, collapsedEvents.size()); // escrever numero de eventos
            for (Map.Entry<Float, Integer> sellQuantityPair : collapsedEvents.entrySet()) {
                // escrever eventos colapsados
                Encodable.writeIntBytes(resultList, Float.floatToIntBits(sellQuantityPair.getKey()));
                Encodable.writeIntBytes(resultList, sellQuantityPair.getValue());
            }
        }

        byte[] result = new byte[resultList.size()];
        int counter = 0;
        for (Byte b : resultList) {
            result[counter++] = b;
        }

        return result;
    }

    public String getAnswer() {
        // ao ler tem de ser por ordem:
        // id do produto
        // numero de entradas de evento (preco, quantidade)
        // preco
        // quantidade
        // ...
        // id do produto
        // ...
        return "";
    }
}
