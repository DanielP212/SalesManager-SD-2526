package core;

import client.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SalesManagerTest {
    PrintWriter writer; // Por onde mandar inputs ao cliente
    ByteArrayOutputStream readTest; // Por onde ler outputs do cliente
    PrintStream clientOut; // Para onde o cliente manda prints. Util para utilizar transferTo()
    Thread clientThread;

    private final List<ClientInstance> activeClients = new ArrayList<>();

    private boolean initialized = false;

    @BeforeEach
    void setup(){
        TestManager.init();
    }

    @AfterEach
    void tearDown(){
        for (ClientInstance c : activeClients ){
            c.stop();
        }
        activeClients.clear();
    }

    @Test
    void registerSale() {
    }

    @Test
    @DisplayName("Sold Qtd concurrent")
    void getSoldQtd() throws InterruptedException {
        int threads = 10;
        try {
            int expected = SalesManager.getSoldQuantity(10 ,2);
            ConcurrencyTestHelper.runConcurrently(threads, () -> {
                try {
                    int result = SalesManager.getSoldQuantity(10 ,2);
                    assertEquals(expected, result, "Incorrect result, expected: " + expected + " got " + result);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Total Money concurrent")
    void getTotalMoney() {
        int threads = 10;
        try {
            float expected = SalesManager.getTotalMoney(10 ,2);
            ConcurrencyTestHelper.runConcurrently(threads, () -> {
                float result = SalesManager.getTotalMoney(10 ,2);
                assertEquals(expected, result, "Incorrect result, expected: " + expected + " got " + result);
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Average Price concurrent")
    void getAveragePrice() {
        int threads = 10;
        try {
            float expected = SalesManager.getAveragePrice(10 ,2);
            ConcurrencyTestHelper.runConcurrently(threads, () -> {
                float result = SalesManager.getAveragePrice(10 ,2);
                assertEquals(expected, result, "Incorrect result, expected: " + expected + " got " + result);
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("Max Price concurrent")
    void getMaxPrice() {
        int threads = 10;
        try {
            float expected = SalesManager.getMaxPrice(10 ,2);
            ConcurrencyTestHelper.runConcurrently(threads, () -> {
                float result = SalesManager.getMaxPrice(10 ,2);
                assertEquals(expected, result, "Incorrect result, expected: " + expected + " got " + result);
            });
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // //////////////////////////////////////////////////////////
    private String sendInput(String input){
        writer.println(input);
        long startTime = System.currentTimeMillis();
        long timeout = 2000;

        try{
            while(System.currentTimeMillis() - startTime < timeout){
                String current = readTest.toString();
                if (!current.isEmpty()) return current;
                Thread.sleep(50);
            }
            return null;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            readTest.reset();
        }
    }

    private ClientInstance spawnClient() {
        try {
            PipedOutputStream writePipe = new PipedOutputStream();
            PipedInputStream clientIn = new PipedInputStream(writePipe);

            ByteArrayOutputStream readTest = new ByteArrayOutputStream();
            PrintStream clientOut = new PrintStream(readTest);

            PrintWriter writer = new PrintWriter(writePipe, true);

            Client clientRunnable = TestManager.createClientInstance(clientIn, clientOut);
            Thread clientThread = new Thread(clientRunnable);
            clientThread.start();

            ClientInstance instance = new ClientInstance();
            instance.writer = writer;
            instance.output = readTest;
            instance.thread = clientThread;

            activeClients.add(instance);
            return instance;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Thread runClientInstance(PipedInputStream clientIn, PrintStream clientOut){
        Client client = TestManager.createClientInstance(clientIn, clientOut);
        return new Thread(client);
    }

    private void init(){
        if (initialized) return;
        TestManager.init();
        try {
            PipedOutputStream writePipe = new PipedOutputStream();
            PipedInputStream clientIn = new PipedInputStream(writePipe);

            readTest = new ByteArrayOutputStream();
            clientOut = new PrintStream(readTest);

            writer = new PrintWriter(writePipe, true);
            clientThread = runClientInstance(clientIn, clientOut);
            clientThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        initialized = true;
    }



    private static class ClientInstance {
        PrintWriter writer;
        ByteArrayOutputStream output;
        Thread thread;

        // Helper to send input and wait for specific client response
        String sendInput(String input) {
            writer.println(input);
            long startTime = System.currentTimeMillis();
            long timeout = 2000;

            try {
                while (System.currentTimeMillis() - startTime < timeout) {
                    String current = output.toString();
                    if (!current.isEmpty()) return current;
                    Thread.sleep(50);
                }
                return null;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                output.reset();
            }
        }

        void stop() {
            writer.println("quit");
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}