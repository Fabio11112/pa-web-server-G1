import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

class FakeSemaphore extends Semaphore {
    public int items;

    public FakeSemaphore(int items) {
        super(0);
        this.items = items;
    }

    @Override
    public boolean tryAcquire(long timeout, TimeUnit unit) {
        if (items == 0) {
            return false;
        }
        --items;
        return true; // Only implement what you test
    }

    @Override
    public void release() {
        ++items;
    }

    // No need to override other methods
}