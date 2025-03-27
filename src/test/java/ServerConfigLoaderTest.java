import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ServerConfigLoaderTest {
    @TempDir
    Path tempDir;

    @Test
    void testEmptyConfigFile() throws IOException {
        String configContent = """
            port=8080
            corePoolSize=5
            maximumPoolSize=10
            directory=/var/www/html
            path404=/error/404.html
            extension=.html
            logPath=/var/log/server.log
            keepAliveTime=60
            maxQueueThreadSize=100
            """;
        // Create an empty config file
        Path configFile = tempDir.resolve("empty.conf");
        Files.writeString(configFile, configContent);

        // Load the config
        ServerConfigLoader loader = new ServerConfigLoader(configFile.toString());

        // Verify all values are defaults
        assertEquals(8080, loader.getPort());
        assertEquals(5, loader.getCorePoolSize());
        assertEquals(10, loader.getMaxPoolSize());
        assertEquals("/var/www/html",loader.getDirectory());
        assertEquals("/error/404.html", loader.getPath404());
        assertEquals(".html",loader.getExtension());
        assertEquals("/var/log/server.log", loader.getLogPath());
        assertEquals(60, loader.getKeepAliveTime());
        assertEquals(100, loader.getMaxQueueThreadSize());
    }
}
