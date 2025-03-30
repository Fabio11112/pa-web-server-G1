import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ThreadPoolTest {

    private ThreadPool threadPool;

    @BeforeEach
    void setUp() {
        threadPool = new ThreadPool(2, 4, 1000, 10);
    }

    @AfterEach
    void tearDown() {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
    }

    @Test
    @DisplayName("Test if the thread pool is initialized correctly")
    void testTaskExecution() throws InterruptedException {
        AtomicBoolean taskExecuted = new AtomicBoolean(false);

        Runnable task = () -> taskExecuted.set(true);

        threadPool.execute(task);

        Thread.sleep(500); // Give some time for execution

        assertTrue(taskExecuted.get(), "Task should have been executed.");
    }

    @Test
    @DisplayName("Test if multiple tasks can be executed")
    void testMultipleTasksExecution() throws InterruptedException {
        AtomicBoolean task1Executed = new AtomicBoolean(false);
        AtomicBoolean task2Executed = new AtomicBoolean(false);

        Runnable task1 = () -> task1Executed.set(true);
        Runnable task2 = () -> task2Executed.set(true);

        threadPool.execute(task1);
        threadPool.execute(task2);

        Thread.sleep(500);

        assertTrue(task1Executed.get(), "Task 1 should have been executed.");
        assertTrue(task2Executed.get(), "Task 2 should have been executed.");
    }

    @Test
    @DisplayName("Test if the thread pool is shut down correctly")
    void testShutdownBehavior() {
        threadPool.shutdown();
        assertTrue(threadPool.isShutdown(), "ThreadPool should be shut down.");
    }
}
