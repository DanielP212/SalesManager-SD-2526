package client;

import comms.Packet;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PendingRequest {
    private final Lock lock = new ReentrantLock();
    private final Condition responseArrived = lock.newCondition();
    private Packet responsePacket = null;

    public Packet waitForResponse(){
        lock.lock();
        try{
            while (responsePacket == null) responseArrived.await();
            return responsePacket;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void complete(Packet received){
        lock.lock();
        try{
            this.responsePacket = received;
            responseArrived.signalAll();
        } finally {
            lock.unlock();
        }
    }

}
