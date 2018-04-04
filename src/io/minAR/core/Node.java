package io.minAR.core;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author nikhil
 * @since 0.0.1
 */
public class Node<T> {
    protected ArrayList<Node<T>> sub_nodes;
    protected transient T user_data;

    public void setData(byte[] data) {
        this.data = data;
    }

    protected byte[] data;

    public String path;

    /**
     * Getter for the <code>data</code> field
     * @return bytes - data
     */
    public byte[] getData() {
        return data;
    }

    public T getUser_data(){
        return user_data;
    }

    /**
     * Sets the sub nodes of this node, only if it has elements, i.e, it has a size greater than 0.
     * @param sub_nodes the Array List of sub nodes
     */
    public void setSub_nodes(ArrayList<Node<T>> sub_nodes){
        if(sub_nodes.size() <= 0) return;
        this.sub_nodes = sub_nodes;
    }

    /**
     * Adds the node(s) to the node as sub node(s)
     * @param nodes the nodes to be added
     */
    public void setSubNodes(Node<T>... nodes){
        if(sub_nodes == null) sub_nodes = new ArrayList<>();
        sub_nodes.addAll(Arrays.asList(nodes));
    }

    @Override
    public String toString() {
        return "[u_data:" + GET_UDATA() + " path:" + path + "] \n" + "(data; " + Arrays.toString(data) + ")";
    }

    private String GET_UDATA(){
        if(user_data != null) return user_data.toString();
        else return "N/A";
    }

    /**
     * Create a new un-attached node
     * @param data The data to be contained in the node
     * @param user_data The user data to be contained in the node
     * @param path The internal path of this node. Every node has a path that uniquely identifies the node, as though it was in a vfs
     * @param subnodes The sub-nodes of this node
     * @param <T>
     * @return The un-attached node
     */
    public static <T> Node<T> newNode(byte[] data, T user_data,String path, Node<T>... subnodes) {
        Node<T> ret = new Node<>();
        ret.data = data;
        ret.user_data = user_data;
        ret.path = path;
        if(subnodes != null){
            ret.sub_nodes = new ArrayList<>();
            ret.sub_nodes.addAll(Arrays.asList(subnodes));
        }
        return ret;
    }
}
