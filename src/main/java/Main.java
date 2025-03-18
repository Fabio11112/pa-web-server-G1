import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Main class that starts the server and the thread pool.
 */
public class Main {
    /**
     * Main method that starts the server and the thread pool.
     * @param args The arguments of the program
     */
    public static void main( String[] args ) {
        String directory = "sites";
        String path404 = "sites/404.html";
        String extension = "html";
        String logPath = "log/logs.txt";


        Runnable[] tasks = new Runnable[5];

        Semaphore semaphore = new Semaphore(0);
        Lock bufferLock = new ReentrantLock();
        ArrayList<Log> buffer = new ArrayList<>();

        Runnable consumerLogs = new ConsumerLogs(logPath, buffer, bufferLock, semaphore);
        Thread consumerLogsThread = new Thread(consumerLogs);

        LockFiles lockFiles = new LockFiles( extension, directory );

        ThreadPool pool = new ThreadPool(tasks.length, tasks.length, 5000 , 10);
        MainHTTPServer server = new MainHTTPServer(8888, pool, directory, path404, lockFiles, buffer, bufferLock, semaphore);

        server.startServer();

        consumerLogsThread.start();

    }
}