import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;

/**
 * A simple HTTP server that listens on a specified port.
 * It serves files from a predefined server root directory.
 */
public class MainHTTPServerThread extends Thread {
    TreeMap<String, Lock> locks;
    private static final String SERVER_ROOT = "sites"; // Define by user
    private final int port;
    private ServerSocket server;

    /**
     * Constructor to initialize the HTTP server thread with a specified port.
     *
     * @param port The port number on which the server will listen.
     */
    public MainHTTPServerThread(int port) {
        this.port = port;

        LockInitialiser lockInitialiser = new LockInitialiser("html");
        locks = lockInitialiser.createLocks("sites");
    }

    /**
     * Reads a binary file and returns its contents as a byte array.
     *
     * @param path The file path to read.
     * @return A byte array containing the file's contents, or an empty array if an error occurs.
     */
    private byte[] readBinaryFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            System.err.println("Error reading file: " + path);
            e.printStackTrace();
            return new byte[0];
        }
    }

    /**
     * Reads a text file and returns its contents as a string.
     *
     * @param path The file path to read.
     * @return A string containing the file's contents, or an empty string if an error occurs.
     */
    private String readFile(String path) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + path);
            e.printStackTrace();
        }
        return content.toString();
    }

    /**
     * Starts the HTTP server and listens for incoming client requests.
     * Processes HTTP GET requests and serves files from the defined server root directory.
     */
    @Override
    public void run() {
        try {
            server = new ServerSocket(port);
            System.out.println("Server started on port: " + port);
            System.out.println("Working Directory: " + System.getProperty("user.dir"));

            while (true) {
                try (Socket client = server.accept();
                     BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                     OutputStream clientOutput = client.getOutputStream())
                {

                    System.out.println("New client connected: " + client);

                    //Reads and parses the HTTP Request
                    if(!clientRequest(clientOutput, br))
                        continue;


                } catch (IOException e) {
                    System.err.println("Error handling client request.");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: Unable to start on port " + port);
            e.printStackTrace();
        }
    }


    private String[] getTokens(BufferedReader br) throws IOException {
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
        catch(IOException e){
            throw e;
        }
    }

    private boolean clientRequest(OutputStream clientOutput, BufferedReader br) throws IOException {
                 // Serve the requested file
        try {

            String[] tokens = getTokens(br);
            if (tokens == null || tokens.length == 0)
                return false;

            String route = tokens[1];
            String routePath = SERVER_ROOT + route;
            routePath = routePath.replace("/", "\\");

            locks.get(routePath).lock();
            clientOutput.write(readBinaryFile(routePath));
            byte[] content = readBinaryFile(routePath);

            // Send HTTP response headers
            clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
            clientOutput.write("Content-Type: text/html\r\n".getBytes());
            clientOutput.write("\r\n".getBytes());

            // Send response body
            clientOutput.write(content);
            clientOutput.write("\r\n\r\n".getBytes());
            clientOutput.flush();
            return true;
        }
        catch(IOException e){
            throw e;
        }
    }
}