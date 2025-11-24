package core;

import core.base.Product;
import core.base.WorkDay;

import javax.xml.crypto.Data;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SalesManager {
    //TODO (a concorrencia deve estar ok) fazer requests para adicionar uma venda ao dia atual e para passar de dia a usar essas funcoes em baixo (esta apenas cliente admin) para ja que se foda

    // com a funcao avancar do tempo esta variavel precisa de ser vista por todas as threads logo
    private static volatile LocalDate mostRecentDate = LocalDate.now();
    private static final TreeMap<LocalDate, WorkDay> workDaysCache = new TreeMap<>();
    private static ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    private static WorkDay currentWorkDay;
    private static DataOutputStream currentDayWriter;
    private static final Lock currentDayLock = new ReentrantLock();

    private static int s; // numero de dias maximos na cache
    private static int d; // numero de dias maximos
    private static boolean initialized = false;

    public SalesManager(int s, int d){
        this.s = s;
        this.d = d;
        if(d < 1 || s < 0) throw new RuntimeException();
        if(s >= d) throw new RuntimeException();
        try {
            initCurrentDay(LocalDate.now());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initCurrentDay(LocalDate date) throws IOException {
        currentDayLock.lock();
        try {
            if (currentDayWriter != null) currentDayWriter.close();

            mostRecentDate = date;
            currentWorkDay = new WorkDay(date);

            File f = new File(date.toString() + ".dat");
            currentDayWriter = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f, true)));
        } finally {
            currentDayLock.unlock();
        }
    }

    public static void registerSale(String productName, int quantity, float price) {
        if (!initialized) throw new IllegalStateException("SalesManager nao esta on!!!");

        currentDayLock.lock();
        try {
            Product product = new Product(productName);
            for(int i = 0 ; i < quantity ; i++){
                currentWorkDay.addSale(product, price);
            }

            if (currentDayWriter != null) {
                currentDayWriter.writeInt(product.getId());
                currentDayWriter.writeInt(quantity);
                currentDayWriter.writeFloat(price);
                currentDayWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            currentDayLock.unlock();
        }
    }

    public static void advanceDay() throws IOException {
        currentDayLock.lock();
        try {

            if (currentDayWriter != null) currentDayWriter.close();
            currentWorkDay.close();

            LocalDate nextDay = mostRecentDate.plusDays(1);

            initCurrentDay(nextDay);

            System.out.println("Dia avançado para: " + nextDay);
        } finally {
            currentDayLock.unlock();
        }
    }

    public static WorkDay getDay(LocalDate date) throws FileNotFoundException {

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

    private static WorkDay loadDayFromFile(LocalDate date, File file) {
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

        for (int i = 1; i <= limit; i++) {
            LocalDate targetDate = mostRecentDate.minusDays(i);

            WorkDay day = getDay(targetDate);
            if (day != null) {
                try {
                    total += day.getSoldQuantity(productID);
                } finally {
                    day.endProcessing();
                }
            }
        }
        return total;
    }

    public WorkDay getCurrentDay(){
        return workDaysCache.get(mostRecentDate);
    }

    // Excluindo o dia atual
    public static List<WorkDay> getLastDays(int numDays){
        return  workDaysCache
                .subMap(mostRecentDate.minusDays(numDays), mostRecentDate.minusDays(1))
                .values().stream().toList();
    }
/*
    public static int oldGetSoldQuantity(int numDays, int productID){
        ArrayList<WorkDay> daysToQuery = (ArrayList<WorkDay>) getLastDays(numDays);
        return daysToQuery.stream()
                .mapToInt(w -> w.getSoldQuantity(productID))
                .sum();
    }
*/
    // Nome merdoso
    public static float getTotalMoney(int numDays, int productID){
        ArrayList<WorkDay> daysToQuery = (ArrayList<WorkDay>) getLastDays(numDays);
        return (float)daysToQuery.stream()
                .mapToDouble(w -> w.getTotal(productID))
                .sum();
    }

    // Verificado no excel
    public static float getMedianPrice(int numDays, int productID){
        ArrayList<WorkDay> daysToQuery = (ArrayList<WorkDay>) getLastDays(numDays);
        return (float)daysToQuery.stream()
                .mapToDouble(w -> w.getMedianPrice(productID))
                .sum() / numDays;
    }

    public static float getMaxPrice(int numDays, int productID){
        return Collections.max(getLastDays(numDays).stream()
                .map(w->w.getHighestPrice(productID)).toList());
    }
}
