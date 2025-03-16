import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for LockInitialiser
 */
@DisplayName("Lock Initialiser")
public class LockFilesTest {


    @Test
    @DisplayName("Test the correct creation of locks")
    void testCreateLocks() {

        LockFiles lockInitialiser = new LockFiles("html", "sites");
        ConcurrentLinkedQueue<String> list = lockInitialiser.("sites");
        assertNotNull(list);

        try (Stream<Path> stream = Files.walk(Paths.get("sites"))) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".html"))
                    .forEach(path -> assertTrue(list.contains(path.toString())));
        } catch (IOException e) {
            fail("IOException occurred while listing .html files: " + e.getMessage());
        }
    }


}
