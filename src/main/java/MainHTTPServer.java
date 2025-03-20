import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * A simple HTTP server that listens on a specified port.
 * It serves files from a predefined server root directory.
 */
public class MainHTTPServer {
    private final LockFiles pathPagesMap;
    private final String SERVER_ROOT;
    private final String PATH404;// Define by user
    private final int port;
    private final ThreadPool pool;
    private final Lock bufferLock;
    private final Semaphore itemsAvailable;
    private final ArrayList<Log> buffer;

    /**
     * Constructor for the MainHTTPServer class.
     * @param port The port that the server will listen to
     * @param pool The thread pool that will be used to handle the requests
     * @param SERVER_ROOT The root directory of the pages of the server
     * @param PATH404 The path of the 404 page
     * @param pathPagesMap The map that will store the locks of the files related to the HTML pages
     * @param buffer The buffer that will store the logs
     * @param bufferLock The lock that will be used to lock the buffer
     * @param itemsAvailable The semaphore that will be used to signal that there are items available in the buffer
     */
    public MainHTTPServer(int port,
                          ThreadPool pool,
                          String SERVER_ROOT,
                          String PATH404,
                          LockFiles pathPagesMap,
                          ArrayList<Log> buffer,
                          Lock bufferLock,
                          Semaphore itemsAvailable
    )
    {
        this.port = port;
        this.pool = pool;
        this.SERVER_ROOT = SERVER_ROOT;
        this.pathPagesMap = pathPagesMap;
        this.PATH404 = PATH404;
        this.buffer = buffer;
        this.bufferLock = bufferLock;
        this.itemsAvailable = itemsAvailable;
    }


    /**
     * Starts the server and listens for incoming client connections.
     */
    public void startServer() {
        try(ServerSocket server = new ServerSocket(port)) {

            System.out.println( "Server started on port: " + port );
            System.out.println( "Working Directory: " + System.getProperty( "user.dir" ) );

            while ( true ) {
                try
                {
                    Socket client = server.accept();

                    //Reads and parses the HTTP Request
                    pool.execute(new ClientHandler(client, pathPagesMap, SERVER_ROOT, PATH404, bufferLock, itemsAvailable, buffer ));


                } catch ( IOException e ) {
                    System.err.println( "Error handling client request." );
                    e.printStackTrace();
                }
            }
        } catch ( IOException e ) {
            System.err.println( "Server error: Unable to start on port " + port );
            e.printStackTrace();
        } finally {
            pool.shutdown();

            try {
                if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException e) {
                pool.shutdownNow();
            }
        }


    }



}