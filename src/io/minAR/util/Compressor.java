package io.minAR.util;

import org.tukaani.xz.LZMA2Options;
import org.tukaani.xz.XZInputStream;
import org.tukaani.xz.XZOutputStream;

import java.io.*;

/**
 * A utilities class, mainly helps in xz compress
 * @author nikhil
 * @see LZMA2Options
 * @see XZInputStream
 * @see XZOutputStream
 * @since 0.0.1
 */
public class Compressor {

    static LZMA2Options lzma2Options = new LZMA2Options();

    /**
     * Compress the contents of the {@link InputStream}
     * @param inputStream
     * @return the compressed contents as <code>byte[]</code>
     */
    public static byte[] compress(InputStream inputStream){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xffff];

        try {
            XZOutputStream outputStream = new XZOutputStream(byteArrayOutputStream, lzma2Options);
            for(int len = inputStream.read(buffer); len != -1; len = inputStream.read(buffer)){
                outputStream.write(buffer, 0, len);
            }
            outputStream.close();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Decompress the contents of the {@link InputStream}
     * @param compressed the {@link InputStream} containing the compressed data
     * @return the decompressed contents as <code>byte[]</code>
     */
    public static byte[] decompress(InputStream compressed){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xffff];
        try {
            XZInputStream xzInputStream = new XZInputStream(compressed);
            for(int len = xzInputStream.read(buffer); len != -1; len = xzInputStream.read(buffer)){
                byteArrayOutputStream.write(buffer, 0, len);
            }
            xzInputStream.close();
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Compress a file
     * @param f the name of the file to be compressed
     * @return the contents of the file after compression
     */
    public static byte[] compress(String f){
        byte[] ret = null;
        try {
            File file = new File(f);
            FileInputStream fileInputStream = new FileInputStream(file);
            ret = compress(fileInputStream);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not find " + f);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Decompress a file
     * @param f the path to the file to be decompressed
     * @return the contents of the file after decompression
     */
    public static byte[] decompress(String f){
        byte[] ret = null;
        try {
            File file = new File(f);
            FileInputStream fileInputStream = new FileInputStream(file);
            ret = decompress(fileInputStream);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not find " + f + " :(");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Compress a file
     * @param file the file to be compressed
     * @return the compressed contents of the file
     */
    public static byte[] compress(File file){
        byte[] ret = null;
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            ret = compress(fileInputStream);
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            System.err.println("Could not find " + FILE_GET_PATH(file));
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private static String FILE_GET_PATH(File file){
        String path = "";
        try {
            path = file.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

}
