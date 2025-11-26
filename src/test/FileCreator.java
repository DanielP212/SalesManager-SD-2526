package test;

import java.io.*;
import java.time.LocalDate;
import java.util.Random;

public class FileCreator {

    public static void createFile(LocalDate date, int productId, int quantity, float price){
        String filename = date.toString() + ".dat";
        try(DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filename, true)))){
            dos.writeInt(productId);
            dos.writeInt(quantity);
            dos.writeFloat(price);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void cleanupOldFiles() {
        File dir = new File(".");
        File[] files = dir.listFiles((d, name) -> name.endsWith(".dat"));
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    public static void main(String[] args) {
        int nFiles = Integer.parseInt(args[0]);
        Random random = new Random();
        LocalDate today = LocalDate.now();

        cleanupOldFiles();

        for(int i = 1; i < nFiles ; i++){
            LocalDate targetDate = today.minusDays(i);

            /*
            createFile(targetDate, random.nextInt(6), random.nextInt(100), random.nextFloat(1000));
            createFile(targetDate, random.nextInt(6), random.nextInt(100), random.nextFloat(1000));
            createFile(targetDate, random.nextInt(6), random.nextInt(100), random.nextFloat(1000));
            createFile(targetDate, random.nextInt(6), random.nextInt(100), random.nextFloat(1000));
            createFile(targetDate, random.nextInt(6), random.nextInt(100), random.nextFloat(1000));
            createFile(targetDate, random.nextInt(6), random.nextInt(100), random.nextFloat(1000));
            createFile(targetDate, random.nextInt(6), random.nextInt(100), random.nextFloat(1000));
            createFile(targetDate, random.nextInt(6), random.nextInt(100), random.nextFloat(1000));
            */

            createFile(targetDate, 2, 5, 100);
            createFile(targetDate, 3, 10, 100);

        }

    }
}
