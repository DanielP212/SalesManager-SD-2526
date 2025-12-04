package core.base;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Isto aqui deve estar a funcionar perfeito para concorrência
public class WorkDay {
    private final LocalDate date;
    private final Map<Integer, ProductEntry> workdayEntries = new HashMap<>();
    private final ReadWriteLock productLock = new ReentrantReadWriteLock();

    private final AtomicInteger activeReaders = new AtomicInteger(0);
    // Se o dia ja acabou
    boolean closed = false;

    public void startProcessing() {
        activeReaders.incrementAndGet();
    }

    public void endProcessing() {
        activeReaders.decrementAndGet();
    }

    public boolean isBeingProcessed() {
        return activeReaders.get() > 0;
    }

    public WorkDay(LocalDate date){ this.date = date; }


    public List<ProductEntry> getEntries(){
        productLock.readLock().lock();
        try{
            return workdayEntries.values().stream().map(ProductEntry::clone).toList();
        } finally {
            productLock.readLock().unlock();
        }
    }

    public void addSale(Product p, float sellPrice, int quantity){
        if (closed) return;
        productLock.writeLock().lock();
        try{
            ProductEntry entry = workdayEntries.get(p.getId());
            if (entry == null) {
                workdayEntries.put(p.getId(), new ProductEntry(p, sellPrice, quantity, date));
            } else {
                workdayEntries.get(p.getId()).addSellEvent(sellPrice, quantity);
            }
        } finally {
            productLock.writeLock().unlock();
        }
    }

    public List<Event> getAllEventsFrom(int productID){
        ProductEntry productEntry = getEntryToRead(productID);
        if (productEntry == null) return null;
        return productEntry.getEvents();
    }

    public void loadEventDisk(int id, int qtd, float price){
        productLock.writeLock().lock();
        try{
            ProductEntry productEntry = workdayEntries.get(id);
            if(productEntry == null){
                productEntry = new ProductEntry(id, date);
            }
            //System.out.println(qtd);
            productEntry.addSellEvent(price, qtd);
            workdayEntries.put(id,productEntry);
        }finally {
            productLock.writeLock().unlock();
        }
    }

    public int getSoldQuantity(int productID){
        ProductEntry entry = getEntryToRead(productID);
        if (entry == null) return -1;
        return entry.getQuantitySold();
    }

    public float getAveragePrice(int productID){
        ProductEntry entry = getEntryToRead(productID);
        if (entry == null) return -1;
        return entry.getAveragePrice();
    }

    public float getHighestPrice(int productID){
        ProductEntry entry = getEntryToRead(productID);
        if (entry == null) return -1;
        return entry.getHighestPrice();
    }

    public float getTotal(int productID){
        ProductEntry entry = getEntryToRead(productID);
        if (entry == null) return -1;
        return entry.getTotal();
    }

    // Verifica se um produto foi vendido neste dia
    public boolean wasProductSold(int productID){
        productLock.readLock().lock();
        try {
            return workdayEntries.containsKey(productID);
        } finally {
            productLock.readLock().unlock();
        }
    }

    public ProductEntry getEntryToRead(int productID){
        productLock.readLock().lock();
        try {
            ProductEntry entry = workdayEntries.get(productID);
            if (closed) return entry;
            return entry != null ? entry.clone() : null;
        }finally {
            productLock.readLock().unlock();
        }
    }

    public void close(){ this.closed = true; }
    public LocalDate getDate(){ return date; }
}
