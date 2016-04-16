package application.model;

import java.util.Map;

import wrapper.Operation;
import wrapper.datastructures.DataStructure;

/**
 * Interface for the model used to define the status of the visualization after each unique operation has been applied
 */
public interface iStep {

    /**
     * Get the current structures in the step
     * @return structures in a map of identifiers and datastructs
     */
    Map<String, DataStructure> getStructures ();

    /**
     * The last applied operation
     * @return
     */
    Operation getLastOp ();

    /**
     * Modify the step by applying an operation
     * @param op
     */
    void applyOperation (Operation op);

    /**
     * Reset the step back to the original state before any operations were applied
     */
    void reset ();
}
