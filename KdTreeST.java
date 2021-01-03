/* *****************************************************************************
 *
 *  Description: This class implements a symbol table whose keys are Points2D
 *  (2-dimensional points), using a 2d generalization of a binary search tree.
 *
 *  @citation Adapted from: https://algs4.cs.princeton.edu/32bst/BST.java.html.
 *  Accessed 10/5/2020.
 **************************************************************************** */

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

public class KdTreeST<Value> {

    private Node root; // root node of BST
    private int size; // num of node

    // nested Node class used to represent BST
    private class Node {
        private final Point2D p; // key
        private Value val; // value
        private final boolean vert; // are node's descendants compared to x-coords?

        private Node left, right; // left and right subtrees
        private final RectHV rect; // corresponding axis-aligned rectangle

        // construct a node with a Point2D as key, an associated value, a
        // vertical or horizontal "divider" indicating whether the node's
        // children should be compared against its x- or y-coords, and an axis-
        // aligned "bounding box"
        public Node(Point2D p, Value val, boolean vert, RectHV rect) {
            this.p = p;
            this.val = val;
            this.vert = vert;
            this.rect = rect;
        }
    }

    // construct an empty symbol table of points
    public KdTreeST() {
        root = null;
        size = 0;
    }

    // is the symbol table empty?
    public boolean isEmpty() {
        return size() == 0; // true if no nodes in ST
    }

    // number of points
    public int size() {
        return this.size;
    }

    // associate the value val with point p
    public void put(Point2D p, Value val) {
        if (p == null || val == null)
            throw new IllegalArgumentException("arguments cannot be null");

        // the root always divides its children vertically (by x-coords) and
        // has no parent
        root = put(root, p, val, true, null);
    }

    // private helper method for put(): start at the root to locate point p
    // on the tree; keep track of the parent node of the node containing p in
    // order to calculate its bounding box
    private Node put(Node node, Point2D p, Value val, boolean vert,
                     Node parent) {
        if (node == null) { // add a new node once reach end of branch
            // use the helper method boundingBox() to calculate new node's
            // axis-aligned rectangle
            RectHV rect = boundingBox(parent, p);
            size++; // increment num of points in ST
            return new Node(p, val, vert, rect);
        }

        if (p.equals(node.p)) { // if key already exists, override val
            node.val = val;
            return node;
        }

        // using the helper method compare(), compare the x- or y-coords of p
        // with that of points starting at the root: go left if p is smaller,
        // go right if p is equal or bigger; the orientation of the divider
        // alternates at each level
        int cmp = compare(node, p);
        if (cmp < 0) node.left = put(node.left, p, val, !node.vert, node);
        else node.right = put(node.right, p, val, !node.vert, node);

        return node;
    }

    // private helper point comparison method
    private int compare(Node node, Point2D p) {
        // compare x-coords if parent node has a vert divider
        if (node.vert) {
            return Double.compare(p.x(), node.p.x());
        }

        // otherwise compare y-coords
        return Double.compare(p.y(), node.p.y());
    }

    // private helper method to calculate axis-aligned rectangle
    private RectHV boundingBox(Node parent, Point2D p) {
        // if creating the root node, set bounding box to
        // [(-inf, -inf), (inf, inf)]
        if (parent == null) return new RectHV(Double.NEGATIVE_INFINITY,
                                              Double.NEGATIVE_INFINITY,
                                              Double.POSITIVE_INFINITY,
                                              Double.POSITIVE_INFINITY);

        // coordinates of bounding box are identical to that of parent except
        // for one: use compare() to figure out which dimension needs to update
        double xmin = parent.rect.xmin();
        double ymin = parent.rect.ymin();
        double xmax = parent.rect.xmax();
        double ymax = parent.rect.ymax();
        int cmp = compare(parent, p);

        if (parent.vert) {
            if (cmp < 0) xmax = parent.p.x(); // if p to the left of vert divider
            else xmin = parent.p.x(); // if p to the right or on vert divider
        }

        else {
            if (cmp < 0) ymax = parent.p.y(); // if p under horizontal divider
            else ymin = parent.p.y(); // if p above or on horizontal divider
        }

        return new RectHV(xmin, ymin, xmax, ymax);
    }

    // value associated with point p
    public Value get(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException("key cannot be null");

        return get(root, p);
    }

    // private helper method for get()
    private Value get(Node node, Point2D p) {
        if (node == null) return null; // if no such key in ST

        if (p.equals(node.p)) return node.val; // if found key, return val

        // using the helper compare() method, go left if p is smaller, go right
        // if p is bigger; if p is found, return the associated val
        int cmp = compare(node, p);
        if (cmp < 0) return get(node.left, p);
        else return get(node.right, p);
    }

    // does the symbol table contain point p?
    public boolean contains(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException("key cannot be null");

        return get(p) != null;
    }

    // all points in the symbol table, returned in level order
    public Iterable<Point2D> points() {
        // output queue of points
        Queue<Point2D> ptsAll = new Queue<Point2D>();
        // temp queue for storing nodes traversed in level-order
        Queue<Node> q = new Queue<Node>();

        if (root == null) return ptsAll; // if no points, return empty queue
        q.enqueue(root); // otherwise start from the root
        while (!q.isEmpty()) {
            Node current = q.dequeue(); // save the next node in q in current
            ptsAll.enqueue(current.p); // add its point to the output queue
            // get the left and right nodes of current node if they exist
            if (current.left != null) q.enqueue(current.left);
            if (current.right != null) q.enqueue(current.right);
        }
        return ptsAll;
    }

    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null)
            throw new IllegalArgumentException("argument cannot be null");

        // output queue of points
        Queue<Point2D> ptsInRange = new Queue<Point2D>();

        // apply the pruning method to search through tree and enqueue points in
        // rectangle
        prune(root, rect, ptsInRange);

        return ptsInRange;
    }

    // private helper method implementing the pruning rule
    private void prune(Node node, RectHV rect, Queue<Point2D> q) {
        if (node == null) return;

        if (rect.intersects(node.rect)) { // only search subtree if intersects
            if (rect.contains(node.p)) q.enqueue(node.p);
            prune(node.left, rect, q); // check left subtree
            prune(node.right, rect, q); // check right subtree
        }
    }

    // a nearest neighbor of point p; null if the symbol table is empty
    public Point2D nearest(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException("argument cannot be null");
        if (isEmpty()) return null;
        return nearest(root, p, root.p); // start at the root
    }

    // private helper method for nearest():
    private Point2D nearest(Node node, Point2D p, Point2D closest) {
        if (node == null) return closest;

        // return if bounding box not closer than closest so far
        if (node.rect.distanceSquaredTo(p) > closest.distanceSquaredTo(p))
            return closest;

        // update closest if node.p is closer to p than closest so far
        if (p.distanceSquaredTo(node.p) < p.distanceSquaredTo(closest))
            closest = node.p;


        int cmp = compare(node, p);
        if (cmp < 0) { // if p is left or below, go left then right
            closest = nearest(node.left, p, closest);
            closest = nearest(node.right, p, closest);
        }
        else { // else go right then left
            closest = nearest(node.right, p, closest);
            closest = nearest(node.left, p, closest);
        }
        return closest;
    }

    // unit testing
    public static void main(String[] args) {
        KdTreeST<Double> st = new KdTreeST<Double>();

        // read in pairs of coordinates from StdIn and add to the symbol table
        double val = 0.0; // initialize non-null values to associate with keys
        while (!StdIn.isEmpty()) {
            double x = StdIn.readDouble();
            double y = StdIn.readDouble();
            Point2D pt = new Point2D(x, y);
            st.put(pt, val++); // val will increment by 1.0 per pair of coords
        }
        // print out all points in level order
        StdOut.println(st.points());

        // get values associated with points: vals should correspond to the
        // order in which the coordinates are listed in the input file
        for (Point2D pt : st.points()) {
            double valOut = st.get(pt);
            StdOut.print(valOut + " ");
        }
        StdOut.println();

        // print out whether st is empty
        StdOut.println("st is empty: " + st.isEmpty());
        // print out num of points
        StdOut.println("# of points: " + st.size());

        // generate a random point p and check if the st contains p
        Point2D p = new Point2D(StdRandom.uniform(0.0, 1.0),
                                StdRandom.uniform(0.0, 1.0));
        StdOut.println("generating random point p: " + p);
        StdOut.println("st contains p: " + st.contains(p));
        // find nearest neighbor in st to p
        StdOut.println("nearest neighbor: " + st.nearest(p));

        // generate an axis-aligned rectangle rect and find all points in st
        // contained in rect
        RectHV rect = new RectHV(0.0, 0.0, 0.5, 0.5);
        StdOut.println("points in lower-left quadrant of unit square: "
                               + st.range(rect));

        // analysis of running time with input1M.txt: generate a random point in
        // the unit square and make a call to nearest, for m num of times;
        // measure the time this takes
        // int m = 1000000;
        // Stopwatch timer = new Stopwatch();
        // for (int i = 0; i < m; i++) {
        //     Point2D rand = new Point2D(StdRandom.uniform(0.0, 1.0),
        //                                StdRandom.uniform(0.0, 1.0));
        //     st.nearest(rand);
        // }
        // StdOut.println(timer.elapsedTime());
    }

}
