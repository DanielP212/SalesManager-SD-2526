package core.base;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Isto aqui deve estar a funcionar perfeito para concorrência
public class WorkDay {
    // TODO ainda precisa de uma lista de eventos
    private final LocalDate date;
    private final Map<Integer, ProductEntry> workdayEntries = new HashMap<>();
    private final ReadWriteLock productLock = new ReentrantReadWriteLock();

    // Se o dia ja acabou
    boolean closed = false;

    public WorkDay(LocalDate date){ this.date = date; }


    public List<ProductEntry> getEntries(){
        productLock.readLock().lock();
        try{
            return workdayEntries.values().stream().map(ProductEntry::clone).toList();
        } finally {
            productLock.readLock().unlock();
        }
    }

    public void addSale(Product p, float sellPrice){
        if (closed) return;
        productLock.writeLock().lock();
        try{
            ProductEntry entry = workdayEntries.get(p.getId());
            if (entry == null) {
                workdayEntries.put(p.getId(), new ProductEntry(p, sellPrice, date));
            } else {
                workdayEntries.get(p.getId()).addSellPrice(sellPrice);
            }
        } finally {
            productLock.writeLock().unlock();
        }
    }

    public void loadEventDisk(int id, int qtd, float price){
        productLock.writeLock().lock();
        try{
            ProductEntry productEntry = workdayEntries.get(id);
            if(productEntry == null){
                productEntry = new ProductEntry(id, date);
            }
            for(int i = 0 ; i < qtd ; i++){
                productEntry.addSellPrice(price);
            }
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

    public float getMedianPrice(int productID){
        ProductEntry entry = getEntryToRead(productID);
        if (entry == null) return -1;
        return entry.getMedianPrice();
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

    // Como esta a retornar um clone so precisa de lock aqui.
    // Se nao for para retornar um clone tem de se meter o lock onde se for usar isto
    // Na operação toda.
    public ProductEntry getEntryToRead(int productID){
        productLock.readLock().lock();
        try {
            ProductEntry entry = workdayEntries.get(productID);
            return entry != null ? entry.clone() : null;
        }finally {
            productLock.readLock().unlock();
        }
    }

    public void close(){ this.closed = true; }
    public LocalDate getDate(){ return date; }
}
