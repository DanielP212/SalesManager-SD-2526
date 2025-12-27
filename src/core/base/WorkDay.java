package core.base;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// Isto aqui deve estar a funcionar perfeito para concorrência
public class WorkDay {
    private final LocalDate date;
    private final Map<Integer, ProductEntry> workdayEntries = new HashMap<>();
    private final ReadWriteLock productLock = new ReentrantReadWriteLock();

    //para notificacoes concorrentes
    private final Lock concNLock = new ReentrantLock();
    private final Map<Integer, ConcSaleNotification> concWaiting = new HashMap<>();
    private int lastSoldPID = -1;
    private int lastSoldPCount = 0;

    //para notificacoes sequenciais
    private final Lock seqNLock = new ReentrantLock();
    private final Map<Integer, SeqSaleNotification> seqWaiting = new HashMap<>();



    private final AtomicInteger activeReaders = new AtomicInteger(0);
    // Se o dia ja acabou
    private final ReadWriteLock closedLock = new ReentrantReadWriteLock();
    private boolean closed = false;

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
        this.closedLock.readLock().lock();
        boolean r = this.closed;
        this.closedLock.readLock().unlock();
        if (r) return;

        concNLock.lock();
        if (p.getId() == lastSoldPID){
            lastSoldPCount++;
            if(concWaiting.containsKey(lastSoldPCount)){
                ConcSaleNotification cn = concWaiting.get(lastSoldPCount); 
                cn.notLock.lock();
                cn.setPName(p.getName());
                cn.cond.signalAll();
                cn.notLock.unlock();
            }
        }
        else{
            lastSoldPCount = 1;
            lastSoldPID = p.getId();
        }
        
        concNLock.unlock();

        seqNLock.lock();
        if(seqWaiting.containsKey(p.getId())){
            SeqSaleNotification sn = seqWaiting.get(p.getId());
            sn.notLock.lock();
            sn.soldProd();
            sn.cond.signalAll();
            sn.notLock.unlock();
        }
        seqNLock.unlock();

        
        
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

    public Map<Integer, List<Event>> getAllEventsFromProducts(int[] productIDs){
        Map<Integer, List<Event>> eventsMap = new HashMap<>();
        for (int i = 0; i < productIDs.length; i++){
            if (productIDs[i] == -1){
                eventsMap.put(productIDs[i], null);
            }
            eventsMap.put(productIDs[i], getAllEventsFrom(productIDs[i]));
        }
        return eventsMap;
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

            this.closedLock.readLock().lock();
            boolean r = this.closed;
            this.closedLock.readLock().unlock();

            if (r) return entry;
            return entry != null ? entry.clone() : null;
        }finally {
            productLock.readLock().unlock();
        }
    }

    public void close(){ 
        this.closedLock.writeLock().lock();
        this.closed = true;
        this.closedLock.writeLock().unlock();

        concNLock.lock();
        for (Integer n : concWaiting.keySet()) {
            concWaiting.get(n).cond.signalAll();
        }

        seqNLock.lock();
        for (Integer pID : seqWaiting.keySet()) {
            seqWaiting.get(pID).cond.signalAll();
        }

        seqNLock.unlock();
        concNLock.unlock();

    }

    public LocalDate getDate(){ return date; }
    
    public boolean isClosed(){ 
        this.closedLock.readLock().lock();
        try{
            return closed;
        }finally{
            this.closedLock.readLock().unlock();
        }
    }

    public ConcSaleNotification addConcNotification(int n){
        ConcSaleNotification cn = null;
        concNLock.lock();
        try{
            if(concWaiting.containsKey(n)){
                cn = concWaiting.get(n);
                cn.notLock.lock();
                cn.incWaiters();
                return concWaiting.get(n);
            }
            else{
                ConcSaleNotification notification = new ConcSaleNotification(n);
                notification.incWaiters();
                concWaiting.put(n, notification);
                return notification;
            }
        }finally{
            if(cn != null) cn.notLock.unlock();
            concNLock.unlock();
        }
    }

    public void removeConcNotification(int n){
        ConcSaleNotification cn = null;
        concNLock.lock();
        try{
            if(concWaiting.containsKey(n)){
                cn = concWaiting.get(n);
                cn.notLock.lock();
                if(cn.noWaiters()) concWaiting.remove(n);
            }
        }finally{
            if(cn != null) cn.notLock.unlock();
            concNLock.unlock();
        }
    }


    public SeqSaleNotification addSeqSaleNotification(int pID){
        SeqSaleNotification sn = null;
        seqNLock.lock();
        try{
            if(seqWaiting.containsKey(pID)){
                sn = seqWaiting.get(pID);
                sn.notLock.lock();
                sn.incWaiters();
                return seqWaiting.get(pID);
            }
            else{
                SeqSaleNotification notification = new SeqSaleNotification(pID);
                notification.incWaiters();
                seqWaiting.put(pID, notification);
                return notification;
            }
        }finally{
            if(sn!=null) sn.notLock.unlock();
            seqNLock.unlock();
        }
    }

    public void removeSeqNotification(int pID){
        SeqSaleNotification sn = null;
        seqNLock.lock();
        try{
            if(seqWaiting.containsKey(pID)){
                sn = seqWaiting.get(pID);
                sn.notLock.lock();
                if(sn.noWaiters()) seqWaiting.remove(pID);
            }
        }finally{
            if(sn != null) sn.notLock.unlock();
            seqNLock.unlock();
        }
    }

}
