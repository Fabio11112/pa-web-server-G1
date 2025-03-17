import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;


/**
 * Represents the locks of each file that has the same extension
 */
public class LockFiles {
    private final ConcurrentHashMap<Path, Lock> map = new ConcurrentHashMap<>();
    private final String extension;


    /**
     * Constructor for the LockFiles class
     * @param extension The extension that will be checked
     */
    public LockFiles(String extension, String directoryPath) {
        this.extension = extension;
        createLocks(directoryPath);
    }

    /**
     * Checks if the path inserted is in the map
     * @param path path that will be checked
     * @return True if the path is in the map, false otherwise
     */
    public boolean exists(Path path) {
        return map.containsKey(path);
    }

    /**
     * Locks the file that has the path given
     * @param path The path of the file that will be locked
     */
    public void lock (Path path) {
        if(exists(path)) {
            getLock(path).lock();
        }

    }

    /**
     * Unlocks the file that has the path given
     * @param path The path of the file that will be unlocked
     */
    public void unlock (Path path) {
        if(exists(path)) {
            getLock(path).unlock();
        }

    }

    /**
     * Creates the ConcurrentHashMap with the paths of the files that has the extension given
     *
     * @param directoryPath The path of the directory that will be checked
     *
     * If there is a IOException, it prints the stack trace
     */
    private void createLocks( String directoryPath ) {
        Path dir = Paths.get( directoryPath );

        try( Stream<Path> stream = Files.walk( dir ) ){
            stream.forEach( path -> {
                if( !Files.isDirectory( ( path ) ) && hasSameExtension( path.toFile(), extension ) ){
                    map.put( path, new ReentrantLock() );
                }
            });

            for(Path value: map.keySet())
            {
                System.out.println( value );
            }

        }
        catch( IOException e )
        {
            e.printStackTrace();
        }

    }



    /**
     * Verifies if the file has the same extension as the one put
     *
     * @param file The file that will be checked
     * @param extension The extension that will be checked
     * @return True if the file has the same extension as the one put, false otherwise
     */
    private boolean hasSameExtension( File file, String extension ){

        // Get the file name
        String fileName = file.getName();

        // Get the file extension
        String fileExtension = "";
        int dotIndex = fileName.lastIndexOf( '.' );
        if ( dotIndex > 0 ) { // Ensure the dot is not the first character
            fileExtension = fileName.substring( dotIndex + 1 );
        }
        return fileExtension.equals( extension );

    }

    /**
     * Returns the lock of the path given
     * @param path The path of the file that has the lock
     * @return The lock of the path given
     */
    public Lock getLock(Path path) {
        return map.get(path);
    }

}
