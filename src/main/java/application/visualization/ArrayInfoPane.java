package application.visualization;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import wrapper.datastructures.Array;

public class ArrayInfoPane extends StructureInfoPane {

    public ArrayInfoPane (Array a){
        super(a.identifier);
        /*
         * Number of elements
         */
        SimpleStringProperty numElements = new SimpleStringProperty("0");
        a.getElements().addListener(new InvalidationListener(){

            @Override
            //TODO: Action call only when elements are added or removed.
            public void invalidated (Observable observable){
                numElements.set(a.getElements().size()+"");
//                System.out.println("new num elements");
//                System.out.println("numElements = " + numElements);
            }
            
        });
        addRow("#Elements", numElements);
        /*
         * Number of elements
         */
        SimpleStringProperty numSwaps = new SimpleStringProperty("0");
        a.getNumSwaps().addListener(new InvalidationListener(){

            @Override
            public void invalidated (Observable observable){
                numSwaps.set(a.getNumSwaps().intValue()+"");
//                System.out.println("new num swaps");
//                System.out.println("numSwaps = " + numSwaps);
            }
            
        });
        addRow("#Swaps", numSwaps);
    }
}
