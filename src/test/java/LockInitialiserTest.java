import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for LockInitialiser
 */
@DisplayName("Lock Initialiser")
public class LockInitialiserTest {


    @Test
    @DisplayName("Test the correct creation of locks")
    void testCreateLocks() {

        LockInitialiser lockInitialiser = new LockInitialiser("html");
        TreeMap<String, Lock> locks = lockInitialiser.createLocks("sites");
        assertNotNull(locks);

        try (Stream<Path> stream = Files.walk(Paths.get("sites"))) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".html"))
                    .forEach(path -> assertTrue(locks.containsKey(path.toString())));
        } catch (IOException e) {
            fail("IOException occurred while listing .html files: " + e.getMessage());
        }
    }


}
