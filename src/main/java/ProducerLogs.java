import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public class ProducerLogs implements Runnable {
    private final ArrayList<Log> buffer;
    private final Lock bufferLock;
    private final Semaphore itemsAvailable;
    private final Log log;

    public ProducerLogs(ArrayList<Log> buffer, Lock bufferLock, Semaphore itemsAvailable, Log log) {
        this.buffer = buffer;
        this.bufferLock = bufferLock;
        this.itemsAvailable = itemsAvailable;
        this.log = log;
    }

    @Override
    public void run(){
        produceLogs();
    }

    private void produceLogs(){
        try
        {
            bufferLock.lock();
            buffer.add(log);
            System.out.println("Producer " + Thread.currentThread().getId() + " produced: " + log.toString());
            bufferLock.unlock();
            itemsAvailable.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }


}


