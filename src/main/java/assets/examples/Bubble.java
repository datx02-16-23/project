package assets.examples;

abstract class Bubble {
    /**
     * Generate a log file for Bubble Sort on the given data. <br>
     * <b>Source: </b> {@link http://mathbits.com/MathBits/Java/arrays/Bubble.htm}
     *
     * @param data
     *            The data to run the algorithm on.
     * @return The operations performed by the algorithm on the given data.
     */
    static void sort (double[] data) {
        int j;
        boolean flag = true; // set flag to true to begin first pass
        double tmp; // holding variable
        while (flag) {
            flag = false; // set flag to false awaiting a possible swap
            for (j = 0; j < data.length - 1; j++) {
                if (data [j] < data [j + 1]) // change to > for ascending sort
                {
                    tmp = data [j]; // swap elements
                    data [j] = data [j + 1];
                    data [j + 1] = tmp;
                    flag = true; // shows a swap occurred
                }
            }
        }
    }
}
