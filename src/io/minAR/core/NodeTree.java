package io.minAR.core;

import java.util.ArrayList;

/**
 * A tree of {@link Node}
 * @author nikhil
 * @param <T> the param for the node.
 * @since 0.0.1
 */
public class NodeTree<T> {
    private transient NodeListener<T> listener;
    protected Node<T> terminalNode;
    transient int current_level = 0;
    transient int current_sel = 0;
    transient ArrayList<Node<T>> current_nodes;

    public NodeTree(Node<T> terminalNode){
        this.terminalNode = terminalNode;
    }

    /**
     * The no-arg constructor for Kryo
     * @see com.esotericsoftware.kryo.Kryo
     */
    private NodeTree(){}

    /**
     * Sets the current nodes as the sub nodes of the node in the specified slot
     * If the current level is 0, then the slot does not matter, and the current nodes are set to the sub nodes.
     * @param slot The slot of the node in the <code>current_nodes</code>
     */
    public void nextLevel(int slot){
        current_sel = 0;
        if(current_level == 0) {
            current_level ++;
            current_nodes = terminalNode.sub_nodes;
        }else {
            current_level ++;
            current_nodes = current_nodes.get(slot).sub_nodes;
        }
    }

    /**
     * Change the currently selected slot.
     * @param id
     */
    public void changeSelection(int id){
        current_sel = id;
    }

    /**
     * Get the currently selected node.
     * Uses <code>current_level</code> and <code>current_sel</code> to identify the node
     * @return The node
     */
    public Node<T> get(){
        if(current_level == 0){
            return terminalNode;
        } else {
            return current_nodes.get(current_sel);
        }
    }

    /**
     * Set the currently selected slot
     * @param node The node to be set in the slot
     */
    public void set(Node<T> node){
        current_nodes.set(current_sel, node);
    }

    /**
     * Append a node to the current node set
     * @param node
     * @throws  NodeAdditionException if the current level is 0, as no node can be added beside the apex node.
     */
    public void add(Node<T> node) throws NodeAdditionException {
        if(current_level == 0) throw new NodeAdditionException(terminalNode.toString());
        current_nodes.set(current_sel, node);
        current_sel++;
    }

    /**
     * Remove the node in currently selected slot
     */
    public void remove(){
        current_nodes.remove(current_sel);
        current_sel --;
    }

    /**
     * Changes the current slot and checks if the node in it has any sub nodes
     * @param slot The slot to change to
     * @return whether the node has sub nodes or not
     */
    public boolean checkSubNodes(int slot){
        this.current_sel = slot;
        return checkSubNodes();
    }

    /**
     * Checks whether the currently selected node has any sub nodes
     * @return whether the current node has sub nodes or not
     */
    public boolean checkSubNodes(){
        return get().sub_nodes != null;
    }

    public boolean checkSubNodesAll(){
        int initial_selection = current_sel;
        boolean ret = false;
        for(int x = 0; x < current_nodes.size(); x ++){
            if(checkSubNodes(x)) {
                ret = true;
                break;
            }
        }
        current_sel = initial_selection;
        return ret;

    }

    /**
     * The Listener is an interface with methods to be called on every node while traversing.
     * @return the current listener
     * @see NodeListener
     */
    public NodeListener<T> getListener() {
        return listener;
    }

    /**
     * Set the current {@link NodeListener} of this tree
     * @param listener the listener to be set
     */
    public void setListener(NodeListener<T> listener) {
        this.listener = listener;
    }

    /**
     * Recursively loop through - identify, and invoke methods from {@link NodeListener} onto nodes
     */
    public void traverse(){
        if(current_nodes == null) nextLevel(0);
        traverse(current_nodes, listener);
    }

    private void traverse(ArrayList<Node<T>> nodes, NodeListener<T> nodeListener){
        for(Node<T> node : nodes){
            nodeListener.listen(node, nodes.size());
            if(node.sub_nodes != null){
                traverse(node.sub_nodes, nodeListener);
            }
        }
    }

    /**
     * An Exception that occurs when a node is added beside the apex or terminalNode
     */
    private static class NodeAdditionException extends Throwable{
        String text;
        public NodeAdditionException(String text) {
            this.text = text;
        }

        @Override
        public String getMessage() {
            return text;
        }
    }


    /**
     * A simple interface, lambda compatible, contains methods invoked upon every node while traversing through the {@link NodeTree}
     * @param <T>
     * @see NodeTree
     */
    public interface NodeListener<T> {
        void listen(Node<T> node, int max_items_current_level);
    }
}
