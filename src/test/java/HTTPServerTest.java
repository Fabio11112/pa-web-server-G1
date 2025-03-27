import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;

class HTTPServerTest {
    Thread serverThread;

    private HTTPServer server;
    private ThreadPool threadPool;
    private LockFiles pathPagesMap;
    private ArrayList<Log> buffer;
    private ReentrantLock bufferLock;
    private Semaphore itemsAvailable;
    private final String SERVER_ROOT = "./www";
    private final String PATH404 = "./www/404.html";
    private int port; //port is NOT final. Tests run concurrently, so if we use the same port, the tests will fail

    @BeforeEach
    void setUp() {
        try(FileWriter writer = new FileWriter("html/tempPage")) {
            writer.write("<html><head></head><body>");
            writer.close();

            threadPool = new ThreadPool(2, 4, 1000, 10);
            pathPagesMap = new LockFiles(".html", "html"); // Assuming LockFiles is a valid class
            buffer = new ArrayList<>();
            bufferLock = new ReentrantLock();
            itemsAvailable = new Semaphore(0);
        } catch (IOException e) {
            fail("Test failed in creating the temporary file");
        }
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
    void testServerStarts() {
        port = 8080;
        server = new HTTPServer(port, threadPool, SERVER_ROOT, PATH404, pathPagesMap, buffer, bufferLock, itemsAvailable);
        Thread serverThread = new Thread(() -> server.startServer());
        serverThread.start();

        try {
            Thread.sleep(500);
            Socket socket = new Socket("localhost", port);
            assertTrue(socket.isConnected(), "Server should accept connections.");
            socket.close();
        } catch (IOException | InterruptedException e) {
            fail("Server did not start correctly.");
        } finally {
            stopServer();
        }
    }

    @Test
    void testServerHandlesMultipleConnections() {
        port = 8081;
        server = new HTTPServer(port, threadPool, SERVER_ROOT, PATH404, pathPagesMap, buffer, bufferLock, itemsAvailable);

        Thread serverThread = new Thread(() -> server.startServer());
        serverThread.start();

        try {
            Thread.sleep(500);
            Socket client1 = new Socket("localhost", port);
            Socket client2 = new Socket("localhost", port);

            assertTrue(client1.isConnected() && client2.isConnected(), "Server should handle multiple connections.");

            client1.close();
            client2.close();
        } catch (IOException | InterruptedException e) {
            fail("Server did not handle multiple connections correctly.");
        } finally {
            stopServer();
        }
    }

    void stopServer() {
        try {
            Socket socket = new Socket("localhost", port);
            socket.close();

            if (serverThread != null && serverThread.isAlive()) {
                serverThread.interrupt();
                serverThread.join();
            }
        } catch (IOException | InterruptedException ignored) {
            System.out.println("Server did not stop correctly.");
        }
    }

}

