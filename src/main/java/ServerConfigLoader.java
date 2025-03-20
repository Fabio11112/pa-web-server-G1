import java.io.BufferedReader;
import java.io.IOException;

public class ServerConfigLoader {

    private String directory;
    private String path404;
    private String extension;
    private String logPath;

    private int port;
    private int maxRequest;
    private int corePoolSize;
    private int maxPoolSize;
    private int keepAliveTime;
    private int maxQueueThreadSize;

    /**
     * Constructor for the ServerConfigLoader class. It reads the file and sets the values of the class.
     *
     * @param path The file path to the server configurations.
     *
     */

    public ServerConfigLoader(String path) {
        decodeFile(path);
    }

    //_______________ GETTERS ______________

    /**
     * Gets the directory of the server
     * @return The directory of the server
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Gets the path of the 404 html file
     * @return The path of the 404 html file
     */
    public String getPath404() {
        return path404;
    }

    /**
     * Gets the extension of the files that will be locked
     * @return The extension of the files that will be locked
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Gets the path of the log file
     * @return The path of the log file
     */
    public String getLogPath() {
        return logPath;
    }

    /**
     * Gets the port of the server
     * @return The port of the server
     */
    public int getPort() {
        return port;
    }

    /**
     * Gets the maximum number of requests that the server can handle at the same time
     * @return The maximum number of requests that the server can handle at the same time
     */
    public int getMaxRequest() {
        return maxRequest;
    }

    /**
     * Gets the core pool size
     * @return The core pool size
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * Gets the maximum number of threads that can be created
     * @return The maximum number of threads that can be created
     */
    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    /**
     * Gets the time that the thread will be alive
     * @return The time that the thread will be alive
     */
    public int getKeepAliveTime() {
        return keepAliveTime;
    }

    /**
     * Gets the maximum number of threads that can be in the queue
     * @return The maximum number of threads that can be in the queue
     */
    public int getMaxQueueThreadSize() {
        return maxQueueThreadSize;
    }
//_________________________________


    /**
     * Reads the file of configuration and returns its content
     * @param path The path of the file to read
     * @return content The content of the file
     */
    public String readFile( String path ) {
        StringBuilder content = new StringBuilder();
        try ( BufferedReader reader = new BufferedReader( new java.io.FileReader( path ) ) ) {
            String line;
            while ( ( line = reader.readLine() ) != null ) {
                content.append( line ).append( "\n" );
            }
        } catch ( IOException e ) {
            System.err.println( "Error reading file: " + path );

        }
        return content.toString();
    }

    /**
     * Decodes the file and sets the values of the class
     * @param path The path of the file to decode
     */
    public void decodeFile(String path) {
        String content = readFile(path);
        String[] lines =  content.split("\n");

        for(String line : lines) {
            String[] parts = line.split("=");
            if(parts.length != 2)
                return;

            switch (parts[0]){
                case "port":
                    this.port = Integer.parseInt(parts[1]);
                    break;
                case "maximum_requests":
                    this.maxRequest = Integer.parseInt(parts[1]);
                    break;
                case "corePoolSize":
                    this.corePoolSize = Integer.parseInt(parts[1]);
                    break;
                case "maximumPoolSize":
                    this.maxPoolSize = Integer.parseInt(parts[1]);
                    break;
                case "directory":
                    this.directory = parts[1];
                    break;
                case "path404":
                    this.path404 = parts[1];
                    break;
                case "extension":
                    this.extension = parts[1];
                    break;
                case "logPath":
                    this.logPath = parts[1];
                    break;
                case "keepAliveTime":
                    this.keepAliveTime = Integer.parseInt(parts[1]);
                    break;
                case "maxQueueThreadSize":
                    this.maxQueueThreadSize = Integer.parseInt(parts[1]);
                    break;
            }

        }

    }
}