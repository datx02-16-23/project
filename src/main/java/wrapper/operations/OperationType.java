package wrapper.operations;

/**
 * The name of the operation.
 */
public enum OperationType{
    message, read, write, swap(true);

    /**
     * True for operations which implement the Consolidable interface.
     */
    public boolean consolidable;

    OperationType (boolean consolidable){
        this.consolidable = consolidable;
    }
    
    OperationType (){
        this(false);
    }
}
