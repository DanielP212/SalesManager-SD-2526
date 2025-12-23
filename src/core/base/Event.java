package core.base;

public class Event{
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
