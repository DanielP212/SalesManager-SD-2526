package core.base;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class ProductEntry implements Cloneable{
    private final int productID;
    private final LocalDate sellDate;
    // Evento tem preco e quantidade
    private final List<Event> sellEvents;

    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private Integer cachedQuantity = null;
    private Float cachedHighestPrice = null;
    private Float cachedAveragePrice = null;
    private Float cachedTotal = null;

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
        cacheLock.readLock().lock();
        try{
            if (cachedTotal != null) return cachedTotal;
        } finally {
            cacheLock.readLock().unlock();
        }

        // Caso nao seja sido calculado
        // Double Checking, acho que e preciso!
        cacheLock.writeLock().lock();
        try{
            if (cachedTotal != null) return cachedTotal;
            float result = (float) sellEvents.stream().mapToDouble(Event::getTotal).sum();
            cachedTotal = result;
            return result;
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public float getHighestPrice() {
        cacheLock.readLock().lock();
        try{
            if (cachedHighestPrice != null) return cachedHighestPrice;
        } finally {
            cacheLock.readLock().unlock();
        }

        cacheLock.writeLock().lock();
        try{
            if (cachedHighestPrice != null) return cachedHighestPrice;
            float result = 0.0f;
            if (!sellEvents.isEmpty())
                result = Collections.max(sellEvents.stream().map(e->e.sellPrice).toList());

            cachedHighestPrice = result;
            return result;
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public float getAveragePrice(){
        cacheLock.readLock().lock();
        try{
            if (cachedAveragePrice != null) return cachedAveragePrice;
        } finally {
            cacheLock.readLock().unlock();
        }

        cacheLock.writeLock().lock();
        try{
            if (cachedAveragePrice != null) return cachedAveragePrice;
            float result = (float) (sellEvents.stream()
                    .mapToDouble(Event::getTotal).sum()
                    / getQuantitySold());
            cachedAveragePrice = result;
            return result;
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public int getQuantitySold(){
        cacheLock.readLock().lock();
        try{
            if (cachedQuantity != null) return cachedQuantity;
        } finally {
            cacheLock.readLock().unlock();
        }

        cacheLock.writeLock().lock();
        try{
            if (cachedQuantity != null) return cachedQuantity;
            int result = sellEvents.stream().mapToInt(e -> e.quantity).sum();
            cachedQuantity = result;
            return result;
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    public int getProductID(){ return productID; }
    public LocalDate getSellDate(){ return sellDate; }

    public List<Event> getEvents() {
        return this.sellEvents;
    }
    public void addSellEvent(float newPrice, int quantity){
        sellEvents.add(new Event(newPrice, quantity));
    }
}
