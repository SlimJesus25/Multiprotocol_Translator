package eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase;

import java.util.List;
import java.util.Map;

/**
 * @author : Ricardo Venâncio - 1210828
 **/
public class AVL <E extends Comparable<E>> extends BST<E> implements Cloneable{

    /**
     * Calculates the balance factor from this node below. The balance factor is the height difference between the left
     * subtree and the right subtree.
     * @param node to calculate the balance factor.
     * @return the difference between the left subtree height and the right subtree height.
     */
    private int balanceFactor(Node<E> node){
        return height(node.getRight()) - height(node.getLeft());
    }

    /**
     * Performs a right rotation. Whenever an insertion/remotion is applied, there is possiblity to mess up the AVL property,
     * so, if it needed, a right rotation is performed.
     * This method is called when:
     *  - The node has a balanceFactor > -1.
     *  - Two rotations calls it.
     * @param node to rotate.
     * @return left node (that is going to occupy the previous one).
     */
    private Node<E> rightRotation(Node<E> node){
        Node<E> leftSon = node.getLeft();

        node.setLeft(leftSon.getRight());
        leftSon.setRight(node);

        node = leftSon;

        return node;
    }

    private Node<E> leftRotation(Node<E> node){
        Node<E> rightSon = node.getRight();

        node.setRight(rightSon.getLeft());
        rightSon.setLeft(node);

        node = rightSon;

        return node;
    }

    private Node<E> twoRotations(Node<E> node){
        if(balanceFactor(node) < 0){
            node.setLeft(leftRotation(node.getLeft()));
            node = rightRotation(node);
        }else{
            node.setRight(rightRotation(node.getRight()));
            node = leftRotation(node);
        }
        return node;
    }

    private Node<E> balanceNode(Node<E> node) {
        int bf = balanceFactor(node);

        if(bf < -1){
            if(balanceFactor(node.getLeft()) + bf > bf)
                return twoRotations(node);
            else
                return rightRotation(node);
        }
        if(bf > 1){
            if(balanceFactor(node.getRight()) + bf < bf)
                return twoRotations(node);
            else
                return leftRotation(node);
        }
        return node;
    }

    @Override
    public void insert(E element){
        if(element == null)
            return;
        if(find(element) == null) {
            root = insert(element, root);
            size++;
        }
    }
    private Node<E> insert(E element, Node<E> node){

        if(node == null)
            return new Node<>(element, null, null);

        int comp = element.compareTo(node.getElement());

        if(comp == 0) node.setElement(element);

        if(comp < 0) node.setLeft(insert(element, node.getLeft()));
        else node.setRight(insert(element, node.getRight()));

        node = balanceNode(node);
        return node;
    }

    @Override
    public void remove(E element){
        if(element == null)
            return;
        if(find(element) != null) {
            root = remove(element, root());
            size--;
        }
    }

    private Node<E> remove(E element, Node<E> node) {

        if(node == null) return null;

        int comp = element.compareTo(node.getElement());

        if(comp == 0){
            return removeNodeSet(node);
        }else if(comp < 0){
            node.setLeft(remove(element, node.getLeft()));
            node = balanceNode(node);
        }else {
            node.setRight(remove(element, node.getRight()));
            node = balanceNode(node);
        }
        return node;
    }


    public boolean equals(Object otherObj) {

        if (this == otherObj)
            return true;

        if (otherObj == null || this.getClass() != otherObj.getClass())
            return false;

        AVL<E> second = (AVL<E>) otherObj;
        return equals(root, second.root);
    }

    public boolean equals(Node<E> root1, Node<E> root2) {
        if (root1 == null && root2 == null)
            return true;
        else if (root1 != null && root2 != null) {
            if (root1.getElement().compareTo(root2.getElement()) == 0) {
                return equals(root1.getLeft(), root2.getLeft())
                        && equals(root1.getRight(), root2.getRight());
            } else
                return false;
        }
        else return false;
    }

    public AVL<E> clone(){
        AVL<E> clone = new AVL<>();
        for(Map.Entry<Integer, List<E>> nodes : this.nodesByLevel().entrySet()){
            for(E element : nodes.getValue()){
                clone.insert(element);
            }
        }
        return clone;
    }

}
