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

    private boolean isTestInstance = false;
    DataInputStream userInput = null;
    PrintStream testOutput = null;

    ClientConnectionThread connectionThread;
    List<PendingRequestThread> busyThreads = new LinkedList<>();
    Queue<PendingRequestThread> freeThreads = new LinkedList<>();

    Lock busyLock = new ReentrantLock();
    Lock freeLock = new ReentrantLock();

    public Client(){;}

    // Para UnitTests
    public Client(InputStream systemIn, PrintStream testOut){
        isTestInstance = true;
        this.testOutput = testOut;
        System.setIn(systemIn);
        id = 1;
    }

    public void run(){
        try {
            Socket socket = new Socket("localhost", 12345);
            userInput = new DataInputStream(System.in);
            connectionThread = new ClientConnectionThread(this, socket);
            connectionThread.start();
            Menu mainMenu = new Menu("Main", userInput, this);
            while(true){
                Thread.sleep(100); // so para ao fazer instantaneo nao ficar feio
                Packet p = mainMenu.execute();
                if (p == null){
                    System.out.println("Null packet. deu muita merda maltinha! Inputs erradas?");
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
