package core;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WorkDay {
    // TODO ainda precisa de uma lista de eventos
    private final LocalDate date;
    private final Map<Integer, ProductEntry> workdayEntries = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public WorkDay(LocalDate date){ this.date = date; }


    // TODO isto tem de retornar uma deep copy
    public List<ProductEntry> getEntries(){
        lock.readLock().lock();
        try{
            return workdayEntries.values().stream().toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addSale(Product p, float sellPrice){
        lock.writeLock().lock();
        try{
            if (!wasProductSold(p.getId())) {
                workdayEntries.put(p.getId(), new ProductEntry(p, sellPrice, date));
            }
            else {
                workdayEntries.get(p.getId()).addSellPrice(sellPrice);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    public int getSoldQuantity(int productID){
        ProductEntry entry = getEntry(productID);
        if (entry == null) return -1;
        return entry.getQuantitySold();
    }

    public float getMedianSellPrice(int productID){
        ProductEntry entry = getEntry(productID);
        if (entry == null) return -1;
        return entry.getMedianPrice();
    }

    public float getHighestSellPrice(int productID){
        ProductEntry entry = getEntry(productID);
        if (entry == null) return -1;
        return entry.getHighestPrice();
    }

    public boolean wasProductSold(int productID){
        lock.readLock().lock();
        try {
            return workdayEntries.containsKey(productID);
        } finally {
            lock.readLock().unlock();
        }
    }

    // Isto é so para dar read. Nao mexer nos valores da entry (vou meter a fazer clone depois)
    // terá de retornar uma deep copy da entry para nao precisar de uma readLock
    public ProductEntry getEntry(int productID){
        lock.readLock().lock();
        try {
            return workdayEntries.get(productID).clone();
        }finally {
            lock.readLock().unlock();
        }
    }
    public LocalDate getDate(){ return date; }
}
