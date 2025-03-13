import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;


/**
 * Initialises the locks for the files in the directory that has the same extension as the one put
 */
public class LockInitialiser {
    private final TreeMap<String, Lock> map = new TreeMap<>();
    private final String extension;


    /**
     * Constructor for the LockInitialiser class
     * @param extension The extension that will be checked
     */
    public LockInitialiser(String extension) {
        this.extension = extension;
    }

    /**
     * Creates the locks for the files in the directory that has the same extension as the one put
     *
     * @param directoryPath The path of the directory that will be checked
     * @return A TreeMap with the locks for the files in the directory that has the same extension as the one put
     */
    public TreeMap<String, Lock> createLocks(String directoryPath ) {
        Path dir = Paths.get( directoryPath );

        try( Stream<Path> stream = Files.list( dir ) ){
            stream.forEach( path -> {
                if( Files.isDirectory( path ) ){
                    map.putAll( createLocks( path.toString() ) );
                }
                else if( hasSameExtension( path.toFile(), extension ) ) {
                    map.put(path.toString(), new ReentrantLock());
                }
            });
            return map;
        }
        catch( IOException e )
        {
            e.printStackTrace();
            return new TreeMap<>();
        }

    }

    /**
     * Verifies if the file has the same extension as the one put
     *
     * @param file The file that will be checked
     * @param extension The extension that will be checked
     * @return True if the file has the same extension as the one put, false otherwise
     */
    private boolean hasSameExtension(File file, String extension){

        // Get the file name
        String fileName = file.getName();

        // Get the file extension
        String fileExtension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) { // Ensure the dot is not the first character
            fileExtension = fileName.substring(dotIndex + 1);
        }
        return fileExtension.equals(extension);

    }

}
