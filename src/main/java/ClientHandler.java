import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientHandler implements Runnable{

    private final LockFiles lockFiles;
    private final String SERVER_ROOT;
    private final Socket client;

    public ClientHandler(Socket client,
                         LockFiles lockFiles,
                         String SERVER_ROOT) {

        this.lockFiles = lockFiles;
        this.SERVER_ROOT = SERVER_ROOT;
        this.client = client;
    }

    private String[] getTokens( BufferedReader br ) throws IOException {
        try {
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
        catch( IOException e ){
            throw e;
        }
    }

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
            if( endsWithHtml) {
                if (  lockFiles.exists( routePath ) ) {
                    //clientOutput.write(readBinaryFile( routePath ));

                    /* logic for mutexes over here */

                } else {
                    throw new FileNotFoundException( routePath );
                }
            }
            content = readBinaryFile( routePath );


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

    @Override
    public void run() {
        clientRequest();
    }
}
