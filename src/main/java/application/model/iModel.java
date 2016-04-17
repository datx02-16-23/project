package application.model;

import wrapper.Operation;
import wrapper.datastructures.DataStructure;

import java.util.List;
import java.util.Map;

/**
 * The model for the visualization application, handles the operations and the datastructures and applies the operations as designated.
 */
public interface iModel {

    /**
     * Return the model to the state before any operations were applied
     */
    void reset ();

    /**
     * Move the model one step forward
     * @return if the step forward was executed succesfully
     */
    boolean stepForward ();

    /**
     * Move the model one step backward. Currently resets model and replays all operations up until current index -1
     * @return if backwards step was succesfull
     */
    boolean stepBackward ();

    /**
     * Set data for the model
     * @param structs to be modeled
     * @param ops to be applied to the structs
     */
    void set (Map<String, DataStructure> structs, List<Operation> ops);

    /**
     * Set the operations to be applied to the structs
     * @param operations to be applied
     */
    void setOperations (List<Operation> operations);

    /**
     * Returns a hashmap with the identifiers and the datastructures.
     * @return The map of data structures held by this model.
     */
    Map<String, DataStructure> getStructures ();

    /**
     * A list containing all the operations held by the model.
     * @return the list of operations held by this model.
     */
    List<Operation> getOperations ();

    /**
     * The current state of the datastructures given the current index contained in an iStep
     * @return the current step of this model.
     */
    iStep getCurrentStep ();

    /**
     * Return the current index of this model.
     * @return The current index of this model.
     */
    int getIndex ();

    /**
     * Jump to designated index
     * @param toStepNo index to go to
     */
    void goToStep (int toStepNo);

    /**
     * Apply all the operations on the datastructures.
     */
    void goToEnd ();
}
