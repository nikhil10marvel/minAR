package io.minAR.core;

import com.esotericsoftware.minlog.Log;
import io.minAR.MinAR;
import io.minAR.util.Compressor;
import io.minAR.util.Crypt;
import io.minAR.util.DataUnits;
import io.minAR.util.Serializer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Core class, involved in the creation of internal file trees, reading of data from the files and outputing the archive
 * @author nikhil
 * @since 0.0.1
 */
public class Analyzer {

    private static final String TAG = "analyzer_minAR";

    /**
     * The apex of the file hierarchy, the directory whose contents are to be archived
     */
    transient File top_directory;
    final boolean COMPRESSED, ENCRYPTED, HASH;
    boolean FILE_SPAN;  private int partsize;
    private String format;
    private static final String EXT = ".mar";
    /** Temporary file tree */
    NodeTree<File> filetree;

    /**
     * Generates an internal file tree, from which is then filled with actual data of the files
     * Excludes empty directories
     * @param dir The directory whose contents are to be compressed
     * @param COMPRESSED whether the archive is compressed or not; xz compression applicable
     * @param ENCRYPTED whether the archive is encrypted or not; if so, then a random key is generated and will be notified.
     * @see Compressor
     * @see Crypt
     */
    public Analyzer(File dir, boolean COMPRESSED, boolean ENCRYPTED, boolean HASH){
        top_directory = dir;
        if(!top_directory.isDirectory()) throw new RuntimeException(dir + " is not a directory");
        this.COMPRESSED = COMPRESSED;
        this.ENCRYPTED = ENCRYPTED;
        this.HASH = HASH;
        filetree = new NodeTree<>(Node.newNode(null,top_directory, "/", createNodes(top_directory.listFiles())));
    }

    private Analyzer(NodeTree<File> nodeTree, boolean COMPRESSED, boolean ENCRYPTED, boolean HASH){
        this.filetree = nodeTree;
        this.COMPRESSED = COMPRESSED;
        this.ENCRYPTED = ENCRYPTED;
        this.HASH = HASH;
    }

    private Node<File>[] createNodes(File... sub_files){
        Node<File>[] ret = new Node[sub_files.length];
        int x = 0;
        for(File sub_file : sub_files){
            String internal = sub_file.getAbsolutePath().replace(top_directory.getAbsolutePath(), "");
            if(sub_file.isDirectory()) ret[x] = Node.newNode(null,sub_file, internal, createNodes(sub_file.listFiles()));
            else { ret[x] = Node.newNode(null,sub_file, internal, null); }
            x++;
        }
        return ret;
    }

    /**
     * A test function to traverse through and print all the nodes by calling <node>{@link Node}.toString()</code>
     */
    protected void test(){
        filetree.nextLevel(filetree.current_sel); // Change to sub nodes of terminalNode
        printSubNodes(filetree.current_nodes);
    }

    /**
     * Traverse through the internal file tree and fill in the actual data in the corresponding files.
     */
    public void convertToDataTree(){
        filetree.setListener((node, max) -> {
            File file = node.getUser_data();
            if(!file.isDirectory()) {
                if(!COMPRESSED) node.setData(FileToBytes(file));
                else node.setData(Compressor.compress(file));
            }
            Log.debug(getClass().getCanonicalName(),node.toString());
        });
        filetree.traverse();
    }

    static byte[] merge(byte[] data1, byte[] data2){
        byte[] ret = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, ret, 0, data1.length);
        System.arraycopy(data2, 0, ret, data1.length, data2.length);
        return ret;
    }

     static class PairOfDisjointBytes {
        final byte[] bytes1;
        final byte[] bytes2;

        private PairOfDisjointBytes(byte[] bytes1, byte[] bytes2){
            this.bytes1 = bytes1;
            this.bytes2 = bytes2;
        }

        protected static PairOfDisjointBytes instance(byte[] bytes1, byte[] bytes2){
            return new PairOfDisjointBytes(bytes1, bytes2);
        }
    }

    static PairOfDisjointBytes split(int length_of_data1, byte[] data){
        if(length_of_data1 > data.length) throw new IllegalArgumentException("Length of constituent greater than sum... " + length_of_data1 + " > " + data.length);
        byte[] c1 = Arrays.copyOfRange(data, 0, length_of_data1);
        byte[] c2 = Arrays.copyOfRange(data, length_of_data1, data.length);
        return PairOfDisjointBytes.instance(c1, c2);
    }

    /**
     * Finally, output the archive onto the file.
     * The file tree is serialized into bytes, encrypted and written into the file
     *
     * Note: As safe this method is, it should not be used in case file spanning is enabled.
     *       This method does not support file spanning, however, if file spanning is disabled this is an efficient and optimized alternative.
     *       Usually this switch is handled by the <code>finish(...)</code> method.
     *
     * @param file the archive file
     * @see Serializer
     * @see Crypt
     */
    public void OUTPUT_minAR(String file){
        file += EXT;
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)){
            if(ENCRYPTED) {
                Crypt.ENC_OBJECT enc_object;
                if(HASH) {
                    byte[] filetree_bytes = Serializer.serialize(filetree);
                    byte[] sha512 = Hash.hash(filetree_bytes);
                    enc_object = Crypt.encrypt(merge(sha512, filetree_bytes));
                } else {
                    enc_object = Crypt.encrypt(Serializer.serialize(filetree));
                }
                fileOutputStream.write(enc_object.getRaw());
                writeToFile(file.replaceAll(EXT, "_mar") + "_secret.key", enc_object.secretKey().getEncoded());
                System.out.println("Your key: " + Crypt.keyAsString(enc_object.secretKey()) + " " +Arrays.toString(enc_object.secretKey().getEncoded()));
            } else {
                if(HASH){
                    byte[] filetree_bytes = Serializer.serialize(filetree);
                    byte[] sha512 = Hash.hash(filetree_bytes);
                    fileOutputStream.write(merge(sha512, filetree_bytes));
                }else {
                    Serializer.serialize(fileOutputStream, filetree);
                }
            }
        } catch (FileNotFoundException e) {
            Log.error(TAG,"File NOT FOUND!", e);
        } catch (IOException e) {
            Log.error(TAG, e.getMessage(), e);
        }
    }

    /**
     * A helper method for outputting byte arrays to files.
     * @param file
     * @param data
     */
    public static void writeToFile(String file, byte[] data){
        try {
            Files.write(Paths.get(file), data);
        } catch (IOException e) {
            Log.error(MinAR.class.getCanonicalName(), e);
        }
    }

    /**
     * Finally output the archive into the file(s). Very similar to {@link Analyzer}.OUTPUT_minAR(...) method.
     * The file tree is serialized into bytes, encrypted and written into the file
     *
     * Note: This method supports file spanning as is recommended against OUTPUT_minAR(...), but is not as optimized as the OUTPUT_minAR(...) method.
     *       Thus, for optimal functioning, using the finish(...) method is recommended which switches to OUTPUT_minAR(...) in case file spanning is not enabled.
     *
     * @param file the name of the file(s) to output the archive into.
     */
    public void create_minAR(String file){
        file += EXT;
        byte[] serialized = null;
        if(ENCRYPTED){
            Crypt.ENC_OBJECT enc_object;
            if(HASH){
                byte[] file_tree_Bytes = Serializer.serialize(filetree);
                byte[] sha512 = Hash.hash(file_tree_Bytes);
                enc_object = Crypt.encrypt(merge(sha512, file_tree_Bytes));
            } else {
                enc_object = Crypt.encrypt(Serializer.serialize(filetree));
            }
            writeToFile(file.replaceAll(EXT, "_mar") + "_secret.key", enc_object.secretKey().getEncoded());
            System.out.println("Your key: " + Crypt.keyAsString(enc_object.secretKey()) + " " +Arrays.toString(enc_object.secretKey().getEncoded()));
            serialized = enc_object.getRaw();
        } else {
            if(HASH){
                byte[] file_tree_bytes = Serializer.serialize(filetree);
                byte[] sha512 = Hash.hash(file_tree_bytes);
                serialized = merge(sha512, file_tree_bytes);
            } else {
                serialized = Serializer.serialize(filetree);
            }
        }
        if(FILE_SPAN){
            DataUnits.writeParts(partsize, serialized, format, file);
        } else {
            writeToFile(file, serialized);
        }
    }

    private void printSubNodes(ArrayList<Node<File>> subnodes){
        if(subnodes == null) return;
        subnodes.forEach((node) -> {
            System.out.println(node);
            if(node.sub_nodes != null){
                printSubNodes(node.sub_nodes);
            }
        });
    }

    /**
     * Finish the archiving process. Outputs the archive into file(s).
     * See OUTPUT_minAR(...) and create_minAR(...)
     * @param file The file name for the archive(s)
     */
    public void finish(String file){
        if(FILE_SPAN){
            create_minAR(file);
        } else {
            OUTPUT_minAR(file);
        }
    }

    static byte[] FileToBytes(File file){
        byte[] ret = null;
        try {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            byte[] buffer = new byte[0xffff];
            for(int r = inputStream.read(buffer); r != -1; r = inputStream.read(buffer)){
                byteArrayOutputStream.write(buffer, 0, r);
            }
            ret = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            System.err.println(file + " Could not be found! [404... :( ] " + e.getMessage());
        } catch (IOException e) {
            Log.error(TAG, "io_error", e);
        }
        return ret;
    }

    public boolean isFILE_SPAN() {
        return FILE_SPAN;
    }

    public void enable_file_spanning(int partsize, String format) {
        this.FILE_SPAN = true;
        this.partsize = partsize;
        this.format = format;
    }

    public void disable_file_spanning(){
        this.FILE_SPAN = false;
    }

    /**
     * A reserved initializer, for internal purpose only.
     * @see Extractor
     */
    protected static Analyzer instance(NodeTree<File> filetree, boolean COMPRESSED, boolean ENCRYPTED, boolean HASH){
        return new Analyzer(filetree, COMPRESSED, ENCRYPTED, HASH);
    }

    public static void main(String[] args){
        MinAR.toggleFlags(MinAR._default_enc_file);
//        MinAR.outputArchive(".", "./backup");
        MinAR.extractArchive("./backup.mar", "./test_D", "./backup_mar_secret");
    }

}
