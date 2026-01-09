package server;

import comms.Connection;
import comms.Packet;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientHandlerThread extends Thread{
    private final int clientID;
    private final Connection conn;
    private List<RequestHandlerThread> busyThreads = new LinkedList<>();
    private Queue<RequestHandlerThread> freeThreads = new LinkedList<>();
    private Lock poolLock = new ReentrantLock();

    public ClientHandlerThread(int clientID, Socket s){
        this.clientID = clientID;
        this.conn = new Connection(s);
        System.out.println("Created thread to handle client with ID " + clientID);
    }

    public void run(){
        while (!conn.isClosed()){
            try{
                Packet received = conn.receive();
                if (received == null){
                    System.out.println("[CLIENT THREAD] Null packet received?");
                    continue;
                }
                System.out.println("Received: " + received.toString());

                RequestHandlerThread worker;
                poolLock.lock();
                try{
                    if (freeThreads.isEmpty()){
                        worker = new RequestHandlerThread(clientID, conn, this);
                        worker.start();
                        busyThreads.add(worker);
                    } else {
                        worker = freeThreads.poll();
                        busyThreads.add(worker);
                    }
                    worker.assign(received);
                }finally {
                    poolLock.unlock();
                }
            } catch (IOException e) {
                System.out.println("[CLIENT HANDLER] Thread Closing!");
                shutdown();
                return;
            }
        }
    }

    public void freeRequestHThread(RequestHandlerThread worker){
        poolLock.lock();
        try{
            busyThreads.remove(worker);
            freeThreads.add(worker);
            System.out.println("Worker returned to pool. Free: " + freeThreads.size() + " Busy: " + busyThreads.size());
        } finally {
            poolLock.unlock();
        }
    }

    public void shutdown(){
        poolLock.lock();
        try{
            for (RequestHandlerThread freeThread : freeThreads) {
                freeThread.stopThread();
            }
            for (RequestHandlerThread freeThread : busyThreads) {
                freeThread.stopThread();
            }
            freeThreads.clear();
            busyThreads.clear();
            conn.close();
            Server.stopTracking(this);
        } finally {
            poolLock.unlock();
        }
    }
}
