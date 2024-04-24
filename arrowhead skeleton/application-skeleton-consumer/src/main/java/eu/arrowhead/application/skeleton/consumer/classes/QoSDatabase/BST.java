package eu.arrowhead.application.skeleton.consumer.classes.QoSDatabase;

import java.util.*;


public class BST<E extends Comparable<E>> implements BSTInterface<E> {


    /**
     * Nested static class for a binary search tree node.
     */

    protected static class Node<E> {
        private E element;          // an element stored at this node
        private Node<E> left;       // a reference to the left child (if any)
        private Node<E> right;      // a reference to the right child (if any)

        /**
         * Constructs a node with the given element and neighbors.
         *
         * @param e          the element to be stored
         * @param leftChild  reference to a left child node
         * @param rightChild reference to a right child node
         */
        public Node(E e, Node<E> leftChild, Node<E> rightChild) {
            element = e;
            left = leftChild;
            right = rightChild;
        }

        // accessor methods
        public E getElement() {
            return element;
        }

        public Node<E> getLeft() {
            return left;
        }

        public Node<E> getRight() {
            return right;
        }

        // update methods
        public void setElement(E e) {
            element = e;
        }

        public void setLeft(Node<E> leftChild) {
            left = leftChild;
        }

        public void setRight(Node<E> rightChild) {
            right = rightChild;
        }
    }

    //----------- end of nested Node class -----------

    public Node<E> root;     // root of the tree

    protected int size;


    /**
     * Constructs an empty binary search tree.
     */
    public BST() {
        root = null;
        size = 0;
    }

    /**
     * @return root Node of the tree (or null if tree is empty)
     */
    public Node<E> root() {
        return root;
    }

    /**
     * Verifies if the tree is empty
     *
     * @return true if the tree is empty, false otherwise
     */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * Returns the Node containing a specific Element, or null otherwise.
     *
     * @param element the element to find
     * @return the Node that contains the Element, or null otherwise
     * <p>
     * This method despite not being essential is very useful.
     * It is written here in order to be used by this class and its
     * subclasses avoiding recoding.
     * So its access level is protected
     */

    public E find(E element){
        if(element == null)
            return null;
        Node<E> n = find(root, element);
        if(n == null)
            return null;
        return n.getElement();
    }
    private Node<E> find(Node<E> node, E element) {

        if (node == null)
            return null;

        int comp = node.getElement().compareTo(element);

        if (comp == 0)
            return node;
        if (comp > 0)
            return find(node.getLeft(), element);
        else return find(node.getRight(), element);
    }

    /**
     * Inserts an element in the tree.
     */
    public void insert(E element) {
        if(element == null)
            return;
        root = insert(element, root);
        size++;
    }

    private Node<E> insert(E element, Node<E> node) {

        if (node == null)
            return new Node<>(element, null, null);

        int comp = element.compareTo(node.getElement());

        if (comp == 0)
            node.setElement(element);

        if (comp > 0)
            node.setRight(insert(element, node.getRight()));
        if (comp < 0)
            node.setLeft(insert(element, node.getLeft()));

        return node;
    }

    /**
     * Removes an element from the tree maintaining its consistency as a Binary Search Tree.
     */
    public void remove(E element) {
        if(element == null)
            return;
        root = remove(element, root());
        size--;
    }

    private Node<E> remove(E element, Node<E> node) {

        if (node == null)
            return null;

        if (element.compareTo(node.getElement()) == 0) {
            return removeNodeSet(node);
        } else if (element.compareTo(node.getElement()) < 0)
            node.setLeft(remove(element, node.getLeft()));
        else
            node.setRight(remove(element, node.getRight()));

        return node;
    }

    protected Node<E> removeNodeSet(Node<E> node){
        // node is the Node to be removed
        if (node.getLeft() == null && node.getRight() == null) //node is a leaf (has no childs)
            return null;

        if (node.getLeft() == null)  //has only right child
            return node.getRight();

        if (node.getRight() == null) //has only left child
            return node.getLeft();

        E min = smallestElement(node.getRight());
        node.setElement(min);
        node.setRight(remove(min, node.getRight()));
        return node;
    }

    /**
     * Returns the number of nodes in the tree.
     *
     * @return number of nodes in the tree
     */
    public int size() {
        return size;
    }

    /**
     * Returns the height of the tree
     *
     * @return height
     */
    public int height() {
        return height(root) - 1;
    }

    /**
     * Returns the height of the subtree rooted at Node node.
     *
     * @param node A valid Node within the tree
     * @return height
     */
    protected int height(Node<E> node) {

        if (node == null) return 0;

        if (node.getLeft() == null && node.getRight() == null) return 1;

        return 1 + Math.max(height(node.getLeft()), height(node.getRight()));

    }

    /**
     * Returns the smallest element within the tree.
     *
     * @return the smallest element within the tree
     */
    public E smallestElement() {
        return smallestElement(root);
    }

    protected E smallestElement(Node<E> node) {
        if (node.getLeft() == null)
            return node.getElement();
        return smallestElement(node.getLeft());
    }

    @SuppressWarnings("Shut up")
    public E biggestElement() {
        return biggestElement(root);
    }

    protected E biggestElement(Node<E> node) {
        if (node.getRight() == null)
            return node.getElement();
        return biggestElement(node.getRight());
    }


    /**
     * Returns an iterable collection of elements of the tree, reported in in-order.
     *
     * @return iterable collection of the tree's elements reported in in-order
     */
    public Iterable<E> inOrder() {
        List<E> snapshot = new ArrayList<>();
        inOrderSubtree(root, snapshot);   // fill the snapshot recursively
        return snapshot;
    }

    /**
     * Adds elements of the subtree rooted at Node node to the given
     * snapshot using an in-order traversal
     *
     * @param node     Node serving as the root of a subtree
     * @param snapshot a list to which results are appended
     */
    private void inOrderSubtree(Node<E> node, List<E> snapshot) {
        if (node == null)
            return;
        inOrderSubtree(node.getLeft(), snapshot);
        snapshot.add(node.getElement());
        inOrderSubtree(node.getRight(), snapshot);
    }

    /**
     * Returns an iterable collection of elements of the tree, reported in pre-order.
     *
     * @return iterable collection of the tree's elements reported in pre-order
     */
    public Iterable<E> preOrder() {
        List<E> snapshot = new ArrayList<>();
        preOrderSubtree(root, snapshot);
        return snapshot;
    }

    /**
     * Adds elements of the subtree rooted at Node node to the given
     * snapshot using a pre-order traversal
     *
     * @param node     Node serving as the root of a subtree
     * @param snapshot a list to which results are appended
     */
    private void preOrderSubtree(Node<E> node, List<E> snapshot) {

        if (node == null)
            return;
        snapshot.add(node.getElement());
        preOrderSubtree(node.getLeft(), snapshot);
        preOrderSubtree(node.getRight(), snapshot);

    }

    /**
     * Returns an iterable collection of elements of the tree, reported in post-order.
     *
     * @return iterable collection of the tree's elements reported in post-order
     */
    public Iterable<E> posOrder() {
        List<E> snapshot = new ArrayList<>();
        posOrderSubtree(root, snapshot);
        return snapshot;
    }

    /**
     * Adds positions of the subtree rooted at Node node to the given
     * snapshot using an post-order traversal
     *
     * @param node     Node serving as the root of a subtree
     * @param snapshot a list to which results are appended
     */
    private void posOrderSubtree(Node<E> node, List<E> snapshot) {
        if (node == null)
            return;
        posOrderSubtree(node.getLeft(), snapshot);
        posOrderSubtree(node.getRight(), snapshot);
        snapshot.add(node.getElement());
    }

    public int[] numNodesByLevel(){
        int[] numNodesByLevel = new int[height()+1];
        numNodesByLevel(root, numNodesByLevel, 0);
        return numNodesByLevel;
    }

    private void numNodesByLevel(Node<E> node, int[] result, int level){
        if(node == null)
            return;
        else
            result[level] = result[level]+1;
        numNodesByLevel(node.getLeft(), result, level+1);
        numNodesByLevel(node.getRight(), result, level+1);
    }

    /**
     * Returns a map with a list of nodes by each tree level.
     *
     * @return a map with a list of nodes by each tree level
     */
    public Map<Integer, List<E>> nodesByLevel() {
        Map<Integer, List<E>> nodesBylevel = new HashMap<>();
        processBstByLevel(root, nodesBylevel, 0);
        return nodesBylevel;
    }

    private void processBstByLevel(Node<E> node, Map<Integer, List<E>> result, int level) {

        if (node == null)
            return;

        result.computeIfAbsent(level, k -> new ArrayList<>()).add(node.getElement());

        processBstByLevel(node.getLeft(), result, level + 1);
        processBstByLevel(node.getRight(), result, level + 1);

    }

    /**
     * This method calculates if the BST is perfect according to the following equation, 2^(n+1)-1 = number of nodes,
     * being n the height.
     * @return true if is perfect, false otherwise.
     */
    public boolean perfectBalanced(){
        return ((int) (Math.pow(2, height()+1)-1)) == size;
    }

    public E lowestCommonAncestor(E elem1, E elem2){
        Node<E> node;
        if(elem1 == null && elem2 == null)
            return null;

        node = lowestCommonAncestor(root, elem1, elem2);
        if(node == null)
            return null;
        return node.getElement();
    }

    private Node<E> lowestCommonAncestor(Node<E> node, E elem1, E elem2){
        if(node == null)
            return null;
        int comp1 = elem1.compareTo(node.getElement());
        int comp2 = elem2.compareTo(node.getElement());
        if(comp1 < 0 && comp2 > 0 || comp1 > 0 && comp2 < 0)
            return node;

        if(comp1 < 0)
            return lowestCommonAncestor(node.getLeft(), elem1, elem2);
        else
            return lowestCommonAncestor(node.getRight(), elem1, elem2);
    }

    public BST<E> autumnTree() {
        root = copyRec(root);
        BST<E> autumnTree = new BST<>();
        autumnTree.root = root;
        return autumnTree;
    }

    private Node<E> copyRec(Node<E> node){
        if(node == null)
            return null;
        if(node.getRight() == null && node.getLeft() == null)
            return null;
        node.setLeft(copyRec(node.getLeft()));
        node.setRight(copyRec(node.getRight()));
        return node;
    }

    public void buildTree(List<E> list) {
        for (E e : list)
            this.insert(e);
    }

    public List<List<E>> allPaths(){
        List<List<E>> allPaths = new ArrayList<>();
        allPaths(root, allPaths, new ArrayList<>());
        return allPaths;
    }

    private void allPaths(Node<E> node, List<List<E>> allPaths, List<E> path){

        if(node == null)
            return;

        path.add(node.getElement());

        if(node.getLeft() == null && node.getRight() == null){
            allPaths.add(new ArrayList<>(path));
            path.remove(path.size()-1);
            return;
        }
        allPaths(node.getLeft(), allPaths, path);
        allPaths(node.getRight(), allPaths, path);
        path.remove(path.size()-1);
    }

    public int minimumBranchesCost(E element1, E element2){
        if(element1 == null || element2 == null)
            return -1;

        List<E> snapshot1 = new ArrayList<>();
        List<E> snapshot2 = new ArrayList<>();

        minimumBranchesCost(root, element1, element2, snapshot1, snapshot2, false);

        return (snapshot1.size() - 1) + (snapshot2.size() - 1);
    }

    private void minimumBranchesCost(Node<E> node, E element1, E element2, List<E> list1, List<E> list2, boolean x){

        int comp1 = element1.compareTo(node.getElement());
        int comp2 = element2.compareTo(node.getElement());

        if(comp1 == 0) {
            list1.add(node.getElement());
            return;
        }

        if(comp2 == 0){
            list2.add(node.getElement());
            return;
        }

        if(compEquals(comp1, comp2) && !x){
            if(comp1 > 0)
                minimumBranchesCost(node.getRight(), element1, element2, list1, list2, false);
            else
                minimumBranchesCost(node.getLeft(), element1, element2, list1, list2, false);

        }else{
            if(comp1 > 0){
                list1.add(node.getElement());
                minimumBranchesCost(node.getRight(), element1, element2, list1, list2, true);
                if(!x) {
                    list2.add(node.getElement());
                    minimumBranchesCost(node.getLeft(), element1, element2, list1, list2, true);
                }
            }else{
                list1.add(node.getElement());
                minimumBranchesCost(node.getLeft(), element1, element2, list1, list2, true);
                if(!x) {
                    list2.add(node.getElement());
                    minimumBranchesCost(node.getRight(), element1, element2, list1, list2, true);
                }
            }

        }


    }

    private boolean compEquals(int comp1, int comp2){
        if(comp1 > 0 && comp2 > 0)
            return true;
        if(comp1 < 0 && comp2 < 0)
            return true;
        return false;
    }


//#########################################################################

    /**
     * Returns a string representation of the tree.
     * Draw the tree horizontally
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toStringRec(root, 0, sb);
        return sb.toString();
    }

    private void toStringRec(Node<E> root, int level, StringBuilder sb) {
        if (root == null)
            return;
        toStringRec(root.getRight(), level + 1, sb);
        if (level != 0) {
            sb.append("|\t".repeat(Math.max(0, level - 1)));
            sb.append("|-------").append(root.getElement()).append("\n");
        } else
            sb.append(root.getElement()).append("\n");
        toStringRec(root.getLeft(), level + 1, sb);
    }
}
