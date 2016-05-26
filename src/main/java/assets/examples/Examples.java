package assets.examples;

/**
 * Generates examples for a given array using the selected algorithm.<br>
 * <br>
 * <b>Sources:</b><br>
 * Insertion Sort: http://mathbits.com/MathBits/Java/arrays/InsertionSort.htm <br>
 * Bubble Sort: http://mathbits.com/MathBits/Java/arrays/Bubble.htm <br>
 * Merge Sort: http://algs4.cs.princeton.edu/14analysis/Mergesort.java.html <br>
 *
 * @author Richard Sundqvist
 *
 */
public abstract class Examples {

    private static double[] arg;
    public static String    json;

    /**
     * Run an algorithm.
     *
     * @param algo
     *            The algorithm to load.
     * @param data
     *            The data to run it on.
     * @return The resulting operations of running the given algorithm on the data.
     */
    public static String getExample (Algorithm algo, double[] data) {
        arg = data;
        switch (algo) {
        case mergesort:
            Merge.sort(arg);
            break;
        case bubblesort:
            Bubble.sort(arg);
            break;
        case insertionsort:
            Insertion.sort(arg);
            break;
        // case quicksort:
        // Quick.sort(arg);
        // break;
        default:
            System.err.println("No such algorithm: " + algo);
            break;
        }
        print();
        return json;
    }

    // @Print(path="")
    public static void print () {
    }

    /**
     * The algorithms known to this Examples thingy.
     *
     * @author Richard Sundqvist
     *
     */
    public enum Algorithm {
        mergesort("Merge Sort"), bubblesort("Bubble Sort"), insertionsort("Insertion Sort"), quicksort("Quick Sort");

        public final String name;

        private Algorithm (String prettyName) {
            this.name = prettyName;
        }
    }
}
