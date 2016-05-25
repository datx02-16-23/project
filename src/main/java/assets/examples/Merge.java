package assets.examples;

class Merge {

    /**
     * Generate a log file for Merge Sort on the given data. <br>
     * <b>Source: </b> {@link http://algs4.cs.princeton.edu/14analysis/Mergesort.java.html}
     *
     * @param data
     *            The data to run the algorithm on.
     * @return The operations performed by the algorithm on the given data.
     */
    static void sort (double[] data) {
        mergesort(data);
    }

    private static double[] merge (double[] a, double[] b) {
        double[] c = new double[a.length + b.length];
        int i = 0, j = 0;
        for (int k = 0; k < c.length; k++) {
            if (i >= a.length) {
                c [k] = b [j++];
            } else if (j >= b.length) {
                c [k] = a [i++];
            } else if (a [i] <= b [j]) {
                c [k] = a [i++];
            } else {
                c [k] = b [j++];
            }
        }
        return c;
    }

    private static double[] mergesort (double[] input) {
        int N = input.length;
        if (N <= 1) {
            return input;
        }
        double[] a = new double[N / 2];
        double[] b = new double[N - N / 2];
        for (int i = 0; i < a.length; i++) {
            a [i] = input [i];
        }
        for (int i = 0; i < b.length; i++) {
            b [i] = input [i + N / 2];
        }
        return merge(mergesort(a), mergesort(b));
    }
    // End Merge Sort
}
