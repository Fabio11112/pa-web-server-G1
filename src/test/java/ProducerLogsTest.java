
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;

class ProducerLogsTest {

    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
    ArrayList<Log> buffer;
    FakeLock bufferLock;
    FakeSemaphore itemsAvailable;
    Log testLog;

    @BeforeEach
    public void Setup() {
        buffer = new ArrayList<>();
        testLog = new Log("GET", tempDir, "0.0.0.0", 200);
    }


    @Test
    public void testProduceLogs_addsLogToBuffer() {

        bufferLock = new FakeLock();
        itemsAvailable = new FakeSemaphore(0);
        ProducerLogs producer = new ProducerLogs(buffer, bufferLock, itemsAvailable, testLog);
        producer.run();

        assertEquals(1, buffer.size(), "Buffer should contain one log");
        assertEquals(testLog, buffer.get(0), "Buffer should contain the test log");
        assertEquals(1, itemsAvailable.getItems(), "Semaphore should have one permit");
    }

    @Test
    public void testProduceLogs_lockIsReleasedAfterException() {
        FakeLock bufferLock = new FakeLock(){
            @Override
            public void lock() {
                throw new RuntimeException("Test exception");
            }
        };
        FakeSemaphore itemsAvailable = new FakeSemaphore(0);
        Log testLog = new Log("GET", tempDir, "0.0.0.0", 200);

        ProducerLogs producer = new ProducerLogs(buffer, bufferLock, itemsAvailable, testLog);
        producer.run();

        assertFalse(bufferLock.getIsLocked(), "Lock should be release even after exception");// Verify unlock is called even after exception
    }
}
