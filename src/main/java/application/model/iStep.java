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
    public Map<String, DataStructure> getStructures ();

    /**
     * The last applied operation
     * @return The last applied operation.
     */
    public Operation getLastOp ();

    /**
     * Modify the step by applying an operation
     * @param op The operation to apply.
     */
    public void applyOperation (Operation op);

    /**
     * Reset the step back to the original state before any operations were applied
     */
    public void reset ();
}
