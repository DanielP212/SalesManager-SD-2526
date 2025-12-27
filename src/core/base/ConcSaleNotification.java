package core.base;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


public class ConcSaleNotification {
    public final Lock notLock = new ReentrantLock();
    public final Condition cond = notLock.newCondition();
    private String prodName = null;
    private int waiters = 0;
    private final int n;

    public ConcSaleNotification(int n){
        this.n = n;
    }


    public String getPName(){
        return prodName;
    }

    public void setPName (String product){
        this.prodName = product;
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
