package core;

import client.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class SalesManagerTest {
    PrintWriter writer; // Por onde mandar inputs ao cliente
    ByteArrayOutputStream readTest; // Por onde ler outputs do cliente
    PrintStream clientOut; // Para onde o cliente manda prints. Util para utilizar transferTo()
    Thread clientThread;

    private boolean initialized = false;

    @Test
    void registerSale() {
    }

    @Test
    @DisplayName("Get Quantity Sold of product")
    void getSoldQuantity() {
        init();
        try {
            int expected = SalesManager.getSoldQuantity(10, 2);
            String result = sendInput("query_qtd B 10");
            assertNotNull(result);
            assertTrue(result.contains(String.valueOf(expected)));

            int expected1 = 0;
            String result1 = sendInput("query_qtd C 10");
            assertNotNull(result1);
            assertTrue(result1.contains(String.valueOf(expected1)));

            writer.println("quit");
            clientThread.join();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getTotalMoney() {
        init();
        float expected = SalesManager.getTotalMoney(10, 2);
        String result = sendInput("query_total B 10");
        assertNotNull(result);
        assertEquals(expected, Float.parseFloat(result));

        writer.println("quit");
        try {
            clientThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getAveragePrice() {
        init();
        float expected = SalesManager.getAveragePrice(10, 2);
        String result = sendInput("query_avg B 10");
        assertNotNull(result);
        assertEquals(expected, Float.parseFloat(result));
        result = null;

        expected = SalesManager.getAveragePrice(10 ,3);
        result = sendInput("query_avg C 10");
        assertNotNull(result);
        assertEquals(expected, Float.parseFloat(result));

        writer.println("quit");
        try {
            clientThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getMaxPrice() {
        init();
        float expected = SalesManager.getMaxPrice(10, 2);
        String result = sendInput("query_max B 10");
        assertNotNull(result);
        assertEquals(expected, Float.parseFloat(result));
        result = null;

        expected = SalesManager.getMaxPrice(10, 3);
        result = sendInput("query_max C 10");
        assertNotNull(result);
        assertEquals(expected, Float.parseFloat(result));

        writer.println("quit");
        try {
            clientThread.join();
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
}