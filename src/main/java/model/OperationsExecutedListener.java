package model;

import java.util.List;

import contract.json.Operation;

/**
 * 
 * @author Richard Sundqvist
 *
 */
public interface OperationsExecutedListener {

    /**
     * Called when operations have been executed, altering the model.
     */
    public void operationsExecuted (List<Operation> executedOperations);
}