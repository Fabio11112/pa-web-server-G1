import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * ConsumerLogs class. It is used to consume the logs from the buffer
 */
public class ConsumerLogs implements Runnable{
    private final String logPath;
    private final ArrayList<Log> buffer;
    private final Lock bufferLock;
    private final Semaphore itemsAvailable;


    /**
     * Constructor for the ConsumerLogs class
     * @param logPath The path of the log file
     * @param buffer The buffer of the logs
     * @param bufferLock The lock of the buffer
     * @param itemsAvailable The semaphore that indicates if there are items available
     */
    public ConsumerLogs(String logPath, ArrayList<Log> buffer, Lock bufferLock, Semaphore itemsAvailable) {
        this.logPath = logPath;
        this.buffer = buffer;
        this.bufferLock = bufferLock;
        this.itemsAvailable = itemsAvailable;
    }

    private void consumeLogs() {
        try {
            while (true) {
                Path path;
                if (logPath == null) {
                    return;
                }
                path = Paths.get(logPath);
                File file = path.toFile();

                FileWriter writer = new FileWriter(file);
                if (itemsAvailable.tryAcquire(5, TimeUnit.SECONDS)) {
                    bufferLock.lock();
                    if (!buffer.isEmpty()) {
                        Log log = buffer.remove(0);
                        writer.write(log.toString());
                        System.out.println("Consumer " + Thread.currentThread().getId() + " consumed: " + log.toString());
                    }

                    bufferLock.unlock();
                } else {
                    System.out.println("Consumer " + Thread.currentThread().getId() + " cannot consumer buffer is locked");
                }

            }
        } catch (InterruptedException e) //itemsAvailable.tryAcquire
        {
            e.printStackTrace();
        } catch (IOException e) //writer.write(log.toString());
        {
            e.printStackTrace();

        }
    }

    @Override
    public void run(){
        consumeLogs();
    }
}