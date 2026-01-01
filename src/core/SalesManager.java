package core;

import core.base.Event;
import core.base.Product;
import core.base.WorkDay;
import core.base.ProductEntry;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.*;

public class SalesManager {

    private static final HashMap<Integer, Product> allProducts = new HashMap<>();
    private static final ReadWriteLock productsLock = new ReentrantReadWriteLock();
    // com a funcao avancar do tempo esta variavel precisa de ser vista por todas as threads logo
    private static volatile LocalDate mostRecentDate = LocalDate.now();
    private static final TreeMap<LocalDate, WorkDay> workDaysCache = new TreeMap<>();
    private static final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private static WorkDay currentWorkDay;
    private static DataOutputStream currentDayWriter;
    private static final Lock currentDayLock = new ReentrantLock();
    private static final Condition canAdvance = currentDayLock.newCondition();
    private static final Condition canWork = currentDayLock.newCondition();
    private static final Lock writerLock = new ReentrantLock();

    private static int s; // numero de dias maximos na cache
    private static int d; // numero de dias maximos
    private static boolean initialized = false;
    private static boolean wantsToAdvance = false;
    private static int activeThreads = 0;

    public SalesManager(int s, int d){
        this.s = s;
        this.d = d;
        if(d < 1 || s < 0) throw new RuntimeException();
        if(s >= d) throw new RuntimeException();
        currentDayLock.lock();
        try {
            initCurrentDay(LocalDate.now());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            currentDayLock.unlock();
        }
    }

    private static void initCurrentDay(LocalDate date) throws IOException {

        if (currentDayWriter != null) currentDayWriter.close();

        mostRecentDate = date;
        currentWorkDay = new WorkDay(date);

        File f = new File(date.toString() + ".dat");
        currentDayWriter = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f, true)));
        initialized = true;
    }

    public static boolean registerSale(int productId, int quantity, float price) {
        if (!initialized) throw new IllegalStateException("SalesManager nao esta on!!!");

        currentDayLock.lock();
        try {
            while (wantsToAdvance) {
                canWork.await();
            }
            activeThreads++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            currentDayLock.unlock();
        }

        boolean success = false;
        try {
            Product product = getProduct(productId);
            if (product != null) {
                currentWorkDay.addSale(product, price, quantity);

                writerLock.lock();
                try {
                    if (currentDayWriter != null) {
                        currentDayWriter.writeInt(product.getId());
                        currentDayWriter.writeInt(quantity);
                        currentDayWriter.writeFloat(price);
                        currentDayWriter.flush();
                    }
                } finally {
                    writerLock.unlock();
                }
                success = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            currentDayLock.lock();
            try {
                activeThreads--;
                if (activeThreads == 0) {
                    canAdvance.signalAll();
                }
            } finally {
                currentDayLock.unlock();
            }
        }
        return success;
    }

    public static void advanceDay() throws IOException, InterruptedException {
        currentDayLock.lock();
        try {
            wantsToAdvance = true;
            while (activeThreads > 0) {
                canAdvance.await();
            }

            writerLock.lock();


            try {
                if (currentDayWriter != null) {
                    currentDayWriter.flush();
                    currentDayWriter.close();
                }
                currentWorkDay.close();

                LocalDate nextDay = mostRecentDate.plusDays(1);
                initCurrentDay(nextDay);

                System.out.println("Dia avançado para: " + nextDay);
            } finally {
                writerLock.unlock();
            }

            wantsToAdvance = false;
            canWork.signalAll(); //acordar todos que estao a espera de trabalhar

        } finally {
            currentDayLock.unlock();
        }
    }

    public static WorkDay getDay(LocalDate date) {
        if (date.equals(mostRecentDate)) {
            return currentWorkDay;
        }

        cacheLock.readLock().lock();
        try {
            if (workDaysCache.containsKey(date)) {
                WorkDay wd = workDaysCache.get(date);
                wd.startProcessing();
                return wd;
            }
        } finally {
            cacheLock.readLock().unlock();
        }

        File file = new File(date.toString() + ".dat");
        if (!file.exists()) return null;

        WorkDay loadedDay = loadDayFromFile(date, file);
        if (loadedDay == null) return null;
        cacheLock.writeLock().lock();
        try {
            if (workDaysCache.containsKey(date)) {
                WorkDay wd = workDaysCache.get(date);
                wd.startProcessing();
                return wd;
            }
            if (workDaysCache.size() >= s) {
                if (!makeRoomInCache()) {
                    // se nao houver maneira de libertar espaco na cache processa apenas
                    loadedDay.startProcessing();
                    return loadedDay;
                }
            }
            workDaysCache.put(date, loadedDay);
            loadedDay.startProcessing();
            return loadedDay;
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    private static boolean makeRoomInCache() {
        Iterator<Map.Entry<LocalDate, WorkDay>> it = workDaysCache.entrySet().iterator();
        while (it.hasNext()) {
            WorkDay day = it.next().getValue();
            if (!day.isBeingProcessed()) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public static WorkDay loadDayFromFile(LocalDate date, File file) {
        WorkDay loadedDay = new WorkDay(date);
        loadedDay.close();

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            while (dis.available() > 0) {
                int id = dis.readInt();
                int qtd = dis.readInt();
                float price = dis.readFloat();
                loadedDay.loadEventDisk(id, qtd, price);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return loadedDay;
    }

    public static int getSoldQuantity(int numDays, int productID) throws FileNotFoundException {
        int total = 0;
        int limit = Math.min(numDays, d);
        LocalDate daySnapshot = mostRecentDate;

        currentDayLock.lock();
        try{
            while (wantsToAdvance) {
                canWork.await();
            }
            activeThreads++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            currentDayLock.unlock();
        }

        try {
            for (int i = 1; i <= limit; i++) {
                LocalDate targetDate = daySnapshot.minusDays(i);

                WorkDay day = getDay(targetDate);
                if (day != null) {
                    try {
                        int currQtd = day.getSoldQuantity(productID);
                        if (currQtd != -1)
                            total += day.getSoldQuantity(productID);
                    } finally {
                        day.endProcessing();
                    }
                }
            }
        }finally {
            currentDayLock.lock();
            try {
                activeThreads--;
                if (activeThreads == 0) {
                    canAdvance.signalAll();
                }
            } finally {
                currentDayLock.unlock();
            }
        }

        return total;
    }

    // Nome merdoso
    public static float getTotalMoney(int numDays, int productID){
        float total = 0f;
        int limit = Math.min(numDays, d);
        LocalDate daySnapshot = mostRecentDate;

        currentDayLock.lock();
        try{
            while (wantsToAdvance) {
                canWork.await();
            }
            activeThreads++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            currentDayLock.unlock();
        }
        try {
            for (int i = 1; i <= limit; i++) {
                LocalDate targetDate = daySnapshot.minusDays(i);
                WorkDay day = getDay(targetDate);
                if (day != null) {
                    try {
                        float currTotal = day.getTotal(productID);
                        if (currTotal != -1) total += currTotal;
                    } finally {
                        day.endProcessing();
                    }
                }
            }
        }finally {
            currentDayLock.lock();
            try {
                activeThreads--;
                if (activeThreads == 0) {
                    canAdvance.signalAll();
                }
            } finally {
                currentDayLock.unlock();
            }
        }
        return total;
    }

    // Verificado no excel
    public static float getAveragePrice(int numDays, int productID){
        int limit = Math.min(numDays, d);
        LocalDate daySnapshot = mostRecentDate;

        currentDayLock.lock();
        try{
            while (wantsToAdvance) {
                canWork.await();
            }
            activeThreads++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            currentDayLock.unlock();
        }
        float average = 0.0f;
        int counter = 0;
        try {
            for (int i = 1; i <= limit; i++) {
                LocalDate targetDate = daySnapshot.minusDays(i);
                WorkDay day = getDay(targetDate);
                if (day != null) {
                    try {
                        float currAverage = day.getAveragePrice(productID);
                        if (currAverage != -1) {
                            average += currAverage;
                            counter++;
                        }
                    } finally {
                        day.endProcessing();
                    }
                }
            }
        }finally {
            currentDayLock.lock();
            try {
                activeThreads--;
                if (activeThreads == 0) {
                    canAdvance.signalAll();
                }
            } finally {
                currentDayLock.unlock();
            }
        }
        return counter == 0 ? 0.0f : average / counter;
    }

    public static float getMaxPrice(int numDays, int productID){
        int limit = Math.min(numDays, d);
        LocalDate daySnapshot = mostRecentDate;

        currentDayLock.lock();
        try{
            while (wantsToAdvance) {
                canWork.await();
            }
            activeThreads++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            currentDayLock.unlock();
        }

        float max = 0.0f;
        try {
            for (int i = 1; i <= limit; i++) {
                LocalDate targetDate = daySnapshot.minusDays(i);
                WorkDay day = getDay(targetDate);
                if (day != null) {
                    try {
                        float currMax = day.getHighestPrice(productID);
                        max = Math.max(currMax, max);
                    } finally {
                        day.endProcessing();
                    }
                }
            }
        }finally {
            currentDayLock.lock();
            try {
                activeThreads--;
                if (activeThreads == 0) {
                    canAdvance.signalAll();
                }
            } finally {
                currentDayLock.unlock();
            }
        }
        return max;
    }

    public static Map<Integer, List<Event>> getAllEventsAt(LocalDate date, int[] productIDs){
        WorkDay day = getDay(date);
        if (day == null) return null;
        return day.getAllEventsFromProducts(productIDs);
    }


    // Excluindo o dia atual ja nao da para utilizar por causa da snapshot que tem que ser feita antes do await
    public static List<WorkDay> getLastDays(int numDays){
        return  workDaysCache
                .subMap(mostRecentDate.minusDays(numDays), mostRecentDate.minusDays(1))
                .values().stream().toList();
    }






    public static int createProduct(String productName, float basePrice){
        productsLock.writeLock().lock();
        try{
            Product newProduct = new Product(productName, basePrice);
            allProducts.put(newProduct.getId(), newProduct);
            return newProduct.getId();
        } finally {
            productsLock.writeLock().unlock();
        }
    }

    // Como produtos sao imutáveis podemos retornar direto sem precisar de fazer clone.
    public static Product getProduct(int productId){
        productsLock.readLock().lock();
        try{
            return allProducts.get(productId);
        } finally {
            productsLock.readLock().unlock();
        }
    }

    public static Product getProductByName(String productName){
        productsLock.readLock().lock();
        try {
            for (Product p : allProducts.values()) {
                if (p.getName().equals(productName)) return p;
            }
            return null;
        } finally {
            productsLock.readLock().unlock();
        }
    }


    public static WorkDay getCurWorkDay(){
        currentDayLock.lock();
        try{
            return currentWorkDay;
        }finally{
            currentDayLock.unlock();
        }
    }

}
