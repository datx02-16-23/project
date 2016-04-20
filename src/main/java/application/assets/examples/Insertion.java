package application.assets.examples;

import com.dennisjonsson.annotation.SourcePath;
import com.dennisjonsson.annotation.VisualizeArg;
import com.dennisjonsson.markup.AbstractType;

@SourcePath(path = "C:/Users/Richard/Documents/datx02-16-23/Bachelor/src/main/java/application/assets/examples")
class Insertion {

    /**
     * Generate a log file for Insertion Sort on the given data. <br>
     * <b>Source: </b> {@link http://mathbits.com/MathBits/Java/arrays/InsertionSort.htm}
     * 
     * 
     * @param data The data to run the algorithm on.
     * @return The operations performed by the algorithm on the given data.
     */
    @VisualizeArg(args = {AbstractType.ARRAY})
    static void sort (double[] data){
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
}
