import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;


/**
 * Initialises the ConcurrenLinkedQueue with the paths of the files 
 */
public class LockInitializer {
    private final ConcurrentLinkedQueue<String> list = new ConcurrentLinkedQueue<>();
    private final String extension;


    /**
     * Constructor for the LockInitialiser class
     * @param extension The extension that will be checked
     */
    public LockInitializer( String extension ) {
        this.extension = extension;
    }

    /**
     * Creates the ConcurrentLinkedQueue with the paths of the files that has the extension givem
     *
     * @param directoryPath The path of the directory that will be checked
     * @return The ConcurrentLinkedQueue with the paths of the files that has the extension given
     */
    public ConcurrentLinkedQueue<String> createConcurrentQueue( String directoryPath ) {
        Path dir = Paths.get( directoryPath );

        try( Stream<Path> stream = Files.walk( dir ) ){
            stream.forEach( path -> {
                if( !Files.isDirectory( ( path ) ) && hasSameExtension( path.toFile(), extension ) ){
                    list.add( path.toString() );
                }
            });
            return list;
        }
        catch( IOException e )
        {
            e.printStackTrace();
            return new ConcurrentLinkedQueue<>();
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
