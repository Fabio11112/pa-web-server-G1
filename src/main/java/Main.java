import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class Main {
    public static void main( String[] args ) {
        Runnable[] tasks = new Runnable[5];
        LockInitializer lockInitializer = new LockInitializer( "html" );
        ThreadPool pool = new ThreadPool(tasks.length, tasks.length, 5000 );



        for ( int i = 0; i < 2; i++ ) {
            tasks[i] = new MainHTTPServerThread( 8888 );
            pool.execute( tasks[i] );
        }

        pool.shutdown();

        try {

            if ( !pool.awaitTermination( 60, TimeUnit.SECONDS ) ) {
                System.err.println( "ThreadPool did not terminate within the timeout." );
            }
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        }
    }
}