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
    private static final int s = 89;
    private static final int d = 90;
    private static int CLIENT_ID_COUNTER = 1;

    private static final int PRODUCT_NUMBER = 5;

    private static Thread serverThread;
    private static List<Client> clients = new ArrayList<>();

    private static boolean initialized = false;
    private static int databaseEntries;

    public static void init(){
        if (initialized) return;
        int nFiles = d;
        Random random = new Random();
        LocalDate today = LocalDate.now();
        FileCreator.cleanupOldFiles();
        int iters = 10000;


        for(int i = 1; i < nFiles ; i++){
            LocalDate targetDate = today.minusDays(i);
            for (int j = 0; j < iters; j++){
                FileCreator.createFile(targetDate, 2,
                        random.nextInt(1, 342),
                        random.nextFloat(1.0f, 100.0f)); // Produto com nome B
            }
            for (int j = 0; j < iters; j++){
                FileCreator.createFile(targetDate, 3,
                        random.nextInt(1, 255),
                        random.nextFloat(1.0f, 100.0f)); // Product com nome C
            }
        }
        databaseEntries = nFiles * iters * 2;

        serverThread = new Thread(new Server(12345, s, d));
        serverThread.start();
        System.out.println("Initialized!");
        initialized = true;
    }

    public static Client createClientInstance(InputStream clientIn, PrintStream clientOut){
        Client client = new Client(clientIn, clientOut, CLIENT_ID_COUNTER++);
        System.out.println("Creating Client Instance with ID " + CLIENT_ID_COUNTER);
        clients.add(client);
        return client;
    }
    public static int getNumDBEntries(){
        return databaseEntries;
    }

    public static int getD(){ return d; }
    public static int getS(){ return s; }

}
