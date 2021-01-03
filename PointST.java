/* *****************************************************************************
 *  Description: This class implements the mutable data type PointST, which uses
 *  the RedBlackBST class to represent a symbol table whose keys are Points2D
 *  (2-dimensional points).
 **************************************************************************** */

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.princeton.cs.algs4.StdIn;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.StdRandom;

public class PointST<Value> {

    private final RedBlackBST<Point2D, Value> pts; // symbol table of 2D points

    // construct an empty symbol table of points
    public PointST() {
        pts = new RedBlackBST<Point2D, Value>();
    }

    // is the symbol table empty?
    public boolean isEmpty() {
        return pts.isEmpty();
    }

    // number of points
    public int size() {
        return pts.size();
    }

    // associate the value val with point p
    public void put(Point2D p, Value val) {
        if (p == null || val == null)
            throw new IllegalArgumentException("arguments cannot be null");
        pts.put(p, val);
    }

    // value associated with point p
    public Value get(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException("argument cannot be null");
        return pts.get(p);
    }

    // does the symbol table contain point p?
    public boolean contains(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException("argument cannot be null");
        return pts.contains(p);
    }

    // all points in the symbol table
    public Iterable<Point2D> points() {
        return pts.keys();
    }

    // all points that are inside the rectangle (or on the boundary)
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null)
            throw new IllegalArgumentException("argument cannot be null");

        // enqueue points within/on the rectangle onto a queue using the
        // contains() method supported by RectHV
        Queue<Point2D> ptsInRange = new Queue<Point2D>();
        for (Point2D pt : this.points()) {
            if (rect.contains(pt))
                ptsInRange.enqueue(pt);
        }
        return ptsInRange;
    }

    // a nearest neighbor of point p; null if the symbol table is empty
    public Point2D nearest(Point2D p) {
        if (p == null)
            throw new IllegalArgumentException("argument cannot be null");
        if (pts.isEmpty())
            return null;

        // iterate through all points, calculating distance to p and updating
        // minimum distance found (as well as the point that gave this min)
        double min = Double.POSITIVE_INFINITY;
        Point2D closest = null;

        for (Point2D pt : this.points()) {
            double dist = p.distanceSquaredTo(pt);
            if (dist < min) {
                min = dist;
                closest = pt;
            }
        }
        return closest;
    }

    // unit testing
    public static void main(String[] args) {
        PointST<Double> st = new PointST<Double>();

        // read in pairs of coordinates from StdIn and add to the symbol table
        double val = 0.0; // initialize non-null values to associate with keys
        while (!StdIn.isEmpty()) {
            double x = StdIn.readDouble();
            double y = StdIn.readDouble();
            Point2D pt = new Point2D(x, y);
            st.put(pt, val++); // val will increment by 1.0 per pair of coords
        }

        // print out all points
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
        // int m = 100;
        // Stopwatch timer = new Stopwatch();
        // for (int i = 0; i < m; i++) {
        //     Point2D rand = new Point2D(StdRandom.uniform(0.0, 1.0),
        //                                StdRandom.uniform(0.0, 1.0));
        //     st.nearest(rand);
        // }
        // StdOut.println(timer.elapsedTime());
    }
    
}
