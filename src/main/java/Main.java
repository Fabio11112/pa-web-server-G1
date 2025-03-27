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
        String configPath = "/server.config.example";

        ServerConfigLoader configs = new ServerConfigLoader( configPath );

        Semaphore semaphore = new Semaphore( 0 );
        Lock bufferLock = new ReentrantLock( );
        ArrayList<Log> buffer = new ArrayList<>( );

        Runnable consumerLogs = new ConsumerLogs( configs.getLogPath( ), buffer, bufferLock, semaphore );
        Thread consumerLogsThread = new Thread( consumerLogs );
        consumerLogsThread.start( );

        LockFiles lockFiles = new LockFiles( configs.getExtension( ), configs.getDirectory( ) );

        ThreadPool pool = new ThreadPool( configs.getCorePoolSize( ), configs.getMaxPoolSize( ), configs.getKeepAliveTime( ), configs.getMaxQueueThreadSize( ) );
        HTTPServer server = new HTTPServer( configs.getPort( ), pool, configs.getDirectory( ), configs.getPath404 ( ), lockFiles, buffer, bufferLock, semaphore );

        server.startServer( );



    }
}