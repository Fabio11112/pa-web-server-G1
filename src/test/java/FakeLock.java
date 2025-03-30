import org.jetbrains.annotations.NotNull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

class FakeLock implements Lock {
    private boolean isLocked = false;

    @Override
    public void lock() { isLocked = true; }

    @Override
    public void unlock() { isLocked = false; }

    @Override
    public boolean tryLock() {
        throw new UnsupportedOperationException("Not needed for tests");
    } // Unused

    @Override
    public boolean tryLock(long time, @NotNull TimeUnit unit) {
        throw new UnsupportedOperationException("Not needed for tests");
    } // Unused

    @Override
    public @NotNull Condition newCondition() {
        throw new UnsupportedOperationException("Not needed for tests");
    } // Unused

    @Override
    public void lockInterruptibly(){
        throw new UnsupportedOperationException("Not needed for tests");
    }

    public boolean getIsLocked() {
        return isLocked;
    }
}