package model2;

import java.util.List;

import contract.json.Operation;

/**
 * 
 * @author Richard Sundqvist
 *
 */
public interface ModelExecutionListener {

    /**
     * Called when operations have been executed, altering the model.
     */
    public void operationsExecuted (List<Operation> executedOperations);
}
