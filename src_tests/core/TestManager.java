package core;

import client.Client;
import core.base.WorkDay;
import server.Server;
import test.FileCreator;

import java.io.DataInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.*;

public class TestManager {
    private static final int s = 5;
    private static final int d = 9;
    private static int CLIENT_ID_COUNTER = 1;

    private static final int PRODUCT_NUMBER = 5;
    private static final Map<LocalDate, WorkDay> days = new HashMap<>();

    private static Thread serverThread;
    private static List<Client> clients = new ArrayList<>();

    private static boolean initialized = false;

    public static void init(){
        if (initialized) return;
        int nFiles = 10;
        Random random = new Random();
        LocalDate today = LocalDate.now();
        FileCreator.cleanupOldFiles();
        int[] id2Quantities = {5, 1, 3, 1, 7};
        float[] id2Prices = {100, 99, 60.1f, 157.5f, 70};
        int[] id3Quantities = {1, 100, 34, 965};
        float[] id3Prices = {2, 500, 30, 45};

        for(int i = 1; i < nFiles ; i++){
            LocalDate targetDate = today.minusDays(i);
            for (int j = 0; j < id2Quantities.length; j++){
                FileCreator.createFile(targetDate, 2, id2Quantities[j], id2Prices[j]);
            }
            for (int j = 0; j < id3Quantities.length; j++){
                FileCreator.createFile(targetDate, 3, id3Quantities[j], id3Prices[j]);
            }

            WorkDay day = SalesManager.loadDayFromFile(targetDate,
                    new File(targetDate.toString() + ".dat"));
            days.put(targetDate, day);
        }

        serverThread = new Thread(new Server(12345, s, d));
        serverThread.start();
        System.out.println("Initialized!");
        initialized = true;
    }

    public static Client createClientInstance(InputStream clientIn, PrintStream clientOut){
        Client client = new Client(clientIn, clientOut, CLIENT_ID_COUNTER++);
        System.out.println("Creating Client Instance!");
        clients.add(client);
        return client;
    }

}
