import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


/**
 * Handler of the Client Requests
 */
public class ClientHandler implements Runnable{

    private final LockFiles lockFiles;
    private final String SERVER_ROOT;
    private final Socket client;
    private final String PATH404;


    /**
     * Constructor for the ClientHandler class
     * @param client The client that will be handled
     * @param lockFiles The lockFiles object that will be used
     * @param SERVER_ROOT The root of the server
     * @param PATH404 The path of the 404 page
     */
    public ClientHandler(Socket client,
                         LockFiles lockFiles,
                         String SERVER_ROOT,
                         String PATH404) {

        this.lockFiles = lockFiles;
        this.SERVER_ROOT = SERVER_ROOT;
        this.client = client;
        this.PATH404 = PATH404;
    }

    /**
     * Get the tokens of the BufferedReader from the client Socket. Used for getting the URL, and consecutively
     * the path of the page wanted
     * @param br The BufferedReader from the client
     * @return tokens of the BufferedReader
     * @throws IOException When the BufferedReader is invalid
     */
    private String[] getTokens( BufferedReader br ) throws IOException {

        StringBuilder requestBuilder = new StringBuilder();
        String line;
        while ( !( line = br.readLine() ).isBlank() ) {
            requestBuilder.append( line ).append( "\r\n" );
        }

        String request = requestBuilder.toString();
        String[] tokens = request.split( " " );
        if (tokens.length < 2) {
            System.err.println("Invalid request received.");
            return null;
        }

        return tokens;
    }

    /**
     * Handles the client request
     */
    private void clientRequest(){

        //instantiation of variables because of finally block
        String routePath = "";

        try(BufferedReader br = new BufferedReader( new InputStreamReader( client.getInputStream() ) );
            OutputStream clientOutput = client.getOutputStream()) {

            System.out.println("New client connected: " + client + "on Thread: " + Thread.currentThread().getId());

            String[] tokens = getTokens( br );
            if ( tokens == null || tokens.length == 0 )
                throw new IOException( "Invalid request received.");

            String route = tokens[1];
            routePath = SERVER_ROOT + route;

            System.out.println( "Route: " + routePath );

            routePath = routePath.replace( "/", "\\" );

            byte[] content;


            boolean endsWithHtml = routePath.endsWith( ".html" );
            Path path = Paths.get( routePath );

            Path pageLockedPath = null;

            try {
                if (endsWithHtml) {
                    if (lockFiles.exists(path))
                    { //if the page html exists itself
                        lockFiles.lock(path);
                        pageLockedPath = path;
                        content = readBinaryFile(routePath); //loads the .html page
                    }
                    else
                    {
                        throw new FileNotFoundException("File not found: " + routePath);//not found
                    }
                }
                else if ( Files.exists( path ) )
                { //not html but exists
                    if( Files.isDirectory( path ) ) { //and is directory
                        Path indexPath = Paths.get( path + "\\index.html");
                        if (Files.exists(indexPath)) { //and index.html of directory exists

                            endsWithHtml = true;
                            lockFiles.lock(indexPath);
                            pageLockedPath = indexPath;

                            content = readBinaryFile(indexPath.toString());
                            System.out.println("Page route: " + indexPath);
                        } else { //index.html of directory does NOT exist
                            throw new FileNotFoundException("File not found: " + path + "/index.html");
                        }
                    }
                    else //exists but is not a Directory neither a .html file
                    {
                        content = readBinaryFile(path.toString());
                    }
                } else { //does NOT exist
                    throw new FileNotFoundException("File not found: " + routePath);
                }
            }
            catch(FileNotFoundException e)
            {
                System.out.println("path not found : " + routePath);
                endsWithHtml = true;
                Path path404 = Paths.get(PATH404);
                lockFiles.lock(path404);
                pageLockedPath = path404;
                content = readBinaryFile(PATH404);
            }



            if ( clientOutput != null ) {
                // Send HTTP response headers


                clientOutput.write( "HTTP/1.1 200 OK\r\n".getBytes() );
                clientOutput.write( "Content-Type: text/html\r\n".getBytes() );
                clientOutput.write( "\r\n".getBytes() );

                // Send response body
                clientOutput.write( content );
                clientOutput.write( "\r\n\r\n".getBytes() );
                clientOutput.flush();
                client.close();
                if(endsWithHtml) {
                    Thread.sleep(15000);
                    lockFiles.unlock(pageLockedPath);
                    //Thread.sleep for testing threads
                }


            }
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
        catch ( SocketException e ) {
            System.err.println( "[CLIENT DISCONNECTED]: " + e.getMessage() );
            e.printStackTrace();
        }
        catch( IOException e ){
            e.printStackTrace();
        }
        catch ( Exception e ){
            e.printStackTrace();
        }
    }

    /**
     * Reads a binary file and returns its contents as a byte array.
     *
     * @param path The file path to read.
     * @return A byte array containing the file's contents, or an empty array if an error occurs.
     */
    private byte[] readBinaryFile( String path ) {
        try {
            return Files.readAllBytes( Paths.get( path ) );
        } catch ( IOException e ) {
            System.err.println( "Error reading file: " + path );
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Run method of the ClientHandler
     */
    @Override
    public void run() {
        clientRequest();
    }
}
