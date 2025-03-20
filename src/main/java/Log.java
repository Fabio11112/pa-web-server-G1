import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a log of a request
 */
public class Log {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );
    private final String timestamp;
    private final String method;
    private final Path path;
    private final String origin;
    private final int response;

    /**
     * Constructor for the Log class
     * @param method The HTTP method of the request
     * @param path The path of the resource requested
     * @param origin The origin of the request
     * @param response The HTTP response status
     */
    public Log( String method, Path path, String origin, int response ) {
        this.timestamp = LocalDateTime.now( ).format( formatter );
        this.method = method;
        this.path = path;
        this.origin = origin;
        this.response = response;
    }

    /**
     * Returns the timestamp of the log
     * @return The timestamp of the log in json format
     */
    @Override
    public String toString( ) {
        return "{\n"+
                "   \"timestamp\":\"" + timestamp + "\",\n" +
                "   \"method\":\"" + method + "\",\n" +
                "   \"route\":\"" + path + "\",\n" +
                "   \"origin\":\"" + origin + "\",\n" +
                "   \"HTTP response status\":\"" + response + "\"\n"+
                "}\n";
    }

}