package comms;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Connection {
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;

    private final Lock readLock = new ReentrantLock();
    private final Lock writeLock = new ReentrantLock();


    public Connection(Socket s){
        this.socket = s;
        try {
            this.in = new DataInputStream(new BufferedInputStream(s.getInputStream()));
            this.out = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Packet receive() throws IOException {
        readLock.lock();
        try{
            int packetSize = in.readInt();
            byte[] buf = new byte[packetSize];
            int bytesRead = in.read(buf);
            if (bytesRead != packetSize - 4){
                System.out.println("[WARNING] Read different bytes(" + bytesRead + ") from packet size told on header. Is this intended?");
                return null;
            }
            return new Packet(packetSize, buf);
        } catch (EOFException e){
            close();
            throw e;
        } finally {
            readLock.unlock();
        }
    }


    public void send(Packet p) throws IOException{
        writeLock.lock();
        out.write(p.getBytes());
        out.flush();
        writeLock.unlock();
    }

    public void close(){
        try{
            if (socket != null && !socket.isClosed()){
                socket.close();
            }
        } catch (IOException e) {
        }
    }

    public DataOutputStream getOutputStream(){ return out; }

    public boolean isClosed(){ return socket.isClosed(); }



}
