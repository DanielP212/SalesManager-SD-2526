package core.base;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SeqSaleNotification {
    public final Lock notLock = new ReentrantLock();
    public final Condition cond = notLock.newCondition();
    private final int pID;
    private int waiters = 0;

    private boolean p_Sold = false;


    public SeqSaleNotification(int pID){
        this.pID = pID;
    }

    public void soldProd(){
        p_Sold = true;
    }

    public boolean get_p_Sold(){
        return p_Sold;
    }

    public void incWaiters(){
        this.waiters++;
    }

    public void decWaiters(){
        this.waiters--;
    }

    public boolean noWaiters(){
        if(waiters == 0) return true;
        return false;
    }

}
