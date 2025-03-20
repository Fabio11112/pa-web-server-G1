import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

/**
 * Represents the producer of logs. It will receive a log and store it in the buffer
 */
public class ProducerLogs implements Runnable {
    private final ArrayList<Log> buffer;
    private final Lock bufferLock;
    private final Semaphore itemsAvailable;
    private final Log log;

    /**
     * Constructor for the ProducerLogs class
     * @param buffer The buffer that will store the logs
     * @param bufferLock The lock that will be used to lock the buffer
     * @param itemsAvailable The semaphore that will be used to signal that there are items available in the buffer
     * @param log The log that will be stored in the buffer
     */
    public ProducerLogs(ArrayList<Log> buffer, Lock bufferLock, Semaphore itemsAvailable, Log log) {
        this.buffer = buffer;
        this.bufferLock = bufferLock;
        this.itemsAvailable = itemsAvailable;
        this.log = log;
    }


    /**
     * Method that will store the log in the buffer and signal that there are items available in the buffer
     */
    private void produceLogs(){
        try
        {
            bufferLock.lock();
            buffer.add(log);
            itemsAvailable.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            bufferLock.unlock();
        }

    }

    /**
     * Method that will be executed when the thread is started. It will receive a log and store it in the buffer
     */
    @Override
    public void run(){
        produceLogs();
    }


}


