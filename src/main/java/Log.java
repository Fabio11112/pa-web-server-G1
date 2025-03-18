import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.json.JSONObject;


public class Log {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String timestamp;
    private final String method;
    private final Path path;
    private final String origin;
    private final String response;

    public Log(String method, Path path, String origin, String response) {
        this.timestamp = LocalDateTime.now().format(formatter);
        this.method = method;
        this.path = path;
        this.origin = origin;
        this.response = response;
    }

    @Override
    public String toString() {
        return new JSONObject()
                .put("timestamp", timestamp)
                .put("method", method)
                .put("route", path.toString())
                .put("origin", origin)
                .put("HTTP response status", response)
                .toString();
    }

}