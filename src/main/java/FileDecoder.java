import java.io.BufferedReader;
import java.io.IOException;

public class FileDecoder {

    String directory;
    String path404;
    String extension;
    String logPath;
    String configPath;

    int maxRequest;
    int corePoolSize;
    int maxPoolSize;

    /**
     * Reads a text file and returns its contents as a string.
     *
     * @param path The file path to read.
     * @return A string containing the file's contents, or an empty string if an error occurs.
     */

    public FileDecoder(String path) {



        decodeFile(path);
    }

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


    public void decodeFile(String path) {
        String content = readFile(path);
        String[] lines =  content.split("\n");

        for(String line : lines) {
            String[] parts = line.split("=");
            if(parts.length != 2)
                return;

            switch (parts[0]){
                case "port":
                    System.out.println("Port: " + parts[1]);
                    break;
                case "maximum_requests":
                    System.out.println("Maximum Requests: " + parts[1]);
                    break;
            }

        }

    }
}