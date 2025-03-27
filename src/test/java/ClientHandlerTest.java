import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientHandlerTest {


    private LockFiles lockFiles;
    private Socket client;
    private FakeLock bufferLock;
    private FakeSemaphore itemsAvailable;
    private ArrayList<Log> buffer;
    private ClientHandler clientHandler;
    private final String SERVER_ROOT = "sites/pages";
    private final String PATH404 = "/server/root/404.html";

    @BeforeEach
    void setUp() throws IOException {
        lockFiles = new LockFiles("html", SERVER_ROOT){
            public boolean lock(String path) {
                super.lock(Paths.get(path));
                return true;
            }
        };
        String fakeRequest = "GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n";
        client = new FakeSocket(fakeRequest);

        itemsAvailable = new FakeSemaphore(0);
        buffer = new ArrayList<>();

        bufferLock = new FakeLock(){
            @Override
            public void lock() {
                // Do nothing
            }
        };

        clientHandler = new ClientHandler(client, lockFiles, SERVER_ROOT, PATH404, bufferLock, itemsAvailable, buffer);
    }

    @Test
    void testGetTokensValidRequest() throws IOException {
        String request = "GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(request));

        String[] tokens = clientHandler.getTokens(reader);

        assertNotNull(tokens);
        assertEquals("GET", tokens[0]);
        assertEquals("/index.html", tokens[1]);
    }

    @Test
    void testGetTokensInvalidRequest() throws IOException {
        String request = "GET /invalid HTTP/1.1\r\nHost: localhost\r\n";
        BufferedReader reader = new BufferedReader(new StringReader(request));

        String[] tokens = clientHandler.getTokens(reader);

        assertNull(tokens);
    }

    @Test
    void testRoutingValidFile() throws IOException {
        Path existingPath = Paths.get(SERVER_ROOT, "index.html");

        Path result = clientHandler.Routing(existingPath);
        assertEquals(existingPath, result);

        Files.delete(existingPath); // Clean up
    }

    @Test
    void testRouting404File() {
        Path nonExistentPath = Paths.get(SERVER_ROOT, "nonexistent.html");

        Path result = clientHandler.Routing(nonExistentPath);
        assertEquals(Paths.get(PATH404), result);
    }


}
