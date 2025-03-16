import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * A simple HTTP server that listens on a specified port.
 * It serves files from a predefined server root directory.
 */
public class MainHTTPServerThread {
    private final LockFiles pathPagesMap;
    private final String SERVER_ROOT;// Define by user
    private final int port;
    private final ThreadPool pool;

    /**
     * Constructor to initialize the HTTP server thread with a specified port.
     *
     * @param port The port number on which the server will listen.
     */
    public MainHTTPServerThread( int port, ThreadPool pool, String SERVER_ROOT) {
        this.port = port;
        this.pool = pool;
        this.SERVER_ROOT = SERVER_ROOT;

        pathPagesMap = new LockFiles( "html", SERVER_ROOT );
    }



    /**
     * Reads a text file and returns its contents as a string.
     *
     * @param path The file path to read.
     * @return A string containing the file's contents, or an empty string if an error occurs.
     */
    private String readFile( String path ) {
        StringBuilder content = new StringBuilder();
        try ( BufferedReader reader = new BufferedReader( new FileReader( path ) ) ) {
            String line;
            while ( ( line = reader.readLine() ) != null ) {
                content.append( line ).append( "\n" );
            }
        } catch ( IOException e ) {
            System.err.println( "Error reading file: " + path );
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * Starts the HTTP server and listens for incoming client requests.
     * Processes HTTP GET requests and serves files from the defined server root directory.
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
                    pool.execute(new ClientHandler(client, pathPagesMap, SERVER_ROOT));


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