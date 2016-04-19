package application.assets;

import com.dennisjonsson.annotation.*;
import com.dennisjonsson.markup.AbstractType;

import application.gui.Main;

/**
 * Generates examples for a given array using the selected algorithm.<br>
 * <br>
 * <b>ACKNOWLEDGEMENTS:</b><br>
 * Insertion Sort: http://mathbits.com/MathBits/Java/arrays/InsertionSort.htm <br>
 * Bubble Sort: http://mathbits.com/MathBits/Java/arrays/Bubble.htm <br>
 * Merge Sort: http://algs4.cs.princeton.edu/14analysis/Mergesort.java.html <br>
 * 
 * @author Richard Sundqvist
 *
 */
@SourcePath(path = "C:/Users/Richard/Documents/datx02-16-23/Bachelor/src/main/java/application/assets/")
public abstract class Examples {

    private static double[] arg;
    private static String   json;

    /**
     * Run an algorithm.
     * 
     * @param algo The algorithm to load.
     * @param data The data to run it on.
     * @return The resulting operations of running the given algorithm on the data.
     */
    //@VisualizeArg(args = {"", AbstractType.ARRAY})
    public static String getExample (Algorithm algo, double[] data){
        arg = data;
        switch (algo) {
            case mergesort:
                getMergeSort(arg);
                break;
            case bubblesort:
                getBubbleSort(arg);
                break;
            case insertionsort:
                getInsertionSort(arg);
                break;
            case quicksort:
                getQuickSort(arg);
                break;
            default:
                Main.console.err("No such algorithm: " + algo);
                break;
        }
        // om du vill printa här
        print();
        return json;
    }

    /*
     denna måste vara definierad i någon annoterad klass i programmet 
     och kallas när du är färdig
    */
    @Print(path="")
    public static void print(){}

    /**
     * Generate a log file for Insertion Sort on the given data. <br>
     * <b>Source: </b> {@link http://mathbits.com/MathBits/Java/arrays/InsertionSort.htm}
     * 
     * 
     * @param data The data to run the algorithm on.
     * @return The operations performed by the algorithm on the given data.
     */
    @VisualizeArg(args = {AbstractType.ARRAY})
    public static void getInsertionSort (double[] data){
        int j; // the number of items sorted so far
        double key; // the item to be inserted
        int i;
        for (j = 1; j < data.length; j++) // Start with 1 (not 0)
        {
            key = data[j];
            for (i = j - 1; (i >= 0) && (data[i] < key); i--) // Smaller values are moving up
            {
                data[i + 1] = data[i];
            }
            data[i + 1] = key; // Put the key in its proper location
        }
    }

    /**
     * Generate a log file for Bubble Sort on the given data. <br>
     * <b>Source: </b> {@link http://mathbits.com/MathBits/Java/arrays/Bubble.htm}
     * 
     * @param data The data to run the algorithm on.
     * @return The operations performed by the algorithm on the given data.
     */
    @VisualizeArg(args = {AbstractType.ARRAY})
    public static void getBubbleSort (double[] data){
        int j;
        boolean flag = true; // set flag to true to begin first pass
        double temp; //holding variable
        while(flag) {
            flag = false; //set flag to false awaiting a possible swap
            for (j = 0; j < data.length - 1; j++) {
                if (data[j] < data[j + 1]) // change to > for ascending sort
                {
                    temp = data[j]; //swap elements
                    data[j] = data[j + 1];
                    data[j + 1] = temp;
                    flag = true; //shows a swap occurred 
                }
            }
        }
    }

    /**
     * Generate a log file for Merge Sort on the given data. <br>
     * <b>Source: </b> {@link http://algs4.cs.princeton.edu/14analysis/Mergesort.java.html}
     * 
     * @param data The data to run the algorithm on.
     * @return The operations performed by the algorithm on the given data.
     */
    
    public static void getMergeSort (double[] data){
        mergesort(data);
    }

    @VisualizeArg(args = {AbstractType.ARRAY, AbstractType.ARRAY})
    private static double[] merge (double[] a, double[] b){
        double[] c = new double[a.length + b.length];
        int i = 0, j = 0;
        for (int k = 0; k < c.length; k++) {
            if (i >= a.length)
                c[k] = b[j++];
            else if (j >= b.length)
                c[k] = a[i++];
            else if (a[i] <= b[j])
                c[k] = a[i++];
            else
                c[k] = b[j++];
        }
        return c;
    }
    
    
    @VisualizeArg(args = {AbstractType.ARRAY})
    private static double[] mergesort (double[] input){
        int N = input.length;
        if (N <= 1)
            return input;
        double[] a = new double[N / 2];
        double[] b = new double[N - N / 2];
        for (int i = 0; i < a.length; i++)
            a[i] = input[i];
        for (int i = 0; i < b.length; i++)
            b[i] = input[i + N / 2];
        return merge(mergesort(a), mergesort(b));
    }
    //End Merge Sort

    /**
     * Generate a log file for Quick Sort on the given data. <br>
     * <b>Source: </b> {@link }
     * 
     * @param data The data to run the algorithm on.
     * @return The operations performed by the algorithm on the given data.
     */
    public static void getQuickSort (double[] data){
        mergesort(data);
    }

    /**
     * The algorithms known to this Examples thingy.
     * 
     * @author Richard Sundqvist
     *
     */
    public enum Algorithm{
        mergesort("Merge Sort"), bubblesort("Bubble Sort"), insertionsort("Insertion Sort"), quicksort("Quick Sort");

        public final String name;

        private Algorithm (String name){
            this.name = name;
        }
    }
}