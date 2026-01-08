package client;

import client.menu.Menu;
import comms.Packet;
import comms.common.PacketType;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Client implements Runnable {
    private static final int NOT_LOGGED_ID = -1;
    private int id = NOT_LOGGED_ID; // MUDAR para qualquer cena para nao ter de dar login
    private InputStream input;

    private boolean isTestInstance = false;
    DataInputStream userInput = null;
    PrintStream testOutput = null;

    ClientConnectionThread connectionThread;
    List<PendingRequestThread> busyThreads = new LinkedList<>();
    Queue<PendingRequestThread> freeThreads = new LinkedList<>();

    Lock busyLock = new ReentrantLock();
    Lock freeLock = new ReentrantLock();

    public Client(){
        this.input = System.in;
    }

    // Para UnitTests
    public Client(InputStream systemIn, PrintStream testOut, int clientID){
        isTestInstance = true;
        this.testOutput = testOut;
        this.input = systemIn;
        id = clientID;
    }

    public void run(){
        try {
            Socket socket = new Socket("localhost", 12345);
            userInput = new DataInputStream(input);
            connectionThread = new ClientConnectionThread(this, socket);
            connectionThread.start();
            Menu mainMenu = new Menu("Main", userInput, this);
            while(true){
                Packet p;
                if (isTestInstance){
                    BufferedReader in = new BufferedReader(new InputStreamReader(input));
                    String input = in.readLine();
                    if (input == null) return;
                    System.out.println("Received input: " + input.trim());
                    if (input.trim().equals("quit")) return;
                    p = InputHandler.handle(id, input.trim());
                } else {
                    Thread.sleep(100); // so para ao fazer instantâneo nao ficar feio
                    p = mainMenu.execute();
                }
                if (p == null){
                    System.out.println("Null packet. Inputs erradas?");
                    continue;
                }
                // Nao fazer nada enquanto nao estiver loggado
                if (!isLoggedIn() &&
                        (p.getType() != PacketType.LOGIN && p.getType() != PacketType.REGISTER)){
                    System.out.println("It seems you are not logged in! Please login or register first!");
                    continue;
                } else if (!isLoggedIn() && p.getType() == PacketType.LOGIN){
                    PendingRequestThread newThread = new PendingRequestThread(this, connectionThread);
                    newThread.giveRequest(p);
                    newThread.start();
                    newThread.join();
                    freeThreads.clear();
                    busyThreads.clear();
                    continue;
                }

                if (freeThreads.isEmpty()){
                    PendingRequestThread newThread = new PendingRequestThread(this, connectionThread);
                    newThread.giveRequest(p);
                    newThread.start();
                } else {
                    PendingRequestThread freeThread = freeThreads.poll();
                    freeThread.giveRequest(p);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void assignID(int assignedID){
        if (isLoggedIn()) return;
        this.id = assignedID;
        Menu.getInstance().setLogged();
    }

    public int getID(){ return id; }

    public void signalRequestDone(PendingRequestThread thread){
        busyLock.lock();
        freeLock.lock();
        busyThreads.remove(thread);
        freeThreads.add(thread);
        busyLock.unlock();
        freeLock.unlock();
        System.out.println(thread.getName() + " finished a request!");
    }

    public void signalRequestStart(PendingRequestThread thread){
        busyLock.lock();
        freeLock.lock();
        freeThreads.remove(thread);
        busyThreads.add(thread);
        System.out.println("Curr number of threads: " + (busyThreads.size() + freeThreads.size()));
        busyLock.unlock();
        freeLock.unlock();
        System.out.println(thread.getName() + " is starting a request");
    }

    public boolean isLoggedIn(){ return id != NOT_LOGGED_ID; }
    public boolean isTestInstance() { return isTestInstance; }
    public PrintStream getTestOutput(){ return testOutput; }
}
