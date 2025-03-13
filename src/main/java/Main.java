import java.util.TreeMap;
import java.util.concurrent.locks.Lock;

public class Main {
    public static void main(String[] args) {
        LockInitialiser lockInitialiser = new LockInitialiser("html");
        TreeMap<String, Lock> locks = lockInitialiser.createLocks("pages");
        MainHTTPServerThread s = new MainHTTPServerThread(8888);
        s.start();
        try {
            s.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
