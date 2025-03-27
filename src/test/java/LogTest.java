
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class LogTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String method = "GET";
        Path path = Paths.get("/test/resource");
        String origin = "127.0.0.1";
        int response = 200;

        // Act
        Log log = new Log(method, path, origin, response);

        // Assert
        assertNotNull(log.toString()); // Ensure toString() doesn't throw an exception
        assertTrue(log.toString().contains(method));
        assertTrue(log.toString().contains(path.toString()));
        assertTrue(log.toString().contains(origin));
        assertTrue(log.toString().contains(String.valueOf(response)));

        // Verify timestamp is roughly correct (within a few seconds of now)
        String timestamp = log.toString().split("\"timestamp\":\"")[1].split("\"")[0];
        LocalDateTime logTime = LocalDateTime.parse(timestamp, FORMATTER);
        LocalDateTime now = LocalDateTime.now();
        assertTrue(logTime.isBefore(now.plusSeconds(2)) && logTime.isAfter(now.minusSeconds(2)));
    }

    @Test
    void testToStringFormat() {
        // Arrange
        String method = "POST";
        Path path = Paths.get("/api/data");
        String origin = "192.168.1.1";
        int response = 404;
        Log log = new Log(method, path, origin, response);

        // Act
        String logString = log.toString();

        // Assert
        assertTrue(logString.startsWith("{\n"));
        assertTrue(logString.endsWith("}\n"));
        assertTrue(logString.contains("   \"timestamp\":\"")); // Check indentation and format
        assertTrue(logString.contains("   \"method\":\"" + method + "\","));
        assertTrue(logString.contains("   \"route\":\"" + path + "\","));
        assertTrue(logString.contains("   \"origin\":\"" + origin + "\","));
        assertTrue(logString.contains("   \"HTTP response status\":\"" + response + "\""));
    }
}
