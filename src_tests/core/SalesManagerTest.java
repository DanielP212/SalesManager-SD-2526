package core;

import client.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SalesManagerTest {
    PrintWriter writer;
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
            writer.println("query_qtd 2 10");
            Thread.sleep(100);
            System.out.println("Expected: " + expected);

            writer.println("quit");
            clientThread.join();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getTotalMoney() {
    }

    @Test
    void getMedianPrice() {
    }

    @Test
    void getMaxPrice() {
    }

    // //////////////////////////////////////////////////////////
    private Thread runClientInstance(PipedInputStream clientIn){
        Client client = TestManager.createClientInstance(clientIn);
        return new Thread(client);
    }

    private void init(){
        if (initialized) return;
        TestManager.init();
        try {
            PipedOutputStream writePipe = new PipedOutputStream();
            PipedInputStream clientIn = new PipedInputStream(writePipe);

            writer = new PrintWriter(writePipe, true);
            clientThread = runClientInstance(clientIn);
            clientThread.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        initialized = true;
    }
}