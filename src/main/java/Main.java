/**
 * Main class that starts the server and the thread pool.
 */
public class Main {
    /**
     * Main method that starts the server and the thread pool.
     * @param args The arguments of the program
     */
    public static void main( String[] args ) {
        Runnable[] tasks = new Runnable[5];
        String directory = "sites";
        String path404 = "sites/404.html";
        String extension = "html";
        LockFiles lockFiles = new LockFiles( extension, directory );

        ThreadPool pool = new ThreadPool(tasks.length, tasks.length, 5000 , 10);
        MainHTTPServer server = new MainHTTPServer(8888, pool, directory, path404, lockFiles);
        server.startServer();
    }
}