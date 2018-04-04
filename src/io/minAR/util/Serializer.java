package io.minAR.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.minAR.core.Node;
import io.minAR.core.NodeTree;

import java.io.*;

/**
 * A utility class, mainly helps in the serialization of object (self-explanatory)
 * @see Kryo
 * @author nikhil
 * @since 0.0.1
 */
public class Serializer {

    private static Kryo kryo;
    static {
        kryo = new Kryo();
        kryo.register(Node.class, 2);
        kryo.register(NodeTree.class, 1);
        // Register Default classes here....
    }

    /**
     * Serialize the given object and writes it into the output stream
     * @param os the output stream that will contain the data of the object once serialized.
     * @param obj the object to be serialized
     */
    public static void serialize(OutputStream os, Object obj){
        Output output = new Output(os);
        kryo.writeObject(output, obj);
        output.close();
    }

    /**
     * Serialize the object into <code>byte[]</code>
     * @param obj the object to be
     * @return a byte array wrapping the object
     */
    public static byte[] serialize(Object obj){
        byte[] ret = null;
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            serialize(baos,obj);
            ret = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Deserializes the object in the input stream
     * @param is the input stream containing the object after serialization
     * @param clazz the class of the object
     * @param <T>
     * @return the object
     */
    public static <T> T deserialize(InputStream is, Class<T> clazz){
        Input input = new Input(is);
        T ret = kryo.readObject(input, clazz);
        input.close();
        return ret;
    }

    /**
     * Deserializes the object in the <code>byte[]</code>
     * @param data the serialized object
     * @param clazz the class of the object
     * @param <T>
     * @return the object
     */
    public static <T> T deserialize(byte[] data, Class<T> clazz){
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        T ret = deserialize(byteArrayInputStream, clazz);
        try {
            byteArrayInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Deserializes the object contained in the file
     * @param file the file which contains the object
     * @param clazz the class of the object
     * @param <T>
     * @return the object
     */
    public static <T> T deserialize(String file, Class<T> clazz){
        T ret = null;
        try(FileInputStream fileInputStream = new FileInputStream(file)){
            ret = deserialize(fileInputStream, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * Serialized the object and outputs it into the file
     * @param file the file to contain the object once serialized
     * @param obj the object to be serialized
     */
    public static void serialize(String file, Object obj){
        try(FileOutputStream fileOutputStream = new FileOutputStream(file)){
            serialize(fileOutputStream, obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
