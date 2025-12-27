package server.requests;

import java.nio.ByteBuffer;

import comms.common.Encodable;
import core.SalesManager;
import core.base.ConcSaleNotification;
import core.base.Product;
import core.base.SeqSaleNotification;
import core.base.WorkDay;

public class NotifyConcRequest extends Request {
    private final int n;

    public NotifyConcRequest(byte[] data){
        ByteBuffer buffer = ByteBuffer.wrap(data);
        String n_str = Encodable.readString(buffer);
        this.n = Integer.parseInt(n_str.trim());
    }

    @Override
    public byte[] execute() {
        WorkDay wd = SalesManager.getCurWorkDay();
        ConcSaleNotification cNotification = wd.addConcNotification(n);
        String prodName = null;
        
        cNotification.notLock.lock();
        try {
            while (cNotification.getPName() == null && !wd.isClosed()) {
                cNotification.cond.await();
            }
            if(!wd.isClosed()){
                cNotification.decWaiters();
                prodName = cNotification.getPName();
                wd.removeConcNotification(n);
            }


        } catch (Exception e) {
            //retornar null se der erro na espera?
            return new byte[]{0x00};
        }finally{
            cNotification.notLock.unlock();
        }

        if(prodName != null){
            byte[] res = new byte[prodName.length() + 1];
            Encodable.writeString(res, 0, prodName);
            return res;
        }

        return new byte[]{0x00}; //é o msm q null?
    }
}
