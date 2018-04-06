package io.minAR.core;

import com.esotericsoftware.minlog.Log;
import io.minAR.util.Compressor;
import io.minAR.util.Crypt;
import io.minAR.util.NeedsOptimisation;
import io.minAR.util.Serializer;

import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Core class, involved in retrieving and re-creating the internal file tree, and extraction of data from the nodes of the tree
 * @author nikhil
 * @since 0.0.1
 */
public class Extractor {

    /** The directory where the content of the archive will be extracted to */
    File dir;
    /** The {@link Path} to the archive file */
    Path ar_file;

    static final String TAG = Extractor.class.getSimpleName();
    Analyzer analyzer;

    /**
     * Sets up the directory for use, creates one if it doesn't exist
     * @param directory the path to the directory ({@link String})
     * @param ar_file the path to the archive ({@link String})
     */
    public Extractor(String directory, String ar_file){
        try {
            dir = new File(directory).getCanonicalFile();
            if(!dir.exists()) dir.mkdirs();
            this.ar_file = Paths.get(ar_file);
        } catch (IOException e) {
            Log.error(TAG, "io_error", e);
        }
    }

    /**
     * Analyzes the archive in order to re-create the file tree
     * @param compressed whether the archive was compressed or not
     * @param ENCRYPTED whether the archive is encrypted or not
     * @param secretKey if encrypted, the {@link SecretKey} to decrypt
     * @see Analyzer
     */
    @NeedsOptimisation(where = "Checks for compression and encryption do not go hand in hand. Unnecessary input streams")
    public void analyze(boolean compressed, boolean ENCRYPTED, SecretKey secretKey){
        try {
            byte[] serialized_data = null;
            if(ENCRYPTED){
                byte[] encrypted = Files.readAllBytes(ar_file);
                serialized_data = Crypt.decrypt(encrypted, secretKey);
            } else {
                serialized_data = Files.readAllBytes(ar_file);
            }
            // Build the file tree from the serialized data
            NodeTree<File> filetree = Serializer.deserialize(serialized_data, NodeTree.class);
            analyzer = Analyzer.instance(filetree, compressed, ENCRYPTED);
        } catch (IOException e) {
            Log.error(TAG, "io_error", e);
        }
    }

    /**
     * Generates the various files, directories contained in the archive after traversing through the re-created file tree
     */
    public void generate(){
        analyzer.filetree.setListener((node, max) -> {
            if(node.sub_nodes != null){
                mkdir(node.path);
            } else {
                try(FileOutputStream fos = new FileOutputStream(mkfile(node.path))) {
                    if(node.getData() != null){ // If for some reason the file contains no data
                        if (analyzer.COMPRESSED) {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(node.getData());
                            fos.write(Compressor.decompress(byteArrayInputStream));
                            byteArrayInputStream.close();
                        } else {
                            fos.write(node.data);
                        }
                    } else System.out.println(node.path);
                } catch (IOException e) {
                    Log.error(TAG, "io_error", e);
                }
            }
        });
        analyzer.filetree.traverse();
    }

    private void mkdir(String path){
        File f = new File(dir.getAbsolutePath() + File.separator + path);
        f.mkdir();
    }

    private File mkfile(String path){
        File file = null;
        try {
            file = new File(dir.getAbsolutePath() + File.separator + path);
            if(file.exists()) file.delete();
            file.createNewFile();
        } catch (IOException e) {
            Log.error(TAG, "io_error", e);
        }
        return file;
    }

}
