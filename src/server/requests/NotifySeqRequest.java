package server.requests;

import java.nio.ByteBuffer;

import comms.common.Encodable;
import core.SalesManager;
import core.base.Product;
import core.base.SeqSaleNotification;
import core.base.WorkDay;

public class NotifySeqRequest extends Request {
    private final String p1;
    private final String p2;
    private final ByteBuffer buffer;

    public NotifySeqRequest(byte[] data){
        this.buffer = ByteBuffer.wrap(data);
        this.p1 = Encodable.readString(buffer);
        this.p2 = Encodable.readString(buffer);
    }

    @Override
    public byte[] execute() {
        WorkDay wd = SalesManager.getCurWorkDay();
        Product pr1 = SalesManager.getProductByName(p1);
        Product pr2 = SalesManager.getProductByName(p2);

        SeqSaleNotification sn1 = wd.addSeqSaleNotification(pr1.getId());
        SeqSaleNotification sn2 = wd.addSeqSaleNotification(pr2.getId());
        
        boolean result = true;
        sn1.notLock.lock();
        try {
            while (!sn1.get_p_Sold() && !wd.isClosed()) {
                sn1.cond.await();
            }
            if(wd.isClosed()){
                result = false;
                sn1.decWaiters();
                
                sn2.notLock.lock();
                sn2.decWaiters();
                
                wd.removeSeqNotification(pr1.getId());
                wd.removeSeqNotification(pr2.getId());
            }

            sn2.notLock.lock();
            while (!sn2.get_p_Sold() && !wd.isClosed()) {
                sn2.cond.await();
            }

            if(wd.isClosed() && result == true){
                result = false;
                sn1.decWaiters();
                sn2.decWaiters();
                wd.removeSeqNotification(pr1.getId());
                wd.removeSeqNotification(pr2.getId());
            }

            if(!wd.isClosed() && result == true){
                sn1.decWaiters();
                sn2.decWaiters();
                wd.removeSeqNotification(pr1.getId());
                wd.removeSeqNotification(pr2.getId());
            }

        } catch (Exception e) {
            // TODO: handle exception
        }finally{
            sn2.notLock.unlock();
            sn1.notLock.unlock();
        }
        int ans = result ? 1 : 0;

        byte[] res = new byte[4];
        Encodable.writeIntBytes(res, 0, ans);
             
        return res;
    }
}
