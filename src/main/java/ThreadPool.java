import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A ThreadPool that will order the threads from the requests
 */
public class ThreadPool extends ThreadPoolExecutor {

    /**
     * Constructor for the Thread Pool class
     *
     * @param corePoolSize Minimum amount of threads to keep in the thread pool
     * @param maxPoolSize Maximum amount of threads to keep in the thread pool
     * @param keepAliveTime Maximum amount of time to keep idle thread alive in milliseconds
     * @param maxQueueThreadSize Maximum amount of threads to keep in queue
     */
    public ThreadPool(int corePoolSize, int maxPoolSize, int keepAliveTime, int maxQueueThreadSize) {
        super(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(maxQueueThreadSize) {
                });
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        System.out.println("Task Completed" + r);
    }

    @Override
    protected void terminated() {
        super.terminated();
    }






}
