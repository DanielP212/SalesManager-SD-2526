package core;

import core.base.WorkDay;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class SalesManager {
    private static LocalDate mostRecentDate;
    private static final TreeMap<LocalDate, WorkDay> workDays = new TreeMap<>();


    public WorkDay getCurrentDay(){
        return workDays.get(mostRecentDate);
    }

    // Excluindo o dia atual
    public static List<WorkDay> getLastDays(int numDays){
        return  workDays
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
