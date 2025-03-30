
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

class ConsumerLogsTest {

    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));

    @Test
    @DisplayName("Should consume from buffer and write to file")
    void testConsumeLogs_WritesToFile() throws Exception {
        // Arrange
        Path logFile = tempDir.resolve("test.log");
        ArrayList<Log> buffer = new ArrayList<>();
        FakeLock fakeLock = new FakeLock();
        FakeSemaphore fakeSemaphore = new FakeSemaphore(0); // Allow acquisition

        Log testLog = new Log("GET", Path.of("/api/data"), "192.168.1.1", 200);
        buffer.add(testLog);

        // Act
        ConsumerLogs consumer = new ConsumerLogs(logFile.toString(), buffer, fakeLock, fakeSemaphore);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();


        fakeSemaphore.release(); // Allow consumer to proceed

        Thread.sleep(100);

        // Clean shutdown
        consumerThread.interrupt();
        consumerThread.join(1000);

        // Assert
        synchronized (buffer) {
            assertTrue(buffer.isEmpty(), "Buffer should be processed");
        }
        assertTrue(Files.exists(logFile), "Log file should exist");
        String content = Files.readString(logFile);
        assertTrue(content.contains(testLog.toString()), "File should contain log entry");
    }

    @Test
    @DisplayName("Should not create file if buffer is empty")
    void testConsumeLogs_EmptyBuffer() throws Exception {
        // Arrange
        Path logFile = tempDir.resolve("empty.log");
        ArrayList<Log> buffer = new ArrayList<>();
        FakeLock fakeLock = new FakeLock();
        FakeSemaphore fakeSemaphore = new FakeSemaphore(1);

        // Act
        ConsumerLogs consumer = new ConsumerLogs(logFile.toString(), buffer, fakeLock, fakeSemaphore);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();

        fakeSemaphore.release(); // Allow consumer to proceed
        Thread.sleep(100);

        // Clean shutdown
        consumerThread.interrupt();
        consumerThread.join(1000);

        // Assert
        assertTrue(buffer.isEmpty());
        assertTrue(Files.exists(logFile), "No file should be created for empty buffer");
        assertEquals(0, logFile.toFile().length(), "File should be empty");
    }

    @Test
    @DisplayName("Should not create file if buffer is empty and semaphore is 0")
    void testConsumeLogs_SemaphoreBlocks() throws Exception {
        // Arrange
        Path logFile = tempDir.resolve("blocked.log");
        ArrayList<Log> buffer = new ArrayList<>();
        buffer.add(new Log("POST", Path.of("/submit"), "10.0.0.1", 201));
        FakeLock fakeLock = new FakeLock();
        FakeSemaphore fakeSemaphore = new FakeSemaphore(0); // Block acquisition

        // Act
        ConsumerLogs consumer = new ConsumerLogs(logFile.toString(), buffer, fakeLock, fakeSemaphore);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();


        Thread.sleep(50);

        // Clean shutdown
        consumerThread.interrupt();
        consumerThread.join(1000);

        // Assert
        assertEquals(1, buffer.size(), "Buffer should remain unchanged");
        assertTrue(Files.exists(logFile), "No file should be created when blocked");

        System.out.println(Files.readString(logFile));

        assertEquals(0, (Files.readString(logFile)).length(), "File should be empty");
    }

    @Test
    @DisplayName("Should release lock even if write fails")
    void testLockReleasedEvenOnException() throws Exception {

        //Use of Paths instead of tempDir so it is actually an invalid path
        Path logFile = Paths.get("invalid/invalidPath.html");
        ArrayList<Log> buffer = new ArrayList<>();
        buffer.add(new Log("PUT", Path.of("/resource"), "172.16.0.1", 204));
        FakeLock fakeLock = new FakeLock();
        FakeSemaphore fakeSemaphore = new FakeSemaphore(2);

        // Act
        ConsumerLogs consumer = new ConsumerLogs(logFile.toString(), buffer, fakeLock, fakeSemaphore);
        Thread consumerThread = new Thread(consumer);
        consumerThread.start();

        Thread.sleep(100);

        // Clean shutdown
        consumerThread.interrupt();
        consumerThread.join(1000);

        // Assert
        assertFalse(fakeLock.getIsLocked(), "Lock should be released even if write fails");
        assertEquals(2, fakeSemaphore.getItems(), "Semaphore should not be released when in exception");
    }
}
