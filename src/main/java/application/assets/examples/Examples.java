package application.assets.examples;

//import com.dennisjonsson.annotation.*;
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

//@Include(classes = {"application.assets.examples.Insertion"})
//@SourcePath(path = "C:/Users/Richard/Documents/datx02-16-23/Bachelor/src/main/java/application/assets/")
public abstract class Examples {

    private static double[] arg;
    public static String   json;

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
                Merge.sort(arg);
                break;
            case bubblesort:
                Bubble.sort(arg);
                break;
            case insertionsort:
                Insertion.sort(arg);
                break;
//            case quicksort:
//                Quick.sort(arg);
//                break;
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
//    @Print(path="")
    public static void print(){}

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