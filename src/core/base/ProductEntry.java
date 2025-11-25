package core.base;

import java.time.LocalDate;
import java.util.*;

class Event{
    public float sellPrice;
    public int quantity;

    public Event(float sellPrice, int quantity){
        this.sellPrice = sellPrice;
        this.quantity = quantity;
    }

    public float getTotal(){ return sellPrice * quantity; }

    @Override
    public String toString() {
        return "Event{" +
                "sellPrice=" + sellPrice +
                ", quantity=" + quantity +
                '}';
    }
}


public class ProductEntry implements Cloneable{
    private final int productID;
    private final LocalDate sellDate;
    // Evento tem preco e quantidade
    private final List<Event> sellEvents;

    // TODO depois no caching todos os getters dos valores em cache teem de ser sincronos
    public ProductEntry(Product p, LocalDate d){
        this.sellDate = d;
        this.productID = p.getId();
        this.sellEvents = new ArrayList<>();
    }

    public ProductEntry(int id, LocalDate d){
        this.sellDate = d;
        this.productID = id;
        this.sellEvents = new ArrayList<>();
    }

    public ProductEntry(Product p, float sellPrice, int quantity, LocalDate d){
        this.sellDate = d;
        this.productID = p.getId();
        this.sellEvents = new ArrayList<>();
        this.sellEvents.add(new Event(sellPrice, quantity));
    }


    public ProductEntry(ProductEntry pe){
        this.productID = pe.productID;
        this.sellDate = pe.getSellDate();
        this.sellEvents = new ArrayList<>(pe.getEvents());
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductEntry that = (ProductEntry) o;
        return productID == that.productID && Objects.equals(sellDate, that.sellDate);
    }

    protected ProductEntry clone() {
        return new ProductEntry(this);
    }

    public float getTotal(){
        return (float) sellEvents.stream().mapToDouble(Event::getTotal).sum();
    }

    public float getHighestPrice() {
        if (sellEvents.isEmpty()) return 0.0f;
        return Collections.max(sellEvents.stream().map(e->e.sellPrice).toList());
    }

    public float getAveragePrice(){
        return (float) (sellEvents.stream()
                        .mapToDouble(Event::getTotal).sum()
                / getQuantitySold());
    }

    public int getQuantitySold(){
        return sellEvents.stream().mapToInt(e -> e.quantity).sum();
    }

    public int getProductID(){ return productID; }
    public LocalDate getSellDate(){ return sellDate; }

    private List<Event> getEvents() {
        return this.sellEvents;
    }
    public void addSellEvent(float newPrice, int quantity){
        sellEvents.add(new Event(newPrice, quantity));
    }
}
