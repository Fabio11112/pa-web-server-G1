import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.Semaphore;

/**
 * Represents the client handler that will handle the client request and send the response to the client socket connection
 * It will also send the log to the buffer of logs
 */
public class ClientHandler implements Runnable
{
    private final LockFiles lockFiles;
    private final String SERVER_ROOT;
    private final Socket client;
    private final String PATH404;
    private final Lock bufferLock;
    private final Semaphore itemsAvailable;
    private final ArrayList<Log> buffer;

    private final static int RESPONSE_OK = 200;
    private final static int RESPONSE_NOT_FOUND = 404;
    private final static int INTERNAL_SERVER_ERROR = 500;

    private final static int MILLISECONDS_TO_WAIT = 15000;

    private final static String INDEX = "/index.html";

    /**
     * Constructor for the ClientHandler class
     * @param client The client that will be handled
     * @param lockFiles The lockFiles object that will be used
     * @param SERVER_ROOT The root of the server
     * @param PATH404 The path of the 404 page
     */
    public ClientHandler( Socket client,
                         LockFiles lockFiles,
                         String SERVER_ROOT,
                         String PATH404,
                         Lock bufferLock,
                         Semaphore itemsAvailable,
                         ArrayList<Log> buffer )
    {

        this.lockFiles = lockFiles;
        this.SERVER_ROOT = SERVER_ROOT;
        this.client = client;
        this.PATH404 = PATH404;
        this.itemsAvailable = itemsAvailable;
        this.bufferLock = bufferLock;
        this.buffer = buffer;

    }

    /**
     * Get the tokens of the BufferedReader from the client Socket. Used for getting the URL, and consecutively
     * the path of the page wanted
     * @param br The BufferedReader from the client
     * @return tokens of the BufferedReader
     * @throws IOException When the BufferedReader is invalid
     */
    protected String[] getTokens(BufferedReader br) throws IOException
    {
        try {
            StringBuilder requestBuilder = new StringBuilder();
            String line;
            while (!(line = br.readLine()).isBlank()) {
                requestBuilder.append(line).append("\r\n");
            }

            String request = requestBuilder.toString();
            String[] tokens = request.split(" ");
            if (tokens.length < 2) {
                System.err.println("Invalid request received.");
                return null;
            }

            return tokens;
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Handles the client request. It reads the request, gets the path of the page wanted, routes the path,
     * reads the binary file of the page, locks the html page if it is in fact one, flushes the request to the client,
     * unlocks the html page if it was locked, and sends the log to the buffer
     */
    private void clientRequest( )
    {

        //instantiation of variables because of finally block
        String routePath;
        byte[] content;
        boolean lockedHtmlPage = false;
        Path resourcePath = null;



        try( BufferedReader br = new BufferedReader( new InputStreamReader( client.getInputStream( ) ) );
            OutputStream clientOutput = client.getOutputStream( ) )
        {
            System.out.println("InputStream: " + client.getInputStream());

            logClientConnection( );
            routePath = getRoutePath( br );

            System.out.println( "Route: " + routePath );
            resourcePath = Paths.get( routePath );
            resourcePath = Routing( resourcePath );
            content = readBinaryFile( resourcePath.toString( ) );

            if(content.length == 0)
                throw new FileNotFoundException( "File not found: " + resourcePath );

            lockedHtmlPage = lockHtmlPage( resourcePath );

            flushRequest( clientOutput, content );


            int response = resourcePath.toString( ).equals( PATH404 ) ? RESPONSE_NOT_FOUND : RESPONSE_OK;
            sendLog( "GET", resourcePath, client.toString( ), response );

        }
        catch( Exception e )
        {
            sendLog( "GET", resourcePath, client.toString(), INTERNAL_SERVER_ERROR );
            e.printStackTrace( );
        }
        finally{
            unlockHtmlPage( lockedHtmlPage, resourcePath );
        }
    }

    /**
     * Reads a binary file and returns its contents as a byte array.
     *
     * @param path The path of the file that will be read.
     * @return A byte array containing the file's contents, or an empty array if an error occurs.
     */
    private byte[] readBinaryFile( String path ) {
        try
        {
            return Files.readAllBytes( Paths.get( path ) );
        }
        catch ( IOException e )
        {
            System.err.println( "Error reading file: " + path );
            e.printStackTrace( );
            return new byte[0];
        }
    }

    /**
     * Flushes the request to the client. This means that the response will be sent to the client
     * @param clientOutput The OutputStream of the client
     * @param content The content of the response in bytes
     * @throws IOException When the OutputStream is invalid
     */
    private void flushRequest( OutputStream clientOutput, byte[] content ) throws IOException
    {
        if ( clientOutput != null )
        {
            clientOutput.write( "HTTP/1.1 200 OK\r\n".getBytes( ) );
            clientOutput.write( "Content-Type: text/html\r\n".getBytes( ) );
            clientOutput.write( "\r\n".getBytes( ) );

            // Send response body
            clientOutput.write( content );
            clientOutput.write( "\r\n\r\n".getBytes( ) );

            System.out.println("ClientOutput:\n" + clientOutput);
            clientOutput.flush( );
            client.close( );
        }
    }

    /**
     * Creates a Thread that sends the log to the buffer of Logs, which will be processed by the instance of ConsumerLogs
     * @param method The method of the request
     * @param path The path of the page sent
     * @param origin The origin of the request
     * @param response The response of the request
     */
    private void sendLog ( String method, Path path, String origin, int response )
    {
        Log log = new Log( method, path, origin, response );
        Runnable ProducerLogs = new ProducerLogs( buffer, bufferLock, itemsAvailable, log );
        Thread producerLogsThread = new Thread( ProducerLogs );
        producerLogsThread.start( );
    }

    /**
     * Get the path of the route requested by the client
     * @param br The BufferedReader from the client socket
     * @return The path of the route requested by the client
     * @throws IOException When the BufferedReader is invalid
     */
    private String getRoutePath( BufferedReader br ) throws IOException
    {
        String[] tokens = getTokens( br );
        if ( tokens == null || tokens.length == 0 )
            throw new IOException( "Invalid request received." );

        String route = tokens[1];
        return SERVER_ROOT + route;
    }

    /**
     * It prints the client connection on the console. Used for debugging purposes
     */
    private void logClientConnection( )
    {
        System.out.println( "New client connected: " + client + "on Thread: " + Thread.currentThread( ).getId( ) );
    }

    /**
     * It routes the path of the page requested by the client. It will return the path inserted whether it was
     * correctly inserted by the client, or it will return the index.html whether the directory inserted exists.
     * It routes the 404.html page file otherwise.
     *
     * @param path The path of the page requested by the client. Or the 404 file if it does not exist
     */
    protected Path Routing ( Path path ) {
        try
        {
            if( !Files.exists( path ) )
                throw new FileNotFoundException( "File not found: " + path + INDEX );

            if ( !Files.isDirectory( path ) )
                return path;

            Path indexPath = Paths.get( path + INDEX );

            if( !Files.exists( indexPath ) )
                throw new FileNotFoundException( "File not found: " + path + INDEX );

            return indexPath;

        }
        catch ( FileNotFoundException e )
        {
            Path path404 = Paths.get( PATH404 );
            System.out.println( "path not found : " + path );
            return path404;
        }

    }



    /**
     * It verifies the resource requested is a html page, if it is, it locks the file, otherwise it does nothing
     * @param resourcePath The path of the resource requested
     * @return True if the resource is a html page and it was locked, false otherwise
     */
    private boolean lockHtmlPage( Path resourcePath )
    {
        boolean endsWithHtml = resourcePath.toString( ).endsWith( ".html" );
        if ( endsWithHtml )
        {
            //if the page html exists itself
            return lockFiles.lock( resourcePath );
        }

        return false;
    }

    /**
     * Unlock the html page if it was locked
     * @param lockedHtmlPage If the html page was locked. This means that the resource wanted for the request was a html page
     *                       and not anything else (e.g. favicon.ico)
     * @param resourcePath The path of the resource that was locked
     */
    private void unlockHtmlPage ( boolean lockedHtmlPage, Path resourcePath )
    {
        try {
            if ( lockedHtmlPage )
                Thread.sleep( MILLISECONDS_TO_WAIT );
        }
        catch( InterruptedException e )
        {
            e.printStackTrace( );
        }
        finally {
            if( lockedHtmlPage )
                lockFiles.unlock( resourcePath );
        }
    }
    /**
     * Run method of the ClientHandler. It will handle the client request and send the response to the client socket connection
     * It will also send the log to the buffer of logs for processing by the ConsumerLogs instance of the server thread
     */
    @Override
    public void run( )
    {
        clientRequest( );
    }
}





