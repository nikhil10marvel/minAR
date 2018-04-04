package io.minAR.core;

import com.esotericsoftware.minlog.Log;
import io.minAR.util.Compressor;
import io.minAR.util.Crypt;
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

    /**
     * The apex of the file hierarchy, the directory whose contents are to be archived
     */
    transient File top_directory;
    boolean COMPRESSED, ENCRYPTED;
    private static final String EXT = ".mar";
    /** Temporary file tree */
    NodeTree<File> filetree;

    /**
     * Generates an internal file tree, from which is then filled with actual data of the files
     * @param dir The directory whose contents are to be compressed
     * @param COMPRESSED whether the archive is compressed or not; xz compression applicable
     * @param ENCRYPTED whether the archive is encrypted or not; if so, then a random key is generated and will be notified.
     * @see Compressor
     * @see Crypt
     */
    public Analyzer(File dir, boolean COMPRESSED, boolean ENCRYPTED){
        top_directory = dir;
        if(!top_directory.isDirectory()) throw new RuntimeException(dir + " is not a directory");
        this.COMPRESSED = COMPRESSED;
        this.ENCRYPTED = ENCRYPTED;
        filetree = new NodeTree<File>(Node.newNode(null,top_directory, "/", createNodes(top_directory.listFiles())));
    }

    private Analyzer(NodeTree<File> nodeTree, boolean COMPRESSED, boolean ENCRYPTED){
        this.filetree = nodeTree;
        this.COMPRESSED = COMPRESSED;
        this.ENCRYPTED = ENCRYPTED;
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

    /**
     * Finally, output the archive onto the file.
     * The file tree is serialized into bytes, encrypted and written into the file
     * @param file the archive file
     * @see Serializer
     * @see Crypt
     */
    public void OUTPUT_minAR(String file){
        file += EXT;
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)){
            if(ENCRYPTED) {
                Crypt.ENC_OBJECT enc_object = Crypt.encrypt(Serializer.serialize(filetree));
                fileOutputStream.write(enc_object.getRaw());
                Files.write(Paths.get(file.replaceAll(EXT, "_mar") + "_secret.key"), enc_object.secretKey().getEncoded());
                System.out.println("Your key: " + Crypt.keyAsString(enc_object.secretKey()) + Arrays.toString(enc_object.secretKey().getEncoded()));
            } else {
                Serializer.serialize(fileOutputStream, filetree);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    private static byte[] FileToBytes(File file){
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
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * A reserved initializer, for internal purpose only.
     * @param filetree
     * @param COMPRESSED
     * @param ENCRYPTED
     * @return
     * @see Extractor
     */
    protected static Analyzer instance(NodeTree<File> filetree, boolean COMPRESSED, boolean ENCRYPTED){
        return new Analyzer(filetree, COMPRESSED, ENCRYPTED);
    }

    public static void main(String[] args){
//        Analyzer analyzer = new Analyzer(new File(System.getProperty("user.dir")), true, true);
//        analyzer.convertToDataTree();
//        analyzer.OUTPUT_minAR("backup");
        Extractor extractor = new Extractor("./test_D", "./backup.mar");
//        extractor.analyze(true, true, Crypt.getKeyFromFile("./backup_mar_secret"));
        extractor.analyze(true, true, Crypt.stringAsKey("rjTVmBCYFYA"));
        extractor.generate();
    }

}
