import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;

public class FakeSocket extends Socket {
    private final ByteArrayInputStream inputStream;
    private final ByteArrayOutputStream outputStream;

    public FakeSocket(String requestData) {
        this.inputStream = new ByteArrayInputStream(requestData.getBytes());
        this.outputStream = new ByteArrayOutputStream();
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void close() throws IOException {
        // No real socket, so nothing to close.
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    // Throw UnsupportedOperationException for unnecessary methods
    @Override
    public void connect(java.net.SocketAddress endpoint) {
        throw new UnsupportedOperationException("Not implemented in mock.");
    }

    @Override
    public void bind(java.net.SocketAddress bindpoint) {
        throw new UnsupportedOperationException("Not implemented in mock.");
    }

    @Override
    public boolean isConnected() {
        throw new UnsupportedOperationException("Not implemented in mock.");
    }

    @Override
    public boolean isBound() {
        throw new UnsupportedOperationException("Not implemented in mock.");
    }

    @Override
    public boolean isInputShutdown() {
        throw new UnsupportedOperationException("Not implemented in mock.");
    }

    @Override
    public boolean isOutputShutdown() {
        throw new UnsupportedOperationException("Not implemented in mock.");
    }

    @Override
    public String toString() {
        return "Fake Socket";
    }
}
