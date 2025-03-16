public class Main {
    public static void main( String[] args ) {
        Runnable[] tasks = new Runnable[5];
        String directory = "sites";
        String extension = "html";
        LockFiles lockFiles = new LockFiles( extension, directory );

        ThreadPool pool = new ThreadPool(tasks.length, tasks.length, 5000 , 10);
        MainHTTPServerThread server = new MainHTTPServerThread(8888, pool, directory, lockFiles);
        server.startServer();
    }
}