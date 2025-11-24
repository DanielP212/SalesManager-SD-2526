package core;

import core.base.WorkDay;

import javax.xml.crypto.Data;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SalesManager {
    private static LocalDate mostRecentDate = LocalDate.now();
    private static final TreeMap<LocalDate, WorkDay> workDaysCache = new TreeMap<>();
    private static ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private static int s; // numero de dias maximos na cache
    private static int d; // numero de dias maximos

    public SalesManager(int s, int d){
        this.s = s;
        this.d = d;
        if(d < 1 || s < 0) throw new RuntimeException();
        if(s >= d) throw new RuntimeException();
    }

    public static WorkDay getDay(LocalDate date) throws FileNotFoundException {
        cacheLock.readLock().lock();
        try{
            // hit
            if(workDaysCache.containsKey(date)){
                return workDaysCache.get(date);
            }
        }finally { cacheLock.readLock().unlock(); }

        File file = new File(date.toString()+".dat");
        if(!file.exists()){
            System.out.println("tas a trolar 2");
            return null;
        }

        cacheLock.writeLock().lock();
        try{
            if(workDaysCache.containsKey(date)) return workDaysCache.get(date);

            if(workDaysCache.size() < s){
                return loadToCache(date, file);
            }
        }finally {
            cacheLock.writeLock().unlock();
        }
        // retorna null se nao tiver espaco na cache
        System.out.println("Nao tem espaco na cache");
        return null;
    }

    private static WorkDay loadToCache(LocalDate date, File file) throws FileNotFoundException {
        WorkDay loadedDay = new WorkDay(date);

        try(DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))){
            while(dis.available() > 0){
                int id = dis.readInt();
                int qtd = dis.readInt();
                float price = dis.readFloat();
                loadedDay.loadEventDisk(id, qtd, price);
            }
        }catch (IOException e){
            return null;
        }
        workDaysCache.put(date, loadedDay);
        return loadedDay;
    }

    public static int getSoldQuantity2(int numDays, int productID) throws FileNotFoundException {
        int qtdTotal = 0;

        if(numDays > d) numDays = d;

        for(int i = 1; i <= numDays ; i++){
            LocalDate targetDate = mostRecentDate.minusDays(i);
            System.out.println(targetDate);
            WorkDay day = getDay(targetDate);

            // conseguiu espaco na cache
            if(day != null){
                System.out.println("Conseguimos cache");
                qtdTotal += day.getSoldQuantity(productID);
                System.out.println("Quantidade desta cache"+ qtdTotal);
            }
            // streaming do disco
            else{
                System.out.println("Stream da quantidade no ficheiro");
                qtdTotal += streamQtdDisk(targetDate, productID);
            }
        }
        System.out.println("Quantidade total "+ qtdTotal);
        return qtdTotal;
    }

    public static int streamQtdDisk(LocalDate date, int targetId){
        int count = 0;
        File file = new File(date.toString()+".dat");
        System.out.println(date.toString()+".dat");
        if(!file.exists()) {
            System.out.println("File nao existe");
            return 0;
        }

        try(DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))){
            while(dis.available() > 0){
                int id = dis.readInt();
                int qtd = dis.readInt();
                float price = dis.readFloat();
                if(id == targetId) count += qtd;
            }
        }catch (IOException e){
            return 0;
        }
        return count;

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

    public static int getSoldQuantity(int numDays, int productID){
        ArrayList<WorkDay> daysToQuery = (ArrayList<WorkDay>) getLastDays(numDays);
        return daysToQuery.stream()
                .mapToInt(w -> w.getSoldQuantity(productID))
                .sum();
    }

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
