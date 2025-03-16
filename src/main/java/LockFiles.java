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
    private final ConcurrentHashMap<String, Lock> map = new ConcurrentHashMap<>();
    private final String extension;


    /**
     * Constructor for the LockFiles class
     * @param extension The extension that will be checked
     */
    public LockFiles(String extension, String directoryPath) {
        this.extension = extension;
        createLocks(directoryPath);
    }

    public boolean exists(String path) {
        return map.containsKey(path);
    }

    public void lock (String path) {
        if(exists(path)) {
            map.get(path).lock();
        }

    }

    public void unlock (String path) {
        if(exists(path)) {
            map.get(path).unlock();
        }

    }

    /**
     * Creates the ConcurrentHashMap with the paths of the files that has the extension given
     *
     * @param directoryPath The path of the directory that will be checked
     * @return The ConcurrentHashMap with the paths of the files that has the extension given.
     * If there is an error, it will return an empty ConcurrentHashMap
     */
    private ConcurrentHashMap<String, Lock> createLocks( String directoryPath ) {
        Path dir = Paths.get( directoryPath );

        try( Stream<Path> stream = Files.walk( dir ) ){
            stream.forEach( path -> {
                if( !Files.isDirectory( ( path ) ) && hasSameExtension( path.toFile(), extension ) ){
                    map.put( path.toString(), new ReentrantLock() );
                }
            });
            return map;
        }
        catch( IOException e )
        {
            e.printStackTrace();
            return new ConcurrentHashMap<>();
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





}
