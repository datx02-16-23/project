package application.model;

import wrapper.Operation;
import wrapper.datastructures.DataStructure;

import java.util.List;
import java.util.Map;

/**
 * The model for the visualization application, handles the operations and the datastructures and applies the operations
 * as designated.
 */
public interface iModel {

    /**
     * Restore the model to its inisual stet.
     */
    public void clear ();

    /**
     * Return the model to the state before any operations were applied
     */
    public void reset ();

    /**
     * Move the model one step forward
     * 
     * @return if the step forward was executed succesfully
     */
    public boolean stepForward ();

    /**
     * Move the model one step backward. Currently resets model and replays all operations up until current index -1
     * 
     * @return if backwards step was succesfull
     */
    public boolean stepBackward ();

    /**
     * Set data for the model
     * 
     * @param structs to be modeled
     * @param ops to be applied to the structs
     */
    public void set (Map<String, DataStructure> structs, List<Operation> ops);

    /**
     * Set the operations to be applied to the structs
     * 
     * @param operations to be applied
     */
    public void setOperations (List<Operation> operations);

    /**
     * Returns a hashmap with the identifiers and the datastructures.
     * 
     * @return The map of data structures held by this model.
     */
    public Map<String, DataStructure> getStructures ();

    /**
     * A list containing all the operations held by the model.
     * 
     * @return the list of operations held by this model.
     */
    public List<Operation> getOperations ();

    /**
     * The current state of the datastructures given the current index contained in an iStep
     * 
     * @return the current step of this model.
     */
    public iStep getCurrentStep ();

    /**
     * Return the current index of this model.
     * 
     * @return The current index of this model.
     */
    public int getIndex ();

    /**
     * Jump to designated index
     * 
     * @param toStepNo index to go to
     */
    public void goToStep (int toStepNo);

    /**
     * Apply all the operations on the datastructures.
     */
    public void goToEnd ();
}
