package io.minAR.core;

import com.esotericsoftware.minlog.Log;
import io.minAR.util.Compressor;
import io.minAR.util.Crypt;
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
    public void analyze(boolean compressed, boolean ENCRYPTED, boolean HASH, SecretKey secretKey){
        try {
            byte[] serialized_data = null;
            byte[] apparent_data = Files.readAllBytes(ar_file);
            if(HASH){
                Analyzer.PairOfDisjointBytes bytes;
                if(ENCRYPTED){
                    byte[] dec_apparent_data = Crypt.decrypt(apparent_data, secretKey);
                    bytes = Analyzer.split(64, dec_apparent_data);
                } else {
                    bytes = Analyzer.split(64, apparent_data);
                }
                if(Hash.checkHash(bytes.bytes1, bytes.bytes2)){
                    serialized_data = bytes.bytes2;
                } else {
                    throw new MalformedDataException(ar_file);
                }
            } else {
                if(ENCRYPTED){  // When apparent data is encrypted
                    serialized_data = Crypt.decrypt(apparent_data, secretKey);
                } else {    // When apparent data is unencrypted
                    serialized_data = apparent_data;
                }
            }
            // Build the file tree from the serialized data
            NodeTree<File> filetree = Serializer.deserialize(serialized_data, NodeTree.class);
            analyzer = Analyzer.instance(filetree, compressed, ENCRYPTED, HASH);
        } catch (IOException e) {
            Log.error(TAG, "io_error", e);
        }
    }

    public void analyze(boolean COMPRESSED, boolean ENCRYPTED, boolean HASH, SecretKey secretKey, String format){
        format = format.replaceAll("\\$\\{FILE_NAME}", ar_file.toString());
        int x = 0; File file = new File(format.replaceAll("\\$\\{PART_NUM}", String.valueOf(x)));
        byte[] apparent = new byte[0];
        while(file.exists()){
            apparent = Analyzer.merge(apparent, Analyzer.FileToBytes(file));
            x++;
            file = new File(format.replaceAll("\\$\\{PART_NUM}", String.valueOf(x)));
        }
        byte[] serialized;
        if(HASH){
            Analyzer.PairOfDisjointBytes bytes;
            if(ENCRYPTED){
                byte[] dec_apparent_data = Crypt.decrypt(apparent, secretKey);
                bytes = Analyzer.split(64, dec_apparent_data);
            } else {
                bytes = Analyzer.split(64, apparent);
            }
            if(Hash.checkHash(bytes.bytes1, bytes.bytes2)){
                serialized = bytes.bytes2;
            } else {
                throw new MalformedDataException(ar_file);
            }
        } else {
            if(ENCRYPTED){
                serialized = Crypt.decrypt(apparent, secretKey);
            } else {
                serialized = apparent;
            }
        }

        // Build the file tree from the serialized data
        NodeTree<File> filetree = Serializer.deserialize(serialized, NodeTree.class);
        analyzer = Analyzer.instance(filetree, COMPRESSED, ENCRYPTED, HASH);
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

    private class MalformedDataException extends RuntimeException {
        String ar_file;
        public MalformedDataException(Path path) {
            ar_file = path.toString();
        }

        @Override
        public String getMessage() {
            return "The file " + ar_file + " is damaged; identical data absent.... [ERR: Hashes do not match]";
        }
    }
}
