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

    /**
     * Method that will be executed before the task is executed
     * @param t the thread that will run task
     * @param r the task that will be executed
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
    }

    /**
     * Method that will be executed after the task is executed
     * @param r the runnable that has completed
     * @param t the exception that caused termination, or null if
     * execution completed normally
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        System.out.println("Task Completed" + r);
    }

    /**
     * Method that will be executed after the thread pool is terminated
     */
    @Override
    protected void terminated() {
        super.terminated();
    }






}
