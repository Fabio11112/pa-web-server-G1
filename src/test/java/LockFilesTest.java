import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for LockInitialiser
 */
@DisplayName("Lock Initialiser")
public class LockFilesTest {

    LockFiles locks;

    @BeforeEach
    void setUp(){
        locks = new LockFiles("html", "html");
    }

    @Test
    @DisplayName("Tests if the paths are in the lockFiles")
    void testCreateLocks_HasSitesInMap() {

        System.out.println(locks);
        assertAll(
                () -> assertNotNull(locks),
                () -> assertNotNull(locks.getMap())
        );


        for(Path path : locks.getMap().keySet()){
            assertTrue(Files.exists(path));
        }
    }


    @ParameterizedTest
    @CsvSource({"sites/405.html",
            "sites/pages/direction.html",
            "sites/pages/navigation/about.html",
            "sites/page/nav/home.html",
            "sites/pages/nav/content/contact.html",
            "site/pages/nav/contact"})
    @DisplayName("Tests if the paths are NOT in the lockFiles")
    void testCreateLocks_HasNotSitesInMap(String path) {

        assertAll(
                () -> assertNotNull(locks),
                () -> assertFalse(locks.exists(Paths.get(path)), "The path should not be in the map")
        );
    }

    @Test
    @DisplayName("Test if a file is locked")
    void testFileIsLocked() throws InterruptedException {
        Path path = Paths.get("html/404.html");
        locks.lock(path);

        Lock lock = locks.getLock(path);
        assertNotNull(lock);


        // Create a new thread to test the lock
        Thread testThread = new Thread(() -> {
            assertFalse(lock.tryLock(), "The file should be unlocked and tryLock should succeed");
        });

        testThread.start();
        testThread.join();

        locks.unlock(path);
    }

    @Test
    @DisplayName("Test if a file is unlocked")
    void testFileIsUnlocked() throws InterruptedException {
        Path path = Paths.get("html/404.html");
        locks.lock(path);
        locks.unlock(path);
        Lock lock = locks.getLock(path);
        assertNotNull(lock);

        // Create a new thread to test the lock
        Thread testThread = new Thread(() -> {
            assertTrue(lock.tryLock(), "The file should be unlocked and tryLock should succeed");
            lock.unlock();
        });

        testThread.start();
        testThread.join();
    }

    @Test
    @DisplayName("Test getLock method")
    void testGetLock() {
        Path path = Paths.get("html/404.html");
        locks.lock(path);
        Lock lock = locks.getLock(path);
        assertNotNull(lock, "The lock should not be null");
        assertTrue(lock.tryLock(), "The lock should be acquired successfully");
        lock.unlock();
    }


}
